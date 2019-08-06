package kobramob.rubeg38.ru.gbrnavigation.workservice

class MessageEvent() {

    lateinit var command: String
    var alarm:String = ""
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
    var otvl: ArrayList<String> = ArrayList()
    var plan: ArrayList<String> = ArrayList()

    constructor(command: String, message: String) : this() {
        this.command = command
        this.message = message
    }
    constructor(command: String, message: ByteArray) : this() {
        this.command = command
        this.byteArray = message
    }

    constructor(command: String, name: String, number: String, lon: Double, lat: Double, inn: Int, zakaz: String, address: String, areaName: String, areaAlarmTime: String, otvl: ArrayList<String>, plan: ArrayList<String>) : this() {
        this.alarm = command
        this.name = name
        this.number = number
        this.lon = lon
        this.lat = lat
        this.inn = inn
        this.zakaz = zakaz
        this.address = address
        this.areaName = areaName
        this.areaAlarmTime = areaAlarmTime
        this.otvl = otvl
        this.plan = plan
    }
    constructor(command: String, routeServer: ArrayList<String>, call: String, status: String, gbrStatus: ArrayList<String>) : this() {
        this.command = command
        this.routeServer = routeServer
        this.call = call
        this.status = status
        this.gbrStatus = gbrStatus
    }
}