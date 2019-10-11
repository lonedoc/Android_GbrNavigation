package newVersion.callback

interface ReportCallback {
    fun sendReport(selectedReport:String,comment:String)
    fun reportNotSend()
}