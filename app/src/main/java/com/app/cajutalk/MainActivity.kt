package com.app.cajutalk

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavType
import androidx.navigation.navArgument
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CajuTalkApp()

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        } }
}

var userProfile = "https://cdn-images.dzcdn.net/images/cover/52634551c3ae630fb3f0b86b6eaed4a0/0x1900-000000-80-0-0.jpg"

@Composable
fun CajuTalkApp() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "cadastro") {
        composable("login") { LoginScreen(navController) }
        composable("cadastro") { CadastroScreen(navController) }
        composable("salas") { SalasScreen(navController) }
        composable("salas") { SalasScreen(navController) }
        composable(
            "chat/{salaNome}",
            arguments = listOf(navArgument("salaNome") { type = NavType.StringType })
        ) { backStackEntry ->
            val salaNome = backStackEntry.arguments?.getString("salaNome") ?: ""
            ChatScreen(salaNome, navController)
        }
        composable("user-profile") { UserProfileScreen(navController) }
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
                .fillMaxWidth(0.8f) // Deixa mais estreito
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
                .fillMaxWidth(0.8f) // Deixa mais estreito
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
fun CriarSalaDialog(onDismiss: () -> Unit, onCreate: (Sala) -> Unit){
    var nomeSala by remember { mutableStateOf("") }
    var isPrivada by remember { mutableStateOf(false) }
    var senhaSala by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Criar Nova Sala", fontFamily = FontFamily(Font(R.font.baloo_bhai)))},
        text = {
            Column {
                OutlinedTextField(
                    value = nomeSala,
                    onValueChange = { nomeSala = it },
                    label = {
                        Text(text = "Nome da Sala", fontFamily = FontFamily(Font(R.font.lexend)), color = Color(0xFFF08080))},
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF6F9C),
                        cursorColor = Color(0xFFFF6F9C)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Privada", fontFamily = FontFamily(Font(R.font.lexend)))
                    Checkbox(checked = isPrivada, onCheckedChange = { isPrivada = it }, colors = CheckboxColors(
                        checkedBoxColor = Color(0xFFFF7094),
                        checkedBorderColor = Color(0xFFFF7094),
                        checkedCheckmarkColor = Color.White,

                        uncheckedBoxColor = Color.Transparent,
                        uncheckedBorderColor = Color(0xFFFF7094),
                        uncheckedCheckmarkColor = Color.Transparent,

                        disabledCheckedBoxColor = Color(0xFFFFB3C1),
                        disabledBorderColor = Color.Transparent,

                        disabledUncheckedBoxColor = Color.Transparent,
                        disabledUncheckedBorderColor = Color(0xFFFFB3C1),

                        disabledIndeterminateBoxColor = Color(0xFFFFB3C1),
                        disabledIndeterminateBorderColor = Color(0xFFFFB3C1)
                    ))
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
                        )
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
                        membros = "Usuário, ...",
                        senha = if (isPrivada) senhaSala else "",
                        imageUrl = userProfile,
                        mensagens = mutableListOf()
                    )
                    onCreate(novaSala)
                    onDismiss()
                }
            ) {
                Text("Criar")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7094))
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun SalaItem(sala: Sala, navController : NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {navController.navigate("chat/${sala.nome}")},
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
            Text(text = sala.nome, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = sala.membros, fontSize = 12.sp, color = Color.Gray)
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
    val salasPublicas = remember {mutableStateListOf(
        Sala(
            nome = "Exército de Sombras",
            membros = "Beru, Igris, Tusk, Iron",
            senha = "",
            imageUrl = "https://criticalhits.com.br/wp-content/uploads/2025/01/Solo-Leveling-Reawakening-Movie-696x392.jpg",
            mensagens = mutableListOf()
        ),
        Sala(
            nome = "Exército de Pokémon",
            membros = "Arceus, Pikachu, Charizard, Pichu",
            senha = "",
            imageUrl = "https://archives.bulbagarden.net/media/upload/2/28/Arceus_Adventures.png",
            mensagens = mutableListOf()
        ),
        Sala(
            nome = "Exército de Banana",
            membros = "Bananão, Banana, Bananinha, Banano",
            senha = "",
            imageUrl = "https://cdn.pixabay.com/photo/2016/10/27/09/45/banana-1773796_1280.png",
            mensagens = mutableListOf()
        ),
        Sala(
            nome = "Exército Genérico",
            membros = "Generico, Generica, Gene, Rico",
            senha = "",
            imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSnXxaw9sq2phxTmVK8kJb-bMOOj6HTb_TXLQ&s",
            mensagens = mutableListOf()
        )
    )}
    val salasPrivadas = remember {mutableStateListOf(
        Sala(
            nome = "Exército de Dragões",
            membros = "Dragão, Dragãozão, Dragãozinho",
            senha = "",
            imageUrl = "https://rodoinside.com.br/wp-content/uploads/2015/12/sopro-do-dragao.jpg",
            mensagens = mutableListOf()
        ),
        Sala(
            nome = "Exército de Lobisomens",
            membros = "Lobisomem, Lobão, Lobimito, Lobo",
            senha = "",
            imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQHCbqyj7ojzt5q9CAAWFsgHKf37qgqbQNReA&s",
            mensagens = mutableListOf()
        )
    )}
    val salasExibidas by remember {
        derivedStateOf {
            if (exibirPublicas) salasPublicas else salasPrivadas
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
                        model = userProfile,
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

                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu",
                    tint = Color.White,
                )
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
                                salasPrivadas.add(sala)
                            }
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Pesquisar",
                        tint = Color(0xFFFF7094)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                LazyColumn {
                    items(salasExibidas) { sala ->
                        SalaItem(sala, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatScreen(nomeSala : String, navController: NavController) {

}

@Composable
fun ChatBubble(text: String) {
    Card(
        colors = CardDefaults.cardColors(Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
fun ColorPicker(selectedColor: Color, onColorChanged: (Color) -> Unit) {
    var red by remember { mutableStateOf((selectedColor.red * 255).toInt()) }
    var green by remember { mutableStateOf((selectedColor.green * 255).toInt()) }
    var blue by remember { mutableStateOf((selectedColor.blue * 255).toInt()) }
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

        Text("Vermelho: $red", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily(Font(R.font.lexend)), color = Color.Red)
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

        Text("Verde: $green", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily(Font(R.font.lexend)), color = Color.Green)
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

        Text("Azul: $blue", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily(Font(R.font.lexend)), color = Color.Blue)
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
                    model = userProfile,
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