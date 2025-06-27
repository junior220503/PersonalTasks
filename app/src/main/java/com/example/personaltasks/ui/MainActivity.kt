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
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), OnTaskClickListener {

    // View Binding para acessar os elementos da interface de forma segura e sem findViewById
    private val amb: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    // Lista mutável que guarda as tarefas atualmente exibidas
    private val taskList: MutableList<Task> = mutableListOf()

    // Adapter da RecyclerView que exibirá as tarefas, recebe a lista e listener de cliques
    private val taskAdapter: TaskAdapter by lazy {
        TaskAdapter(taskList, this)
    }

    // Lançador para receber resultados de atividades (como a tela de adicionar/editar tarefas)
    private lateinit var acResult: ActivityResultLauncher<Intent>

    // Controller responsável pela lógica de manipulação dos dados (inserção, remoção, edição)
    private val mainController: MainController by lazy {
        MainController(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(amb.root)

        // Configura a toolbar customizada e remove o título padrão
        setSupportActionBar(amb.toolbarIn.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Botão de adicionar nova tarefa na toolbar
        val addButton = findViewById<ImageView>(R.id.toolbar_icon)
        addButton.setOnClickListener {
            // Inicia a TaskActivity para adicionar uma nova tarefa, esperando resultado
            acResult.launch(Intent(this, TaskActivity::class.java))
        }

        // Registro do callback que recebe o resultado da TaskActivity
        acResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Recupera a tarefa enviada de volta da TaskActivity (compatível com versões antigas e novas)
                val task = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra(EXTRA_TASK, Task::class.java)
                } else {
                    result.data?.getParcelableExtra<Task>(EXTRA_TASK)
                }

                task?.let { receivedTask ->
                    // Verifica se a tarefa já existe na lista para editar ou adicionar nova
                    val position = taskList.indexOfFirst { it.id == receivedTask.id }
                    if (position != -1) {
                        // Atualiza tarefa existente
                        taskList[position] = receivedTask
                        mainController.modifyTask(receivedTask)
                    } else {
                        // Adiciona tarefa nova
                        taskList.add(receivedTask)
                        mainController.insertTask(receivedTask)
                    }
                    // Ordena e atualiza a lista exibida
                    makeTaskListOrdenated()
                }
            }
        }

        // Configura RecyclerView com adapter e layout manager vertical
        amb.taskRv.adapter = taskAdapter
        amb.taskRv.layoutManager = LinearLayoutManager(this)

        // Inicializa a lista de tarefas ao abrir a activity
        fillTaskList()
    }

    private fun fillTaskList() {
        Thread {
            // Busca e ordena as tarefas em uma thread separada para não travar a UI
            makeTaskListOrdenated()
        }.start()
    }

    private fun makeTaskListOrdenated() {
        // Obtém as tarefas do controller
        val tasks = mainController.getTasks()

        // Ordena as tarefas pela data limite (deadline), tratando possíveis erros de parsing
        val ordenatedTasks = tasks.sortedWith(compareBy {
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                dateFormat.parse(it.deadline)?: Date(Long.MAX_VALUE)
            } catch (e:Exception) {
                Date(Long.MAX_VALUE)
            }
        })

        // Atualiza a lista local com as tarefas ordenadas
        taskList.clear()
        taskList.addAll(ordenatedTasks)

        // Atualiza a interface na thread principal
        runOnUiThread {
            taskAdapter.notifyDataSetChanged()
        }
    }

    // Quando o usuário clica em uma tarefa na lista, abre a TaskActivity em modo de visualização
    override fun onTaskClick(position: Int) {
        Intent(this, TaskActivity::class.java).apply {
            putExtra(EXTRA_TASK, taskList[position])
            putExtra(EXTRA_VIEW_TASK, true)
            startActivity(this)
        }
    }

    // Quando o usuário seleciona remover uma tarefa via menu de contexto
    override fun onRemoveTaskMenuItemClick(position: Int) {
        mainController.removeTask(taskList[position].id!!)
        taskList.removeAt(position)
        taskAdapter.notifyItemRemoved(position)
        Toast.makeText(this, "Task removida!", Toast.LENGTH_SHORT).show()
    }

    // Quando o usuário seleciona editar uma tarefa via menu de contexto, abre a TaskActivity para edição
    override fun onEditTaskMenuItemClick(position: Int) {
        Intent(this, TaskActivity::class.java).apply {
            putExtra(EXTRA_TASK, taskList[position])
            acResult.launch(this)
        }
    }
}