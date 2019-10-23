package newVersion.alarm.plan

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import kobramob.rubeg38.ru.gbrnavigation.R
import java.io.Serializable

class ImageScaleFragment:Fragment() {
    companion object{
        var isAlive = false
        fun newInstance(image:Bitmap):ImageScaleFragment{
            val frag = ImageScaleFragment()
            val args = Bundle()
            val bitmap = SerializableBitmap(image)
            args.putSerializable("image",bitmap)
            frag.arguments = args
            return frag
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_alarm_scale_image, container, false)

        val imageView: SubsamplingScaleImageView = rootView.findViewById(R.id.scale_image)!!

        val bitmap = arguments!!.getSerializable("image") as SerializableBitmap
        imageView.setImage(ImageSource.bitmap(bitmap.image))

        val button: Button = rootView.findViewById(R.id.close_scale)!!

        button.setOnClickListener {
            if(activity==null){
                Log.d("Activity","null")
            }
            else
            {
                activity!!.supportFragmentManager.popBackStack()
            }
        }
        return rootView
    }

    override fun onResume() {
        super.onResume()
        isAlive= true
    }
    override fun onStop() {
        super.onStop()
        isAlive = false
    }
}

data class SerializableBitmap(
    val image:Bitmap
):Serializable