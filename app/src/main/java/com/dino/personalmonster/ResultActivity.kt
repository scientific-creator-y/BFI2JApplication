package com.dino.personalmonster

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.dino.personalmonster.DiagnosisActivity.Trait
import com.dino.personalmonster.ui.InsetsUtil
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.roundToInt
import com.github.mikephil.charting.formatter.ValueFormatter


class ResultActivity : AppCompatActivity() {

    enum class ResultMode {
        FIRST_DIAGNOSIS,
        PROFILE,
        ENJOY_DIAGNOSIS
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var mode: String

    private lateinit var eLevel: String
    private lateinit var aLevel: String
    private lateinit var cLevel: String
    private lateinit var nLevel: String
    private lateinit var oLevel: String

    private lateinit var eMonsterId: String
    private lateinit var aMonsterId: String
    private lateinit var cMonsterId: String
    private lateinit var nMonsterId: String
    private lateinit var oMonsterId: String

    private var eInitial = 0f
    private var aInitial = 0f
    private var cInitial = 0f
    private var nInitial = 0f
    private var oInitial = 0f

    private var sociability = 0f
    private var assertiveness = 0f
    private var energyLevel = 0f
    private var compassion = 0f
    private var respectfulness = 0f
    private var trust = 0f
    private var organization = 0f
    private var productiveness = 0f
    private var responsibility = 0f
    private var calmness = 0f
    private var resilience = 0f
    private var stability = 0f
    private var intellectualCuriosity = 0f
    private var aestheticSensitivity = 0f
    private var creativeImagination = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)


        setContentView(R.layout.activity_result)

        // UI取得
        val rootLayout = findViewById<ScrollView>(R.id.rootLayout)
        val rootSubTraitE = findViewById<LinearLayout>(R.id.rootSubTraitE)
        val rootSubTraitA = findViewById<LinearLayout>(R.id.rootSubTraitA)
        val rootSubTraitC = findViewById<LinearLayout>(R.id.rootSubTraitC)
        val rootSubTraitN = findViewById<LinearLayout>(R.id.rootSubTraitN)
        val rootSubTraitO = findViewById<LinearLayout>(R.id.rootSubTraitO)


        // Viewを取得して余白を入れる
        InsetsUtil.applySystemBarsInsets(rootLayout)


        // 画面のタイトル設定
        supportActionBar?.title = "あなたの性格診断の結果"


        // 渡ってきた表示モードを習得
        mode = intent.getStringExtra("mode")?: ResultMode.PROFILE.name
//        Log.i("表示モードの習得", "取り出せたのは${mode}")


        // 渡ってきた初期値を習得
        eInitial = intent.getFloatExtra(DiagnosisActivity.Trait.E.name, 0f)
        aInitial = intent.getFloatExtra(DiagnosisActivity.Trait.A.name, 0f)
        cInitial = intent.getFloatExtra(DiagnosisActivity.Trait.C.name, 0f)
        nInitial = intent.getFloatExtra(DiagnosisActivity.Trait.N.name, 0f)
        oInitial = intent.getFloatExtra(DiagnosisActivity.Trait.O.name, 0f)

        // 下位特性の平均値
        sociability = intent.getFloatExtra(DiagnosisActivity.Facet.SOCIABILITY.name, 0f)
        assertiveness = intent.getFloatExtra(DiagnosisActivity.Facet.ASSERTIVENESS.name, 0f)
        energyLevel = intent.getFloatExtra(DiagnosisActivity.Facet.ENERGY_LEVEL.name, 0f)

        compassion = intent.getFloatExtra(DiagnosisActivity.Facet.COMPASSION.name, 0f)
        respectfulness = intent.getFloatExtra(DiagnosisActivity.Facet.RESPECTFULNESS.name, 0f)
        trust = intent.getFloatExtra(DiagnosisActivity.Facet.TRUST.name, 0f)

        organization = intent.getFloatExtra(DiagnosisActivity.Facet.ORGANIZATION.name, 0f)
        productiveness = intent.getFloatExtra(DiagnosisActivity.Facet.PRODUCTIVENESS.name , 0f)
        responsibility = intent.getFloatExtra(DiagnosisActivity.Facet.RESPONSIBILITY.name , 0f)

        calmness = intent.getFloatExtra(DiagnosisActivity.Facet.CALMNESS.name , 0f)
        resilience = intent.getFloatExtra(DiagnosisActivity.Facet.RESILIENCE.name , 0f)
        stability = intent.getFloatExtra(DiagnosisActivity.Facet.EMOTIONAL_STABILITY.name , 0f)

        intellectualCuriosity = intent.getFloatExtra(DiagnosisActivity.Facet.INTELLECTUAL_CURIOSITY.name , 0f)
        aestheticSensitivity = intent.getFloatExtra(DiagnosisActivity.Facet.AESTHETIC_SENSITIVITY.name , 0f)
        creativeImagination = intent.getFloatExtra(DiagnosisActivity.Facet.CREATIVE_IMAGINATION.name , 0f)

//        Log.i("渡ってきた平均値", "取り出せたのは${eInitial}, ${aInitial}, ${cInitial}, ${nInitial}, ${oInitial}")
//        Log.i("渡ってきた平均値(下位特性）", "取り出せたのは${sociability}, ${compassion}, ${organization}, ${calmness}, ${intellectualCuriosity}")



        // 共有ボタンをタップしたときの処理
        val ivShareResult = findViewById<ImageView>(R.id.ivShareResult)
        ivShareResult.setOnClickListener {
            shareRadarChart()
        }


        // レーダーチャートを作成
        createInitialRadarChart()



        // テキストのUI習得
        val tvEtitle = findViewById<TextView>(R.id.tvETitle)
        val tvAtitle = findViewById<TextView>(R.id.tvATitle)
        val tvCtitle = findViewById<TextView>(R.id.tvCTitle)
        val tvNtitle = findViewById<TextView>(R.id.tvNTitle)
        val tvOtitle = findViewById<TextView>(R.id.tvOTitle)

        val tvEDesc = findViewById<TextView>(R.id.tvEDesc)
        val tvADesc = findViewById<TextView>(R.id.tvADesc)
        val tvCDesc = findViewById<TextView>(R.id.tvCDesc)
        val tvNDesc = findViewById<TextView>(R.id.tvNDesc)
        val tvODesc = findViewById<TextView>(R.id.tvODesc)

        // 特性ごとの平均値を設定 → 0.3+-の場合
        val eAverage = 2.72f // 3.02→高い、2.42→低い
        val aAverage = 3.26f // 3.56→高い、2.96→低い
        val cAverage = 3.18f // 3.48→高い、2.88→低い
        val nAverage = 3.06f // 3.36→高い、2.76→低い
        val oAverage = 3.08f // 3.38→高い、2.78→低い


        // 条件分岐で結果を表示＆レベルを戻り値で返す
        fun resultShow(
            // 条件分岐に必要な値
            total: Float,
            average: Float,

            // 反映するビュー
            title: TextView,
            desc: TextView,

            // 表示する実際の文字列（stringファイルから）
            stringHighTitle: Int,
            stringLowTitle: Int,
            stringMidTitle: Int,
            stringHighDesc: Int,
            stringLowDesc: Int,
            stringMidDesc: Int,
        ): String {
            return if (total > average + 0.3f) {
                title.text = getString(stringHighTitle)
                desc.text = getString(stringHighDesc)
                "high"
            } else if (total < average - 0.3f) {
                title.text = getString(stringLowTitle)
                desc.text = getString(stringLowDesc)
                "low"
            } else {
                title.text = getString(stringMidTitle)
                desc.text = getString(stringMidDesc)
                "mid"
            }
        }
        eLevel = resultShow(
            total = eInitial,
            average = eAverage,
            title = tvEtitle,
            desc = tvEDesc,
            R.string.tv_resultHighTitleE,
            R.string.tv_resultLowTitleE,
            R.string.tv_resultMiddleTitleE,
            R.string.tv_resultHighDescE,
            R.string.tv_resultLowDescE,
            R.string.tv_resultMiddleDescE
        )
        aLevel = resultShow(
            aInitial,
            aAverage,
            tvAtitle,
            tvADesc,
            R.string.tv_resultHighTitleA,
            R.string.tv_resultLowTitleA,
            R.string.tv_resultMiddleTitleA,
            R.string.tv_resultHighDescA,
            R.string.tv_resultLowDescA,
            R.string.tv_resultMiddleDescA
        )
        cLevel = resultShow(
            cInitial,
            cAverage,
            tvCtitle,
            tvCDesc,
            R.string.tv_resultHighTitleC,
            R.string.tv_resultLowTitleC,
            R.string.tv_resultMiddleTitleC,
            R.string.tv_resultHighDescC,
            R.string.tv_resultLowDescC,
            R.string.tv_resultMiddleDescC
        )
        nLevel = resultShow(
            nInitial,
            nAverage,
            tvNtitle,
            tvNDesc,
            R.string.tv_resultHighTitleN,
            R.string.tv_resultLowTitleN,
            R.string.tv_resultMiddleTitleN,
            R.string.tv_resultHighDescN,
            R.string.tv_resultLowDescN,
            R.string.tv_resultMiddleDescN
        )
        oLevel = resultShow(
            oInitial,
            oAverage,
            tvOtitle,
            tvODesc,
            R.string.tv_resultHighTitleO,
            R.string.tv_resultLowTitleO,
            R.string.tv_resultMiddleTitleO,
            R.string.tv_resultHighDescO,
            R.string.tv_resultLowDescO,
            R.string.tv_resultMiddleDescO
        )


        // 条件分岐で初期モンスターを確定
        fun checkMonster(
            // 必要な値
            total: Float,
            average: Float,

            highId: String,
            lowId: String,
            midId: String
        ): String {
            return if (total > average + 0.3f) {
                highId
            } else if (total < average - 0.3f){
                lowId
            } else {
                midId
            }
        }
        eMonsterId = checkMonster(eInitial, eAverage, "001", "011", "006")
        aMonsterId = checkMonster(aInitial, aAverage, "002", "012", "007")
        cMonsterId = checkMonster(cInitial, cAverage, "003", "013", "008")
        nMonsterId = checkMonster(nInitial, nAverage, "004", "014", "009")
        oMonsterId = checkMonster(oInitial, oAverage, "005", "015", "010")
//        Log.i("モンスターのid", "${eMonsterId}, ${aMonsterId}, ${cMonsterId}, ${nMonsterId}, ${oMonsterId},")


        // 下位特性の表示
        val tvESubTrait1 = findViewById<TextView>(R.id.tvESubTrait1)
        val tvESubTrait2 = findViewById<TextView>(R.id.tvESubTrait2)
        val tvESubTrait3 = findViewById<TextView>(R.id.tvESubTrait3)

        val tvASubTrait1 = findViewById<TextView>(R.id.tvASubTrait1)
        val tvASubTrait2 = findViewById<TextView>(R.id.tvASubTrait2)
        val tvASubTrait3 = findViewById<TextView>(R.id.tvASubTrait3)

        val tvCSubTrait1 = findViewById<TextView>(R.id.tvCSubTrait1)
        val tvCSubTrait2 = findViewById<TextView>(R.id.tvCSubTrait2)
        val tvCSubTrait3 = findViewById<TextView>(R.id.tvCSubTrait3)

        val tvNSubTrait1 = findViewById<TextView>(R.id.tvNSubTrait1)
        val tvNSubTrait2 = findViewById<TextView>(R.id.tvNSubTrait2)
        val tvNSubTrait3 = findViewById<TextView>(R.id.tvNSubTrait3)

        val tvOSubTrait1 = findViewById<TextView>(R.id.tvOSubTrait1)
        val tvOSubTrait2 = findViewById<TextView>(R.id.tvOSubTrait2)
        val tvOSubTrait3 = findViewById<TextView>(R.id.tvOSubTrait3)


        tvESubTrait1.text = "${sociability} / 5"
        tvESubTrait2.text = "${assertiveness} / 5"
        tvESubTrait3.text = "${energyLevel} / 5"

        tvASubTrait1.text = "${compassion} / 5"
        tvASubTrait2.text = "${respectfulness} / 5"
        tvASubTrait3.text = "${trust} / 5"

        tvCSubTrait1.text = "${organization} / 5"
        tvCSubTrait2.text = "${productiveness} / 5"
        tvCSubTrait3.text = "${responsibility} / 5"

        tvNSubTrait1.text = "${calmness} / 5"
        tvNSubTrait2.text = "${resilience} / 5"
        tvNSubTrait3.text = "${stability} / 5"

        tvOSubTrait1.text = "${intellectualCuriosity} / 5"
        tvOSubTrait2.text = "${aestheticSensitivity} / 5"
        tvOSubTrait3.text = "${creativeImagination} / 5"



        // 結果の表示とともにFirebaseへの保存
        auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser?.uid
        if (userId != null && mode == ResultMode.FIRST_DIAGNOSIS.name) {
            registerToFirestore(userId)
        }

        // 性格の詳細へのページ
        val btToAlalysisE = findViewById<Button>(R.id.btToAnalysisE)
        val btToAlalysisA = findViewById<Button>(R.id.btToAnalysisA)
        val btToAlalysisC = findViewById<Button>(R.id.btToAnalysisC)
        val btToAlalysisN = findViewById<Button>(R.id.btToAnalysisN)
        val btToAlalysisO = findViewById<Button>(R.id.btToAnalysisO)

        btToAlalysisE.setOnClickListener { openAnalysisPage("E", eInitial, eLevel) }
        btToAlalysisA.setOnClickListener { openAnalysisPage("A", aInitial, aLevel) }
        btToAlalysisC.setOnClickListener { openAnalysisPage("C", cInitial, cLevel) }
        btToAlalysisN.setOnClickListener { openAnalysisPage("N", nInitial, nLevel) }
        btToAlalysisO.setOnClickListener { openAnalysisPage("O", oInitial, oLevel) }


        // モードによってUIの決定
//        val btSave = findViewById<Button>(R.id.btSave)
        val btBack = findViewById<Button>(R.id.btBack)
        val btBackToHome = findViewById<Button>(R.id.btBackToHome)
        val btGoToHome = findViewById<Button>(R.id.btGoToHome)
//        val btBackToDiagnosis = findViewById<Button>(R.id.btBackToDiagnosis)

        val tvIntroText = findViewById<TextView>(R.id.tvIntroText)
        val tvLastText = findViewById<TextView>(R.id.tvLastText)

            // 表示モードによってUIの表示切り替え
        when (mode) {
            ResultMode.FIRST_DIAGNOSIS.name -> { // 初期診断なら、イントロ・アウトロ・保存ボタン
                tvIntroText.visibility = View.VISIBLE
                tvLastText.visibility = View.VISIBLE
                btBackToHome.visibility = View.GONE
                btGoToHome.visibility = View.VISIBLE
//                btBackToDiagnosis.visibility = View.VISIBLE
                btBack.visibility = View.GONE
//                btSave.visibility = View.VISIBLE
            }
            ResultMode.PROFILE.name -> { // プロフィール確認なら、戻るボタンだけ
                tvIntroText.visibility = View.GONE
                tvLastText.visibility = View.GONE
                btBackToHome.visibility = View.GONE
                btGoToHome.visibility = View.GONE
//                btBackToDiagnosis.visibility = View.GONE
                btBack.visibility = View.VISIBLE
//                btSave.visibility = View.GONE


            }
            else -> {                                      // 仮診断モードならイントロ・ホームへ戻るボタン
                tvIntroText.visibility = View.VISIBLE
                tvLastText.visibility = View.GONE
                btBackToHome.visibility = View.VISIBLE
                btGoToHome.visibility = View.GONE
//                btBackToDiagnosis.visibility = View.GONE
                btBack.visibility = View.GONE
//                btSave.visibility = View.GONE

                // 下位特性は表示せず
                rootSubTraitE.visibility = View.GONE
                rootSubTraitA.visibility = View.GONE
                rootSubTraitC.visibility = View.GONE
                rootSubTraitN.visibility = View.GONE
                rootSubTraitO.visibility = View.GONE

            }
        }

        // 保存ボタンで値をFirebaseに保存（初期診断モード）
//        btSave.setOnClickListener {
//            val dialog =
//                ConfirmDialogFragment.newInstance("診断結果を保存しますか？", 2) { requestCode ->
//                    // 値を確定する
//                    auth = FirebaseAuth.getInstance()
//
//                    val userId = auth.currentUser?.uid
//                    if (userId != null) {
//                        registerToFirestore(userId)
//                    }
//                }
//            dialog.show(supportFragmentManager, "ConfirmDialog")
//        }

        // ホームボタンに戻る（仮診断モード）
        btBackToHome.setOnClickListener {
            val dialog = ConfirmDialogFragment.newInstance("ホーム画面へ戻りますか？", 3) { requestCode ->
                val intent = Intent(this@ResultActivity, MainActivity2::class.java)
                startActivity(intent)

                finishAffinity()
            }
            dialog.show(supportFragmentManager, "ConfirmDialog")
        }


        // ホームボタンに進む（初期診断モード）
        btGoToHome.setOnClickListener {
            val intent = Intent(this@ResultActivity, MainActivity2::class.java)
            startActivity(intent)

            finishAffinity()
        }

        // 診断し直す（初期モード）
//        btBackToDiagnosis.setOnClickListener {
//            val dialog = ConfirmDialogFragment.newInstance("診断を最初からやり直しますか？", 4) { requestCode ->
//                // Firebaseのデータ消去
//                resetFirestore()
//            }
//            dialog.show(supportFragmentManager, "ConfirmDialog")
//        }


    }



    // レーダーチャート
    private fun createInitialRadarChart() {
        // レーダーチャートを取得
        val radarChart = findViewById<RadarChart>(R.id.radarChart)
        // 描画される前にリセット
        radarChart.clear()
        radarChart.setExtraOffsets(40f, 40f, 40f, 40f)

        val eDisplay = (eInitial* 10).roundToInt().toFloat()
        val aDisplay = (aInitial* 10).roundToInt().toFloat()
        val cDisplay = (cInitial* 10).roundToInt().toFloat()
        val nDisplay = (nInitial* 10).roundToInt().toFloat()
        val oDisplay = (oInitial* 10).roundToInt().toFloat()


        val entry = listOf(
            RadarEntry(eDisplay),
            RadarEntry(aDisplay),
            RadarEntry(cDisplay),
            RadarEntry(nDisplay),
            RadarEntry(oDisplay)
        )

        // データセットを作成
        val dataset = RadarDataSet(entry, "").apply {
            color = ContextCompat.getColor(this@ResultActivity, R.color.radarInitialColor)
            fillColor = ContextCompat.getColor(this@ResultActivity, R.color.radarInitialFillColor)
            fillAlpha = 120
            setDrawFilled(true)

            lineWidth = 2.5f
            valueTextSize = 12f
            valueTextColor = Color.DKGRAY
        }

        // 小数点削除
        dataset.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String? {
                return value.toInt().toString()
            }
        }


        // データセットをデータに流し込む
        val radarData = RadarData(dataset)
        radarChart.data = radarData

        // X軸(ラベル)の設定
        val labels = listOf("社交力", "調和力", "意志力", "精神力", "探究力")
        radarChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        radarChart.xAxis.textSize = 12f
        radarChart.xAxis.textColor = Color.DKGRAY

        // Y軸の設定
        radarChart.yAxis.apply {
            axisMinimum = 0f
            axisMaximum = 50f
            setDrawLabels(false)
        }


        // 網目デザインの設定
        radarChart.webLineWidth = 1f
        radarChart.webColor = Color.LTGRAY
        radarChart.webLineWidthInner = 1f
        radarChart.webColorInner = Color.LTGRAY
        radarChart.webAlpha = 100

        // その他設定
        radarChart.isRotationEnabled = false
        radarChart.description.isEnabled = false
        radarChart.legend.isEnabled = false

//        radarChart.setBackgroundColor(Color.TRANSPARENT)


        // アニメーション
        radarChart.animateY(1000)
        // 描画を更新する
        radarChart.invalidate()

    }

    // レーダーチャートを共有する
    private fun shareRadarChart(){
        val intent = Intent(this@ResultActivity, ShareLayoutActivity::class.java)
        intent.putExtra(Trait.E.name, eInitial)
        intent.putExtra(Trait.A.name, aInitial)
        intent.putExtra(Trait.C.name, cInitial)
        intent.putExtra(Trait.N.name, nInitial)
        intent.putExtra(Trait.O.name, oInitial)



        startActivity(intent)
    }



        // 詳細ボタンで遷移して値を渡す処理
    private fun openAnalysisPage(type: String, total: Float, level: String) {
        val score = total

        val intent = Intent(this, ResultDetailActivity::class.java)
        intent.putExtra("type", type)
        intent.putExtra("score", score)
        intent.putExtra("level", level)


        startActivity(intent)
    }




    // データベースに値を登録する処理
    private fun registerToFirestore(userId: String) {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userResultRef = db.collection("results").document(userId)




        // データをFirebase用に用意
        val eCurrent = (eInitial * 10).roundToInt()
        val aCurrent = (aInitial * 10).roundToInt()
        val cCurrent = (cInitial * 10).roundToInt()
        val nCurrent = (nInitial * 10).roundToInt()
        val oCurrent = (oInitial * 10).roundToInt()

        // HP担当のモンスターは固定で016
        val hpMonsterId = "016"
        val hpCurrent = "30".toInt()


        // まずは初期値と現在値を保存する
        val personalityData = mapOf(
            // 初期値
            "initialData" to mapOf(
                "extraversion" to eInitial,
                "agreeableness" to aInitial,
                "conscientiousness" to cInitial,
                "neuroticism" to nInitial,
                "openness" to oInitial
            ),

            // 下位特性
            "facetData" to mapOf(
                "extraversion" to mapOf(
                    "sociability" to sociability,
                    "assertiveness" to assertiveness,
                    "energyLevel" to energyLevel,
                ),
                "agreeableness" to mapOf(
                    "compassion" to compassion,
                    "respectfulness" to respectfulness,
                    "trust" to trust,
                ),
                "conscientiousness" to mapOf(
                    "organization" to organization,
                    "productiveness" to productiveness,
                    "responsibility" to responsibility,
                ),
                "neuroticism" to mapOf(
                    "calmness" to calmness,
                    "resilience" to resilience,
                    "stability" to stability,
                ),
                "openness" to mapOf(
                    "intellectualCuriosity" to intellectualCuriosity,
                    "aestheticSensitivity" to aestheticSensitivity,
                    "creativeImagination" to creativeImagination,
                )
            ),

            // 現在値(HPを追加)
            "currentData" to mapOf(
                "social" to eCurrent,
                "harmony" to aCurrent,
                "will" to cCurrent,
                "mental" to nCurrent,
                "explore" to oCurrent,
                // HPは現在値のみ保存
                "HP" to hpCurrent
            ),

            // 高いor中ぐらいor低い
            "typeData" to mapOf(
                "extraversion" to eLevel,
                "agreeableness" to aLevel,
                "conscientiousness" to cLevel,
                "neuroticism" to nLevel,
                "openness" to oLevel
            )
        )


        // 診断されたモンスターをユーザーの装備中としてセットする
        val equippedMonsters = mapOf(
            "social" to eMonsterId,
            "harmony" to aMonsterId,
            "will" to cMonsterId,
            "mental" to nMonsterId,
            "explore" to oMonsterId,
            "HP" to  hpMonsterId,

        )



        // ユーザーの基本情報をセット
        userResultRef.set(
            mapOf(
                "diagnosed" to true,
                "createAt" to FieldValue.serverTimestamp(),
                "personality" to personalityData,
                "equippedMonsters" to equippedMonsters
            )
        )




        // モンスターを登録
        // 保持しているモンスターを保存する
        val ownedMonsterData = listOf(
            eMonsterId,
            aMonsterId,
            cMonsterId,
            nMonsterId,
            oMonsterId,
            hpMonsterId
        )

        // 保存した所有データの数をカウントする
        var savedCount = 0

        ownedMonsterData.forEach { monsterId ->
            // マスターデータからスロットの情報を取得する
            db.collection("monsters")
                .document(monsterId)
                .get()
                .addOnSuccessListener { snapshot ->
                    val slot = snapshot.getString("slot") ?: return@addOnSuccessListener


                    // 所有モンスターデータに入れる情報をまとめる
                    val ownedMonsterInfo = hashMapOf(
                        "monsterId" to monsterId,
                        "equipped" to true,
                        "slot" to slot,
                        "obtainedAt" to FieldValue.serverTimestamp(),

                        // 初期値も設定
                        "level" to 1,
                        "exp" to 0,
                        "totalExp" to 0
                    )

                    // 同じモンスターIDとして所有モンスターデータにセット
                    userResultRef.collection("ownedMonsters")
                        .document(monsterId)
                        .set(ownedMonsterInfo)
                        .addOnSuccessListener {

//                            // 保存カウントを増やす
//                            savedCount++
////                            Log.i("tag", "カウントは${savedCount}です")
//                            // もし5体の保存が終わったら画面遷移 or まだ保存が残っていたらループに戻る
//                            if (savedCount == 6) {
////                                Log.i("tag", "診断された結果が保存されます。")
//
//                                val intentHome = Intent(this@ResultActivity, MainActivity2::class.java)
//                                startActivity(intentHome)
//                                finishAffinity()
//                            }
//
                        }
                        .addOnFailureListener { e ->
//                            Log.i("tag", "保存失敗: ${e.message}")
                        }
                }
        }

    }

    // Firebaseを削除する処理 → いらない？
    private fun resetFirestore() {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val useId = auth.currentUser?.uid
        if (useId != null) {
            db.collection("results")
                .document(useId)
                .delete()
                .addOnSuccessListener {
//                    Log.i("tag", "診断・育成データを削除しました。")
                    db.collection("results").document(useId).update("diagnosed", false)

                    Toast.makeText(this, "診断した結果をリセットしました。最初から診断を始めます。", Toast.LENGTH_LONG).show()


                    // 診断から始まる
                    val intent = Intent(this@ResultActivity, DiagnosisActivity::class.java)
                    intent.putExtra("mode", ResultMode.FIRST_DIAGNOSIS)
                    intent.putExtra("type", DiagnosisActivity.Diagnosis.FULL)
                    startActivity(intent)
                    finishAffinity()
                }
        }
    }

    fun onBackButtonClick(view: View) {
        finish()
    }
}

