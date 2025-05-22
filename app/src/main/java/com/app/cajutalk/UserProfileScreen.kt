package com.app.cajutalk

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.app.cajutalk.network.dto.UsuarioUpdateRequest
import com.app.cajutalk.ui.theme.ACCENT_COLOR
import com.app.cajutalk.ui.theme.BACKGROUND_COLOR
import com.app.cajutalk.ui.theme.BACK_ICON_TINT
import com.app.cajutalk.ui.theme.CajuTalkTheme
import com.app.cajutalk.ui.theme.HEADER_TEXT_COLOR
import com.app.cajutalk.ui.theme.WAVE_COLOR
import com.app.cajutalk.viewmodels.AuthUiState
import com.app.cajutalk.viewmodels.AuthViewModel

@Composable
fun ColorPicker(selectedColor: Color, onColorChanged: (Color) -> Unit) {
    var red by remember { mutableIntStateOf((selectedColor.red * 255).toInt()) }
    var green by remember { mutableIntStateOf((selectedColor.green * 255).toInt()) }
    var blue by remember { mutableIntStateOf((selectedColor.blue * 255).toInt()) }
    var textColor by remember { mutableStateOf("$red, $green, $blue") }

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
                .background(
                    Color(red / 255f, green / 255f, blue / 255f),
                    shape = RoundedCornerShape(8.dp)
                )
                .border(2.dp, Color.Black, shape = RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = textColor,
            onValueChange = { input ->
                textColor = input
                updateColorFromText(input)
            },
            label = { Text(text = "RGB", color = ACCENT_COLOR) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ACCENT_COLOR,
                cursorColor = ACCENT_COLOR,
                unfocusedBorderColor = ACCENT_COLOR,
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
            onValueChange = { red = it.toInt(); textColor = "$red, $green, $blue"; onColorChanged(
                Color(red / 255f, green / 255f, blue / 255f)
            ) },
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
            onValueChange = { green = it.toInt(); textColor = "$red, $green, $blue"; onColorChanged(
                Color(red / 255f, green / 255f, blue / 255f)
            ) },
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
            onValueChange = { blue = it.toInt(); textColor = "$red, $green, $blue"; onColorChanged(
                Color(red / 255f, green / 255f, blue / 255f)
            ) },
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
                    colors = ButtonDefaults.buttonColors(containerColor = ACCENT_COLOR)
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ACCENT_COLOR)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Helper para converter Color do Compose para String "R,G,B"
fun Color.toRgbString(): String {
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    return "$red,$green,$blue"
}

// Helper para converter String "R,G,B" para Color do Compose
fun String?.toComposeColor(): Color {
    this ?: return Color(0xE5, 0xFF, 0xFA, 0xFA) // Cor padrão se nulo ou inválido
    val parts = this.split(",").mapNotNull { it.trim().toIntOrNull() }
    return if (parts.size == 3) {
        Color(parts[0].coerceIn(0, 255), parts[1].coerceIn(0, 255), parts[2].coerceIn(0, 255))
    } else {
        Color(0xE5, 0xFF, 0xFA, 0xFA) // Cor padrão
    }
}


@Composable
fun UserProfileScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val authState = authViewModel.authUiState

    // Estados locais para os campos editáveis, inicializados pelo authState
    var currentUserName by remember { mutableStateOf("") }
    var currentUserLogin by remember { mutableStateOf("") }
    var currentMessage by remember { mutableStateOf("") } // Assumindo que 'message' é um recado local
    var currentSelectedColor by remember { mutableStateOf(Color(0xE5,0xFF,0xFA,0xFA)) } // chatBackgroundColor padrão
    var currentImageUriString by remember { mutableStateOf<String?>(null) } // URL da imagem da API

    var localNameEdit by remember { mutableStateOf("") }
    var localMessageEdit by remember { mutableStateOf("") }
    var localSelectedColorEdit by remember { mutableStateOf(Color(0xE5,0xFF,0xFA,0xFA)) }
    var localSelectedImageUriForUpload by remember { mutableStateOf<Uri?>(null) } // Para nova imagem selecionada
    var localImageDisplayUri by remember { mutableStateOf<Any?>(null) } // Para AsyncImage (pode ser String URL ou Uri local)

    var showExitConfirmationDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }


    // Atualizar estados locais quando o perfil do usuário é carregado ou atualizado
    LaunchedEffect(authState) {
        if (authState is AuthUiState.UserProfileLoaded) {
            val user = (authState as AuthUiState.UserProfileLoaded).user
            currentUserName = user.name
            currentUserLogin = user.login
            currentMessage = user.message ?: "" // Assumindo que User tem 'recado'
            currentSelectedColor = user.corFundoRGB?.toComposeColor() ?: Color(0xE5,0xFF,0xFA,0xFA) // Assumindo User tem 'corFundoRGB'
            currentImageUriString = user.imageUrl

            // Resetar campos de edição para os valores carregados
            localNameEdit = currentUserName
            localMessageEdit = currentMessage
            localSelectedColorEdit = currentSelectedColor
            localSelectedImageUriForUpload = null // Limpar seleção de upload
            localImageDisplayUri = currentImageUriString // Mostrar imagem da API
        } else if (authState is AuthUiState.UserProfileUpdated) {
            val user = (authState as AuthUiState.UserProfileUpdated).user
            currentUserName = user.name
            currentUserLogin = user.login
            currentMessage = user.message ?: ""
            currentSelectedColor = user.corFundoRGB?.toComposeColor() ?: Color(0xE5,0xFF,0xFA,0xFA)
            currentImageUriString = user.imageUrl

            localNameEdit = currentUserName
            localMessageEdit = currentMessage
            localSelectedColorEdit = currentSelectedColor
            localSelectedImageUriForUpload = null
            localImageDisplayUri = currentImageUriString
            Toast.makeText(context, "Perfil atualizado!", Toast.LENGTH_SHORT).show()
        } else if (authState is AuthUiState.UserDeleted) {
            Toast.makeText(context, "Conta deletada.", Toast.LENGTH_LONG).show()
            navController.navigate("login") { // Navegar para login após deletar
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
            authViewModel.resetAuthState()
        }
    }

    // Inicializar os campos de edição na primeira composição se o perfil já estiver carregado
    // Isso garante que, se o usuário navegar para esta tela e o perfil já estiver no AuthViewModel,
    // os campos sejam preenchidos corretamente.
    DisposableEffect(Unit) {
        val user = authViewModel.getSavedUser() // Pega o usuário das SharedPreferences
        if (user != null) {
            currentUserName = user.name
            currentUserLogin = user.login
            currentMessage = user.message ?: ""
            currentSelectedColor = user.corFundoRGB?.toComposeColor() ?: Color(0xE5,0xFF,0xFA,0xFA)
            currentImageUriString = user.imageUrl

            localNameEdit = currentUserName
            localMessageEdit = currentMessage
            localSelectedColorEdit = currentSelectedColor
            localImageDisplayUri = currentImageUriString
        }
        onDispose { }
    }


    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            localSelectedImageUriForUpload = uri
            localImageDisplayUri = uri // Mostrar a imagem selecionada localmente
        }
    }

    val hasChanges: Boolean by remember(
        localNameEdit, currentUserName,
        localMessageEdit, currentMessage,
        localSelectedColorEdit, currentSelectedColor,
        localSelectedImageUriForUpload, // Se uma nova imagem foi selecionada, há mudanças
        // currentImageUriString // Não precisa comparar com currentImageUriString se localSelectedImageUriForUpload já indica nova imagem
    ) {
        derivedStateOf {
            localNameEdit != currentUserName ||
                    localMessageEdit != currentMessage ||
                    localSelectedColorEdit != currentSelectedColor ||
                    localSelectedImageUriForUpload != null // Se uma nova imagem foi selecionada, há mudanças
        }
    }

    if (showExitConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmationDialog = false },
            title = { Text("Descartar alterações?", fontFamily = FontFamily(Font(R.font.lexend)), color = ACCENT_COLOR) },
            text = { Text("Você tem alterações não salvas. Deseja sair mesmo assim?", fontFamily = FontFamily(Font(R.font.lexend)), color = ACCENT_COLOR) },
            confirmButton = {
                TextButton(onClick = {
                    showExitConfirmationDialog = false
                    navController.popBackStack()
                }) { Text("Sair sem salvar", fontFamily = FontFamily(Font(R.font.lexend)), color = ACCENT_COLOR) }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmationDialog = false }) { Text("Cancelar", fontFamily = FontFamily(Font(R.font.lexend)), color = ACCENT_COLOR) }
            }
        )
    }
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Deletar Conta", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza que deseja deletar sua conta? Esta ação é irreversível e todos os seus dados serão perdidos.", color = Color.DarkGray) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        authViewModel.deleteCurrentUserAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("DELETAR", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Cancelar") }
            }
        )
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BACKGROUND_COLOR)
    ) {
        // Ícone de voltar
        IconButton(
            onClick = {
                if (hasChanges) {
                    showExitConfirmationDialog = true
                } else {
                    navController.popBackStack()
                }
            },
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                contentDescription = "Voltar",
                modifier = Modifier.size(32.dp),
                tint = BACK_ICON_TINT
            )
        }

        // Botão de Logout e Deletar Conta
        Row(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = {
                authViewModel.logout()
                navController.navigate("login") { // Navegar para login após logout
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                Text("Logout", color = Color.White, fontSize = 12.sp)
            }
            Button(onClick = {
                showDeleteAccountDialog = true
            }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f))) {
                Text("Excluir", color = Color.White, fontSize = 12.sp)
            }
        }


        Column(
            modifier = Modifier
                .fillMaxSize() // Ocupar todo o espaço
                .padding(top = 56.dp) // Espaço para os ícones do topo
                .verticalScroll(rememberScrollState()), // Permitir rolagem
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (authState is AuthUiState.Loading && localSelectedImageUriForUpload == null) { // Mostrar loading só se não for upload de imagem
                CircularProgressIndicator(modifier = Modifier.padding(top = 60.dp))
            } else {
                Text(
                    text = "Olá, ${currentUserLogin.ifBlank { "Usuário" }}",
                    fontSize = 26.sp,
                    fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                    fontWeight = FontWeight(700),
                    color = HEADER_TEXT_COLOR,
                    modifier = Modifier.padding(top = 10.dp)
                )

                Box(
                    modifier = Modifier.size(140.dp).padding(top = 10.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    AsyncImage(
                        model = localImageDisplayUri, // Usa URI local se selecionado, senão URL da API ou placeholder
                        contentDescription = "Ícone do Usuário",
                        modifier = Modifier
                            .fillMaxSize() // Preenche o Box
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(48.dp) // Tamanho do botão da câmera
                            .background(ACCENT_COLOR, shape = CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                            .align(Alignment.BottomEnd) // Alinhar no canto inferior direito da imagem
                            .offset(x = 4.dp, y = 4.dp) // Pequeno offset para sobrepor um pouco
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.camera_icon),
                            contentDescription = "Escolher imagem",
                            modifier = Modifier.size(24.dp) // Tamanho do ícone da câmera
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Card de edição
                Card(
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x9EFFFFFF)), // Cor do card mais clara
                    modifier = Modifier
                        .fillMaxWidth()
                        // .heightIn(min = 400.dp) // Altura mínima para o card
                        .padding(bottom = 0.dp) // Garantir que cole no fundo se for o último
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text("Pessoal", style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily(Font(R.font.anton)), color = WAVE_COLOR))
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = localNameEdit,
                            onValueChange = { localNameEdit = it },
                            label = { Text("Nome de Usuário", fontFamily = FontFamily(Font(R.font.lexend))) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ACCENT_COLOR, cursorColor = ACCENT_COLOR),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // OutlinedTextField( // Se você tiver um campo "recado" que quer editar
                        //     value = localMessageEdit,
                        //     onValueChange = { localMessageEdit = it },
                        //     label = { Text("Seu Recado", fontFamily = FontFamily(Font(R.font.lexend))) },
                        //     modifier = Modifier.fillMaxWidth().height(100.dp),
                        //     colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ACCENT_COLOR, cursorColor = ACCENT_COLOR)
                        // )
                        // Spacer(modifier = Modifier.height(16.dp))

                        Text("Personalização", style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily(Font(R.font.anton)), color = WAVE_COLOR))
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Cor de Fundo (Chat):", style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily(Font(R.font.baloo_bhai)), color = ACCENT_COLOR))
                        Spacer(modifier = Modifier.height(8.dp))
                        ColorPickerButton(selectedColor = localSelectedColorEdit) { newColor ->
                            localSelectedColorEdit = newColor
                        }

                        Spacer(modifier = Modifier.weight(1f)) // Empurra o botão para baixo

                        Button(
                            onClick = {
                                // Implementar lógica de upload de imagem se localSelectedImageUriForUpload não for nulo
                                // Por enquanto, vamos assumir que a API aceita uma string para novaFotoPerfil
                                // Se for um upload real, você precisaria converter Uri para File e chamar um endpoint diferente ou modificar o atual
                                var fotoPerfilParaEnviar: String? = null
                                if (localSelectedImageUriForUpload != null) {
                                    // AQUI VOCÊ PRECISARIA DA LÓGICA DE UPLOAD
                                    // Se a API só aceita URL, você faria upload para um serviço (Firebase Storage, S3, etc.)
                                    // e pegaria a URL.
                                    // Se a API aceitasse base64 (não recomendado para arquivos grandes):
                                    // fotoPerfilParaEnviar = uriToBase64String(context, localSelectedImageUriForUpload!!)
                                    // Se a API aceitasse o URI direto (improvável e não seguro):
                                    // fotoPerfilParaEnviar = localSelectedImageUriForUpload.toString()

                                    // *Suposição para este exemplo: A API C# foi modificada para aceitar um File via Multipart,
                                    // e você teria uma função separada no AuthViewModel para isso, ou AuthViewModel.updateUserProfile
                                    // lidaria com o File se `localSelectedImageUriForUpload` não fosse nulo.*

                                    // Por agora, vamos apenas passar a string da URI para demonstração,
                                    // sabendo que isso provavelmente não funcionará sem modificações na API C#
                                    // ou uma lógica de upload de imagem adequada.
                                    // SE VOCÊ NÃO TEM UPLOAD DE IMAGEM IMPLEMENTADO NA API, NÃO ENVIE ISSO
                                    // fotoPerfilParaEnviar = localSelectedImageUriForUpload.toString() // Placeholder
                                    Toast.makeText(context, "Upload de nova foto não implementado neste exemplo.", Toast.LENGTH_LONG).show()
                                }


                                val updateRequest = UsuarioUpdateRequest(
                                    nomeUsuario = if (localNameEdit != currentUserName) localNameEdit else null,
                                    loginUsuario = null, // Não permitir edição de login aqui, ou adicionar campo
                                    senhaUsuario = null, // Não permitir edição de senha aqui, ou adicionar campos
                                    novaFotoPerfil = fotoPerfilParaEnviar
                                    /* corFundo = if (localSelectedColorEdit != currentSelectedColor) localSelectedColorEdit.toRgbString() else null */
                                )
                                authViewModel.updateUserProfile(updateRequest)
                            },
                            enabled = hasChanges && authState !is AuthUiState.Loading, // Habilitar apenas se houver mudanças e não estiver carregando
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ACCENT_COLOR)
                        ) {
                            if (authState is AuthUiState.Loading && localSelectedImageUriForUpload != null) { // Mostrar loading se for upload
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Salvar Alterações", color = Color.White, fontFamily = FontFamily(Font(R.font.lexend)))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserProfileScreenPreview() {
    // Para o Preview, você precisaria mockar o AuthViewModel ou fornecer um estado inicial
    // Aqui está um exemplo muito básico
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as android.app.Application
    val previewAuthViewModel = AuthViewModel(app)
    // Simular um usuário carregado para o preview
    // previewAuthViewModel.setPreviewState(AuthUiState.UserProfileLoaded(User(1, "previewUser", "Preview User", null, "Olá Preview", "255,111,156")))

    CajuTalkTheme {
        UserProfileScreen(navController = navController, authViewModel = previewAuthViewModel)
    }
}