package gbr.utils.api.alarm

interface OnAlarmListener {
    fun onAlarmDataReceived(flag:String,alarm:String)
}