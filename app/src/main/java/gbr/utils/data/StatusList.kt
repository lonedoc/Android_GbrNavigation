package gbr.utils.data

import com.google.gson.annotations.SerializedName

data class StatusList(
    @SerializedName("name") val status: String,
    val time: String
)