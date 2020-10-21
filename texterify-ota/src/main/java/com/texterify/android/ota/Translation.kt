package com.texterify.android.ota

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class I18NData(
    @Json(name = "texts") val texts: List<Translation.Text>,
    @Json(name = "plurals") val plurals: List<Translation.Plural>,
)

private const val TYPE_TEXT = "text"
private const val TYPE_PLURAL = "plural"

internal sealed class Translation(
    open val type: String,
    open val key: String,
    open val isHtml: Boolean,
) {
    object Unknown : Translation("?", "?", false)

    @JsonClass(generateAdapter = true)
    class Text(
        @Json(name = "key") override val key: String,
        @Deprecated("not implemented") @Json(name = "is_html") override val isHtml: Boolean = false,
        @Json(name = "value") val value: String,
    ) : Translation(TYPE_TEXT, key, isHtml) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Text

            if (type != other.type) return false
            if (key != other.key) return false
            if (isHtml != other.isHtml) return false
            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            var result = type.hashCode()
            result = 31 * result + key.hashCode()
            result = 31 * result + isHtml.hashCode()
            result = 31 * result + value.hashCode()
            return result
        }
    }

    @JsonClass(generateAdapter = true)
    class Plural(
        @Json(name = "key") override val key: String,
        @Json(name = "is_html") override val isHtml: Boolean,
        @Json(name = "zero") val zero: String?,
        @Json(name = "one") val one: String?,
        @Json(name = "two") val two: String?,
        @Json(name = "few") val few: String?,
        @Json(name = "many") val many: String?,
        @Json(name = "other") val other: String,
    ) : Translation(TYPE_PLURAL, key, isHtml) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Plural

            if (type != other.type) return false
            if (key != other.key) return false
            if (isHtml != other.isHtml) return false
            if (zero != other.zero) return false
            if (one != other.one) return false
            if (two != other.two) return false
            if (few != other.few) return false
            if (many != other.many) return false
            if (other != other.other) return false

            return true
        }

        override fun hashCode(): Int {
            var result = type.hashCode()
            result = 31 * result + key.hashCode()
            result = 31 * result + isHtml.hashCode()
            result = 31 * result + (zero?.hashCode() ?: 0)
            result = 31 * result + (one?.hashCode() ?: 0)
            result = 31 * result + (two?.hashCode() ?: 0)
            result = 31 * result + (few?.hashCode() ?: 0)
            result = 31 * result + (many?.hashCode() ?: 0)
            result = 31 * result + other.hashCode()
            return result
        }
    }
}
