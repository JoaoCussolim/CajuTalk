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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.app.cajutalk.R
import com.app.cajutalk.network.models.UsuarioDaSalaDto
import com.app.cajutalk.network.models.UsuarioDto
import com.app.cajutalk.ui.theme.BACKGROUND_COLOR
import com.app.cajutalk.ui.theme.WAVE_COLOR
import com.app.cajutalk.viewmodels.DataViewModel
import com.app.cajutalk.viewmodels.SalaViewModel
import com.app.cajutalk.viewmodels.UserViewModel

@Composable
fun RoomMemberItem(
    member: UsuarioDaSalaDto,
    isCreator: Boolean,
    onViewProfile: (UsuarioDaSalaDto) -> Unit,
    onBanUser: (UsuarioDaSalaDto) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .background(color = WAVE_COLOR, shape = RoundedCornerShape(25.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .clickable { onViewProfile(member) }
        ) {
            AsyncImage(
                model = member.FotoPerfilURL,
                contentDescription = "Foto de ${member.LoginUsuario}",
                placeholder = painterResource(id = R.drawable.placeholder_image),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onViewProfile(member) }
        ) {
            Text(
                text = member.LoginUsuario,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontFamily = FontFamily(Font(R.font.lexend))
            )
            if (member.IsCriador) {
                Text(
                    text = "👑 Criador",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily(Font(R.font.lexend))
                )
            }
        }

        // 3. Exibe o menu de opções (banir) apenas se o usuário logado for o criador
        // e o membro da lista não for ele mesmo.
        if (isCreator && !member.IsCriador) {
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Mais opções")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Banir da Sala", fontFamily = FontFamily(Font(R.font.lexend))) },
                        onClick = {
                            onBanUser(member) // Chama a função de banir
                            expanded = false
                        }
                    )
                    // TODO: A sua API não possui um endpoint para "remover" (kick).
                    // Apenas para banir. Se necessário, um novo endpoint precisaria ser criado.
                }
            }
        }
    }
}

// Alteração: A função agora recebe SalaViewModel e UserViewModel.
@Composable
fun RoomMembersScreen(
    navController: NavController,
    dataViewModel: DataViewModel,
    salaViewModel: SalaViewModel,
    userViewModel: UserViewModel
) {
    val sala = dataViewModel.estadoSala.sala
    val context = LocalContext.current

    // 1. Busca a lista de membros da sala assim que a tela é aberta.
    LaunchedEffect(sala) {
        if (sala != null) {
            salaViewModel.getUsersInSala(sala.ID)
        }
    }

    // 2. Observa os estados do ViewModel.
    val membersResult by salaViewModel.usersInSala.observeAsState()
    val banResult by salaViewModel.banirUsuarioResult.observeAsState()
    val isLoading by salaViewModel.isLoading.observeAsState(false)
    val userDetailsResult by userViewModel.userById.observeAsState()

    // Efeito para tratar o resultado da ação de banir.
    LaunchedEffect(banResult) {
        banResult?.let { result ->
            result.onSuccess {
                Toast.makeText(context, "Usuário banido com sucesso!", Toast.LENGTH_SHORT).show()
                // Atualiza a lista de membros após o banimento.
                if (sala != null) {
                    salaViewModel.getUsersInSala(sala.ID)
                }
            }
            result.onFailure {
                Toast.makeText(context, "Erro ao banir usuário: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Efeito para navegar para a tela de perfil após buscar os detalhes do usuário.
    LaunchedEffect(userDetailsResult) {
        userDetailsResult?.onSuccess { userDto ->
            dataViewModel.usuarioProcurado = userDto
            navController.navigate("searched-user-profile")
        }
    }

    // Determina se o usuário logado é o criador da sala.
    // Isso assume que `dataViewModel.usuarioLogado` foi preenchido após o login.
    val isCreator = dataViewModel.usuarioLogado?.ID == sala?.CriadorID

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
                text = "Membros de '${sala?.Nome ?: ""}'",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                membersResult?.let { result ->
                    result.onSuccess { members ->
                        if (members.isEmpty()) {
                            Text("Não há membros nesta sala.")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                items(members) { member ->
                                    RoomMemberItem(
                                        member = member,
                                        isCreator = isCreator,
                                        onViewProfile = { selectedMember ->
                                            // Busca os detalhes completos do usuário antes de navegar.
                                            userViewModel.getUserById(selectedMember.UsuarioId)
                                        },
                                        onBanUser = { memberToBan ->
                                            // 4. Conecta o botão de banir à chamada do ViewModel.
                                            sala?.let {
                                                salaViewModel.banirUsuario(it.ID, memberToBan.UsuarioId)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    result.onFailure {
                        Text("Erro ao carregar membros: ${it.message}", color = Color.Red)
                    }
                }
            }
        }
    }
}