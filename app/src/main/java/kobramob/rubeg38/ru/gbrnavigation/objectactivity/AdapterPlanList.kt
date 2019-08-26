package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class AdapterPlanList(private val bitmapList: ArrayList<Bitmap>, private val context: Context) : RecyclerView.Adapter<AdapterPlanList.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(kobramob.rubeg38.ru.gbrnavigation.R.layout.recyclerview_image, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return bitmapList.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imageView.setImageBitmap(bitmapList[position])
        holder.parentLayout.setOnClickListener {
            val fragment: Fragment = ImageFragment(bitmap = bitmapList[position])
            openFragment(fragment)
        }
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()
        transaction.replace(kobramob.rubeg38.ru.gbrnavigation.R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(kobramob.rubeg38.ru.gbrnavigation.R.id.imagePlan)
        val parentLayout: LinearLayout = itemView.findViewById(kobramob.rubeg38.ru.gbrnavigation.R.id.image_parent)
    }
}
