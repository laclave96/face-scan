package com.savent.recognition.face

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.savent.recognition.face.data.local.database.AppDatabase
import com.savent.recognition.face.data.local.datasource.*
import com.savent.recognition.face.data.local.model.AppPreferences
import com.savent.recognition.face.data.remote.datasource.*
import com.savent.recognition.face.data.remote.service.CompaniesApiService
import com.savent.recognition.face.data.remote.service.EmployeesApiService
import com.savent.recognition.face.data.remote.service.PlaceApiService
import com.savent.recognition.face.data.remote.service.PresencesApiService
import com.savent.recognition.face.data.repository.CompanyRepositoryImpl
import com.savent.recognition.face.data.repository.EmployeeRepositoryImpl
import com.savent.recognition.face.data.repository.PresenceRepositoryImpl
import com.savent.recognition.face.domain.repository.CompanyRepository
import com.savent.recognition.face.domain.repository.EmployeeRepository
import com.savent.recognition.face.domain.repository.PresenceRepository
import com.savent.recognition.face.domain.usecase.*
import com.savent.recognition.face.presentation.model.PersonData
import com.savent.recognition.face.presentation.viewmodel.AddFaceVectorViewModel
import com.savent.recognition.face.presentation.viewmodel.FaceDetectionViewModel
import com.savent.recognition.face.presentation.viewmodel.MainViewModel
import com.savent.recognition.face.presentation.viewmodel.SendPresenceViewModel
import com.savent.recognition.face.utils.Mappers
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.security.cert.CertificateException

private val Context.datastore: DataStore<Preferences> by preferencesDataStore(AppConstants.APP_PREFERENCES)

val baseModule = module {

    single { Gson() }

    fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
        return try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts: Array<TrustManager> = arrayOf<TrustManager>(
                object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }

                }
            )

            // Install the all-trusting trust manager
            val sslContext: SSLContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }
            builder.addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", AppConstants.AUTHORIZATION)
                    .build()
                chain.proceed(request)
            }
            builder
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    single<OkHttpClient> {
        getUnsafeOkHttpClient().build()
    }

    single {
        Room.databaseBuilder(
            androidApplication(),
            AppDatabase::class.java,
            AppConstants.APP_DATABASE_NAME
        ).build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(AppConstants.SAVENT_FACE_RECOGNITION_API_BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<AppPreferencesLocalDatasource> {
        AppPreferencesLocalDatasourceImpl(
            DataObjectStorage<AppPreferences>(
                get(),
                object : TypeToken<AppPreferences>() {}.type,
                androidContext().datastore,
                stringPreferencesKey((AppConstants.APP_PREFERENCES))
            )
        )
    }
}

val employeeDataModule = module {
    includes(baseModule)

    single {
        get<AppDatabase>().employeeDao()
    }

    single<EmployeeLocalDataSource> {
        EmployeeLocalDataSourceImpl(get())
    }

    single<EmployeesApiService> {
        get<Retrofit>().create(EmployeesApiService::class.java)
    }

    single<EmployeeRemoteDataSource> {
        EmployeeRemoteDataSourceImpl(get())
    }

    single<EmployeeRepository> {
        EmployeeRepositoryImpl(get(), get(), Mappers::getPersonMapper)
    }

}

val companyDataModule = module {
    includes(baseModule)

    single {
        get<AppDatabase>().companyDao()
    }

    single<CompanyLocalDataSource> {
        CompanyLocalDataSourceImpl(get())
    }

    single<CompaniesApiService> {
        get<Retrofit>().create(CompaniesApiService::class.java)
    }

    single<CompanyRemoteDataSource> {
        CompanyRemoteDataSourceImpl(get())
    }

    single<CompanyRepository> {
        CompanyRepositoryImpl(get(), get(), Mappers::getCompanyMapper)
    }

}

val presenceDataModule = module {
    includes(baseModule)


    single<PresencesApiService> {
        get<Retrofit>().create(PresencesApiService::class.java)
    }

    single<PresenceRemoteDataSource> {
        PresenceRemoteDataSourceImpl(get())
    }

    single<PresenceRepository> {
        PresenceRepositoryImpl(get())
    }

}

val detectedPersonsDataModule = module {
    single<LastDetectedPersonsLocalDataSource> {
        LastDetectedPersonsLocalDataSourceImpl(
            DataObjectStorage<List<PersonData>>(
                get(), object : TypeToken<List<PersonData>>() {}.type, androidContext().datastore,
                stringPreferencesKey((AppConstants.LAST_DETECTED_PERSONS))
            )
        )
    }
}

val dataModule = module {
    includes(employeeDataModule, presenceDataModule, companyDataModule, detectedPersonsDataModule)
}

val mainModule = module {
    includes(companyDataModule)

    viewModel {
        MainViewModel(androidApplication() as MyApplication, get(), get())
    }
}

val faceDetectionModule = module {
    includes(employeeDataModule, detectedPersonsDataModule)

    single {
        GetPersonIfExistsLocallyUseCase()
    }
    single {
        GetSavedPersonsUseCase(get())
    }
    single {
        GetDetectedFacesUseCase(get(), get())
    }
    single {
        SaveLocallyDetectedPersonsUseCase(get())
    }

    viewModel {
        FaceDetectionViewModel(androidApplication() as MyApplication, get(), get(), get(), get())
    }
}

val addEmployeeModule = module {
    includes(employeeDataModule)

    viewModel { AddFaceVectorViewModel(androidApplication() as MyApplication, get(), get()) }
}

val sendPresenceModule = module {
    includes(presenceDataModule, employeeDataModule, detectedPersonsDataModule)

    single<PlaceApiService> {
        Retrofit.Builder()
            .baseUrl(AppConstants.BIG_DATA_CLOUD_API_BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(PlaceApiService::class.java)
    }

    single<PlaceRemoteDataSource> {
        PlaceRemoteDataSourceImpl(get())
    }

    single {
        GetLastDetectedPersonsUseCase(get(), get())
    }

    single {
        GetPlaceByCoordinatesUseCase(get())
    }

    single {
        GetPresencesUseCase(get(), get())
    }

    viewModel {
        SendPresenceViewModel(
            androidApplication() as MyApplication,
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}

val appModule = module {
    includes(mainModule, faceDetectionModule, addEmployeeModule, sendPresenceModule)
}