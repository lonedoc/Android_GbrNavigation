package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.widget.Toolbar
import android.support.v4.app.Fragment
import kobramob.rubeg38.ru.gbrnavigation.R


class ObjectActivity : AppCompatActivity() {

    private val objectFragment:ObjectFragment = ObjectFragment()
    private val feedFragment:FeedFragment = FeedFragment()
    private val responsibleFragment:ResponsibleFragment = ResponsibleFragment()
    private val planFragment:PlanFragment = PlanFragment()
    private val navigatorFragment:NavigatorFragment = NavigatorFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_object)

        val toolbar: Toolbar = this.findViewById(R.id.toolbar_main_map)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Карточка объекта"

        val bnv: BottomNavigationView = findViewById(R.id.objectMenu)
        bnv.menu.getItem(0).isChecked = true
        bnv.setOnNavigationItemSelectedListener {
            item->

            when(item.itemId)
            {
                R.id.cardObject->{
                    openFragment(objectFragment)
                    supportActionBar!!.title = "Карточка объекта"
                }

                R.id.feedList->{
                    openFragment(feedFragment)
                    supportActionBar!!.title = "Лента событий"
                }

                R.id.responsible->{
                    openFragment(responsibleFragment)
                    supportActionBar!!.title = "Список отвественных"
                }

                R.id.planDiagram->{
                    openFragment(planFragment)
                    supportActionBar!!.title = "План-схема здания"
                }

                R.id.navigator ->{
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
