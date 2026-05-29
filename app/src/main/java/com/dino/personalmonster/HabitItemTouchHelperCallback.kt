package com.dino.personalmonster

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


class HabitItemTouchHelperCallback(
    private val adapter: HabitAdapter,
    private val onDragFinished: () -> Unit
): ItemTouchHelper.Callback() {

    // 長押しの処理は無効
    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    // スワイプの処理は無効
    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = if (adapter.isEditMode == true) ItemTouchHelper.UP or ItemTouchHelper.DOWN else 0
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val from = viewHolder.bindingAdapterPosition
        val to = target.bindingAdapterPosition

        if (from == RecyclerView.NO_POSITION ||
            to == RecyclerView.NO_POSITION) {
            return false
        }

        // 掴んでいるのアイテムを取得
        val fromItem = adapter.currentList()[from]
        // 移動先のアイテムを取得
        val toItem = adapter.currentList()[to]


        // ①掴んでいるアイテムが子メニューなら
        if (fromItem is HabitItem.menuItem && fromItem.parentRoutineId != null) {

            // 移動先と掴んでいるアイテムが同じ親なら移動可能（子同士）
            if (toItem is HabitItem.menuItem && fromItem.parentRoutineId == toItem.parentRoutineId) {

                adapter.moveItem(from, to)
                return true
            } else {
                // それ以外なら移動不可（子のルーティンまたぎ防止）
                return false
            }
        }

        // ②掴んでいるアイテムが親or単体メニューで
        // 移動先のアイテムが子メニューなら禁止
        if (toItem is HabitItem.menuItem && toItem.parentRoutineId != null) {
            return false
        }

        // それ以外なら移動OK（親同士 or 単体メニュー同士のみ動かせる）
        adapter.moveItem(from, to)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        // ドラッグ終了時の処理をコールバックで呼ぶ
        onDragFinished()
    }

}

