package org.example.project.di

import com.russhwolf.settings.Settings
import org.example.project.data.local.MongoImpl
import org.example.project.data.local.PreferenceImpl
import org.example.project.data.remote.api.CurrencyApiServiceImpl
import org.example.project.domain.CurrencyApiService
import org.example.project.domain.MongoRepository
import org.example.project.domain.PreferenceRepository
import org.example.project.presentation.screen.HomeViewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

val appModule = module {
    single { Settings() }
    single<MongoRepository> { MongoImpl() }
    single<PreferenceRepository> { PreferenceImpl(settings = get()) }
    single<CurrencyApiService> { CurrencyApiServiceImpl(preferenceRepository = get()) }
    factory {
        HomeViewModel(
            preferenceRepository = get(),
            mongoRepository = get(),
            currencyApiService = get()
        )
    }
}

fun initializeKoin() = startKoin {
    modules(appModule)
}