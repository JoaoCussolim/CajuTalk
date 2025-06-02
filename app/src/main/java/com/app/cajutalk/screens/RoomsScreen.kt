// app/src/main/java/com/app/cajutalk/screens/RoomsScreen.kt
package com.app.cajutalk.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.app.cajutalk.R
import com.app.cajutalk.network.models.SalaChatDto
import com.app.cajutalk.network.models.SalaCreateDto
import com.app.cajutalk.network.models.EntrarSalaDto // Import EntrarSalaDto
import com.app.cajutalk.ui.theme.ACCENT_COLOR
import com.app.cajutalk.ui.theme.HEADER_TEXT_COLOR
import com.app.cajutalk.viewmodels.DataViewModel
import com.app.cajutalk.viewmodels.SalaViewModel
import com.app.cajutalk.viewmodels.UserViewModel
import com.app.cajutalk.viewmodels.AuthViewModel // Import AuthViewModel
import androidx.compose.runtime.livedata.observeAsState


@Composable
fun CreateRoomDialog(onDismiss: () -> Unit, onCreate: (SalaCreateDto) -> Unit) {
    var nomeSala by remember { mutableStateOf("") }
    var isPrivada by remember { mutableStateOf(false) }
    var senhaSala by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") } // Start with empty string for API
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if(uri != null) {
            selectedImageUri = uri
            imageUrl = uri.toString() // For now, storing URI as string for display
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Criar Nova Sala",
                fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                fontSize = 22.sp,
                color = ACCENT_COLOR,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    AsyncImage(
                        model = selectedImageUri ?: R.drawable.placeholder_image, // Use a default drawable if no image selected
                        contentDescription = "Imagem da Sala",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFDDC1)),
                        contentScale = ContentScale.Crop
                    )

                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFF80AB), shape = CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.camera_icon),
                            contentDescription = "Escolher imagem",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nomeSala,
                    onValueChange = { nomeSala = it },
                    label = {
                        Text(
                            text = "Nome da Sala",
                            fontFamily = FontFamily(Font(R.font.lexend)),
                            color = Color(0xFFF08080)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ACCENT_COLOR,
                        cursorColor = ACCENT_COLOR
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Privada", fontFamily = FontFamily(Font(R.font.lexend)))
                    Checkbox(
                        checked = isPrivada,
                        onCheckedChange = { isPrivada = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFF7094))
                    )
                }

                if (isPrivada) {
                    OutlinedTextField(
                        value = senhaSala,
                        onValueChange = { senhaSala = it },
                        label = { Text(text = "Senha", color = Color(0xFFF08080)) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ACCENT_COLOR,
                            cursorColor = ACCENT_COLOR
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7094)),
                onClick = {
                    val newSalaDto = SalaCreateDto(
                        Nome = nomeSala,
                        Publica = !isPrivada, // Assuming privada means not public
                        Senha = if (isPrivada) senhaSala else null,
                        FotoPerfilURL = imageUrl.ifEmpty { null } // Send null if empty
                    )
                    onCreate(newSalaDto)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Criar", color = Color.White, fontFamily = FontFamily(Font(R.font.lexend)))
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7094)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar", color = Color.White)
            }
        }
    )
}

@Composable
fun EnterPrivateRoomDialog(roomViewModel: DataViewModel, salaViewModel: SalaViewModel, navController: NavController, onDismiss: () -> Unit) {
    var senhaSala by remember { mutableStateOf("") }
    var senhaIncorreta by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val entrarSalaResult by salaViewModel.entrarSalaResult.observeAsState()

    entrarSalaResult?.let { result ->
        if (result.isSuccess) {
            Toast.makeText(context, "Entrou na sala com sucesso!", Toast.LENGTH_SHORT).show()
            navController.navigate("chat")
            onDismiss()
        } else {
            senhaIncorreta = true
            val errorMessage = result.exceptionOrNull()?.message ?: "Erro desconhecido ao entrar na sala."
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Entrar na Sala: ${roomViewModel.estadoSala.sala?.Nome}",
                fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                fontSize = 22.sp,
                color = ACCENT_COLOR,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (senhaIncorreta) {
                    Text(
                        text = "Senha incorreta. Tente novamente.",
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.lexend)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                OutlinedTextField(
                    value = senhaSala,
                    onValueChange = {
                        senhaSala = it
                        senhaIncorreta = false // limpa o erro quando começa a digitar novamente
                    },
                    label = { Text(text = "Senha", color = Color(0xFFF08080)) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ACCENT_COLOR,
                        cursorColor = ACCENT_COLOR
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7094)),
                onClick = {
                    val salaId = roomViewModel.estadoSala.sala?.ID
                    if (salaId != null) {
                        salaViewModel.entrarSala(EntrarSalaDto(SalaId = salaId, Senha = senhaSala))
                    } else {
                        Toast.makeText(context, "Erro: ID da sala não encontrado.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entrar", color = Color.White, fontFamily = FontFamily(Font(R.font.lexend)))
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7094)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar", color = Color.White)
            }
        }
    )
}

@Composable
fun RoomItem(sala: SalaChatDto, navController : NavController, roomViewModel: DataViewModel, salaViewModel: SalaViewModel) {
    var mostrarDialogo by remember { mutableStateOf(false) }

    Spacer(modifier = Modifier.height(16.dp))

    if(mostrarDialogo) {
        EnterPrivateRoomDialog(
            roomViewModel,
            salaViewModel,
            navController,
            onDismiss = { mostrarDialogo = false },
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                roomViewModel.estadoSala = roomViewModel.estadoSala.copy(sala = sala)
                if(sala.Publica){ // Assuming Publica means not private
                    navController.navigate("chat")
                }else{
                    mostrarDialogo = true
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
        ) {
            AsyncImage(
                model = sala.FotoPerfilURL,
                contentDescription = "Ícone da Sala",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(text = sala.Nome, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = FontFamily(Font(
                R.font.lexend
            )))
            // Note: The SalaChatDto from API doesn't have a direct 'membros' list or 'getMembrosToString'
            // You might need to fetch members separately or update the DTO if this info is needed here.
            // For now, let's just display a placeholder or omit this line if not available.
            // Text(text = sala.getMembrosToString(), fontSize = 12.sp, color = Color.Gray, fontFamily = FontFamily(Font(R.font.lexend)))
        }
    }
}

@Composable
fun MenuDropdown(navController: NavController, authViewModel: AuthViewModel) {
    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                    authViewModel.logout()
                    Toast.makeText(context, "Sessão encerrada.", Toast.LENGTH_SHORT).show()
                    navController.navigate("login") {
                        popUpTo("salas") { inclusive = true } // Clear back stack
                    }
                }
            )
        }
    }
}

@Composable
fun RoomsScreenHeader(navController: NavController, userViewModel: UserViewModel, dataViewModel: DataViewModel, authViewModel: AuthViewModel) { // Added dataViewModel and AuthViewModel
    val currentUserDetails by userViewModel.currentUserDetails.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        userViewModel.getCurrentUserDetails()
    }

    // Update dataViewModel.usuarioLogado based on fetched details
    currentUserDetails?.let { result ->
        if (result.isSuccess) {
            dataViewModel.usuarioLogado = result.getOrNull()
        } else {
            Toast.makeText(context, "Erro ao carregar perfil: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            // Optionally, log out if current user details cannot be fetched
            authViewModel.logout()
            navController.navigate("login") {
                popUpTo("salas") { inclusive = true }
            }
        }
    }

    val loggedInUser = dataViewModel.usuarioLogado

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
                model = loggedInUser?.FotoPerfilURL?.ifEmpty { R.drawable.placeholder_image } ?: R.drawable.placeholder_image, // Use placeholder if URL is empty or user is null
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
        MenuDropdown(navController, authViewModel) // Pass AuthViewModel
    }
}

@Composable
fun RoomsScreen(navController: NavController, dataViewModel: DataViewModel) {
    val bottomColor = Color(0xFFFDB361)
    var exibirPublicas by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    val salaViewModel: SalaViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current

    val allSalasResult by salaViewModel.allSalas.observeAsState()
    val createSalaResult by salaViewModel.createSalaResult.observeAsState()

    val publicRooms = remember { mutableStateListOf<SalaChatDto>() }
    val userRooms = remember { mutableStateListOf<SalaChatDto>() }

    LaunchedEffect(Unit) {
        salaViewModel.getAllSalas()
    }

    allSalasResult?.let { result ->
        if (result.isSuccess) {
            val fetchedSalas = result.getOrNull() ?: emptyList()
            publicRooms.clear()
            userRooms.clear()
            val currentUserId = dataViewModel.usuarioLogado?.ID // Get current user ID
            publicRooms.addAll(fetchedSalas.filter { it.Publica })
            // Filter user rooms based on CriadorID
            userRooms.addAll(fetchedSalas.filter { !it.Publica && it.CriadorID == currentUserId })
        } else {
            Toast.makeText(context, "Erro ao carregar salas: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
        }
    }

    createSalaResult?.let { result ->
        if (result.isSuccess) {
            Toast.makeText(context, "Sala criada com sucesso!", Toast.LENGTH_SHORT).show()
            salaViewModel.getAllSalas() // Refresh list after creation
        } else {
            Toast.makeText(context, "Erro ao criar sala: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
        }
    }

    var searchText by remember { mutableStateOf("") }

    val salasFiltradas by remember {
        derivedStateOf {
            val salas = if (exibirPublicas) publicRooms else userRooms
            salas.filter { it.Nome.contains(searchText, ignoreCase = true) }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            RoomsScreenHeader(navController, userViewModel, dataViewModel, authViewModel) // Pass dataViewModel and AuthViewModel

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { exibirPublicas = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!exibirPublicas) Color(0xE5FFD670) else Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = "Minhas Salas",
                        fontSize = 25.sp,
                        fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                        fontWeight = FontWeight(400),
                        color = if (!exibirPublicas) Color(0xFFFFFFFF) else Color(0xFFFF5313)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { exibirPublicas = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (exibirPublicas) Color(0xE5FFD670) else Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = "Explorar",
                        fontSize = 25.sp,
                        fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                        fontWeight = FontWeight(400),
                        color = if (exibirPublicas) Color(0xFFFFFFFF) else Color(0xFFFF5313)
                    )
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { mostrarDialogo = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF7094)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ){
                        Text(
                            text = "Criar sala",
                            fontSize = 20.sp,
                            color = Color(0xFFFFFFFF),
                            fontFamily = FontFamily(Font(R.font.lexend)),
                            fontWeight = FontWeight(400)
                        )
                    }
                    if (mostrarDialogo) {
                        CreateRoomDialog(
                            onDismiss = { mostrarDialogo = false },
                            onCreate = { salaCreateDto ->
                                salaViewModel.createSala(salaCreateDto)
                            }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Pesquisar",
                            tint = ACCENT_COLOR,
                            modifier = Modifier.padding(start = 16.dp)
                        )

                        BasicTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusable()
                                .weight(1f)
                                .padding(start = 8.dp),
                            textStyle = TextStyle(color = ACCENT_COLOR, fontFamily = FontFamily(Font(
                                R.font.lexend
                            ))),
                            cursorBrush = SolidColor(ACCENT_COLOR),
                            decorationBox = {innerTextField ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ){
                                    if (searchText.isEmpty()){
                                        Text(
                                            text = "Digite aqui...",
                                            color = ACCENT_COLOR,
                                            fontFamily = FontFamily(Font(R.font.lexend)),
                                            modifier = Modifier.align(Alignment.CenterStart)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                LazyColumn {
                    items(salasFiltradas) { sala ->
                        RoomItem(sala = sala, navController = navController, roomViewModel = dataViewModel, salaViewModel = salaViewModel)
                    }
                }
            }
        }
    }
}