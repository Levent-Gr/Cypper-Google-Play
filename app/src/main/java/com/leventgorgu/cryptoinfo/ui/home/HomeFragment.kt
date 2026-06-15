package com.leventgorgu.cryptoinfo.ui.home

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView.OnQueryTextListener
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.leventgorgu.cryptoinfo.R
import com.leventgorgu.cryptoinfo.adapter.CryptoRecyclerAdapter
import com.leventgorgu.cryptoinfo.databinding.FragmentHomeBinding
import com.leventgorgu.cryptoinfo.roomdb.CryptoEntity
import com.leventgorgu.cryptoinfo.roomdb.CryptoFavoriteEntity
import com.leventgorgu.cryptoinfo.util.CoinTheme
import com.leventgorgu.cryptoinfo.util.Status
import com.leventgorgu.cryptoinfo.util.Util.LIMIT
import com.leventgorgu.cryptoinfo.util.bindChangePercent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel : HomeViewModel by viewModels()
    private var cryptoRecyclerAdapter = CryptoRecyclerAdapter(arrayListOf())
    private var cryptoFavoriteEntity = ArrayList<CryptoFavoriteEntity>()
    private val heroGold = CoinTheme.overrideFor("BTC") ?: 0xFFF7931A.toInt()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHero()
        subscribeObserve()
        homeViewModel.loadHeroChart()
    }

    override fun onResume() {
        super.onResume()

        homeViewModel.refreshData()

        binding.cryptoRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.cryptoRecyclerView.adapter = cryptoRecyclerAdapter

        binding.searchView.clearFocus()
        binding.searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                homeViewModel.searchCrypto(newText)
                return false
            }
        })

        binding.swipeRefreshCryptos.setOnRefreshListener {
            homeViewModel.getCryptosFromAPI(LIMIT)
        }
    }

    private fun subscribeObserve(){
        homeViewModel.cryptoListLiveDataFromSQl.observe(viewLifecycleOwner, Observer {
            if (it!=null){
                val cryptoList = it
                cryptoRecyclerAdapter.updateCryptosDataList(ArrayList(cryptoList))
                cryptoList.firstOrNull { c -> c.id == 1 }?.let { btc -> bindHero(btc) }
            }
        })
        homeViewModel.heroChart.observe(viewLifecycleOwner, Observer { prices ->
            setHeroChartData(prices)
        })
        homeViewModel.cryptosSearch.observe(viewLifecycleOwner, Observer {
            if (it!=null){
                val cryptoList = it
                cryptoRecyclerAdapter.updateCryptosDataList(cryptoList)
            }
        })
        homeViewModel.cryptos.observe(viewLifecycleOwner, Observer {
            when(it.status){
                Status.SUCCESS -> {
                    binding.progressCircular.visibility = View.GONE
                    it?.let { cryptoData ->
                        val data = cryptoData.data
                        cryptoRecyclerAdapter.updateCryptosDataList(data!!)
                        homeViewModel.insertCryptos(data)
                        binding.swipeRefreshCryptos.isRefreshing = false

                    }
                }
                Status.LOADING ->{
                    binding.swipeRefreshCryptos.isRefreshing = true
                    binding.progressCircular.visibility = View.VISIBLE
                }
                Status.ERROR ->{
                    binding.progressCircular.visibility = View.GONE
                    Toast.makeText(requireContext(),it.message ?: "Error", Toast.LENGTH_LONG).show()
                    binding.swipeRefreshCryptos.isRefreshing = false

                }
            }
        })
        homeViewModel.cryptoFavoritesEntity.observe(viewLifecycleOwner, Observer {
            it.let { cryptoFavorites ->
                cryptoFavoriteEntity = ArrayList(cryptoFavorites)
                cryptoRecyclerAdapter.refreshCryptoFavorites(cryptoFavoriteEntity)
            }
        })
    }

    private fun setupHero() {
        val surface = ContextCompat.getColor(requireContext(), R.color.surface)
        binding.heroCard.background = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(CoinTheme.blend(heroGold, surface, 0.28f), CoinTheme.blend(heroGold, surface, 0.06f))
        ).apply {
            cornerRadius = 20f * resources.displayMetrics.density
            setStroke(resources.displayMetrics.density.toInt(), CoinTheme.blend(heroGold, surface, 0.5f))
        }
        Glide.with(this)
            .load("https://s2.coinmarketcap.com/static/img/coins/128x128/1.png")
            .into(binding.heroLogo)
        binding.heroChart.apply {
            setTouchEnabled(false)
            setDrawGridBackground(false)
            description.isEnabled = false
            legend.isEnabled = false
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            xAxis.isEnabled = false
            setViewPortOffsets(0f, 6f, 0f, 0f)
            minOffset = 0f
        }
    }

    private fun bindHero(btc: CryptoEntity) {
        binding.heroPrice.text = btc.price
        bindChangePercent(binding.heroChange, btc.changePercent24h)
    }

    private fun setHeroChartData(prices: List<Float>) {
        if (prices.isEmpty() || _binding == null) return
        val entries = prices.mapIndexed { i, p -> Entry(i.toFloat(), p) }
        val set = LineDataSet(entries, "btc").apply {
            color = heroGold
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(ColorUtils.setAlphaComponent(heroGold, 80), ColorUtils.setAlphaComponent(heroGold, 0))
            )
        }
        binding.heroChart.data = LineData(set)
        binding.heroChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}