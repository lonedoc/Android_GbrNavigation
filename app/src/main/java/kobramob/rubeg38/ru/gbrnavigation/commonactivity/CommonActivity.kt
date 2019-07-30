package kobramob.rubeg38.ru.gbrnavigation.commonactivity

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.github.clans.fab.FloatingActionMenu
import android.util.DisplayMetrics
import android.util.Log


class CommonActivity:AppCompatActivity() {

    val SIZE_NORMAL = 0
    val SIZE_MINI = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(kobramob.rubeg38.ru.gbrnavigation.R.layout.activity_common)

        val toolbar:Toolbar = findViewById(kobramob.rubeg38.ru.gbrnavigation.R.id.common_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Позывной"

        val statusList:ArrayList<String> = ArrayList()
        statusList.add("Свободен")
        statusList.add("Обед")
        statusList.add("Ремонт")

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val heightPixels = metrics.heightPixels
        val widthPixels = metrics.widthPixels
        val densityDpi = metrics.densityDpi
        val xdpi = metrics.xdpi
        val ydpi = metrics.ydpi
        Log.i("CommonActivity", "widthPixels  = $widthPixels")
        Log.i("CommonActivity", "heightPixels = $heightPixels")
        Log.i("CommonActivity", "densityDpi   = $densityDpi")
        Log.i("CommonActivity", "xdpi         = $xdpi")
        Log.i("CommonActivity", "ydpi         = $ydpi")
        val fabMenu: FloatingActionMenu = findViewById(kobramob.rubeg38.ru.gbrnavigation.R.id.common_fab_menu)
        for(i in 0 until statusList.count())
        {
            val actionButton = com.github.clans.fab.FloatingActionButton(this)
            actionButton.labelText = statusList[i]
            when(resources.configuration.orientation){
                ORIENTATION_LANDSCAPE->{
                    actionButton.buttonSize = SIZE_MINI
                }
                ORIENTATION_PORTRAIT->{
                    actionButton.buttonSize = SIZE_NORMAL
                }
            }

            fabMenu.addMenuButton(actionButton)
        }

    }
}