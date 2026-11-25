package com.labs.barnaton.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor(
    private val apiKey: String,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val trimmedKey = apiKey.trim()

        if (trimmedKey.isEmpty()) {
            return chain.proceed(originalRequest)
        }

        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")

        val isBearerToken = trimmedKey.startsWith("eyJ") || trimmedKey.length > 64
        if (isBearerToken) {
            requestBuilder.header("Authorization", "Bearer $trimmedKey")
        } else {
            val updatedUrl = originalRequest.url.newBuilder()
                .addQueryParameter("api_key", trimmedKey)
                .build()
            requestBuilder.url(updatedUrl)
        }

        return chain.proceed(requestBuilder.build())
    }
}
