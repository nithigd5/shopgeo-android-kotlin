package app.shopgeo.`in`.user.myAccount

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import app.shopgeo.`in`.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import timber.log.Timber


class AlertDialog(private val viewModel: MyAccountViewModel, val type:String) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let{

           val builder = when (type) {
               "name_dialog" -> setNameChangeDialog(it)
               "phone_dialog" -> setPhoneChangeDialog(it)
               "email_dialog" -> setEmailChangeDialog(it)
               "otp_dialog" -> setOTPChangeDialog(it)
               else -> throw IllegalArgumentException("Dialog Not Found")
           }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun setPhoneChangeDialog(activity : FragmentActivity) : AlertDialog.Builder{
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val layoutView = inflater.inflate(R.layout.fragment_alert_dialogue,null)
        layoutView.findViewById<TextInputLayout>(R.id.edit_phone_layout).visibility = View.VISIBLE
        builder.setView(layoutView)
            .setPositiveButton(R.string.change,DialogInterface.OnClickListener { dialogInterface, i ->
                viewModel.updatePhone(layoutView.findViewById<TextInputEditText>(R.id.edit_phone_dialog).text.toString())
            })
            .setNegativeButton(R.string.cancel,DialogInterface.OnClickListener { dialogInterface, i ->
                dialog?.cancel()
            })
     return builder
    }
    private fun setEmailChangeDialog(activity : FragmentActivity) : AlertDialog.Builder{
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val layoutView = inflater.inflate(R.layout.fragment_alert_dialogue,null)
        layoutView.findViewById<TextView>(R.id.head).text = "Enter your email"
        layoutView.findViewById<TextInputLayout>(R.id.edit_email_layout).visibility = View.VISIBLE
        builder.setView(layoutView)
            .setPositiveButton(R.string.change,DialogInterface.OnClickListener { dialogInterface, i ->
                viewModel.updateEmail(layoutView.findViewById<TextInputEditText>(R.id.edit_email_dialog).text.toString())
            })
            .setNegativeButton(R.string.cancel,DialogInterface.OnClickListener { dialogInterface, i ->
                dialog?.cancel()
            })
     return builder
    }
    private fun setOTPChangeDialog(activity : FragmentActivity) : AlertDialog.Builder{
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val layoutView = inflater.inflate(R.layout.fragment_alert_dialogue,null)

        layoutView.findViewById<TextView>(R.id.head).text = "Enter OTP"
        layoutView.findViewById<TextInputLayout>(R.id.edit_otp_layout).visibility = View.VISIBLE
        builder.setView(layoutView)
            .setPositiveButton(R.string.change,DialogInterface.OnClickListener { dialogInterface, i ->

                viewModel.verifyOTP(layoutView.findViewById<TextInputEditText>(R.id.edit_otp_dialog).text.toString())
            })
            .setNegativeButton(R.string.cancel,DialogInterface.OnClickListener { dialogInterface, i ->
                dialog?.cancel()
            })
     return builder
    }

  private fun setNameChangeDialog(activity : FragmentActivity) : AlertDialog.Builder{
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val layoutView = inflater.inflate(R.layout.fragment_alert_dialogue,null)
      layoutView.findViewById<TextInputLayout>(R.id.edit_name_layout).visibility = View.VISIBLE
      layoutView.findViewById<TextView>(R.id.head).text = "Enter your Name"
      builder.setView(layoutView)
            .setPositiveButton(R.string.change,DialogInterface.OnClickListener { dialogInterface, i ->
                viewModel.updateName(layoutView.findViewById<TextInputEditText>(R.id.edit_name_dialog).text.toString())
            })
            .setNegativeButton(R.string.cancel,DialogInterface.OnClickListener { dialogInterface, i ->
                dialog?.cancel()
            })
     return builder
    }

    override fun onDestroy() {
        Timber.i("Dialogue Destroyed")
        super.onDestroy()
    }

    interface NoticeDialogListener{
        fun onDialogpositiveClick(dialog : DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

}