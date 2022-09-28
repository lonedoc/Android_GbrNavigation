package gbr.presentation.presenter.responsible

import gbr.presentation.view.responsible.ResponsibleView
import gbr.utils.data.AlarmInfo
import moxy.InjectViewState
import moxy.MvpPresenter

@InjectViewState
class ResponsiblePresenter:MvpPresenter<ResponsibleView>() {

    val alarmInfo: AlarmInfo = AlarmInfo

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
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
}