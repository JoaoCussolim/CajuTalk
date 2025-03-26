package com.app.cajutalk

import android.app.Activity
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import okhttp3.internal.concurrent.formatDuration
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.round

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CajuTalkApp()

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        } }
}

var mainUser = User(login = "Eba", senha = "Eba123", name = "Sung Jin Woo", imageUrl = "https://cdn-images.dzcdn.net/images/cover/52634551c3ae630fb3f0b86b6eaed4a0/0x1900-000000-80-0-0.jpg")
var secondUser = User(login = "InimigoDoEba", senha = "morraeba", name = "Antares", imageUrl = "https://i0.wp.com/ovicio.com.br/wp-content/uploads/2025/02/20250219-antares.webp?resize=555%2C555&ssl=1")

@Composable
fun FocusClearContainer(content: @Composable () -> Unit) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        content()
    }
}

@Composable
fun CajuTalkApp() {
    val navController = rememberNavController()
    val audioRecorderViewModel = AudioRecorderViewModel()

    FocusClearContainer{
        NavHost(navController, startDestination = "cadastro") {
            composable("login") { LoginScreen(navController) }
            composable("cadastro") { CadastroScreen(navController) }
            composable("salas") { SalasScreen(navController) }
            composable("chat/{salaNome}/{salaCriador}/{salaImagem}") { backStackEntry ->
                val salaNomeEncoded = backStackEntry.arguments?.getString("salaNome")
                val salaCriadorEncoded = backStackEntry.arguments?.getString("salaCriador")
                val salaImagemEncoded = backStackEntry.arguments?.getString("salaImagem")

                if (salaNomeEncoded != null && salaCriadorEncoded != null && salaImagemEncoded != null) {
                    val salaNome = URLDecoder.decode(salaNomeEncoded, StandardCharsets.UTF_8.toString())
                    val salaCriador = URLDecoder.decode(salaCriadorEncoded, StandardCharsets.UTF_8.toString())
                    val salaImagem = URLDecoder.decode(salaImagemEncoded, StandardCharsets.UTF_8.toString())

                    ChatScreen(audioRecorderViewModel, navController, salaNome, salaCriador, salaImagem)
                }
            }
            composable("user-profile") { UserProfileScreen(navController) }
        }
    }
}


@Composable
fun WaveBackground(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.clipToBounds()) {
        val width = size.width
        val height = size.height

        val path = Path().apply {
            moveTo(0f, height * 0.4f)

            cubicTo(
                width * 0.25f, height * 0.3f,
                width * 0.75f, height * 0.6f,
                width, height * 0.5f
            )

            lineTo(width, 0f)
            lineTo(0f, 0f)
            close()
        }

        drawPath(path = path, color = color)
    }
}

@Composable
fun LoginScreen(navController: NavController) {
    val topColor = Color(0xFFFF9770)
    val bottomColor = Color(0xFFFDB361)
    val waveColor = Color(0xFFFFD670)
    val accentColor = Color(0xFFFF6F9C)
    val highlightColor = Color(0xFFFF7D4C)

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(topColor, bottomColor),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {

        WaveBackground(
            color = waveColor,
            modifier = Modifier.fillMaxSize()
        )

        Text(
            text = "CajuTalk",
            fontSize = 70.sp,
            fontFamily = FontFamily(Font(R.font.baloo_bhai)),
            fontWeight = FontWeight(400),
            color = Color(0xE5FFFAFA),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Login",
                    fontSize = 40.sp,
                    color = highlightColor,
                    fontWeight = FontWeight(400),
                    fontFamily = FontFamily(Font(R.font.anton))
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = {
                        Text(
                            text = "Nome de ${mainUser.login}",
                            fontSize = 15.sp,
                            fontFamily = FontFamily(Font(R.font.lexend)),
                            fontWeight = FontWeight(700),
                            color = Color(0xFFF08080),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        cursorColor = accentColor
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(
                            text = "Senha",
                            fontSize = 15.sp,
                            fontFamily = FontFamily(Font(R.font.lexend)),
                            fontWeight = FontWeight(700),
                            color = Color(0xFFF08080),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        cursorColor = accentColor
                    ),
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        navController.navigate("salas")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Login",
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.lexend)),
                        fontWeight = FontWeight(700),
                        color = Color(0xFFFFE4EB),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ainda não possui uma conta? ")
                    Text(
                        text = "Cadastre-se",
                        fontSize = 15.sp,
                        fontFamily = FontFamily(Font(R.font.lexend)),
                        color = accentColor,
                        modifier = Modifier.clickable {
                            navController.navigate("cadastro")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CadastroScreen(navController: NavController) {
    val topColor = Color(0xFFFF9770)
    val bottomColor = Color(0xFFFDB361)
    val waveColor = Color(0xFFFFD670)
    val accentColor = Color(0xFFFF6F9C)
    val highlightColor = Color(0xFFFF7D4C)

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(topColor, bottomColor),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {

        WaveBackground(
            color = waveColor,
            modifier = Modifier.fillMaxSize()
        )

        Text(
            text = "CajuTalk",
            fontSize = 70.sp,
            fontFamily = FontFamily(Font(R.font.baloo_bhai)),
            fontWeight = FontWeight(400),
            color = Color(0xE5FFFAFA),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Cadastre-se",
                    fontSize = 40.sp,
                    color = highlightColor,
                    fontWeight = FontWeight(400),
                    fontFamily = FontFamily(Font(R.font.anton))
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = {
                        Text(
                            text = "Nome de usuário",
                            fontSize = 15.sp,
                            fontFamily = FontFamily(Font(R.font.lexend)),
                            fontWeight = FontWeight(700),
                            color = Color(0xFFF08080),
                        )
                            },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        cursorColor = accentColor
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(
                            text = "Senha",
                            fontSize = 15.sp,
                            fontFamily = FontFamily(Font(R.font.lexend)),
                            fontWeight = FontWeight(700),
                            color = Color(0xFFF08080),
                        )
                            },
                    modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        cursorColor = accentColor
                    ),
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = {
                        Text(
                        text = "Confirme a senha",
                        fontSize = 15.sp,
                        fontFamily = FontFamily(Font(R.font.lexend)),
                        fontWeight = FontWeight(700),
                        color = Color(0xFFF08080),
                        )
                            },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        cursorColor = accentColor
                    ),
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        navController.navigate("salas")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Cadastrar",
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.lexend)),
                        fontWeight = FontWeight(700),
                        color = Color(0xFFFFE4EB),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Já possui uma conta? ")
                    Text(
                        text = "Faça login",
                        fontSize = 15.sp,
                        fontFamily = FontFamily(Font(R.font.lexend)),
                        color = accentColor,
                        modifier = Modifier.clickable {
                            navController.navigate("login")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CriarSalaDialog(onDismiss: () -> Unit, onCreate: (Sala) -> Unit) {
    var nomeSala by remember { mutableStateOf("") }
    var isPrivada by remember { mutableStateOf(false) }
    var senhaSala by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf(mainUser.imageUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Criar Nova Sala",
                fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                fontSize = 22.sp,
                color = Color(0xFFFF6F9C),
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
                        model = imageUrl,
                        contentDescription = "Imagem da Sala",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFDDC1)),
                        contentScale = ContentScale.Crop
                    )

                    IconButton(
                        onClick = { /* Implementar ação para escolher imagem */ },
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
                        focusedBorderColor = Color(0xFFFF6F9C),
                        cursorColor = Color(0xFFFF6F9C)
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
                            focusedBorderColor = Color(0xFFFF6F9C),
                            cursorColor = Color(0xFFFF6F9C)
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
                        membros = "Usuário",
                        senha = if (isPrivada) senhaSala else "",
                        imageUrl = imageUrl,
                        mensagens = mutableListOf(),
                        criador = mainUser,
                    )
                    onCreate(novaSala)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Criar", color = Color.White)
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
fun SalaItem(sala: Sala, navController : NavController) {
    val salaNomeEncoded = URLEncoder.encode(sala.nome, StandardCharsets.UTF_8.toString())
    val salaCriadorEncoded = URLEncoder.encode(sala.criador.name, StandardCharsets.UTF_8.toString())
    val salaImagemEncoded = URLEncoder.encode(sala.imageUrl, StandardCharsets.UTF_8.toString())

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {navController.navigate("chat/${salaNomeEncoded}/${salaCriadorEncoded}/${salaImagemEncoded}")},
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
            Text(text = sala.membros, fontSize = 12.sp, color = Color.Gray, fontFamily = FontFamily(Font(R.font.lexend)))
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
fun SalasScreen(navController: NavController) {
    val topColor = Color(0xFFFF9770)
    val bottomColor = Color(0xFFFDB361)
    var exibirPublicas by remember { mutableStateOf(false) }
    val headerSpace = 32.dp
    var mostrarDialogo by remember { mutableStateOf(false) }
    val salasExplorar = remember {mutableStateListOf(
        Sala(
            nome = "Exército de Dragões",
            membros = "Dragão, Dragãozão, Dragãozinho",
            senha = "",
            imageUrl = "https://rodoinside.com.br/wp-content/uploads/2015/12/sopro-do-dragao.jpg",
            mensagens = mutableListOf(),
            criador = secondUser
        ),
        Sala(
            nome = "Exército de Pokémon",
            membros = "Arceus, Pikachu, Charizard, Pichu",
            senha = "",
            imageUrl = "https://archives.bulbagarden.net/media/upload/2/28/Arceus_Adventures.png",
            mensagens = mutableListOf(),
            criador = secondUser
        ),
        Sala(
            nome = "Exército de Banana",
            membros = "Bananão, Banana, Bananinha, Banano",
            senha = "",
            imageUrl = "https://cdn.pixabay.com/photo/2016/10/27/09/45/banana-1773796_1280.png",
            mensagens = mutableListOf(),
            criador = secondUser
        ),
        Sala(
            nome = "Exército Genérico",
            membros = "Generico, Generica, Gene, Rico",
            senha = "",
            imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSnXxaw9sq2phxTmVK8kJb-bMOOj6HTb_TXLQ&s",
            mensagens = mutableListOf(),
            criador = secondUser
        )
    )}
    val salasUsuario = remember {mutableStateListOf(
        Sala(
            nome = "Exército de Sombras",
            membros = "Beru, Igris, Tusk, Iron",
            senha = "",
            imageUrl = "https://criticalhits.com.br/wp-content/uploads/2025/01/Solo-Leveling-Reawakening-Movie-696x392.jpg",
            mensagens = mutableListOf(),
            criador = mainUser
        ),
        Sala(
            nome = "Exército de Lobisomens",
            membros = "Lobisomem, Lobão, Lobimito, Lobo",
            senha = "",
            imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQHCbqyj7ojzt5q9CAAWFsgHKf37qgqbQNReA&s",
            mensagens = mutableListOf(),
            criador = mainUser
        )
    )}

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
                    colors = listOf(topColor, bottomColor),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
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
                        modifier = Modifier.fillMaxSize().clickable{ navController.navigate("user-profile") },
                        contentScale = ContentScale.Crop,
                    )
                }

                Spacer(modifier = Modifier.width(headerSpace))

                Text(
                    text = "CajuTalk",
                    fontSize = 40.sp,
                    fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFFFFFFF),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.width(headerSpace))

                MenuDropdown(navController)
            }

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
                            fontWeight = FontWeight(400))
                    }
                    if (mostrarDialogo) {
                        CriarSalaDialog(
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
                        SalaItem(sala = sala, navController = navController)
                    }
                }
            }
        }
    }
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
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

fun roundIfGreaterThanOrEqualToNine(value: Float): Int {
    return if (value >= 0.9) {
        kotlin.math.ceil(value).toInt()
    } else {
        value.toInt()
    }
}

@Composable
fun AudioBubble(audioPath: String, sender: User, showProfile: Boolean) {
    val context = LocalContext.current
    val audioPlayer = remember { AudioPlayer() }
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf("00:00") }
    var currentTime by remember { mutableStateOf("00:00") }
    var totalDurationMs by remember { mutableStateOf(1L) } // Para evitar divisão por zero

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
        if (file.exists()) {
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(audioPath)
                prepare()
                isPlaying = false
            }
            totalDurationMs = mediaPlayer.duration.toLong()
            duration = formatAudioDuration(totalDurationMs)
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            audioPlayer.setProgressListener { currentPosition ->
                currentTime = formatAudioDuration(currentPosition.toLong())

                progress = currentPosition.toFloat() / totalDurationMs.toFloat()
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
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
                            if (isPlaying) {
                                audioPlayer.pauseAudio()
                            } else {
                                if (roundIfGreaterThanOrEqualToNine(progress) >= 1) {
                                    progress = 0f
                                    audioPlayer.seekTo(0L)
                                }
                                println("Progresso $progress")
                                audioPlayer.playAudio(context, audioPath) { isPlaying = false }

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

@Composable
fun ChatScreen(viewModel: AudioRecorderViewModel, navController: NavController, salaNome: String, salaCriador: String?, salaImagem: String?) {
    val topColor = Color(0xFFFF9770)
    val bottomColor = Color(0xFFFDB361)

    var message by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Triple<String, User, String>>() }

    val context = LocalContext.current
    val activity = context as? Activity
    var isRecording by remember { mutableStateOf(false) }

    fun sendMessage(text: String, sender: User, messageType: String) {
        if (text.isBlank()) return

        messages.add(Triple(text, sender, messageType))

        if (sender == mainUser) {
            messages.add(Triple("Vou te matar!", secondUser, "text"))
            messages.add(Triple("Ou você é o sung jin woo? \uD83D\uDE28", secondUser, "text"))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(topColor, bottomColor),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(vertical = 16.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                    contentDescription = "Sair",
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                        .size(62.dp)
                        .clickable { navController.navigate("salas") },
                    tint = Color(0xFFFF5313)
                )
                Box(
                    modifier = Modifier
                        .size(83.dp)
                        .clip(CircleShape)
                        .background(color = Color(0xFFFFD670))
                        .align(Alignment.CenterVertically)
                ) {
                    AsyncImage(
                        model = salaImagem,
                        contentDescription = "Ícone da Sala",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    Modifier.align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = salaNome,
                        fontSize = 30.sp,
                        fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFFFF5313),
                        lineHeight = 24.sp,
                    )
                    Text(
                        text = "Criada por: $salaCriador",
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                        fontWeight = FontWeight(400),
                        color = Color(0xE5FFD670),
                        modifier = Modifier.offset(y = (-12).dp)
                    )
                }
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Menu",
                    tint = Color(0xFFFF5313),
                    modifier = Modifier
                        .padding(vertical = 28.dp, horizontal = 16.dp)
                        .size(40.dp)
                )
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
                        IconButton(onClick = { /* Lógica para anexos */ }) {
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
                                focusedContainerColor = Color(0xFFFFD670),
                                unfocusedContainerColor = Color(0xFFFFD670),
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF7090))
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            if (message.isNotBlank()) {
                                                sendMessage(message, mainUser, "text")
                                                message = ""
                                            }
                                        },
                                        onPress = {
                                            val pressStartTime = System.currentTimeMillis()
                                            tryAwaitRelease()

                                            val pressDuration  = System.currentTimeMillis() - pressStartTime
                                            if (pressDuration >= 500L) {
                                                if (viewModel.hasPermissions(context)) {
                                                    viewModel.startRecording(context)
                                                } else {
                                                    activity?.let { viewModel.requestPermissions(it) }
                                                }
                                                isRecording = true
                                            }

                                            awaitRelease()

                                            viewModel.stopRecording()
                                            viewModel.audioPath?.let {
                                                if (File(it).exists()) {
                                                    sendMessage(it, mainUser, "audio")
                                                } else {
                                                    println("Arquivo de áudio não foi criado corretamente: $it")
                                                }
                                            }
                                            isRecording = false
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
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

@Composable
fun ColorPicker(selectedColor: Color, onColorChanged: (Color) -> Unit) {
    var red by remember { mutableIntStateOf((selectedColor.red * 255).toInt()) }
    var green by remember { mutableIntStateOf((selectedColor.green * 255).toInt()) }
    var blue by remember { mutableIntStateOf((selectedColor.blue * 255).toInt()) }
    var textColor by remember { mutableStateOf("$red, $green, $blue") }
    val accentColor = Color(0xFFFF6F9C)

    fun updateColorFromText(input: String) {
        val values = input.split(",").map { it.trim().toIntOrNull() ?: 0 }
        if (values.size == 3) {
            red = values[0].coerceIn(0, 255)
            green = values[1].coerceIn(0, 255)
            blue = values[2].coerceIn(0, 255)
            onColorChanged(Color(red / 255f, green / 255f, blue / 255f))
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(red / 255f, green / 255f, blue / 255f), shape = RoundedCornerShape(8.dp))
                .border(2.dp, Color.Black, shape = RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = textColor,
            onValueChange = { input ->
                textColor = input
                updateColorFromText(input)
            },
            label = { Text(text = "RGB", color = accentColor)},
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                cursorColor = accentColor,
                unfocusedBorderColor = accentColor,
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Vermelho: $red", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily(Font(R.font.lexend)), color = Color.Black)
        Slider(
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFF08080),
                activeTrackColor = Color(0xFFFF7094),
                inactiveTrackColor = Color(0xFFDDDDDD)
            ),
            value = red.toFloat(),
            onValueChange = { red = it.toInt(); textColor = "$red, $green, $blue"; onColorChanged(Color(red / 255f, green / 255f, blue / 255f)) },
            valueRange = 0f..255f
        )

        Text("Verde: $green", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily(Font(R.font.lexend)), color = Color.Black)
        Slider(
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFF08080),
                activeTrackColor = Color(0xFFFF7094),
                inactiveTrackColor = Color(0xFFDDDDDD)
            ),
            value = green.toFloat(),
            onValueChange = { green = it.toInt(); textColor = "$red, $green, $blue"; onColorChanged(Color(red / 255f, green / 255f, blue / 255f)) },
            valueRange = 0f..255f
        )

        Text("Azul: $blue", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily(Font(R.font.lexend)), color = Color.Black)
        Slider(
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFF08080),
                activeTrackColor = Color(0xFFFF7094),
                inactiveTrackColor = Color(0xFFDDDDDD)
            ),
            value = blue.toFloat(),
            onValueChange = { blue = it.toInt(); textColor = "$red, $green, $blue"; onColorChanged(Color(red / 255f, green / 255f, blue / 255f)) },
            valueRange = 0f..255f
        )
    }
}

@Composable
fun ColorPickerButton(selectedColor: Color, onColorSelected: (Color) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var tempColor by remember { mutableStateOf(selectedColor) }

    Box(
        modifier = Modifier
            .size(50.dp)
            .background(tempColor, shape = RoundedCornerShape(8.dp))
            .border(2.dp, Color.Black, shape = RoundedCornerShape(8.dp))
            .clickable { showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Escolha uma cor", fontFamily = FontFamily(Font(R.font.baloo_bhai))) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ColorPicker(
                        selectedColor = tempColor,
                        onColorChanged = { color -> tempColor = color }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onColorSelected(tempColor)
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F9C))
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF08080))
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun UserProfileScreen(navController: NavController) {
    val backgroundColor = Color(0xFFFBE5B0)
    val accentColor = Color(0xFFFF6F9C)
    var name by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color.White) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
            contentDescription = "Sair",
            modifier = Modifier
                .padding(16.dp)
                .size(40.dp)
                .clickable { navController.navigate("salas") },
            tint = Color(0xFFFFAA80)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Olá, Usuário",
                fontSize = 30.sp,
                fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                fontWeight = FontWeight(700),
                color = Color(0xFFFF9770)
            )

            Box(
                modifier = Modifier
                    .size(140.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                AsyncImage(
                    model = mainUser.imageUrl,
                    contentDescription = "Ícone do Usuário",
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFDDC1)),
                    contentScale = ContentScale.Crop
                )

                IconButton(
                    onClick = { /* Ação para trocar a imagem */ },
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFFFF80AB), shape = CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.camera_icon),
                        contentDescription = "Escolher imagem",
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x9EFFFAFA)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(550.dp)
                .align(alignment = Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Pessoal",
                    fontSize = 28.sp,
                    fontFamily = FontFamily(Font(R.font.anton)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFFFD670),
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Nome",
                    fontSize = 24.sp,
                    fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFF08080),
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text(
                            text = "",
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.lexend)),
                            fontWeight = FontWeight(700),
                            color = Color(0xFFF08080),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        cursorColor = accentColor
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Recado",
                    fontSize = 24.sp,
                    fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFF08080),
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = {
                        Text(
                            text = "",
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.lexend)),
                            fontWeight = FontWeight(700),
                            color = Color(0xFFF08080),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        cursorColor = accentColor
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Personalização",
                    fontSize = 28.sp,
                    fontFamily = FontFamily(Font(R.font.anton)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFFFD670),
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Cor de Fundo",
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFF08080),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ColorPickerButton(selectedColor) { newColor ->
                        selectedColor = newColor
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Text("RGB Atual", fontSize = 15.sp, fontWeight = FontWeight(400), color = Color(0xFFF08080), fontFamily = FontFamily(Font(R.font.lexend)))
                }
            }
        }
    }
}

@Composable
@Preview
fun Preview(){
    CajuTalkApp()
}