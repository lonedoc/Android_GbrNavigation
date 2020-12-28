package gbr.utils.data

import java.io.Serializable

data class HostPool(
    var addresses: ArrayList<String>,
    var port: Int
) : Serializable
