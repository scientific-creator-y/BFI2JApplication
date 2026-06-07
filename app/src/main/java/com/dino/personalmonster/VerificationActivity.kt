package com.dino.personalmonster

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.google.firebase.firestore.FirebaseFirestore

class VerificationActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var mode: String

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
        db = FirebaseFirestore.getInstance()

        // ボタンUI取得
        val btCheckVerification = findViewById<Button>(R.id.btCheckVerification)
        val btResendMail = findViewById<Button>(R.id.btResendMail)
        val tvLogout = findViewById<TextView>(R.id.tvLogout)

        // 遷移元の取得
        mode = intent.getStringExtra("mode") ?: "mode"

        // 認証確認のボタンが押されたときの処理
        btCheckVerification.setOnClickListener {
            reloadVerification()
        }


        // 再送信のボタンが押されたときの処理
        btResendMail.setOnClickListener {

            val user = auth.currentUser

            if (user == null) {
                Toast.makeText(this, "ログイン情報が見つかりません", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            user.sendEmailVerification()
                .addOnSuccessListener {
                    Toast.makeText(this, "認証メールを送信しました", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "送信失敗: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    Log.e("EMAIL_VERIFY", "送信失敗", e)
                }
        }

        // モード別処理
        if (mode == "fromGuestUpgrade") { // 正式登録なら別ログインは押せないようにする
            tvLogout.visibility = View.GONE
        } else {
            onBackPressedDispatcher.addCallback(this) {} // 初期登録ならバックボタンは押せないようにする（サインアップ画面以前に戻らないように）
        }


        tvLogout.setOnClickListener { // 別ログインが選ばれたらログイン画面に飛ばす
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }



    }

    override fun onResume() {
        super.onResume()
        reloadVerification()
    }

    private fun reloadVerification() {
        val user = auth.currentUser

        user?.reload()?.addOnSuccessListener {
            if (user.isEmailVerified) {
                // 認証できたらイントロ画面に遷移
                Toast.makeText(this, "メールアドレスが認証されました。", Toast.LENGTH_LONG).show()

                routeUser(user.uid)

//                if (mode == "fromGuestUpgrade") {
//                    val intent = Intent(this, MainActivity2::class.java)
//                    startActivity(intent)
//                    finish()
//                } else {
//                    val intent = Intent(this, MainActivity::class.java)
//                    startActivity(intent)
//                    finish()
//                }

            } else {
                Toast.makeText(this, "まだ認証されていません。", Toast.LENGTH_LONG).show()

            }
        }

    }


    private fun routeUser(userId: String) {
    // アカウントがあるなら遷移が発生
        db.collection("results")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val diagnosed = document.getBoolean("diagnosed") ?: false
                if (document.exists() && diagnosed) {
                    // 診断済みならホーム画面に遷移
                    val intent = Intent(this, MainActivity2::class.java)
                    startActivity(intent)
                    finishAffinity()

                } else {
                    // 未診断ならイントロ画面に遷移
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finishAffinity()

                }
                // アカウント作成画面は消去
                finish()
            }
    }
}