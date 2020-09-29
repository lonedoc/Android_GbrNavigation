package gbr.utils.adapters.responsible

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import gbr.presentation.presenter.responsible.ResponsiblePresenter
import kobramob.rubeg38.ru.gbrnavigation.R
import newVersion.utils.ResponsibleList

class AdapterResponsible internal constructor(
    private val responsibleList:ArrayList<ResponsibleList>,
    private val presenter: ResponsiblePresenter
): RecyclerView.Adapter<AdapterResponsible.ViewHolder>(){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_alarm_responsible, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return responsibleList.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.responsibleName.text = responsibleList[position].name
        holder.responsiblePosition.text = responsibleList[position].position
        holder.responsibleContainer.setOnClickListener {
            presenter.dialTheNumber(responsibleList[position].name,responsibleList[position].position,responsibleList[position].phone,responsibleList[position].phonew,responsibleList[position].phoneh)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val responsibleName: TextView = itemView.findViewById(R.id.responsible_name)
        val responsiblePosition: TextView = itemView.findViewById(R.id.responsible_position)
        val responsibleContainer: ConstraintLayout = itemView.findViewById(R.id.responsible_container)
    }
}