package com.app.cajutalk.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.app.cajutalk.R
import com.app.cajutalk.ui.theme.ACCENT_COLOR
import com.app.cajutalk.ui.theme.HEADER_TEXT_COLOR
import com.app.cajutalk.ui.theme.WAVE_COLOR
import com.app.cajutalk.viewmodels.AuthViewModel
import com.app.cajutalk.viewmodels.DataViewModel
import com.app.cajutalk.viewmodels.UserViewModel

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
fun AuthHeader() {
    Box(modifier = Modifier.fillMaxSize()) {
        WaveBackground(color = WAVE_COLOR, modifier = Modifier.fillMaxSize())
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
    }
}

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel, userViewModel: UserViewModel,
                dataViewModel: DataViewModel
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var loginTriggered by remember { mutableStateOf(false) }

    val loginResult by authViewModel.loginResult.observeAsState()
    val userDetailsResult by userViewModel.currentUserDetails.observeAsState()
    val isLoadingAuth by authViewModel.isLoading.observeAsState(false)
    val isLoadingUser by userViewModel.isLoading.observeAsState(false)
    val isLoading = isLoadingAuth || isLoadingUser

    LaunchedEffect(loginResult) {
        loginResult?.onSuccess {
            userViewModel.getCurrentUserDetails()
        }
        loginResult?.onFailure { exception ->
            errorMessage = exception.message ?: "Erro desconhecido no login."
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            authViewModel.onLoginResultConsumed()
        }
    }

    LaunchedEffect(userDetailsResult) {
        userDetailsResult?.onSuccess { userDto ->
            // Apenas executa se o login tiver sido disparado para evitar re-navegação
            if(loginResult?.isSuccess == true) {
                dataViewModel.usuarioLogado = userDto // Ponto chave: Armazena o usuário logado
                Toast.makeText(context, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                navController.navigate("salas") {
                    popUpTo("login") { inclusive = true }
                }
                authViewModel.onLoginResultConsumed() // Limpa o evento de login
            }
        }
        userDetailsResult?.onFailure { exception ->
            if(loginResult?.isSuccess == true) {
                errorMessage = "Falha ao carregar dados do usuário: ${exception.message}"
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                authViewModel.onLoginResultConsumed()
            }
        }
    }

    val bottomColor = Color(0xFFFDB361)
    val highlightColor = Color(0xFFFF7D4C)

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

        AuthHeader()

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
                            text = "Nome de usuário",
                            fontSize = 15.sp,
                            fontFamily = FontFamily(Font(R.font.lexend)),
                            fontWeight = FontWeight(700),
                            color = Color(0xFFF08080),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ACCENT_COLOR,
                        cursorColor = ACCENT_COLOR
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
                        focusedBorderColor = ACCENT_COLOR,
                        cursorColor = ACCENT_COLOR
                    ),
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        errorMessage = null
                        authViewModel.login(username, password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ACCENT_COLOR,
                        contentColor = Color.White
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Login",
                            fontSize = 20.sp,
                            fontFamily = FontFamily(Font(R.font.lexend)),
                            fontWeight = FontWeight(700),
                            color = Color(0xFFFFE4EB),
                        )
                    }
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
                        color = ACCENT_COLOR,
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
fun CadastroScreen(navController: NavController, authViewModel: AuthViewModel,userViewModel: UserViewModel,
                   dataViewModel: DataViewModel ) { // Added authViewModel parameter
    val context = LocalContext.current
    var login by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val registerResult by authViewModel.registerResult.observeAsState()
    val userDetailsResult by userViewModel.currentUserDetails.observeAsState()
    val isLoadingAuth by authViewModel.isLoading.observeAsState(false)
    val isLoadingUser by userViewModel.isLoading.observeAsState(false)
    val isLoading = isLoadingAuth || isLoadingUser

    // Efeito 1: Dispara a busca de dados do usuário após registro bem-sucedido
    LaunchedEffect(registerResult) {
        registerResult?.onSuccess {
            userViewModel.getCurrentUserDetails()
        }
        registerResult?.onFailure { exception ->
            errorMessage = exception.message ?: "Erro desconhecido no cadastro."
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            authViewModel.onRegisterResultConsumed()
        }
    }

    // Efeito 2: Salva os dados do usuário e navega após a busca ser concluída
    LaunchedEffect(userDetailsResult) {
        userDetailsResult?.onSuccess { userDto ->
            if (registerResult?.isSuccess == true) {
                dataViewModel.usuarioLogado = userDto
                Toast.makeText(context, "Cadastro bem-sucedido!", Toast.LENGTH_SHORT).show()
                navController.navigate("salas") {
                    popUpTo("cadastro") { inclusive = true }
                }
                authViewModel.onRegisterResultConsumed()
            }
        }
        userDetailsResult?.onFailure { exception ->
            if (registerResult?.isSuccess == true) {
                errorMessage = "Falha ao carregar dados do usuário: ${exception.message}"
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                authViewModel.onRegisterResultConsumed()
            }
        }
    }

    val bottomColor = Color(0xFFFDB361)
    val waveColor = Color(0xFFFFD670)
    val highlightColor = Color(0xFFFF7D4C)

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
                    value = login,
                    onValueChange = { login = it },
                    label = {
                        Text(
                            text = "Login",
                            fontSize = 15.sp,
                            fontFamily = FontFamily(Font(R.font.lexend)),
                            fontWeight = FontWeight(700),
                            color = Color(0xFFF08080),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ACCENT_COLOR,
                        cursorColor = ACCENT_COLOR
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

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
                        focusedBorderColor = ACCENT_COLOR,
                        cursorColor = ACCENT_COLOR
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
                        focusedBorderColor = ACCENT_COLOR,
                        cursorColor = ACCENT_COLOR
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
                        focusedBorderColor = ACCENT_COLOR,
                        cursorColor = ACCENT_COLOR
                    ),
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        if (password != confirmPassword) {
                            errorMessage = "As senhas não coincidem."
                        } else {
                            errorMessage = null
                            authViewModel.register(username, login, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ACCENT_COLOR,
                        contentColor = Color.White
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Cadastrar",
                            fontSize = 20.sp,
                            fontFamily = FontFamily(Font(R.font.lexend)),
                            fontWeight = FontWeight(700),
                            color = Color(0xFFFFE4EB),
                        )
                    }
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
                        color = ACCENT_COLOR,
                        modifier = Modifier.clickable {
                            navController.navigate("login")
                        }
                    )
                }
            }
        }
    }
}