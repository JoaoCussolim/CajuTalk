package com.app.cajutalk.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.app.cajutalk.R
import com.app.cajutalk.network.models.UsuarioDaSalaDto
import com.app.cajutalk.ui.theme.ACCENT_COLOR
import com.app.cajutalk.ui.theme.BACKGROUND_COLOR
import com.app.cajutalk.ui.theme.WAVE_COLOR
import com.app.cajutalk.viewmodels.DataViewModel
import com.app.cajutalk.viewmodels.SalaViewModel

@Composable
fun RoomMemberItem(
    member: UsuarioDaSalaDto,
    isCurrentUserCreator: Boolean,
    onBanClick: () -> Unit,
    onUnbanClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val itemColor = if (member.IsBanido) Color.Gray.copy(alpha = 0.5f) else WAVE_COLOR

    Row(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 4.dp)
            .background(color = itemColor, shape = RoundedCornerShape(12.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = member.FotoPerfilURL?.replace("http://", "https://"),
            contentDescription = "Foto de ${member.LoginUsuario}",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = member.LoginUsuario,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontFamily = FontFamily(Font(R.font.lexend)),
                    textDecoration = if (member.IsBanido) TextDecoration.LineThrough else TextDecoration.None
                )
                if (member.IsCriador) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Criador da Sala",
                        tint = ACCENT_COLOR,
                        modifier = Modifier.padding(start = 8.dp).size(16.dp)
                    )
                }
            }
            if (member.IsBanido) {
                Text(
                    text = "Banido",
                    fontSize = 12.sp,
                    color = Color.Red,
                    fontFamily = FontFamily(Font(R.font.lexend))
                )
            }
        }

        // Mostra o menu de opções se o usuário atual for o criador e o item não for ele mesmo
        if (isCurrentUserCreator && !member.IsCriador) {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opções")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (member.IsBanido) {
                        DropdownMenuItem(
                            text = { Text("Desbanir Usuário") },
                            onClick = {
                                onUnbanClick()
                                showMenu = false
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Banir Usuário") },
                            onClick = {
                                onBanClick()
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoomMembersScreen(
    navController: NavController,
    dataViewModel: DataViewModel,
    salaViewModel: SalaViewModel
) {
    val context = LocalContext.current
    val currentSala = dataViewModel.estadoSala.sala
    val currentUser = dataViewModel.usuarioLogado

    if (currentSala == null || currentUser == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Erro: Dados da sala ou do usuário não encontrados.")
        }
        return
    }

    val membersResult by salaViewModel.usersInSala.observeAsState()
    val isLoading by salaViewModel.isLoading.observeAsState(false)

    // Busca a lista de membros ao entrar na tela
    LaunchedEffect(key1 = currentSala.ID) {
        salaViewModel.getUsersInSala(currentSala.ID)
    }

    // Atualiza o DataViewModel quando a busca for bem-sucedida
    LaunchedEffect(membersResult) {
        membersResult?.onSuccess { members ->
            dataViewModel.estadoSala.membros = members
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BACKGROUND_COLOR)
    ) {
        DefaultBackIcon(navController)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Membros de '${currentSala.Nome}'",
                fontSize = 24.sp,
                fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                fontWeight = FontWeight.Bold,
                color = ACCENT_COLOR
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading && dataViewModel.estadoSala.membros.isEmpty()) {
                CircularProgressIndicator()
            } else {
                membersResult?.let { result ->
                    result.onFailure {
                        Text(
                            text = "Erro ao carregar membros: ${it.message}",
                            color = Color.Red
                        )
                    }
                    // A lista é lida do DataViewModel, que é atualizado pelo LaunchedEffect
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(dataViewModel.estadoSala.membros, key = { it.UsuarioId }) { member ->
                            RoomMemberItem(
                                member = member,
                                isCurrentUserCreator = currentSala.CriadorID == currentUser.ID,
                                onBanClick = {
                                    Toast.makeText(context, "Banindo ${member.LoginUsuario}...", Toast.LENGTH_SHORT).show()
                                    salaViewModel.banirUsuario(currentSala.ID, member.UsuarioId)
                                },
                                onUnbanClick = {
                                    Toast.makeText(context, "Desbanindo ${member.LoginUsuario}...", Toast.LENGTH_SHORT).show()
                                    salaViewModel.desbanirUsuario(currentSala.ID, member.UsuarioId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}