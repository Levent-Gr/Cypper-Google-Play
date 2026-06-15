package com.leventgorgu.cryptoinfo.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView.OnQueryTextListener
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.leventgorgu.cryptoinfo.adapter.CryptoRecyclerAdapter
import com.leventgorgu.cryptoinfo.databinding.FragmentHomeBinding
import com.leventgorgu.cryptoinfo.roomdb.CryptoFavoriteEntity
import com.leventgorgu.cryptoinfo.util.Status
import com.leventgorgu.cryptoinfo.util.Util.LIMIT
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
        subscribeObserve()
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
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}