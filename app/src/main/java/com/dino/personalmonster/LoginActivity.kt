package com.dino.personalmonster

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.LinearLayout

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.dino.personalmonster.ui.InsetsUtil
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import androidx.activity.result.contract.ActivityResultContracts


class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var googleSignInClient: GoogleSignInClient


    private val launcher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            val task =
                GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {

                val account =
                    task.getResult(ApiException::class.java)

                firebaseAuthWithGoogle(account.idToken!!)

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    "Googleログインに失敗しました。",
                    Toast.LENGTH_LONG
                ).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContentView(R.layout.activity_login)

        // Viewを取得して余白を入れる
        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)
        InsetsUtil.applySystemBarsInsets(rootLayout)


//        Log.i("画面遷移", "LoginActivityに遷移しました。")


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        // Googleログインｎ
        val gso = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val btnGoogleLogin = findViewById<Button>(R.id.btnGoogleLogin)
        btnGoogleLogin.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }


        // すでにアカウントがあるならMainActivity or ないならそのまま
//        if(currentUser != null) {
//            currentUser.getIdToken(true)
//                .addOnSuccessListener {
//                    // サーバーでも有効だと確認されたユーザーなら
//                    Log.i("tag", "User is valid on server: ${currentUser.uid}")
//                    routeUser()
//                }
//                .addOnFailureListener { e ->
//                    // サーバーで無効ならログイン画面を表示
//                    Log.i("tag", "User is invalid or deleted on server: ${e.message}")
//
//                }
//        } else {
//            // そもそもユーザーがいない
//            Log.i("tag", "No user found: Staing on Login screen")
//
//        }


        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        val tvMoveSignUp = findViewById<TextView>(R.id.tvMoveSignUp)
        val tvPasswordForget = findViewById<TextView>(R.id.tvPasswordForget)


        // ログイン処理
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if(task.isSuccessful) {
                            val user = auth.currentUser
                            if (user?.isEmailVerified == true) {
                                Toast.makeText(this, "ログインに成功しました。",Toast.LENGTH_LONG).show()

                                // 状態に応じて画面遷移
                                routeUser(user.uid)
                            } else {
                                // 未認証なら認証画面へ飛ぶ
                                Toast.makeText(this, "メールアドレスの認証を完了してください。",Toast.LENGTH_LONG).show()

                                val intent = Intent(this, VerificationActivity::class.java)
                                startActivity(intent)
                            }
                        } else {
                            Toast.makeText(this, "ログインに失敗しました。メールアドレスかパスワードが違うようです。",Toast.LENGTH_LONG).show()
                        }
                    }
            }
            else {
                Toast.makeText(this, "メールアドレスとパスワードを入力してください。", Toast.LENGTH_LONG).show()
            }
        }


        // ゲストとして始める
        val btnGuestLogin = findViewById<Button>(R.id.btnGuestLogin)
        btnGuestLogin.setOnClickListener {
            auth.signInAnonymously()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // 匿名用のユーザーIDを取得
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            Toast.makeText(this, "ゲストとしてログインに成功しました。",Toast.LENGTH_LONG).show()

                            // 状態に応じて画面遷移
                            routeUser(userId)
                        }
                    } else {
                        Toast.makeText(this, "ゲストとしてログインに失敗しました。",Toast.LENGTH_LONG).show()
                    }
                }
        }

        // アプリが起動したとき
//        if(currentUser != null) {
//            // 状態に応じて画面遷移
//            routeUser()
//        }

        // 新規登録への移動
        tvMoveSignUp.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)
        }

        // パスワード忘れの処理
        tvPasswordForget.setOnClickListener {
            val editText = EditText(this)

            editText.hint = "メールアドレス"
            editText.setPadding(32, 24, 32, 24)
            editText.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

            AlertDialog.Builder(this)
                .setTitle("パスワード再設定")
                .setMessage("登録したメールアドレスを入力してください")
                .setView(editText)
                .setPositiveButton("送信") { _, _ ->
                    val email = editText.text.toString().trim()
                    if (email.isEmpty()) {
                        Toast.makeText(this, "メールアドレスを入力してください。",Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }

                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener {
                            Toast.makeText(this, "入力されたメールアドレスにパスワード再設定のメールを送信しました。",Toast.LENGTH_LONG).show()

                        }
                }
                .setNegativeButton("キャンセル", null)
                .show()
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
                val intent = Intent(this@LoginActivity, MainActivity2::class.java)
                startActivity(intent)
                finishAffinity()

            } else {
                // 未診断ならイントロ画面に遷移
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finishAffinity()

            }
            // アカウント作成画面は消去
            finish()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    Toast.makeText(this, "Googleログインに成功しました。", Toast.LENGTH_LONG).show()

                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        routeUser(userId)
                    }
                } else {
                    Toast.makeText(this, "Firebase認証に失敗しました。", Toast.LENGTH_LONG).show()
                }
            }
    }



}