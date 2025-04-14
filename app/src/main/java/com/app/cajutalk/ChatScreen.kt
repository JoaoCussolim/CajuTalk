package com.app.cajutalk

import android.app.Activity
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.app.cajutalk.ui.theme.HEADER_TEXT_COLOR
import com.app.cajutalk.ui.theme.WAVE_COLOR
import kotlinx.coroutines.delay
import java.io.File

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
    var progress by remember { mutableStateOf(0f) }
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
    val messages = remember { mutableStateListOf<Triple<String, User, String>>() }

    var menuExpanded by remember { mutableStateOf(false) }
    var showAlertDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? Activity

    fun sendMessage(text: String, sender: User, messageType: String) {
        if (text.isBlank()) return

        messages.add(Triple(text, sender, messageType))
    }

    fun otherUserMessages(){
        messages.add(Triple("Vou te matar!", antares, "text"))
        messages.add(Triple("Ou você é o sung jin woo? \uD83D\uDE28", antares, "text"))
        messages.add(Triple("", antares, "audio"))
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
                                fontFamily = FontFamily(Font(R.font.lexend))) },
                            text = { Text("Tem certeza que deseja excluir esta sala? Essa ação não pode ser desfeita.", fontFamily = FontFamily(Font(R.font.lexend))) },
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
                colors = CardDefaults.cardColors(containerColor = Color(0xE5FFFAFA)),
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

                        itemsIndexed(reversedMessages) { index, (message, sender, messageType) ->
                            val nextSender = reversedMessages.getOrNull(index + 1)?.second
                            val showProfile = nextSender?.login != sender.login

                            if(messageType == "text"){
                                ChatBubble(message, sender, showProfile)
                            }else if (messageType == "audio"){
                                AudioBubble(message, sender, showProfile)
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
                        IconButton(onClick = { otherUserMessages() }) {
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
                                                        sendMessage(it, mainUser, "audio")
                                                    } else {
                                                        println("⚠️ Arquivo de áudio inválido: $it")
                                                    }
                                                }
                                            } else if (pressDuration < 500L) {
                                                if (message.isNotBlank()) {
                                                    sendMessage(message, mainUser, "text")
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
