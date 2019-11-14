package newVersion.alarm.responsible

import moxy.MvpView
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import newVersion.utils.ResponsibleList

interface ResponsibleView : MvpView {

    fun initRecyclerView(responsibleList:ArrayList<ResponsibleList>)

    fun showToastMessage(message:String)

    @StateStrategyType(value = SkipStrategy::class)
    fun showPersonalCard(name:String,position:String,mobileNumber:String,workNumber:String,homeNumber:String)
}