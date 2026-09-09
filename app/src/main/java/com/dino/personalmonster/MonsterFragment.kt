package com.dino.personalmonster

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.firestore.FirebaseFirestore


class MonsterFragment : Fragment() {
    // アプリ表示用とFirestore登録用を管理するデータ
    data class TypeItem(
        val label: String = "",
        val value: String? = ""
    ) {
        override fun toString(): String {
            return label
        }
    }
    private lateinit var recyclerView : RecyclerView
    private lateinit var monsterAdapter: MonsterAdapter
    private val db = FirebaseFirestore.getInstance()
    private val repository = MonsterRepository()

    // フィルターの状態
    private var showOwnedOnly: Boolean = true
    private var selectedType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_monster, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 画面のタイトル設定
//        requireActivity().title = "パソモン図鑑"
        (activity as AppCompatActivity).supportActionBar?.title = "パソモン図鑑"


        // リサイクラービューの取得と、2列で並べる
        recyclerView = view.findViewById<RecyclerView>(R.id.rvMonsterList)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // トグルボタンで表示範囲に応じてモンスターのロード
        val tgMonsterScope = view.findViewById<MaterialButtonToggleGroup>(R.id.tgMonsterScope)

            // トグルで「所有」のトグルボタンをデフォルトで表示する
        tgMonsterScope.check(R.id.btOwned)

        tgMonsterScope.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            // 今のモード（所有済みorすべて）に応じて、フラグを切り替え
            showOwnedOnly = when(checkedId) {
                R.id.btOwned -> true
                R.id.btAddHabit -> false
                else -> false
            }

            // フラグに応じてモンスターのロード
            loadMonsterDictionary(showOwnedOnly = showOwnedOnly, typeFilter = selectedType)
        }

        // スピナーでタイプに応じてモンスターのロード
            // スピナーのドロップダウンに入れるリスト
        val typeList = listOf(
            TypeItem("すべてのタイプ", null),
            TypeItem("炎タイプ", "fire"),
            TypeItem("妖タイプ", "fairy"),
            TypeItem("霊タイプ", "ghost"),

            TypeItem("草タイプ", "grass"),
            TypeItem("花タイプ", "flower"),
            TypeItem("毒タイプ", "poison"),

            TypeItem("水タイプ", "water"),
            TypeItem("霧タイプ", "mist"),
            TypeItem("風タイプ", "wind"),

            TypeItem("岩タイプ", "rock"),
            TypeItem("鉱物タイプ", "crystal"),
            TypeItem("氷タイプ", "ice"),

            TypeItem("雷タイプ", "thunder"),
            TypeItem("光タイプ", "light"),
            TypeItem("鉄タイプ", "metal"),

            )
            // ますスピナーのUI取得と設定
        val spType = view.findViewById<Spinner>(R.id.spType)
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, typeList)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spType.adapter = spinnerAdapter

        spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
//                val selected = parent.getItemAtPosition(position) as String
                val selected = parent.getItemAtPosition(position) as TypeItem
//                selectedType = if(selected == "すべて") null else selectedType
                selectedType = selected.value
                loadMonsterDictionary(showOwnedOnly = showOwnedOnly, typeFilter = selectedType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedType = null
            }
        }



        monsterAdapter = MonsterAdapter(
            showLevel = false,
            // クリックで詳細画面に遷移
            onClick = { monster ->
                // 所有していない場合は詳細に移動しない

                if (!monster.owned) {
                    Toast.makeText(requireContext(), "このパソモンはまだ登録されていません。", Toast.LENGTH_LONG).show()
                    return@MonsterAdapter
                }

                val intent = Intent(requireContext(), MonsterDetailActivity::class.java)
                intent.putExtra("monsterId", monster.monsterId)
                startActivity(intent)
            },
            // 長押しの処理はなし
            onLongClick = {
//                Log.i("長押し確認", "長押しの処理はありません")
            }

        )

        recyclerView.adapter = monsterAdapter
        // 初期のロード
//        loadMonsterDictionary()
    }

    // 画面情報をActivityに渡す処理
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity2)?.setCurrentScreen("monster")
    }



    // 所有モンスターのIDを読み込む実際の処理

//    private fun loadOwnedMonsterIds(onLoaded: (Set<String>) -> Unit) {
//        val userId = FirebaseAuth.getInstance().currentUser!!.uid
//        db.collection("results")
//            .document(userId)
//            .collection("ownedMonsters")
//            .get()
//            .addOnSuccessListener { snapshot ->
//                val ownedIds = snapshot.documents.map { it.id }.toSet()
//                onLoaded(ownedIds)
//            }
//            .addOnFailureListener {
//                onLoaded(emptySet())
//            }
//
//    }


    private fun loadMonsterDictionary(showOwnedOnly: Boolean = false,
                                      typeFilter: String? = null) {

        repository.loadOwnedMonsterIds { ownedIds ->
            db.collection("monsters")
                .orderBy("monsterId")
                .get()
                .addOnSuccessListener { snapshots ->
                    // フラグメントが生きていないなら
                    if(!isAdded) return@addOnSuccessListener

                    // コンテキストがとれないなら処理をとめる
                    val ctx = context ?: return@addOnSuccessListener

                    val displayList = snapshots.documents.mapNotNull { doc ->
                        val master = doc.toObject(MonsterMaster::class.java)?: return@mapNotNull null
                        val owned = ownedIds.contains(master.monsterId)

                        // 所有のみ表示のときで、未所持のモンスターオブジェクトならば追加しないで終わる
                        if (showOwnedOnly && !owned) return@mapNotNull null

                        // タイプフィルターと合致しないモンスターオブジェクトならば追加しないで終わる
                        if (typeFilter != null && master.type != typeFilter) {
                            return@mapNotNull null
                        }

                        // すべて表示のときや、タイプフィルターと一致するとき？
                        val iconImageRes = if (owned) {
                             resources.getIdentifier(master.monsterIconRes, "drawable", ctx.packageName)
                        } else {
                            R.drawable.question
                        }
                        DisplayMonster(
                            monsterId = master.monsterId,
                            name = if(owned) master.name else "???",
//                            monsterImageRes = imageRes,
                            monsterIconRes = iconImageRes,
                            owned = owned,
                            type = master.type,
                        )
                    }
//                        .filter {
//                            typeFilter == null || it.type == typeFilter
//                        }
                    monsterAdapter.submitList(displayList)
                }
        }
    }

}