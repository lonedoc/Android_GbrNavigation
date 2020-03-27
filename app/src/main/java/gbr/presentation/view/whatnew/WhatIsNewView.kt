package gbr.presentation.view.whatnew

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

interface WhatIsNewView:MvpView {
    @StateStrategyType(value = AddToEndSingleStrategy::class)
    fun setTitle(title: String)
    @StateStrategyType(value = AddToEndSingleStrategy::class)
    fun setChanges(changes: String?)
    @StateStrategyType(value = AddToEndSingleStrategy::class)
    fun setFix(fix: String?)
    @StateStrategyType(value = AddToEndSingleStrategy::class)
    fun setAdd(add:String?)
}