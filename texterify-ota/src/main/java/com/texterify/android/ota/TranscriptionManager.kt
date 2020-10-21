package com.texterify.android.ota

import android.content.res.Resources
import android.view.View
import androidx.annotation.StringRes

public class TranscriptionManager(
    private val resources: Resources,
    transcriberFactories: List<Transcriber.Factory>
) {

    private val factories: List<Transcriber.Factory> =
        transcriberFactories + mutableListOf(TexterifyTranscriberFactory())

    public fun <V : View> nextTranscriber(
        clazz: Class<V>,
        from: Transcriber.Factory? = null
    ): Transcriber<V>? {
        val factories = if (from == null) {
            factories.asSequence()
        } else {
            factories.asSequence()
                // skip until the next factory after `from`
                .dropWhile { factory -> factory != from }.drop(1)
        }

        return factories.asSequence()
            .mapNotNull { it.createTranscriber(clazz, this) }
            .firstOrNull()
    }

    public fun updateText(@StringRes id: Int, updateText: (CharSequence) -> Unit) {
        updateText(resources.getText(id))
    }

    public fun getText(@StringRes id: Int): CharSequence =
        if (id == 0) "" else resources.getText(id)
}
