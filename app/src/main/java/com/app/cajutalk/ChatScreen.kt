package com.app.cajutalk

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.app.cajutalk.ui.theme.ACCENT_COLOR
import com.app.cajutalk.ui.theme.HEADER_TEXT_COLOR
import com.app.cajutalk.ui.theme.WAVE_COLOR
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime

typealias ConteudoChat = Triple<Mensagem, User, String>

@Composable
fun ChatBubble(message: String, sender: User, showProfile: Boolean) {
    val isUserMessage = sender.login == mainUser.login
    val bubbleColor = if (isUserMessage) Color(0xFFFF7090) else Color(0xFFF08080)
    val shape = if (isUserMessage) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isUserMessage && showProfile) {
            AsyncImage(
                model = sender.imageUrl,
                contentDescription = "Foto de ${sender.name}",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        if(!isUserMessage && !showProfile){
            Spacer(modifier = Modifier.width(48.dp))
        }

        Box(
            modifier = Modifier
                .background(bubbleColor, shape)
                .padding(12.dp)
                .wrapContentWidth()
        ) {
            Text(
                text = message,
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}

fun formatAudioDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

fun formatAudioButtonDuration(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}

@Composable
fun AudioBubble(audioPath: String, sender: User, showProfile: Boolean) {
    val context = LocalContext.current
    val audioPlayer = remember { AudioPlayer() }
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableStateOf("00:00") }
    var currentTime by remember { mutableStateOf("00:00") }
    var totalDurationMs by remember { mutableLongStateOf(1L) } // Para evitar divisão por zero

    val isUserMessage = sender.login == mainUser.login
    val bubbleColor = if (isUserMessage) Color(0xFFFF7090) else Color(0xFFF08080)
    val shape = if (isUserMessage) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    LaunchedEffect(audioPath) {
        progress = 0f
        currentTime = "00:00"

        val file = File(audioPath)
        val mediaPlayer = if (file.exists()) {
            MediaPlayer().apply {
                setDataSource(audioPath)
                prepare()
            }
        } else {
            MediaPlayer.create(context, R.raw.antareskkkk)
        }

        mediaPlayer?.let {
            totalDurationMs = it.duration.toLong()
            duration = formatAudioDuration(totalDurationMs)
            it.release()
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            val position = audioPlayer.getCurrentPosition()
            currentTime = formatAudioDuration(position.toLong())
            progress = position.toFloat() / totalDurationMs.toFloat()
            delay(100)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isUserMessage && showProfile) {
            AsyncImage(
                model = sender.imageUrl,
                contentDescription = "Foto de ${sender.name}",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        if(!isUserMessage && !showProfile){
            Spacer(modifier = Modifier.width(48.dp))
        }

        Box(
            modifier = Modifier
                .background(bubbleColor, shape)
                .padding(12.dp)
                .wrapContentWidth()
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val file = File(audioPath)
                            if (file.exists()) {
                                if (isPlaying) {
                                    audioPlayer.pauseAudio()
                                } else {
                                    audioPlayer.playAudio(context, audioPath) { isPlaying = false }
                                }
                            } else if (!isUserMessage) {
                                audioPlayer.playRawAudio(context, R.raw.antareskkkk) { isPlaying = false }
                            } else {
                                println("Arquivo de áudio não encontrado: $audioPath")
                            }

                            isPlaying = !isPlaying
                        }
                    ) {
                        Image(
                            painter = if (isPlaying) painterResource(id = R.drawable.pause_audio_icon) else painterResource(id = R.drawable.play_audio_icon),
                            contentDescription = if (isPlaying) "Pausar" else "Continuar",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Slider(
                        value = progress,
                        onValueChange = { newProgress ->
                            progress = newProgress
                            val newPositionMs = (newProgress * totalDurationMs).toLong()
                            audioPlayer.seekTo(newPositionMs)
                        },
                        modifier = Modifier.weight(1f),
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White.copy(alpha = 0.8f),
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                }

                Text(
                    text = "$currentTime / $duration",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

fun salvarArquivo(context: Context, uri: Uri, nome: String) {
    val inputStream = context.contentResolver.openInputStream(uri)
    val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    val outFile = File(downloadDir, nome)

    inputStream?.use { input ->
        FileOutputStream(outFile).use { output ->
            input.copyTo(output)
        }
    }

    Toast.makeText(context, "Arquivo salvo em: ${outFile.absolutePath}", Toast.LENGTH_LONG).show()
}

@Composable
fun FileBubble(arquivo: Mensagem, sender: User, showProfile: Boolean, onDownloadClick: (Uri) -> Unit) {
    val isUserMessage = sender.login == mainUser.login
    val bubbleColor = if (isUserMessage) Color(0xFFFF7090) else Color(0xFFF08080)
    val shape = if (isUserMessage) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isUserMessage && showProfile) {
            AsyncImage(
                model = sender.imageUrl,
                contentDescription = "Foto de ${sender.name}",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        if (!isUserMessage && !showProfile) {
            Spacer(modifier = Modifier.width(48.dp))
        }

        Box(
            modifier = Modifier
                .background(bubbleColor, shape)
                .padding(12.dp)
                .wrapContentWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.widthIn(max = 240.dp)
            ) {
                Icon(
                    painter =  painterResource(id = R.drawable.description_icon),
                    contentDescription = "Arquivo",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = arquivo.nomeArquivo,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = {
                    arquivo.uriArquivo?.let { onDownloadClick(it) }
                }) {
                    Icon(
                        painter =  painterResource(id = R.drawable.download_icon),
                        contentDescription = "Baixar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

fun getFileNameFromUri(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        it.moveToFirst()
        it.getString(nameIndex)
    } ?: "arquivo"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ImageContainer(
    imageUri: Uri,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    maxHeight: Dp = 240.dp,
    isUserMessage: Boolean,
) {
    val borderColor = if (isUserMessage) Color(0xFFFF7090) else Color(0xFFF08080)
    var showFullScreen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        Card(
            shape = RectangleShape,
            border = BorderStroke(1.dp, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = modifier
                .wrapContentSize()
                .sizeIn(maxHeight = maxHeight)
                .clickable { showFullScreen = true }
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit, // mantém proporção e centraliza
                modifier = Modifier
                    .wrapContentSize()
                    .padding(4.dp)
            )
        }

        if (showFullScreen) {
            Dialog(onDismissRequest = { showFullScreen = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = contentDescription,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                    )
                    IconButton(
                        onClick = { showFullScreen = false },
                        modifier = Modifier
                            .padding(16.dp)
                            .size(36.dp)
                            .align(Alignment.TopStart)
                            .background(
                                color = Color(0x66000000),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VideoPreview(
    videoUri: Uri,
    modifier: Modifier = Modifier,
    maxWidth: Dp = 240.dp,
    maxHeight: Dp = 240.dp,
    isUserMessage: Boolean
) {
    val borderColor = if (isUserMessage) Color(0xFFFF7090) else Color(0xFFF08080)
    var showFullScreen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            playWhenReady = false
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        Card(
            shape = RectangleShape,
            border = BorderStroke(1.dp, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = modifier
                .wrapContentSize()
                .sizeIn(maxWidth = maxWidth, maxHeight = maxHeight)
                .clickable { showFullScreen = true }
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                },
                modifier = Modifier
                    .wrapContentSize()
                    .padding(4.dp)
            )
        }
    }

    if (showFullScreen) {
        Dialog(onDismissRequest = {
            showFullScreen = false
            exoPlayer.release()
        }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = true
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                )

                IconButton(
                    onClick = { showFullScreen = false },
                    modifier = Modifier
                        .padding(16.dp)
                        .size(36.dp)
                        .align(Alignment.TopStart)
                        .background(
                            color = Color(0x66000000),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatScreen(viewModel: AudioRecorderViewModel, navController: NavController, roomViewModel: DataViewModel) {
    val sala = roomViewModel.estadoSala.sala

    if (sala == null) {
        Text("Erro: sala não encontrada")
        return
    }

    roomViewModel.estadoSala.membros = sala.membros

    val bottomColor = Color(0xFFFDB361)

    var message by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ConteudoChat>() }

    var menuExpanded by remember { mutableStateOf(false) }
    var showAlertDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? Activity

    fun sendMessage(conteudo: Mensagem, sender: User, tipo: String) {
        messages.add(Triple(conteudo, sender, tipo))
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val nomeArquivo = getFileNameFromUri(context, it)
            val mensagem = Mensagem(
                texto = "", // não tem texto
                nomeArquivo = nomeArquivo,
                uriArquivo = it,
                isUser = true,
                data = LocalDateTime.now()
            )
            sendMessage(mensagem, mainUser, "file")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(HEADER_TEXT_COLOR, bottomColor),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                    contentDescription = "Sair",
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                        .size(60.dp)
                        .clickable { navController.popBackStack() },
                    tint = Color(0xFFFF5313)
                )
                Box(
                    modifier = Modifier
                        .size(85.dp)
                        .clip(CircleShape)
                        .background(color = Color(0xFFFFD670))
                        .align(Alignment.CenterVertically)
                ) {
                    AsyncImage(
                        model = sala.imageUrl,
                        contentDescription = "Ícone da Sala",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = sala.nome,
                        fontSize = 30.sp,
                        fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFFFF5313),
                        lineHeight = 30.sp,
                    )

                    Text(
                        text = "Criada por: ${sala.criador.name}",
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                        fontWeight = FontWeight(400),
                        color = Color(0xE5FFD670),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp,
                    )
                }

                Box(
                    modifier = Modifier
                        .wrapContentSize(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Menu",
                        tint = Color(0xFFFF5313),
                        modifier = Modifier
                            .padding(vertical = 28.dp, horizontal = 16.dp)
                            .size(40.dp)
                            .clickable { menuExpanded = true } // abre o menu ao clicar
                    )

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Ver membros",
                                    fontFamily = FontFamily(Font(R.font.lexend)),
                                    color = Color(0xFFFF7094)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                navController.navigate("room-members")
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Excluir sala",
                                    fontFamily = FontFamily(Font(R.font.lexend)),
                                    color = Color(0xFFFF7094)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                showAlertDialog = true
                            }
                        )
                    }

                    if (showAlertDialog) {
                        AlertDialog(
                            onDismissRequest = { showAlertDialog = false },
                            title = { Text("Excluir sala",
                                fontFamily = FontFamily(Font(R.font.lexend)),
                                color = ACCENT_COLOR) },
                            text = { Text("Tem certeza que deseja excluir esta sala? Essa ação não pode ser desfeita.", fontFamily = FontFamily(Font(R.font.lexend)), color = ACCENT_COLOR) },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showAlertDialog = false
                                        //excluirSala()
                                    }
                                ) {
                                    Text("Confirmar",
                                        fontFamily = FontFamily(Font(R.font.lexend)),
                                        color = Color(0xFFFF7094)
                                    )
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showAlertDialog = false }
                                ) {
                                    Text("Cancelar", fontFamily = FontFamily(Font(R.font.lexend)),
                                        color = Color(0xFFFF7094)
                                    )
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = chatBackgroundColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        reverseLayout = true
                    ) {
                        val reversedMessages = messages.reversed()

                        itemsIndexed(reversedMessages) { idx, (mensagem, sender, tipo) ->
                            val next = reversedMessages.getOrNull(idx + 1)?.second
                            val showProfile = next?.login != sender.login

                            if (tipo == "file") {
                                val mime = context.contentResolver.getType(mensagem.uriArquivo!!)
                                if (mime?.startsWith("image") == true) {
                                    ImageContainer(
                                        imageUri = mensagem.uriArquivo,
                                        contentDescription = mensagem.nomeArquivo,
                                        isUserMessage = mensagem.isUser
                                    )
                                    FileBubble(mensagem, sender, showProfile) { uri ->
                                        salvarArquivo(context, uri, mensagem.nomeArquivo)
                                    }
                                } else if (mime?.startsWith("video") == true){
                                    VideoPreview(videoUri = mensagem.uriArquivo, isUserMessage = mensagem.isUser)
                                    FileBubble(mensagem, sender, showProfile) { uri ->
                                        salvarArquivo(context, uri, mensagem.nomeArquivo)
                                    }
                                } else {
                                    FileBubble(mensagem, sender, showProfile) { uri ->
                                        salvarArquivo(context, uri, mensagem.nomeArquivo)
                                    }
                                }
                            } else if (tipo == "text") {
                                ChatBubble(mensagem.texto, sender, showProfile)
                            } else if (tipo == "audio") {
                                AudioBubble(mensagem.texto, sender, showProfile)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { filePickerLauncher.launch("*/*") }) {
                            Image(
                                painter = painterResource(id = R.drawable.anexo_icon),
                                contentDescription = "Anexo"
                            )
                        }
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            placeholder = { Text("Digitar...", color = Color(0xFFFFA000)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(25.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                cursorColor = Color(0xFFFFA000),
                                focusedContainerColor = WAVE_COLOR,
                                unfocusedContainerColor = WAVE_COLOR,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        var pressStartTime by remember { mutableLongStateOf(0L) }
                        var recordingDuration by remember { mutableLongStateOf(0L) }
                        var isRecording by remember { mutableStateOf(false) }
                        var appearDuration by remember { mutableStateOf(false) }

                        val buttonSize by animateDpAsState(if (isRecording) 80.dp else 50.dp, animationSpec = tween(200))

                        LaunchedEffect(isRecording) {
                            if (isRecording) {
                                appearDuration = true
                                while (isRecording) {
                                    delay(1000)
                                    recordingDuration = (System.currentTimeMillis() - pressStartTime) / 1000
                                }
                            } else {
                                appearDuration = false
                                recordingDuration = 0L
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(buttonSize)
                                .clip(CircleShape)
                                .background(Color(0xFFFF7090))
                                .pointerInteropFilter { event ->
                                    when (event.action) {
                                        MotionEvent.ACTION_DOWN -> {
                                            pressStartTime = System.currentTimeMillis()
                                            isRecording = false

                                            Handler(Looper.getMainLooper()).postDelayed({
                                                if (System.currentTimeMillis() - pressStartTime >= 500L) {
                                                    if (viewModel.hasPermissions(context)) {
                                                        isRecording = true
                                                        viewModel.startRecording(context)
                                                    } else {
                                                        if (activity != null) {
                                                            viewModel.requestPermissions(activity)
                                                        }
                                                    }
                                                }
                                            }, 500L)

                                            true
                                        }

                                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                            val pressDuration =
                                                System.currentTimeMillis() - pressStartTime

                                            if (isRecording) {
                                                isRecording = false
                                                viewModel.stopRecording()

                                                viewModel.audioPath?.let {
                                                    val file = File(it)
                                                    if (file.exists() && file.length() > 0) {
                                                        sendMessage( Mensagem(
                                                            texto = it,
                                                            nomeArquivo = "",
                                                            uriArquivo = null,
                                                            isUser = true,
                                                            data = LocalDateTime.now()
                                                        ), mainUser, "audio")
                                                    } else {
                                                        println("⚠️ Arquivo de áudio inválido: $it")
                                                    }
                                                }
                                            } else if (pressDuration < 500L) {
                                                if (message.isNotBlank()) {
                                                    sendMessage( Mensagem(
                                                        texto = message,
                                                        nomeArquivo = "",
                                                        uriArquivo = null,
                                                        isUser = true,
                                                        data = LocalDateTime.now()
                                                    ), mainUser, "text")
                                                    message = ""
                                                }
                                            }

                                            Handler(Looper.getMainLooper()).postDelayed({
                                                isRecording = false
                                            }, 500L)

                                            recordingDuration = 0L
                                            true
                                        }

                                        else -> false
                                    }
                                },
                            contentAlignment = Alignment.Center
                        )
                        {
                            if(isRecording && appearDuration) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        formatAudioButtonDuration(recordingDuration),
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )

                                    Image(
                                        painter = painterResource(id = R.drawable.microfone_icon),
                                        contentDescription = "Microfone",
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            } else {
                                Image(
                                    painter = if (message.isNotBlank()) painterResource(id = R.drawable.send_icon) else painterResource(id = R.drawable.microfone_icon),
                                    contentDescription = if (message.isNotBlank()) "Enviar" else "Microfone",
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
