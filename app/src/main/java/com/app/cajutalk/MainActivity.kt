package com.app.cajutalk;

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CajuTalkApp()
        }
    }
}

@Composable
fun CajuTalkApp() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("cadastro") { CadastroScreen(navController) }
        composable("salas") { SalasScreen(navController) }
        composable("criar_sala") { CriarSalaScreen(navController) }
        composable("chat") { ChatScreen(navController) }
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
    val topColor = Color(0xFFFFD779)
    val bottomColor = Color(0xFFFDB361)
    val waveColor = Color(0xFFFFA95C)
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
                        // Lógica de login
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
    val topColor = Color(0xFFFFD779)
    val bottomColor = Color(0xFFFDB361)
    val waveColor = Color(0xFFFFA95C)
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
                        // Lógica de cadastro
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
                    Text(text = "Já possui uma conta? ",)
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
fun SalasScreen(navController: NavController) {

}

@Composable
fun CriarSalaScreen(navController: NavController) {

}

@Composable
fun ChatScreen(navController: NavController) {

}

@Composable
@Preview
fun Preview(){
    CajuTalkApp()
}