package kobramob.rubeg38.ru.gbrnavigation.loginactivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.commonactivity.CommonActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val editText:TextInputEditText = findViewById(R.id.login_ip)
        val listener = MaskedTextChangedListener("[099]{.}[099]{.}[099]{.}[099]",editText)
        editText.addTextChangedListener(listener)
        editText.onFocusChangeListener = listener

        val loginButton: Button = findViewById(R.id.login_button)
        loginButton.setOnClickListener {

            val intent = Intent(this@LoginActivity,CommonActivity::class.java)
            startActivity(intent)

        }
    }

    override fun onBackPressed() {
    }
}
