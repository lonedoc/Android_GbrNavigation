package newVersion.network.alarm

import oldVersion.workservice.Alarm

interface OnAlarmListener {
    fun onAlarmDataReceived(alarm: Alarm)
}