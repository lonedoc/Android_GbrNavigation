package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kobramob.rubeg38.ru.gbrnavigation.R
import android.support.v4.app.Fragment
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView

class NavigatorFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.navigator_fragment, container, false)
        initMapView(rootView)

        val bnv: BottomNavigationView = activity!!.findViewById(R.id.objectMenu)
        bnv.menu.getItem(1).isChecked = true

        return rootView
    }

    private fun initMapView(rootView: View) {
        val mMapView: MapView = rootView.findViewById(R.id.navigator_view)
        mMapView.setTileSource(TileSourceFactory.MAPNIK)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}