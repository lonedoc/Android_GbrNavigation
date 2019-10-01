package newVersion.main

import android.os.Bundle
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import kobramob.rubeg38.ru.gbrnavigation.R

class MainAcitivity: MvpAppCompatActivity(), MainView {
    @InjectPresenter
    lateinit var presenter: MainPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}