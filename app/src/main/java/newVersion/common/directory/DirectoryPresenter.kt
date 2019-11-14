package newVersion.common.directory


import moxy.InjectViewState
import moxy.MvpPresenter
import newVersion.utils.DataStoreUtils

@InjectViewState
class DirectoryPresenter: MvpPresenter<DirectoryView>() {

    private val enable = true
    private val disable = false

    fun setTitle()
    {
        if(DataStoreUtils.cityCard!=null)
            if(DataStoreUtils.cityCard!!.pcsinfo.name!="")
                viewState.setTitle(DataStoreUtils.cityCard!!.pcsinfo.name)
            else
                viewState.setTitle("Нет имени ЧОПА")
        else
            viewState.setTitle("Нет имени ЧОПА")
    }

    fun initPCSInfo()
    {
        if(DataStoreUtils.cityCard!=null)
        {
            if(DataStoreUtils.cityCard!!.pcsinfo.dist!="") {
                viewState.setDistanceArrived(DataStoreUtils.cityCard!!.pcsinfo.dist)
            }
            else {
                viewState.setDistanceArrived("50")
            }

            if(DataStoreUtils.cityCard!!.pcsinfo.operatorphone!=""){
                viewState.setPcsPhone(DataStoreUtils.cityCard!!.pcsinfo.operatorphone,enable)
            }
            else
            {
                viewState.setPcsPhone("Номер телефона не указан",disable)
            }

            if(DataStoreUtils.cityCard!!.pcsinfo.servicecenterphone!=""){
                viewState.setServicePhone(DataStoreUtils.cityCard!!.pcsinfo.servicecenterphone,enable)
            }
            else
            {
                viewState.setServicePhone("Номер телефона не указан",disable)
            }

        }
        else
        {
            viewState.setDistanceArrived("50")
            viewState.setPcsPhone("Номер телефона не указан", disable)
            viewState.setServicePhone("Номер телефона не указан", disable)
            viewState.showToastMessage("Не указаны номера экстренных служб")
            viewState.showToastMessage("Не указаны номера коммунальных служб")
        }

    }

    fun initUSInfo()
    {
        val usPhones = DataStoreUtils.cityCard?.usinfo

        if(usPhones!=null)
        {
            if(usPhones.count() !=0)
            {
                viewState.initUSInfo(usPhones)
            }
            else
            {
                viewState.showToastMessage("Не указаны номера коммунальных служб")
            }
        }
        else
        {
            viewState.showToastMessage("Не указаны номера коммунальных служб")
        }

    }

    fun initESInfo()
    {
        val esPhones = DataStoreUtils.cityCard?.esinfo
        if(esPhones!=null)
        {
            if(esPhones.count() !=0)
            {
                viewState.initEsInfo(esPhones)
            }
            else
            {
                viewState.showToastMessage("Не указаны номера экстренных служб")
            }
        }
        else
        {
            viewState.showToastMessage("Не указаны номера экстренных служб")
        }
    }
}