package com.app.cajutalk.screens

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.MimeTypeMap
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import com.app.cajutalk.R
import com.app.cajutalk.classes.AndroidDownloader
import com.app.cajutalk.classes.AudioPlayer
import com.app.cajutalk.network.RetrofitClient
import com.app.cajutalk.network.models.MensagemDto
import com.app.cajutalk.ui.theme.ACCENT_COLOR
import com.app.cajutalk.ui.theme.HEADER_TEXT_COLOR
import com.app.cajutalk.ui.theme.WAVE_COLOR
import com.app.cajutalk.viewmodels.AudioRecorderViewModel
import com.app.cajutalk.viewmodels.DataViewModel
import com.app.cajutalk.viewmodels.MensagemViewModel
import com.app.cajutalk.viewmodels.SalaViewModel
import com.app.cajutalk.viewmodels.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import com.app.cajutalk.network.models.TipoMensagem

// Funções de Bubble (ChatBubble, AudioBubble, etc.) permanecem as mesmas
@Composable
fun ChatBubble(message: String, isUserMessage: Boolean) {
    val bubbleColor = if (isUserMessage) Color(0xFFFF7090) else Color(0xFFF08080)
    val shape = if (isUserMessage) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)

    Box(
        modifier = Modifier
            .background(bubbleColor, shape)
            .padding(12.dp)
    ) {
        Text(text = message, color = Color.White, fontSize = 16.sp)
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
fun AudioBubble(audioUrl: String, isUserMessage: Boolean) {
    val context = LocalContext.current
    val audioPlayer = remember { AudioPlayer() }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var totalDurationMs by remember { mutableLongStateOf(0L) }

    // Calcula o progresso a partir da posição e duração
    val progress = remember(currentPositionMs, totalDurationMs) {
        if (totalDurationMs > 0) (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }

    val durationFormatted = remember(totalDurationMs) { formatAudioDuration(totalDurationMs) }
    val currentTimeFormatted = remember(currentPositionMs) { formatAudioDuration(currentPositionMs) }

    val bubbleColor = if (isUserMessage) Color(0xFFFF7090) else Color(0xFFF08080)
    val shape = if (isUserMessage) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp) else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)

    val fullAudioUrl = remember(audioUrl) {
        if (audioUrl.startsWith("http")) audioUrl else RetrofitClient.BASE_URL + audioUrl.removePrefix("/")
    }

    LaunchedEffect(fullAudioUrl) {
        if (fullAudioUrl.isBlank()) return@LaunchedEffect
        try {
            MediaPlayer().apply {
                setDataSource(fullAudioUrl)
                prepare()
                totalDurationMs = duration.toLong()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioBubble", "Falha ao obter duração: $fullAudioUrl", e)
            totalDurationMs = 0
        }
    }

    // Efeito para atualizar a posição do áudio enquanto toca
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPositionMs = audioPlayer.getCurrentPosition().toLong()
            delay(100)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.stopAudio()
        }
    }

    Box(
        modifier = Modifier
            .background(bubbleColor, shape)
            .padding(12.dp)
            .widthIn(min = 180.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (totalDurationMs == 0L) return@IconButton // Não faz nada se não conseguiu carregar o áudio

                    if (isPlaying) {
                        audioPlayer.pauseAudio()
                    } else {
                        if (currentPositionMs == 0L && !audioPlayer.isPlaying()) {
                            audioPlayer.playAudio(context, fullAudioUrl) {
                                isPlaying = false
                                currentPositionMs = 0L
                            }
                        } else {
                            audioPlayer.resumeAudio()
                        }
                    }
                    isPlaying = !isPlaying
                }) {
                    Icon(
                        painter = if (isPlaying) painterResource(R.drawable.pause_audio_icon) else painterResource(R.drawable.play_audio_icon),
                        contentDescription = "Play/Pause",
                        tint = Color.White
                    )
                }
                Slider(
                    value = progress,
                    onValueChange = { newProgress ->
                        if (totalDurationMs > 0) {
                            val newPosition = (newProgress * totalDurationMs).toLong()
                            currentPositionMs = newPosition
                            audioPlayer.seekTo(newPosition)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White.copy(alpha = 0.8f),
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            }
            Text(
                text = "$currentTimeFormatted / $durationFormatted",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 8.dp)
            )
        }
    }
}

@Composable
fun FileBubble(fileUrl: String, isUserMessage: Boolean) {
    val context = LocalContext.current
    val fileName = remember { Uri.parse(fileUrl).lastPathSegment ?: "Arquivo" }

    val downloader = remember { AndroidDownloader(context) }

    val fullFileUrl = remember(fileUrl) {
        if (fileUrl.startsWith("http")) fileUrl else RetrofitClient.BASE_URL + fileUrl.removePrefix("/")
    }

    val secureUrl = fullFileUrl.replace("http://", "https://")

    val bubbleColor = if (isUserMessage) Color(0xFFFF7090) else Color(0xFFF08080)
    val shape = if (isUserMessage) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)

    Box(
        modifier = Modifier
            .background(bubbleColor, shape)
            .padding(12.dp)
            .widthIn(max = 240.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.description_icon),
                contentDescription = "Arquivo",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = fileName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick =  {
                val fileExtension = MimeTypeMap.getFileExtensionFromUrl(fileName)
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension) ?: "application/octet-stream"

                downloader.downloadFile(secureUrl, fileName, mimeType)
                Toast.makeText(context, "Iniciando download...", Toast.LENGTH_SHORT).show()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.download_icon),
                    contentDescription = "Baixar",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ImageContainer(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    maxHeight: Dp = 240.dp,
    isUserMessage: Boolean,
) {
    val borderColor = if (isUserMessage) Color(0xFFFF7090) else Color(0xFFF08080)
    var showFullScreen by remember { mutableStateOf(false) }
    val fullImageUrl = remember(imageUrl) {
        if (imageUrl.startsWith("http")) {
            imageUrl
        } else {
            RetrofitClient.BASE_URL + imageUrl.removePrefix("/")
        }
    }
    val secureImageUrl = remember(fullImageUrl) { fullImageUrl.replace("http://", "https://") }


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
                model = secureImageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(4.dp),
                error = painterResource(id = R.drawable.placeholder_image)
            )
        }

        if (showFullScreen) {
            Dialog(onDismissRequest = { showFullScreen = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable { showFullScreen = false }
                ) {
                    AsyncImage(
                        model = secureImageUrl,
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

@Composable
fun VideoPreview(
    videoUrl: String,
    modifier: Modifier = Modifier,
    maxWidth: Dp = 240.dp,
    maxHeight: Dp = 240.dp,
    isUserMessage: Boolean
) {
    val borderColor = if (isUserMessage) Color(0xFFFF7090) else Color(0xFFF08080)
    var showFullScreen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val fullVideoUrl = remember(videoUrl) {
        if (videoUrl.startsWith("http")) {
            videoUrl
        } else {
            RetrofitClient.BASE_URL + videoUrl.removePrefix("/")
        }
    }
    val secureVideoUrl = remember(fullVideoUrl) { fullVideoUrl.replace("http://", "https://") }

    // MUDANÇA: Melhor gerenciamento do ciclo de vida do ExoPlayer
    val exoPlayer = remember(secureVideoUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(secureVideoUrl))
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
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
        val fullScreenExoPlayer = remember(secureVideoUrl) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(secureVideoUrl))
                prepare()
                playWhenReady = true
            }
        }

        Dialog(onDismissRequest = {
            showFullScreen = false
            fullScreenExoPlayer.release()
        }) {
            DisposableEffect(Unit) {
                onDispose {
                    fullScreenExoPlayer.release()
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = fullScreenExoPlayer
                            useController = true
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
                        .background(color = Color(0x66000000), shape = CircleShape)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MessageRow(message: MensagemDto, isUserMessage: Boolean, showProfile: Boolean, context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isUserMessage && showProfile) {
            AsyncImage(
                model = message.FotoPerfilURL?.replace("http://", "https://"),
                contentDescription = "Foto de ${message.LoginUsuario}",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                error = painterResource(id = R.drawable.placeholder_image)
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else if (!isUserMessage) {
            Spacer(modifier = Modifier.width(48.dp))
        }

        when (message.TipoMensagem.lowercase()) {
            "imagem" -> ImageContainer(message.Conteudo, null, isUserMessage = isUserMessage)
            "video" -> VideoPreview(message.Conteudo, isUserMessage = isUserMessage)
            "audio" -> AudioBubble(message.Conteudo, isUserMessage = isUserMessage)
            "arquivo" -> FileBubble(message.Conteudo, isUserMessage = isUserMessage)
            else -> ChatBubble(message = message.Conteudo, isUserMessage = isUserMessage)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatScreen(
    viewModel: AudioRecorderViewModel,
    navController: NavController,
    roomViewModel: DataViewModel,
    mensagemViewModel: MensagemViewModel,
    userViewModel: UserViewModel,
    salaViewModel: SalaViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val sala = roomViewModel.estadoSala.sala
    val usuarioLogado = roomViewModel.usuarioLogado

    if (sala == null || usuarioLogado == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Erro ao carregar dados da sala ou do usuário.")
        }
        return
    }

    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableLongStateOf(0L) }
    var pressStartTime by remember { mutableLongStateOf(0L) }

    val buttonSize by animateDpAsState(
        targetValue = if (isRecording) 80.dp else 50.dp,
        animationSpec = tween(200), label = ""
    )

    val bottomColor = Color(0xFFFDB361)
    var message by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<MensagemDto>() }

    val mensagensDaSalaResult by mensagemViewModel.mensagensDaSala.observeAsState()
    val enviarMensagemResult by mensagemViewModel.enviarMensagemResult.observeAsState()
    val isLoading by mensagemViewModel.isLoading.observeAsState(initial = true)

    var creatorName by remember { mutableStateOf<String?>(null) }
    val creatorUserResult by userViewModel.userById.observeAsState()

    val deleteSalaResult by salaViewModel.deleteSalaResult.observeAsState()

    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                recordingDuration = (System.currentTimeMillis() - pressStartTime) / 1000
                delay(100)
            }
        } else {
            recordingDuration = 0L
        }
    }

    LaunchedEffect(deleteSalaResult) {
        deleteSalaResult?.onSuccess {
            Toast.makeText(context, "Sala '${sala.Nome}' excluída com sucesso.", Toast.LENGTH_SHORT).show()
            navController.navigate("salas") {
                popUpTo("salas") { inclusive = true }
            }
        }
        deleteSalaResult?.onFailure {
            Toast.makeText(context, "Erro ao excluir a sala: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(key1 = sala.CriadorID) {
        if (creatorName == null) {
            userViewModel.getUserById(sala.CriadorID)
        }
    }
    LaunchedEffect(creatorUserResult) {
        creatorUserResult?.onSuccess { user ->
            creatorName = user.NomeUsuario
        }
    }

    LaunchedEffect(key1 = sala.ID) {
        mensagemViewModel.getMensagensPorSala(sala.ID)
    }

    LaunchedEffect(mensagensDaSalaResult) {
        mensagensDaSalaResult?.onSuccess { fetchedMessages ->
            messages.clear()
            messages.addAll(fetchedMessages)
            if (fetchedMessages.isNotEmpty()) {
                coroutineScope.launch {
                    listState.scrollToItem(messages.lastIndex)
                }
            }
        }
        mensagensDaSalaResult?.onFailure { error ->
            Log.e("ChatScreen", "Erro ao carregar mensagens: ${error.message}")
            Toast.makeText(context, "Erro ao carregar mensagens", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(enviarMensagemResult) {
        enviarMensagemResult?.onSuccess { novaMensagem ->
            if (messages.none { it.Id == novaMensagem.Id }) {
                messages.add(novaMensagem)
            }
            coroutineScope.launch {
                listState.animateScrollToItem(messages.lastIndex)
            }
        }
        enviarMensagemResult?.onFailure { error ->
            Toast.makeText(context, "Falha ao enviar: ${error.message}", Toast.LENGTH_SHORT).show()
        }
    }

    var menuExpanded by remember { mutableStateOf(false) }
    var showAlertDialog by remember { mutableStateOf(false) }

    val activity = context as? Activity

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val mimeType = context.contentResolver.getType(it) ?: "application/octet-stream"
            val tipoMensagem = when {
                mimeType.startsWith("image") -> "imagem"
                mimeType.startsWith("video") -> "video"
                mimeType.startsWith("audio") -> "audio"
                else -> "arquivo"
            }
            Toast.makeText(context, "Enviando ${tipoMensagem.lowercase()}...", Toast.LENGTH_SHORT).show()
            mensagemViewModel.enviarMensagem(
                idSala = sala.ID,
                conteudoTexto = "arquivo",
                tipoMensagem = tipoMensagem,
                arquivoUri = it
            )
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
                    .height(120.dp)
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                    contentDescription = "Sair",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(40.dp)
                        .clickable { navController.popBackStack() },
                    tint = Color(0xFFFF5313)
                )
                AsyncImage(
                    model = sala.FotoPerfilURL?.replace("http://", "https://"),
                    contentDescription = "Ícone da Sala",
                    modifier = Modifier
                        .size(64.dp)
                        .padding(start = 8.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.placeholder_image)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = sala.Nome, fontSize = 24.sp, fontFamily = FontFamily(Font(R.font.baloo_bhai)), fontWeight = FontWeight(400), color = Color.White, lineHeight = 24.sp)
                    Text(text = "Criada por: ${creatorName ?: "..."}", fontSize = 16.sp, fontFamily = FontFamily(Font(R.font.lexend)), fontWeight = FontWeight(400), color = Color(0xE5FFD670), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clickable { menuExpanded = true }
                    )
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(text = { Text("Ver membros") }, onClick = { menuExpanded = false; navController.navigate("room-members") })
                        if (sala.CriadorID == usuarioLogado.ID) {
                            DropdownMenuItem(text = { Text("Excluir sala", color = Color.Red) }, onClick = { menuExpanded = false; showAlertDialog = true })
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = chatBackgroundColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (isLoading && messages.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(messages.size, key = { index -> messages[index].Id }) { index ->
                                val msg = messages[index]
                                val isUserMessage = msg.UsuarioId == usuarioLogado.ID
                                val showProfile = index == 0 || messages.getOrNull(index - 1)?.UsuarioId != msg.UsuarioId
                                MessageRow(message = msg, isUserMessage = isUserMessage, showProfile = showProfile, context = context)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { filePickerLauncher.launch("*/*") }, enabled = !isLoading) {
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
                            ),
                            readOnly = isLoading
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .size(buttonSize)
                                .clip(CircleShape)
                                .background(Color(0xFFFF7090))
                                .pointerInteropFilter { event ->
                                    if (isLoading || activity == null || message.isNotBlank()) {
                                        // Se estiver carregando ou se houver texto, apenas lida com o envio de texto
                                        if (event.action == MotionEvent.ACTION_UP && message.isNotBlank()) {
                                            val tipo = TipoMensagem.Texto.ordinal.toString()
                                            mensagemViewModel.enviarMensagem(sala.ID, message, "texto", null)
                                            message = ""
                                        }
                                        return@pointerInteropFilter true
                                    }

                                    when (event.action) {
                                        MotionEvent.ACTION_DOWN -> {
                                            pressStartTime = System.currentTimeMillis()
                                            isRecording = true
                                            viewModel.startRecording(context)
                                            true
                                        }
                                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                            isRecording = false
                                            viewModel.stopRecording()
                                            viewModel.audioPath?.let { path ->
                                                mensagemViewModel.enviarMensagem(sala.ID, "audio", "audio", Uri.fromFile(File(path)))
                                            }
                                            true
                                        }
                                        else -> false
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else if (isRecording) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = formatAudioButtonDuration(recordingDuration),
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Image(
                                        painter = painterResource(id = R.drawable.microfone_icon),
                                        contentDescription = "Gravando",
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            else {
                                val iconRes = if (message.isNotBlank()) R.drawable.send_icon else R.drawable.microfone_icon
                                Image(
                                    painter = painterResource(id = iconRes),
                                    contentDescription = if (message.isNotBlank()) "Enviar" else "Gravar Áudio",
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAlertDialog) {
            AlertDialog(
                onDismissRequest = { showAlertDialog = false },
                title = { Text("Excluir Sala", color = Color.Red) },
                text = { Text("Tem certeza que deseja excluir a sala '${sala.Nome}'? Esta ação é permanente.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showAlertDialog = false
                            salaViewModel.deleteSala(sala.ID)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Excluir")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAlertDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}