package com.app.cajutalk

import android.app.Application
import android.content.pm.ActivityInfo // Se você ainda quiser usar setRequestedOrientation
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment // Import para Alignment.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Se usado para chatBackgroundColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.app.cajutalk.ui.theme.BACK_ICON_TINT
import com.app.cajutalk.ui.theme.CajuTalkTheme
import com.app.cajutalk.viewmodels.AuthUiState // Importar o AuthUiState atualizado
import com.app.cajutalk.viewmodels.AuthViewModel
import com.app.cajutalk.viewmodels.AudioRecorderViewModel
import com.app.cajutalk.viewmodels.DataViewModel
// Importar ViewModelFactory se estiver em outro arquivo
// import com.app.cajutalk.viewmodels.ViewModelFactory

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Opcional: Definir orientação no código, mas geralmente é melhor no AndroidManifest.xml
        // requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContent {
            CajuTalkTheme {
                CajuTalkApp(authViewModel = authViewModel, application = application)
            }
        }
    }
}

// REMOVA ou COMENTE as variáveis globais de estado:
// O estado do usuário deve vir do AuthViewModel.
var mainUser: User = User(id = -1, login = "Visitante", /*senha = "",*/ name = "Visitante", imageUrl = null, message = "", corFundoRGB = null)
// A cor de fundo do chat deve ser gerenciada pelo tema ou pelo estado do usuário (via AuthViewModel/DataViewModel).
var chatBackgroundColor = Color(0xE5FFFAFA)

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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CajuTalkApp(authViewModel: AuthViewModel, application: Application) {
    val navController = rememberNavController()

    val authStateValue = authViewModel.authUiState // Não usar 'by' aqui, apenas ler o valor do State
    var startDestination by remember { mutableStateOf("loading_screen") }

    // Este LaunchedEffect agora observa o valor do estado, não o objeto State em si.
    LaunchedEffect(authStateValue) {
        startDestination = when (authStateValue) {
            is AuthUiState.UserProfileLoaded -> "salas"
            // AuthSuccess significa que temos tokens, mas o perfil ainda não foi carregado.
            // Poderia ir para uma tela intermediária ou esperar o UserProfileLoaded.
            // Para simplificar, se tivermos tokens, tentamos ir para salas.
            is AuthUiState.AuthSuccess -> {
                if (authViewModel.getSavedUser() != null) "salas" else "loading_screen" // Ou "login" se o perfil falhar
            }
            is AuthUiState.Idle,
            is AuthUiState.Error,
            is AuthUiState.RegistrationSuccessState, // Após registro, o usuário deve logar
            is AuthUiState.UserDeleted -> "login"
            is AuthUiState.Loading -> "loading_screen" // Estado de carregamento do AuthViewModel
            is AuthUiState.UserProfileUpdated -> TODO()
        }
        Log.d("CajuTalkApp", "AuthState changed: $authStateValue, New StartDest: $startDestination")
    }
    // Lógica adicional para garantir o destino correto na primeira carga,
    // pois o init do AuthViewModel tenta carregar o usuário.
    // Esta lógica pode ser um pouco redundante com o LaunchedEffect, mas serve como uma verificação robusta.
    LaunchedEffect(Unit) { // Executar apenas uma vez na composição inicial
        if (authViewModel.getAccessToken() != null && authViewModel.getSavedUser() != null) {
            if(startDestination != "salas") { // Só atualiza se não já estiver definido para salas
                startDestination = "salas"
                Log.d("CajuTalkApp", "Initial check: User authenticated, StartDest: salas")
            }
        } else if (startDestination == "loading_screen") { // Se ainda estiver em loading e não autenticado
            startDestination = "login"
            Log.d("CajuTalkApp", "Initial check: User not authenticated, StartDest: login")
        }
    }


    // CORREÇÃO: Instanciar DataViewModel usando a ViewModelFactory
    val dataViewModel: DataViewModel = viewModel()
    val audioRecorderViewModel: AudioRecorderViewModel = viewModel()


    // Mostrar loading enquanto o startDestination ainda é "loading_screen"
    // e o estado de autenticação ainda não foi resolvido para um estado final.
    if (startDestination == "loading_screen" && authStateValue is AuthUiState.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
            Log.d("CajuTalkApp", "Displaying Loading Screen")
        }
    } else if (startDestination == "loading_screen" && authViewModel.getAccessToken() == null) {
        // Se após o loading inicial não houver token, força o login (caso o LaunchedEffect(Unit) não pegue a tempo)
        LaunchedEffect(Unit){ startDestination = "login" }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator() // Ainda mostra loading enquanto a navegação para login é processada
            Log.d("CajuTalkApp", "Displaying Loading Screen (forcing login)")
        }
    }
    else {
        Log.d("CajuTalkApp", "Proceeding to NavHost with StartDest: $startDestination")
        FocusClearContainer {
            NavHost(
                navController = navController,
                startDestination = startDestination, // Usar o startDestination determinado dinamicamente
                enterTransition = { fadeIn(animationSpec = tween(200)) }, // Animações mais rápidas
                exitTransition = { fadeOut(animationSpec = tween(200)) },
                popEnterTransition = { fadeIn(animationSpec = tween(200)) },
                popExitTransition = { fadeOut(animationSpec = tween(200)) }
            ) {
                composable("loading_screen") { /* Pode ser uma tela de splash mais elaborada */
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                composable("login") { LoginScreen(navController, authViewModel) }
                composable("cadastro") { CadastroScreen(navController, authViewModel) }
                composable("salas") { RoomsScreen(navController, dataViewModel, authViewModel) }

                composable(
                    route = "chat/{salaId}",
                    arguments = listOf(navArgument("salaId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val salaId = backStackEntry.arguments?.getInt("salaId")
                    if (salaId != null) {
                        ChatScreen(
                            salaId = salaId,
                            navController = navController,
                            authViewModel = authViewModel,
                            dataViewModel = dataViewModel,
                            audioRecorderViewModel = audioRecorderViewModel
                        )
                    } else { Text("Erro: ID da sala inválido.") }
                }

                composable("user-profile") { UserProfileScreen(navController, authViewModel) }
                composable("search-user") { SearchUserScreen(navController, dataViewModel) }
                composable("searched-user-profile") { SearchedUserProfileScreen(navController, dataViewModel) }

                composable(
                    route = "room-members/{salaId}",
                    arguments = listOf(navArgument("salaId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val salaId = backStackEntry.arguments?.getInt("salaId")
                    if (salaId != null) {
                        RoomMembersScreen(navController = navController, salaId = salaId, authViewModel = authViewModel, dataViewModel = dataViewModel)
                    } else { Text("Erro: ID da sala para membros inválido.") }
                }
            }
        }
    }
}

@Composable
fun DefaultBackIcon(navController: NavController, modifier: Modifier = Modifier) {
    IconButton(onClick = { navController.popBackStack() }, modifier = modifier.padding(8.dp)) { // Usar IconButton
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
            contentDescription = "Voltar",
            modifier = Modifier.size(32.dp), // Tamanho do ícone
            tint = BACK_ICON_TINT
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Preview(showBackground = true)
fun PreviewCajuTalkAppNotLoggedIn() {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val previewAuthViewModel = AuthViewModel(app)
    // Para o preview, o estado inicial do AuthViewModel (Idle) deve levar à tela de login.

    CajuTalkTheme {
        // Para testar o startDestination "salas", você precisaria simular um estado logado no previewAuthViewModel.
        // Exemplo (você precisaria de uma função no AuthViewModel para setar um estado de preview):
        // previewAuthViewModel.setPreviewState(AuthUiState.UserProfileLoaded(User(1, "test", "Test User", null, null, null)))
        CajuTalkApp(authViewModel = previewAuthViewModel, application = app)
    }
}