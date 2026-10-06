package com.renger.system.note.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.renger.system.note.data.Note
import com.renger.system.note.ui.NoteViewModel
import com.renger.system.note.util.AttachmentManager
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(navController: NavController, viewModel: NoteViewModel, noteId: Int) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(0xFF6C63FF.toInt()) }
    var folderId by remember { mutableStateOf<Int?>(null) }
    var tags by remember { mutableStateOf(listOf<String>()) }
    var newTag by remember { mutableStateOf("") }
    var attachments by remember { mutableStateOf(listOf<String>()) }
    var loaded by remember { mutableStateOf(false) }

    val folders by viewModel.folders.collectAsState()

    val palette = listOf(
        0xFF6C63FF.toInt(), 0xFFFF6584.toInt(), 0xFF4CAF50.toInt(),
        0xFFFFA726.toInt(), 0xFF29B6F6.toInt()
    )

    // Пикер фото
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val path = AttachmentManager.copyToAppStorage(context, it)
            if (path != null) attachments = attachments + path
        }
    }

    // Пикер любого файла
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val path = AttachmentManager.copyToAppStorage(context, it)
            if (path != null) attachments = attachments + path
        }
    }

    LaunchedEffect(noteId) {
        if (noteId != 0) {
            viewModel.getNote(noteId) { note ->
                note?.let {
                    title = it.title
                    content = it.content
                    color = it.color
                    folderId = it.folderId
                    tags = it.tagList()
                    attachments = it.attachmentList()
                }
                loaded = true
            }
        } else loaded = true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == 0) "Новая заметка" else "Редактирование") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (title.isNotBlank() || content.isNotBlank() || attachments.isNotEmpty()) {
                            viewModel.save(
                                Note(
                                    id = noteId,
                                    title = title,
                                    content = content,
                                    color = color,
                                    folderId = folderId,
                                    tags = tags.joinToString(","),
                                    attachments = attachments.joinToString("\n")
                                )
                            )
                        }
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Check, null,
                            tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (!loaded) return@Scaffold
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Заголовок", fontSize = 22.sp) },
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Папка
            if (folders.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text("Папка:", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                Spacer(Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FolderPick(
                            name = "Без папки",
                            selected = folderId == null,
                            onClick = { folderId = null }
                        )
                    }
                    items(folders, key = { it.id }) { folder ->
                        FolderPick(
                            name = folder.name,
                            color = Color(folder.color),
                            selected = folderId == folder.id,
                            onClick = {
                                folderId = if (folderId == folder.id) null else folder.id
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Теги
            Text("Теги:", fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = newTag,
                onValueChange = { newTag = it },
                placeholder = { Text("Добавить тег...", fontSize = 12.sp) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodySmall,
                trailingIcon = {
                    IconButton(onClick = {
                        val t = newTag.trim().lowercase()
                        if (t.isNotEmpty() && !tags.contains(t)) {
                            tags = tags + t
                            newTag = ""
                        }
                    }) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                    }
                }
            )

            if (tags.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(tags) { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Row(
                                Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("#$tag", fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary)
                                IconButton(
                                    onClick = { tags = tags - tag },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(Icons.Default.Close, null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Вложения — заголовок и кнопки
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Вложения (${attachments.size}):", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                    Icon(Icons.Default.Image, "Добавить фото",
                        tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = {
                    filePicker.launch(arrayOf("*/*"))
                }) {
                    Icon(Icons.Default.AttachFile, "Добавить файл",
                        tint = MaterialTheme.colorScheme.primary)
                }
            }

            // Список вложений
            if (attachments.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    items(attachments) { path ->
                        AttachmentChip(
                            path = path,
                            onOpen = { openAttachment(context, path) },
                            onRemove = { attachments = attachments - path }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Контент
            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("Начните писать...") },
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // Цвет
            Text("Цвет заметки", fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                palette.forEach { c ->
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(c))
                            .clickable { color = c },
                        contentAlignment = Alignment.Center
                    ) {
                        if (color == c)
                            Icon(Icons.Default.Check, null, tint = Color.White,
                                modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun AttachmentChip(
    path: String,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    val isImage = AttachmentManager.isImage(path)
    val exists = AttachmentManager.exists(path)
    val displayName = AttachmentManager.displayName(path)
    val fileName = File(path).name

    Surface(
        onClick = { if (exists) onOpen() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.height(72.dp)
    ) {
        Row(
            Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isImage) Icons.Default.Image else Icons.Default.Description,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.widthIn(max = 140.dp)) {
                Text(
                    displayName,
                    fontSize = 12.sp,
                    maxLines = 2,
                    color = if (exists)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
                if (!exists) {
                    Text("файл удалён", fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.error)
                } else {
                    Text(
                        if (isImage) "Фото" else "Файл",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun FolderPick(
    name: String,
    selected: Boolean,
    color: Color? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = when {
            selected -> MaterialTheme.colorScheme.primary
            color != null -> color.copy(alpha = 0.2f)
            else -> MaterialTheme.colorScheme.surface
        },
        modifier = Modifier.height(30.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Folder, null,
                modifier = Modifier.size(13.dp),
                tint = if (selected) Color.White
                       else (color ?: MaterialTheme.colorScheme.onSurface))
            Spacer(Modifier.width(4.dp))
            Text(name, fontSize = 11.sp,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface)
        }
    }
}

/**
 * Открывает вложение через внешнее приложение (просмотрщик фото, документов и т.п.).
 */
private fun openAttachment(context: android.content.Context, path: String) {
    try {
        val file = File(path)
        if (!file.exists()) return

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val mime = when (path.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "mp4" -> "video/mp4"
            "mp3" -> "audio/mpeg"
            else -> "*/*"
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Открыть с помощью"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}