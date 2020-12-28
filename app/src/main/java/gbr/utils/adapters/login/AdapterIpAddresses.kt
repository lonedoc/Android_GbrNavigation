package gbr.utils.adapters.login

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.redmadrobot.inputmask.MaskedTextChangedListener
import gbr.utils.callbacks.ValidateAddress
import kobramob.rubeg38.ru.gbrnavigation.R
import ru.rubeg38.technicianmobile.utils.setOnTextChanged

class AdapterIpAddresses
    (
    val address:ArrayList<String>,
    private val callback: ValidateAddress
): RecyclerView.Adapter<AdapterIpAddresses.ViewHolder>()
{
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_ip,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return address.count()
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
            callback.validateAddress(holder,str.toString())
            address[position] = str.toString()
        }
        holder.ipAddressTextView.setText(address[position])
    }

    fun addItem(): Int {
        address.add("")
        return address.count()
    }

    fun removeItem():Int{
        address.remove(address.last())
        return address.count()
    }

    fun getAddresses():ArrayList<String>{
        return address
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val ipAddressTextView:TextInputEditText= itemView.findViewById(R.id.ipAddressTextView)
        val ipAddressLayoutView:TextInputLayout = itemView.findViewById(R.id.ipAddressInputLayout)
    }
}