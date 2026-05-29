package com.dino.personalmonster


import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {


    private lateinit var tvMessage: TextView
    private lateinit var tvIndicator: TextView

    private val message = listOf(
        "ようこそ、パーソナルモンスターの世界へ！",
        "ここでは、あなたの性格を象徴する5体のモンスターを召喚し、育成することができます。",
        "さっそく、性格の診断を始めましょう！"
    )

    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        Log.i("画面遷移", "MainActivityに遷移しました。")


        // イントロ画面ではアプリタイトルを消す
        supportActionBar?.hide()


        // テキストの内容を設定（0番目から）
        tvMessage = findViewById<TextView>(R.id.tvMessage)
        showMessage(message[index])

        // 押されたときにインデックスを増やす
        tvMessage.setOnClickListener {
            index++

            // インデックス0～2まではテキストを表示
            if (index < message.size) {
                showMessage(message[index])
                // メッセージのサイズと同じになったら診断画面に遷移
            } else {
                val intent = Intent(this@MainActivity, DiagnosisActivity::class.java)
                intent.putExtra("type", DiagnosisActivity.Diagnosis.FULL.name)
                intent.putExtra("mode", ResultActivity.ResultMode.FIRST_DIAGNOSIS)

                startActivity(intent)
                finish()
            }
        }

        // 矢印のチカチカ
        tvIndicator = findViewById<TextView>(R.id.tvIndicator)
        blinkIndicator()

    }

    // イントロのテキストを表示する処理
    private fun showMessage(text: String) {
        // テキストの演出
        tvMessage.alpha = 0f
        tvMessage.text = text


        tvMessage.animate()
            .alpha(1f)
            .setDuration(1500)
            .start()
    }

    // 矢印のチカチカをさせる処理
    private fun blinkIndicator() {
        tvIndicator.animate()
            .alpha(0f)
            .setDuration(600)
            .withEndAction {
                tvIndicator.animate()
                    .alpha(1f)
                    .setDuration(600)
                    .withEndAction {
                        blinkIndicator()
                    }
                    .start()
            }
            .start()
    }




}


