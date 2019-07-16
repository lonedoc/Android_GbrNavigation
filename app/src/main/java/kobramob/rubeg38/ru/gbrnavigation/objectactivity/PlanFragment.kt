package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kobramob.rubeg38.ru.gbrnavigation.R

class PlanFragment : androidx.fragment.app.Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.plang_fragment, container, false)

        return rootView
    }
}