package newVersion.utils

import gbr.utils.data.CityCard
import gbr.utils.data.StatusList
import newVersion.models.AuthInfo

object DataStoreUtils {
    var call: String? = null
    var namegbr: String? = null
    var status: String? = null
    var cityCard: CityCard? = null
    var statusList: ArrayList<StatusList> = ArrayList()
    var routeServer: ArrayList<String> = ArrayList()
    var reports: ArrayList<String> = ArrayList()

    fun saveRegistrationData(authInfo: AuthInfo) {
        call = authInfo.call
        namegbr = authInfo.nameGbr
        status = authInfo.status
        cityCard = authInfo.cityCard

        if (authInfo.gpsStatus.count() >= 1) {
            if(statusList.count()>1){
                statusList.clear()
            }
            statusList.addAll(authInfo.gpsStatus)
            statusList.sortBy { it.status }
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
        call = null
        namegbr = null
        status = null
        cityCard = null

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