package com.dino.personalmonster

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.dino.personalmonster.ui.InsetsUtil
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class PrivacyActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContentView(R.layout.activity_privacy)

        // Viewを取得して余白を入れる
        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)
        InsetsUtil.applySystemBarsInsets(rootLayout)


        auth = FirebaseAuth.getInstance()


        // 画面のタイトル設定
        supportActionBar?.title = "アカウントやデータの削除"

        // ボタンの取得
        val btnDeleteData = findViewById<Button>(R.id.btnDeleteData)
        val btnDeleteAccount = findViewById<Button>(R.id.btnDeleteAccount)

        // データの削除
        btnDeleteData.setOnClickListener {
            val dialog = ConfirmDialogFragment.newInstance("すべての結果をリセットしますか？（診断結果、トレーニングの結果もすべてリセットされます。", 3) { requestCode ->

                val user = auth.currentUser
                val uid = user?.uid

                if (uid != null) {
                    deleteFirestoreData(uid) {
                        Toast.makeText(this, "このアカウントの診断・育成などのデータを削除しました。", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@PrivacyActivity, MainActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    }
                }

            }
            dialog.show(supportFragmentManager, "ConfirmDialog")
        }

        // アカウントの削除
        btnDeleteAccount.setOnClickListener {
            val editText = EditText(this)
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            val alertDialog = AlertDialog.Builder(this)
                .setTitle("パスワードを入力してください")
                .setView(editText)
                .setPositiveButton("認証",null)
                .setNegativeButton("キャンセル", null)
                .show()

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val password = editText.text.toString()

                if (password.isBlank()) {
                    editText.error = "パスワードを入力してください"
                    return@setOnClickListener
                }
                removeFirebaseAuth(password, editText, alertDialog)


            }
        }

    }



    // アカウントを削除する処理
    private fun removeFirebaseAuth(password: String, editText: EditText, alertDialog: AlertDialog) {

        val user = auth.currentUser ?: return
        val email = user.email ?: return

        val credential =
            EmailAuthProvider.getCredential(email, password)

        // 再認証
        user.reauthenticate(credential)
            .addOnSuccessListener {
                // パスワード入力ダイアログを閉じる
                alertDialog.dismiss()

                val dialog = ConfirmDialogFragment.newInstance("アカウントを削除しますか？（診断結果、トレーニングの結果もすべて削除されます。", 3) { requestCode ->
                    // Firestore削除
                    deleteFirestoreData(user.uid) {
                        // Auth削除
                        deleteAuthUser(user)
                    }
                }
                dialog.show(supportFragmentManager, "ConfirmDialog")

            }
            .addOnFailureListener { e ->
//                Toast.makeText(this, "再認証失敗: ${e.message}", Toast.LENGTH_LONG).show()
                editText.error = "パスワードが違います"
            }
    }


    // データを削除する処理
    private fun deleteFirestoreData(
        uid: String,
        onComplete: () -> Unit
    ) {

        val db = FirebaseFirestore.getInstance()
        val userDoc = db.collection("results").document(uid)

        userDoc.collection("ownedMonsters")
            .get()
            .addOnSuccessListener { ownedSnapshot ->

                val batch = db.batch()

                for (doc in ownedSnapshot.documents) {
                    batch.delete(doc.reference)
                }

                userDoc.collection("trainingMenus")
                    .get()
                    .addOnSuccessListener { trainingSnapshot ->
                        for (doc in trainingSnapshot.documents) {
                            batch.delete(doc.reference)
                        }

                        userDoc.collection("routines")
                            .get()
                            .addOnSuccessListener { routineSnapshots ->
                                for (doc in routineSnapshots.documents) {
                                    batch.delete(doc.reference)
                                }

                                // 最後に親ドキュメントを削除
                                batch.delete(userDoc)

                                batch.commit()
                                    .addOnSuccessListener {
                                        onComplete()
                                    }
                            }
                    }
            }
    }


    // アカウントを削除する処理
    private fun deleteAuthUser(user: FirebaseUser) {

        user.delete()
            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "アカウント削除完了",
                    Toast.LENGTH_LONG
                ).show()

                startActivity(
                    Intent(this, LoginActivity::class.java)
                )

                finishAffinity()
            }
    }



}