package com.example.personaltasks.ui

interface OnTaskClickListener {
    fun onTaskClick(position: Int)
    fun onRemoveTaskMenuItemClick(position: Int)
    fun onEditTaskMenuItemClick(position: Int)
}