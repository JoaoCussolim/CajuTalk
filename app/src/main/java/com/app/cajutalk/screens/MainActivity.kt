package com.app.cajutalk.screens

import android.app.Application
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.app.cajutalk.classes.User
import com.app.cajutalk.ui.theme.BACK_ICON_TINT
import com.app.cajutalk.viewmodels.AudioRecorderViewModel
import com.app.cajutalk.viewmodels.AuthViewModel
import com.app.cajutalk.viewmodels.DataViewModel
import com.app.cajutalk.viewmodels.ViewModelFactory // Import the ViewModelFactory

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Create an instance of your ViewModelFactory
            val factory = ViewModelFactory(application) // Pass the application instance
            CajuTalkApp(factory) // Pass the factory to your CajuTalkApp
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
    }
}

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
fun CajuTalkApp(factory: ViewModelFactory) {
    val navController = rememberNavController()
    val audioRecorderViewModel = AudioRecorderViewModel()
    val dataViewModel: DataViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel(factory = factory)

    val startDestination = "login"

    FocusClearContainer {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable("login") {
                LoginScreen(navController, authViewModel = viewModel(factory = factory), userViewModel = viewModel(factory = factory), // Adicione esta linha
                    dataViewModel = dataViewModel                   // Adicione esta linha
                ) // Use factory
            }
            composable("cadastro") {
                CadastroScreen(navController, authViewModel = viewModel(factory = factory), userViewModel = viewModel(factory = factory), // Adicione esta linha
                    dataViewModel = dataViewModel                   // Adicione esta linha
                ) // Use factory
            }
            composable("salas") {
                RoomsScreen(
                    navController = navController,
                    roomViewModel = dataViewModel,
                    salaViewModel = viewModel(factory = factory),
                    authViewModel = viewModel(factory = factory)
                )
            }
            composable("chat") {
                ChatScreen(
                    viewModel = audioRecorderViewModel,
                    navController = navController,
                    roomViewModel = dataViewModel,
                    mensagemViewModel = viewModel(factory = factory),
                    userViewModel = viewModel(factory = factory),
                    salaViewModel = viewModel(factory = factory)
                )
            }
            composable("user-profile") {
                UserProfileScreen(
                    navController = navController,
                    dataViewModel = dataViewModel,
                    userViewModel = viewModel(factory = factory),
                    authViewModel = authViewModel
                )
            }
            composable("search-user") {
                SearchUserScreen(
                    navController = navController,
                    dataViewModel = dataViewModel,
                    userViewModel = viewModel(factory = factory)
                )
            }
            composable("searched-user-profile") {
                SearchedUserProfileScreen(
                    navController = navController,
                    dataViewModel = dataViewModel,
                )
            }
            composable("room-members") {
                RoomMembersScreen(
                    navController = navController,
                    dataViewModel = dataViewModel,
                    salaViewModel = viewModel(factory = factory)
                )
            }
        }
    }
}

@Composable
fun DefaultBackIcon(navController: NavController) {
    Icon(
        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
        contentDescription = "Voltar",
        modifier = Modifier
            .padding(16.dp)
            .size(40.dp)
            .clickable { navController.popBackStack() },
        tint = BACK_ICON_TINT
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Preview
fun Preview(){
    val application = LocalContext.current.applicationContext as Application
    val factory = ViewModelFactory(application)
    CajuTalkApp(factory)
}