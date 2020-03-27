package newVersion.alarm.responsible


import moxy.InjectViewState
import moxy.MvpPresenter
import newVersion.utils.Alarm
import gbr.utils.interfaces.DestroyableAPI
import newVersion.commonInterface.Init

@InjectViewState
class ResponsiblePresenter : MvpPresenter<ResponsibleView>(),
    DestroyableAPI,Init {
    override var init: Boolean = false

    override fun isInit(): Boolean {
        return init
    }

    fun init(alarmInfo:Alarm){
        when{
            alarmInfo.responsibleList.count()>0->{
                viewState.initRecyclerView(alarmInfo.responsibleList)
            }
            else->{
                viewState.showToastMessage("Список ответственных пуст")
            }
        }
    }

    fun showToast(message:String){
        viewState.showToastMessage(message)
    }

    fun dialTheNumber(name:String,position:String,mobileNumber:String,workNumber:String,homeNumber:String){
        viewState.showPersonalCard(name,position,mobileNumber,workNumber,homeNumber)
    }
    override fun onDestroy() {
        super.onDestroy()
        init = false
    }
}