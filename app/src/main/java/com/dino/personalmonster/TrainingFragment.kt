package com.dino.personalmonster

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


// トレーニングのマスターデータ
data class TrainingMenu(
    var id: String = "",

    var title: String = "",
    var url : String = "",
    var parameterKey: String = "",
    var incrementValue: Int = 0,
    var description: String = "",
//    var skillDesc: String ="",
//    var skillName: String ="",
    var guide: String = "",
    var step: String ="",
    var tags: List<String> = emptyList(),
    )


// ユーザー独自のトレーニング情報
data class TrainingState(
    var id: String = "",
    var habit: Boolean = false,


    // イフゼンプラン機能で追加
    var triggerText: String = "",
    var practiceText: String = "",


    // 並び替え機能で追加
    var orderIndex: Int = 0,

    // 時間情報機能で追加
    var isCompletedToday: Boolean? = null,
    var lastCompletedDate: String? = null,
    var streakCount: Long? = null,

    var parentRoutineId: String? = null

)


// 表示用で使うトレーニングデータ
data class TrainingMenuUi(
    val id: String = "",

    var title: String = "",
    var url : String = "",
    var parameterKey: String = "",
    var incrementValue: Int = 0,
    var description: String = "",
//    var skillDesc: String ="",
//    var skillName: String ="",
    var step: String ="",
    var tags: List<String> = emptyList(),


    var guide: String = "",

    // ユーザー独自情報
    var habit: Boolean = false,

    var triggerText: String = "",
    var practiceText: String = "",


    var orderIndex: Int = 0,

    var lastCompletedDate: String? = null,
    var streakCount: Long? = null,

    var parentRoutineId: String? = null
)

enum class ParameterType(val key: String, val label: String) {
    All("all", "すべて"),
    SOCIAL("social", "社交力"),
    HARMONY("harmony", "調和力"),
    WILL("will", "意志力"),
    MENTAL("mental", "精神力"),
    EXPLORE("explore", "探究力"),
    HP("HP", "HP");

    companion object {
        fun fromLabel(label: String): ParameterType? {
            return values().find { it.label == label }
        }

        fun fromKey(key: String): ParameterType? {
            return values().find { it.key == key }
        }
    }


    // ラベルを返す処理
    override fun toString(): String {
        return label
    }
}

class TrainingFragment : Fragment() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrainingAdapter
    private lateinit var searchText: EditText
    private lateinit var categorySpinner: Spinner


    val categoryList = listOf("すべて", "社交力", "調和力", "意志力","精神力", "探究力","HP",)
//    val categoryList = listOf(
//        ParameterType.SOCIAL,
//        ParameterType.HARMONY,
//        ParameterType.WILL,
//        ParameterType.MENTAL,
//        ParameterType.EXPLORE,
//    )


    // 画面情報をActivityに渡す処理
    override fun onResume() {
        super.onResume()
        val screen = "training"
        (activity as? MainActivity2)?.setCurrentScreen(screen)
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_training, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 画面のタイトル設定
//        requireActivity().title = "全トレーニングリスト"
        (activity as AppCompatActivity).supportActionBar?.title = "全トレーニングリスト"


        // UI取得
        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewTraining)

        // 初期化
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // トレーニングメニューを読み込んでユーザーのデータを同期
        syncTrainingMenus {
            loadTrainingMenus()
        }


        // リサイクラービューでリストを表示
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = TrainingAdapter(
            items = emptyList(),
            onClick = { id, menu ->
                // アダプターのコールバックで詳細画面へと遷移する
                val intent = Intent(requireContext(), TrainingDetailActiivty::class.java)
                intent.putExtra("title", menu.title)
                intent.putExtra("url", menu.url)
                intent.putExtra("parameterKey", menu.parameterKey)
                intent.putExtra("incrementValue", menu.incrementValue)

                intent.putExtra("description", menu.description)
//                intent.putExtra("skillDesc", menu.skillDesc)
//                intent.putExtra("skillName", menu.skillName)
                intent.putExtra("step", menu.step)

                intent.putExtra("guide", menu.guide)
                intent.putStringArrayListExtra("tags", ArrayList(menu.tags))


                // 習慣化を識別するためにアイテムのIDとブーリンも運ぶ

                intent.putExtra("menuId", id)

                startActivity(intent)
            },
        )

        recyclerView.adapter = adapter



        // キーワードによって検索する処理
        searchText = view.findViewById<EditText>(R.id.etSearchEditText)
        searchText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val selectedCategory = categorySpinner.selectedItem.toString()
                val parameterKey = ParameterType.fromLabel(selectedCategory)?.key ?:return
                adapter.filterTrainingList(s.toString(), parameterKey)
            }
        })

        // カテゴリを用意
        categorySpinner = view.findViewById<Spinner>(R.id.spCategory)
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryList)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = spinnerAdapter

        // スピナーでカテゴリを選択したときのフィルター処理
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCategory = categoryList[position]

                val parameterKey = ParameterType.fromLabel(selectedCategory)?.key?: return
                adapter.filterTrainingList(searchText.text.toString(), parameterKey)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        




    }

    // トレーニングのマスターデータから取得する処理
    private fun syncTrainingMenus(onComplete: () -> Unit) {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val userMenus = db.collection("results")
                .document(userId)
                .collection("trainingMenus")

            db.collection("trainingMenus")
                .get()
                .addOnSuccessListener { masterSnapshot ->
                    userMenus.get()
                        .addOnSuccessListener { userSnapshot ->
                            val existingIds = userSnapshot.documents.map { it.id }.toSet()

                            val batch = db.batch()

                            masterSnapshot.documents.forEach { masterDoc ->
                                if (!existingIds.contains(masterDoc.id)) {
                                    batch.set(
                                        userMenus.document(masterDoc.id),
                                        mapOf(
                                            "habit" to false,
                                            "orderIndex" to 0
                                        )
                                    )
                                }
                            }

                            batch.commit()
                                .addOnSuccessListener {
                                    onComplete()
                                }
                        }
                }
        }

    }

    // トレーニング一覧をリサイクラービューに表示する処理
    private fun loadTrainingMenus() {
        // データベースからデータを取得

        val user = auth.currentUser ?: return

        db.collection("trainingMenus")
            .get()
            .addOnSuccessListener { masterDoc ->
                // 全ユーザー共通のメニューデータベースを取得
                val masterList = masterDoc.map {
                    val menu = it.toObject(TrainingMenu::class.java)
                    menu.id = it.id
//                    Log.i("タグ確認", "${menu.title} : ${menu.tags}")

                    menu

                }

                // マスターデータとユーザーのデータを統合ー
                db.collection("results")
                    .document(user.uid)
                    .collection("trainingMenus")
                    .get()
                    .addOnSuccessListener { stateDoc ->
                        val stateMap = stateDoc.map {
                            val state = it.toObject(TrainingState::class.java)
                            state.id = it.id
                            state
                        }.associateBy { it.id }


                        val mergedList = masterList.map { master ->

                            val state = stateMap[master.id]

                            TrainingMenuUi(
                                id = master.id,

                                title = master.title,
                                url = master.url,
                                parameterKey = master.parameterKey,
                                incrementValue = master.incrementValue,
                                description = master.description,
                                step = master.step,

                                guide = master.guide,
                                tags = master.tags,

                                triggerText = state?.triggerText?: "",
                                habit = state?.habit ?: false,
                                orderIndex = state?.orderIndex?: 0,
                                streakCount = state?.streakCount,
                                lastCompletedDate = state?.lastCompletedDate,
                                parentRoutineId = state?.parentRoutineId
                            )


                        }


                        adapter.updateList(mergedList)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("tag", "データ取得失敗：${exception.message}")
            }



    }




}
