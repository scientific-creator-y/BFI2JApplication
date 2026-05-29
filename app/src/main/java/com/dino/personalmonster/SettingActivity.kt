package com.dino.personalmonster

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.dino.personalmonster.ui.InsetsUtil

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContentView(R.layout.activity_setting)

        // ルートView取得して余白入れる
        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)
        InsetsUtil.applySystemBarsInsets(rootLayout)


        // UIの取得
        val btUpdate = findViewById<Button>(R.id.btUpdate)
        val btHelp = findViewById<Button>(R.id.btHelp)

        // 更新確認の処理
        btUpdate.setOnClickListener {
            confirmUpdate()
        }

        // ガイドページに飛ぶ処理
        btHelp.setOnClickListener {
            val intent =Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.notion.so/Personal-Monster-36cd30cb9fca80e293dfd2e2316e3ba3?source=copy_link")
            )
            startActivity(intent)
        }
    }

    private fun confirmUpdate() {
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$packageName")
            )
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            startActivity(intent)
        }
    }
}