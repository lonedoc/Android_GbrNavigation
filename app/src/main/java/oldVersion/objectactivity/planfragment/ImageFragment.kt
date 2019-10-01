package oldVersion.objectactivity.planfragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import kobramob.rubeg38.ru.gbrnavigation.R

class ImageFragment(val bitmap: Bitmap) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.image_fragment, container, false)
        val imageView: SubsamplingScaleImageView = rootView.findViewById(R.id.fragment_image)
        imageView.setImage(ImageSource.bitmap(bitmap))
        val button: Button = rootView.findViewById(R.id.close_fragment)
        button.setOnClickListener {
            activity!!.supportFragmentManager.popBackStack()
        }
        return rootView
    }
}