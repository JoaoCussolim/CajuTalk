package com.app.cajutalk.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.app.cajutalk.R
import com.app.cajutalk.network.models.UsuarioDto
import com.app.cajutalk.ui.theme.ACCENT_COLOR
import com.app.cajutalk.ui.theme.BACKGROUND_COLOR
import com.app.cajutalk.ui.theme.BACK_ICON_TINT
import com.app.cajutalk.ui.theme.HEADER_TEXT_COLOR
import com.app.cajutalk.ui.theme.WAVE_COLOR
import com.app.cajutalk.viewmodels.AuthViewModel
import com.app.cajutalk.viewmodels.DataViewModel
import com.app.cajutalk.viewmodels.UserViewModel

private const val TAG = "UserProfileDebug"

@Composable
fun UserProfileScreen(
    navController: NavController,
    dataViewModel: DataViewModel,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel // MUDANÇA: Adicionado
) {
    val context = LocalContext.current

    val usuarioLogado = dataViewModel.usuarioLogado
    if (usuarioLogado == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Erro: Usuário não encontrado.")
        }
        return
    }

    var nome by remember { mutableStateOf(usuarioLogado.NomeUsuario ?: "") }
    var recado by remember { mutableStateOf(usuarioLogado.Recado ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) } // NOVO

    val updateUserResult by userViewModel.updateUserResult.observeAsState()
    val deleteUserResult by userViewModel.deleteUserResult.observeAsState() // NOVO
    val isLoading by userViewModel.isLoading.observeAsState(false)

    LaunchedEffect(updateUserResult) {
        updateUserResult?.let { result ->
            result.onSuccess { newCorrectDto ->
                Log.d(TAG, "1. A atualização do usuário foi bem-sucedida no ViewModel.")

                // Agora simplesmente atribuímos o DTO correto que o ViewModel nos deu.
                dataViewModel.usuarioLogado = newCorrectDto
                Log.d(TAG, "2. DataViewModel local foi atualizado com o DTO do ViewModel.")

                Toast.makeText(context, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "3. Toast de sucesso foi exibido.")

                navController.navigate("salas") {
                    popUpTo("user-profile") { inclusive = true }
                }
                Log.d(TAG, "4. Navegação para 'salas' foi iniciada.")

            }.onFailure {
                Log.e(TAG, "A atualização do usuário falhou.", it)
                Toast.makeText(context, "Falha ao atualizar o perfil: ${it.message}", Toast.LENGTH_LONG).show()
            }
            userViewModel.onUpdateUserResultConsumed()
        }
    }

    LaunchedEffect(deleteUserResult) {
        deleteUserResult?.let { result ->
            result.onSuccess {
                Toast.makeText(context, "Conta excluída com sucesso.", Toast.LENGTH_SHORT).show()
                authViewModel.logout()
                navController.navigate("login") {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            }.onFailure {
                Toast.makeText(context, "Falha ao excluir a conta: ${it.message}", Toast.LENGTH_LONG).show()
            }
            userViewModel.onDeleteUserResultConsumed()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val hasChanges = nome != usuarioLogado.NomeUsuario || recado != (usuarioLogado.Recado ?: "") || selectedImageUri != null

    if (showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            title = { Text(text = "Descartar alterações?", fontFamily = FontFamily(Font(R.font.lexend)), color = ACCENT_COLOR) },
            text = { Text(text = "Você tem alterações não salvas. Deseja sair mesmo assim?", fontFamily = FontFamily(Font(R.font.lexend)), color = ACCENT_COLOR) },
            confirmButton = {
                TextButton(onClick = {
                    showUnsavedChangesDialog = false
                    navController.popBackStack()
                }) {
                    Text(text = "Sair sem salvar", fontFamily = FontFamily(Font(R.font.lexend)), color = ACCENT_COLOR)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedChangesDialog = false }) {
                    Text(text = "Cancelar", fontFamily = FontFamily(Font(R.font.lexend)), color = ACCENT_COLOR)
                }
            }
        )
    }

    // NOVO: Diálogo de confirmação para exclusão
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Confirmar Exclusão", color = Color.Red) },
            text = { Text("Tem certeza que deseja excluir sua conta? Esta ação é permanente e não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        userViewModel.deleteUser(usuarioLogado.ID)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(color = BACKGROUND_COLOR)) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
            contentDescription = "Voltar",
            modifier = Modifier
                .padding(16.dp)
                .size(40.dp)
                .clickable {
                    if (hasChanges) {
                        showUnsavedChangesDialog = true
                    } else {
                        navController.popBackStack()
                    }
                },
            tint = BACK_ICON_TINT
        )

        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Olá, ${usuarioLogado.LoginUsuario}",
                fontSize = 30.sp,
                fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                fontWeight = FontWeight(700),
                color = HEADER_TEXT_COLOR
            )

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .padding(top = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                AsyncImage(
                    model = selectedImageUri ?: usuarioLogado.FotoPerfilURL?.replace("http://", "https://"),
                    contentDescription = "Ícone do Usuário",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFDDC1)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.placeholder_image),
                    placeholder = painterResource(id = R.drawable.placeholder_image)
                )

                IconButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
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
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "Pessoal",
                    fontSize = 28.sp,
                    fontFamily = FontFamily(Font(R.font.anton)),
                    fontWeight = FontWeight(400),
                    color = WAVE_COLOR,
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
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ACCENT_COLOR,
                        cursorColor = ACCENT_COLOR
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
                    value = recado,
                    onValueChange = { recado = it },
                    label = { Text("") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ACCENT_COLOR,
                        cursorColor = ACCENT_COLOR
                    )
                )

                Spacer(modifier = Modifier.weight(1f)) // Empurra os botões para baixo

                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7094)),
                    onClick = {
                        userViewModel.updateUserProfile(
                            currentUserDto = usuarioLogado,
                            nome = nome,
                            recado = recado,
                            newImageUri = selectedImageUri
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = hasChanges && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(text = "Salvar", color = Color.White, fontSize = 20.sp, fontFamily = FontFamily(Font(R.font.lexend)))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935), contentColor = Color.White),
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isLoading
                ) {
                    Text(text = "Deletar Conta", fontSize = 20.sp, fontFamily = FontFamily(Font(R.font.lexend)))
                }
            }
        }
    }
}