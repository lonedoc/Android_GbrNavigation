package gbr.utils.data

import newVersion.models.Credentials
import newVersion.models.HostPool

data class ProtocolServiceInfo(
    val credentials: Credentials,
    val hostPool: HostPool
)