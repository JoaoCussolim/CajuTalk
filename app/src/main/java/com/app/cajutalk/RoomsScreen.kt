package com.app.cajutalk

import android.net.Uri
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
import com.app.cajutalk.ui.theme.ACCENT_COLOR
import com.app.cajutalk.ui.theme.HEADER_TEXT_COLOR

@Composable
fun CreateRoomDialog(onDismiss: () -> Unit, onCreate: (Sala) -> Unit) {
    var nomeSala by remember { mutableStateOf("") }
    var isPrivada by remember { mutableStateOf(false) }
    var senhaSala by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf(mainUser.imageUrl) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if(uri != null) {
            selectedImageUri = uri
            imageUrl = uri.toString()
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
                        model = selectedImageUri ?: imageUrl,
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
                    val novaSala = Sala(
                        nome = nomeSala,
                        membros = listOf(mainUser),
                        senha = senhaSala,
                        imageUrl = imageUrl,
                        mensagens = mutableListOf(),
                        criador = mainUser,
                        privado = isPrivada
                    )
                    onCreate(novaSala)
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
fun EnterPrivateRoomDialog(roomViewModel: DataViewModel, navController: NavController, onDismiss: () -> Unit) {
    var senhaSala by remember { mutableStateOf("") }
    var senhaIncorreta by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Entrar na Sala: ${roomViewModel.estadoSala.sala?.nome}",
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
                    if (senhaSala == roomViewModel.estadoSala.sala?.senha) {
                        navController.navigate("chat")
                        onDismiss()
                    } else {
                        senhaIncorreta = true
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
fun RoomItem(sala: Sala, navController : NavController, roomViewModel: DataViewModel) {
    var mostrarDialogo by remember { mutableStateOf(false) }

    Spacer(modifier = Modifier.height(16.dp))

    if(mostrarDialogo) {
        EnterPrivateRoomDialog(
            roomViewModel,
            navController,
            onDismiss = { mostrarDialogo = false },
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                roomViewModel.estadoSala.sala = sala
                if(sala.privado == true){
                    mostrarDialogo = true
                }else{
                    navController.navigate("chat")
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
                model = sala.imageUrl,
                contentDescription = "Ícone da Sala",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(text = sala.nome, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = FontFamily(Font(R.font.lexend)))
            Text(text = sala.getMembrosToString(), fontSize = 12.sp, color = Color.Gray, fontFamily = FontFamily(Font(R.font.lexend)))
        }
    }
}

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
                    navController.navigate("login")
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
                model = mainUser.imageUrl,
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

@Composable
fun RoomsScreen(navController: NavController, roomViewModel: DataViewModel) {
    val bottomColor = Color(0xFFFDB361)
    var exibirPublicas by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    val salasExplorar = remember {
        mutableStateListOf(
        Sala(
            nome = "Exército de Dragões",
            membros = listOf(antares),
            senha = "",
            imageUrl = "https://rodoinside.com.br/wp-content/uploads/2015/12/sopro-do-dragao.jpg",
            mensagens = mutableListOf(),
            criador = antares,
            privado = false,
        ),
        Sala(
            nome = "Exército de Pokémon",
            membros = listOf(antares),
            senha = "",
            imageUrl = "https://archives.bulbagarden.net/media/upload/2/28/Arceus_Adventures.png",
            mensagens = mutableListOf(),
            criador = antares,
            privado = false,
        ),
        Sala(
            nome = "Exército de Banana",
            membros = listOf(antares),
            senha = "",
            imageUrl = "https://cdn.pixabay.com/photo/2016/10/27/09/45/banana-1773796_1280.png",
            mensagens = mutableListOf(),
            criador = antares,
            privado = false,
        ),
        Sala(
            nome = "Exército Genérico",
            membros = listOf(antares),
            senha = "",
            imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSnXxaw9sq2phxTmVK8kJb-bMOOj6HTb_TXLQ&s",
            mensagens = mutableListOf(),
            criador = antares,
            privado = false,
        )
    )
    }
    val salasUsuario = remember {
        mutableStateListOf(
        Sala(
            nome = "Exército de Sombras",
            membros = listOf(mainUser, beru, igris, bellion),
            senha = "",
            imageUrl = "https://criticalhits.com.br/wp-content/uploads/2025/01/Solo-Leveling-Reawakening-Movie-696x392.jpg",
            mensagens = mutableListOf(),
            criador = mainUser,
            privado = false,
        ),
        Sala(
            nome = "Exército Escondido \uD83D\uDE08",
            membros = listOf(mainUser, chaHaeIn),
            senha = "amor123",
            imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTf3oWVeZdxwlFvz0usloJnSvUqR_xee4G6zQ&s",
            mensagens = mutableListOf(),
            criador = mainUser,
            privado = true,
        )
    )
    }

    var searchText by remember { mutableStateOf("") }

    val salasFiltradas by remember {
        derivedStateOf {
            val salas = if (exibirPublicas) salasExplorar else salasUsuario
            salas.filter { it.nome.contains(searchText, ignoreCase = true) }
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
            RoomsScreenHeader(navController)

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
                            onCreate = { sala ->
                                salasUsuario.add(sala)
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
                            tint = Color(0xFFFF7094),
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
                            textStyle = TextStyle(color = Color(0xFFFF7094), fontFamily = FontFamily(Font(R.font.lexend))),
                            cursorBrush = SolidColor(Color(0xFFFF7094)),
                            decorationBox = {innerTextField ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ){
                                    if (searchText.isEmpty()){
                                        Text(
                                            text = "Digite aqui...",
                                            color = Color(0xFFFF7094),
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
                        RoomItem(sala = sala, navController = navController, roomViewModel = roomViewModel)
                    }
                }
            }
        }
    }
}