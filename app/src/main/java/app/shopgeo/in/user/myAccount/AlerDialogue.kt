package app.shopgeo.`in`.user.myAccount

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import app.shopgeo.`in`.R
import timber.log.Timber


class AlerDialogue(private val viewModel: MyAccountViewModel) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let{
            val builder = AlertDialog.Builder(it)
             val inflater = requireActivity().layoutInflater;
            val layoutView = inflater.inflate(R.layout.fragment_alert_dialogue,null)
            builder.setView(layoutView)
                .setPositiveButton(R.string.change,DialogInterface.OnClickListener { dialogInterface, i ->
                    viewModel.updatePhone(layoutView.findViewById<TextView>(R.id.edit_phone_dialog).text.toString())
                })
                .setNegativeButton(R.string.cancel,DialogInterface.OnClickListener { dialogInterface, i ->
                    dialog?.cancel()
                })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
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