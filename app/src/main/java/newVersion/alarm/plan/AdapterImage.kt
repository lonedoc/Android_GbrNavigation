package newVersion.alarm.plan

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import kobramob.rubeg38.ru.gbrnavigation.R

class AdapterImage(
    val plan: ArrayList<Bitmap?>,
    val presenter: PlanPresenter,
    val context: Context?
) : RecyclerView.Adapter<AdapterImage.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_image, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return plan.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(plan[position]){
            null->{
                holder.progressBar.visibility = View.VISIBLE
                holder.planImage.visibility = View.GONE
            }
            else->{
                Log.d("ImageAdapter","${plan.count()}")
                holder.planImage.setImageBitmap(plan[position])
                holder.planImage.visibility = View.VISIBLE
                holder.progressBar.visibility = View.GONE
                holder.parent.setOnClickListener {
                    showImageFragment(plan[position]!!)
                }
            }
        }
    }

    fun showImageFragment(image: Bitmap) {
        val fragment = ImageScaleFragment.newInstance(image)
        val transaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()
        transaction.add(R.id.alarm_fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val planImage: ImageView = itemView.findViewById(R.id.alarm_plan_image)
        val progressBar: ProgressBar = itemView.findViewById(R.id.alarm_plan_progress)
        val parent:ConstraintLayout = itemView.findViewById(R.id.alarm_plan_parent)
    }
}