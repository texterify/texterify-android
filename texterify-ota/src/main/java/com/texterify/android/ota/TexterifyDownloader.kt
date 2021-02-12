package com.texterify.android.ota

import android.content.Context
import android.os.Build
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.util.Locale

internal class TexterifyDownloader(
    private val context: Context,
    private val downloadUrl: String,
) {

    companion object {
        private const val QUERY_LOCALE = "locale"
        private const val QUERY_TIMESTAMP = "timestamp"
    }

    private val moshi by lazy {
        Moshi.Builder().build()
    }
    private val cache = Cache(
        File(context.cacheDir, "texterify_http_cache"),
        5L * 1024L * 1024L // 5 MiB
    )
    private val client = OkHttpClient.Builder()
        .cache(cache)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    internal fun loadCached(locale: Locale, onLoaded: (I18NData) -> Unit) {
        val url = buildUrl(locale)
        val request: Request = Request.Builder()
            .url(url)
            .cacheControl(CacheControl.FORCE_CACHE)
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                Logger.d("Loaded cached data")
                val translations = readTranslations(response) ?: return
                onLoaded(translations)
            } else {
                Logger.w("Failed loading cached data")
            }
        }
    }

    internal fun download(
        locale: Locale,
        forceRefresh: Boolean = false,
        onLoaded: (I18NData) -> Unit,
        onComplete: ((success: Boolean) -> Unit)? = null
    ) {
        val url = buildUrl(locale)
        val request: Request = Request.Builder()
            .url(url)
            .apply { if (forceRefresh) cacheControl(CacheControl.FORCE_CACHE) }
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // ignore failure
                Logger.w("Refreshed data", e)
                onComplete?.invoke(false)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful &&
                        response.networkResponse() != null &&
                        response.code() != HttpURLConnection.HTTP_NOT_MODIFIED
                    ) {
                        Logger.d("Refreshed data")
                        val translations = readTranslations(response) ?: return
                        onComplete?.invoke(true)
                        onLoaded(translations)
                    } else {
                        onComplete?.invoke(false)
                    }
                }
            }
        })
    }

    private fun buildUrl(locale: Locale): HttpUrl {
        val timestamp = context.getString(R.string.texterify_timestamp)
        val languageTag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                locale.toLanguageTag()
            } else {
                locale.language
            }

        return HttpUrl.get(downloadUrl)
            .newBuilder()
            .addEncodedQueryParameter(QUERY_LOCALE, languageTag)
            .addEncodedQueryParameter(QUERY_TIMESTAMP, timestamp)
            .build()
    }

    private fun readTranslations(response: Response): I18NData? {
        return try {
            moshi.adapter(TexterifyRelease::class.java).fromJson(response.body()!!.source())!!.data
        } catch (ex: JsonDataException) {
            Logger.e("Failed loading I18N data", ex)
            null
        }
    }
}
