package newVersion.network.alarm

import newVersion.Utils.Alarm

interface OnAlarmListener {
    fun onAlarmDataReceived(alarm: Alarm)
}