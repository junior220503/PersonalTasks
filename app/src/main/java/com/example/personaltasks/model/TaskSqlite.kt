package com.example.personaltasks.model

import android.content.ContentValues
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.personaltasks.R
import java.sql.SQLException

class TaskSqlite (context: Context): TaskDAO{
    companion object {
        private val TASK_DATABASE_FILE = "personalTasks"
        private val TASK_TABLE = "tasks"
        private val ID_COLUMN = "id"
        private val TITLE_COLUMN = "title"
        private val DESCRIPTION_COLUMN = "description"
        private val DEADLINE_COLUMN = "deadline"
        private val STATUS_COLUMN = "status"

        val CREATE_TASK_TABLE = "CREATE TABLE IF NOT EXISTS $TASK_TABLE (" +
                "$ID_COLUMN INTEGER NOT NULL PRIMARY KEY, " +
                "$TITLE_COLUMN TEXT NOT NULL, " +
                "$DESCRIPTION_COLUMN TEXT NOT NULL, " +
                "$DEADLINE_COLUMN TEXT NOT NULL," +
                "$STATUS_COLUMN TEXT NOT NULL );"
    }

    private val taskDatabase: SQLiteDatabase = context.openOrCreateDatabase(
        TASK_DATABASE_FILE,
        MODE_PRIVATE,
        null
    )

    init {
        try {
            taskDatabase.execSQL(CREATE_TASK_TABLE)
        } catch (se: SQLException) {
            Log.e(context.getString(R.string.app_name), se.message.toString())
        }
    }

    private fun Task.toContentValues() = ContentValues().apply {
        put(ID_COLUMN, id)
        put(TITLE_COLUMN, title)
        put(DESCRIPTION_COLUMN, description)
        put(DEADLINE_COLUMN, deadline)
        put(STATUS_COLUMN, status)
    }

    override fun createTask(task: Task): Long = taskDatabase.insert(TASK_TABLE, null, task.toContentValues())

    private fun Cursor.toTask() = Task(
        getInt(getColumnIndexOrThrow(ID_COLUMN)),
        getString(getColumnIndexOrThrow(TITLE_COLUMN)),
        getString(getColumnIndexOrThrow(DESCRIPTION_COLUMN)),
        getString(getColumnIndexOrThrow(DEADLINE_COLUMN)),
        getString(getColumnIndexOrThrow(STATUS_COLUMN))
    )

    override fun retrieveTask(id: Int): Task {
        val cursor = taskDatabase.query(
            true,
            TASK_TABLE,
            null,
            "$ID_COLUMN = ?",
            arrayOf(id.toString()),
            null,
            null,
            null,
            null
        )
        return if (cursor.moveToFirst()) cursor.toTask()
        else Task()
    }

    override fun retrieveTasks(): MutableList<Task> {
        val taskList: MutableList<Task> = mutableListOf()
        val cursor = taskDatabase.rawQuery("SELECT * FROM $TASK_TABLE;", null)
        while (cursor.moveToNext()) taskList.add(cursor.toTask())

        return taskList
    }

    override fun updateTask(task: Task): Int = taskDatabase.update(
        TASK_TABLE,
        task.toContentValues(),
        "$ID_COLUMN = ?",
        arrayOf(task.id.toString())
    )

    override fun deleteTask(id: Int): Int = taskDatabase.delete(
        TASK_TABLE,
        "$ID_COLUMN = ?",
        arrayOf(id.toString())
    )
}