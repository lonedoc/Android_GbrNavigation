package gbr.utils.data

import android.location.Location
import java.math.BigDecimal
import java.text.DateFormat
import java.util.*

class GPSPoint {
    var latitude: BigDecimal? = null
        private set
    var longitude: BigDecimal? = null
        private set
    var date: Date? = null
        private set
    var lastUpdate: String? = null
        private set
    var location: Location? = null

    constructor(latitude: BigDecimal?, longitude: BigDecimal?) {
        this.latitude = latitude
        this.longitude = longitude
        date = Date()
        lastUpdate = DateFormat.getTimeInstance().format(date!!)
    }

    constructor(latitude: Double?, longitude: Double?) {
        this.latitude = BigDecimal.valueOf(latitude!!)
        this.longitude = BigDecimal.valueOf(longitude!!)
    }

    constructor(location: Location?) {
        this.location = location
    }

    override fun toString(): String {
        return "(" + latitude + ", " + longitude + ")"
    }
}