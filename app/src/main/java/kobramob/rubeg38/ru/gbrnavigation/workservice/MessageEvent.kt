package kobramob.rubeg38.ru.gbrnavigation.workservice



data class AlarmEvent(
    val command:String,
    val name:String,
    val number:String,
    val lon:Double,
    val lat:Double,
    val inn:Long,
    val zakaz:String,
    val address:String,
    val area:AreaInfo,
    val otvl:ArrayList<OtvlList> = ArrayList(),
    val plan:ArrayList<String> = ArrayList(),
    val photo:ArrayList<String> = ArrayList()
)

data class RegistrationEvent(
    val command:String,
    val routeServer: ArrayList<String>,
    val call:String,
    val status:String,
    val gbrStatus: ArrayList<String>
)
class MessageEvent() {

    lateinit var command: String
    var alarm: String = ""
    lateinit var message: String

    lateinit var byteArray: ByteArray

    var routeServer: ArrayList<String> = ArrayList()
    lateinit var call: String
    lateinit var status: String
    var gbrStatus: ArrayList<String> = ArrayList()

    lateinit var name: String
    lateinit var number: String
    var lon: Double? = null
    var lat: Double? = null
    var inn: Int? = null
    lateinit var zakaz: String
    lateinit var address: String
    lateinit var areaName: String
    lateinit var areaAlarmTime: String
    var otvl: ArrayList<OtvlList> = ArrayList()
    var plan: ArrayList<String> = ArrayList()
    var photo:ArrayList<String> = ArrayList()

    constructor(command: String, message: String) : this() {
        this.command = command
        this.message = message
    }
    constructor(command: String, message: ByteArray) : this() {
        this.command = command
        this.byteArray = message
    }
}