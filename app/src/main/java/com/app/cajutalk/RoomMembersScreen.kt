package com.app.cajutalk

import android.app.Application // Para ViewModelFactory
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // Para obter ViewModels
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.app.cajutalk.network.dto.UsuarioDaSalaResponse
import com.app.cajutalk.ui.theme.*
import com.app.cajutalk.viewmodels.AuthViewModel
import com.app.cajutalk.viewmodels.DataViewModel
import com.app.cajutalk.viewmodels.SalaUiState
import com.app.cajutalk.viewmodels.UserSearchUiState
import com.app.cajutalk.viewmodels.UsuarioSalaUiState

@Composable
fun RoomMemberItem(
    member: UsuarioDaSalaResponse, // DTO da API para membros da sala
    isCurrentUserTheCreator: Boolean,
    currentUserIdLoggedIn: Int,
    dataViewModel: DataViewModel, // Para ações de banir/desbanir e preparar perfil
    navController: NavController,
    salaId: Int // Necessário para algumas ações como recarregar membros
) {
    var showMemberActionMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable {
                // Preparar dados para a tela de perfil
                dataViewModel.searchUserById(member.usuarioId)
                // Idealmente, observar o resultado de searchUserById antes de navegar
                // ou a tela de perfil do usuário lida com o carregamento.
                navController.navigate("searched-user-profile")
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = member.fotoPerfilURL,
                contentDescription = "Foto de ${member.loginUsuario}",
                modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    member.loginUsuario,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.lexend)),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (member.isCriador) {
                    Text("Criador da Sala", fontSize = 12.sp, color = ACCENT_COLOR, fontFamily = FontFamily(Font(R.font.lexend)))
                }
                if (member.isBanido) {
                    Text("Banido", fontSize = 12.sp, color = Color.Red.copy(alpha = 0.8f), fontFamily = FontFamily(Font(R.font.lexend)))
                }
            }

            // Menu de Ações do Admin (Banir/Desbanir)
            if (isCurrentUserTheCreator && member.usuarioId != currentUserIdLoggedIn && !member.isCriador) {
                Box {
                    IconButton(onClick = { showMemberActionMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Opções do Membro", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(
                        expanded = showMemberActionMenu,
                        onDismissRequest = { showMemberActionMenu = false }
                    ) {
                        if (member.isBanido) {
                            DropdownMenuItem(
                                text = { Text("Desbanir Usuário") },
                                onClick = {
                                    dataViewModel.desbanirUsuarioDaSala(salaId, member.usuarioId)
                                    showMemberActionMenu = false
                                },
                                leadingIcon = { Icon(Icons.Filled.CheckCircle, contentDescription = "Desbanir", tint = Color.Green) }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Banir Usuário") },
                                onClick = {
                                    dataViewModel.banirUsuarioDaSala(salaId, member.usuarioId)
                                    showMemberActionMenu = false
                                },
                                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = "Banir", tint = Color.Red) }
                            )
                        }
                    }
                }
            } else if (member.isCriador) { // Mostrar ícone de admin se for o criador
                Icon(Icons.Filled.Star, contentDescription = "Criador", tint = ACCENT_COLOR.copy(alpha = 0.7f), modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Para TopAppBar
@Composable
fun RoomMembersScreen(
    navController: NavController,
    salaId: Int, // Recebido via argumento de navegação
    authViewModel: AuthViewModel, // Injetado da MainActivity/CajuTalkApp
    dataViewModel: DataViewModel  // Injetado da MainActivity/CajuTalkApp
) {
    val context = LocalContext.current

    // Estado para a lista de membros e informações da sala
    val membersListState = dataViewModel.usuarioSalaUiState
    val salaInfoState = dataViewModel.activeSalaDetailUiState

    // Informações do usuário logado e da sala
    val currentUser = authViewModel.getSavedUser()
    var isCurrentUserTheCreator by remember { mutableStateOf(false) }
    var currentSalaNome by remember { mutableStateOf("Membros") }

    // Buscar dados ao entrar na tela
    LaunchedEffect(salaId) {
        dataViewModel.fetchUsuariosDaSala(salaId)
        dataViewModel.fetchSalaById(salaId) // Para obter nome da sala e ID do criador
    }

    // Atualizar informações da sala e status de criador
    LaunchedEffect(salaInfoState, currentUser) {
        if (salaInfoState is SalaUiState.SuccessSingle && currentUser != null) {
            val salaDetails = (salaInfoState as SalaUiState.SuccessSingle).sala
            currentSalaNome = salaDetails.nome
            isCurrentUserTheCreator = salaDetails.criadorID == currentUser.id
        } else if (salaInfoState is SalaUiState.Idle && salaId != 0) {
            // Se o estado da sala estiver Idle, mas temos um salaId, tenta buscar novamente.
            // Isso pode acontecer se o usuário navegar para cá rapidamente.
            dataViewModel.fetchSalaById(salaId)
        }
    }

    // Feedback para ações de banir/desbanir
    LaunchedEffect(membersListState) {
        when (val state = membersListState) {
            is UsuarioSalaUiState.SuccessUserStatusChanged -> {
                Toast.makeText(context, "Status do membro atualizado!", Toast.LENGTH_SHORT).show()
                dataViewModel.fetchUsuariosDaSala(salaId) // Recarregar lista após mudança
                dataViewModel.resetUsuarioSalaState()     // Resetar estado para evitar re-trigger
            }
            is UsuarioSalaUiState.Error -> {
                // Mostrar erro apenas se não for o estado inicial de carregamento de lista
                if (state !is UsuarioSalaUiState.Loading && state !is UsuarioSalaUiState.Idle && state !is UsuarioSalaUiState.SuccessUserList) {
                    Toast.makeText(context, "Erro: ${state.message}", Toast.LENGTH_LONG).show()
                    dataViewModel.resetUsuarioSalaState()
                }
            }
            else -> {}
        }
    }
    // Feedback para quando o usuário procurado é carregado (após clicar em um membro)
    val userSearchState = dataViewModel.userSearchUiStateResult
    LaunchedEffect(userSearchState) {
        if (userSearchState is UserSearchUiState.Success) {
            // O usuário foi carregado no DataViewModel, a navegação para searched-user-profile
            // deve acontecer a partir do RoomMemberItem ou de uma lógica que observa isso
            // e tem acesso ao navController.
            // Aqui, apenas resetamos o estado para evitar re-navegações.
            dataViewModel.resetUserSearchState()
        } else if (userSearchState is UserSearchUiState.Error || userSearchState is UserSearchUiState.NotFound) {
            Toast.makeText(context, "Não foi possível carregar o perfil do usuário.", Toast.LENGTH_SHORT).show()
            dataViewModel.resetUserSearchState()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Membros de '$currentSalaNome'",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ACCENT_COLOR, // Usar ACCENT_COLOR
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BACKGROUND_COLOR) // Um fundo um pouco mais claro
        ) {
            when (val state = membersListState) {
                is UsuarioSalaUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = ACCENT_COLOR)
                }
                is UsuarioSalaUiState.SuccessUserList -> {
                    val members = state.usuarios
                    if (members.isEmpty()) {
                        Text(
                            "Nenhum membro encontrado nesta sala.",
                            modifier = Modifier.align(Alignment.Center).padding(16.dp),
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            fontFamily = FontFamily(Font(R.font.lexend))
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp) // Padding para a lista
                        ) {
                            items(members, key = { it.usuarioId }) { member ->
                                RoomMemberItem(
                                    member = member,
                                    isCurrentUserTheCreator = isCurrentUserTheCreator,
                                    currentUserIdLoggedIn = currentUser?.id ?: -1,
                                    dataViewModel = dataViewModel,
                                    navController = navController,
                                    salaId = salaId
                                )
                            }
                        }
                    }
                }
                is UsuarioSalaUiState.Error -> {
                    // Não mostrar erro se for apenas o estado inicial antes de SuccessUserList
                    if (state !is UsuarioSalaUiState.Loading && state !is UsuarioSalaUiState.Idle && state !is UsuarioSalaUiState.SuccessUserList) {
                        Text(
                            "Erro ao carregar membros: ${state.message}",
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center).padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    // Idle ou outro estado não tratado, pode mostrar um placeholder ou nada
                    if(currentUser == null){ // Se o usuário atual ainda não carregou
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = ACCENT_COLOR)
                    }
                }
            }
        }
    }
}