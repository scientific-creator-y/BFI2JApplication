package com.dino.personalmonster

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ResultConfirmDialogFragment : DialogFragment() {
    interface ResultConfirmListener {
        fun onDialogPositiveClick()
    }

    private var listener : ResultConfirmListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ResultConfirmListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ResultConfirmListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.dialog_title)
            builder.setPositiveButton(R.string.dialog_button_ok, { _, _ -> listener?.onDialogPositiveClick()})
            builder.setNegativeButton(R.string.dialog_button_ng, null)
            builder.create()
        }
        return dialog ?: throw IllegalStateException("アクティビティがNullです")
    }

}