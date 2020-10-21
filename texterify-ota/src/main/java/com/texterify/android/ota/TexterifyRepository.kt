package com.texterify.android.ota

internal class TexterifyRepository {

    private var texts: Map<String, Translation.Text> = emptyMap()
    private var plurals: Map<String, Translation.Plural> = emptyMap()

    fun getText(id: String): Translation.Text? = texts[id]
    fun getPlural(id: String): Translation.Plural? = plurals[id]

    fun updateTranslations(translations: I18NData) {
        texts = translations.texts.associateBy { it.key }
        plurals = translations.plurals.associateBy { it.key }
    }
}
