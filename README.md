# Seyahat Asistanım

 <img src="https://i.hizliresim.com/d5ixyhx.png" width="250" height="250">

## Proje Hakkında
**Seyahat Asistanım**, seyahat planlarınızı zahmetsizce organize etmenize yardımcı olan, yapay zeka destekli yenilikçi bir mobil uygulamadır. Kullanıcılar, uygulama üzerinden seyahat bilgilerini girdikten sonra, varış noktalarına dair hava durumu tahminleri, kişiselleştirilmiş kontrol listeleri ve yerel öneriler gibi detaylı bilgilere kolayca erişebilirler.

Seyahat Asistanım, OpenAI’nin ChatGPT-4O modelini kullanmaktadır.

## Kullanım Videosu

<a href="https://www.youtube.com/shorts/lgvJ-na1mtU?si=A2Is9vkUwP2Ap1fd"> <img src="https://i.hizliresim.com/o59t6g4.png" alt="Seyahat Asistanım Kullanım Videosu" width="320" height="680">  </a> 

## Özellikler

- **Hava Durumu Tahminleri:** Seyahat edeceğiniz tarihlerdeki varış noktanızın hava durumu tahminlerini detaylı olarak öğrenin.
- **Kişiselleştirilmiş Kontrol Listeleri:** Seyahat türünüz ve hava koşullarına göre yapay zeka tarafından özelleştirilmiş kontrol listeleri alın.
- **Yerel Bilgi ve Öneriler:** Varış noktanızdaki en popüler mekanlar, restoranlar, kültürel aktiviteler ve daha fazlası hakkında ayrıntılı bilgi edinin.
- **Yapay Zeka Destekli Sohbet:** Seyahat ChatBot'una sorularınızı sorun ve anında, kişiselleştirilmiş yanıtlar alın.
- **Kullanıcı Dostu Arayüz:** Seyahat planlamanızı kolaylaştıran, sade ve anlaşılır bir kullanıcı arayüzü ile uygulamayı rahatça kullanın.
- **Dil Desteği:** Uygulama Türkçe ve İngilizce dillerinde kullanım imkânı sunar, böylece dil bariyerini aşmanıza yardımcı olur.
- **Çevrimdışı Kullanım:** İlk kurulum sonrasında tüm özellikleri internet bağlantısı olmadan da kullanabilme imkânına sahip olun.

## Teknoloji Yığını (Tech Stack)
- **Kotlin (1.9.23)**
- **Jetpack Compose** (Latest Preview Beta Version [1.3.0-rc01] )
- **Android Jetpack**: Navigation, Flow, ViewModel, Room
- **Retrofit** & **OkHttp**
- **GSON**
- **Markdown Compose**
- **Firebase**: Analytics, Crashlytics
- **Chucker** (Network Debugging)
- **LeakCanary** (Memory Leak Detection)
- **ChatGPT API**
- **Coroutines** (Asynchronous Programming)
- **Lottie** (Animations)
- **Koin** (Dependency Injection)
- **OpenWeather API**
- **Ktlint** (Code Style)
- **Material Design**

## Yaklaşımlar (Approaches)
- **%100 Localization** (Türkçe, İngilizce)
- **SOLID Principles**
- **Clean Code**

## Mimari (Architect)
- **MVVM** (Model-View-ViewModel)

## Katmanlar (Layers)
- **Data Layer**: Veri yönetimi (Room Database, Retrofit)
- **Domain Layer**: İş mantığı (Use Cases, Repository Interface)
- **Presentation Layer**: Kullanıcı arayüzü (Compose, ViewModel)


## Proje Yapısı
```plaintext
com.erendogan6.seyahatasistanim
|
├── data
│   ├── local
│   │   ├── dao
│   │   │   ├── ChatMessageDao.kt          // Chat mesajları için veri erişim nesnesi (DAO)
│   │   │   ├── ChecklistDao.kt            // Kontrol listesi öğeleri için DAO
│   │   │   ├── LocalInfoDao.kt            // Yerel bilgiler için DAO
│   │   │   ├── TravelDao.kt               // Seyahat bilgileri için DAO
│   │   │   └── WeatherDao.kt              // Hava durumu verileri için DAO
│   │   └── database
│   │       └── TravelDatabase.kt          // Veritabanı yapılandırma sınıfı
│   ├── model
│   │   ├── dto
│   │   │   ├── chatGPT
│   │   │   │   ├── ChatGptRequest.kt      // ChatGPT API istek modelini temsil eder
│   │   │   │   ├── ChatGptResponse.kt     // ChatGPT API yanıt modelini temsil eder
│   │   │   │   ├── Choice.kt              // ChatGPT yanıt seçenekleri modeli
│   │   │   │   └── Message.kt             // ChatGPT mesaj modeli
│   │   │   └── weather
│   │   │       ├── City.kt                // Şehir bilgilerini içeren model
│   │   │       ├── LocalNames.kt          // Şehirlerin yerel adlarını temsil eden model
│   │   │       ├── Temperature.kt         // Sıcaklık bilgilerini içeren model
│   │   │       ├── Weather.kt             // Genel hava durumu modelini temsil eder
│   │   │       ├── WeatherApiResponse.kt  // Hava durumu API yanıt modelini temsil eder
│   │   │       └── WeatherForecast.kt     // Hava durumu tahmin modelini içerir
│   │   └── entity
│   │       ├── ChatMessageEntity.kt       // Chat mesajları için veritabanı varlık sınıfı
│   │       ├── ChecklistItemEntity.kt     // Kontrol listesi öğeleri için veritabanı varlık sınıfı
│   │       ├── LocalInfoEntity.kt         // Yerel bilgiler için veritabanı varlık sınıfı
│   │       ├── TravelEntity.kt            // Seyahat bilgileri için veritabanı varlık sınıfı
│   │       └── WeatherEntity.kt           // Hava durumu verileri için veritabanı varlık sınıfı
│   ├── remote
│   │   ├── ChatGptApiService.kt           // ChatGPT API servis tanımı
│   │   ├── CityApiService.kt              // Şehir verileri için API servis tanımı
│   │   └── WeatherApiService.kt           // Hava durumu API servis tanımı
│   └── repository
│       ├── ChatGptRepositoryImpl.kt       // ChatGPT API verileri için repository implementasyonu
│       ├── ChecklistRepositoryImpl.kt     // Kontrol listesi verileri için repository implementasyonu
│       ├── LocalInfoRepositoryImpl.kt     // Yerel bilgiler için repository implementasyonu
│       ├── TravelRepositoryImpl.kt        // Seyahat verileri için repository implementasyonu
│       └── WeatherRepositoryImpl.kt       // Hava durumu verileri için repository implementasyonu
|
├── di
│   ├── DatabaseModule.kt                  // Veritabanı bağımlılık enjeksiyon modülü
│   ├── NetworkModule.kt                   // Ağ servisleri için bağımlılık enjeksiyon modülü
│   ├── RepositoryModule.kt                // Repository'ler için bağımlılık enjeksiyon modülü
│   ├── SeyahatAsistaniApp.kt              // Uygulama sınıfı (Application)
│   ├── UseCaseModule.kt                   // Use case'ler için bağımlılık enjeksiyon modülü
│   └── ViewModelModule.kt                 // ViewModel'ler için bağımlılık enjeksiyon modülü
|
├── domain
│   ├── repository
│   │   ├── ChatGptRepository.kt           // ChatGPT API verileri için repository arayüzü
│   │   ├── ChecklistRepository.kt         // Kontrol listesi verileri için repository arayüzü
│   │   ├── LocalInfoRepository.kt         // Yerel bilgiler için repository arayüzü
│   │   ├── TravelRepository.kt            // Seyahat verileri için repository arayüzü
│   │   └── WeatherRepository.kt           // Hava durumu verileri için repository arayüzü
│   └── usecase
│       ├── AddChecklistItemUseCase.kt     // Kontrol listesi öğesi ekleme use case'i
│       ├── DeleteChecklistItemUseCase.kt  // Kontrol listesi öğesi silme use case'i
│       ├── GetAllChatMessagesUseCase.kt   // Tüm chat mesajlarını getirme use case'i
│       ├── GetCitySuggestionsUseCase.kt   // Şehir önerilerini getirme use case'i
│       ├── GetLastTravelInfoUseCase.kt    // Son seyahat bilgilerini getirme use case'i
│       ├── GetLocalInfoUseCase.kt         // Yerel bilgileri getirme use case'i
│       ├── GetSuggestionsUseCase.kt       // ChatGPT'den öneri alma use case'i
│       ├── GetWeatherDataUseCase.kt       // Hava durumu verilerini getirme use case'i
│       ├── GetWeatherForecastUseCase.kt   // Hava durumu tahminlerini getirme use case'i
│       ├── LoadChecklistItemsUseCase.kt   // Kontrol listesi öğelerini yükleme use case'i
│       ├── SaveChatMessageUseCase.kt      // Chat mesajını kaydetme use case'i
│       ├── SaveChecklistItemsUseCase.kt   // Kontrol listesi öğelerini kaydetme use case'i
│       ├── SaveLocalInfoUseCase.kt        // Yerel bilgileri kaydetme use case'i
│       ├── SaveTravelInfoUseCase.kt       // Seyahat bilgilerini kaydetme use case'i
│       ├── SaveWeatherDataUseCase.kt      // Hava durumu verilerini kaydetme use case'i
│       └── ToggleItemCompletionUseCase.kt // Kontrol listesi öğesi tamamlama durumunu değiştirme use case'i
|
├── extension
│   ├── CapitalizeWords.kt                 // Metin işlemleri için genişletme fonksiyonu
│   └── WeatherExtensions.kt               // Hava durumu verileri için genişletme fonksiyonları
|
├── presentation
│   ├── theme
│   │   ├── Color.kt                       // Uygulama renk teması
│   │   ├── Theme.kt                       // Genel tema yapılandırması
│   │   └── Type.kt                        // Yazı tipi yapılandırması
│   ├── ui
│   │   ├── components
│   │   │   └── LottieLoadingScreen.kt     // Yükleme ekranı bileşeni (Lottie animasyonlu)
│   │   └── screens
│   │       ├── AppNavigation.kt           // Uygulama navigasyonu
│   │       ├── ChatGptScreen.kt           // ChatGPT ile etkileşim ekranı
│   │       ├── CheckListScreen.kt         // Seyahat kontrol listesi ekranı
│   │       ├── HomeScreen.kt              // Ana ekran
│   │       ├── LocalInfoScreen.kt         // Yerel bilgi ekranı
│   │       ├── TravelInfoScreen.kt        // Seyahat bilgileri ekranı
│   │       └── WeatherDetailScreen.kt     // Hava durumu detay ekranı
│   └── viewmodel
│       ├── ChatGptViewModel.kt            // ChatGPT işlemleri için ViewModel
│       ├── TravelViewModel.kt             // Seyahat bilgileri için ViewModel
│       ├── WeatherViewModel.kt            // Hava durumu verileri için ViewModel
|
└── utils
    ├── Converters.kt                      // Veri dönüşümleri için yardımcı sınıf
    └── DateUtils.kt                       // Tarih işlemleri için yardımcı sınıf
```

## Test Edilen Sürümler
- Android 9.0
- Android 10.0
- Android 11.0
- Android 12.0
- Android 13.0
- Android 14.0

## Kurulum

- Uygulamayı kullanmak için öncelikle Android Studio'yu bilgisayarınıza kurmanız gerekmektedir. Daha sonra aşağıdaki adımları takip edebilirsiniz:
- Bu repoyu yerel makinenize klonlayın:
```bash
git clone https://github.com/erendogan6/SeyahatAsistanim.git
```
- Android Studio'yu açın ve "Open an existing project" seçeneğini kullanarak indirdiğiniz projeyi seçin.
- Projeyi açtıktan sonra gereken bağımlılıkların indirilmesini bekleyin.
- Gerekli API'leri local.properties içerisine girin.
- Uygulamayı bir Android cihazda veya emülatörde çalıştırın.

## Katkıda Bulunma ##

Projeye katkıda bulunmak isteyenler için katkı kuralları ve adımları CONTRIBUTING.md dosyasında açıklanmıştır.

##  Lisans ## 
Bu proje MIT Lisansı altında lisanslanmıştır.
