package com.texterify.android.ota

import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import androidx.core.util.set
import com.texterify.android.ota.Transcriber.Companion.withMappings

/**
 * Handles the text update for a given view type. You can use [withMappings] for views with simple text attributes:
 *
 *     Transcriber.withMappings<TextView>(transcriptionManager) { mappings ->
 *         mappings(android.R.attr.text) { v, text -> v.text = text }
 *     }
 */
public interface Transcriber<T> {

    public fun transcribe(view: T, attrs: AttributeSet)

    public companion object {

        public fun <T : View> withMappings(
            manager: TranscriptionManager,
            registerMappings: (put: (id: Int, ((T, CharSequence) -> Unit)) -> Unit) -> Unit
        ): Transcriber<T> {
            val mappings = SparseArray<(T, CharSequence) -> Unit>(5)
            registerMappings { id, mapping -> mappings[id] = mapping }
            return MappingTranscriber(manager, mappings)
        }
    }

    /**
     * Factory to create transcribers.
     */
    public interface Factory {

        public fun <T : View> createTranscriber(
            clazz: Class<T>,
            transcriptionManager: TranscriptionManager
        ): Transcriber<T>?
    }

    private class MappingTranscriber<T : View>(
        private val manager: TranscriptionManager,
        private val mappings: SparseArray<(T, CharSequence) -> Unit>
    ) : Transcriber<T> {

        override fun transcribe(view: T, attrs: AttributeSet) {
            for (i in 0 until attrs.attributeCount) {
                val attr = attrs.getAttributeNameResource(i)

                val mapping = mappings[attr] ?: continue

                val value = attrs.getAttributeResourceValue(i, -1)
                val text = manager.getText(value)

                mapping(view, text)
            }
        }
    }
}
