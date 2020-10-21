package com.texterify.android.ota

import android.view.View
import io.github.inflationx.viewpump.InflateResult
import io.github.inflationx.viewpump.Interceptor

/**
 * ViewPump interceptor that will connect view inflation with Texterify to allow for replacing texts
 * with their latest version.
 */
internal class TexterifyUpdateTranslationsInterceptor(
    private val transcriptionManager: TranscriptionManager
) : Interceptor {

    private val transcriberCache: MutableMap<Class<*>, Transcriber<*>?> = mutableMapOf()

    override fun intercept(chain: Interceptor.Chain): InflateResult {
        val result = chain.proceed(chain.request())
        val view = result.view ?: return result

        transcribeView(view, result)

        return result
    }

    private fun transcribeView(view: View, result: InflateResult) {
        val clazz = view.javaClass
        val transcriber = fetchTranscriber(clazz)

        transcriber?.transcribe(view, result.attrs!!)
    }

    private fun <T : View> fetchTranscriber(clazz: Class<T>): Transcriber<T>? {
        if (transcriberCache.containsKey(clazz)) {
            @Suppress("UNCHECKED_CAST")
            return transcriberCache[clazz] as Transcriber<T>?
        }

        val transcriber = transcriptionManager.nextTranscriber(clazz)
        transcriberCache[clazz] = transcriber
        return transcriber
    }
}
