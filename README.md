# Cypper

This is Cypper app.You can add the cryptocurrencies yo want to fallow closely to your favorites and control them with a single click.

## Setup

The app uses the CoinMarketCap Pro API. The API key is **not** stored in source control; it is read from `local.properties` at build time.

1. Get a key from the [CoinMarketCap Developer Portal](https://pro.coinmarketcap.com/).
2. Add this line to your `local.properties` (the file is git-ignored):

   ```properties
   CMC_API_KEY=your_api_key_here
   ```

3. Build the project. The key is exposed in code as `BuildConfig.CMC_API_KEY`.

## Libraries And Tools

- Used Language Kotlin
- MVVM Architecture
- Retrofit
- DI-Hilt
- Room Database
- Jetpack Navigation
- Coroutines
- View Binding
- Data Binding
- ViewModel
- Glide
- LiveData
- RecyclerView
- JUnit4
- Truth
- Hilt Testing
- Room Testing
- Coroutines Test

### UI/UX Designer
#### Behance
https://www.behance.net/sercanbarisguldez
#### Linkedin
https://www.linkedin.com/in/sercanbarisguldez/

### Google Play Link
https://play.google.com/store/apps/details?id=com.leventgorgu.cryptoinfo&hl=tr&gl=US


## Logo
![Logo](https://raw.githubusercontent.com/Levent-Gr/CryptoInfo/master/screenShots/logo/kapakCypper.png)

![Logo](https://raw.githubusercontent.com/Levent-Gr/CryptoInfo/master/screenShots/logo/512logo.png)


### First fragment
<img src = https://raw.githubusercontent.com/Levent-Gr/CryptoInfo/master/screenShots/first_fragment.png height="660" width="330"  />

### Home fragment
<img src = https://raw.githubusercontent.com/Levent-Gr/CryptoInfo/master/screenShots/home_fragment.png height="660" width="330"  />

### Home fragment search
<img src = https://raw.githubusercontent.com/Levent-Gr/CryptoInfo/master/screenShots/home_fragment_seach.png height="660" width="330"  />

### Home fragment search white
<img src = https://raw.githubusercontent.com/Levent-Gr/CryptoInfo/master/screenShots/home_fragment_seach_white.png height="660" width="330"  />

### Detail fragment 
<img src = https://raw.githubusercontent.com/Levent-Gr/CryptoInfo/master/screenShots/detail_fragment.png height="660" width="330"  />

### Favorites fragment 
<img src = https://raw.githubusercontent.com/Levent-Gr/CryptoInfo/master/screenShots/favorites_fragment.png height="660" width="330"  />

### Detail fragment tether
<img src = https://raw.githubusercontent.com/Levent-Gr/CryptoInfo/master/screenShots/detail_fragment_tether.png height="660" width="330"  />
