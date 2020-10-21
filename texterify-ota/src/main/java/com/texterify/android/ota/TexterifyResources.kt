package com.texterify.android.ota

import android.content.res.Resources
import android.icu.text.PluralRules
import android.text.Html
import android.util.Log
import com.texterify.android.ota.plural.DummyPluralResolver
import com.texterify.android.ota.plural.IcuPluralResolver
import com.texterify.android.ota.plural.PluralResolver

private const val TAG = "TxtfyRes"

internal class TexterifyResources(
    private val repository: TexterifyRepository,
    private val resources: Resources,
) : Resources(resources.assets, resources.displayMetrics, resources.configuration) {

    private val pluralResolver: PluralResolver =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
            IcuPluralResolver(
                repository,
                resources,
                PluralRules.forLocale(resources.configuration.locale)
            )
        else
            DummyPluralResolver()

    override fun getString(id: Int): String {
        Log.d(TAG, "getString($id)")
        return getTranslation(id)?.value ?: super.getString(id)
    }

    override fun getString(id: Int, vararg formatArgs: Any?): String {
        Log.d(TAG, "getString($id, ${formatArgs.joinToString()})")
        return getTranslation(id)?.value?.format(formatArgs) ?: super.getString(id, *formatArgs)
    }

    override fun getText(id: Int): CharSequence {
        Log.d(TAG, "getText($id)")

        val text = getTranslation(id) ?: return super.getText(id)
        return formatText(text)
    }

    override fun getText(id: Int, def: CharSequence?): CharSequence {
        Log.d(TAG, "getText($id, $def)")

        val text = getTranslation(id) ?: return super.getString(id, def)
        return formatText(text)
    }

    private fun formatText(text: Translation.Text): CharSequence =
        if (text.isHtml) text.value else Html.fromHtml(text.value)

    override fun getQuantityText(id: Int, quantity: Int): CharSequence {
        Log.d(TAG, "getQuantityText($id, $quantity)")

        return pluralResolver.readPluralForQuantity(id, quantity)
            ?: resources.getQuantityText(id, quantity)
    }

    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any?): String {
        Log.d(TAG, "getQuantityString($id, $quantity, ${formatArgs.joinToString()})")

        return pluralResolver.readPluralForQuantity(id, quantity)?.format(*formatArgs)
            ?: resources.getQuantityString(id, quantity, *formatArgs)
    }

    override fun getQuantityString(id: Int, quantity: Int): String {
        Log.d(TAG, "getQuantityString($id, $quantity)")

        return pluralResolver.readPluralForQuantity(id, quantity)
            ?: resources.getQuantityString(id, quantity)
    }

    override fun getTextArray(id: Int): Array<CharSequence> {
        Log.d(TAG, "getTextArray($id)")
        // todo should we support this? how?
        return resources.getTextArray(id)
    }

    override fun getStringArray(id: Int): Array<String> {
        Log.d(TAG, "getStringArray($id)")
        // todo should we support this? how?
        return resources.getStringArray(id)
    }

    private fun getTranslation(id: Int): Translation.Text? {
        val name = resources.getResourceEntryName(id)
        return repository.getText(name).apply {
            Log.v(TAG, "getTranslation($id) -> $name ($this)")
        }
    }
}
