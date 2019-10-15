package newVersion.alarm.responsible.personalCard

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.arellomobile.mvp.MvpAppCompatDialogFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import kobramob.rubeg38.ru.gbrnavigation.R


class PersonalCardFragment:MvpAppCompatDialogFragment(),PersonalCardView {

    @InjectPresenter
    lateinit var presenter: PersonalCardPresenter

    override fun setName(name: String) {
        val nameTV: TextView = rootView?.findViewById(R.id.personal_name)!!
        nameTV.text = name
    }

    override fun setPosition(position: String) {
        val positionTV: TextView = rootView?.findViewById(R.id.personal_position)!!
        positionTV.text = position
    }

    override fun setMobile(mobile: String) {
        val mobileNumberTV: TextView = rootView?.findViewById(R.id.personal_mobileNumber)!!
        mobileNumberTV.text = mobile
    }

    override fun setWork(work: String) {
        val workNumberTV: TextView = rootView?.findViewById(R.id.personal_workNumber)!!
        workNumberTV.text = work
    }

    override fun setHome(home: String) {
        val homeNumberTV: TextView = rootView?.findViewById(R.id.personal_homeNumber)!!
        homeNumberTV.text = home
    }

    override fun setInvisibleMobile() {
        val buttonDialMobile: ImageButton = rootView?.findViewById(R.id.dial_mobile)!!
        buttonDialMobile.visibility = View.INVISIBLE
    }

    override fun setInvisibleWork() {
        val buttonDialWork: ImageButton = rootView?.findViewById(R.id.dial_work)!!
        buttonDialWork.visibility = View.INVISIBLE
    }

    override fun setInvisibleHome() {
        val buttonDialHome: ImageButton = rootView?.findViewById(R.id.dial_home)!!
        buttonDialHome.visibility = View.INVISIBLE
    }

    companion object{
        fun onNewInstance(name:String,position:String,mobileNumber:String,workNumber:String,homeNumber:String): PersonalCardFragment {
            val frag = PersonalCardFragment()
            val args = Bundle()
            args.putString("name",name)
            args.putString("position",position)
            args.putString("mobile",mobileNumber)
            args.putString("work",workNumber)
            args.putString("home",homeNumber)
            frag.arguments = args
            return frag
        }
    }

    private var rootView: View? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val layoutInflater: LayoutInflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rootView = layoutInflater.inflate(R.layout.fragment_alarm_personal_card, null)

        val buttonDialMobile: ImageButton = rootView?.findViewById(R.id.dial_mobile)!!
        val buttonDialWork: ImageButton = rootView?.findViewById(R.id.dial_work)!!
        val buttonDialHome: ImageButton = rootView?.findViewById(R.id.dial_home)!!

        buttonDialMobile.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:" + arguments?.getString("mobile")!!)
            )
            startActivity(intent)
        }

        buttonDialWork.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:" + arguments?.getString("work")!!)
            )
            startActivity(intent)
        }

        buttonDialHome.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:" + arguments?.getString("home")!!)
            )
            startActivity(intent)
        }

        isCancelable = false
        return builder
            .setNegativeButton("Закрыть"){
                dialogInterface, i ->
                dismiss()
            }
            .setView(rootView)
            .create()
    }

    override fun onResume() {
        super.onResume()
        if(!presenter.init){
            presenter.init(
                name = arguments?.getString("name")!!,
                position = arguments?.getString("position")!!,
                mobile = arguments?.getString("mobile")!!,
                work = arguments?.getString("work")!!,
                home = arguments?.getString("home")!!
            )
        }
    }

    override fun onPause() {
        super.onPause()
        presenter.onDestroy()
    }
}