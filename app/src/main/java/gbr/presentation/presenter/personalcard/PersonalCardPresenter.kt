package gbr.presentation.presenter.personalcard

import moxy.MvpPresenter

class PersonalCardPresenter:MvpPresenter<gbr.presentation.view.personalcard.PersonalCardView>() {

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
        super.onDestroy()
    }
}