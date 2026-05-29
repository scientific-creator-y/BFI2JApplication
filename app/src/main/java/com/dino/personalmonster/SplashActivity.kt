package com.dino.personalmonster

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Firebaseからのロードができたかのフラグ
//    private var isReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
//        val splashScreen = installSplashScreen()
//        splashScreen.setKeepOnScreenCondition {
//            !isReady
//        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


//        Log.i("画面遷移", "SplashActivityが開かれました。")


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser


        // アカウントがないなら
        if (currentUser == null) {
            // 読み込めたら用意完了
//            isReady = true

            // ログイン画面に
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()

        } else {


            // アカウントがあるならホームorイントロ画面へ
            db.collection("results")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    val diagnosed = document.getBoolean("diagnosed") ?: false


                    if (document.exists() && diagnosed) {
                        // 読み込めたら用意完了
//                        isReady = true

                        // 診断済みならホーム画面に遷移
                        val intent = Intent(this@SplashActivity, MainActivity2::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        // 読み込めたら用意完了
//                        isReady = true

                        // 未診断ならイントロ画面に遷移
                        val intent = Intent(this@SplashActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    }
                    // この画面は消去
                    finish()
                }
        }
    }



}