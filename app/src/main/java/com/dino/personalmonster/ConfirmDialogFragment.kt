package com.dino.personalmonster

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ConfirmDialogFragment(private val onConfirm: (requestCode: Int) -> Unit):DialogFragment() {
    // コンストラクタ引数を設定して、OKが押されたときに呼ばれるようにする
//    interface ConfirmDialogListener {
//        fun onPositiveButtonClick(requestCode: Int)
//    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = arguments?.getString("message") ?: ""
        val requestCode = arguments?.getInt("requestCode") ?: 0

        // アクティビティにもフラグメントにも対応できるように汎用性を持たせる
//        val dialog = activity?.let {
//            val builder = AlertDialog.Builder(it)
            val dialog = AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setPositiveButton("OK") {_, _ ->
                    // ボタンが押されたらコンストラクタ引数のメソッドが働いて
                    onConfirm(requestCode)
//                    (activity as? ConfirmDialogListener)?.onPositiveButtonClick(requestCode)
                }
                .setNegativeButton("キャンセル", null)
                .create()
//        }
        return dialog
//        return dialog ?:throw IllegalStateException("ActivityがNullです")
//        return super.onCreateDialog(savedInstanceState)
    }
    
    companion object {
        fun newInstance(message: String, requestCode: Int, onConfirm: (Int) -> Unit): ConfirmDialogFragment {
           val fragment = ConfirmDialogFragment(onConfirm)
            fragment.arguments = Bundle().apply {
                putString("message", message)
                putInt("requestCode", requestCode)
            }
            return fragment
        }
    }
}