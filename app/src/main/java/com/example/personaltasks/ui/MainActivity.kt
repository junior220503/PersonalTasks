package com.example.personaltasks.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personaltasks.R
import com.example.personaltasks.adapter.TaskAdapter
import com.example.personaltasks.controller.MainController
import com.example.personaltasks.databinding.ActivityMainBinding
import com.example.personaltasks.model.Constants.EXTRA_TASK
import com.example.personaltasks.model.Constants.EXTRA_VIEW_TASK
import com.example.personaltasks.model.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), OnTaskClickListener {
    private val amb: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val taskList: MutableList<Task> = mutableListOf()

    private val taskAdapter: TaskAdapter by lazy {
        TaskAdapter(taskList, this)
    }

    private lateinit var acResult: ActivityResultLauncher<Intent>

    private val mainController: MainController by lazy {
        MainController(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(amb.root)

        setSupportActionBar(amb.toolbarIn.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val addButton = findViewById<ImageView>(R.id.toolbar_icon)
        addButton.setOnClickListener {
            acResult.launch(Intent(this, TaskActivity::class.java))
        }

        acResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra(EXTRA_TASK, Task::class.java)
                } else {
                    result.data?.getParcelableExtra<Task>(EXTRA_TASK)
                }

                task?.let {receivedTask ->
                    val position = taskList.indexOfFirst { it.id == receivedTask.id }
                    if (position != -1) {
                        taskList[position] = receivedTask
                        mainController.modifyTask(receivedTask)
                    }else {
                        taskList.add(receivedTask)
                        mainController.insertTask(receivedTask)
                    }
                    makeTaskListOrdenated()
                }
            }
        }
        amb.taskRv.adapter = taskAdapter
        amb.taskRv.layoutManager = LinearLayoutManager(this)

        fillTaskList()
    }

    private fun fillTaskList() {
        Thread {
            makeTaskListOrdenated()
        }.start()
    }

    private fun makeTaskListOrdenated() {
        val tasks = mainController.getTasks()
        val ordenatedTasks = tasks.sortedWith(compareBy {
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                dateFormat.parse(it.deadline)?: Date(Long.MAX_VALUE)
            } catch (e:Exception) {
                Date(Long.MAX_VALUE)
            }
        })
        taskList.clear()
        taskList.addAll(ordenatedTasks)

        runOnUiThread {
            taskAdapter.notifyDataSetChanged()
        }
    }

    override fun onTaskClick(position: Int) {
        Intent(this, TaskActivity::class.java).apply {
            putExtra(EXTRA_TASK, taskList[position])
            putExtra(EXTRA_VIEW_TASK, true)
            startActivity(this)
        }
    }

    override fun onRemoveTaskMenuItemClick(position: Int) {
        mainController.removeTask(taskList[position].id!!)
        taskList.removeAt(position)
        taskAdapter.notifyItemRemoved(position)
        Toast.makeText(this, "Task removida!", Toast.LENGTH_SHORT).show()
    }

    override fun onEditTaskMenuItemClick(position: Int) {
        Intent(this, TaskActivity::class.java).apply {
            putExtra(EXTRA_TASK, taskList[position])
            acResult.launch(this)
        }
    }
}