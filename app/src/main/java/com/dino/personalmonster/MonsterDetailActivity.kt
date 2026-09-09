package com.dino.personalmonster

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ScrollView

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.dino.personalmonster.ui.InsetsUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MonsterDetailActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var level = 1

    enum class Rarity(val key: String, val label: String) {
        BRONZE("bronze", "ブロンズ"),
        SILVER("silver", "シルバー"),
        GOLD("gold", "ゴールド");

        companion object {
            fun fromKey(key: String): Rarity? {
                return values().find { it.key == key }
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)


        setContentView(R.layout.activity_monster_detail)

        // Viewを取得して余白を入れる
        val rootLayout = findViewById<ScrollView>(R.id.rootLayout)
        InsetsUtil.applySystemBarsInsets(rootLayout)

        // 画面のタイトル設定 → パソモン図鑑のフラグメントからの遷移
        supportActionBar?.title = "パソモン図鑑"


        val monsterId = intent.getStringExtra("monsterId")?: ""
//        Log.i("monsterId", monsterId)


        // 表示する要素のUIを取得
        val tvMonsterNumber = findViewById<TextView>(R.id.tvMonsterNumber)
        tvMonsterNumber.text = "${monsterId}"

        val tvTypeLabel = findViewById<TextView>(R.id.tvTypeLabel)
        val ivTypeIcon = findViewById<ImageView>(R.id.ivTypeIcon)
        val tvType = findViewById<TextView>(R.id.tvType)
        val tvMonsterName = findViewById<TextView>(R.id.tvMonsterName)
        val ivMonsterImage = findViewById<ImageView>(R.id.ivMonsterImage)
        val tvMonsterDescription = findViewById<TextView>(R.id.tvMonsterDescription)


        // Firestore（マスターのモンスター情報）
        val monsterRef = db.collection("monsters")
            .document(monsterId)

        // Firestoreから遷移してきたモンスターのデータを取得
        monsterRef.get()
            .addOnSuccessListener { snapshot ->
                // マスターデータから習得
                // 名前と説明文、レアリティを習得
                val master = snapshot.toObject(MonsterMaster::class.java)?: return@addOnSuccessListener
                tvMonsterName.text = master.name
                tvMonsterDescription.text = master.description

                val rarityKey = master.rarity
                val rarity = Rarity.fromKey(rarityKey)?.label

                val tvMonsterReality = findViewById<TextView>(R.id.tvMonsterReality)
                tvMonsterReality.text = "${rarity}"


                // 画像を習得
                val imageRes = resources.getIdentifier(master.monsterImageRes, "drawable", packageName)
                if (imageRes != 0) {
                    ivMonsterImage.setImageResource(imageRes)
                }


                // タイプと画像を反映
                val type = master.type
                when(type) {
                    "fire" -> {
                        ivTypeIcon.setImageResource(R.drawable.icon_fire)
                        tvType.text = "炎"
                    }
                    "fairy" -> {
                        ivTypeIcon.setImageResource(R.drawable.icon_fairy)
                        tvType.text = "妖"
                    }
                    "ghost" -> {
                        ivTypeIcon.setImageResource(R.drawable.icon_ghost)
                        tvType.text = "霊"
                    }
                    "grass" -> {
                        ivTypeIcon.setImageResource(R.drawable.icon_grass)
                        tvType.text = "草"
                    }
                    "flower" -> {
                        ivTypeIcon.setImageResource(R.drawable.icon_flower)
                        tvType.text = "花"
                    }
                    "poison" -> {
                        ivTypeIcon.setImageResource(R.drawable.icon_poison)
                        tvType.text = "毒"
                    }
                    "water" -> {
                        ivTypeIcon.setImageResource(R.drawable.icon_water)
                        tvType.text = "水"
                    }
                    "mist" -> {
                        ivTypeIcon.setImageResource(R.drawable.icon_mist)
                        tvType.text = "霧"
                    }
                    "wind" -> {
                        ivTypeIcon.setImageResource(R.drawable.icon_wind)
                        tvType.text = "風"
                    }
                    "rock" -> {
                        ivTypeIcon.setImageResource(R.drawable.icon_rock)
                        tvType.text = "岩"
                    }
                    "crystal" -> {
                        ivTypeIcon.setImageResource(R.drawable.icon_crystal)
                        tvType.text = "鉱物"
                    }
                    "ice" -> {
                        ivTypeIcon.setImageResource(R.drawable.icon_ice)
                        tvType.text = "氷"
                    }
                    "thunder" -> {
                        ivTypeIcon.setImageResource(R.drawable.icon_thunder)
                        tvType.text = "雷"
                    }
                    "light" -> {
                        ivTypeIcon.setImageResource(R.drawable.icon_light)
                        tvType.text = "光"
                    }
                    "metal" -> {
                        ivTypeIcon.setImageResource(R.drawable.icon_metal)
                        tvType.text = "鉄"

                    }
                    // HP担当のモンスターの場合は表示せず（none）
                    "none" -> {
                        tvTypeLabel.visibility = View.GONE
                        ivTypeIcon.visibility = View.GONE
                        tvType.visibility = View.GONE
                    }
                }

            }

        // レベルと経験値
        val tvMonsterLevel = findViewById<TextView>(R.id.tvMonsterLevel)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvNeedExp = findViewById<TextView>(R.id.tvNeedExp)
        val tvMonsterTotalExp = findViewById<TextView>(R.id.tvMonsterTotalExp)


        val user = auth.currentUser?: return
        db.collection("results")
            .document(user.uid)
            .collection("ownedMonsters")
            .document(monsterId)
            .get()
            .addOnSuccessListener { doc ->
                level = (doc.getLong("level") ?: 1).toInt()
                val exp = doc.getLong("exp") ?: 0
                val totalExp = doc.getLong("totalExp") ?: 0

                // UIに反映
                tvMonsterLevel.text = "（Lv.${level}）"

                tvMonsterTotalExp.text = "$totalExp"

                val needExp = requiredExp(level)
                val currentExp = exp.toInt()

                progressBar.max = needExp
                progressBar.progress = currentExp

                tvNeedExp.text = "あと${needExp - currentExp}"
            }


        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }




    }


    private fun requiredExp(level: Int): Int {
        // レベルの10の位に応じて計算 → 例：10台なら
        val tier = level / 10
        return (tier + 1) * 100
    }
}