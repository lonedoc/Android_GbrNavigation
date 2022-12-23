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

        val changes ="#### Изменения: \n" +
                "Приложение поддерживает 11 и 12 Андроид"


        val add = "#### Добавлено: \n" +
                "* При установке dist = 0, отключается автоприбытие \n" +
                "* Параметр dist ограничен значением 500 \n"

        val fix:String = "#### Исправлено: \n" +
                "* Ошибка при которой приложение не запускалось на 12 Андроиде \n" +
                "* Ошибка при которой приложение не изменяло дистанция при изменение параметра dist \n"


        viewState.setAdd(add)

        viewState.setChanges(changes)

        viewState.setFix(fix)
    }
}