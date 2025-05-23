package com.example.personaltasks.controller

import com.example.personaltasks.model.Task
import com.example.personaltasks.model.TaskDAO
import com.example.personaltasks.model.TaskSqlite
import com.example.personaltasks.ui.MainActivity

class MainController (mainActivity: MainActivity){
    private val taskDAO: TaskDAO = TaskSqlite(mainActivity)

    fun insertTask(task: Task) = taskDAO.createTask(task)
    fun getTask(id: Int) = taskDAO.retrieveTask(id)
    fun getTasks() = taskDAO.retrieveTasks()
    fun modifyTask(task: Task) = taskDAO.updateTask(task)
    fun removeTask(id: Int) = taskDAO.deleteTask(id)
}