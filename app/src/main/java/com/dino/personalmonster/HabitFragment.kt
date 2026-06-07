package com.dino.personalmonster

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
//import androidx.compose.material3.AlertDialog
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate


class HabitFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: HabitAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabRoutine: ExtendedFloatingActionButton
    private lateinit var fabMain: ExtendedFloatingActionButton

    private val repository = TrainingRepository()
    private lateinit var tvNoHabit: TextView




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_habit, container, false)
    }





    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 画面のタイトル設定
//        requireActivity().title = "習慣リスト"
        (activity as AppCompatActivity).supportActionBar?.title = "習慣リスト"


        // Firebaseの取得
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 「追加されていません」のテキスト → 基本は非表示
        tvNoHabit = view.findViewById(R.id.tvNoHabit)
        tvNoHabit.visibility = View.GONE


        // フローティングボタンの取得
        fabRoutine = view.findViewById<ExtendedFloatingActionButton>(R.id.fabRoutine)
        fabMain = view.findViewById<ExtendedFloatingActionButton>(R.id.fabMain)

        // 最初は非表示
        fabMain.visibility = View.GONE // 最初は非表示
        fabRoutine.visibility = View.GONE // 最初は非表示


        // ボタンを取得して、押されたときに編集モードに入る
        fabMain.setOnClickListener { toggleEditMode() }

        // ルーティン化のFABが押されたときに一括でルーティンとして登録する
        fabRoutine.setOnClickListener {
            val selectedIds = adapter.getSelectedIds()
            if (selectedIds.isEmpty()) {
                Toast.makeText(requireContext(), "項目を選択してください", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            showRoutineSelectedDialog(selectedIds)

        }



        // リサイクラービューでリストを作る
        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewHabit)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HabitAdapter(
            allItems = mutableListOf(),
            showTriggerText = true,
//            onItemClick = { (id, menu) ->
            // メニュー（ルーティンor単体メニュー）を押したときの処理
            onMenuClick = { id, menu ->
                // 詳細画面へと遷移する
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
    //            intent.putExtra("habit", menu.habit)
                intent.putExtra("menuId", id)
                startActivity(intent)
            },
            onRoutineClick = { routineId ->
//                Log.i("ローティンからの遷移", "${routineId}のルーティンの詳細へ")
                val intent = Intent(requireContext(), RoutineDetailActivity::class.java)
                intent.putExtra("routineId", routineId)
                startActivity(intent)
            },
            // メニュー（ルーティンor単体メニュー）を完了したときの処理
            onMenuCompleteClick = { id, menu ->
                // 単体の達成処理を行う
                repository.completeTraining(
                    menuId = id,
                    parameterKey = menu.parameterKey,
                    incrementValue = menu.incrementValue,
                    onSuccess = { result ->
                        loadHabit()

//                        Log.i("渡ってきた結果", "${result.monsterName}")
//                        Log.i("渡ってきた結果", "${result.popupColorRes}")
//                        Log.i("渡ってきた結果", "${result.newLevel}")


                        // 達成のポップアップを出す
                        repository.showTrainingPopup(
                            activity = requireActivity(),
                            popupColorRes = result.popupColorRes,
                            line1 = "${result.parameterLabel}＋${result.incrementValue}",
                            monsterIconRes = result.monsterImageRes,
                            line2 = "${result.monsterName}＋${result.gainedExp}",
                            levelUpCount = result.levelUpCount,
                            newLevel = result.newLevel
                        )

                    },
                    onFailure = {}
                )
            },
            onRoutineCompleteClick = { routineItem ->
                val dialog = ConfirmDialogFragment.newInstance("このルーティン内のすべてのアイテムを達成にしますか？", 8) { requestCode ->
                    completeRoutine(routineItem)
                }
                dialog.show(parentFragmentManager, "ConfirmDialog")

            },
            onDeleteClick = { menuId ->
                showDeleteDialog(menuId)
            }
        )

        recyclerView.adapter = adapter


        // 習慣化された項目を読み込む
        loadHabit()


        // 編集モードでハンドラをタッチされたときに入れ替え処理をする
        val callback = HabitItemTouchHelperCallback(adapter) {

            // アダプターから表示用リストと元データリストを同期する
//            adapter.syncDisplayToRoot()

            // Firestoreに並び順を保存する
            updateOrderInFirestore()
        }
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recyclerView)

        adapter.touchHelper = touchHelper

    }


    // 編集モードを制御するメソッド
    private fun toggleEditMode() {
        if(!adapter.isEditMode) {
            adapter.enterEditMode()
//            Log.i("編集モード入った", "${adapter.isEditMode}")
            // アニメーションでフローティングボタンが飛び出る処理


            // 編集モードでは☓アイコンに切り替え
            fabMain.setIconResource(R.drawable.ic_close)
            fabMain.text = "編集を完了"
            showFab(fabRoutine, -dpToPx(70))
        } else {
            adapter.exitEditMode()
//            Log.i("編集モード抜ける", "${adapter.isEditMode}")

            // 編集モードを抜けたらアイコンを戻す
            fabMain.setIconResource(R.drawable.ic_edit)
            fabMain.text = "習慣を編集"

            hideHFab(fabRoutine)

        }
    }

    // dp変換の処理
    private fun dpToPx(dp: Int): Float {
        return dp * resources.displayMetrics.density
    }

    // フローティングボタンをアニメーションで飛び出す処理
    private fun showFab(fab: ExtendedFloatingActionButton, translationY: Float) {
//        Log.i("移動距離", "${translationY}")
        fab.visibility = View.VISIBLE
        fab.animate()
            .translationY(translationY)
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(200)
            .start()
    }

    // フローティングボタンをアニメーションつきでしまう処理
    private fun hideHFab(fab: ExtendedFloatingActionButton) {
        fab.animate()
            .translationY(0f)
            .alpha(0f)
            .scaleX(0f)
            .scaleY(0f)
            .setDuration(150)
            .withEndAction {
                fab.visibility = View.INVISIBLE
            }
            .start()
    }

    // 詳細から戻ったときにも更新
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        // 戻ったときにも画面更新 → 習慣詳細画面からの削除や達成に対応
        loadHabit()


        // 画面情報をActivityに渡す処理
        (activity as? MainActivity2)?.setCurrentScreen("habit")
    }


    // 習慣化された項目を読み込むメソッド
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadHabit() {
        val user = auth.currentUser ?:return

        // 最終的なルーティン＋単体メニューの総合リストを定義
        val habitItems = mutableListOf<HabitItem>()

        // 重複防止用データセット
        val routineMenuSet = mutableListOf<String>()

        // ルーティン取得
        db.collection("results")
            .document(user.uid)
            .collection("routines")
            .orderBy("orderIndex")
            .get()
            .addOnSuccessListener { routineSnapshots ->
//                Log.i("ルーティンの取得数", "ルーティン登録されているものは${routineSnapshots.size()}")
                // ルーティンメニュー自体を仮置きするリスト
                val routineList = mutableListOf<HabitItem.routineItem>()

                routineSnapshots.forEach { routineDoc ->
                    // ルーティンのid・名前・ネストされている単体メニューのリストを取得
                    val routineId = routineDoc.id
                    val routineName = routineDoc.getString("name") ?: ""
                    val routineTrigger = routineDoc.getString("triggerText") ?:""
                    val menuIds = routineDoc.get("menuIds") as? List<String> ?: emptyList()

                    // 重複防止用データセットに入れておく
                    routineMenuSet.addAll(menuIds)

                    // ルーティンメニュー自体を仮置き
                    val routineItem = HabitItem.routineItem(
                        routineId = routineId,
                        name = routineName,
                        orderIndex = routineDoc.getLong("orderIndex")?.toInt() ?:0,
                        triggerText = routineTrigger
                    )
//                    Log.i("ルーティンの概要", "${routineId}, ${routineName}")

                    // 仮置きリストに追加
                    routineList.add(routineItem)

                }

                // ルーティン取得成功後のなかに、単体メニュー取得の処理
                db.collection("results")
                    .document(user.uid)
                    // ユーザーの全データからフラグがtrueのものだけを表示する
                    .collection("trainingMenus")
                    .whereEqualTo("habit",true)
                    .orderBy("orderIndex")
                    .get()
                    .addOnSuccessListener { stateSnapshots ->
//                        Log.i("習慣化フラグの取得数", "習慣化フラグがついているものは${menuSnapshots.size()}")

                        val stateMap = stateSnapshots.documents.mapNotNull { doc ->
                            val state = doc.toObject(TrainingState::class.java)
                            state?.apply { id = doc.id }
                        }.associateBy { it.id }

                        db.collection("trainingMenus")
                            .get()
                            .addOnSuccessListener { masterSnapshots ->
                                // 単体メニューIDとルーティンネストの単体メニューIDを照合するためのマップ
                                val menuMap = mutableMapOf<String, TrainingMenuUi>()

                                masterSnapshots.documents.forEach { masterDoc ->
                                    val master = masterDoc.toObject(TrainingMenu::class.java) ?:return@forEach

                                    val state = stateMap[masterDoc.id]

                                    if (state?.habit == true) {
                                        val menu = TrainingMenuUi(
                                            id = masterDoc.id,

                                            title = master.title,
                                            url = master.url,
                                            parameterKey = master.parameterKey,
                                            incrementValue = master.incrementValue,
                                            description = master.description,
                                            step = master.step,
                                            guide = master.guide,
                                            tags = master.tags,

                                            triggerText = state.triggerText,
                                            practiceText = state.practiceText,
                                            habit = state.habit,
                                            orderIndex = state.orderIndex,
                                            streakCount = state.streakCount,
                                            lastCompletedDate = state.lastCompletedDate,
                                            parentRoutineId = state.parentRoutineId

                                        )

                                        menuMap[masterDoc.id] = menu
                                    }
                                }


                                // 仮置きしたルーティンリストから
                                routineList.forEach { routine ->
                                    // ルーティンリストとしてまとめたもののidと、Firestoreから取得したルーティンドキュメントを照合
                                    val routineDoc = routineSnapshots.documents.first{ it.id == routine.routineId}

                                    // ルーティンドキュメントからidだけのリストを作成
                                    val menuIds = routineDoc.get("menuIds") as? List<String> ?:emptyList()

                                    // 作成したリストから、習慣化のみまとめたマップのidと照合し、子要素に追加
//                            menuIds.forEach { id ->
//                                val menu = menuMap[id]
//                                if (menu != null) {
//                                    routine.menuItems.add(
//                                        HabitItem.menuItem(id,menu, routine.routineId)
//                                    )
//                                }
//                            }

                                    val children = menuIds.mapNotNull { id ->
                                        val menu = menuMap[id]
                                        if (menu != null) {
                                            HabitItem.menuItem(id, menu, routine.routineId)
                                        } else null
                                    }.toMutableList()

                                    children.sortBy { it.data.orderIndex }

                                    routine.menuItems.clear()
                                    routine.menuItems.addAll(children)

                                    // 表示用リストに、子要素をセットしたルーティンを追加
                                    habitItems.add(routine)


                                    // 子要素を今日達成しているかどうかの判定
                                    val today = LocalDate.now().toString()

                                    val isCompletedToday = children.isNotEmpty() &&
                                            children.all { it.data.lastCompletedDate == today }

                                    routine.isCompletedToday = isCompletedToday

                                }

                                // ルーティンに属していない単体メニューだけを追加（習慣化のみまとめたマップから照合）
                                menuMap.forEach { id, menu ->
                                    if (!routineMenuSet.contains(id)) {
                                        habitItems.add(
                                            HabitItem.menuItem(id,menu, null)
                                        )
                                    }
                                }

                                // 第1階層のリストをルーティンと単体メニューを含めてソートする
                                habitItems.sortBy {
                                    when(it) {
                                        is HabitItem.routineItem -> it.orderIndex
                                        is HabitItem.menuItem -> it.data.orderIndex
                                    }
                                }


                                // ログを出してルーティンの構造を確認する
//                        habitItems.forEach { item ->
//                            when(item) {
//                                is HabitItem.routineItem -> {
////                                    Log.i("表示ルーティン", "ルーティン：${item.routineId}")
//                                    item.menuItems.forEach { item ->
//                                        Log.i("表示ルーティン内メニュー", "   |_メニュー：${item.data.title}")
//
//                                    }
//                                }
//                                is HabitItem.menuItem -> {
//                                    Log.i("表示メニュー", "メニュー：${item.data.title}")
//                                }
//                            }
//                        }
                                // 何も表示するものがなければ
                                if (habitItems.isEmpty()) {
                                    // 「追加されていません」のテキスト表示
                                    tvNoHabit.visibility = View.VISIBLE

//                            // 編集のFABボタンを非表示
//                            fabMain.visibility = View.GONE
//                            fabRoutine.visibility = View.GONE

                                } else {
                                    // 編集のFABボタンを表示
                                    fabMain.visibility = View.VISIBLE
                                    fabRoutine.visibility = View.VISIBLE
                                }
                                adapter.updateList(habitItems)



                            }



//                        // 単体メニューの型に変換する
//                        menuSnapshots.forEach { menuDoc ->
//                            val menu = menuDoc.toObject(TrainingMenuUi::class.java)
//
//
//
//                            // 取得した単体メニューのidに単体メニューの中身を対応させる
//                            menuMap[menuDoc.id] = menu
//                        }



                    }
            }


            .addOnFailureListener { exception ->
                Log.i("tag", "習慣リストの取得に失敗しました：${exception.message}")
            }

    }

    // Firestoreに並び順を更新する処理
    private fun updateOrderInFirestore() {
        val user = auth.currentUser ?:return
        val batch = db.batch()

        // ネスト構造を考慮した現在の順番のリストを取得
        val rootItems = adapter.getRootItems()
//        val currentList = adapter.currentList()

        // 現在の表示順のリストのアイテムごとに繰り返し処理
        rootItems.forEachIndexed { index, item ->
//        currentList.forEachIndexed { index, item ->
            when(item) {
                // 単体メニューなら
                is HabitItem.menuItem -> {
//                      pair.second.orderIndex = index
//                      Log.i("並び替え後に取得したリスト","${index}番目の項目は${pair.first}の${pair.second}です")

                    item.data.orderIndex = index
                    Log.i("並び替え後に取得したリスト","${index}番目の項目は${item.menuId}の${item.data.title}です")

                    val docRef = db.collection("results")
                        .document(user.uid)
                        .collection("trainingMenus")
                        .document(item.menuId)

                    // 順番は現在の順番・親はなしで更新
//                    batch.update(docRef, "orderIndex", index)
                    batch.update(docRef, mapOf(
                        "orderIndex" to index,
                        "parentRoutineId" to null
                    ))
                }

                // ルーティンなら
                is HabitItem.routineItem -> {
                    val docRef = db.collection("results")
                        .document(user.uid)
                        .collection("routines")
                        .document(item.routineId)

                    // ルーティンの順番を保存
                    batch.update(docRef, "orderIndex", index)
                    Log.i("並び替え後に取得したリスト","${index}番目の項目は${item.routineId}の${item.name}です")


                    item.menuItems.forEachIndexed { childIndex, menu ->

                        val docRef = db.collection("results")
                            .document(user.uid)
                            .collection("trainingMenus")
                            .document(menu.menuId)

                        // ルーティン内メニューの順番と親を保存
                        batch.update(
                            docRef, mapOf(
                                "orderIndex" to childIndex,
                                "parentRoutineId" to item.routineId
                            )
                        )
                    }
                }
            }


        }

            batch.commit()
                .addOnSuccessListener {
                    Log.i("順番の更新", "順番の更新完了")
                }
                .addOnFailureListener {
                    Log.i("順番の更新", "順番の更新失敗")

                }
    }

    // 一括でルーティンに追加する処理で、最初のダイアログを出す
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showRoutineSelectedDialog(selectedIds: List<String>) {
        val user = auth.currentUser ?: return


        val routineRef = db.collection("results")
            .document(user.uid)
            .collection("routines")

        routineRef.get().addOnSuccessListener { snapshots ->
            val routineDoc = snapshots.documents
            val options = mutableListOf<String>()
            val optionIds = mutableListOf<String>()

            // 選択肢に現在のルーティンのリスト
            routineDoc.forEach {
                options.add(it.getString("name") ?: "無名のルーティン")
                optionIds.add((it.id))
            }

            // 選択肢の最後に新規ルーティン追加
            options.add("＋ 新規ルーティンを作成")

            // 配列に変換
            val optionArray = options.toTypedArray()

            // ダイアログ作成
            AlertDialog.Builder(requireContext())
                .setTitle("ルーティンを管理")
                .setItems(optionArray) { _, which ->
                    when(which) {
                        optionArray.size -1 -> {
                            // 新規ルーティン追加
                            showCreateRoutineDialog(selectedIds)
                        }
                        else -> {
                            // 既存ルーティンに追加
                            val selectedRoutineId = optionIds[which]
                            updateRoutineWithMenus(selectedIds, selectedRoutineId)
                        }
                    }
                }
                .show()

        }
    }

    // 新規ルーティン作成のダイアログ
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showCreateRoutineDialog(selectedIds: List<String>) {
        val editText = EditText(requireContext()).apply {
            hint = "ルーティン名を入力"
        }



        val dialog = AlertDialog.Builder(requireContext())
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
            createRoutineWithMenus(selectedIds, routineName)

            dialog.dismiss()

        }
//        AlertDialog.Builder(requireContext())
//            .setTitle("新しいルーティン名")
//            .setView(editText)
//            .setPositiveButton("作成") {_, _ ->
//                val routineName = editText.text.toString()
//                if (routineName.isNotBlank()) {
//                    createRoutineWithMenus(selectedIds, routineName)
//                } else {
//                    Toast.makeText(requireContext(), "ルーティン名を入れてください", Toast.LENGTH_LONG).show()
//
//                }
//            }
//            .setNegativeButton("キャンセル", null)
//            .show()
    }


    // 新規ルーティンを追加する処理
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createRoutineWithMenus(selectedIds: List<String>, routineName: String) {
        val user = auth.currentUser ?: return

        val routineRef = db.collection("results")
            .document(user.uid)
            .collection("routines")
            .document() // ←自動ID

        val menuCollection = db.collection("results")
            .document(user.uid)
            .collection("trainingMenus")

        val batch = db.batch()

        // ルーティンの作成
        batch.set(routineRef,mapOf(
            "name" to routineName,
            "menuIds" to selectedIds,
            "orderIndex" to 0
        ))



        // 子要素に親追加
        selectedIds.forEachIndexed { index, menuId ->
            val menuRef = menuCollection.document(menuId)
            batch.update(menuRef, mapOf(
                "parentRoutineId" to routineRef.id,
                "orderIndex" to index
            ))
        }

        batch.commit().addOnSuccessListener {
            Toast.makeText(requireContext(), "${routineName}という新しいルーティンを作成しました", Toast.LENGTH_LONG).show()
            loadHabit()
            toggleEditMode()
        }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "新しいルーティン作成に失敗しました", Toast.LENGTH_LONG).show()
            }

    }

    // 既存ルーティンを更新する処理
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateRoutineWithMenus(selectedIds: List<String>, selectedRoutineId: String) {

        val user = auth.currentUser ?: return

        val routineRef = db.collection("results")
            .document(user.uid)
            .collection("routines")
            .document(selectedRoutineId)

        val routineCollection = db.collection("results")
            .document(user.uid)
            .collection("routines")

        val menuCollection = db.collection("results")
            .document(user.uid)
            .collection("trainingMenus")


        // 選ばれたidのアイテムを取得
        menuCollection.whereIn(FieldPath.documentId(), selectedIds)
            .get()
            .addOnSuccessListener { snapshots ->
                val batch = db.batch()

                // 選ばれたアイテムの元親を習得
                snapshots.forEachIndexed { index, doc ->
                    val menuId = doc.id
                    val oldParentId = doc.getString("parentRoutineId")

                    // 移動先のルーティンと違う元親が存在するなら
                    if (!oldParentId.isNullOrEmpty() && oldParentId != selectedRoutineId) {
                        val oldParentRef = routineCollection.document(oldParentId)
                        batch.update(oldParentRef, "menuIds", FieldValue.arrayRemove(menuId))
                    }

                    // 新親→子id追加
                    val newRoutineRef = routineCollection.document(selectedRoutineId)
                    batch.update(newRoutineRef, "menuIds", FieldValue.arrayUnion(menuId))

                    // 子→親id追加
                    val menuRef = menuCollection.document(menuId)
                    batch.update(menuRef, mapOf(
                        "parentRoutineId" to selectedRoutineId,
                        "orderIndex" to index
                    ))
                }

                batch.commit().addOnSuccessListener {
                    Toast.makeText(requireContext(), "既存のルーティンに追加しました", Toast.LENGTH_LONG).show()
                    loadHabit()
                    toggleEditMode()

                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "既存のルーティンに追加に失敗しました", Toast.LENGTH_LONG).show()
                }
            }
    }


    // ルーティンから一括で達成処理
    @RequiresApi(Build.VERSION_CODES.O)
    private fun completeRoutine(routine: HabitItem.routineItem) {

        // ルーティン内が空なら処理を止める
        if (routine.menuItems.isEmpty()) {
            Toast.makeText(requireContext(), "このルーティン内のアイテムはありません。。", Toast.LENGTH_LONG).show()
            return
        }

        // 達成済みのものは処理しない
        val today = LocalDate.now().toString()
        val targets = routine.menuItems.filter{
            it.data.lastCompletedDate != today
        }

        if (targets.isEmpty()) {
            Toast.makeText(requireContext(), "このルーティン内のアイテムはすべて達成済みです。", Toast.LENGTH_LONG).show()
            return
        }

        // 現在の処理数を定義
        var completedCount = 0

        // 合計値のリストを定義
        val results = mutableListOf<TrainingRepository.TrainingResult>()


        targets.forEach { menuItem ->
            repository.completeTraining(
                menuId = menuItem.menuId,
                parameterKey = menuItem.data.parameterKey,
                incrementValue = menuItem.data.incrementValue,
                onSuccess = { result ->
                    results.add(result)
                    completedCount++

                    Log.i("渡ってきた結果", "${result.monsterName}")
                    Log.i("渡ってきた結果", "${result.popupColorRes}")



                    // すべての達成処理が終わったら完了
                    if (completedCount == targets.size) {
                        loadHabit()

                        // 集約処理
                        val aggregated = repository.aggregatedResult(results)

                        // 順番に処理
                        repository.showPopupQueue(requireActivity(), aggregated)

//                        Toast.makeText(requireContext(), "ルーティン内の${targets.size}個のアイテムを達成しました。", Toast.LENGTH_LONG).show()
                    }


                },
                onFailure = {}
            )
        }
    }





    // 一括で達成処理を行う処理 → いらない？
    @RequiresApi(Build.VERSION_CODES.O)
    private fun completeSelectedItems() {
        // チェックされていないときには処理を止める
        val selectedIds = adapter.getSelectedIds()
        if(selectedIds.isEmpty()) {
            Toast.makeText(requireContext(), "達成したい項目をチェックしてください", Toast.LENGTH_LONG).show()
            return
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
//        val user = auth.currentUser?: return

        // 選択されたアイテムを繰り返し処理する
        selectedIds.forEach { id ->
            val menu = adapter.getItemById(id)



            repository.completeTraining(
                menuId = id,
                parameterKey = menu.parameterKey,
                incrementValue = menu.incrementValue,
                onSuccess = {},
                onFailure = {}
            )

        }
        Toast.makeText(requireContext(), "おめでとうございます。選択されたトレーニングはすべて達成されました", Toast.LENGTH_SHORT).show()


        // 編集モードを解除し、選択をクリアする
        adapter.exitEditMode()
        // 編集モードを抜けたらアイコンを戻す
        fabMain.setIconResource(R.drawable.ic_edit)

        hideHFab(fabRoutine)
//        hideHFab(fabDone)

        // パラメーターの画面に置き換え
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()

    }

    // 削除の通知処理
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDeleteDialog(menuId: String) {
        val dialog = ConfirmDialogFragment.newInstance(
            message = "このトレーニングを習慣から外しますか？",
            requestCode = 0
        ) {
            removeHabit(menuId)
        }

        dialog.show(parentFragmentManager, "deleteDialog")
    }

    // 削除を行う処理
    @RequiresApi(Build.VERSION_CODES.O)
    private fun removeHabit(menuId: String) {
        val user = auth.currentUser ?:return
        val batch = db.batch()

        val menuRef = db.collection("results")
            .document(user.uid)
            .collection("trainingMenus")
            .document(menuId)

        val routineRef = db.collection("results")
            .document(user.uid)
            .collection("routines")

        // 習慣化から外す & 子→親のid削除
        batch.update(menuRef,mapOf(
            "habit" to false,
            "parentRoutineId" to null
        ))

        // 親→子のid削除
        routineRef.get().addOnSuccessListener { snapshots ->
            snapshots.documents.forEach { doc ->
                batch.update(
                    doc.reference,
                    "menuIds",
                    FieldValue.arrayRemove(menuId)
                )
            }

            batch.commit()
                .addOnSuccessListener {
                    loadHabit()
                }
        }
    }

    // 一括削除を行う処理 → いらない？
    @RequiresApi(Build.VERSION_CODES.O)
    private fun deleteSelectedItems() {
        val user = auth.currentUser?: return

        // チェックされたアイテムを取得
        val selectedIds = adapter.getSelectedIds()
        if(selectedIds.isEmpty()) return

        // バッチ処理
        val batch = db.batch()
        val menuCollection = db.collection("results")
            .document(user.uid)
            .collection("trainingMenus")

        val routineCollection = db.collection("results")
            .document(user.uid)
            .collection("routines")

        adapter.currentList().forEach { item ->
            when (item) {
                is HabitItem.menuItem -> {
                    if (selectedIds.contains(item.menuId)) {
                        val docRef = menuCollection.document(item.menuId)
                        batch.update(docRef, "habit", false)
                    }
                }

                is HabitItem.routineItem -> {
                    if (selectedIds.contains(item.routineId)) {
                        val docRef = routineCollection.document(item.routineId)
                        batch.delete(docRef)
                    }
                }
            }
        }

        batch.commit()
            .addOnSuccessListener {
                Log.e("tag", "一括削除に成功")
                Toast.makeText(requireContext(), "選択されたトレーニングを習慣から外しました",
                    Toast.LENGTH_LONG).show()

                adapter.exitEditMode()

                // 編集モードを抜けたらアイコンを戻す
                fabMain.setIconResource(R.drawable.ic_edit)

                hideHFab(fabRoutine)
//                hideHFab(fabDone)

                // 念のため再読み込み
                loadHabit()
            }
            .addOnFailureListener { exception ->
                Log.e("tag", "一括削除に失敗：${exception.message}")
            }
    }
}