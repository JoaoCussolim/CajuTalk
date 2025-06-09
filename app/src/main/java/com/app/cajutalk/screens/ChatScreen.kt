package com.app.cajutalk.screens

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
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
import androidx.compose.runtime.livedata.observeAsState
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
import com.app.cajutalk.R
import com.app.cajutalk.classes.AudioPlayer
import com.app.cajutalk.classes.Mensagem
import com.app.cajutalk.classes.User
import com.app.cajutalk.network.models.MensagemDto
import com.app.cajutalk.ui.theme.ACCENT_COLOR
import com.app.cajutalk.ui.theme.HEADER_TEXT_COLOR
import com.app.cajutalk.ui.theme.WAVE_COLOR
import com.app.cajutalk.viewmodels.AudioRecorderViewModel
import com.app.cajutalk.viewmodels.DataViewModel
import com.app.cajutalk.viewmodels.MensagemViewModel
import com.app.cajutalk.viewmodels.UserViewModel
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime

typealias ConteudoChat = Triple<Mensagem, User, String>

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
fun AudioBubble(audioUrl: String, isUserMessage: Boolean, context: Context) {
    val audioPlayer = remember { AudioPlayer() }
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableStateOf("00:00") }
    var currentTime by remember { mutableStateOf("00:00") }
    var totalDurationMs by remember { mutableLongStateOf(1L) }

    val bubbleColor = if (isUserMessage) Color(0xFFFF7090) else Color(0xFFF08080)
    val shape = if (isUserMessage) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)

    // Efeito para buscar a duração do áudio da URL
    LaunchedEffect(audioUrl) {
        try {
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl.replace("http://", "https://"))
                prepare()
            }
            totalDurationMs = mediaPlayer.duration.toLong()
            duration = formatAudioDuration(totalDurationMs)
            mediaPlayer.release()
        } catch (e: Exception) {
            Log.e("AudioBubble", "Falha ao preparar áudio da URL: $audioUrl", e)
            duration = "00:00"
        }
    }

    // Efeito para atualizar a barra de progresso
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            val position = audioPlayer.getCurrentPosition().toLong()
            currentTime = formatAudioDuration(position)
            if (totalDurationMs > 0) {
                progress = position.toFloat() / totalDurationMs.toFloat()
            }
            delay(100)
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
                    val secureUrl = audioUrl.replace("http://", "https://")
                    if (isPlaying) {
                        audioPlayer.pauseAudio()
                    } else {
                        audioPlayer.playAudio(context, secureUrl) { isPlaying = false }
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
                        progress = newProgress
                        audioPlayer.seekTo((newProgress * totalDurationMs).toLong())
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
                text = "$currentTime / $duration",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.End).padding(end = 8.dp)
            )
        }
    }
}


@Composable
fun FileBubble(fileUrl: String, isUserMessage: Boolean) {
    val context = LocalContext.current
    val fileName = remember { Uri.parse(fileUrl).lastPathSegment ?: "Arquivo" }

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
            IconButton(onClick = {
                // TODO: Implementar lógica de download com DownloadManager
                Toast.makeText(context, "Download (a ser implementado)", Toast.LENGTH_SHORT).show()
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
@Composable
fun MessageRow(message: MensagemDto, isUserMessage: Boolean, showProfile: Boolean, context: Context) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        // Mostra a foto de perfil se não for do usuário e se for a primeira mensagem da sequência
        if (!isUserMessage && showProfile) {
            AsyncImage(
                model = message.FotoPerfilURL?.replace("http://", "https://"),
                contentDescription = "Foto de ${message.LoginUsuario}",
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray)
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else if (!isUserMessage) {
            // Adiciona um espaçamento para alinhar as mensagens
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Renderiza a bolha de chat apropriada
        when (message.TipoMensagem.lowercase()) {
            "imagem" -> ImageContainer(Uri.parse(message.Conteudo), null, isUserMessage = isUserMessage)
            "video" -> VideoPreview(Uri.parse(message.Conteudo), isUserMessage = isUserMessage)
            "audio" -> AudioBubble(message.Conteudo, isUserMessage = isUserMessage, context)
            "arquivo" -> FileBubble(message.Conteudo, isUserMessage = isUserMessage)
            else -> ChatBubble(message = message.Conteudo, isUserMessage = isUserMessage)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatScreen(viewModel: AudioRecorderViewModel, navController: NavController, roomViewModel: DataViewModel, mensagemViewModel: MensagemViewModel, userViewModel: UserViewModel) {

    val context = LocalContext.current
    val sala = roomViewModel.estadoSala.sala
    val usuarioLogado = roomViewModel.usuarioLogado

    if (sala == null || usuarioLogado == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Erro ao carregar dados da sala ou do usuário.")
        }
        return
    }

    //roomViewModel.estadoSala.membros = users

    val bottomColor = Color(0xFFFDB361)

    var message by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<MensagemDto>() }

    val mensagensDaSalaResult by mensagemViewModel.mensagensDaSala.observeAsState()
    val enviarMensagemResult by mensagemViewModel.enviarMensagemResult.observeAsState()
    val isLoading by mensagemViewModel.isLoading.observeAsState(initial = false)
    var creatorName by remember { mutableStateOf<String?>(null) }
    val creatorUserResult by userViewModel.userById.observeAsState()

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
            messages.addAll(fetchedMessages.reversed()) // Invertemos para o LazyColumn começar de baixo
        }
        mensagensDaSalaResult?.onFailure { error ->
            // Você pode mostrar um Toast ou uma mensagem de erro aqui
            Log.e("ChatScreen", "Erro ao carregar mensagens: ${error.message}")
        }
    }

    LaunchedEffect(enviarMensagemResult) {
        enviarMensagemResult?.onSuccess { novaMensagem ->
            messages.add(0, novaMensagem) // Adiciona no topo da lista (que está invertida)
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
                mimeType.startsWith("image") -> "Imagem"
                mimeType.startsWith("video") -> "Video"
                mimeType.startsWith("audio") -> "Audio"
                else -> "Arquivo"
            }

            mensagemViewModel.enviarMensagem(
                idSala = sala.ID,
                conteudoTexto = null,
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
                    val secureRoomImageUrl = sala.FotoPerfilURL?.replace("http://", "https://")

                    AsyncImage(
                        model = secureRoomImageUrl,
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
                        text = sala.Nome,
                        fontSize = 30.sp,
                        fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFFFF5313),
                        lineHeight = 30.sp,
                    )

                    Text(
                        text = "Criada por: ${creatorName ?: "..."}",
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
                            text = { Text("Tem certeza que deseja excluir esta sala? Essa ação não pode ser desfeita.", fontFamily = FontFamily(Font(
                                R.font.lexend
                            )), color = ACCENT_COLOR) },
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

                        items(messages.size, key = { index -> messages[index].Id }) { index ->
                            val msg = messages.asReversed()[index]
                            val isUserMessage = msg.UsuarioId == usuarioLogado?.ID
                            val showProfile = messages.asReversed().getOrNull(index + 1)?.UsuarioId != msg.UsuarioId || index == messages.lastIndex

                            MessageRow(message = msg, isUserMessage = isUserMessage, showProfile = showProfile, context = context)
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
                                                viewModel.audioPath?.let { audioPath ->
                                                    mensagemViewModel.enviarMensagem(
                                                        idSala = sala.ID,
                                                        conteudoTexto = null,
                                                        tipoMensagem = "Audio",
                                                        arquivoUri = Uri.fromFile(File(audioPath))
                                                    )
                                                }
                                            } else if (pressDuration < 500L) {
                                                if (message.isNotBlank()) {
                                                    mensagemViewModel.enviarMensagem(
                                                        idSala = sala.ID,
                                                        conteudoTexto = message,
                                                        tipoMensagem = "Texto",
                                                        arquivoUri = null
                                                    )
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
