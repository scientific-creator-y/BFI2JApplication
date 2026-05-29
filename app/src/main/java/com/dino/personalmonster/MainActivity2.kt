package com.dino.personalmonster

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.dino.personalmonster.ui.InsetsUtil
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity2 : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

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
    private lateinit var currentScreen: String
//    var currentScreen: String = "home"



    // 更新の有無を取得
    private val appUpdateManager by lazy {
        AppUpdateManagerFactory.create(this)
    }
    private var isUpdateChecked = false

    data class InfoContent(
        val title: String = "",
        val message: String = "",
        val imageResource: Int? = null

    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContentView(R.layout.activity_main2)

        // ルートView取得して余白入れる
        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)
        InsetsUtil.applyBottomInsets(rootLayout)

//        Log.i("画面遷移", "MainActivity2が開かれました。")

        // 自前のツールバーを取得
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

//        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//
//            view.setPadding(
//                view.paddingLeft,
//                systemBars.top,
//                view.paddingRight,
//                view.paddingBottom
//            )
//
//            insets
//        }


        // 画面情報取得の初期化
        currentScreen = "home"
        setCurrentScreen(currentScreen)



        // Firebaseから初期値を習得（プロフィールオプション用）
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 認証済化どうかをチェック
        val user = auth.currentUser
        Log.i("認証状態", "${user?.isEmailVerified}")

        val userId = auth.currentUser?.uid
        if(userId != null) {
            db.collection("results")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        eInitial =
                            (document.getDouble("personality.initialData.extraversion") ?: 0.0).toFloat()
                        aInitial =
                            (document.getDouble("personality.initialData.agreeableness") ?: 0.0).toFloat()
                        cInitial =
                            (document.getDouble("personality.initialData.conscientiousness") ?: 0.0).toFloat()
                        nInitial =
                            (document.getDouble("personality.initialData.neuroticism") ?: 0.0).toFloat()
                        oInitial = (document.getDouble("personality.initialData.openness") ?: 0.0).toFloat()
//                        Log.i("初期値", "初期値は${eInitial}, ${aInitial}, ${cInitial}, ${nInitial}, ${oInitial}")


                        sociability = (document.getDouble("personality.facetData.extraversion.sociability") ?: 0.0).toFloat()
                        assertiveness = (document.getDouble("personality.facetData.extraversion.assertiveness") ?: 0.0).toFloat()
                        energyLevel = (document.getDouble("personality.facetData.extraversion.energyLevel") ?: 0.0).toFloat()

                        compassion = (document.getDouble("personality.facetData.agreeableness.compassion") ?: 0.0).toFloat()
                        respectfulness = (document.getDouble("personality.facetData.agreeableness.respectfulness") ?: 0.0).toFloat()
                        trust = (document.getDouble("personality.facetData.agreeableness.trust") ?: 0.0).toFloat()

                        organization = (document.getDouble("personality.facetData.conscientiousness.organization") ?: 0.0).toFloat()
                        productiveness = (document.getDouble("personality.facetData.conscientiousness.productiveness") ?: 0.0).toFloat()
                        responsibility = (document.getDouble("personality.facetData.conscientiousness.responsibility") ?: 0.0).toFloat()

                        calmness = (document.getDouble("personality.facetData.neuroticism.calmness") ?: 0.0).toFloat()
                        resilience = (document.getDouble("personality.facetData.neuroticism.resilience") ?: 0.0).toFloat()
                        stability = (document.getDouble("personality.facetData.neuroticism.stability") ?: 0.0).toFloat()

                        intellectualCuriosity = (document.getDouble("personality.facetData.openness.intellectualCuriosity") ?: 0.0).toFloat()
                        aestheticSensitivity = (document.getDouble("personality.facetData.openness.aestheticSensitivity") ?: 0.0).toFloat()
                        creativeImagination = (document.getDouble("personality.facetData.openness.creativeImagination") ?: 0.0).toFloat()


                    }
                }
        }


        // ナビゲーションの設定
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

            // デフォルト表示
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()

        bottomNav.setOnItemSelectedListener { item ->
            // 現在表示されているフラグメントを習得
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

            when (item.itemId) {
                R.id.nav_home -> {
                    if (currentFragment is HomeFragment) { return@setOnItemSelectedListener true }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                    true
                }
                R.id.nav_training -> {
                    if (currentFragment is TrainingFragment) { return@setOnItemSelectedListener true }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, TrainingFragment())
                        .commit()
                    true
                }
                R.id.nav_habits -> {
                    if (currentFragment is HabitFragment) { return@setOnItemSelectedListener true }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HabitFragment())
                        .commit()
                    true
                }
                R.id.nav_monster -> {
                    if (currentFragment is MonsterFragment) { return@setOnItemSelectedListener true }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, MonsterFragment())
                        .commit()
                    true
                }

                else -> false
            }
        }

        // 更新があれば通知
        if (!isUpdateChecked) {
            checkUpdate()

            isUpdateChecked = true
        }


    }

    // 更新があれば通知する処理
    private fun checkUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                AlertDialog.Builder(this)
                    .setTitle("アプリの更新があります")
                    .setMessage("アプリの新しいバージョンをお使いいただけます。更新しますか？")
                    .setPositiveButton("更新") { _, _ ->
                        appUpdateManager.startUpdateFlowForResult(info, AppUpdateType.FLEXIBLE,this, 1001)
                    }
                    .setNegativeButton("あとで", null)
                    .show()
            }
        }
    }


    // 画面の情報を取得する処理（Fragment側で使用）
    fun setCurrentScreen(screen: String) {
        currentScreen = screen
//        Log.i("現在の画面", "現在の画面のタグは${screen}")
    }

    // オプションアイテムを作る処理
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }


    // オプションアイテムを選択したときの処理
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val returnVal = true
        when (item.itemId) {
            R.id.privacy_option -> {
                val intent = Intent(this@MainActivity2, PrivacyActivity::class.java)
                startActivity(intent)
            }
//             データ削除のメニュー
            R.id.remove_user -> {

          }
            // データ削除のメニュー
            R.id.delete_data -> {

          }

            // 診断を楽しむボタン
            R.id.diagnosis_option -> {
                val intent = Intent(this@MainActivity2, SelectionActivity::class.java)
                startActivity(intent)
            }

            // マイプロファイルのメニュー
            R.id.info_option -> {
                val intent = Intent(this@MainActivity2, ResultActivity::class.java)

                // パラメーターを少数で
                intent.putExtra(DiagnosisActivity.Trait.E.name, eInitial)
                intent.putExtra(DiagnosisActivity.Trait.A.name, aInitial)
                intent.putExtra(DiagnosisActivity.Trait.C.name, cInitial)
                intent.putExtra(DiagnosisActivity.Trait.N.name, nInitial)
                intent.putExtra(DiagnosisActivity.Trait.O.name, oInitial)


                intent.putExtra(DiagnosisActivity.Facet.SOCIABILITY.name, sociability)
                intent.putExtra(DiagnosisActivity.Facet.ASSERTIVENESS.name, assertiveness)
                intent.putExtra(DiagnosisActivity.Facet.ENERGY_LEVEL.name, energyLevel)

                intent.putExtra(DiagnosisActivity.Facet.COMPASSION.name, compassion)
                intent.putExtra(DiagnosisActivity.Facet.RESPECTFULNESS.name, respectfulness)
                intent.putExtra(DiagnosisActivity.Facet.TRUST.name, trust)

                intent.putExtra(DiagnosisActivity.Facet.ORGANIZATION.name, organization)
                intent.putExtra(DiagnosisActivity.Facet.PRODUCTIVENESS.name, productiveness)
                intent.putExtra(DiagnosisActivity.Facet.RESPONSIBILITY.name, responsibility)

                intent.putExtra(DiagnosisActivity.Facet.CALMNESS.name, calmness)
                intent.putExtra(DiagnosisActivity.Facet.RESILIENCE.name, resilience)
                intent.putExtra(DiagnosisActivity.Facet.EMOTIONAL_STABILITY.name, stability)

                intent.putExtra(DiagnosisActivity.Facet.INTELLECTUAL_CURIOSITY.name, intellectualCuriosity)
                intent.putExtra(DiagnosisActivity.Facet.AESTHETIC_SENSITIVITY.name, aestheticSensitivity)
                intent.putExtra(DiagnosisActivity.Facet.CREATIVE_IMAGINATION.name, creativeImagination)



                // プロフィールモードで結果表示
                intent.putExtra("mode", ResultActivity.ResultMode.PROFILE.name)
                startActivity(intent)
            }

            // ログアウトのメニュー
            R.id.logout_option -> {
                val dialog = ConfirmDialogFragment.newInstance("ログアウトしますか？", 4) { requestCode ->
                    logout()
                }
                dialog.show(supportFragmentManager, "ConfirmDialog")

            }


            // 設定のメニュー
            R.id.setting_option -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
            }


            // 情報を出すメニュー
            R.id.action_info -> {
                showInfoDialog()
            }

        }
        return returnVal
    }

    // 情報のダイアログを出す処理
    private fun showInfoDialog() {
        val content = getInfoContent(currentScreen)

        if (content.imageResource == null) {
            AlertDialog.Builder(this)
                .setTitle(content.title)
                .setMessage(content.message)
                .setPositiveButton("OK", null)
                .show()
        } else {
            // 画像の場合
            val dialogView = layoutInflater.inflate(R.layout.dialog_info_type, null)

            val tvMessage = dialogView.findViewById<TextView>(R.id.tvInfoAboutType)
            val ivImage = dialogView.findViewById<ImageView>(R.id.ivInfoAboutType)

            tvMessage.text = content.message
            ivImage.setImageResource(content.imageResource)


            AlertDialog.Builder(this)
                .setTitle(content.title)
                .setView(dialogView)
                .setPositiveButton("閉じる", null)
                .show()
        }

    }

    // 情報を習得・管理する処理
    private fun getInfoContent(screen: String): InfoContent {
        return when(screen) {
            "home" -> InfoContent(
                title = "パーソナルモンスターとは？",
                message = "パソモンはあなたの性格を象徴するモンスターです。あなたが性格スキルを鍛えるたびに、パソモンも成長していきます。"
            )
            "training" -> InfoContent(
                title = "トレーニングとは？",
                message = "この画面では、性格スキルを伸ばすことができるトレーニングを探すことができます。気になったトレーニングがあれば、タップして「習慣に追加する」を押しましょう。",
                imageResource = R.drawable.dialog_info_training_transparent
            )

            "habit" -> InfoContent(
                title = "習慣リストとは？",
                message = "この画面では、「習慣」として登録したトレーニングだけを一覧できます。「達成！」を押すとトレーニングの種類に応じてあなたのパソモンに経験値が入ります。",
                imageResource = R.drawable.dialog_info_habit_transparent

            )

            "monster" -> InfoContent(
                title = "パソモン図鑑とは？",
                message = "この画面では、あなたが登録したパソモンの一覧を確認することができます。"
            )



            else -> InfoContent(
                title = "情報",
                message = "情報がありません。"
            )
        }
    }


    // ログアウトする処理
    private fun logout() {
        auth.signOut()

        val intent = Intent(this@MainActivity2, LoginActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }




}