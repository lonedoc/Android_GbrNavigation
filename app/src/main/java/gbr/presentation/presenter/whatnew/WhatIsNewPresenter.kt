package gbr.presentation.presenter.whatnew

import android.content.Context
import gbr.presentation.view.whatnew.WhatIsNewView
import io.noties.markwon.Markwon
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import moxy.InjectViewState
import moxy.MvpPresenter
import java.lang.Exception

@InjectViewState
class WhatIsNewPresenter:MvpPresenter<WhatIsNewView>() {
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.setTitle("Версия приложения : ${BuildConfig.VERSION_NAME}")

        val changes = "#### Изменения: \n" +
                "Звук уведомления на тревогу и потери соединения"


        val add =  ""
        /*"#### Добавлено: \n" +
                "* Выбор основной карты (OSM или Google) \n" +
                "* Выбор дополнительного навигатор (Yandex или Navitel)\n" +
                "* Автоотправка прибытия\n"*/


        val fix:String = "#### Исправлено: \n" +
                "* Ошибка при тревоге \n" +
                "* Пустая тревога \n"

        viewState.setAdd(add)

        viewState.setChanges(changes)

        viewState.setFix(fix)
    }
}