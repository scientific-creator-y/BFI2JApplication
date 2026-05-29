package com.dino.personalmonster

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.dino.personalmonster.ui.InsetsUtil
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class VerificationActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContentView(R.layout.activity_verification)
        Log.i("画面遷移", "VerificationActivity opened")


        // ルートView取得して余白入れる
        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)
        InsetsUtil.applySystemBarsInsets(rootLayout)

        // Firebase
        auth = FirebaseAuth.getInstance()

        // ボタンUI取得
        val btCheckVerification = findViewById<Button>(R.id.btCheckVerification)
        val btResendMail = findViewById<Button>(R.id.btResendMail)
        val tvLogout = findViewById<TextView>(R.id.tvLogout)

        // 認証確認のボタンが押されたときの処理
        btCheckVerification.setOnClickListener {
            reloadVerification()
        }


        // 再送信のボタンが押されたときの処理
        btResendMail.setOnClickListener {
            val user = auth.currentUser

            user?.sendEmailVerification()
                ?.addOnSuccessListener {
                    Toast.makeText(this, "認証メールを送信しました。", Toast.LENGTH_LONG).show()
                }
        }

        // 別のアカウントでログインが選ばれたらログイン画面に飛ばす
        tvLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // バックボタンは押せないようにする（サインアップ画面以前に戻らないように）
        onBackPressedDispatcher.addCallback(this) {}

    }

    override fun onResume() {
        super.onResume()
        reloadVerification()
    }

    private fun reloadVerification() {

        auth.currentUser?.reload()?.addOnSuccessListener {
            val refreshedUser = auth.currentUser
            if (refreshedUser?.isEmailVerified == true) {
                // 認証できたらイントロ画面に遷移
                Toast.makeText(this, "メールアドレスが認証されました。", Toast.LENGTH_LONG).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "まだ認証されていません。", Toast.LENGTH_LONG).show()

            }
        }
    }
}