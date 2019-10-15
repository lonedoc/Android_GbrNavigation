package newVersion.Utils

import newVersion.models.AuthInfo

object DataStoreUtils {
    var call: String? = null
    var namegbr: String? = null
    var status: String? = null
    var cityCard: CityCard? = null
    var statusList: ArrayList<GpsStatus> = ArrayList()
    var routeServer: ArrayList<String> = ArrayList()
    var reports: ArrayList<String> = ArrayList()

    fun saveRegistrationData(authInfo: AuthInfo) {
        this.call = authInfo.call
        this.namegbr = authInfo.nameGbr
        this.status = authInfo.status
        this.cityCard = authInfo.cityCard

        if (authInfo.gpsStatus.count() >= 1) {
            statusList.addAll(authInfo.gpsStatus)
            statusList.sortBy { it.name }
        }

        if (authInfo.routeServer.count() >= 1) {
            routeServer.addAll(authInfo.routeServer)
        } else {
            routeServer.add("91.189.160.38:5000")
        }

        if (authInfo.report.count() >= 1) {
            reports.addAll(authInfo.report)
            reports.sortBy { it }
        }
    }

    fun clearAllData() {
        this.call = null
        this.namegbr = null
        this.status = null
        this.cityCard = null

        if (statusList.count() >= 1) {
            statusList.clear()
        }
        if (routeServer.count() >= 1) {
            routeServer.clear()
        }
        if (reports.count() >= 1) {
            reports.clear()
        }
    }
}