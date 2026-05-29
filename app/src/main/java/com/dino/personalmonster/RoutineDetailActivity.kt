package com.dino.personalmonster

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.dino.personalmonster.ui.InsetsUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class RoutineDetailActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var routineId: String
    private lateinit var routineName: String
    private lateinit var etRoutineName: EditText
    private lateinit var etRoutineTrigger: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContentView(R.layout.activity_routine_detail)

        // Viewを取得して余白を入れる
        val rootLayout = findViewById<ScrollView>(R.id.rootLayout)
        InsetsUtil.applySystemBarsInsets(rootLayout)


        // 画面のタイトル設定
        supportActionBar?.title = "ルーティンの概要"

        // Firebaseインスタンス
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // ルーティンid取り出し
        routineId = intent.getStringExtra("routineId")?: ""
//        Log.i("遷移されたルーティンid", "わたってきたのは：${routineId}")

        // ルーティンタイトルやトリガーなどを読み出し・反映
        loadRoutineStatus()

        // ルーティンタイトル保存の処理
        etRoutineName = findViewById<EditText>(R.id.etRoutineName)
        etRoutineName.setOnFocusChangeListener {_, hasFocus ->
            if (!hasFocus) {
                val nameText = etRoutineName.text.toString().trim()

                if (nameText.isBlank()) {
                    etRoutineName.error = "ルーティン名に何か入れてください"
//                    Toast.makeText(this@RoutineDetailActivity, "ルーティン名に何か入れてください",Toast.LENGTH_LONG).show()
                    return@setOnFocusChangeListener
                }

                saveNameToFirebase(nameText)
                Toast.makeText(this, "ルーティンの名前を「${nameText}」にしました。", Toast.LENGTH_LONG).show()
            }
        }


        // トリガー保存の処理
        etRoutineTrigger = findViewById<EditText>(R.id.etRoutineTrigger)
        etRoutineTrigger.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val triggerText = etRoutineTrigger.text.toString().trim()
                saveTriggerToFirebase(triggerText)
            }
        }



        // ルーティン削除ボタン
        val btRoutineDelete = findViewById<Button>(R.id.btRoutineDelete)
        btRoutineDelete.setOnClickListener {
            val dialog = ConfirmDialogFragment.newInstance("このルーティンを削除しますか？（ルーティン内のアイテムは習慣リストに残ります）", 8) { requestCode ->
                // ルーティンを削除する
                deleteRoutine(routineId)
            }
            dialog.show(supportFragmentManager, "ConfirmDialog")

        }

        // 戻るボタン
        val btBackToMenuList = findViewById<Button>(R.id.btBackToMenuList)
        btBackToMenuList.setOnClickListener {
            finish()
        }



    }



    // 戻るボタンでもトリガーテキストを保存
    override fun onPause() {
        super.onPause()
            // ルーティンタイトルを保存
            val nameText = etRoutineName.text.toString().trim()
            saveNameToFirebase(nameText)

            // ルーティントリガーを保存
            val triggerText = etRoutineTrigger.text.toString().trim()
            saveTriggerToFirebase(triggerText)
    }


    // タイトルをFirebaseに保存
    private fun saveNameToFirebase(nameText: String) {
        if (nameText.isEmpty() ) {
            Toast.makeText(this@RoutineDetailActivity, "ルーティン名に何か入れてください",
                Toast.LENGTH_LONG).show()
            loadRoutineStatus()
            return
        }
        val user = auth.currentUser?: return
        db.collection("results")
            .document(user.uid)
            .collection("routines")
            .document(routineId)
            .update("name", nameText)
            .addOnSuccessListener {
//                Log.i("ルーティンタイトルの保存", "ルーティンタイトルの保存成功：${nameText}")
            }
    }

    // トリガーをFirebaseに保存
    private fun saveTriggerToFirebase(triggerText: String) {
        val user = auth.currentUser?: return
        db.collection("results")
            .document(user.uid)
            .collection("routines")
            .document(routineId)
            .update("triggerText", triggerText)
            .addOnSuccessListener {
//                Log.i("ルーティントリガーの保存", "ルーティントリガーの保存成功：${triggerText}")
            }
    }

    // ルーティンを削除する処理
    private fun deleteRoutine(routineId: String) {

        val user = auth.currentUser?: return
        val menuCollection = db.collection("results")
            .document(user.uid)
            .collection("trainingMenus")

        // 子→親idの削除：
        menuCollection.whereEqualTo("parentRoutineId", routineId)
            .get()
            .addOnSuccessListener { snapshots ->
                val batch = db.batch()
                // ルーティンidが含まれるアイテムから削除

                snapshots.forEach { doc ->
                    batch.update(doc.reference, "parentRoutineId", null)
                }

                // 親自体の削除
                val routineRef = db.collection("results")
                    .document(user.uid)
                    .collection("routines")
                    .document(routineId)

                batch.delete(routineRef)

                batch.commit()
                    .addOnSuccessListener {
                        Toast.makeText(this, "${routineName}のルーティンが削除されました", Toast.LENGTH_LONG).show()
                        finish()
                    }
            }


    }

    private fun loadRoutineStatus() {
        val user = auth.currentUser?: return
        db.collection("results")
            .document(user.uid)
            .collection("routines")
            .document(routineId)
            .get()
            .addOnSuccessListener { doc ->

                // ルーティンのタイトルを習得・反映
                etRoutineName = findViewById<EditText>(R.id.etRoutineName)
                routineName = doc.getString("name")?: ""
                etRoutineName.setText(routineName)

                // 習慣トリガーのテキストを取得・反映
                val triggerText = doc.getString("triggerText") ?:""
                etRoutineTrigger.setText(triggerText)
            }

    }
}