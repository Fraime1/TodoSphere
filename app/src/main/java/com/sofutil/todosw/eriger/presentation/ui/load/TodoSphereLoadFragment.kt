package com.sofutil.todosw.eriger.presentation.ui.load

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.sofutil.todosw.MainActivity
import com.sofutil.todosw.R
import com.sofutil.todosw.databinding.FragmentLoadTodoSphereBinding
import com.sofutil.todosw.eriger.data.shar.TodoSphereSharedPreference
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class TodoSphereLoadFragment : Fragment(R.layout.fragment_load_todo_sphere) {
    private lateinit var todoSphereLoadBinding: FragmentLoadTodoSphereBinding

    private val todoSphereLoadViewModel by viewModel<TodoSphereLoadViewModel>()

    private val todoSphereSharedPreference by inject<TodoSphereSharedPreference>()

    private var todoSphereUrl = ""

    private val todoSphereRequestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            todoSphereNavigateToSuccess(todoSphereUrl)
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                todoSphereSharedPreference.todoSphereNotificationRequest =
                    (System.currentTimeMillis() / 1000) + 259200
                todoSphereNavigateToSuccess(todoSphereUrl)
            } else {
                todoSphereNavigateToSuccess(todoSphereUrl)
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        todoSphereLoadBinding = FragmentLoadTodoSphereBinding.bind(view)

        todoSphereLoadBinding.todoSphereGrandButton.setOnClickListener {
            val todoSpherePermission = Manifest.permission.POST_NOTIFICATIONS
            todoSphereRequestNotificationPermission.launch(todoSpherePermission)
            todoSphereSharedPreference.todoSphereNotificationRequestedBefore = true
        }

        todoSphereLoadBinding.todoSphereSkipButton.setOnClickListener {
            todoSphereSharedPreference.todoSphereNotificationRequest =
                (System.currentTimeMillis() / 1000) + 259200
            todoSphereNavigateToSuccess(todoSphereUrl)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                todoSphereLoadViewModel.todoSphereHomeScreenState.collect {
                    when (it) {
                        is TodoSphereLoadViewModel.TodoSphereHomeScreenState.TodoSphereLoading -> {

                        }

                        is TodoSphereLoadViewModel.TodoSphereHomeScreenState.TodoSphereError -> {
                            requireActivity().startActivity(
                                Intent(
                                    requireContext(),
                                    MainActivity::class.java
                                )
                            )
                            requireActivity().finish()
                        }

                        is TodoSphereLoadViewModel.TodoSphereHomeScreenState.TodoSphereSuccess -> {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                                val todoSpherePermission = Manifest.permission.POST_NOTIFICATIONS
                                val todoSpherePermissionRequestedBefore = todoSphereSharedPreference.todoSphereNotificationRequestedBefore

                                if (ContextCompat.checkSelfPermission(requireContext(), todoSpherePermission) == PackageManager.PERMISSION_GRANTED) {
                                    todoSphereNavigateToSuccess(it.data)
                                } else if (!todoSpherePermissionRequestedBefore && (System.currentTimeMillis() / 1000 > todoSphereSharedPreference.todoSphereNotificationRequest)) {
                                    // первый раз — показываем UI для запроса
                                    todoSphereLoadBinding.todoSphereNotiGroup.visibility = View.VISIBLE
                                    todoSphereLoadBinding.todoSphereLoadingGroup.visibility = View.GONE
                                    todoSphereUrl = it.data
                                } else if (shouldShowRequestPermissionRationale(todoSpherePermission)) {
                                    // временный отказ — через 3 дня можно показать
                                    if (System.currentTimeMillis() / 1000 > todoSphereSharedPreference.todoSphereNotificationRequest) {
                                        todoSphereLoadBinding.todoSphereNotiGroup.visibility = View.VISIBLE
                                        todoSphereLoadBinding.todoSphereLoadingGroup.visibility = View.GONE
                                        todoSphereUrl = it.data
                                    } else {
                                        todoSphereNavigateToSuccess(it.data)
                                    }
                                } else {
                                    // навсегда отклонено — просто пропускаем
                                    todoSphereNavigateToSuccess(it.data)
                                }
                            } else {
                                todoSphereNavigateToSuccess(it.data)
                            }
                        }

                        TodoSphereLoadViewModel.TodoSphereHomeScreenState.TodoSphereNotInternet -> {
                            todoSphereLoadBinding.todoSphereLoadConnectionStateText.visibility = View.VISIBLE
                            todoSphereLoadBinding.todoSphereLoadingGroup.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }


    private fun todoSphereNavigateToSuccess(data: String) {
        findNavController().navigate(
            R.id.action_todoSphereLoadFragment_to_todoSphereV,
            bundleOf(TODO_SPHERE_D to data)
        )
    }

    companion object {
        const val TODO_SPHERE_D = "todoSphereData"
    }
}