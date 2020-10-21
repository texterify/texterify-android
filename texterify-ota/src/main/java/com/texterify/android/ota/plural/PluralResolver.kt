package com.texterify.android.ota.plural

internal interface PluralResolver {

    fun readPluralForQuantity(
        id: Int,
        quantity: Int,
    ): String?
}
