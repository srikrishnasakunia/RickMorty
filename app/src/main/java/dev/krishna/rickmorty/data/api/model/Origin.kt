package dev.krishna.rickmorty.data.api.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@JsonClass(generateAdapter = true)
data class Origin(
    val name: String,
    val url: String
): Parcelable
