package com.app.cajutalk

import android.widget.Toast // Para mensagens de validação/erro
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* // Importa tudo de layout
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.* // Importa tudo de material3
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext // Para Toasts
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // Para injetar ViewModel
import androidx.navigation.NavController
import com.app.cajutalk.ui.theme.ACCENT_COLOR
import com.app.cajutalk.ui.theme.HEADER_TEXT_COLOR
import com.app.cajutalk.ui.theme.WAVE_COLOR
import com.app.cajutalk.viewmodels.AuthViewModel // Importar seu ViewModel
import com.app.cajutalk.viewmodels.AuthUiState   // Importar os estados da UI
import com.app.cajutalk.network.dto.LoginRequest   // DTO para Login
import com.app.cajutalk.network.dto.RegisterRequest // DTO para Cadastro

// Constantes para validação (você pode ajustar esses valores)
const val MIN_USERNAME_LENGTH = 3
const val MAX_USERNAME_LENGTH = 30 // Seu RegisterModel.LoginUsuario tem max 30
const val MIN_PASSWORD_LENGTH = 6
const val MAX_PASSWORD_LENGTH = 30 // Defina um máximo razoável para o campo de senha

@Composable
fun WaveBackground(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.clipToBounds()) {
        val width = size.width
        val height = size.height
        val path = Path().apply {
            moveTo(0f, height * 0.4f)
            cubicTo(width * 0.25f, height * 0.3f, width * 0.75f, height * 0.6f, width, height * 0.5f)
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
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel() // Injeta o ViewModel
) {
    val bottomColor = Color(0xFFFDB361)
    val highlightColor = Color(0xFFFF7D4C)
    val context = LocalContext.current

    var loginUsuario by remember { mutableStateOf("") } // Para LoginModel.LoginUsuario
    var senhaUsuario by remember { mutableStateOf("") } // Para LoginModel.SenhaUsuario
    var loginUsuarioError by remember { mutableStateOf<String?>(null) }
    var senhaUsuarioError by remember { mutableStateOf<String?>(null) }

    val authState = authViewModel.authUiState

    LaunchedEffect(authState) {
        when (authState) {
            is AuthUiState.UserProfileLoaded -> {
                Toast.makeText(context, "Login como ${authState.user.name}!", Toast.LENGTH_SHORT).show()
                navController.navigate("salas") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
                authViewModel.resetAuthState()
            }
            is AuthUiState.Error -> {
                Toast.makeText(context, authState.message, Toast.LENGTH_LONG).show()
                authViewModel.resetAuthState()
            }
            else -> Unit
        }
    }

    fun validateLoginFields(): Boolean {
        loginUsuarioError = if (loginUsuario.isBlank()) "Login não pode ser vazio." else null
        senhaUsuarioError = if (senhaUsuario.isBlank()) "Senha não pode ser vazia." else null
        return loginUsuarioError == null && senhaUsuarioError == null
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
                Text("Login", fontSize = 40.sp, color = highlightColor, fontFamily = FontFamily(Font(R.font.anton)))
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = loginUsuario,
                    onValueChange = {
                        loginUsuario = it
                        loginUsuarioError = null // Limpa erro ao digitar
                    },
                    label = { Text("Login de Usuário", fontSize = 15.sp, fontFamily = FontFamily(Font(R.font.lexend)), fontWeight = FontWeight(700), color = Color(0xFFF08080)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = loginUsuarioError != null,
                    supportingText = { loginUsuarioError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ACCENT_COLOR, cursorColor = ACCENT_COLOR)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = senhaUsuario,
                    onValueChange = {
                        senhaUsuario = it
                        senhaUsuarioError = null // Limpa erro ao digitar
                    },
                    label = { Text("Senha", fontSize = 15.sp, fontFamily = FontFamily(Font(R.font.lexend)), fontWeight = FontWeight(700), color = Color(0xFFF08080)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = senhaUsuarioError != null,
                    supportingText = { senhaUsuarioError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ACCENT_COLOR, cursorColor = ACCENT_COLOR)
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (authState is AuthUiState.Loading) {
                    CircularProgressIndicator(color = ACCENT_COLOR, modifier = Modifier.size(45.dp))
                } else {
                    Button(
                        onClick = {
                            if (validateLoginFields()) {
                                authViewModel.login(LoginRequest(loginUsuario, senhaUsuario))
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(45.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ACCENT_COLOR, contentColor = Color.White)
                    ) {
                        Text("Login", fontSize = 20.sp, fontFamily = FontFamily(Font(R.font.lexend)), fontWeight = FontWeight(700), color = Color(0xFFFFE4EB))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ainda não possui uma conta? ")
                    Text("Cadastre-se", fontSize = 15.sp, fontFamily = FontFamily(Font(R.font.lexend)), color = ACCENT_COLOR, modifier = Modifier.clickable { navController.navigate("cadastro") })
                }
            }
        }
    }
}

@Composable
fun CadastroScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel() // Injeta o ViewModel
) {
    val bottomColor = Color(0xFFFDB361)
    val waveColor = Color(0xFFFFD670)
    val highlightColor = Color(0xFFFF7D4C)
    val context = LocalContext.current

    var loginUsuario by remember { mutableStateOf("") } // Para RegisterModel.LoginUsuario
    var senhaUsuario by remember { mutableStateOf("") } // Para RegisterModel.SenhaUsuario
    var confirmaSenhaUsuario by remember { mutableStateOf("") }

    var nomeUsuarioError by remember { mutableStateOf<String?>(null) }
    var loginUsuarioError by remember { mutableStateOf<String?>(null) }
    var senhaUsuarioError by remember { mutableStateOf<String?>(null) }
    var confirmaSenhaError by remember { mutableStateOf<String?>(null) }

    val authState = authViewModel.authUiState

    LaunchedEffect(authState) {
        when (authState) {
            is AuthUiState.Success -> { // Após registro bem-sucedido (API retorna tokens)
                // Toast já mostrado pelo ViewModel
                navController.navigate("login") {
                    popUpTo("login") { inclusive = true }
                }
                authViewModel.resetAuthState()
            }
            is AuthUiState.Error -> {
                Toast.makeText(context, authState.message, Toast.LENGTH_LONG).show()
                authViewModel.resetAuthState()
            }
            else -> Unit
        }
    }

    fun validateRegistrationFields(): Boolean {
        loginUsuarioError = when {
            loginUsuario.isBlank() -> "Login não pode ser vazio."
            loginUsuario.length < MIN_USERNAME_LENGTH -> "Login deve ter no mínimo $MIN_USERNAME_LENGTH caracteres."
            loginUsuario.length > MAX_USERNAME_LENGTH -> "Login deve ter no máximo $MAX_USERNAME_LENGTH caracteres."
            else -> null
        }
        senhaUsuarioError = when {
            senhaUsuario.isBlank() -> "Senha não pode ser vazia."
            senhaUsuario.length < MIN_PASSWORD_LENGTH -> "Senha deve ter no mínimo $MIN_PASSWORD_LENGTH caracteres."
            senhaUsuario.length > MAX_PASSWORD_LENGTH -> "Senha deve ter no máximo $MAX_PASSWORD_LENGTH caracteres."
            else -> null
        }
        confirmaSenhaError = when {
            confirmaSenhaUsuario.isBlank() -> "Confirmação de senha não pode ser vazia."
            senhaUsuario != confirmaSenhaUsuario -> "As senhas não coincidem."
            else -> null
        }
        return nomeUsuarioError == null && loginUsuarioError == null && senhaUsuarioError == null && confirmaSenhaError == null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(HEADER_TEXT_COLOR, bottomColor)))
    ) {
        WaveBackground(color = waveColor, modifier = Modifier.fillMaxSize())
        Text("CajuTalk", fontSize = 70.sp, fontFamily = FontFamily(Font(R.font.baloo_bhai)), color = Color(0xE5FFFAFA), modifier = Modifier.align(Alignment.TopCenter).padding(top = 60.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth(0.8f).align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Cadastre-se", fontSize = 40.sp, color = highlightColor, fontFamily = FontFamily(Font(R.font.anton)))
                Spacer(modifier = Modifier.height(16.dp))

                // Campo Nome Completo (NomeUsuario na API
                // Campo Login de Usuário (LoginUsuario na API)
                OutlinedTextField(
                    value = loginUsuario,
                    onValueChange = {
                        loginUsuario = if (it.length <= MAX_USERNAME_LENGTH) it else it.substring(0, MAX_USERNAME_LENGTH)
                        loginUsuarioError = null
                    },
                    label = { Text("Login de Usuário", fontSize = 15.sp, fontFamily = FontFamily(Font(R.font.lexend)), fontWeight = FontWeight(700), color = Color(0xFFF08080)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = loginUsuarioError != null,
                    supportingText = { loginUsuarioError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ACCENT_COLOR, cursorColor = ACCENT_COLOR)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Campo Senha (SenhaUsuario na API)
                OutlinedTextField(
                    value = senhaUsuario,
                    onValueChange = {
                        senhaUsuario = if (it.length <= MAX_PASSWORD_LENGTH) it else it.substring(0, MAX_PASSWORD_LENGTH)
                        senhaUsuarioError = null
                    },
                    label = { Text("Senha", fontSize = 15.sp, fontFamily = FontFamily(Font(R.font.lexend)), fontWeight = FontWeight(700), color = Color(0xFFF08080)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = senhaUsuarioError != null,
                    supportingText = { senhaUsuarioError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ACCENT_COLOR, cursorColor = ACCENT_COLOR)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Campo Confirme a Senha
                OutlinedTextField(
                    value = confirmaSenhaUsuario,
                    onValueChange = {
                        confirmaSenhaUsuario = if (it.length <= MAX_PASSWORD_LENGTH) it else it.substring(0, MAX_PASSWORD_LENGTH)
                        confirmaSenhaError = null
                    },
                    label = { Text("Confirme a Senha", fontSize = 15.sp, fontFamily = FontFamily(Font(R.font.lexend)), fontWeight = FontWeight(700), color = Color(0xFFF08080)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmaSenhaError != null,
                    supportingText = { confirmaSenhaError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ACCENT_COLOR, cursorColor = ACCENT_COLOR)
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (authState is AuthUiState.Loading) {
                    CircularProgressIndicator(color = ACCENT_COLOR, modifier = Modifier.size(45.dp))
                } else {
                    Button(
                        onClick = {
                            if (validateRegistrationFields()) {
                                authViewModel.register(
                                    RegisterRequest(
                                        nomeUsuario = loginUsuario.trim(), // Envia sem espaços extras
                                        loginUsuario = loginUsuario.trim(),
                                        senhaUsuario = senhaUsuario // Senha não precisa de trim, pois espaços podem ser intencionais
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(45.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ACCENT_COLOR, contentColor = Color.White)
                    ) {
                        Text("Cadastrar", fontSize = 20.sp, fontFamily = FontFamily(Font(R.font.lexend)), fontWeight = FontWeight(700), color = Color(0xFFFFE4EB))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Já possui uma conta? ")
                    Text("Faça login", fontSize = 15.sp, fontFamily = FontFamily(Font(R.font.lexend)), color = ACCENT_COLOR, modifier = Modifier.clickable { navController.navigate("login") })
                }
            }
        }
    }
}