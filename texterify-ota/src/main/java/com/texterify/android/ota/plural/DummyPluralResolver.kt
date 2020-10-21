package com.texterify.android.ota.plural

/**
 * No-op implementation for unsupported devices.
 */
internal class DummyPluralResolver : PluralResolver {
    override fun readPluralForQuantity(id: Int, quantity: Int): String? = null
}
