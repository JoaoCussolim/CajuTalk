package com.app.cajutalk

import android.app.Application // Necessário para ViewModelFactory se usada aqui
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // Para obter ViewModels
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.app.cajutalk.network.dto.EntrarSalaRequest
import com.app.cajutalk.network.dto.SalaChatResponse
import com.app.cajutalk.network.dto.SalaCreateRequest
import com.app.cajutalk.ui.theme.*
import com.app.cajutalk.viewmodels.AuthViewModel
import com.app.cajutalk.viewmodels.DataViewModel
import com.app.cajutalk.viewmodels.SalaUiState
import com.app.cajutalk.viewmodels.UsuarioSalaUiState

@Composable
fun CreateRoomDialog(
    authViewModel: AuthViewModel,
    dataViewModel: DataViewModel,
    onDismiss: () -> Unit
) {
    var nomeSala by remember { mutableStateOf("") }
    var isPrivada by remember { mutableStateOf(false) }
    var senhaSala by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUriStringForDisplay by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    // O ID do criador é pego do token na API, não precisamos passar explicitamente do currentUser.

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        imageUriStringForDisplay = uri?.toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Criar Nova Sala",
                fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                fontSize = 22.sp,
                color = ACCENT_COLOR,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(100.dp), // Tamanho um pouco menor
                    contentAlignment = Alignment.BottomEnd
                ) {
                    AsyncImage(
                        model = imageUriStringForDisplay,
                        contentDescription = "Imagem da Sala",
                        modifier = Modifier.fillMaxSize().clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.size(36.dp) // Botão de câmera menor
                            .background(ACCENT_COLOR, shape = CircleShape)
                            .border(1.dp, Color.White, CircleShape)
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.camera_icon),
                            contentDescription = "Escolher imagem",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = nomeSala,
                    onValueChange = { nomeSala = it },
                    label = { Text("Nome da Sala", fontFamily = FontFamily(Font(R.font.lexend))) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ACCENT_COLOR,
                        cursorColor = ACCENT_COLOR,
                        unfocusedBorderColor = ACCENT_COLOR.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(
                        checked = isPrivada,
                        onCheckedChange = { isPrivada = it },
                        colors = CheckboxDefaults.colors(checkedColor = ACCENT_COLOR)
                    )
                    Text("Sala Privada", fontFamily = FontFamily(Font(R.font.lexend)), modifier = Modifier.clickable { isPrivada = !isPrivada })
                }
                if (isPrivada) {
                    OutlinedTextField(
                        value = senhaSala,
                        onValueChange = { senhaSala = it },
                        label = { Text("Senha da Sala", fontFamily = FontFamily(Font(R.font.lexend))) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ACCENT_COLOR,
                            cursorColor = ACCENT_COLOR,
                            unfocusedBorderColor = ACCENT_COLOR.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nomeSala.isBlank()) {
                        Toast.makeText(context, "Nome da sala não pode ser vazio.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (isPrivada && senhaSala.isBlank()) {
                        Toast.makeText(context, "Sala privada precisa de senha.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // AVISO: imageUriStringForDisplay é uma content:// URI local.
                    // A API C# precisa de uma lógica para lidar com isso (ex: se for um serviço que pode buscar de URIs públicas)
                    // ou você precisa fazer upload para um storage (Firebase, S3) e enviar a URL resultante.
                    // Para um upload direto, a API precisaria aceitar IFormFile.
                    val fotoUrlParaApi = imageUriStringForDisplay // Temporário, precisa de estratégia de upload real

                    val salaCreateRequest = SalaCreateRequest(
                        nome = nomeSala,
                        publica = !isPrivada,
                        senha = if (isPrivada) senhaSala else null,
                        fotoPerfilURL = fotoUrlParaApi
                    )
                    dataViewModel.createSala(salaCreateRequest)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ACCENT_COLOR),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) { Text("Criar Sala", color = Color.White, fontFamily = FontFamily(Font(R.font.lexend))) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) { Text("Cancelar", color = ACCENT_COLOR, fontFamily = FontFamily(Font(R.font.lexend))) }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun EnterPrivateRoomDialog(
    sala: SalaChatResponse,
    dataViewModel: DataViewModel,
    onDismiss: () -> Unit,
    onSuccessNavigation: (salaId: Int) -> Unit
) {
    var senhaSalaInput by remember { mutableStateOf("") }
    var showLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val joinRoomState = dataViewModel.usuarioSalaUiState

    LaunchedEffect(joinRoomState) {
        when (val state = joinRoomState) {
            is UsuarioSalaUiState.Loading -> showLoading = true
            is UsuarioSalaUiState.SuccessJoined -> {
                showLoading = false
                Toast.makeText(context, "Entrou na sala!", Toast.LENGTH_SHORT).show()
                dataViewModel.resetUsuarioSalaState()
                onSuccessNavigation(sala.id) // Passar o ID da sala
                onDismiss()
            }
            is UsuarioSalaUiState.Error -> {
                showLoading = false
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                dataViewModel.resetUsuarioSalaState()
                // Não fechar o diálogo automaticamente em caso de erro de senha,
                // a menos que o erro seja "banido" ou algo assim.
                if(state.message.contains("banido", ignoreCase = true)) onDismiss()
            }
            else -> showLoading = false
        }
    }

    AlertDialog(
        onDismissRequest = { if (!showLoading) onDismiss() },
        title = { Text("Entrar em '${sala.nome}'", fontFamily = FontFamily(Font(R.font.baloo_bhai)), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column {
                OutlinedTextField(
                    value = senhaSalaInput,
                    onValueChange = { senhaSalaInput = it },
                    label = { Text("Senha da Sala", fontFamily = FontFamily(Font(R.font.lexend))) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ACCENT_COLOR, cursorColor = ACCENT_COLOR),
                    singleLine = true,
                    enabled = !showLoading
                )
                if (showLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = ACCENT_COLOR)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (senhaSalaInput.isNotBlank()) {
                        val entrarRequest = EntrarSalaRequest(salaId = sala.id, senha = senhaSalaInput)
                        dataViewModel.entrarSala(entrarRequest)
                    } else {
                        Toast.makeText(context, "Digite a senha.", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !showLoading,
                colors = ButtonDefaults.buttonColors(containerColor = ACCENT_COLOR),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Entrar", fontFamily = FontFamily(Font(R.font.lexend))) }
        },
        dismissButton = {
            TextButton(onClick = { if (!showLoading) onDismiss() }) {
                Text("Cancelar", color = ACCENT_COLOR, fontFamily = FontFamily(Font(R.font.lexend)))
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun RoomItem(
    sala: SalaChatResponse,
    navController: NavController,
    dataViewModel: DataViewModel
) {
    var showEnterPrivateDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Este LaunchedEffect é para navegação APÓS entrar com sucesso (via diálogo ou clique direto em pública)
    // O diálogo em si tem seu próprio LaunchedEffect para feedback imediato.
    val joinRoomStateForNavigation = dataViewModel.usuarioSalaUiState
    LaunchedEffect(joinRoomStateForNavigation) {
        if (joinRoomStateForNavigation is UsuarioSalaUiState.SuccessJoined) {
            val relacao = (joinRoomStateForNavigation as UsuarioSalaUiState.SuccessJoined).relacao
            if (relacao.salaId == sala.id) { // Certifica que o evento é para esta sala
                if (!relacao.isBanido) {
                    navController.navigate("chat/${sala.id}")
                } else {
                    Toast.makeText(context, "Você está banido desta sala.", Toast.LENGTH_LONG).show()
                }
                dataViewModel.resetUsuarioSalaState() // Importante resetar
            }
        } else if (joinRoomStateForNavigation is UsuarioSalaUiState.Error) {
            // Se o erro de "banido" não for tratado no diálogo, pode ser tratado aqui
            if ((joinRoomStateForNavigation as UsuarioSalaUiState.Error).message.contains("banido", ignoreCase = true)) {
                Toast.makeText(context, (joinRoomStateForNavigation as UsuarioSalaUiState.Error).message, Toast.LENGTH_LONG).show()
                dataViewModel.resetUsuarioSalaState()
            }
        }
    }


    if (showEnterPrivateDialog) {
        EnterPrivateRoomDialog(
            sala = sala,
            dataViewModel = dataViewModel,
            onDismiss = { showEnterPrivateDialog = false },
            onSuccessNavigation = { salaId -> navController.navigate("chat/$salaId") }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp) // Espaçamento vertical entre itens
            .clip(RoundedCornerShape(12.dp))
            .background(WAVE_COLOR.copy(alpha = 0.15f)) // Fundo mais sutil para cada item
            .clickable {
                if (!sala.publica) {
                    showEnterPrivateDialog = true
                } else {
                    dataViewModel.entrarSala(EntrarSalaRequest(salaId = sala.id, senha = null))
                }
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = sala.fotoPerfilURL,
            contentDescription = "Ícone da Sala",
            modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(sala.nome, fontWeight = FontWeight.Bold, color = Color.DarkGray, fontFamily = FontFamily(Font(R.font.lexend)), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(if (sala.publica) "Pública" else "Privada", fontSize = 12.sp, color = Color.Gray, fontFamily = FontFamily(Font(R.font.lexend)))
        }
        if (!sala.publica) {
            Icon(painterResource(id = R.drawable.lock_icon), contentDescription = "Sala Privada", tint = Color.Gray.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun MenuDropdown(navController: NavController, authViewModel: AuthViewModel) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { menuExpanded = true }) {
            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White, modifier = Modifier.size(30.dp))
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLowest) // Cor de fundo do menu
        ) {
            DropdownMenuItem(
                text = { Text("Buscar Usuário", fontFamily = FontFamily(Font(R.font.lexend)), color = ACCENT_COLOR) },
                onClick = { menuExpanded = false; navController.navigate("search-user") }
            )
            DropdownMenuItem(
                text = { Text("Meu Perfil", fontFamily = FontFamily(Font(R.font.lexend)), color = ACCENT_COLOR) },
                onClick = { menuExpanded = false; navController.navigate("user-profile") }
            )
            DropdownMenuItem(
                text = { Text("Sair", fontFamily = FontFamily(Font(R.font.lexend)), color = Color.Red) },
                onClick = {
                    menuExpanded = false
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun RoomsScreenHeader(navController: NavController, authViewModel: AuthViewModel) {
    val user = authViewModel.getSavedUser() // Idealmente, observar um State do AuthViewModel

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user?.imageUrl,
            contentDescription = "Meu Perfil",
            modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant).clickable { navController.navigate("user-profile") },
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "CajuTalk",
            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily(Font(R.font.baloo_bhai)), color = Color.White, fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        MenuDropdown(navController, authViewModel)
    }
}

@Composable
fun RoomsScreen(
    navController: NavController,
    dataViewModel: DataViewModel,
    authViewModel: AuthViewModel
) {
    val bottomColor = Color(0xFFFDB361) // Pode vir do tema
    var exibirMinhasSalas by remember { mutableStateOf(true) }
    var mostrarCreateDialog by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    val context = LocalContext.current

    val salaListState = dataViewModel.salaListUiState
    val currentUser = authViewModel.getSavedUser() // Para filtrar "Minhas Salas"

    LaunchedEffect(Unit) {
        dataViewModel.fetchAllSalas() // Busca todas as salas públicas/acessíveis
    }

    val salasFiltradas by remember(salaListState, exibirMinhasSalas, searchText, currentUser) {
        derivedStateOf {
            if (salaListState is SalaUiState.SuccessList) {
                val todasAsSalasAPI = (salaListState as SalaUiState.SuccessList).salas
                val salasParaAbaAtual = if (exibirMinhasSalas) {
                    // Simplificação: mostra salas onde o usuário atual é o criador
                    // Idealmente, a API teria um endpoint para /minhas-salas ou indicaria se é membro
                    todasAsSalasAPI.filter { it.criadorID == currentUser?.id }
                } else {
                    // Para "Explorar", mostra todas as salas (assumindo que fetchAllSalas as retorna)
                    // Você pode querer filtrar aqui para mostrar apenas públicas se a API retornar misturado
                    todasAsSalasAPI //.filter { it.publica }
                }
                if (searchText.isBlank()) {
                    salasParaAbaAtual
                } else {
                    salasParaAbaAtual.filter { it.nome.contains(searchText, ignoreCase = true) }
                }
            } else {
                emptyList()
            }
        }
    }

    val activeSalaState = dataViewModel.activeSalaDetailUiState // Para observar criação de sala
    LaunchedEffect(activeSalaState) {
        if (activeSalaState is SalaUiState.SuccessSalaCreated) {
            Toast.makeText(context, "'${(activeSalaState as SalaUiState.SuccessSalaCreated).sala.nome}' criada!", Toast.LENGTH_SHORT).show()
            dataViewModel.fetchAllSalas() // Recarregar
            dataViewModel.resetActiveSalaDetailState()
            // Opcional: Navegar para a sala criada
            // val newSalaId = (activeSalaState as SalaUiState.SuccessSalaCreated).sala.id
            // navController.navigate("chat/$newSalaId")
        } else if (activeSalaState is SalaUiState.Error) {
            Toast.makeText(context, "Erro: ${(activeSalaState as SalaUiState.Error).message}", Toast.LENGTH_LONG).show()
            dataViewModel.resetActiveSalaDetailState()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(HEADER_TEXT_COLOR, bottomColor)))) {
        Column(modifier = Modifier.fillMaxSize()) {
            RoomsScreenHeader(navController, authViewModel)

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { exibirMinhasSalas = true },
                    colors = ButtonDefaults.buttonColors(containerColor = if (exibirMinhasSalas) ACCENT_COLOR else ACCENT_COLOR.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) { Text("Minhas Salas", color = Color.White, style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily(Font(R.font.lexend)), fontWeight = FontWeight.SemiBold)) }

                Button(
                    onClick = { exibirMinhasSalas = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (!exibirMinhasSalas) ACCENT_COLOR else ACCENT_COLOR.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) { Text("Explorar", color = Color.White, style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily(Font(R.font.lexend)), fontWeight = FontWeight.SemiBold)) }
            }

            Card(
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp), // Cantos arredondados apenas no topo
                colors = CardDefaults.cardColors(containerColor = chatBackgroundColor),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            placeholder = { Text("Buscar salas...", color = ACCENT_COLOR.copy(alpha = 0.7f), fontFamily = FontFamily(Font(R.font.lexend))) },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar", tint = ACCENT_COLOR) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(25.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = ACCENT_COLOR.copy(alpha = 0.4f),
                                focusedBorderColor = ACCENT_COLOR,
                                cursorColor = ACCENT_COLOR,
                            ),
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = FontFamily(Font(R.font.lexend)), color = Color.DarkGray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { mostrarCreateDialog = true },
                            modifier = Modifier.background(ACCENT_COLOR, CircleShape).size(48.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Criar Sala", tint = Color.White)
                        }
                    }

                    if (mostrarCreateDialog) {
                        CreateRoomDialog(authViewModel, dataViewModel, onDismiss = { mostrarCreateDialog = false })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    when (salaListState) {
                        is SalaUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ACCENT_COLOR) }
                        is SalaUiState.SuccessList -> {
                            if (salasFiltradas.isEmpty()) {
                                Text(
                                    text = if (searchText.isNotBlank()) "Nenhuma sala encontrada para '$searchText'."
                                    else if (exibirMinhasSalas) "Você ainda não criou nenhuma sala ou não faz parte de nenhuma."
                                    else "Nenhuma sala pública para explorar no momento. Crie uma!",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxSize().wrapContentHeight(Alignment.CenterVertically).padding(16.dp),
                                    color = Color.Gray,
                                    fontFamily = FontFamily(Font(R.font.lexend))
                                )
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(salasFiltradas, key = { it.id }) { sala ->
                                        RoomItem(sala = sala, navController = navController, dataViewModel = dataViewModel)
                                    }
                                }
                            }
                        }
                        is SalaUiState.Error -> Text("Erro: ${(salaListState as SalaUiState.Error).message}", color = Color.Red, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                        else -> {} // Idle
                    }
                }
            }
        }
    }
}