package com.app.cajutalk.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.app.cajutalk.R
import com.app.cajutalk.ui.theme.ACCENT_COLOR
import com.app.cajutalk.ui.theme.BACKGROUND_COLOR
import com.app.cajutalk.ui.theme.BACK_ICON_TINT
import com.app.cajutalk.ui.theme.HEADER_TEXT_COLOR
import com.app.cajutalk.ui.theme.WAVE_COLOR
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.cajutalk.viewmodels.DataViewModel
import com.app.cajutalk.viewmodels.UserViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import com.app.cajutalk.network.models.UsuarioUpdateDto
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily

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

        Text("Vermelho: $red", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily(Font(
            R.font.lexend
        )), color = Color.Black)
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

        Text("Verde: $green", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily(Font(
            R.font.lexend
        )), color = Color.Black)
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

        Text("Azul: $blue", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily(Font(
            R.font.lexend
        )), color = Color.Black)
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

@Composable
fun UserProfileScreen(navController: NavController, dataViewModel: DataViewModel) {
    val userViewModel: UserViewModel = viewModel()
    val context = LocalContext.current

    val loggedInUser = dataViewModel.usuarioLogado

    var name by remember { mutableStateOf(loggedInUser?.NomeUsuario ?: "") }
    var message by remember { mutableStateOf(loggedInUser?.Recado ?: "") }
    var selectedColor by remember { mutableStateOf(chatBackgroundColor) } // Initial color from global var
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var stringSelectedImageUri by remember { mutableStateOf(loggedInUser?.FotoPerfilURL ?: "") }

    val updateResult by userViewModel.updateUserResult.observeAsState()

    LaunchedEffect(loggedInUser) {
        // Initialize UI states when loggedInUser changes
        name = loggedInUser?.NomeUsuario ?: ""
        message = loggedInUser?.Recado ?: ""
        stringSelectedImageUri = loggedInUser?.FotoPerfilURL ?: ""
        // chatBackgroundColor is global, so no direct update from DTO unless you add a field for it
    }

    LaunchedEffect(updateResult) {
        updateResult?.let { result ->
            if (result.isSuccess) {
                Toast.makeText(context, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                // Refresh the logged-in user in DataViewModel after successful update
                userViewModel.getCurrentUserDetails()
            } else {
                Toast.makeText(context, "Erro ao atualizar perfil: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if(uri != null){
            selectedImageUri = uri
            stringSelectedImageUri = uri.toString()
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Descartar alterações?", fontFamily = FontFamily(Font(R.font.lexend)), color = ACCENT_COLOR) },
            text = { Text(text = "Você tem alterações não salvas. Deseja sair mesmo assim?", fontFamily = FontFamily(Font(
                R.font.lexend
            )), color = ACCENT_COLOR) },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    navController.popBackStack()
                }) {
                    Text(text = "Sair sem salvar", fontFamily = FontFamily(Font(R.font.lexend)), color = ACCENT_COLOR)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = "Cancelar", fontFamily = FontFamily(Font(R.font.lexend)), color = ACCENT_COLOR)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BACKGROUND_COLOR)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
            contentDescription = "Sair",
            modifier = Modifier
                .padding(16.dp)
                .size(40.dp)
                .clickable {
                    // Check if changes were made before popping back
                    val hasChanges = name != (loggedInUser?.NomeUsuario ?: "") ||
                            message != (loggedInUser?.Recado ?: "") ||
                            stringSelectedImageUri != (loggedInUser?.FotoPerfilURL ?: "") ||
                            chatBackgroundColor != selectedColor
                    if(hasChanges){
                        showDialog = true
                    } else {
                        navController.popBackStack()
                    }
                },
            tint = BACK_ICON_TINT
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Olá, ${loggedInUser?.LoginUsuario ?: "Usuário"}",
                fontSize = 30.sp,
                fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                fontWeight = FontWeight(700),
                color = HEADER_TEXT_COLOR
            )

            Box(
                modifier = Modifier
                    .size(140.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                AsyncImage(
                    model = selectedImageUri ?: stringSelectedImageUri.ifEmpty { R.drawable.placeholder_image }, // Use placeholder if URL is empty
                    contentDescription = "Ícone do Usuário",
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFDDC1)),
                    contentScale = ContentScale.Crop
                )

                IconButton(
                    onClick = {  imagePickerLauncher.launch("image/*") },
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
                        focusedBorderColor = ACCENT_COLOR,
                        cursorColor = ACCENT_COLOR
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Personalização",
                    fontSize = 28.sp,
                    fontFamily = FontFamily(Font(R.font.anton)),
                    fontWeight = FontWeight(400),
                    color = WAVE_COLOR,
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
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ColorPickerButton(selectedColor) { newColor ->
                        selectedColor = newColor;
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Text("RGB Atual", fontSize = 15.sp, fontWeight = FontWeight(400), color = Color(0xFFF08080), fontFamily = FontFamily(Font(
                        R.font.lexend
                    )))

                    Spacer(modifier = Modifier.width(32.dp))

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7094)),
                        onClick = {
                            loggedInUser?.let { user ->
                                val updateDto = UsuarioUpdateDto(
                                    NomeUsuario = name,
                                    LoginUsuario = null, // Not updating login here
                                    SenhaUsuario = null, // Not updating password here
                                    NovaFotoPerfil = stringSelectedImageUri.ifEmpty { null }, // Send null if empty
                                    Recado = message
                                )
                                userViewModel.updateUser(user.ID, updateDto)
                            }
                            chatBackgroundColor = selectedColor
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Salvar", color = Color.White, fontFamily = FontFamily(Font(R.font.lexend)))
                    }
                }
            }
        }
    }
}