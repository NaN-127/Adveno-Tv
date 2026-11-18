package com.adveno.tv.core.network

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object NetworkClientProvider {

    fun createOkHttpClient(context: Context, credentialStore: DeviceCredentialStore): OkHttpClient {
        val chuckerCollector = ChuckerCollector(
            context = context,
            showNotification = true,
            retentionPeriod = RetentionManager.Period.ONE_HOUR
        )

        val chuckerInterceptor = ChuckerInterceptor.Builder(context)
            .collector(chuckerCollector)
            .maxContentLength(250_000L)
            .redactHeaders("Authorization", "X-Pairing-Token")
            .alwaysReadResponseBody(true)
            .createShortcut(true)
            .build()

        val headerInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
                .header("ngrok-skip-browser-warning", "true")
                .header("User-Agent", "AdvenoTv/1.0 (Android TV)")
            // Do not overwrite an explicit user bearer token, such as the one required
            // when creating a new screen. Otherwise use the current device credential.
            if (chain.request().header("Authorization") == null) {
                credentialStore.current()?.let { token ->
                    requestBuilder.header("Authorization", "Bearer $token")
                }
            }
            chain.proceed(requestBuilder.build())
        }

        return OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(chuckerInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }
}
