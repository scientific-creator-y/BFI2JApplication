package com.dino.personalmonster

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


sealed class HabitItem {
    data class menuItem(
        val menuId: String,
        val data: TrainingMenu,
        val parentRoutineId: String? = null
    ): HabitItem()

    data class routineItem(
        val routineId: String,
        val name: String,
        val orderIndex: Int,
        var triggerText: String = "",
        val menuItems: MutableList<menuItem> = mutableListOf(),
        var isCompletedToday: Boolean = false,
        var expanded: Boolean = true


    ): HabitItem()
}
class HabitAdapter(
    private var allItems: MutableList<HabitItem>,
    private val showTriggerText: Boolean,
    private val onMenuClick: (String, TrainingMenu) -> Unit,
    private val onRoutineClick: (String) -> Unit,
    private val onMenuCompleteClick: (String, TrainingMenu) -> Unit,
    private val onRoutineCompleteClick: (HabitItem.routineItem) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_MENU = 0
        private const val TYPE_ROUTINE = 1
    }

    private var filteredItems: MutableList<HabitItem> = allItems.toMutableList()

    // 編集モード、最初はfalse
    var isEditMode: Boolean = false

    // 選択されたIDを保持する
    private val selectedIds = mutableSetOf<String>()


    // タッチヘルパーを取得
    lateinit var touchHelper: ItemTouchHelper


    class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTrigger: TextView = view.findViewById(R.id.tvTrigger)
        val tvTitle: TextView = view.findViewById(R.id.tvTrainingTitle)
        val btComplete: Button = view.findViewById(R.id.btnComplete)
        val cbTrainingCheckBox: CheckBox = view.findViewById(R.id.cbTrainingCheckBox)

        // ドラッグハンドルを取得
        val dragHandle: View = view.findViewById(R.id.dragHandle)

        // 削除アイコンを追加
        val btnDelete : View = view.findViewById(R.id.btnDelete)


    }

    class RoutineViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val tvRoutineTitle: TextView= view.findViewById(R.id.tvRoutineTitle)
//        val cbRoutineCheckBox: CheckBox = view.findViewById(R.id.cbRoutineCheckBox)
        val dragHandle: View = view.findViewById(R.id.dragHandle)
        val btnComplete: Button = view.findViewById(R.id.btnComplete)
        // 矢印を取得
        val ivArrow: View = view.findViewById(R.id.ivArrow)

        val tvRoutineTrigger: TextView = view.findViewById(R.id.tvRoutineTrigger)
    }

    override fun getItemViewType(position: Int): Int {
        return when(filteredItems[position]) {
            is HabitItem.menuItem -> TYPE_MENU
            is HabitItem.routineItem -> TYPE_ROUTINE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == TYPE_MENU) {
            val view = inflater.inflate(R.layout.training_item, parent, false)
            MenuViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.routine_item, parent,false)
            RoutineViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(val item = filteredItems[position]) {
            is HabitItem.menuItem -> bindMenu(holder as MenuViewHolder ,item)
            is HabitItem.routineItem -> bindRoutine(holder as RoutineViewHolder, item)
        }
    }

    private fun bindMenu(holder: MenuViewHolder, item: HabitItem.menuItem) {

        // 日付と達成フラグ取得
        val today = getTodayString()

        // Pairで渡すのをやめて、HabitItemのmenuItemが持つdataプロパティで指定
//        val isCompletedToday = menu.second.lastCompletedDate == today
        val isCompletedToday = item.data.lastCompletedDate == today

        // 習慣トリガーが設定されていれば表示
            // 再利用対策でリセット
        holder.tvTrigger.visibility = View.GONE
        holder.tvTrigger.text = ""

        if (showTriggerText && !item.data.triggerText.isNullOrBlank()) {
            holder.tvTrigger.visibility = View.VISIBLE
            holder.tvTrigger.text = "[${item.data.triggerText}]"
        } else {
            holder.tvTrigger.visibility = View.GONE
        }



        // 習慣のタイトルのテキストのUIと紐づける
        holder.tvTitle.text = item.data.title

        // ルーティン内ならインデント
        if (item.parentRoutineId != null) {
            holder.itemView.setPadding(80, 16,16,16)
        } else {
            holder.itemView.setPadding(16, 16,16,16)

        }

        // 編集モードと達成フラグに応じてチェックボックスと、トリガーの表示切り替え
        holder.cbTrainingCheckBox.visibility = if(isEditMode) View.VISIBLE else View.GONE
//        if (isCompletedToday) {
//            holder.cbTrainingCheckBox.isEnabled = false
//            holder.cbTrainingCheckBox.isChecked = false
//            selectedIds.remove(item.menuId)
//        } else {
//            holder.cbTrainingCheckBox.isEnabled = true
//        }
        holder.tvTrigger.visibility = if(isEditMode) View.GONE else View.VISIBLE
        holder.cbTrainingCheckBox.setOnCheckedChangeListener(null) // 古いリスナーを解除

        // リサイクラービューがスクロールしてもチェックされているビューにチェックが入るように維持する
        holder.cbTrainingCheckBox.isChecked = selectedIds.contains(item.menuId)

        // チェックされたときの処理
        holder.cbTrainingCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) selectedIds.add(item.menuId) else selectedIds.remove(item.menuId)
        }

        // ドラッグハンドルの表示切り替え
        holder.dragHandle.visibility = if (isEditMode) View.VISIBLE else View.GONE

        // 達成ボタンの表示切り替え
        holder.btComplete.visibility = if (isEditMode) View.GONE else View.VISIBLE

        // 削除アイコンの表示切り替え
        holder.btnDelete.visibility = if (isEditMode) View.VISIBLE else View.GONE

        // 通常タップの処理
        holder.itemView.setOnClickListener {
            // 編集モードのときは無効
            if(!isEditMode) onMenuClick(item.menuId, item.data)
        }

        // 達成ボタンUIの処理・達成済みの消込処理
        if (isCompletedToday) {
            holder.btComplete.isEnabled = false
            holder.btComplete.text = "達成済み"
            holder.tvTitle.paint.isStrikeThruText = true
        } else {
            holder.btComplete.isEnabled = true
            holder.btComplete.text = "達成！"
            holder.tvTitle.paint.isStrikeThruText = false
        }

        // 完了ボタンのクリック処理をコールバック
        holder.btComplete.setOnClickListener {
            onMenuCompleteClick(item.menuId, item.data)
        }

        // ハンドルタッチの処理
        holder.dragHandle.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                touchHelper.startDrag(holder)
            }
            false
        }

        // 削除アイコンのクリック処理をコールバック
        holder.btnDelete.setOnClickListener {
            onDeleteClick(item.menuId)
        }

    }

    private fun bindRoutine(holder: RoutineViewHolder ,item: HabitItem.routineItem) {
        holder.tvRoutineTitle.text = item.name

        // 再利用に備えてインデントを上書きする
        holder.itemView.setPadding(16,16,16,16)

//        holder.cbRoutineCheckBox.visibility = if (isEditMode) View.VISIBLE else View.GONE
        holder.dragHandle.visibility = if (isEditMode) View.VISIBLE else View.GONE

//        holder.cbRoutineCheckBox.isChecked = selectedIds.contains(item.routineId)
//        holder.cbRoutineCheckBox.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) selectedIds.add(item.routineId) else selectedIds.remove(item.routineId)
//        }
        holder.dragHandle.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                touchHelper.startDrag(holder)
            }
            false
        }



        // ハンドルがタップされたときに展開する処理
        holder.ivArrow.setOnClickListener {
            // 展開フラグを切り替え
            item.expanded = !item.expanded

            // ビルドし直す
            filteredItems = buildDisplayList(allItems)
            notifyDataSetChanged()
        }
//        Log.i("ルーティンの展開", "今の展開状態は、${item.expanded}")

        // 展開状態に酔って矢印のUIを回転
        if (item.expanded) {
            holder.ivArrow.rotation = 90f
        } else {
            holder.ivArrow.rotation = 0f
        }

        // 習慣トリガーが設定されていれば表示
            // 再利用対策でリセット
        holder.tvRoutineTrigger.visibility = View.GONE
        holder.tvRoutineTrigger.text = ""

        if (showTriggerText && !item.triggerText.isNullOrBlank()) {
            holder.tvRoutineTrigger.visibility = View.VISIBLE
            holder.tvRoutineTrigger.text = "[${item.triggerText}]"
        } else {
            holder.tvRoutineTrigger.visibility = View.GONE

        }



        // ルーティンが押されたときに遷移する処理
//        holder.itemView.setOnClickListener {
        holder.tvRoutineTitle.setOnClickListener {
            if (!isEditMode) onRoutineClick(item.routineId)
        }

        // ルーティンの達成が押されたときに一括達成する処理
        holder.btnComplete.visibility = if (isEditMode) View.GONE else View.VISIBLE
        holder.btnComplete.setOnClickListener {
            onRoutineCompleteClick(item)
        }

        // ルーティン内がすべて達成済みなら消込処理
        if (item.isCompletedToday) {
            holder.btnComplete.isEnabled = false
            holder.btnComplete.text = "すべて達成"
            holder.tvRoutineTitle.paint.isStrikeThruText = true
        }


    }

    // 基本処理
    override fun getItemCount(): Int = filteredItems.size


    // リサイクラービューを更新する処理
    fun updateList(newList: List<HabitItem>) {
        // 元データを更新
        allItems = newList.toMutableList()

        // フラット化したリストでリサイクラービューを更新
        filteredItems = buildDisplayList(newList)
        notifyDataSetChanged()
    }

    // 階層的なデータをフラット化する処理
    private fun buildDisplayList(items: List<HabitItem>) : MutableList<HabitItem> {
        val displayList = mutableListOf<HabitItem>()
        items.forEach { item ->
            when(item) {
                is HabitItem.routineItem -> {
                    displayList.add(item)

                    if (item.expanded) {
                        displayList.addAll(item.menuItems)
                    }
                }
                is HabitItem.menuItem -> {
                    displayList.add(item)
                }
            }
        }

        return displayList
    }



    // 編集モード関連でFragmentから使う処理
    fun getSelectedIds(): List<String> {
        return selectedIds.toList()
    }
    fun clearSelection() {
        selectedIds.clear()
        notifyDataSetChanged()
    }
    fun enterEditMode() {
        isEditMode = true
        notifyDataSetChanged()
//        Log.i("tag", "編集モードに入りました。チェックされている項目のIDは${selectedIds}です")
    }

    fun exitEditMode() {
        isEditMode = false
        clearSelection()
//        Log.i("tag", "編集モードから出ました。チェックされている項目のIDは${selectedIds}です")

    }

    fun getItemById(id: String): TrainingMenu {
//        return allItems.first { it.first == id }.second
        val item = allItems.first { it is HabitItem.menuItem && it.menuId == id }
            return (item as HabitItem.menuItem).data
    }

    fun currentList(): List<HabitItem> {
        return filteredItems.toList()
    }

    // ネスト構造を考慮したデータを返す処理
    fun getRootItems(): List<HabitItem> {
        return allItems
    }



    // ハンドルタッチで行を動かす処理
    fun moveItem(from: Int, to: Int) {
        val fromItem = filteredItems[from]
        val toItem = filteredItems[to]


        // 掴んでいるアイテムが子要素の場合
        if (fromItem is HabitItem.menuItem && fromItem.parentRoutineId != null) {
            // 掴んでいるアイテムの親を探す
            val parent = allItems.find {
                it is HabitItem.routineItem && it.routineId == fromItem.parentRoutineId
            } as? HabitItem.routineItem ?:return

            // 掴んでいるアイテムの番号と、同じ親内の移動先のアイテムの番号
            val fromIndex = parent.menuItems.indexOf(fromItem)
            val toIndex = parent.menuItems.indexOf(toItem)

            if (fromIndex == -1 || toIndex == -1) return

            val item = parent.menuItems.removeAt(fromIndex)
            parent.menuItems.add(toIndex, item)

            filteredItems = buildDisplayList(allItems)
            notifyDataSetChanged()
            return
        }

        // 掴んでいるアイテムが親の場合
        val fromIndex = allItems.indexOf(fromItem)
        val toIndex = allItems.indexOf(toItem)

        if (fromIndex == -1 || toIndex == -1) return


        // 表示用リストではなく元リストを更新
        val item = allItems.removeAt(fromIndex)
        allItems.add(toIndex, item)

        filteredItems = buildDisplayList(allItems)
//        notifyItemMoved(from, to)
        notifyDataSetChanged()
    }





    // 日付を取得する処理
    private fun getTodayString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

}