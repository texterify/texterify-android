package com.texterify.android.ota.plural

import android.content.res.Resources
import android.icu.text.PluralRules
import androidx.annotation.RequiresApi
import com.texterify.android.ota.TexterifyRepository
import com.texterify.android.ota.Translation

/**
 * Resolver to pick the correct quantity depending on the plural rules.
 */
@RequiresApi(android.os.Build.VERSION_CODES.N)
internal class IcuPluralResolver(
    private val repository: TexterifyRepository,
    private val resources: Resources,
    private val rules: PluralRules,
) : PluralResolver {

    private fun lookupPlural(id: Int): Translation.Plural? {
        val name = resources.getResourceEntryName(id)
        return repository.getPlural(name)
    }

    override fun readPluralForQuantity(
        id: Int,
        quantity: Int,
    ): String? {
        val plural: Translation.Plural = lookupPlural(id) ?: return null

        return when (rules.select(quantity.toDouble())) {
            PluralRules.KEYWORD_ZERO -> plural.zero
            PluralRules.KEYWORD_ONE -> plural.one
            PluralRules.KEYWORD_TWO -> plural.two
            PluralRules.KEYWORD_FEW -> plural.few
            PluralRules.KEYWORD_MANY -> plural.many
            else -> plural.other
        } ?: plural.other
    }
}
