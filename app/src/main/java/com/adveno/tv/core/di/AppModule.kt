package com.adveno.tv.core.di

import com.adveno.tv.core.database.AdvenoDatabaseHelper
import com.adveno.tv.core.datastore.DevicePreferences
import com.adveno.tv.core.kiosk.KioskManager
import com.adveno.tv.core.network.ConnectivityMonitor
import com.adveno.tv.core.network.DeviceCredentialStore
import com.adveno.tv.core.network.NetworkClientProvider
import com.adveno.tv.core.network.NetworkConnectivityMonitor
import com.adveno.tv.core.telemetry.HeartbeatMonitor
import com.adveno.tv.data.device.DeviceRepositoryImpl
import com.adveno.tv.data.media.MediaRepositoryImpl
import com.adveno.tv.data.pairing.MockPairingRepositoryImpl
import com.adveno.tv.data.pairing.RealPairingRepositoryImpl
import com.adveno.tv.data.pairing.network.PairingApiClient
import com.adveno.tv.data.playlist.SqlitePlaylistRepositoryImpl
import com.adveno.tv.data.synchronization.SynchronizationRepositoryImpl
import com.adveno.tv.domain.repository.DeviceRepository
import com.adveno.tv.domain.repository.MediaRepository
import com.adveno.tv.domain.repository.PairingRepository
import com.adveno.tv.domain.repository.PlaylistRepository
import com.adveno.tv.domain.repository.SynchronizationRepository
import com.adveno.tv.domain.usecase.DetermineAppLaunchDestinationUseCase
import com.adveno.tv.feature.device.DeviceStatusViewModel
import com.adveno.tv.feature.diagnostics.DiagnosticsViewModel
import com.adveno.tv.feature.initialization.InitializationViewModel
import com.adveno.tv.feature.pairing.PairingViewModel
import com.adveno.tv.feature.playback.PlaybackViewModel
import com.adveno.tv.playback.cache.MediaCacheManager
import com.adveno.tv.playback.cache.MediaDownloader
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Network & HTTP Inspector (Chucker)
    single { DeviceCredentialStore(get()) }
    single { NetworkClientProvider.createOkHttpClient(androidContext(), get()) }
    single { PairingApiClient(get()) }

    // DataStore Preferences & Database
    single { DevicePreferences(get()) }
    single { AdvenoDatabaseHelper(get()) }

    // Media Cache, Downloader, Network Monitor, and Kiosk
    single { MediaCacheManager(get()) }
    single { MediaDownloader(get()) }
    single<ConnectivityMonitor> { NetworkConnectivityMonitor(get()) }
    single { KioskManager(get()) }

    // Telemetry & Monitoring
    single { HeartbeatMonitor(get(), get(), get(), get()) }

    // Repositories
    single<DeviceRepository> { DeviceRepositoryImpl(get(), get(), get()) }
    single<PairingRepository> { RealPairingRepositoryImpl(get(), get()) }
    single<PlaylistRepository> { SqlitePlaylistRepositoryImpl(get()) }
    single<MediaRepository> { MediaRepositoryImpl(get(), get()) }
    single<SynchronizationRepository> { SynchronizationRepositoryImpl(get(), get()) }

    // UseCases
    factory { DetermineAppLaunchDestinationUseCase(get()) }

    // ViewModels
    viewModel { InitializationViewModel(get()) }
    viewModel { PairingViewModel(get(), get()) }
    viewModel { PlaybackViewModel(get(), get()) }
    viewModel { DeviceStatusViewModel(get(), get(), get()) }
    viewModel { DiagnosticsViewModel(get(), get(), get(), get(), get(), get(), get()) }
}
