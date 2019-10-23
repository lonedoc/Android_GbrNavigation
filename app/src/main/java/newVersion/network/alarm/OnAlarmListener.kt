package newVersion.network.alarm

import newVersion.utils.Alarm

interface OnAlarmListener {
    fun onAlarmDataReceived(alarm: Alarm)
}