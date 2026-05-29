package com.dino.personalmonster

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.dino.personalmonster.ui.InsetsUtil
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GuestUpgradeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContentView(R.layout.activity_guest_upgrade)

        // Viewを取得して余白を入れる
        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)
        InsetsUtil.applySystemBarsInsets(rootLayout)


        // Firebaseから取得
        val auth = FirebaseAuth.getInstance()


        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        val btnUpgrade = findViewById<Button>(R.id.btnUpgrade)

        btnUpgrade.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {

                val credential = EmailAuthProvider.getCredential(email, password)
                auth.currentUser
                    ?.linkWithCredential(credential)
                    ?.addOnSuccessListener {
                        Toast.makeText(this, "アカウント登録完了", Toast.LENGTH_LONG).show()

                        finish()
                    }
                    ?.addOnFailureListener {
                        Toast.makeText(this, "登録失敗", Toast.LENGTH_LONG).show()

                    }
            } else {
                Toast.makeText(this, "メールアドレスとパスワードを入力してください。", Toast.LENGTH_LONG).show()
            }


        }


    }
}