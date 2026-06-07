package com.dino.personalmonster.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.dino.personalmonster.MainActivity2
import com.dino.personalmonster.R
import com.dino.personalmonster.data.InfoContent
import androidx.core.net.toUri

object ShowInfoUtil {

    // 情報を出す処理
    fun showInfoDialog(context: Context, content: InfoContent) {
        if (content.imageResource == null) {
            val builder = AlertDialog.Builder(context)
                .setTitle(content.title)
                .setMessage(content.message)
                .setPositiveButton("閉じる", null)

            if (content.url.isNotBlank()) {
                builder.setNeutralButton("より詳しく") { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW, content.url.toUri())
                    context.startActivity(intent)
                }
            }
            builder.show()
        } else {
            // 画像の場合
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_info_type, null)

            val tvMessage = dialogView.findViewById<TextView>(R.id.tvInfoAboutType)
            val ivImage = dialogView.findViewById<ImageView>(R.id.ivInfoAboutType)

            tvMessage.text = content.message
            ivImage.setImageResource(content.imageResource)


            AlertDialog.Builder(context)
                .setTitle(content.title)
                .setView(dialogView)
                .setPositiveButton("閉じる", null)
                .show()
        }

    }
}