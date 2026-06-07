package com.dino.personalmonster

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
//import androidx.compose.material3.AlertDialog
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.dino.personalmonster.data.InfoContent
import com.dino.personalmonster.data.TrainingTag
import com.dino.personalmonster.ui.InsetsUtil
import com.dino.personalmonster.ui.ShowInfoUtil
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import org.w3c.dom.Text
import java.time.chrono.JapaneseEra
import java.time.chrono.JapaneseEra.values

class TrainingDetailActiivty : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var menuId: String
    private var habit : Boolean = false // habitフラグを用意してはデータベースでの更新の処理のときに切り替え
    private lateinit var btAddHabit : MaterialButton
    private lateinit var btnComplete: Button
    private lateinit var title : String
    private lateinit var layoutTags: FlexboxLayout


    // 後半レイヤー（習慣化詳細設定）関連の変数
    private lateinit var layoutHabitSettings : LinearLayout
    private lateinit var etTrigger: EditText
    private lateinit var etPractice: EditText
    private lateinit var tvStreak: TextView

    // 時間情報関連の変数
    private var isCompletedToday: Boolean = false
    private var lastCompletedDate: String = ""
    private var streakCount: Long = 0

    // レポジトリクラスを取得
    private val repository = TrainingRepository()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContentView(R.layout.activity_training_detail_actiivty)

        // Viewを取得して余白を入れる
        val rootLayout = findViewById<ScrollView>(R.id.rootLayout)
        InsetsUtil.applySystemBarsInsets(rootLayout)

        // 画面のタイトル設定
        supportActionBar?.title = "トレーニングの概要"

        // Firebase関連の初期化
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 後半レイヤーのUI取得
        layoutHabitSettings = findViewById<LinearLayout>(R.id.layoutHabitSetting)
        etTrigger = findViewById<EditText>(R.id.etTrigger)
        etPractice = findViewById<EditText>(R.id.etPractice)
        tvStreak = findViewById<TextView>(R.id.tvStreak)
        val btnManageRoutine = findViewById<Button>(R.id.btnManageRoutine)

        // 渡ってきたデータを取り出し
        title = intent.getStringExtra("title") ?: ""
        val url = intent.getStringExtra("url") ?: ""
        val parameterKey = intent.getStringExtra("parameterKey") ?: ""
        val description = intent.getStringExtra("description") ?: ""
//        val skillName = intent.getStringExtra("skillName") ?: ""
//        val skillDesc = intent.getStringExtra("skillDesc") ?: ""
        val step = intent.getStringExtra("step") ?: ""
        val incrementValue = intent.getIntExtra("incrementValue", 1)

        val guide = intent.getStringExtra("guide") ?:""
        val tags: List<String> = intent.getStringArrayListExtra("tags") ?:arrayListOf()
//        Log.i("タグの確認", "タグ：${tags}、タグの数：${tags.size} 、空かどうか：${tags.isEmpty()}")

//        habit = intent.getBooleanExtra("habit", false)
//        Log.i("tag", "選ばれたメニューのhabitは${habit}")
        menuId = intent.getStringExtra("menuId") ?: return
//        Log.i("tag", "選ばれたメニューのIDは${menuId}")


        // UI（タイトル、説明など）の取得
        val tvMenuTitle = findViewById<TextView>(R.id.tvMenuTitle)
        val tvMenuDes = findViewById<TextView>(R.id.tvMenuDes)
        val tvMenuStep = findViewById<TextView>(R.id.tvMenuStep)
//        val tvSkillName = findViewById<TextView>(R.id.tvSkillName)

        layoutTags = findViewById<FlexboxLayout>(R.id.layoutTags)
        val tvNoTag = findViewById<TextView>(R.id.tvNoTag)


        // 習慣化設定のトリガーなど
        val ivTriggerInfo = findViewById<ImageView>(R.id.ivTriggerInfo)
        val ivPracticeInfo = findViewById<ImageView>(R.id.ivPracticeInfo)

        val rootMenuGuide = findViewById<LinearLayout>(R.id.rootMenuGuide)
        val tvMenuGuide = findViewById<TextView>(R.id.tvMenuGuide)




        val tvSkillParameter = findViewById<TextView>(R.id.tvSkillParameter)
//        val tvSkillDesc = findViewById<TextView>(R.id.tvSkillDesc)
//        val ivSkillIcon = findViewById<ImageView>(R.id.ivSkillIcon)

//        val btBlogLink = findViewById<Button>(R.id.btBlogLink)
        btnComplete = findViewById<Button>(R.id.btnComplete)
        btAddHabit = findViewById<MaterialButton>(R.id.btAddHabit)
        val btBackToMenuList = findViewById<Button>(R.id.btBackToMenuList)
    
        // Firestoreで達成状況を取得するまでは達成ボタンは押せない
        btnComplete.isEnabled = false


        // UIにデータを反映
        tvMenuTitle.text = title
        tvMenuDes.text = description
        tvMenuStep.text = step
//        tvSkillName.text = skillName
//        tvSkillDesc.text = skillDesc

        // 実践手順があれば表示する
        rootMenuGuide.visibility = View.GONE
        if (guide.isNotBlank()) {
            rootMenuGuide.visibility = View.VISIBLE
            tvMenuGuide.text = guide
        }

        // タグがあれば表示する
        tvNoTag.visibility = View.GONE


        if (tags.isEmpty()) {
//            Log.i("タグがあるかの確認", "isEmpty=${tags.isEmpty()}")
            tvNoTag.visibility = View.VISIBLE
        } else {
            layoutTags.removeAllViews()
            tags.forEach { key ->
                addTagView(TrainingTag.fromKey(key)?.displayName)
            }
        }




        // アイコンを表示する
        when (parameterKey) {
            "social" -> {
//                ivSkillIcon.setImageResource(R.drawable.icon_fire)
                tvSkillParameter.text = ParameterType.SOCIAL.label
                tvSkillParameter.background = ContextCompat.getDrawable(this@TrainingDetailActiivty, R.drawable.bg_ic_social)
            }
            "harmony" -> {
//                ivSkillIcon.setImageResource(R.drawable.icon_grass)
                tvSkillParameter.text = ParameterType.HARMONY.label
                tvSkillParameter.background = ContextCompat.getDrawable(this@TrainingDetailActiivty, R.drawable.bg_ic_harmony)

            }
            "will" -> {
//                ivSkillIcon.setImageResource(R.drawable.icon_water)
                tvSkillParameter.text = ParameterType.WILL.label
                tvSkillParameter.background = ContextCompat.getDrawable(this@TrainingDetailActiivty, R.drawable.bg_ic_will)

            }
            "mental" -> {
//                ivSkillIcon.setImageResource(R.drawable.icon_rock)
                tvSkillParameter.text = ParameterType.MENTAL.label
                tvSkillParameter.background = ContextCompat.getDrawable(this@TrainingDetailActiivty, R.drawable.bg_ic_mental)

            }
            "explore" -> {
//                ivSkillIcon.setImageResource(R.drawable.icon_thunder)
                tvSkillParameter.text = ParameterType.EXPLORE.label
                tvSkillParameter.background = ContextCompat.getDrawable(this@TrainingDetailActiivty, R.drawable.bg_ic_explore)

            }
            "HP" -> {
//                ivSkillIcon.setImageResource(R.drawable.home)
                tvSkillParameter.text = ParameterType.HP.label
                tvSkillParameter.background = ContextCompat.getDrawable(this@TrainingDetailActiivty, R.drawable.bg_ic_hp)


            }
            else -> {
//                ivSkillIcon.setImageResource(R.drawable.home)
                tvSkillParameter.text = ParameterType.SOCIAL.label
                tvSkillParameter.background = ContextCompat.getDrawable(this@TrainingDetailActiivty, R.drawable.bg_ic_social)


            }
        }



        // 現在の状態を参照してボタンの表示を切り替え
        loadHabitStatus()

        // ブログボタンが押されたときの処理
//        btBlogLink.setOnClickListener {
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//            startActivity(intent)
//        }

        // 戻るボタンが押されたときの処理
        btBackToMenuList.setOnClickListener { finish() }



        // 習慣に追加ボタンが押されたときの処理
        btAddHabit.setOnClickListener {
            // 習慣のhabitフラグを更新する（Kotlin側で）
            val newValue = !habit // 現在のブーリンを反転した値を保持
//            Log.i("tag", "ボタン押したときに、${habit}から${newValue}に変更")
            val user = auth.currentUser

            // 習慣リストに追加・除去する処理(コピーしたユーザーのトレーニングメニューでフラグだけつけて識別)

            if (user != null) {

                val stateRef = db.collection("results")
                    .document(user.uid)
                    .collection("trainingMenus")
                    .document(menuId)

                val routineRef = db.collection("results")
                    .document(user.uid)
                    .collection("routines")

                val batch = db.batch()


                // フラグを変更
                batch.update(stateRef, "habit", newValue) // 現在のture,falseを逆転
//                batch.set(
//                    stateRef,
//                    mapOf("habit" to newValue),
//                    SetOptions.merge()
//                )

                // 習慣から外すとき（今habitがtrueのとき）には親子関係解消
                if (habit) {
                    // 子→親のidを消す
                    batch.update(stateRef, "parentRoutineId", null)
//                    batch.set(
//                        stateRef,
//                        mapOf("parentRoutineId" to null),
//                        SetOptions.merge()
//                    )

                    // 親→子のidを消す
                    routineRef.get().addOnSuccessListener { snapshots ->
                        snapshots.documents.forEach { documentSnapshot ->
                            batch.update(
                                documentSnapshot.reference,
                                "menuIds",
                                FieldValue.arrayRemove(menuId)
                            )
                        }

                        batch.commit()
                            .addOnSuccessListener {
                                habit = newValue // Kotlin側でも更新
                                afterHabitToggle(habit)
                            }
                    }

                } else {
                    // 習慣に追加するときならそのままフラグだけ
                    batch.commit().addOnSuccessListener {
                        habit = newValue // Kotlin側でも更新
                        afterHabitToggle(habit)
                    }
                }
            }

        }


        // 達成ボタンが押されたときの処理
        btnComplete.setOnClickListener {
            // すでに達成済みなら押したときにトーストを出して処理を止める
            if (isCompletedToday) {
                Toast.makeText(this@TrainingDetailActiivty, "今日はすでに達成済みです", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            repository.completeTraining(
                menuId = menuId,
                parameterKey = parameterKey,
                incrementValue = incrementValue,
                onSuccess = { result ->

                    // ローカルで計算するのではなく、loadHabitStatus()で一括処理（中でFirestoreから取得してローカル変数代入、UIアップデートもできる）
                    loadHabitStatus()

//                    Log.i("渡ってきた結果", "${result.monsterName}")
//                    Log.i("渡ってきた結果", "${result.popupColorRes}")
//                    val parameterLabel = ParameterType.fromKey(parameterKey)?.label

                    // 達成のポップアップを出す
                    repository.showTrainingPopup(
                        activity = this@TrainingDetailActiivty,
                        popupColorRes = result.popupColorRes,
                        line1 = "${result.parameterLabel}＋${result.incrementValue}",
                        monsterIconRes = result.monsterImageRes,
                        line2 = "${result.monsterName}＋${result.gainedExp}",
                        levelUpCount = result.levelUpCount,
                        newLevel = result.newLevel
                    )

                },
                onFailure = {
                    Toast.makeText(this@TrainingDetailActiivty, "達成処理失敗", Toast.LENGTH_LONG).show()

                }
            )

        }


        // エディットテキストのフォーカスアウト時に保存の処理を実行
        etTrigger.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && habit) {
//                Log.i("フォーカス", "hasFocus = $hasFocus , habit = $habit")
                val triggerText = etTrigger.text.toString().trim()
                saveTriggerToFirebase(triggerText)
            }
        }
        // エディットテキストのフォーカスアウト時に保存の処理を実行
        etPractice.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && habit) {
//                Log.i("フォーカス", "hasFocus = $hasFocus , habit = $habit")
                val practiceText = etPractice.text.toString().trim()
                savePracticeToFirebase(practiceText)
            }
        }



        // ルーティン管理ボタンが押された時にダイアログを表示する
        btnManageRoutine.setOnClickListener {
            showRoutineManageDialog(menuId)
        }

        // 現在の所属ルーティンを表示
        showCurrentRoutine()


        // 情報アイコン
        ivTriggerInfo.setOnClickListener {
            val content  = InfoContent(
                title = "習慣に取り組むキッカケ",
                message = "「習慣化の例」を参考に、習慣に取り組むキッカケを具体的に決めておきましょう。具体的なタイミングを決めておくと習慣化に成功しやすいです。",
                url = "https://app.notion.com/p/373d30cb9fca804ea91eddd69ae0de8b?source=copy_link"
            )
            ShowInfoUtil.showInfoDialog(this, content)
        }
        ivPracticeInfo.setOnClickListener {
            val content  = InfoContent(
                title = "習慣として実践する内容",
                message = "「習慣化の例」を参考に、自分が取り組む内容を決めておきましょう。数値や基準を盛り込んで達成したかどうかが一発でわかるようにしておくといいです。\n\nここで決めた内容は習慣リストに表示されます。",
                url = "https://app.notion.com/p/373d30cb9fca80938bd1c074a3271643?source=copy_link"
            )
            ShowInfoUtil.showInfoDialog(this, content)
        }

    }

    // 戻るボタンでもトリガーテキストを保存
    override fun onPause() {
        super.onPause()

        if (habit) {
            val triggerText = etTrigger.text.toString().trim()
            saveTriggerToFirebase(triggerText)

            val practiceText = etPractice.text.toString().trim()
            savePracticeToFirebase(practiceText)
        }
    }



    // タグを表示する
    private fun addTagView(tag: String?) {
//        Log.i("タグ生成", "追加するタグ：$tag")
        val tv = TextView(this)

        tv.text = tag
        tv.textSize = 16f
        tv.setTextColor(
            ContextCompat.getColor(this, R.color.text_primary)
        )
//        tv.setBackgroundResource(R.drawable.bg_ic_tag)
//        tv.setPadding(4, 4, 4, 4)

        val params = FlexboxLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.marginEnd = 16
        tv.layoutParams = params

        layoutTags.addView(tv)

    }



    //
    private fun loadHabitStatus() {
        val user = auth.currentUser?: return
        db.collection("results")
            .document(user.uid)
            .collection("trainingMenus")
            .document(menuId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val menu = documentSnapshot.toObject(TrainingMenuUi::class.java)

                // Firestoreから取得してローカル変数を更新
                habit = menu?.habit ?: false // Firestoreでのhabitを取得して（右辺）、habitフラグを更新（左辺）
//                Log.i("tag", "選ばれたメニューのhabitは${habit}")
//                Log.i("tag", "選ばれたメニューのtitleは${menu?.title}")

                lastCompletedDate = menu?.lastCompletedDate ?:"" // 時間情報を取得する
                streakCount = menu?.streakCount ?: 0

                val today = getTodayString() // 達成フラグはその場で計算
                isCompletedToday = (lastCompletedDate == today)
//              isCompletedToday = menu?.isCompletedToday ?: false 達成フラグはFirestoreから削除

//                Log.i("時間情報の取得とローカル変数への代入", "達成済みフラグは${isCompletedToday}")
//                Log.i("時間情報の取得とローカル変数への代入", "最後の達成日は${lastCompletedDate}")
//                Log.i("時間情報の取得とローカル変数への代入", "連続達成日は${streakCount}")



                // 画面を更新
                updateButtonUI()
//                updateCompleteButtonUI()

//                // UIが決まったら達成ボタンを押せるようにする
//                btnComplete.isEnabled = true

                // 習慣トリガーのテキストを取得・反映
                val triggerText = menu?.triggerText ?:""
                etTrigger.setText(triggerText)

                // 実践内容のテキストを取得・反映
                val practiceText = menu?.practiceText ?:""
                etPractice.setText(practiceText)

                // 習慣連続達成のテキストを反映
                tvStreak.text = "連続達成日数：${streakCount}日"

            }

    }


    // 習慣ボタンを押したときの制御
    private fun afterHabitToggle(newValue: Boolean) {
        habit = newValue // Kotlin側でも更新

        // 習慣フラグに応じて後半レイヤーを開くor閉じる（アニメーション処理つき）
        if (habit) {
            animateHabitSectionOpen()
        } else {
            animateHabitSectionClose()
        }

        // ボタンのUIの更新とトースト表示
        updateButtonUI()
        Toast.makeText(
            this,
            if (habit) "{$title}を習慣リストに追加しました。" else "{$title}を習慣リストから削除しました。",
            Toast.LENGTH_LONG
        ).show()

    }

    // 習慣ボタンのUI更新
    private fun updateButtonUI() {
        if (habit) {
            // テキストを「削除」で表示
            btAddHabit.text = getString(R.string.btn_addHabit_delete)

            // ネガティブスタイルを適応
            setNegativeStyle(btAddHabit)

            // アニメーションで詳細設定を開く
            animateHabitSectionOpen()


        } else {
            // テキストを「追加」で表示
            btAddHabit.text = getString(R.string.btn_addHabit_add)

            // ポジティブスタイルを適応
            setPositiveStyle(btAddHabit)

            // アニメーションで詳細設定を閉じる
            animateHabitSectionClose()
        }

        // 達成ボタンのUI更新
        if (isCompletedToday) {
            // 達成済みなら押せないようにする
            btnComplete.text = "今日は達成済み"
            btnComplete.isEnabled = false
        } else {
            // 未達成なら押せるようにする
            btnComplete.text = "達成"
            btnComplete.isEnabled = true

        }
    }

    // 習慣ボタンの切り替え（ネガティブへ）
    private fun setNegativeStyle(button: MaterialButton) {
        val color = ContextCompat.getColor(this, R.color.button_negative)
        val backgroundColor = ContextCompat.getColor(this, R.color.white)


        button.strokeColor = ColorStateList.valueOf(color)
        btAddHabit.strokeWidth = 4
        button.setTextColor(color)
        button.backgroundTintList = ColorStateList.valueOf(backgroundColor)


    }


    // 習慣ボタンの切り替え（ポジティブへ）
    private fun setPositiveStyle(button: MaterialButton) {
        val color = ContextCompat.getColor(this, R.color.button_positive)
        val textColor = ContextCompat.getColor(this, R.color.white)

//        button.strokeColor = ColorStateList.valueOf(color)
        btAddHabit.strokeWidth = 0
        button.backgroundTintList = ColorStateList.valueOf(color)
        button.setTextColor(textColor)

    }

    // 所属ルーティンの表示・更新処理
    private fun showCurrentRoutine() {
        val tvParentRoutine = findViewById<TextView>(R.id.tvParentRoutine)
        val user = auth.currentUser
        if (user != null) {
            db.collection("results")
                .document(user.uid)
                .collection("trainingMenus")
                .document(menuId)
                .get()
                .addOnSuccessListener { documentSnapshot ->

                    val parentRoutineId = documentSnapshot.getString("parentRoutineId")

                    if (parentRoutineId.isNullOrEmpty()) {
                        tvParentRoutine.text = "なし"
                        return@addOnSuccessListener
                    }
                    db.collection("results")
                        .document(user.uid)
                        .collection("routines")
                        .document(parentRoutineId)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            tvParentRoutine.text = documentSnapshot.getString("name")
                        }
                }

        }

    }



    // 今日の日付を取得する処理 → レポジトリクラスでも用意
    private fun getTodayString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    // 必要なら達成or非達成のリセットをする処理 → ロードするときに達成フラグ確認できる
//    private fun checkResetIfNeeded() {
//        val today = getTodayString()
//        // 今日達成されていなければ
//        if (lastCompletedDate != today) {
//            // 達成済みをオフにして復活させる
//            isCompletedToday = false
//
//             Firestoreでも達成済みをオフにする → FirestoreではisCompletedTodayフィールドは削除
//            val user = auth.currentUser ?:return
//            db.collection("results")
//                .document(user.uid)
//                .collection("trainingMenus")
//                .document(menuId)
//                .update("isCompletedToday", false)
//        }
//    }




//    習慣フラグに応じて後半レイヤーを開くor閉じる（アニメーション処理つき）
    private fun animateHabitSectionOpen() {
        layoutHabitSettings.alpha = 0f
        layoutHabitSettings.visibility = View.VISIBLE
        layoutHabitSettings.animate()
            .alpha(1f)
            .setDuration(200)
            .start()
//        Log.i("開くアニメーション", "開くアニメーションが行われました")
    }

    private fun animateHabitSectionClose() {
        layoutHabitSettings.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                layoutHabitSettings.visibility = View.GONE
            }
            .start()
//        Log.i("閉じるアニメーション", "閉じるアニメーションが行われました")

    }

    // 習慣トリガーに書いたテキストをFirestoreに保存する処理
    private fun saveTriggerToFirebase(trigger: String) {
        val user = auth.currentUser ?: return

        db.collection("results")
            .document(user.uid)
            .collection("trainingMenus")
            .document(menuId)
//            .update("triggerText", trigger)
            .set(
                mapOf("triggerText" to trigger),
                SetOptions.merge()
            )
            .addOnSuccessListener {
//                Log.i("習慣トリガーの保存", "習慣トリガーの保存成功：${trigger}")
            }
            .addOnFailureListener { exception ->
//                Log.i("習慣トリガーの保存", "習慣トリガーの保存失敗：${exception.message}")

            }
    }

    // 自分の実践内容に書いたテキストをFirestoreに保存する処理
    private fun savePracticeToFirebase(practice: String) {
        val user = auth.currentUser ?: return

        db.collection("results")
            .document(user.uid)
            .collection("trainingMenus")
            .document(menuId)
            .update("practiceText", practice)

            .addOnSuccessListener {
//                Log.i("習慣トリガーの保存", "習慣トリガーの保存成功：${trigger}")
            }
            .addOnFailureListener { exception ->
//                Log.i("習慣トリガーの保存", "習慣トリガーの保存失敗：${exception.message}")

            }
    }



    // ルーティン管理のダイアログの処理
    private fun showRoutineManageDialog(menuId: String) {
        val user = auth.currentUser ?:return
        val routineRef = db.collection("results")
            .document(user.uid)
            .collection("routines")

        routineRef.get().addOnSuccessListener { snapshots ->
            // ルーティンすべてを取得
            val routineDoc = snapshots.documents

            // ラジオボタン形式の選択肢
            val options = mutableListOf<String>()

            // 未ルーティンの選択肢
            options.add("ルーティンに入れない")

            // 既存の選択肢 →ルーティン全てからそれぞれ名前を取得
            routineDoc.forEach {
                options.add(it.getString("name")?: "無名のルーティン")
            }

            // 新規の選択肢
            options.add("＋ 新規ルーティンを作成")

            // 配列に変換
            val optionArray = options.toTypedArray()


            // ダイアログ作成
            AlertDialog.Builder(this)
                .setTitle("ルーティンを管理")
                .setItems(optionArray) { _, which ->
                    when(which) {
                        0 -> {
                            // ルーティンから外す
                            updateRoutine(menuId, null)
                        }
                        optionArray.size -1 -> {
                            // 新規ルーティン追加
                            showCreateRoutineDialog(menuId)
                        }
                        else -> {
                            val selectedRoutine = routineDoc[which-1]
                            updateRoutine(menuId, selectedRoutine.id)
                        }
                    }
                }
                .show()


        }

    }

    // 既存ルーティンへの登録・ルーティンの移動
    private fun updateRoutine(menuId: String, selectedRoutineId: String?) {
        val user = auth.currentUser?: return

        val routineRef = db.collection("results")
            .document(user.uid)
            .collection("routines")

        val menuRef = db.collection("results")
            .document(user.uid)
            .collection("trainingMenus")

        routineRef.get().addOnSuccessListener { snapshots ->
            val batch = db.batch()

            // すべての親に関して、そのアイテムが含まれているなら消す
            snapshots.documents.forEach { doc ->
                batch.update(doc.reference, "menuIds", FieldValue.arrayRemove(menuId))
            }

            // 新しい親 →子id追加
            if (selectedRoutineId != null) {
                val targetRef = routineRef.document(selectedRoutineId)
                batch.update(targetRef, "menuIds", FieldValue.arrayUnion(menuId))
            }

            // 子 → 親id追加
//            batch.update(menuRef.document(menuId), "parentRoutineId", selectedRoutineId)
            batch.set(
                menuRef.document(menuId),
                mapOf("parentRoutineId" to selectedRoutineId),
                SetOptions.merge()
            )


            batch.commit()
                .addOnSuccessListener {
                    Toast.makeText(this, "ルーティンを更新しました", Toast.LENGTH_LONG).show()
                    showCurrentRoutine()
                }
        }
    }

    // 新規ルーティンのダイアログ
    private fun showCreateRoutineDialog(menuId: String) {
        val editText = EditText(this)



        val dialog = AlertDialog.Builder(this)
            .setTitle("新しいルーティン名")
            .setView(editText)
            .setPositiveButton("作成", null)
            .setNegativeButton("キャンセル", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val routineName = editText.text.toString()
            if (routineName.isBlank()) {
                editText.error = "ルーティン名を入力してください"
                return@setOnClickListener
            }

            // 成功時だけ作成処理をして、ダイアログを閉じる
            createRoutine(menuId, routineName)

            dialog.dismiss()

        }
//        AlertDialog.Builder(this)
//            .setTitle("新しいルーティン名")
//            .setView(editText)
//            .setPositiveButton("作成") {_, _ ->
//                val name = editText.text.toString()
//                if (name.isNotBlank()) {
//                    createRoutine(menuId, name)
//                }
//            }
//            .setNegativeButton("キャンセル", null)
//            .show()
    }

    private fun createRoutine(menuId: String, name: String) {
        val user = auth.currentUser?: return

        val routineRef = db.collection("results")
            .document(user.uid)
            .collection("routines")

        // 新しいIDを作成して、新規ルーティンのデータをセット
        val newRoutineRef = routineRef.document()

        // 新規ルーティンに子要素を追加
        val data = mapOf(
            "name" to name,
            "menuIds" to listOf(menuId),
            "orderIndex" to 0
        )
        newRoutineRef.set(data)
            .addOnSuccessListener {
                Toast.makeText(this, "新規ルーティンを追加", Toast.LENGTH_LONG).show()
                showCurrentRoutine()
            }

        // 子要素からも新しい親を追加
        db.collection("results")
            .document(user.uid)
            .collection("trainingMenus")
            .document(menuId)
//            .update("parentRoutineId", newRoutineRef.id)
            .set(
                mapOf("parentRoutineId" to newRoutineRef.id),
                SetOptions.merge()
            )
    }





}