package com.dino.personalmonster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.dino.personalmonster.ui.InsetsUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContentView(R.layout.activity_sign_up)

        // Viewを取得して余白を入れる
        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)
        InsetsUtil.applySystemBarsInsets(rootLayout)


//        Log.i("画面遷移", "SignUpActivityに遷移しました。")

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSighUp = findViewById<Button>(R.id.btnSignUp)

        val tvMoveLogin = findViewById<TextView>(R.id.tvMoveLogin)

        // アカウント作成処理
        btnSighUp.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if(task.isSuccessful) {
                            val user = auth.currentUser
                            val userId = user?.uid

                            user?.sendEmailVerification()
                                ?.addOnSuccessListener {

                                    Toast.makeText(this, "認証メールを送信しました。", Toast.LENGTH_LONG).show()
                                    val intent = Intent(this, VerificationActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
//                            if (userId != null) {
////                                Log.i("tag", "アカウントの作成が完了。")
//
//                                // 新規登録ができたらイントロに遷移
//                                val intent = Intent(this@SignUpActivity, MainActivity::class.java)
//                                startActivity(intent)
//                            }

                        } else {
                            Toast.makeText(this, "アカウントの作成に失敗しました: ${task.exception?.message}",Toast.LENGTH_LONG).show()
                        }
                    }
            }
            else {
                Toast.makeText(this, "メールアドレスとパスワードを入力してください。", Toast.LENGTH_LONG).show()
            }
        }

        // ログインへの移動
        tvMoveLogin.setOnClickListener {
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}