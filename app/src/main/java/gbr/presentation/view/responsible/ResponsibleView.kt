package gbr.presentation.view.responsible

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import newVersion.utils.ResponsibleList

interface ResponsibleView:MvpView {

    @StateStrategyType(value = AddToEndStrategy::class)
    fun initRecyclerView(responsibleList:ArrayList<ResponsibleList>)

    fun showToastMessage(message:String)

    fun showPersonalCard(name:String,position:String,mobileNumber:String,workNumber:String,homeNumber:String)

}