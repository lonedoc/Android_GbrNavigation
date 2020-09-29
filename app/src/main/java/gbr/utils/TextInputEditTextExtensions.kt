package ru.rubeg38.technicianmobile.utils

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.google.android.material.textfield.TextInputEditText

fun TextInputEditText.setOnTextChanged(listener: (CharSequence?) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {}
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(str: CharSequence?, start: Int, before: Int, count: Int) {
            listener(str)
        }
    })
}

fun TextInputEditText.setOnFocusChanged(listener: (View?, Boolean) -> Unit) {
    this.onFocusChangeListener = object : View.OnFocusChangeListener {
        override fun onFocusChange(view: View?, hasFocus: Boolean) {
            listener(view, hasFocus)
        }
    }
}