package com.pertamina.absensiapplication.di

import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.pertamina.absensiapplication.R
import com.pertamina.absensiapplication.api.PermitApi
import com.pertamina.absensiapplication.repository.PermitRepository
import com.pertamina.absensiapplication.viewmodel.PermitViewModel
import com.pertamina.absensiapplication.viewmodel.UserViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    factory { OkHttpClient.Builder().build() }
    single {
        Retrofit.Builder()
            .client(get())
            .baseUrl("https://absensi-app-3449e.firebaseapp.com/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
    }

    factory { get<Retrofit>().create(PermitApi::class.java) }
}

val repositoryModule = module {
    factory { PermitRepository(get()) }
}

val viewModelModule = module {
    viewModel { PermitViewModel(get(), get()) }
    viewModel { UserViewModel(androidContext().resources.getStringArray(R.array.organicPosition), get(), get()) }
}

val firebaseModule = module {
    factory { FirebaseAuth.getInstance() }
}


val appComponent = listOf(networkModule, viewModelModule, repositoryModule, firebaseModule)