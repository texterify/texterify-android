package com.texterify.android.ota

import android.view.View
import android.widget.ImageView
import android.widget.TextView

internal class TexterifyTranscriberFactory : Transcriber.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : View> createTranscriber(
        clazz: Class<T>,
        transcriptionManager: TranscriptionManager
    ): Transcriber<T>? {
        return when {
            TextView::class.java.isAssignableFrom(clazz) -> {
                Transcriber.withMappings<TextView>(transcriptionManager) { mappings ->
                    mappings(android.R.attr.text) { v, text -> v.text = text }
                    mappings(android.R.attr.hint) { v, text -> v.hint = text }
                    mappings(android.R.attr.contentDescription) { v, text ->
                        v.contentDescription = text
                    }
                }
            }
            ImageView::class.java.isAssignableFrom(clazz) -> {
                Transcriber.withMappings<ImageView>(transcriptionManager) { mappings ->
                    mappings(android.R.attr.contentDescription) { v, text ->
                        v.contentDescription = text
                    }
                }
            }
            else -> {
                Logger.d("Unsupported type $clazz")
                null
            }
        } as Transcriber<T>?
    }
}
