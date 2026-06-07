package com.dino.personalmonster

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TrainingAdapter(
    private var items: List<TrainingMenuUi>,
    private val onClick: (String, TrainingMenuUi) -> Unit
): RecyclerView.Adapter<TrainingAdapter.ViewHolder>() {

    private var filteredItems: MutableList<TrainingMenuUi> = items.toMutableList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTrainingTitle)
        val tvParameterIcon: TextView = view.findViewById(R.id.tvParameterIcon)
        val tvHabitIcon: TextView = view.findViewById(R.id.tvHabitIcon)

//        val btnComplete: Button = view.findViewById(R.id.btnComplete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.training_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredItems[position]
        holder.tvTitle.text = item.title

        when(item.parameterKey) {
            "social" -> {
                holder.tvParameterIcon.text = ParameterType.SOCIAL.label
                holder.tvParameterIcon.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_ic_social)
            }
            "harmony" -> {
                holder.tvParameterIcon.text = ParameterType.HARMONY.label
                holder.tvParameterIcon.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_ic_harmony)
            }
            "will" -> {
                holder.tvParameterIcon.text = ParameterType.WILL.label
                holder.tvParameterIcon.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_ic_will)
            }
            "mental" -> {
                holder.tvParameterIcon.text = ParameterType.MENTAL.label
                holder.tvParameterIcon.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_ic_mental)
            }
            "explore" -> {
                holder.tvParameterIcon.text = ParameterType.EXPLORE.label
                holder.tvParameterIcon.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_ic_explore)
            }
            "HP" -> {
                holder.tvParameterIcon.text = ParameterType.HP.label
                holder.tvParameterIcon.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_ic_hp)
            }
        }

        if (item.habit) {
            holder.tvHabitIcon.visibility = View.VISIBLE
        } else {
            holder.tvHabitIcon.visibility = View.GONE
        }

        // アイテムが押されたら詳細に遷移
        holder.itemView.setOnClickListener {
            onClick(item.id, item)
        }

        // 達成ボタンは表示しない
//        holder.btnComplete.visibility = View.GONE

    }

    override fun getItemCount(): Int = filteredItems.size

    fun updateList(newItems: List<TrainingMenuUi>) {
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