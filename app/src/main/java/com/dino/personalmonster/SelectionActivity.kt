package com.dino.personalmonster

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.WindowCompat
import com.dino.personalmonster.ui.InsetsUtil

class SelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContentView(R.layout.activity_selection)

        // Viewを取得して余白を入れる
        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)
        InsetsUtil.applySystemBarsInsets(rootLayout)


        // 画面のタイトル設定
        supportActionBar?.title = "診断を楽しむ"


        val btSimpleDiagnosis = findViewById<CardView>(R.id.btSimpleDiagnosis)
        val btFullDiagnosis = findViewById<CardView>(R.id.btFullDiagnosis)

        // タッチされたときに押し込む演出
        btSimpleDiagnosis.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.scaleX = 0.95f
                    v.scaleY = 0.95f
                }

                MotionEvent.ACTION_UP -> {
                    v.scaleX = 1f
                    v.scaleY = 1f
                }
            }
            false
        }

        btFullDiagnosis.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.scaleX = 0.95f
                    v.scaleY = 0.95f
                }

                MotionEvent.ACTION_UP -> {
                    v.scaleX = 1f
                    v.scaleY = 1f
                }
            }
            false
        }


        // ボタンが押されたときに診断画面に移る
        btSimpleDiagnosis.setOnClickListener {
            moveToDiagnosis(DiagnosisActivity.Diagnosis.SIMPLE.name)
        }
        btFullDiagnosis.setOnClickListener {
            moveToDiagnosis(DiagnosisActivity.Diagnosis.FULL.name)
        }



    }


    // 診断画面に移る処理
    private fun moveToDiagnosis(type: String) {
        val intent = Intent(this@SelectionActivity, DiagnosisActivity::class.java)
        // 診断尺度数のキーを渡す
        intent.putExtra("type", type)

        // 結果モードのキーは固定で「仮診断モード」
        intent.putExtra("mode", ResultActivity.ResultMode.ENJOY_DIAGNOSIS.name)
        startActivity(intent)
    }


}