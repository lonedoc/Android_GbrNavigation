package gbr.ui.whatnew

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import gbr.presentation.presenter.whatnew.WhatIsNewPresenter
import gbr.presentation.view.whatnew.WhatIsNewView
import gbr.utils.callbacks.GpsCallback
import io.noties.markwon.Markwon
import kobramob.rubeg38.ru.gbrnavigation.R
import moxy.MvpAppCompatDialogFragment
import moxy.presenter.InjectPresenter


class WhatIsNewFragment: MvpAppCompatDialogFragment(),WhatIsNewView {
    @InjectPresenter
    lateinit var presenter: WhatIsNewPresenter

    lateinit var rootView:View
    lateinit var changesField: TextView
    lateinit var fixField:TextView
    lateinit var addField:TextView
    lateinit var titleField:TextView

    companion object{
        lateinit var callback: GpsCallback
        fun newInstance(callback:GpsCallback): WhatIsNewFragment {
            this.callback = callback
            return WhatIsNewFragment()
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layoutInflater: LayoutInflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rootView = layoutInflater.inflate(R.layout.fragment_what_new,null)
        titleField = rootView.findViewById(R.id.whatNew_title)
        changesField = rootView.findViewById(R.id.whatNew_changes)
        fixField = rootView.findViewById(R.id.whatNew_fix)
        addField = rootView.findViewById(R.id.whatNew_add)
        isCancelable = false
        return  AlertDialog.Builder(context)
            .setPositiveButton("Закрыть"){
                    dialogInterface, _ ->
                callback.gpsCheck()
                dialogInterface.cancel()
            }
            .setView(rootView)
            .create()
    }

    override fun setTitle(title: String) {
        titleField.text = title
    }

    override fun setChanges(changes: String?) {
        if(changes == null)
        {
            changesField.visibility = View.GONE
            return
        }

        context?.let { Markwon.create(it) }?.setMarkdown(changesField,changes)
    }

    override fun setAdd(add:String?){
        if(add == null)
        {
            addField.visibility = View.GONE
            return
        }

        context?.let { Markwon.create(it) }?.setMarkdown(addField,add)
    }

    override fun setFix(fix: String?) {
        if(fix == null)
        {
            fixField.visibility = View.GONE
            return
        }

        context?.let { Markwon.create(it) }?.setMarkdown(fixField,fix)
    }
}