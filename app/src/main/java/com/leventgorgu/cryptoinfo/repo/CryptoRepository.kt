package com.leventgorgu.cryptoinfo.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.leventgorgu.cryptoinfo.api.CryptoAPI
import com.leventgorgu.cryptoinfo.model.cryptoInfo.CryptoInfo
import com.leventgorgu.cryptoinfo.model.cryptos.Cryptos
import com.leventgorgu.cryptoinfo.roomdb.CryptoDao
import com.leventgorgu.cryptoinfo.roomdb.CryptoDetailEntity
import com.leventgorgu.cryptoinfo.roomdb.CryptoEntity
import com.leventgorgu.cryptoinfo.roomdb.CryptoFavoriteEntity
import com.leventgorgu.cryptoinfo.util.APIResult
import com.leventgorgu.cryptoinfo.util.Util.API_KEY
import java.lang.Exception
import java.text.DecimalFormat
import javax.inject.Inject

class CryptoRepository @Inject constructor(private val cryptoAPI: CryptoAPI,private val cryptoDao: CryptoDao):CryptoRepositoryInterface {

    override suspend fun insertCrypto(cryptoList: ArrayList<CryptoEntity>) {
        cryptoDao.insertCrypto(*cryptoList.toTypedArray())
    }

    override suspend fun insertCryptoDetail(cryptoDetailEntity: CryptoDetailEntity) {
        cryptoDao.insertCryptoDetail(cryptoDetailEntity)
    }

    override  fun getCryptos(): LiveData<List<CryptoEntity>> {
        return cryptoDao.getAllCrypto()
    }

    override fun getAllFavoriteCryptos(): LiveData<List<CryptoFavoriteEntity>> {
        return cryptoDao.getAllFavoriteCryptos()
    }

    override suspend fun insertCryptoFavorite(cryptoFavoriteEntity: CryptoFavoriteEntity) {
        return cryptoDao.insertCryptoFavorite(cryptoFavoriteEntity)
    }

    override suspend fun deleteCryptoFavorite(cryptoFavoriteEntity: CryptoFavoriteEntity) {
        return cryptoDao.deleteCryptoFavorite(cryptoFavoriteEntity)
    }

    override suspend fun searchCrypto(cryptoName: String): List<CryptoEntity> {
        return cryptoDao.searchCrypto(cryptoName)
    }

    override suspend fun getCryptoWithId(cryptoId: Int): CryptoEntity {
        return cryptoDao.getCryptoWithId(cryptoId)
    }

    override suspend fun getCryptoDetailEntity(cryptoDetailId: Int): CryptoDetailEntity {
        return cryptoDao.getCryptoDetailEntity(cryptoDetailId)
    }

    override suspend fun getCryptoDataFromAPI(limit: String): APIResult<ArrayList<CryptoEntity>> {
        return try {
            val response = cryptoAPI.getCryptos(API_KEY,limit)
            if (response.isSuccessful){
                response.body()?.let {  cryptos ->
                    val data = getCryptoEntityData(cryptos)
                    if (data!=null){
                        return@let APIResult.success(data)
                    }
                    return APIResult.error("Error",null)

                }?: return APIResult.error("Error",null)
            }else{
                return APIResult.error("Error",null)
            }
        }catch (e : Exception){
            return APIResult.error(e.localizedMessage?:"Error",null )
        }
    }

    override suspend fun getCryptoInfoDataFromAPI(symbol: String): APIResult<CryptoInfo> {
        return try {
            val response = cryptoAPI.getCryptoInfo(API_KEY,symbol)
            if (response.isSuccessful){
                response.body()?.let {  cryptoInfo ->
                    return@let APIResult.success(cryptoInfo)
                }?: return APIResult.error("Error",null)
            }else{
                return APIResult.error("Error",null)
            }
        }catch (e : Exception){
            return APIResult.error(e.localizedMessage?:"Error",null )
        }
    }

    override fun getCryptoEntityData(cryptos: Cryptos):ArrayList<CryptoEntity>{
        val cryptoEntityList = ArrayList<CryptoEntity>(arrayListOf())
        val cryptoData = cryptos.data
        for (crypto in cryptoData){
            val id = crypto.id
            val name = crypto.name
            val rank = crypto.cmc_rank ?: 0

            val price = crypto.quote.USD.price ?: 0.0
            val formatter = DecimalFormat("$##,###.##")
            var priceString = formatter.format(price)
            priceString = priceString.substring(0, 1) + " " + priceString.substring(1, priceString.length);
            if (priceString.endsWith("00")) {
                val i = priceString.lastIndexOf("00")
                priceString = StringBuilder(priceString).replace(i, i + 2, "-").toString()
            }

            val symbol = crypto.symbol
            val platform = crypto.platform?.name ?: ""
            val cryptoEntity = CryptoEntity(id,name,symbol,rank,priceString,platform)
            cryptoEntityList.add(cryptoEntity)
        }
        return cryptoEntityList
    }

}