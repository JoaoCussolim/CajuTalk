package com.app.cajutalk.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.app.cajutalk.R
import com.app.cajutalk.network.models.SalaChatDto
import com.app.cajutalk.network.models.SalaCreateDto
import com.app.cajutalk.ui.theme.ACCENT_COLOR
import com.app.cajutalk.ui.theme.HEADER_TEXT_COLOR
import com.app.cajutalk.viewmodels.DataViewModel
import com.app.cajutalk.viewmodels.SalaViewModel

// Alteração: A função agora recebe o SalaViewModel para interagir com a API.
@Composable
fun RoomsScreen(
    navController: NavController,
    dataViewModel: DataViewModel,
    salaViewModel: SalaViewModel
) {
    val bottomColor = Color(0xFFFDB361)
    var exibirPublicas by remember { mutableStateOf(true) } // Iniciar em Explorar
    var mostrarDialogoCriarSala by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    val context = LocalContext.current

    // 1. Executa a chamada à API para buscar as salas quando a tela é iniciada.
    LaunchedEffect(Unit) {
        salaViewModel.getAllSalas()
    }

    // 2. Observa os resultados vindos do ViewModel.
    val salasResult by salaViewModel.allSalas.observeAsState()
    val isLoading by salaViewModel.isLoading.observeAsState(initial = false)
    val createSalaResult by salaViewModel.createSalaResult.observeAsState()

    // Exibe um Toast em caso de sucesso ou falha na criação da sala.
    LaunchedEffect(createSalaResult) {
        createSalaResult?.let { result ->
            result.onSuccess {
                Toast.makeText(context, "Sala '${it.Nome}' criada com sucesso!", Toast.LENGTH_SHORT).show()
                salaViewModel.getAllSalas() // Atualiza a lista de salas
            }
            result.onFailure {
                Toast.makeText(context, "Erro ao criar sala: ${it.message}", Toast.LENGTH_LONG).show()
            }
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
            RoomsScreenHeader(navController)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // Botão "Minhas Salas"
                Button(
                    onClick = { exibirPublicas = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!exibirPublicas) Color(0xE5FFD670) else Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Minhas Salas", fontSize = 25.sp, fontFamily = FontFamily(Font(R.font.baloo_bhai)), color = if (!exibirPublicas) Color.White else Color(0xFFFF5313))
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Botão "Explorar"
                Button(
                    onClick = { exibirPublicas = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (exibirPublicas) Color(0xE5FFD670) else Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Explorar", fontSize = 25.sp, fontFamily = FontFamily(Font(R.font.baloo_bhai)), color = if (exibirPublicas) Color.White else Color(0xFFFF5313))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xE5FFFAFA)),
            modifier = Modifier
                .fillMaxWidth(1f)
                .height(640.dp)
                .align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { mostrarDialogoCriarSala = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7094)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Criar sala", fontSize = 20.sp, color = Color.White, fontFamily = FontFamily(Font(R.font.lexend)))
                    }
                    // 4. Passa o ViewModel para o diálogo de criação de sala.
                    if (mostrarDialogoCriarSala) {
                        CreateRoomDialog(
                            onDismiss = { mostrarDialogoCriarSala = false },
                            salaViewModel = salaViewModel
                        )
                    }
                    // Barra de busca...
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Gerencia o estado da UI com base no resultado da API.
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    salasResult?.let { result ->
                        result.onSuccess { allRooms ->
                            // Filtra as salas com base na aba selecionada e no texto da busca.
                            val filteredRooms = allRooms.filter { sala ->
                                val matchesSearch = sala.Nome.contains(searchText, ignoreCase = true)
                                val matchesTab = if (exibirPublicas) {
                                    sala.Publica
                                } else {
                                    // TODO: Implementar lógica para "Minhas Salas"
                                    // A API atual não informa se o usuário é membro de uma sala nesta chamada.
                                    // O ideal seria um novo endpoint ou filtrar por `sala.CriadorID`.
                                    // Por enquanto, exibiremos todas as salas não públicas aqui.
                                    !sala.Publica
                                }
                                matchesSearch && matchesTab
                            }
                            LazyColumn {
                                items(filteredRooms) { sala ->
                                    RoomItem(sala = sala, navController = navController, dataViewModel = dataViewModel, salaViewModel = salaViewModel)
                                }
                            }
                        }
                        result.onFailure {
                            Text("Erro ao carregar salas: ${it.message}", color = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateRoomDialog(onDismiss: () -> Unit, salaViewModel: SalaViewModel) {
    var nomeSala by remember { mutableStateOf("") }
    var isPrivada by remember { mutableStateOf(false) }
    var senhaSala by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Criar Nova Sala", fontFamily = FontFamily(Font(R.font.baloo_bhai)), color = ACCENT_COLOR, textAlign = TextAlign.Center) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(120.dp).clickable { imagePickerLauncher.launch("image/*") }, contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(model = selectedImageUri ?: R.drawable.camera_icon, contentDescription = "Imagem da Sala", modifier = Modifier.size(120.dp).clip(CircleShape).background(Color(0xFFFFDDC1)), contentScale = ContentScale.Crop)
                    // ... (ícone de câmera)
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = nomeSala, onValueChange = { nomeSala = it }, label = { Text("Nome da Sala") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Privada")
                    Checkbox(checked = isPrivada, onCheckedChange = { isPrivada = it })
                }
                if (isPrivada) {
                    OutlinedTextField(value = senhaSala, onValueChange = { senhaSala = it }, label = { Text("Senha") }, visualTransformation = PasswordVisualTransformation())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Lembrete: A lógica de upload de imagem deve ser tratada aqui.
                    // Por simplicidade, estamos passando a URL como nula.
                    // Você deve primeiro fazer o upload da `selectedImageUri` (se não for nula),
                    // obter a URL e só então criar o DTO.
                    if (nomeSala.isNotBlank()) {
                        val salaCreateDto = SalaCreateDto(
                            Nome = nomeSala,
                            Publica = !isPrivada,
                            Senha = if (isPrivada) senhaSala else null,
                            FotoPerfilURL = imageUrl
                        )
                        salaViewModel.createSala(salaCreateDto)
                        onDismiss()
                    } else {
                        Toast.makeText(context, "O nome da sala não pode ser vazio.", Toast.LENGTH_SHORT).show()
                    }
                }
            ) { Text("Criar") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// 5. O RoomItem agora aceita SalaChatDto, que é o modelo vindo da API.
@Composable
fun RoomItem(
    sala: SalaChatDto,
    navController: NavController,
    dataViewModel: DataViewModel,
    salaViewModel: SalaViewModel
) {
    var mostrarDialogoSenha by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (mostrarDialogoSenha) {
        EnterPrivateRoomDialog(
            sala = sala,
            onDismiss = { mostrarDialogoSenha = false },
            onConfirm = { senha ->
                // Lógica para entrar na sala com senha
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                dataViewModel.estadoSala = dataViewModel.estadoSala.copy(sala = sala)
                if (!sala.Publica) {
                    mostrarDialogoSenha = true
                } else {
                    navController.navigate("chat")
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(model = sala.FotoPerfilURL, contentDescription = "Ícone da Sala", modifier = Modifier.size(50.dp).clip(CircleShape), contentScale = ContentScale.Crop, placeholder = painterResource(id = R.drawable.placeholder_image))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = sala.Nome, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = FontFamily(Font(R.font.lexend)))
            // A API não fornece a lista de membros aqui, então exibimos o criador.
            Text(text = "Criador ID: ${sala.CriadorID}", fontSize = 12.sp, color = Color.Gray, fontFamily = FontFamily(Font(R.font.lexend)))
        }
    }
}

// Diálogo para entrar em sala privada
@Composable
fun EnterPrivateRoomDialog(
    sala: SalaChatDto,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var senhaSala by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Entrar na Sala: ${sala.Nome}") },
        text = {
            OutlinedTextField(
                value = senhaSala,
                onValueChange = { senhaSala = it },
                label = { Text("Senha") },
                visualTransformation = PasswordVisualTransformation()
            )
        },
        confirmButton = { Button(onClick = { onConfirm(senhaSala) }) { Text("Entrar") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancelar") } }
    )
}


// O restante das funções (MenuDropdown, RoomsScreenHeader) podem permanecer como estão.
// ... (Cole o restante do código original de RoomsScreen.kt aqui)
@Composable
fun MenuDropdown(navController: NavController) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box {
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = "Menu",
            tint = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .size(40.dp)
                .clickable { menuExpanded = true }
        )

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier.background(Color(0xE5FFFAFA))
        ) {
            DropdownMenuItem(
                text = { Text(text = "Buscar Perfil", fontFamily = FontFamily(Font(R.font.lexend)), color = Color(0xFFFF7094)) },
                onClick = {
                    menuExpanded = false
                    navController.navigate("search-user")
                }
            )
            DropdownMenuItem(
                text = { Text(text = "Sair", fontFamily = FontFamily(Font(R.font.lexend)), color = Color(0xFFFF7094)) },
                onClick = {
                    menuExpanded = false
                    // Adicione aqui a lógica para logout, por exemplo:
                    // auth.signOut()
                    navController.navigate("login") {
                        // Limpa a pilha de navegação para que o usuário não possa voltar
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun RoomsScreenHeader(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
        ) {
            AsyncImage(
                model = mainUser.imageUrl, // TODO: Substituir por dados do usuário logado
                contentDescription = "Ícone do Usuário",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { navController.navigate("user-profile") },
                contentScale = ContentScale.Crop,
            )
        }
        Spacer(modifier = Modifier.width(32.dp))
        Text(
            text = "CajuTalk",
            fontSize = 40.sp,
            fontFamily = FontFamily(Font(R.font.baloo_bhai)),
            fontWeight = FontWeight(400),
            color = Color.White,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(32.dp))
        MenuDropdown(navController)
    }
}