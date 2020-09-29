package gbr.utils.data


data class StatusGson(
val command: String,
val number: String,
val call: String,
val status: String,
val color: String,
val member: ArrayList<String> = ArrayList(),
val gpsstatus: ArrayList<String> = ArrayList()
)
