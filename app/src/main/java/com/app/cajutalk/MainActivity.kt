package com.app.cajutalk

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.app.cajutalk.ui.theme.BACK_ICON_TINT
import com.app.cajutalk.viewmodels.AudioRecorderViewModel
import com.app.cajutalk.viewmodels.DataViewModel

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CajuTalkApp()

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        } }
}

var mainUser: User = User(id = -1, login = "Visitante", name = "Visitante")
val users = listOf(mainUser)

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
fun CajuTalkApp() {
    val navController = rememberNavController()
    val audioRecorderViewModel = AudioRecorderViewModel()
    val dataViewModel: DataViewModel = viewModel()

    FocusClearContainer{
        NavHost(
            navController = navController,
            startDestination = "cadastro",
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable("login") { LoginScreen(navController) }
            composable("cadastro") { CadastroScreen(navController) }
            composable("salas") { RoomsScreen(navController, dataViewModel) }
            composable("chat") { ChatScreen(audioRecorderViewModel, navController, dataViewModel) }
            composable("user-profile") { UserProfileScreen(navController) }
            composable("search-user") { SearchUserScreen(navController, dataViewModel) }
            composable("searched-user-profile") { SearchedUserProfileScreen(navController, dataViewModel) }
            composable("room-members") { RoomMembersScreen(navController, dataViewModel) }
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
    CajuTalkApp()
}