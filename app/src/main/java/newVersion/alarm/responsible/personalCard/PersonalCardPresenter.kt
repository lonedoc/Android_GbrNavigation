package newVersion.alarm.responsible.personalCard

import moxy.InjectViewState
import moxy.MvpPresenter
import gbr.utils.interfaces.DestroyableAPI
import newVersion.commonInterface.Init

@InjectViewState
class PersonalCardPresenter: MvpPresenter<PersonalCardView>(),Init,
    DestroyableAPI {

    override var init: Boolean = false

    override fun isInit(): Boolean {
         return init
    }

    fun init(name:String,position:String,mobile:String,work:String,home:String){

        viewState.setName(name)
        viewState.setPosition(position)

        if(mobile!=""){
            viewState.setMobile(mobile)
        }
        else
        {
            viewState.setMobile("Номер отсутствует")
            viewState.setInvisibleMobile()
        }

        if(work!=""){
            viewState.setWork(work)
        }
        else
        {
            viewState.setWork("Номер отсутствует")
            viewState.setInvisibleWork()
        }

        if(home!=""){
            viewState.setHome(home)
        }
        else
        {
            viewState.setHome("Номер отсутствует")
            viewState.setInvisibleHome()
        }
    }
    override fun onDestroy() {
        init = false
        super.onDestroy()
    }
}