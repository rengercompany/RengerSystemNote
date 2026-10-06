package com.renger.system.note.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.renger.system.note.ui.NoteViewModel
import com.renger.system.note.ui.screens.NoteEditScreen
import com.renger.system.note.ui.screens.NotesListScreen

@Composable
fun NavGraph() {
    val nav = rememberNavController()
    val vm: NoteViewModel = viewModel()

    NavHost(navController = nav, startDestination = "list") {
        composable("list") { NotesListScreen(nav, vm) }
        composable(
            "edit/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt("id") ?: 0
            NoteEditScreen(nav, vm, id)
        }
    }
}