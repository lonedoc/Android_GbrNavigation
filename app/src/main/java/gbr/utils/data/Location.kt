package gbr.utils.data

data class Location (
    val lat:Double,
    val lon:Double,
    val accuracy:Float,
    val speed:Float,
    val satelliteCount:Int?
){
}