package com.texterify.android.ota.androidx

import android.view.View
import androidx.appcompat.widget.Toolbar
import com.texterify.android.ota.Transcriber
import com.texterify.android.ota.TranscriptionManager

class TexterifyAppcompatTranscriber : Transcriber.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : View> createTranscriber(
        clazz: Class<T>,
        transcriptionManager: TranscriptionManager
    ): Transcriber<T>? {
        return when {
            Toolbar::class.java.isAssignableFrom(clazz) -> {
                Transcriber.withMappings<Toolbar>(transcriptionManager) { mappings ->
                    mappings(android.R.attr.title) { v, text -> v.title = text }
                    mappings(android.R.attr.subtitle) { v, text -> v.subtitle = text }
                }
            }
            else -> null
        } as Transcriber<T>?
    }
}
