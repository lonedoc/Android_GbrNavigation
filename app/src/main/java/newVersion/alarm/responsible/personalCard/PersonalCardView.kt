package newVersion.alarm.responsible.personalCard

import com.arellomobile.mvp.MvpView

interface PersonalCardView:MvpView {
    fun setName(name:String)
    fun setPosition(position:String)
    fun setMobile(mobile:String)
    fun setWork(work:String)
    fun setHome(home:String)

    fun setInvisibleMobile()
    fun setInvisibleWork()
    fun setInvisibleHome()
}