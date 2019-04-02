package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.widget.Toolbar
import android.support.v4.app.Fragment
import kobramob.rubeg38.ru.gbrnavigation.R

class ObjectActivity : AppCompatActivity() {

    private val tabFragment: TabFragment = TabFragment()
    private val navigatorFragment: NavigatorFragment = NavigatorFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_object)

        val toolbar: Toolbar = this.findViewById(R.id.toolbar_main_map)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Карточка объекта"

        val bnv: BottomNavigationView = findViewById(R.id.objectMenu)
        bnv.menu.getItem(0).isChecked = true

        openFragment(tabFragment)
        supportActionBar!!.title = "Карточка объекта"

        bnv.setOnNavigationItemSelectedListener {
            item ->

            when (item.itemId) {
                R.id.cardObject -> {
                    openFragment(tabFragment)
                    supportActionBar!!.title = "Карточка объекта"
                }

                R.id.navigator -> {
                    openFragment(navigatorFragment)
                    supportActionBar!!.title = "Навигатор"
                }
            }
            true
        }
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(kobramob.rubeg38.ru.gbrnavigation.R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
