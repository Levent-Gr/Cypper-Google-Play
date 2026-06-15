package com.leventgorgu.cryptoinfo.ui.cryptodetail


import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.gson.Gson
import com.leventgorgu.cryptoinfo.R
import com.leventgorgu.cryptoinfo.databinding.FragmentCryptoDetailBinding
import com.leventgorgu.cryptoinfo.model.cryptoInfo.CryptoDetail
import com.leventgorgu.cryptoinfo.model.cryptoInfo.CryptoInfo
import com.leventgorgu.cryptoinfo.roomdb.CryptoDetailEntity
import com.leventgorgu.cryptoinfo.roomdb.CryptoEntity
import com.leventgorgu.cryptoinfo.roomdb.CryptoFavoriteEntity
import com.leventgorgu.cryptoinfo.util.CoinTheme
import com.leventgorgu.cryptoinfo.util.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.json.JSONArray
import org.json.JSONObject


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class CryptoDetailFragment : Fragment() {

    private var _binding: FragmentCryptoDetailBinding? = null
    private val binding get() = _binding!!
    private val cryptoDetailViewModel: CryptoDetailViewModel by viewModels()
    private var cryptoId: Int = 0
    private var cryptoSymbol: String = ""
    private var cryptofavorite: Boolean = false
    private lateinit var cryptoEntity: CryptoEntity
    private var themeApplied = false
    private var originalStatusBarColor: Int = 0
    private var coinColor: Int? = null
    private var chartPrices: List<Float>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            cryptoId = CryptoDetailFragmentArgs.fromBundle(it).id
            cryptoSymbol = CryptoDetailFragmentArgs.fromBundle(it).symbol
            cryptofavorite = CryptoDetailFragmentArgs.fromBundle(it).favorite
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCryptoDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        originalStatusBarColor = requireActivity().window.statusBarColor

        cryptoDetailViewModel.getCryptoDetails(cryptoId)
        cryptoDetailViewModel.getCryptoDetailEntity(cryptoId)
        observeSubscribe()

        binding.backButton.setOnClickListener { findNavController().popBackStack() }

        binding.saveCryptoButton.text =
            if (cryptofavorite) getString(R.string.remove_from_favorites)
            else getString(R.string.save_to_favorites)

        binding.saveCryptoButton.setOnClickListener { saveAndRemoveCrypto() }

        // Adaptive theming: recolor the whole screen to the selected coin's brand color.
        applyCoinTheme(cryptoSymbol, cryptoId)

        // Per-coin 7-day price chart (CoinGecko), drawn in the coin's color.
        setupDetailChart()
        cryptoDetailViewModel.loadChart(cryptoSymbol)
        cryptoDetailViewModel.detailChart.observe(viewLifecycleOwner) { prices ->
            chartPrices = prices
            maybeDrawChart()
        }
    }

    private fun applyCoinTheme(symbol: String, id: Int) {
        if (themeApplied) return
        val override = CoinTheme.overrideFor(symbol)
        if (override != null) {
            themeApplied = true
            applyAccent(override)
            return
        }
        val url = "https://s2.coinmarketcap.com/static/img/coins/128x128/$id.png"
        Glide.with(this).asBitmap().load(url).into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                if (_binding == null || themeApplied) return
                themeApplied = true
                val fallback = ContextCompat.getColor(requireContext(), R.color.accent)
                applyAccent(CoinTheme.fromBitmap(resource, fallback))
            }

            override fun onLoadCleared(placeholder: Drawable?) {}
        })
    }

    private fun applyAccent(coin: Int) {
        val b = _binding ?: return
        val ctx = requireContext()
        val bg = ContextCompat.getColor(ctx, R.color.bg)
        val surface = ContextCompat.getColor(ctx, R.color.surface)
        val tintTop = CoinTheme.blend(coin, bg, 0.22f)

        // Ambient background: coin tint at the top fading into the base, revealed smoothly.
        b.ambientBg.background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(tintTop, bg)
        )
        b.ambientBg.alpha = 0f
        b.ambientBg.animate().alpha(1f).setDuration(450).start()

        // Hero card recolored to the coin.
        b.heroCard.background = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(CoinTheme.blend(coin, surface, 0.30f), CoinTheme.blend(coin, surface, 0.08f))
        ).apply {
            cornerRadius = dpF(20f)
            setStroke(dpI(1), CoinTheme.blend(coin, surface, 0.55f))
        }

        // Save button in the coin accent.
        b.saveCryptoButton.background = GradientDrawable().apply {
            cornerRadius = dpF(26f)
            setColor(coin)
        }
        b.saveCryptoButton.setTextColor(CoinTheme.textOn(coin))

        requireActivity().window.statusBarColor = tintTop

        coinColor = coin
        maybeDrawChart()
    }

    private fun setupDetailChart() {
        binding.detailChart.apply {
            setTouchEnabled(false)
            setDrawGridBackground(false)
            description.isEnabled = false
            legend.isEnabled = false
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            xAxis.isEnabled = false
            setViewPortOffsets(0f, 8f, 0f, 8f)
            minOffset = 0f
        }
    }

    private fun maybeDrawChart() {
        val b = _binding ?: return
        val lineColor = coinColor ?: return
        val prices = chartPrices ?: return
        if (prices.isEmpty()) return
        val entries = prices.mapIndexed { i, p -> Entry(i.toFloat(), p) }
        val set = LineDataSet(entries, "price").apply {
            color = lineColor
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(ColorUtils.setAlphaComponent(lineColor, 80), ColorUtils.setAlphaComponent(lineColor, 0))
            )
        }
        b.detailChart.data = LineData(set)
        b.detailChart.visibility = View.VISIBLE
        b.detailChart.invalidate()
    }

    private fun dpF(value: Float) = value * resources.displayMetrics.density
    private fun dpI(value: Int) = (value * resources.displayMetrics.density).toInt()


    private fun saveAndRemoveCrypto() {
        val id = cryptoEntity.id
        val name = cryptoEntity.name
        val symbol = cryptoEntity.symbol
        val rank = cryptoEntity.cmcRank

        val cryptoFavorite = CryptoFavoriteEntity(id, name, symbol, rank)

        if (cryptofavorite) {
            val result = cryptoDetailViewModel.deleteCryptoFavorite(cryptoFavorite)
            when (result.status) {
                Status.SUCCESS -> {
                    Toast.makeText(context, "Crypto removed from favorites", Toast.LENGTH_LONG).show()
                    binding.saveCryptoButton.text = getString(R.string.save_to_favorites)
                    cryptofavorite = false
                }
                Status.ERROR -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        } else {
            val result = cryptoDetailViewModel.insertCryptoFavorite(cryptoFavorite)
            when (result.status) {
                Status.SUCCESS -> {
                    Toast.makeText(context, "Crypto saved to favorites", Toast.LENGTH_LONG).show()
                    binding.saveCryptoButton.text = getString(R.string.remove_from_favorites)
                    cryptofavorite = true
                }
                Status.ERROR -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun observeSubscribe() {
        cryptoDetailViewModel.selectedCryptoDetail.observe(viewLifecycleOwner, Observer {
            it.let {
                binding.selectedCrypto = it
                cryptoEntity = it
                binding.saveCryptoButton.text =
                    if (cryptofavorite) getString(R.string.remove_from_favorites)
                    else getString(R.string.save_to_favorites)
            }
        })
        cryptoDetailViewModel.cryptoDetailEntity.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                binding.cryptoDetailEntity = it
            } else {
                cryptoDetailViewModel.getCryptoInfoFromAPI(cryptoSymbol)
            }
        })
        cryptoDetailViewModel.cryptoInfo.observe(viewLifecycleOwner, Observer { cryptoInfo ->
            when (cryptoInfo.status) {
                Status.SUCCESS -> {
                    parseData(cryptoInfo.data!!)
                    binding.progressBar.visibility = View.GONE
                }
                Status.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                Status.ERROR -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), cryptoInfo.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun parseData(it: CryptoInfo) {
        val gson = Gson()
        val json = gson.toJson(it?.data)
        val jsonObject = JSONObject(json)
        val jsonArray = jsonObject[cryptoSymbol] as JSONArray

        val cryptoData = gson.fromJson(jsonArray.getJSONObject(0).toString(), CryptoDetail::class.java)

        val cryptoId = cryptoData.id
        val cryptoName = cryptoData.name
        val cryptoCategory = cryptoData.category
        val cryptoDescription = cryptoData.description
        val cryptoSymbol = cryptoData.symbol

        binding.cryptoDescription.text = cryptoDescription
        binding.cryptoCategory.text = cryptoCategory

        val cryptoDetailEntity = CryptoDetailEntity(cryptoId, cryptoCategory, cryptoDescription, cryptoName, cryptoSymbol)

        cryptoDetailViewModel.insertSelectedCryptoDetail(cryptoDetailEntity)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.statusBarColor = originalStatusBarColor
        _binding = null
    }
}
