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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.app.cajutalk.ui.theme.BACK_ICON_TINT

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CajuTalkApp()

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        } }
}

var mainUser = User(login = "FakeDoAshborn", senha = "AmoChaHaeIn", name = "Sung Jin Woo", message = "Amo meu exército", imageUrl = "https://cdn-images.dzcdn.net/images/cover/52634551c3ae630fb3f0b86b6eaed4a0/0x1900-000000-80-0-0.jpg")
var antares = User(login = "MonarcaDragoes", senha = "OdeioAshborn", name = "Antares", message = "Vou te matar Ashborn \uD83D\uDE21", imageUrl = "https://i0.wp.com/ovicio.com.br/wp-content/uploads/2025/02/20250219-antares.webp?resize=555%2C555&ssl=1")
var igris = User(login = "AmoMestreJinWoo", senha = "SouMelhorQueBeru", name = "Igris", message = "Mestre Jin Woo é demais \uD83D\uDE0A", imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_ZqpS-yB9uKbBrwPatsuYcvnX9Emtbz5-gw&s")
var beru = User(login = "AmoMaisMestreJinwoo", senha = "IgrisBuxa", name = "Beru", message = "Eu > Igris", imageUrl = "https://static.beebom.com/wp-content/uploads/2025/01/beru-solo-leveling.jpg?w=1250&quality=75")
var chaHaeIn = User(login = "OlfatoRankS", senha = "ChaHae123", name = "Chae Hae-in", message = "O cheiro do Jin Woo é bom \uD83D\uDE33", imageUrl = "https://images4.alphacoders.com/139/1391416.png")
var bellion = User(login = "SombraMaisForte", senha = "MorraAntares123", name = "Bellion", message = "Ashborn é absoluto.", imageUrl = "https://staticg.sportskeeda.com/editor/2024/02/a1b54-17071808977993-1920.jpg")

val users = listOf(mainUser, antares,igris,beru,chaHaeIn,bellion)

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

@Composable
@Preview
fun Preview(){
    CajuTalkApp()
}