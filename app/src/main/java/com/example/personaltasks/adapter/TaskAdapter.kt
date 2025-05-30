package com.example.personaltasks.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.personaltasks.R
import com.example.personaltasks.databinding.TileTaskBinding
import com.example.personaltasks.model.Task
import com.example.personaltasks.ui.OnTaskClickListener

class TaskAdapter (
    private val taskList: MutableList<Task>,
    private val onTaskClickListener: OnTaskClickListener
): RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {
    inner class TaskViewHolder(ttb: TileTaskBinding): RecyclerView.ViewHolder(ttb.root) {
        val titleTile: TextView = ttb.titleTv
        val descriptionTile: TextView = ttb.descriptionTv
        val deadlineTile: TextView = ttb.deadlineTv

        init {
            ttb.root.setOnCreateContextMenuListener {menu, v, menuInfo ->
                (onTaskClickListener as AppCompatActivity).menuInflater.inflate(R.menu.context_menu_main, menu)

                menu.findItem(R.id.detail_task_mi).setOnMenuItemClickListener {
                    onTaskClickListener.onTaskClick(adapterPosition)
                    true
                }
                menu.findItem(R.id.edit_task_mi).setOnMenuItemClickListener {
                    onTaskClickListener.onEditTaskMenuItemClick(adapterPosition)
                    true
                }
                menu.findItem(R.id.remove_task_mi).setOnMenuItemClickListener {
                    onTaskClickListener.onRemoveTaskMenuItemClick(adapterPosition)
                    true
                }
            }
            ttb.root.setOnClickListener {}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder = TaskViewHolder(
        TileTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun getItemCount(): Int = taskList.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        taskList[position].let { task ->
            with(holder) {
                titleTile.text = task.title
                descriptionTile.text = task.description
                deadlineTile.text = task.deadline
            }
        }
    }
}
