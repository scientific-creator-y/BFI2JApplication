package com.dino.personalmonster

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TrainingAdapter(
    private var items: List<TrainingMenu>,
    private val onClick: (String, TrainingMenu) -> Unit
): RecyclerView.Adapter<TrainingAdapter.ViewHolder>() {

    private var filteredItems: MutableList<TrainingMenu> = items.toMutableList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTrainingTitle)
        val btnComplete: Button = view.findViewById(R.id.btnComplete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.training_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredItems[position]
        holder.tvTitle.text = item.title

        // アイテムが押されたら詳細に遷移
        holder.itemView.setOnClickListener {
            onClick(item.id, item)
        }

        // 達成ボタンは表示しない
        holder.btnComplete.visibility = View.GONE

    }

    override fun getItemCount(): Int = filteredItems.size

    fun updateList(newItems: List<TrainingMenu>) {
        items = newItems.toMutableList()
        filteredItems = items.toMutableList()
        notifyDataSetChanged()
    }

    fun filterTrainingList(query: String, category: String) {
        filteredItems = items.filter { menu ->
            val matchesQuery = menu.title.contains(query, ignoreCase = true)
            val matchesCategory = (category == "all" || menu.parameterKey == category)
            matchesQuery && matchesCategory
        }.toMutableList()

        notifyDataSetChanged()
    }


}