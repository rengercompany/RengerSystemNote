package com.renger.system.note.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.renger.system.note.data.Folder
import com.renger.system.note.data.Note
import com.renger.system.note.ui.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotesListScreen(navController: NavController, viewModel: NoteViewModel) {
    val notes by viewModel.notes.collectAsState()
    val folders by viewModel.folders.collectAsState()

    var query by remember { mutableStateOf("") }
    var selectedFolderId by remember { mutableStateOf<Int?>(null) }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var showTagFilter by remember { mutableStateOf(false) }

    // Собираем все уникальные теги
    val allTags = remember(notes) {
        notes.flatMap { it.tagList() }.distinct().sorted()
    }

    // Фильтрация
    val filtered = notes.filter { note ->
        val matchQuery = query.isBlank() ||
                note.title.contains(query, true) ||
                note.content.contains(query, true) ||
                note.tags.contains(query, true)
        val matchFolder = selectedFolderId == null || note.folderId == selectedFolderId
        val matchTag = selectedTag == null || note.tagList().contains(selectedTag)
        matchQuery && matchFolder && matchTag
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("edit/0") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(20.dp))
            Text(
                "Renger System",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Text(
                "Мои заметки",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(16.dp))

            // Поиск
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Поиск по заметкам и тегам...") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(Modifier.height(12.dp))

            // Папки (горизонтальный скролл)
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Папки:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = { showFolderDialog = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.CreateNewFolder, null, modifier = Modifier.size(18.dp))
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                item {
                    FolderChip(
                        name = "Все",
                        selected = selectedFolderId == null,
                        onClick = { selectedFolderId = null }
                    )
                }
                items(folders, key = { it.id }) { folder ->
                    FolderChip(
                        name = folder.name,
                        selected = selectedFolderId == folder.id,
                        color = Color(folder.color),
                        onClick = {
                            selectedFolderId = if (selectedFolderId == folder.id) null else folder.id
                        }
                    )
                }
            }

            // Теги (фильтр)
            if (allTags.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Теги:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = { showTagFilter = !showTagFilter },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(if (showTagFilter) "Скрыть" else "Показать",
                            fontSize = 12.sp)
                    }
                }

                if (showTagFilter) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        item {
                            TagChip(
                                tag = "Все",
                                selected = selectedTag == null,
                                onClick = { selectedTag = null }
                            )
                        }
                        items(allTags) { tag ->
                            TagChip(
                                tag = tag,
                                selected = selectedTag == tag,
                                onClick = {
                                    selectedTag = if (selectedTag == tag) null else tag
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Список заметок
            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        when {
                            query.isNotBlank() -> "Ничего не найдено"
                            selectedFolderId != null -> "В этой папке нет заметок"
                            else -> "Пока нет заметок.\nНажмите + чтобы создать."
                        },
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    verticalItemSpacing = 12.dp,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            folder = folders.find { it.id == note.folderId },
                            onClick = { navController.navigate("edit/${note.id}") },
                            onDelete = { viewModel.delete(note) }
                        )
                    }
                }
            }
        }
    }

    // Диалог создания папки
    if (showFolderDialog) {
        var folderName by remember { mutableStateOf("") }
        var folderColor by remember { mutableStateOf(0xFF6C63FF.toInt()) }

        val palette = listOf(
            0xFF6C63FF.toInt(), 0xFFFF6584.toInt(),
            0xFF4CAF50.toInt(), 0xFFFFA726.toInt(), 0xFF29B6F6.toInt()
        )

        AlertDialog(
            onDismissRequest = { showFolderDialog = false },
            title = { Text("Новая папка") },
            text = {
                Column {
                    OutlinedTextField(
                        value = folderName,
                        onValueChange = { folderName = it },
                        label = { Text("Название") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Цвет:", fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        palette.forEach { c ->
                            Box(
                                Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(c))
                                    .clickable { folderColor = c },
                                contentAlignment = Alignment.Center
                            ) {
                                if (folderColor == c)
                                    Icon(Icons.Default.Folder, null,
                                        tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (folderName.isNotBlank()) {
                        viewModel.addFolder(folderName, folderColor)
                        showFolderDialog = false
                    }
                }) { Text("Создать") }
            },
            dismissButton = {
                TextButton(onClick = { showFolderDialog = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
fun FolderChip(name: String, selected: Boolean, color: Color? = null, onClick: () -> Unit) {
    val bg = when {
        selected -> MaterialTheme.colorScheme.primary
        color != null -> color.copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surface
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = bg,
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Folder, null,
                modifier = Modifier.size(14.dp),
                tint = if (selected) Color.White else (color ?: MaterialTheme.colorScheme.onSurface))
            Spacer(Modifier.width(4.dp))
            Text(
                name,
                fontSize = 12.sp,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TagChip(tag: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface,
        modifier = Modifier.height(28.dp)
    ) {
        Box(
            Modifier.padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "#$tag",
                fontSize = 11.sp,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun NoteCard(
    note: Note,
    folder: Folder?,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val date = SimpleDateFormat("dd MMM, HH:mm", Locale("ru")).format(Date(note.timestamp))
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
    ) {
        Column(Modifier.padding(14.dp)) {
            Box(
                Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(note.color))
            )
            Spacer(Modifier.height(8.dp))

            // Папка
            if (folder != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Folder, null,
                        modifier = Modifier.size(11.dp),
                        tint = Color(folder.color))
                    Spacer(Modifier.width(3.dp))
                    Text(folder.name, fontSize = 10.sp,
                        color = Color(folder.color),
                        fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(4.dp))
            }

            Text(
                note.title.ifBlank { "Без названия" },
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                note.content,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 5
            )

            // Теги
            val tags = note.tagList()
            if (tags.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tags.take(3).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "#$tag",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (tags.size > 3) {
                        Text("+${tags.size - 3}", fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(date, fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, "Удалить",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}