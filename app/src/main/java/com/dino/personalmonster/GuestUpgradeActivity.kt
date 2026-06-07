package com.dino.personalmonster

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.CheckBox

import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.dino.personalmonster.ui.InsetsUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class GuestUpgradeActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    private val launcher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            val task =
                GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {

                val account =
                    task.getResult(ApiException::class.java)

                linkGoogleAccount(account.idToken!!)

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    "Googleとの連携に失敗しました。",
                    Toast.LENGTH_LONG
                ).show()
            }
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContentView(R.layout.activity_guest_upgrade)

        // Viewを取得して余白を入れる
        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)
        InsetsUtil.applySystemBarsInsets(rootLayout)

        // Firebaseから取得
        auth = FirebaseAuth.getInstance()

        // UI取得
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etPasswordConfirm = findViewById<EditText>(R.id.etPasswordConfirm)
        val cbShowPassword = findViewById<CheckBox>(R.id.cbShowPassword)


        val btnUpgrade = findViewById<Button>(R.id.btnUpgrade)
        val btnLinkGoogle = findViewById<Button>(R.id.btnLinkGoogle)


        // Google認証
        val gso = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        btnLinkGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent

            launcher.launch(signInIntent)
        }


        // パスワードの表示処理
        cbShowPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // チェックされていたら見せる
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                etPasswordConfirm.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                // チェックされていなかったら見せない
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                etPasswordConfirm.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

            etPassword.setSelection(etPassword.text.length)
            etPasswordConfirm.setSelection(etPasswordConfirm.text.length)

        }

        // メールアドレスで実行
        btnUpgrade.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            val passwordConfirm = etPasswordConfirm.text.toString().trim()

            if (password.length < 6) {
                etPassword.error = "6文字以上で入力してください"
                return@setOnClickListener
            }
            if (password != passwordConfirm) {
                etPasswordConfirm.error = "パスワードが一致しません"
                return@setOnClickListener
            }

            if (email.isNotEmpty() && password.isNotEmpty()) {

                val credential = EmailAuthProvider.getCredential(email, password)
                auth.currentUser
                    ?.linkWithCredential(credential)
                    ?.addOnSuccessListener { result ->
                        val user = result.user
                        user?.sendEmailVerification()
                            ?.addOnSuccessListener {

                                Toast.makeText(this, "認証メールを送信しました。", Toast.LENGTH_LONG).show()
                                val intent = Intent(this, VerificationActivity::class.java)
                                intent.putExtra("mode", "fromGuestUpgrade")
                                startActivity(intent)
                                finish()
                            }
                    }
                    ?.addOnFailureListener {
                        Toast.makeText(this, "登録失敗", Toast.LENGTH_LONG).show()

                    }
            } else {
                Toast.makeText(this, "メールアドレスとパスワードを入力してください。", Toast.LENGTH_LONG).show()
            }


        }


    }

    private fun linkGoogleAccount(idToken: String) {

        val credential =
            GoogleAuthProvider.getCredential(idToken, null)

        auth.currentUser
            ?.linkWithCredential(credential)
            ?.addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Googleアカウントと連携しました",
                    Toast.LENGTH_SHORT
                ).show()
                finish()

            }
            ?.addOnFailureListener { e ->

                if (e is FirebaseAuthUserCollisionException) {

                    Toast.makeText(
                        this,
                        "このGoogleアカウントは既に登録されています",
                        Toast.LENGTH_LONG
                    ).show()

                } else {

                    Toast.makeText(
                        this,
                        "連携に失敗しました",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

//                Toast.makeText(
//                    this,
//                    "連携に失敗しました",
//                    Toast.LENGTH_SHORT
//                ).show()

    }
}