package newVersion.login.resource

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kobramob.rubeg38.ru.gbrnavigation.R
import newVersion.login.OldLoginPresenter
import ru.rubeg38.technicianmobile.utils.setOnTextChanged

class AdapterIpAddress(
    private val presenter: OldLoginPresenter,
    val address: ArrayList<String>?
) : RecyclerView.Adapter<AdapterIpAddress.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_ip, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return address!!.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val listener = MaskedTextChangedListener(
            "[099]{.}[099]{.}[099]{.}[099]",
            holder.ipAddressTextView
        )
        holder.ipAddressTextView.addTextChangedListener(listener)
        holder.ipAddressTextView.onFocusChangeListener = listener

        holder.ipAddressTextView.setOnTextChanged {
            str ->

                presenter.validateAddress(holder, str.toString())
                address!![position] = str.toString()
                Log.d("Adapter", "Position: $position Str: $str")
            }

        holder.ipAddressTextView.setText(address?.get(position)!!)

        Log.d("Adapter", "$address")
    }

    fun addItem() {
        val indexItem = address?.count()!! + 1
        when {
            indexItem == 3 -> {
                Log.d("Adapter", "AddItem $indexItem")
                presenter.visibilityAddButton(false)
                presenter.addItem(address!!)
            }
            indexItem> 3 ->
                {
                    presenter.viewState.showToastMessage("Превышено максимальное количество IP")
                }
            else -> {
                Log.d("Adapter", "AddItem $indexItem")
                presenter.visibilityRemoveButton(true)
                presenter.addItem(address!!)
            }
        }
    }

    fun removeItem() {
        val indexItem = address?.count()!! - 1
        when {
            indexItem <2 -> {

                Log.d("Adapter", "RemoveItem $indexItem")
                presenter.visibilityRemoveButton(false)
                address.removeAt(indexItem)
                presenter.removeItem(indexItem, address.count())
            }
            indexItem <1 -> {
                presenter.viewState.showToastMessage("Количество IP адресов не может быть меньше одного")
            }
            else -> {
                Log.d("Adapter", "RemoveItem $indexItem")
                presenter.visibilityAddButton(true)
                address.removeAt(indexItem)
                presenter.removeItem(indexItem, address.count())
            }
        }
    }

    fun getAddresses(): ArrayList<String>? {
        return address.let { presenter.validateAddresses(it!!) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ipAddressTextView: TextInputEditText = itemView.findViewById(R.id.ipAddressTextView)
        val ipAddressLayoutView: TextInputLayout = itemView.findViewById(R.id.ipAddressInputLayout)
    }
}
