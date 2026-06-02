package com.dino.personalmonster

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.roundToInt



// 所有しているモンスターのデータ
data class OwnedMonster(
    val monsterId: String = "",
    val slot: String = "",
    val equipped: Boolean = false,
    val level: Int = 0
)

// ユーザー共通のモンスターの基本データ
data class MonsterMaster(
    val monsterId: String = "",
    val name: String = "",
    val description: String = "",
    val imageRes: String = "",
    val type: String = "",
    val slot: String = "",
    val rarity: String = "",

    )

// リサイクラービューに表示する用の統合モンスターデータ
data class DisplayMonster(
    val monsterId: String = "",
    val name: String = "",
    val slot: String = "",

    // 変換語のリソースなので数値
    val imageRes: Int = 1,
    // モンスタータブのために拡張
    val owned: Boolean = false,
    val type:  String = "",

    val level: Int = 0

)


class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val repository = MonsterRepository()

    private lateinit var radarChart: RadarChart
    private lateinit var recyclerView: RecyclerView
    private lateinit var monsterAdapter: MonsterAdapter
    private lateinit var currentEntries: List<RadarEntry>
    private lateinit var initialEntries: List<RadarEntry>

    private lateinit var ivTypeIconE: ImageView
    private lateinit var ivTypeIconA: ImageView
    private lateinit var ivTypeIconC: ImageView
    private lateinit var ivTypeIconN: ImageView
    private lateinit var ivTypeIconO: ImageView

    private lateinit var cvGuestRegister: CardView


        override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // 画面のタイトル設定
//        requireActivity().title = "ホーム画面"
        (activity as AppCompatActivity).supportActionBar?.title = "ホーム画面"

        // Firebaseからパラメータを取得
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid

        // 新規モンスター登録処理 → 仮
//        val btnRegisterMonster = view.findViewById<Button>(R.id.btnRegisterMonster)
//        btnRegisterMonster.setOnClickListener {
//            registerNewMonster("111")
//
//        }
        cvGuestRegister = view.findViewById<CardView>(R.id.cvGuestRegister)

        // ゲストへの案内
        showGuestRegister()


        val btnGuestRegister = view.findViewById<TextView>(R.id.btnGuestRegister)
        btnGuestRegister.setOnClickListener {
            val intent = Intent(requireContext(), GuestUpgradeActivity::class.java)
            startActivity(intent)
        }

        // レーダーチャートを取得
        radarChart = view.findViewById<RadarChart>(R.id.radarChart)


        // 現在値の習得
        if(userId != null) {
            db.collection("results")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val eTotal = document.getLong("personality.currentData.social") ?: 0
                        val aTotal = document.getLong("personality.currentData.harmony") ?: 0
                        val cTotal = document.getLong("personality.currentData.will") ?: 0
                        val nTotal = document.getLong("personality.currentData.mental") ?: 0
                        val oTotal = document.getLong("personality.currentData.explore") ?: 0
                        // HPは現在値だけ
                        val hpTotal = document.getLong("personality.currentData.HP") ?: 0

//                        Log.i("現在値", "現在値は${eTotal}, ${aTotal}, ${cTotal}, ${nTotal}, ${oTotal}, ${hpTotal}")

                        val eInitial = document.getDouble("personality.initialData.extraversion") ?: 0.0
                        val aInitial = document.getDouble("personality.initialData.agreeableness") ?: 0.0
                        val cInitial = document.getDouble("personality.initialData.conscientiousness") ?: 0.0
                        val nInitial = document.getDouble("personality.initialData.neuroticism") ?: 0.0
                        val oInitial = document.getDouble("personality.initialData.openness") ?: 0.0
//                        Log.i("初期値", "初期値は${eInitial}, ${aInitial}, ${cInitial}, ${nInitial}, ${oInitial}")

                        // 初期値は10倍にして四捨五入
                        val eInitialDisplay = (eInitial * 10).roundToInt()
                        val aInitialDisplay = (aInitial * 10).roundToInt()
                        val cInitialDisplay = (cInitial * 10).roundToInt()
                        val nInitialDisplay = (nInitial * 10).roundToInt()
                        val oInitialDisplay = (oInitial * 10).roundToInt()



//                        Log.i("ホームに表示する現在値", "現在値は${eTotal}, ${aTotal}, ${cTotal}, ${nTotal}, ${oTotal}, ${hpTotal}")
//                        Log.i("ホームに表示する初期値", "初期値は${eInitialDisplay}, ${aInitialDisplay}, ${cInitialDisplay}, ${nInitialDisplay}, ${oInitialDisplay}")


                        // テキストにスコアを反映（現在値で）
                        val tvParaE = view.findViewById<TextView>(R.id.tvParaIntE)
                        val tvParaA = view.findViewById<TextView>(R.id.tvParaIntA)
                        val tvParaC = view.findViewById<TextView>(R.id.tvParaIntC)
                        val tvParaN = view.findViewById<TextView>(R.id.tvParaIntN)
                        val tvParaO = view.findViewById<TextView>(R.id.tvParaIntO)
                        val tvParaHP = view.findViewById<TextView>(R.id.tvParaIntHP)

                        tvParaE.text = eTotal.toString()
                        tvParaA.text = aTotal.toString()
                        tvParaC.text = cTotal.toString()
                        tvParaN.text = nTotal.toString()
                        tvParaO.text = oTotal.toString()
                        tvParaHP.text = hpTotal.toString()


                        // レーダーチャートに反映

                            // 値をレーダーチャート用のデータに変換
                        currentEntries = listOf(
                            RadarEntry(eTotal.toFloat()),
                            RadarEntry(aTotal.toFloat()),
                            RadarEntry(cTotal.toFloat()),
                            RadarEntry(nTotal.toFloat()),
                            RadarEntry(oTotal.toFloat())
                        )
                        initialEntries = listOf(
                            RadarEntry(eInitialDisplay.toFloat()),
                            RadarEntry(aInitialDisplay.toFloat()),
                            RadarEntry(cInitialDisplay.toFloat()),
                            RadarEntry(nInitialDisplay.toFloat()),
                            RadarEntry(oInitialDisplay.toFloat()),
                        )


                        createRadarChart()

                    }
                }
                .addOnFailureListener { e ->
//                    Log.i("tag", "データ取得失敗： ${e.message}")
                }
        }





//        ホーム画面のモンスターを動的に表示する
        // タイプセットアイコンの習得
        ivTypeIconE = view.findViewById<ImageView>(R.id.ivTypeIconE)
        ivTypeIconA = view.findViewById<ImageView>(R.id.ivTypeIconA)
        ivTypeIconC = view.findViewById<ImageView>(R.id.ivTypeIconC)
        ivTypeIconN = view.findViewById<ImageView>(R.id.ivTypeIconN)
        ivTypeIconO = view.findViewById<ImageView>(R.id.ivTypeIconO)

        // 情報アイコン
        val ivInfoIcon = view.findViewById<ImageView>(R.id.ivInfoIcon)
        val ivTypeInfoIcon = view.findViewById<ImageView>(R.id.ivTypeInfoIcon)

        ivInfoIcon.setOnClickListener {
            val content = MainActivity2.InfoContent(
                title = "育成中モンスター",
                message = "あなたが性格スキルを鍛えると、ここにセットされているパソモンに経験値が入ります。自分が育てたいパソモンをセットしておきましょう。",
                imageResource = null
            )
            showInfoDialog(content)
        }

        ivTypeInfoIcon.setOnClickListener {
            val content = MainActivity2.InfoContent(
                title = "タイプについて",
                message = "タイプは診断されたときの性格によって決まります。",
                imageResource = R.drawable.dialog_info_type_transparent
            )
            showInfoDialog(content)
        }

        // リサイクラービューを取得
        recyclerView = view.findViewById<RecyclerView>(R.id.rvMonsters)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)


        // クリック、長押しに対応したアダプターを取得
        monsterAdapter = MonsterAdapter(
            showLevel = true,

            // クリックで詳細画面に遷移
            onClick = { monster ->
                // 所有していない場合は詳細に移動しない
//                if (!monster.owned) return@MonsterAdapter

                // モンスター詳細へ遷移する際に、IDを渡す
                val intent = Intent(requireContext(), MonsterDetailActivity::class.java)
                intent.putExtra("monsterId", monster.monsterId)
                startActivity(intent)
            },

                // 長押しで置き換えの処理
            onLongClick = { monster ->
//                Log.i("長押し確認", "長押しで置き換え処理を開始")
                showReplaceMonsterList(monster)
                true
            }
        )

            // アダプターをリサイクラービューにセット
        recyclerView.adapter = monsterAdapter



        // 所有モンスターをロードする処理
        reloadMonsterList()
    }



    // 画面情報をActivityに渡す処理
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity2)?.setCurrentScreen("home")

        showGuestRegister()
    }


    private fun showGuestRegister() {
        if (auth.currentUser?.isAnonymous == true) {
            cvGuestRegister.visibility = View.VISIBLE
        } else {
            cvGuestRegister.visibility = View.GONE
        }
    }

    private fun reloadMonsterList() {
        val userId = auth.currentUser?.uid

        if(userId != null) {
            db.collection("results")
                .document(userId)
                .collection("ownedMonsters")
                .whereEqualTo("equipped", true)
                .get()
                .addOnSuccessListener { documentSnapshots ->
                    // equippedが6件取得できているかチェック
//                    Log.i("所有モンスターの取得件数", "equipped count = ${documentSnapshots.size()}")

                    val ownedMonsterList = documentSnapshots.mapNotNull {
                        it.toObject(OwnedMonster::class.java)
                    }


                    // 取得したモンスターIDからマスターデータを引き、描画する
                    loadMonsterMasters(ownedMonsterList)
                }
        }
    }

    // 取得したモンスターIDからマスターデータを引き、描画する実際の処理
    private fun loadMonsterMasters(list: List<OwnedMonster>) {
        val displayList = mutableListOf<DisplayMonster>()
        val db = FirebaseFirestore.getInstance()

        list.forEach { owned ->
            db.collection("monsters")
                .document(owned.monsterId)
                .get()
                .addOnSuccessListener { snapshot ->
                    // フラグメントが生きていないなら
                    if(!isAdded) return@addOnSuccessListener

                    // コンテキストがないなら処理をとめる
                    val ctx = context ?: return@addOnSuccessListener

//                    Log.e("debug", "${owned.monsterId}")
                    val master = snapshot.toObject(MonsterMaster::class.java)?: return@addOnSuccessListener

//                    Log.i("tag", master.imageRes)
                    val imageRes = resources.getIdentifier(master.imageRes, "drawable", ctx.packageName)
//                    Log.i("img", "${imageRes}")
                    displayList.add(
                        DisplayMonster(
                            monsterId = master.monsterId,
                            name = master.name,
                            imageRes = imageRes,
                            slot = owned.slot,
                            type = master.type,
                            level = owned.level

                        )
                    )

//                    Log.i("size", "${displayList.size}")

                    // 5つのデータをすべて取得できたら
                    if (displayList.size == list.size) {
                        // 表示する順番に並び替え
                        displayList.sortBy {
                            when (it.slot) {
                                "social" -> 0
                                "harmony" -> 1
                                "will" -> 2
                                "mental" -> 3
                                "explore" -> 4
                                else -> 99
                            }
                        }
                        monsterAdapter.submitList(displayList)

                        createTypeSet(displayList)
                    }

                }
        }
    }

    private fun showTypeSet(type: String, ivTypeIcon: ImageView) {

        when (type) {
            "fire" -> { ivTypeIcon.setImageResource(R.drawable.icon_fire) }
            "fairy" -> { ivTypeIcon.setImageResource(R.drawable.icon_fairy) }
            "ghost" -> { ivTypeIcon.setImageResource(R.drawable.icon_ghost) }

            "grass" -> { ivTypeIcon.setImageResource(R.drawable.icon_grass) }
            "flower" -> { ivTypeIcon.setImageResource(R.drawable.icon_flower) }
            "poison" -> { ivTypeIcon.setImageResource(R.drawable.icon_poison) }

            "water" -> { ivTypeIcon.setImageResource(R.drawable.icon_water) }
            "mist" -> { ivTypeIcon.setImageResource(R.drawable.icon_mist) }
            "wind" -> { ivTypeIcon.setImageResource(R.drawable.icon_wind) }

            "rock" -> { ivTypeIcon.setImageResource(R.drawable.icon_rock) }
            "crystal" -> { ivTypeIcon.setImageResource(R.drawable.icon_crystal) }
            "ice" -> { ivTypeIcon.setImageResource(R.drawable.icon_ice) }

            "thunder" -> { ivTypeIcon.setImageResource(R.drawable.icon_thunder) }
            "magnet" -> { ivTypeIcon.setImageResource(R.drawable.icon_magnet) }
            "metal" -> { ivTypeIcon.setImageResource(R.drawable.icon_metal) }
        }
    }

    private fun createTypeSet(monsters: MutableList<DisplayMonster>) {
        val typeSet = mutableListOf<String>()

        // EACNOの順番に取り出して、タイプを把握
        monsters.forEach { monster ->
            typeSet.add(monster.type)
        }

//        Log.i("タイプのリスト", "${typeSet}")
        showTypeSet(typeSet[0], ivTypeIconE)
        showTypeSet(typeSet[1], ivTypeIconA)
        showTypeSet(typeSet[2], ivTypeIconC)
        showTypeSet(typeSet[3], ivTypeIconN)
        showTypeSet(typeSet[4], ivTypeIconO)

//
//
//
//                "grass" -> {
//                    ivTypeIconA.setImageResource(R.drawable.icon_grass)
//                }
//                "flower" -> {
//                    ivTypeIconA.setImageResource(R.drawable.icon_flower)
//                }
//                "poison" -> {
//                    ivTypeIconA.setImageResource(R.drawable.icon_poison)
//                }
//                "water" -> {
//                    ivTypeIconC.setImageResource(R.drawable.icon_water)
//                }
//                "mist" -> {
//                    ivTypeIconC.setImageResource(R.drawable.icon_mist)
//                }
//                "wind" -> {
//                    ivTypeIconC.setImageResource(R.drawable.icon_wind)
//                }
//                "rock" -> {
//                    ivTypeIconN.setImageResource(R.drawable.icon_rock)
//                }
//                "crystal" -> {
//                    ivTypeIconN.setImageResource(R.drawable.icon_crystal)
//                }
//                "ice" -> {
//                    ivTypeIconN.setImageResource(R.drawable.icon_ice)
//                }
//                "thunder" -> {
//                    ivTypeIconO.setImageResource(R.drawable.icon_thunder)
//                }
//                "magnet" -> {
//                    ivTypeIconO.setImageResource(R.drawable.icon_magnet)
//                }
//                "metal" -> {
//                    ivTypeIconO.setImageResource(R.drawable.icon_metal)
//
//                }
    }

    private fun createRadarChart() {
        val ctx = context ?:return

        // 描画される前にリセット
        radarChart.clear()
        radarChart.setExtraOffsets(40f, 40f, 40f, 40f)

        // データセットを作成（現在値）
        val currentDataset = RadarDataSet(currentEntries, "現在値").apply {

            color = ContextCompat.getColor(ctx, R.color.primary)
            fillColor = ContextCompat.getColor(ctx, R.color.radarCurrentFillColor)
            fillAlpha = 120
            setDrawFilled(true)

            lineWidth = 2.5f
            valueTextSize = 12f
            valueTextColor = Color.DKGRAY
        }

        // 小数点削除
        currentDataset.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String? {
                return value.toInt().toString()
            }
        }

        // データセットを作成（初期値）
        val initialDataset = RadarDataSet(initialEntries, "初期値").apply {
            color = ContextCompat.getColor(ctx, R.color.radarInitialColor)
            fillColor = ContextCompat.getColor(ctx, R.color.radarInitialFillColor)
            fillAlpha = 120
            setDrawFilled(true)
            enableDashedHighlightLine(10f, 5f, 0f)

            lineWidth = 1.5f
            setDrawValues(false)
        }



        // データセットをデータに流し込む
        val radarData = RadarData(listOf(initialDataset, currentDataset))
        radarChart.data = radarData

        // X軸(ラベル)の設定
        val labels = listOf("社交力","調和力","意志力","精神力","探究力")
        radarChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        radarChart.xAxis.textSize = 12f
        radarChart.xAxis.textColor = Color.DKGRAY

        // Y軸の設定
        val maxValue = currentEntries.maxOf { it.y }
        radarChart.yAxis.apply {
            axisMinimum = 0f
            axisMaximum = maxValue
            setDrawLabels(false)
        }


        // 網目デザインの設定
        radarChart.webLineWidth = 0f
        radarChart.webColor = Color.LTGRAY
        radarChart.webLineWidthInner = 0f
        radarChart.webColorInner = Color.LTGRAY
        radarChart.webAlpha = 100

        // その他設定
        radarChart.isRotationEnabled = false
        radarChart.description.isEnabled = false
//        radarChart.legend.isEnabled = false

//        radarChart.setBackgroundColor(Color.TRANSPARENT)



        // アニメーション
        radarChart.animateY(1000)
        // 描画を更新する
        radarChart.invalidate()

    }


    // 置き換え処理で置き換えの候補を出す処理
    private fun showReplaceMonsterList(currentMonster: DisplayMonster) {
        val slot = currentMonster.slot
        val type = currentMonster.type
//        Log.i("現在セットのスロットとタイプ確認", "スロットは${slot},タイプは${type}")

        repository.loadOwnedMonsterIds { ownedIds ->
            db.collection("monsters")
                .whereEqualTo("type", type)
                .get()
                .addOnSuccessListener { snapshots ->
                    // 非同期処理なのでコンテキストがないなら処理をとめる
                    val ctx = context ?: return@addOnSuccessListener


                    val candidates = snapshots.documents.mapNotNull { documentSnapshot ->
                        val master = documentSnapshot.toObject(MonsterMaster::class.java)
                            ?: return@mapNotNull null
                        if (!ownedIds.contains(master.monsterId)) {
                            return@mapNotNull null
                        }

                        DisplayMonster(
                            monsterId = master.monsterId,
                            name = master.name,
                            imageRes = resources.getIdentifier(
                                master.imageRes,
                                "drawable",
                                ctx.packageName
                            ),
                            slot = master.slot,
                            type = master.type,
                        )

                    }

                    // 置き換えの候補の画面を作る
                    showBottomSheet(slot, candidates)
                }

        }

    }

    // 置き換えの候補の画面を作る処理
    private fun showBottomSheet(slot: String, candidates: List<DisplayMonster>) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_monster, null)
        val rvCandidates = view.findViewById<RecyclerView>(R.id.rvCandidates)
        rvCandidates.layoutManager = GridLayoutManager(requireContext(), 3)

        val candidateAdapter = MonsterAdapter(
            showLevel = false,
            onClick =  { selectedMonster ->
                // 実際の置き換えを行う
                replaceMonster(slot, selectedMonster.monsterId)
                dialog.dismiss()
            },
            onLongClick = {}
        )

        rvCandidates.adapter = candidateAdapter
        candidateAdapter.submitList(candidates)

        dialog.setContentView(view)
        dialog.show()
    }






    //  実際の置き換えを行う処理
    private fun replaceMonster(slot: String, newMonsterId: String) {
        val uid = FirebaseAuth.getInstance().uid!!
        val ownedRef = db.collection("results")
            .document(uid)
            .collection("ownedMonsters")

        ownedRef.whereEqualTo("slot", slot)
            .whereEqualTo("equipped", true)
            .get()
            .addOnSuccessListener { snapshots ->
                val batch = db.batch()

                // 現在の装備モンスターの状態をfalseにする
                snapshots.documents.forEach { documentSnapshot ->
                    batch.update(documentSnapshot.reference, "equipped", false)
                }

                // 新しい装備モンスターの状態をtrueにする
                val newRef = ownedRef.document(newMonsterId)
                batch.update(newRef, "equipped", true)

                // ユーザー側の装備中モンスターも更新
                val userRef = db.collection("results")
                    .document(uid)
                batch.update(userRef, "equippedMonsters.${slot}", newMonsterId)

                // 一括更新
                batch.commit().addOnSuccessListener {
                    // モンスターをリロードして表示し直す
                    reloadMonsterList()
                }
            }

    }

    // 新しいモンスターを登録する処理
    private fun registerNewMonster(monsterId: String) {
        val uid = FirebaseAuth.getInstance().uid!!
        val ownedRef = db.collection("results")
            .document(uid)
            .collection("ownedMonsters")


        db.collection("monsters")
            .document(monsterId)
            .get()
            .addOnSuccessListener { snapshot ->
                val slot = snapshot.getString("slot") ?:return@addOnSuccessListener

                val newMonsterInfo = mapOf(
                    "monsterId" to monsterId,
                    "equipped" to false,
                    "slot" to slot,
                    "obtainedAt" to FieldValue.serverTimestamp(),

                    // 初期値も設定
                    "level" to 1,
                    "exp" to 0,
                    "totalExp" to 0
                )

                ownedRef
                    .document(monsterId)
                    .set(newMonsterInfo)
                    .addOnSuccessListener {
//                        Log.i("新規モンスター登録", "新規モンスターとして${monsterId}が登録されました。")
                    }


            }
    }

    // 情報を出す処理
    private fun showInfoDialog(content: MainActivity2.InfoContent) {
        if (content.imageResource == null) {
            AlertDialog.Builder(requireContext())
                .setTitle(content.title)
                .setMessage(content.message)
                .setPositiveButton("閉じる", null)
                .show()
        } else {
            // 画像の場合
            val dialogView = layoutInflater.inflate(R.layout.dialog_info_type, null)

            val tvMessage = dialogView.findViewById<TextView>(R.id.tvInfoAboutType)
            val ivImage = dialogView.findViewById<ImageView>(R.id.ivInfoAboutType)

            tvMessage.text = content.message
            ivImage.setImageResource(content.imageResource)


            AlertDialog.Builder(requireContext())
                .setTitle(content.title)
                .setView(dialogView)
                .setPositiveButton("閉じる", null)
                .show()
        }

    }




}