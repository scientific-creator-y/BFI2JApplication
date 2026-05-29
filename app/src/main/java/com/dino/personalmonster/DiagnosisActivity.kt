package com.dino.personalmonster

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.core.view.WindowCompat
import com.dino.personalmonster.ui.InsetsUtil
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.roundToInt

class DiagnosisActivity : AppCompatActivity() {

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // キーの定義
    private lateinit var type: String
    private lateinit var mode: String

    private var currentPage = 0
    private val pageSize = 10
    private lateinit var questions: List<Question>
    private lateinit var answers: MutableList<Int>


    // 本格版の次へor診断ボタン
//    private lateinit var btNext: Button

    // 診断数のキー管理
    enum class Diagnosis {
        FULL,
        SIMPLE
    }



    // 性格（5特性）の型
    enum class Trait(val label: String) {
        E("外向性"), 
        A("協調性"), 
        C("勤勉性"), 
        N("情緒安定性"),
        O("開放性")
        
    }

    // 性格（15ファセット）の型
    enum class Facet(val label: String) {
        // 外向性
        SOCIABILITY("社交性"), // 社交性
        ASSERTIVENESS("リーダーシップ"),  // 主張性（自己主張性）
        ENERGY_LEVEL("活発さ"), // 活動性（活力）

        // 協調性
        COMPASSION("思いやり"), // 思いやり
        RESPECTFULNESS("礼儀正しさ"), // 礼儀正しさ、敬意
        TRUST("素直さ"), // 信頼、信用

        // 誠実性
        ORGANIZATION("きっちり"), // 秩序、計画性
        PRODUCTIVENESS("努力家"), // 生産性、
        RESPONSIBILITY("責任感"), // 責任感

        // ネガティブ感情
        CALMNESS("冷静さ"), // 不安、
        RESILIENCE("打たれ強さ"), // 抑うつ
        EMOTIONAL_STABILITY("感情の安定"), // 感情の不安定性

        // 開放性
        INTELLECTUAL_CURIOSITY("知的好奇心"), // 知的好奇心
        AESTHETIC_SENSITIVITY("芸術性"), // 美的感受性
        CREATIVE_IMAGINATION("想像豊かさ") // 創造的想像力
    }


    // 質問のデータクラス
    data class Question(
        val text: String,
        val trait: Trait,
        val facet: Facet,
        val reverse: Boolean = false,

    )



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)



//        Log.i("DiagnosisActivityに遷移", "DiagnosisActivityに遷移しました。")

        // 診断タイプの受け取り
        type = intent.getStringExtra("type")?: Diagnosis.FULL.name
//        Log.i("診断タイプの受け取り", "診断タイプは${type}")

        // 診断モードの受け取り（仮診断の場合のみ）
        mode = intent.getStringExtra("mode")?: ResultActivity.ResultMode.FIRST_DIAGNOSIS.name

        // 簡易版の方の画面・ボタン処理
        if (type == Diagnosis.SIMPLE.name) {
            setContentView(R.layout.activity_diagnosis_simple)

            // ルートView取得して余白入れる
            val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)
            InsetsUtil.applyBottomInsets(rootLayout)


            val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)

            supportActionBar?.title = "性格診断"

            // 診断ボタンで計算＆結果画面に遷移
            val btSubmit = findViewById<Button>(R.id.btSubmit)
            btSubmit.setOnClickListener {
                if (type == Diagnosis.SIMPLE.name) {
                    submitSimple()
                } else {
                    submitFull()
                }
            }

        // 本格版の方の画面・ボタン処理
        } else {
            setContentView(R.layout.activity_diagnosis_full)

            // ルートView取得して余白入れる
            val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)
            InsetsUtil.applyBottomInsets(rootLayout)


            val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)

            supportActionBar?.title = "性格診断"

            // 質問の生成
            questions = createQuestions()

            // 回答初期化
            answers = MutableList(questions.size) { -1 }

            // ページを表示する
            showPage()

            val btNext = findViewById<Button>(R.id.btNext)
            btNext.setOnClickListener() {
                if (!collectAnswers()) return@setOnClickListener

//                currentPage++

                // 60項目まで進んでいれば
                if ((currentPage + 1) * pageSize >= questions.size) {
                    // ダイアログを出して本格版を提出
                    showConfirmDialog()
                } else {
                    // 次のページを表示する
                    currentPage++
                    showPage()
                }

            }
        }


        // Firebase取得
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()



    }

    // 戻る画面では1つ前のページに戻るだけ
    override fun onBackPressed() {
        if (currentPage > 0) {

            // 2ページ目以降（1以上）なら1つ前に戻るだけ
            currentPage--
            showPage()
        } else {
            // 1ページ目（0）なら前に戻る
            super.onBackPressed()

        }
    }

    // 診断前にダイアログを出す処理
    private fun showConfirmDialog() {
        val dialog = ConfirmDialogFragment.newInstance("この内容で診断しますか？", 1) { requestCode ->
            submitFull()
        }
        dialog.show(supportFragmentManager, "ConfirmDialog")
    }


    // 質問ページを表示する処理
    private fun showPage() {
        // 現在のページが「0」で渡ってきたらスタートは0、「1」で渡ってきたらスタートは10
        val start = currentPage * pageSize

        val tvPage = findViewById<TextView>(R.id.tvPage)
        tvPage.text = "${currentPage + 1}ページ  /  ${questions.size / pageSize}ページ"

        // 0から9までのループを回す
        for (i in 0 until pageSize) {
            // 質問番号は0 → 1 → 2 …
            val questionIndex = start + i

            // テキストとラジオグループのid習得
            val tvId = resources.getIdentifier("tvQuestion${i + 1}", "id", packageName)
            val rgId = resources.getIdentifier("rgItem${i + 1}", "id", packageName)


            val tv = findViewById<TextView>(tvId)
            val rg = findViewById<RadioGroup>(rgId)

            // 質問リストの0番目 → 1番目 →2番目…
            tv.text = questions[questionIndex].text

            // 前回回答の復元(ページ戻り用)
            val saved = answers[questionIndex]
                // 選択されていたら…
            if (saved != -1) {
                for (j in 0 until rg.childCount) {
                    // 選択されている番号で…
                    val rb = rg.getChildAt(j) as RadioButton
                    if (rb.tag.toString().toInt() == saved) {
                        // チェックにする
                        rb.isChecked = true
                    }
                }
            } else {
                // 選択されていないならチェックはつけない
                rg.clearCheck()
            }
            // 最後のページならUI調整
            if ((currentPage + 1) * pageSize >= questions.size) {
                val btNext = findViewById<Button>(R.id.btNext)
                btNext.text = "診断する"
            } else {
                val btNext = findViewById<Button>(R.id.btNext)
                btNext.text = "次へ"
            }

        }

        // スクロールの始めに巻き戻る
        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        scrollView.post {
            scrollView.smoothScrollTo(0, 0)
        }




    }

    // 回答を集めて集計する処理
    private fun collectAnswers(): Boolean {
        val start = currentPage * pageSize

        // ページに応じてラジオグループを取得し、ラジオグループごとに繰り返し処理
        for (i in 0 until pageSize) {

            val rgId = resources.getIdentifier("rgItem${i + 1}", "id", packageName)
            val rg = findViewById<RadioGroup>(rgId)

            val errorId = resources.getIdentifier("tvError${i + 1}", "id", packageName)
            val tvError = findViewById<TextView>(errorId)
            tvError.visibility = View.GONE

            // 選択されていない場合
            if (rg.checkedRadioButtonId == -1) {
                // エラーメッセージ表示
                tvError.visibility = View.VISIBLE
                Toast.makeText(this, "${start + i +1}問目が未回答です。", Toast.LENGTH_LONG).show()


                // 未選択のラジオグループにスクロールする → ちょっとうまくいかないので後回し
//                rg.requestFocus()

//                val scrollView = findViewById<ScrollView>(R.id.scrollView)
//                scrollView.post {
//                    scrollView.smoothScrollTo(0, rg.top)
//                }
                return false
            }

            val rb = findViewById<RadioButton>(rg.checkedRadioButtonId)
            val value = rb.tag.toString().toInt()

            answers[start + i]  = value
        }

        return true
    }


    // 本格版(60問）の診断
    private fun submitFull() {
//        Log.i("全部の回答", "全回答： ${answers}")

        // 5特性のそれぞれ合計とカウント
        val traitScores = mutableMapOf(
            Trait.E to 0,
            Trait.A to 0,
            Trait.C to 0,
            Trait.N to 0,
            Trait.O to 0,
        )
        val traitCounts = mutableMapOf<Trait, Int>()


        // 15下位特性のそれぞれ合計とカウント
        val facetScores = mutableMapOf<Facet, Int>()
        val facetCount = mutableMapOf<Facet, Int>()


        for (i in questions.indices) {
            val q = questions[i]
            val raw = answers[i]

            // 逆転項目対応
            val value = if (q.reverse) {
                6 - raw
            } else {
                raw
            }

            // それぞれの5特性に加算
            traitScores[q.trait] = traitScores[q.trait]!! + value
            traitCounts[q.trait] = (traitCounts[q.trait]?: 0) + 1


            // それぞれの1下位5特性に加算
            facetScores[q.facet] = (facetScores[q.facet]?: 0) + value
            facetCount[q.facet] = (facetCount[q.facet]?: 0) + 1

        }

//        Log.i("合計スコア", "${traitScores}")
//        Log.i("合計スコア（下位特性）", "${facetScores}")


        // 画面遷移の用意
        val intent = Intent(this, ResultActivity::class.java)


        // 5特性のそれぞれの平均
        val traitAverages = mutableMapOf<Trait, Double>()

        for (trait in Trait.values()) {
            val total = traitScores[trait]?: 0
            val count = traitCounts[trait]?: 1
            traitAverages[trait] = total.toDouble() / count

            // 平均値のスコアを渡す
            val score = traitAverages[trait]?.toFloat() ?: 0f
            intent.putExtra("${trait.name}", score)
//            Log.i("平均スコア", "${trait.label}：${score}")
        }


//        Log.i("平均スコア：外向性", "${traitAverages[Trait.E]}")
//        Log.i("平均スコア：調和性", "${traitAverages[Trait.A]}")
//        Log.i("平均スコア：勤勉性", "${traitAverages[Trait.C]}")
//        Log.i("平均スコア：情緒安定性", "${traitAverages[Trait.N]}")
//        Log.i("平均スコア：開放性", "${traitAverages[Trait.O]}")


        // 15下位特性のそれぞれの平均
        val facetAverages = mutableMapOf<Facet, Double>()

        for (facet in Facet.values()) {
            val total = facetScores[facet]?: 0
            val count = facetCount[facet]?: 1
            facetAverages[facet] = total.toDouble() / count

            // 平均値のスコアを渡す
            val score = facetAverages[facet]?.toFloat()?: 0f
            intent.putExtra("${facet.name}", score)
            Log.i("下位特性", "${facet.label}： ${score}")
        }


        // 画面遷移
//        intent.putExtra("e", traitAverages[Trait.E]?.toFloat()?: 0f)
//        intent.putExtra("a", traitAverages[Trait.A]?.toFloat()?: 0f)
//        intent.putExtra("c", traitAverages[Trait.C]?.toFloat()?: 0f)
//        intent.putExtra("n", traitAverages[Trait.N]?.toFloat()?: 0f)
//        intent.putExtra("o", traitAverages[Trait.O]?.toFloat()?: 0f)


        // 診断モードは初期診断or仮診断で渡す
        intent.putExtra("mode", mode)
        startActivity(intent)
        finishAffinity()


    }


    // 簡易版（10問）の方の診断
    private fun submitSimple() {

        val rgList = listOf(
            findViewById<RadioGroup>(R.id.rg_item1),
            findViewById<RadioGroup>(R.id.rg_item2),
            findViewById<RadioGroup>(R.id.rg_item3),
            findViewById<RadioGroup>(R.id.rg_item4),
            findViewById<RadioGroup>(R.id.rg_item5),
            findViewById<RadioGroup>(R.id.rg_item6),
            findViewById<RadioGroup>(R.id.rg_item7),
            findViewById<RadioGroup>(R.id.rg_item8),
            findViewById<RadioGroup>(R.id.rg_item9),
            findViewById<RadioGroup>(R.id.rg_item10),
        )

        val answers = mutableListOf<Int>()

        // 押された値をチェックする
        fun getValue(rg: RadioGroup): Int? {
            if (rg.checkedRadioButtonId == -1) return null

            val checkedButton = findViewById<RadioButton>(rg.checkedRadioButtonId)
            return checkedButton.tag.toString().toInt()
        }

        for (rg in rgList) {
            val value = getValue(rg)
            if (value == null) {
                Toast.makeText(this, "すべて選択してください。", Toast.LENGTH_LONG).show()
                return
            }
            answers.add(value)
        }

        // 逆転項目を処理
        answers[0] = 6 - answers[0] // 外向
        answers[2] = 6 - answers[2] // 勤勉
        answers[4] = 6 - answers[4] // 開放
        answers[6] = 6 - answers[6]  // 協調
        answers[8] = 6 - answers[8] // 情緒安定



        val eTotal = ((answers[5] + answers[0]) / 2.0).roundToInt()
        val aTotal = ((answers[6] + answers[1]) / 2.0).roundToInt()
        val cTotal = ((answers[7] + answers[2]) / 2.0).roundToInt()
        val nTotal = ((answers[8] + answers[3]) / 2.0).roundToInt()
        val oTotal = ((answers[9] + answers[4]) / 2.0).roundToInt()




        Log.i("診断された数値", "保存したのは${answers[0]}, ${answers[1]}, ${answers[2]}, ${answers[3]}, ${answers[4]}, ${answers[5]}, ${answers[6]}, ${answers[7]}, ${answers[8]}, ${answers[9]}")

        Log.i("計算値", "${eTotal}, ${aTotal}, ${cTotal}, ${nTotal}, ${oTotal}, ")


        val dialog = ConfirmDialogFragment.newInstance("診断しますか？", 1) {requestCode ->
            val intent = Intent(this@DiagnosisActivity, ResultActivity::class.java)
            intent.putExtra(Trait.E.name, eTotal.toFloat())
            intent.putExtra(Trait.A.name, aTotal.toFloat())
            intent.putExtra(Trait.C.name, cTotal.toFloat())
            intent.putExtra(Trait.N.name, nTotal.toFloat())
            intent.putExtra(Trait.O.name, oTotal.toFloat())


            intent.putExtra("mode", mode)

            startActivity(intent)
            finishAffinity()

        }
        dialog.show(supportFragmentManager, "ConfirmDialog")
    }


    // オプションアイテムを作る処理
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.initial_option_menu, menu)
        return true
    }


    // オプションアイテムを選択したときの処理
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val returnVal = true
        when (item.itemId) {

            // ログアウトのメニュー
            R.id.logout_option -> {
                val dialog = ConfirmDialogFragment.newInstance("ログアウトしますか？", 4) { requestCode ->
                    logout()
                }
                dialog.show(supportFragmentManager, "ConfirmDialog")
            }

            // 情報を出すメニュー
//            R.id.action_info -> {
//                showInfoDialog()
//            }

        }
        return returnVal
    }


    // ログアウトする処理
    private fun logout() {
        auth.signOut()

        val intent = Intent(this@DiagnosisActivity, LoginActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }





    // 質問を作る処理
    private fun createQuestions(): List<Question> {
        return listOf(
            // 0～10問
            Question("積極的で，社交的である", Trait.E, Facet.SOCIABILITY),
            Question("思いやりがあり，優しい", Trait.A, Facet.COMPASSION),
            Question("行き当たりばったりな方だ", Trait.C, Facet.ORGANIZATION, true), // 逆転項目
            Question("リラックスしていて，ストレスにうまく対処している", Trait.N, Facet.CALMNESS, false), // 逆転項目→正のキー（例外的に）
            Question("芸術的関心があまりない", Trait.O, Facet.AESTHETIC_SENSITIVITY, true), // 逆転項目
            Question("積極的な性格だ", Trait.E, Facet.ASSERTIVENESS),
            Question("礼儀正しく，他人に敬意をもって接する", Trait.A, Facet.RESPECTFULNESS),
            Question("だらだらと過ごす方だ", Trait.C, Facet.PRODUCTIVENESS, true), // 逆転項目
            Question("失敗を経験しても楽天的なままでいる", Trait.N, Facet.RESILIENCE, false), // 逆転項目→正のキー（例外的に）
            Question("色々な物事に対する好奇心が強い", Trait.O, Facet.INTELLECTUAL_CURIOSITY),

            // 11～20問
            Question("めったに興奮したり，熱狂したりしない", Trait.E, Facet.ENERGY_LEVEL, true), // 逆転項目
            Question("他人の欠点を見つけ出す方だ", Trait.A, Facet.TRUST, true), // 逆転項目
            Question("しっかりしていて，真面目だ", Trait.C, Facet.RESPONSIBILITY),
            Question("不機嫌になりやすく，感情の起伏が激しい", Trait.N, Facet.EMOTIONAL_STABILITY, true), // 普通の項目→負のキー（例外的に）
            Question("創意工夫が得意で，うまい方法を思いつくことができる", Trait.O, Facet.CREATIVE_IMAGINATION),
            Question(" 無口な方だ", Trait.E, Facet.SOCIABILITY, true), // 逆転項目
            Question("他人のことを思って心が痛むことはほとんどない", Trait.A, Facet.COMPASSION, true), // 逆転項目
            Question("几帳面で，規則正しく整えることが好きだ", Trait.C, Facet.ORGANIZATION),
            Question("神経が張り詰めることがある", Trait.N, Facet.CALMNESS,  true), // 普通の項目→負のキー（例外的に）
            Question("芸術，音楽，文学に魅了されている", Trait.O, Facet.AESTHETIC_SENSITIVITY),

            // 21～30問
            Question("上に立つ方で，リーダーとして活動する", Trait.E, Facet.ASSERTIVENESS),
            Question("他人と言い争いを始める", Trait.A, Facet.RESPECTFULNESS, true), // 逆転項目
            Question("なかなか作業に取り掛かることができない", Trait.C, Facet.PRODUCTIVENESS, true), // 逆転項目
            Question("安心感を抱いており，心地よい", Trait.N, Facet.RESILIENCE, false), // 逆転項目→正のキー（例外的に）
            Question("知的で哲学的な考察を避けるようにしている", Trait.O, Facet.INTELLECTUAL_CURIOSITY, true), // 逆転項目
            Question("他の人と比べて活発ではない", Trait.E, Facet.ENERGY_LEVEL, true), // 逆転項目
            Question("他人を大目に見る寛大な人間だ", Trait.A, Facet.TRUST),
            Question("少し不注意なところがある", Trait.C, Facet.RESPONSIBILITY, true), // 逆転項目
            Question("情緒が安定しており，簡単には取り乱さない", Trait.N, Facet.EMOTIONAL_STABILITY, false), // 逆転項目→正のキー（例外的に）
            Question("創造性がほとんどない", Trait.O, Facet.CREATIVE_IMAGINATION, true), // 逆転項目
//
//            // 31～40問
            Question("内気なところがあり，内向的である", Trait.E, Facet.SOCIABILITY, true), // 逆転項目
            Question("進んで手伝おうとし，他人の利益を優先する", Trait.A, Facet.COMPASSION),
            Question("物事をきれいに揃えたりまとめたりする", Trait.C, Facet.ORGANIZATION),
            Question("多くの悩みごとを抱えている", Trait.N, Facet.CALMNESS, true), // 普通の項目→負のキー（例外的に）
            Question("芸術と美を重視する", Trait.O, Facet.AESTHETIC_SENSITIVITY),
            Question("人々の行動を左右するような影響力をもつことは難しいと感じる", Trait.E, Facet.ASSERTIVENESS, true), // 逆転項目
            Question("他人を見下すことがある", Trait.A, Facet.RESPECTFULNESS, true),
            Question("手際よく行動し，物事を最後までやり遂げる", Trait.C, Facet.PRODUCTIVENESS),
            Question("よく悲しい気分になる", Trait.N, Facet.RESILIENCE, true), // 普通の項目→負のキー（例外的に）
            Question("考え方が複雑で，深く考える人間だ", Trait.O, Facet.INTELLECTUAL_CURIOSITY),
//
//            // 41～50問
            Question("活力にあふれている", Trait.E, Facet.ENERGY_LEVEL),
            Question("他人が考えていることを怪しんで不信感を抱く", Trait.A, Facet.TRUST, true), // 逆転項目
            Question("ちゃんとしていて，いつも周りから当てにされる", Trait.C, Facet.RESPONSIBILITY),
            Question("自分の感情をコントロールしている", Trait.N, Facet.EMOTIONAL_STABILITY, false), // 逆転項目→正のキー（例外的に）
            Question("ものごとを自由に心に思い描くのは難しい", Trait.O, Facet.CREATIVE_IMAGINATION, true), // 逆転項目
            Question("おしゃべりな方だ", Trait.E, Facet.SOCIABILITY),
            Question("冷淡で思いやりに欠けることがある", Trait.A, Facet.COMPASSION, true), // 逆転項目
            Question("乱雑なものはそのままにして，きれいにしない", Trait.C, Facet.ORGANIZATION, true), // 逆転項目
            Question("不安や恐れを感じることはめったにない", Trait.N, Facet.CALMNESS, false), // 逆転項目→正のキー（例外的に）
            Question("詩や演劇をつまらないと思う", Trait.O, Facet.AESTHETIC_SENSITIVITY, true), // 逆転項目
//
//            // 51～60問
            Question("他の人にリーダーシップを発揮してもらうほうが良いと思う  ", Trait.E, Facet.ASSERTIVENESS, true), // 逆転項目
            Question("他人に丁寧で，礼儀正しい", Trait.A, Facet.RESPECTFULNESS),
            Question("根気強く，与えられた課題が終わるまで取り組む", Trait.C, Facet.PRODUCTIVENESS),
            Question("憂うつになり，落胆する方だ", Trait.N, Facet.RESILIENCE, true), // 普通の項目→負のキー（例外的に）
            Question("抽象的な知識にはほとんど関心がない", Trait.O, Facet.INTELLECTUAL_CURIOSITY, true), // 逆転項目
            Question("情熱を大いに表に出す", Trait.E, Facet.ENERGY_LEVEL),
            Question("人々のいちばん良いところを思い浮かべる", Trait.A, Facet.TRUST),
            Question("無責任な行動をしてしまうことがある", Trait.C, Facet.RESPONSIBILITY, true), // 逆転項目
            Question("神経質で，感情的になりやすい", Trait.N, Facet.EMOTIONAL_STABILITY, true), // 普通の項目→負のキー（例外的に）
            Question("個性的で，新しいアイディアを思いつく", Trait.O, Facet.CREATIVE_IMAGINATION),
        )
    }
}