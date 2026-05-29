package com.dino.personalmonster

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class MonsterAdapter(
    // レベルを表示するか
    private val showLevel: Boolean,

    // クリックのコールバック
    private val onClick: (DisplayMonster) -> Unit,
    // 長押し用のコールバックを追加
    private val onLongClick: (DisplayMonster) -> Unit
) : ListAdapter<DisplayMonster, MonsterAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DisplayMonster>() {

            override fun areItemsTheSame(
                oldItem: DisplayMonster,
                newItem: DisplayMonster
            ): Boolean {
                // 「同一アイテムか？」
                return oldItem.slot == newItem.slot
            }

            override fun areContentsTheSame(
                oldItem: DisplayMonster,
                newItem: DisplayMonster
            ): Boolean {
                // 「中身が同じか？」
                return oldItem == newItem
            }
        }
    }


    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val ivMonster: ImageView = view.findViewById(R.id.ivMonster)
        val tvName: TextView = view.findViewById(R.id.tvMonsterName)
        val tvLevel: TextView = view.findViewById(R.id.tvMonsterLevel)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_monster, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val monster = monsterList[position]
        val monster = getItem(position)

        holder.ivMonster.setImageResource(monster.imageRes)
        holder.tvName.text = monster.name

        // レベルは「育成中モンスター」のリストのみ表示する
        if (showLevel) {
            holder.tvLevel.text = "（Lv.${(monster.level)}）"
        } else {
            holder.tvLevel.visibility = View.GONE
        }


        holder.itemView.setOnClickListener {
            onClick(monster)
        }

        // 長押しされたときの処理を追加
        holder.itemView.setOnLongClickListener {
            onLongClick(monster)
            true
        }
    }

//    override fun getItemCount(): Int = monsterList.size
}