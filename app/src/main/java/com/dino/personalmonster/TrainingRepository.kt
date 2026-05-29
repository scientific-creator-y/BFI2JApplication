package com.dino.personalmonster

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class TrainingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()




    // 単体のトレーニング結果のデータクラス
    data class TrainingResult(
        val parameterKey: String,
        val parameterLabel: String?,
        val incrementValue: Int,

        val monsterName: String,
        val monsterImageRes: String,
        val gainedExp: Int,

//        val isLevelUp: Boolean,
        var levelUpCount: Int,
        val newLevel: Int?,

        val popupColorRes: Int
    )

    // 集約サれたトレーニング結果のデータクラス
    data class AggregatedResult(
        val parameterKey: String,
        val aggregatedValue: Int,

        val monsterName: String,
        val monsterImageRes: String,
        val aggregatedExp: Int,

//        val isLevelUp: Boolean,
        var levelUpCount: Int,
        val newLevel: Int?,

        val popupColorRes: Int
    )


    // 習慣達成の処理
    @RequiresApi(Build.VERSION_CODES.O)
    fun completeTraining(
        menuId: String,
        parameterKey: String,
        incrementValue: Int,
        onSuccess: (TrainingResult) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val user = auth.currentUser ?: return
        val today = getTodayString()



        // 選ばれたメニュー項目を取得
        val userRef = db.collection("results").document(user.uid)
        val menuRef = userRef
            .collection("trainingMenus")
            .document(menuId)


        // トランザクション開始（同期のみ）
        db.runTransaction { transaction ->

            // Firestoreから時間情報を取得
            val snapshot = transaction.get(menuRef)
            val lastDate = snapshot.getString("lastCompletedDate") ?: ""
            var currentStreak = snapshot.getLong("streakCount") ?: 0
            val yesterday = java.time.LocalDate.now().minusDays(1).toString()

            currentStreak = when (lastDate) {
                // 最後の日付が…
                yesterday -> currentStreak + 1 // 昨日達成している状態で、今日も達成したから＋1
                today -> currentStreak // 今日達成ならそのまま（一応？）
                else -> 1 // それ以外なら1にリセット（昨日達成してないなら連続していない）
            }

            // セット中モンスターの情報を習得
            val userSnapshot = transaction.get(userRef)
            val monsterId = userSnapshot.getString("equippedMonsters.${parameterKey}")?: throw Exception("モンスターがセットされていない")
            val monsterRef =  userRef
                .collection("ownedMonsters")
                .document(monsterId)

            val monsterSnapshot = transaction.get(monsterRef)


            // ①メニューの達成状況更新
            transaction.update(
                menuRef, mapOf(
//                    "isCompletedToday" to true,
                    "lastCompletedDate" to today,
                    "streakCount" to currentStreak
                )
            )

            // ②パラメーター加算処理
            transaction.update(
                userRef,
                "personality.currentData.${parameterKey}",
                FieldValue.increment(incrementValue.toLong())
            )

            // ③モンスターの経験値処理
            // モンスターの経験値ゲット・レベルアップ処理
            var level = (monsterSnapshot.getLong("level")?: 1).toInt()
            var exp = (monsterSnapshot.getLong("exp")?: 0).toInt()
            var totalExp = (monsterSnapshot.getLong("totalExp")?: 0).toInt()

//            var isLevelUp = false


            exp += 100
            totalExp += 100
            var levelUpCount = 0


            while (exp >= requiredExp(level)) {
                exp -= requiredExp(level)
                level++
//                isLevelUp = true
                levelUpCount++
            }

            // 更新処理
            transaction.update(
                monsterRef, mapOf(
                    "level" to level,
                    "exp" to exp,
                    "totalExp" to totalExp
                )
            )

            // 戻り値
            Triple(monsterId, level, levelUpCount)
        }
        .addOnSuccessListener { (monsterId, newLevel, levelUpCount) ->


            db.collection("monsters")
                .document(monsterId)
                .get()
                .addOnSuccessListener { masterDoc ->
                    val monsterName = masterDoc.getString("name")?: ""
                    val monsterImageRes = masterDoc.getString("imageRes")?: ""

                    val result = TrainingResult(
                        parameterKey = parameterKey,
                        parameterLabel = ParameterType.fromKey(parameterKey)?.label,
                        incrementValue = incrementValue,

                        monsterName = monsterName,
                        monsterImageRes = monsterImageRes,
                        gainedExp = 100,

//                        isLevelUp = isLevelUp,
                        levelUpCount = levelUpCount,
                        newLevel = newLevel,

                        popupColorRes =  getPopupColor(parameterKey)
                    )

                    onSuccess(result)
                }
                .addOnFailureListener { onFailure(it) }
        }
        .addOnFailureListener { onFailure(it) }
    }






    // ポップアップを出す処理
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun showTrainingPopup(
        activity: Activity,
        popupColorRes: Int,
        line1: String,
        monsterIconRes: String,
        line2: String,
//        isLevelUp: Boolean,
        levelUpCount: Int,
        newLevel: Int?
    ) {
        // ルートViewを習得
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        // ポップアップのレイアウトをビューに変換
        val popupView = LayoutInflater.from(activity).inflate(R.layout.view_training_result_popup, rootView, false)
        popupView.setBackgroundResource(popupColorRes)

        // 表示する文字列
        val tvLine1 = popupView.findViewById<TextView>(R.id.tvLine1)
        val tvLine2 = popupView.findViewById<TextView>(R.id.tvLine2)

        tvLine1.text = line1
        tvLine2.text = line2

        // 表示するアイコン
        val ivMonsterIcon= popupView.findViewById<ImageView>(R.id.ivMonsterIcon)
        val imageResId = activity.resources.getIdentifier(
            monsterIconRes,
            "drawable",
            activity.packageName
        )

        if (imageResId != 0) {
            ivMonsterIcon.setImageResource(imageResId)
        }

        // 初期設定
        popupView.translationY = -200f
        popupView.alpha = 0f

        rootView.addView(popupView)

        // アニメーション
        // 上からフェードイン
        popupView.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(300)
            .withEndAction {

                // 1.5秒表示
                popupView.postDelayed({
                    // 上にフェードアウト
                    popupView.animate()
                        .translationY(-100f)
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            rootView.removeView(popupView)
                        }
                        .start()
                }, 1500)
            }
            .start()

        val vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(50)
        }

        // レベルアップの表示
        val tvLevelUp = popupView.findViewById<TextView>(R.id.tvLevelUp)
        tvLevelUp.visibility = View.GONE
        if (levelUpCount > 0) {
            tvLevelUp.visibility = View.VISIBLE
            tvLevelUp.text = "レベルアップ！"
        } else {
            tvLevelUp.visibility = View.GONE
        }

    }


    // ルーティン達成の結果を集約する処理
    fun aggregatedResult(results: List<TrainingResult>): List<AggregatedResult> {
        return results
            .groupBy { it.parameterKey }
            .map { (key, list) ->
                val first = list.first()

                AggregatedResult(
                    parameterKey = key,
                    aggregatedValue = list.sumOf { it.incrementValue },
                    aggregatedExp = list.sumOf { it.gainedExp },
                    monsterName = first.monsterName,
                    monsterImageRes = first.monsterImageRes,

//                    isLevelUp = first.isLevelUp,
                    levelUpCount = first.levelUpCount,
                    newLevel = first.newLevel,
                    popupColorRes = first.popupColorRes,

                )
            }
    }

    // 順番にポップアップする処理
    fun showPopupQueue(activity: Activity, list: List<AggregatedResult>) {
        showNextPopup(activity, list, 0)
    }

    fun showNextPopup(activity: Activity, list: List<AggregatedResult>, index: Int) {
        if (index >= list.size) return

        val item = list[index]
        showTrainingPopup(
            activity = activity,
            popupColorRes = item.popupColorRes,
            line1 = "${ParameterType.fromKey(item.parameterKey)?.label}＋${item.aggregatedValue}",
            monsterIconRes = item.monsterImageRes,
            line2 = "${item.monsterName}＋${item.aggregatedExp}",
//            isLevelUp = item.isLevelUp,
            levelUpCount = item.levelUpCount,
            newLevel = item.newLevel

        )

        Handler(Looper.getMainLooper()).postDelayed( {
            showNextPopup(activity, list,index+1)
        }, 2000)
    }



    // パラメーターの変換処理 → マスターデータでslot統一すればいらない？
//    private fun getParameter(parameterKey: String) : String{
//        return when(parameterKey) {
//            "social" -> "extraversion"
//            "harmony" -> "agreeableness"
//            "will" -> "conscientiousness"
//            "mental" -> "neuroticism"
//            "explore" -> "openness"
//            else -> "extraversion"
//        }
//    }


    // 日付を取得する処理
    private fun getTodayString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    // レベルに応じた必要経験値を計算する処理
    private fun requiredExp(level: Int): Int {
        // レベルの10の位に応じて計算 → 例：10台なら
        val tier = level / 10
        return (tier + 1) * 100
    }

    // パラメーターに応じたポップアップの色を取得する処理
    private fun getPopupColor(parameterKey: String): Int {
        return when(parameterKey) {
            "social" -> R.drawable.bg_popup_social
            "harmony" -> R.drawable.bg_popup_harmony
            "will" -> R.drawable.bg_popup_will
            "mental" -> R.drawable.bg_popup_mental
            "explore" -> R.drawable.bg_popup_explore
            "HP" -> R.drawable.bg_popup_hp
            else -> R.drawable.bg_popup_social
        }
    }
}