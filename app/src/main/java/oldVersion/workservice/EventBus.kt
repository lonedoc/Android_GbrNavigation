package oldVersion.workservice

import newVersion.Utils.AreaInfo
import newVersion.Utils.ResponsibleList

data class AlarmEvent(
    val command: String,
    val name: String,
    val number: String,
    val lon: Double,
    val lat: Double,
    val inn: String,
    var zakaz: String = "",
    val address: String,
    val area: AreaInfo,
    val responsible: ArrayList<ResponsibleList> = ArrayList(),
    val plan: ArrayList<String> = ArrayList(),
    val photo: ArrayList<String> = ArrayList()
)

data class RegistrationEvent(
    var command: String
)

data class ImageEvent(
    val command: String,
    val byteArray: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageEvent

        if (command != other.command) return false
        if (!byteArray.contentEquals(other.byteArray)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = command.hashCode()
        result = 31 * result + byteArray.contentHashCode()
        return result
    }
}

class MessageEvent() {

    lateinit var name: String
    lateinit var command: String
    lateinit var message: String

    lateinit var byteArray: ByteArray

    lateinit var status: String
    lateinit var number: String

    constructor(command: String, message: String, name: String) : this() {
        this.command = command
        this.message = message
        this.name = name
    }
    constructor(command: String, message: String) : this() {
        this.command = command
        this.message = message
    }
    constructor(command: String, message: ByteArray) : this() {
        this.command = command
        this.byteArray = message
    }
}