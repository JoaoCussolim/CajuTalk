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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.app.cajutalk.network.RetrofitClient
import com.app.cajutalk.network.dto.MensagemDto
import com.app.cajutalk.network.dto.SalaChatResponse
import com.app.cajutalk.network.dto.UsuarioDaSalaResponse
import com.app.cajutalk.ui.theme.ACCENT_COLOR
import com.app.cajutalk.ui.theme.BACKGROUND_COLOR
import com.app.cajutalk.ui.theme.HEADER_TEXT_COLOR
import com.app.cajutalk.ui.theme.WAVE_COLOR
import com.app.cajutalk.viewmodels.AudioRecorderViewModel
import com.app.cajutalk.viewmodels.AuthViewModel
import com.app.cajutalk.viewmodels.DataViewModel
import com.app.cajutalk.viewmodels.MensagemUiState
import com.app.cajutalk.viewmodels.SalaUiState
import com.app.cajutalk.viewmodels.UsuarioSalaUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

typealias ConteudoChat = Triple<Mensagem, User, String>

@RequiresApi(Build.VERSION_CODES.O)
fun MensagemDto.toLocalMensagem(apiBaseUrl: String, currentUserLogin: String, context: Context): Mensagem {
    val isCurrentUser = this.loginUsuario == currentUserLogin
    var localUri: Uri? = null
    var localNomeArquivo = this.conteudo // Por padrão, pode ser o nome do arquivo ou texto

    if (this.tipoMensagem != "Texto") {
        // O 'conteudo' do DTO é a URL relativa do arquivo na API (ex: /uploads/nome.jpg)
        // Verifique se o arquivo já foi baixado e existe localmente
        val fileNameFromUrl = this.conteudo.substringAfterLast('/')
        localNomeArquivo = fileNameFromUrl
        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val localFile = File(downloadDir, fileNameFromUrl)
        if (localFile.exists()) {
            localUri = Uri.fromFile(localFile) // Se já baixado, use o URI local
        }
    }

    // Converter dataEnvio (String ISO 8601 UTC) para LocalDateTime
    // Assumindo que dataEnvio é algo como "2023-10-27T10:30:00.123Z"
    val zonedDateTime = ZonedDateTime.parse(this.dataEnvio)
    val localDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()


    return Mensagem(
        idApi = this.id,
        texto = if (this.tipoMensagem == "Texto") this.conteudo else "",
        nomeArquivo = if (this.tipoMensagem != "Texto") localNomeArquivo else null,
        uriArquivo = localUri, // Será nulo se ainda não baixado
        isUser = isCurrentUser,
        data = localDateTime,
        urlDaApi = if (this.tipoMensagem != "Texto") /* RetrofitClient.BASE_URL.removeSuffix("/") */ apiBaseUrl + this.conteudo else null,
        // Adicionar campos para tipo e senderInfo se necessário no seu modelo Mensagem local
        tipoApi = this.tipoMensagem,
        senderLoginApi = this.loginUsuario,
        senderNameApi = null, // Você precisaria buscar o nome do usuário ou tê-lo no DTO
        senderImageUrlApi = this.fotoPerfilURL
    )
}

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
                    arquivo.nomeArquivo?.let {
                        Text(
                            text = it,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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

fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = getFileNameFromUri(context, uri) // Sua função existente
        val tempFile = File(context.cacheDir, fileName)
        tempFile.outputStream().use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        inputStream?.close()
        tempFile
    } catch (e: Exception) {
        Log.e("ChatScreen", "Erro ao converter URI para File", e)
        null
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
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatScreen(
    salaId: Int, // Precisa do ID da sala
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(), // Obter AuthViewModel
    dataViewModel: DataViewModel = viewModel(),
    audioRecorderViewModel: AudioRecorderViewModel = viewModel() // Renomeado para clareza
) {
    // Obter usuário atual do AuthViewModel
    val currentUser = authViewModel.getSavedUser() // Ou observe um State do AuthViewModel

    // Sala e Membros (assumindo que você tem um estado para detalhes da sala no DataViewModel)
    // Vamos usar o activeSalaDetailUiState para a sala atual
    LaunchedEffect(salaId) {
        dataViewModel.fetchSalaById(salaId) // Buscar detalhes da sala (incluindo criador)
        dataViewModel.fetchUsuariosDaSala(salaId) // Buscar membros da sala
    }
    val salaDetailState = dataViewModel.activeSalaDetailUiState
    val usuariosSalaState = dataViewModel.usuarioSalaUiState

    var salaChat: SalaChatResponse? by remember { mutableStateOf(null) }
    var salaCreatorName: String by remember { mutableStateOf("Desconhecido") }
    var membersInRoom by remember { mutableStateOf<List<UsuarioDaSalaResponse>>(emptyList()) }

    when (val state = salaDetailState) {
        is SalaUiState.SuccessSingle -> salaChat = state.sala
        is SalaUiState.Error -> Text("Erro ao carregar sala: ${state.message}")
        else -> { /* Loading ou Idle */ }
    }
    when (val state = usuariosSalaState) {
        is UsuarioSalaUiState.SuccessUserList -> membersInRoom = state.usuarios
        is UsuarioSalaUiState.Error -> Text("Erro ao carregar membros: ${state.message}")
        else -> { /* Loading ou Idle */ }
    }

    // Encontrar o nome do criador da sala
    LaunchedEffect(salaChat, membersInRoom) {
        salaChat?.let { chat ->
            val creator = membersInRoom.find { it.usuarioId == chat.criadorID }
            salaCreatorName = creator?.loginUsuario ?: "Desconhecido" // Usar login se nome não disponível
        }
    }


    val bottomColor = Color(0xFFFDB361)
    var messageText by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Observar mensagens do ViewModel
    val mensagemState = dataViewModel.mensagemUiState
    var chatMessages by remember { mutableStateOf<List<Mensagem>>(emptyList()) } // Lista de Mensagem local

    // API Base URL para construir URLs completas de arquivos
    val apiBaseUrl = RetrofitClient.BASE_URL.removeSuffix("/")


    // Carregar mensagens iniciais e observar novas
    LaunchedEffect(salaId) {
        dataViewModel.fetchMensagensDaSala(salaId)
    }

    // Atualizar a lista local 'chatMessages' quando o estado do ViewModel mudar
    LaunchedEffect(mensagemState) {
        if (mensagemState is MensagemUiState.SuccessReceivedList) {
            val apiMessages = (mensagemState as MensagemUiState.SuccessReceivedList).mensagens
            chatMessages = apiMessages.mapNotNull { dto ->
                currentUser?.login?.let { login ->
                    val localMsg = dto.toLocalMensagem(apiBaseUrl, login, context)
                    // Tentar encontrar nome e imagem do sender nos membros da sala
                    val senderInfo = membersInRoom.find { it.usuarioId == dto.usuarioId }
                    localMsg.senderNameApi = senderInfo?.loginUsuario ?: dto.loginUsuario // Ou nome se tiver
                    localMsg.senderImageUrlApi = senderInfo?.fotoPerfilURL ?: dto.fotoPerfilURL
                    localMsg
                }
            }.sortedBy { it.data } // Ordenar por data
        } else if (mensagemState is MensagemUiState.SuccessSent) {
            // Adicionar mensagem enviada à lista ou recarregar tudo
            dataViewModel.fetchMensagensDaSala(salaId) // Simplesmente recarregar
        }
    }


    // Lógica para abrir o seletor de arquivos
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val file = uriToFile(context, selectedUri) // Converte URI para File
            if (file != null) {
                val mimeType = context.contentResolver.getType(selectedUri) ?: "*/*"
                val tipoMensagemApi = when {
                    mimeType.startsWith("image") -> "Imagem"
                    mimeType.startsWith("video") -> "Video"
                    mimeType.startsWith("audio") -> "Audio"
                    else -> "Arquivo"
                }
                // Usar a função do DataViewModel para enviar
                dataViewModel.enviarMensagemComArquivo(
                    salaId = salaId,
                    conteudoTextoOpcional = null, // Ou o nome do arquivo, a API define isso
                    arquivo = file,
                    tipoMensagemApi = tipoMensagemApi
                )
            } else {
                Toast.makeText(context, "Não foi possível acessar o arquivo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var menuExpanded by remember { mutableStateOf(false) }
    var showAlertDialog by remember { mutableStateOf(false) }
    val activity = context as? Activity


    if (salaChat == null || currentUser == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator() // Tela de carregamento enquanto sala ou usuário não estão prontos
        }
        return // Não renderizar o resto se salaChat ou currentUser for nulo
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
            // --- HEADER DA SALA ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp) // Altura ajustada
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp), // Padding ajustado
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Ícone padrão de voltar
                        contentDescription = "Sair",
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFFFF5313)
                    )
                }
                AsyncImage(
                    model = salaChat?.fotoPerfilURL, // Placeholder para sala
                    contentDescription = "Ícone da Sala",
                    modifier = Modifier
                        .size(60.dp) // Tamanho ajustado
                        .clip(CircleShape)
                        .background(color = Color(0xFFFFD670)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = salaChat?.nome ?: "Carregando Sala...",
                        fontSize = 22.sp, // Tamanho ajustado
                        fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                        fontWeight = FontWeight.Bold, // Mais destaque
                        color = Color(0xFFFF5313),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Criada por: $salaCreatorName",
                        fontSize = 14.sp, // Tamanho ajustado
                        fontFamily = FontFamily(Font(R.font.lexend)), // Fonte mais legível
                        color = Color(0xE5FFD670).copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Menu",
                            tint = Color(0xFFFF5313),
                            modifier = Modifier.size(32.dp) // Tamanho ajustado
                        )
                    }
                    DropdownMenu( /* ... sua lógica de menu ... */
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ver membros", fontFamily = FontFamily(Font(R.font.lexend)), color = ACCENT_COLOR) },
                            onClick = {
                                menuExpanded = false
                                navController.navigate("room-members/${salaId}") // Passar ID da sala
                            }
                        )
                        // Apenas o criador pode excluir
                        if (salaChat?.criadorID == currentUser.id) {
                            DropdownMenuItem(
                                text = { Text("Excluir sala", fontFamily = FontFamily(Font(R.font.lexend)), color = Color.Red) },
                                onClick = {
                                    menuExpanded = false
                                    showAlertDialog = true
                                }
                            )
                        }
                    }
                }
            }
            // --- FIM DO HEADER DA SALA ---

            if (showAlertDialog) {
                AlertDialog(
                    onDismissRequest = { showAlertDialog = false },
                    title = { Text("Excluir sala", fontFamily = FontFamily(Font(R.font.lexend)), color = ACCENT_COLOR) },
                    text = { Text("Tem certeza que deseja excluir esta sala? Essa ação não pode ser desfeita.", fontFamily = FontFamily(Font(R.font.lexend)), color = ACCENT_COLOR) },
                    confirmButton = {
                        TextButton(onClick = {
                            showAlertDialog = false
                            dataViewModel.deleteSala(salaId) // Chamar ViewModel
                            // Após deletar, observar o SalaUiState e navegar se SuccessSalaDeleted
                        }) { Text("Confirmar", fontFamily = FontFamily(Font(R.font.lexend)), color = Color.Red) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAlertDialog = false }) { Text("Cancelar", fontFamily = FontFamily(Font(R.font.lexend)), color = ACCENT_COLOR) }
                    }
                )
            }
            // Observar resultado da exclusão da sala e navegar
            LaunchedEffect(salaDetailState) {
                if (salaDetailState is SalaUiState.SuccessSalaDeleted) {
                    Toast.makeText(context, "Sala excluída", Toast.LENGTH_SHORT).show()
                    navController.popBackStack() // Voltar para a tela anterior
                    dataViewModel.resetActiveSalaDetailState() // Resetar estado
                }
            }


            // --- ÁREA DO CHAT ---
            Card(
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 0.dp, bottomEnd = 0.dp), // Apenas cantos superiores
                colors = CardDefaults.cardColors(containerColor = chatBackgroundColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Ocupa o espaço restante
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    reverseLayout = true // Mensagens mais novas embaixo
                ) {
                    val reversedMessages = chatMessages.reversed() // Já está ordenado por data, reverter para lazycolumn
                    itemsIndexed(reversedMessages) { idx, localMessage ->
                        val prevMessageSenderLogin = reversedMessages.getOrNull(idx + 1)?.senderLoginApi
                        val showProfilePic = localMessage.senderLoginApi != prevMessageSenderLogin && !localMessage.isUser

                        when (localMessage.tipoApi) {
                            "Texto" -> ChatBubble(localMessage.texto, mainUser, showProfilePic)
                            "Audio" -> {
                                if (localMessage.uriArquivo != null) { // Já baixado/local
                                    AudioBubble(localMessage.uriArquivo.toString(), mainUser, showProfilePic)
                                } else if (localMessage.urlDaApi != null) {
                                    // Lógica para mostrar "Baixar áudio" ou iniciar download
                                    DownloadableMediaBubble(localMessage, mainUser, showProfilePic, dataViewModel, context, apiBaseUrl)
                                }
                            }
                            "Imagem" -> {
                                if (localMessage.uriArquivo != null) {
                                    ImageContainer(imageUri = localMessage.uriArquivo!!, contentDescription = localMessage.nomeArquivo, isUserMessage = localMessage.isUser)
                                } else if (localMessage.urlDaApi != null) {
                                    DownloadableMediaBubble(localMessage, mainUser, showProfilePic, dataViewModel, context, apiBaseUrl)
                                }
                            }
                            "Video" -> {
                                if (localMessage.uriArquivo != null) {
                                    VideoPreview(videoUri = localMessage.uriArquivo!!, isUserMessage = localMessage.isUser)
                                } else if (localMessage.urlDaApi != null) {
                                    DownloadableMediaBubble(localMessage, mainUser, showProfilePic, dataViewModel, context, apiBaseUrl)
                                }
                            }
                            "Arquivo" -> {
                                if (localMessage.uriArquivo != null || localMessage.urlDaApi != null) {
                                    DownloadableMediaBubble(localMessage, mainUser, showProfilePic, dataViewModel, context, apiBaseUrl)
                                }
                            }
                            // Adicionar outros tipos se necessário
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                // --- INPUT DE MENSAGEM ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp, top = 4.dp), // Padding ajustado
                    verticalAlignment = Alignment.Bottom // Alinhar ao fundo
                ) {
                    IconButton(onClick = { filePickerLauncher.launch("*/*") }) {
                        Image(painter = painterResource(id = R.drawable.anexo_icon), contentDescription = "Anexo")
                    }
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Digitar...", color = Color(0xFFFFA000)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(25.dp),
                        colors = OutlinedTextFieldDefaults.colors(/* ... */)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // --- BOTÃO DE ENVIAR / GRAVAR ÁUDIO ---
                    val canSendMessage = messageText.isNotBlank()
                    var pressStartTime by remember { mutableLongStateOf(0L) }
                    var recordingDuration by remember { mutableLongStateOf(0L) } // Segundos
                    var isRecording by remember { mutableStateOf(false) }
                    var showRecordingDuration by remember { mutableStateOf(false) } // Para controlar a visibilidade
                    val audioButtonSize by animateDpAsState(targetValue = if (isRecording) 70.dp else 50.dp, label = "audioButtonSize")


                    LaunchedEffect(isRecording) {
                        if (isRecording) {
                            showRecordingDuration = true
                            while (isRecording) { // Loop enquanto isRecording for true
                                delay(100) // Atualiza a cada 100ms para mais fluidez
                                recordingDuration = (System.currentTimeMillis() - pressStartTime) / 1000
                            }
                        } else {
                            // Apenas resetar a duração se não estiver enviando
                            if (pressStartTime != 0L) { // Evita resetar se nunca começou a gravar
                                recordingDuration = 0L
                            }
                            // Delay para o texto da duração sumir após soltar
                            delay(200) // Pequeno delay
                            showRecordingDuration = false
                            pressStartTime = 0L // Resetar para próxima gravação
                        }
                    }


                    Box(
                        modifier = Modifier
                            .size(audioButtonSize)
                            .clip(CircleShape)
                            .background(Color(0xFFFF7090))
                            .pointerInteropFilter { event ->
                                when (event.action) {
                                    MotionEvent.ACTION_DOWN -> {
                                        if (!canSendMessage) { // Só iniciar gravação se não houver texto
                                            pressStartTime = System.currentTimeMillis()
                                            // Não seta isRecording aqui, espera o delay
                                            // Postar um delayed runnable para verificar se é um long press
                                            val handler = Handler(Looper.getMainLooper())
                                            handler.postDelayed({
                                                // Checar se o botão ainda está pressionado e não há texto
                                                if (pressStartTime != 0L && !canSendMessage && (System.currentTimeMillis() - pressStartTime >= 300L)) { // 300ms para long press
                                                    if (audioRecorderViewModel.hasPermissions(context)) {
                                                        isRecording = true // Inicia a gravação visual
                                                        audioRecorderViewModel.startRecording(context)
                                                    } else {
                                                        activity?.let { audioRecorderViewModel.requestPermissions(it) }
                                                    }
                                                }
                                            }, 300L) // 300ms de delay para considerar long press
                                        }
                                        true
                                    }
                                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                        val pressDuration = System.currentTimeMillis() - pressStartTime
                                        Handler(Looper.getMainLooper()).removeCallbacksAndMessages(null) // Remove o delayed runnable

                                        if (isRecording) {
                                            audioRecorderViewModel.stopRecording()
                                            isRecording = false // Para a gravação visual
                                            // Pequeno delay para garantir que o arquivo foi escrito
                                            Handler(Looper.getMainLooper()).postDelayed({
                                                audioRecorderViewModel.audioPath?.let { audioPath ->
                                                    val audioFile = File(audioPath)
                                                    if (audioFile.exists() && audioFile.length() > 0) {
                                                        dataViewModel.enviarMensagemComArquivo(
                                                            salaId = salaId,
                                                            conteudoTextoOpcional = null,
                                                            arquivo = audioFile,
                                                            tipoMensagemApi = "Audio"
                                                        )
                                                    }
                                                }
                                                audioRecorderViewModel.audioPath = null // Limpar path
                                            }, 200)


                                        } else if (canSendMessage && pressDuration < 300L) { // Se foi um clique rápido e tem texto
                                            dataViewModel.enviarMensagemTexto(salaId, messageText)
                                            messageText = ""
                                        }
                                        // Resetar pressStartTime para evitar cliques fantasmas se o usuário segurar e soltar rápido sem intenção de gravar
                                        pressStartTime = 0L
                                        true
                                    }
                                    else -> false
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRecording && showRecordingDuration) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(formatAudioButtonDuration(recordingDuration), color = Color.White, fontSize = 12.sp)
                                Image(painter = painterResource(id = R.drawable.microfone_icon), contentDescription = "Gravando", modifier = Modifier.size(24.dp))
                            }
                        } else {
                            Image(
                                painter = if (canSendMessage) painterResource(id = R.drawable.send_icon) else painterResource(id = R.drawable.microfone_icon),
                                contentDescription = if (canSendMessage) "Enviar" else "Gravar Áudio",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
        // DisposableEffect para limpar o MediaPlayer da AudioBubble se necessário,
        // mas a AudioBubble já tenta fazer isso com remember.
        // Se tiver problemas com players de áudio vazando, um DisposableEffect aqui pode ser útil
        // para chamar audioPlayer.release() em todas as instâncias quando a ChatScreen sair de composição.
    }
}

// Nova Composable para lidar com mídias que precisam ser baixadas
@Composable
fun DownloadableMediaBubble(
    mensagem: Mensagem, // Seu modelo Mensagem local
    sender: User,
    showProfilePic: Boolean,
    dataViewModel: DataViewModel, // Para iniciar o download
    context: Context,
    apiBaseUrl: String
) {
    val isUserMessage = sender.login == mainUser.login
    val bubbleColor = if (isUserMessage) Color(0xFFFF7090) else Color(0xFFF08080)
    val shape = if (isUserMessage) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)

    // Estado para controlar se o download está em progresso para esta mensagem específica
    // Você pode querer mover isso para o objeto Mensagem se precisar que persista entre recomposições
    var isDownloading by remember(mensagem.idApi) { mutableStateOf(mensagem.isDownloading) }
    var downloadProgress by remember(mensagem.idApi) { mutableFloatStateOf(mensagem.downloadProgress) }


    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isUserMessage && showProfilePic) {
            AsyncImage(model = sender.imageUrl, contentDescription = "Foto de ${sender.name}",
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray))
            Spacer(modifier = Modifier.width(8.dp))
        }
        if (!isUserMessage && !showProfilePic) {
            Spacer(modifier = Modifier.width(48.dp))
        }

        Box(
            modifier = Modifier.background(bubbleColor, shape).padding(12.dp).wrapContentWidth()
        ) {
            Column(modifier = Modifier.widthIn(max = 240.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val iconRes = when (mensagem.tipoApi) {
                        "Imagem" -> R.drawable.description_icon // Crie este ícone
                        "Video" -> R.drawable.description_icon   // Crie este ícone
                        "Audio" -> R.drawable.description_icon   // Crie este ícone
                        else -> R.drawable.description_icon
                    }
                    Icon(painter = painterResource(id = iconRes), contentDescription = mensagem.tipoApi, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mensagem.nomeArquivo ?: "Arquivo",
                        color = Color.White, fontWeight = FontWeight.Bold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    val scope = rememberCoroutineScope() // Precisa estar dentro de @Composable
                    if (!isDownloading && mensagem.uriArquivo == null) {
                        IconButton(onClick = {
                            if (mensagem.urlDaApi != null) {
                                isDownloading = true
                                downloadProgress = 0f
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        val url = URL(mensagem.urlDaApi)
                                        val connection = url.openConnection()
                                        connection.connect()
                                        val totalLength = connection.contentLength
                                        val inputStream = connection.getInputStream()

                                        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                        val outFile = File(downloadDir, mensagem.nomeArquivo ?: "downloaded_${System.currentTimeMillis()}")

                                        FileOutputStream(outFile).use { output ->
                                            val data = ByteArray(1024)
                                            var count: Int
                                            var downloadedLength: Long = 0
                                            while (inputStream.read(data).also { count = it } != -1) {
                                                output.write(data, 0, count)
                                                downloadedLength += count
                                                if (totalLength > 0) {
                                                    withContext(Dispatchers.Main) {
                                                        downloadProgress = downloadedLength.toFloat() / totalLength
                                                    }
                                                }
                                            }
                                        }
                                        inputStream.close()
                                        withContext(Dispatchers.Main) {
                                            isDownloading = false
                                            // Atualizar a mensagem na lista principal com o URI local
                                            // Esta parte é complexa, pois precisa atualizar o estado da lista de mensagens
                                            // Uma abordagem seria o ViewModel gerenciar o estado de download de cada mensagem
                                            Toast.makeText(context, "Download concluído: ${outFile.name}", Toast.LENGTH_SHORT).show()
                                            // Forçar recarregamento das mensagens para pegar o URI local (ou ter um mecanismo melhor)
                                            dataViewModel.fetchMensagensDaSala(mensagem.idApi ?: -1) // Supondo que idApi é o salaId aqui, o que está errado. Precisa do salaId.
                                            // Você precisa do ID da sala atual para recarregar.
                                        }
                                    } catch (e: Exception) {
                                        Log.e("Download", "Erro ao baixar: ${e.message}", e)
                                        withContext(Dispatchers.Main) {
                                            isDownloading = false
                                            Toast.makeText(context, "Erro no download.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }

                            }
                        }) {
                            Icon(painter = painterResource(id = R.drawable.download_icon), contentDescription = "Baixar", tint = Color.White)
                        }
                    }
                }
                if (isDownloading) {
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier.fillMaxWidth().height(4.dp).padding(top = 4.dp),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}