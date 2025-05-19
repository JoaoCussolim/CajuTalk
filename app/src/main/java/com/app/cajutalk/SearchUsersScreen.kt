package com.app.cajutalk

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.app.cajutalk.ui.theme.ACCENT_COLOR
import com.app.cajutalk.ui.theme.BACKGROUND_COLOR
import com.app.cajutalk.ui.theme.WAVE_COLOR
import com.app.cajutalk.viewmodels.DataViewModel
import com.app.cajutalk.viewmodels.UserSearchUiState

@Composable
fun SearchedUserProfileItem(
    user: User, // Recebe o seu modelo User local
    navController: NavController,
    dataViewModel: DataViewModel
) {
    // O Spacer aqui pode ser desnecessário se for o único item
    // Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth(0.8f) // Considerar se isso deve ser fillMaxWidth() se for o único resultado
            .padding(8.dp)
            .background(color = WAVE_COLOR, shape = RoundedCornerShape(25.dp))
            .clickable {
                dataViewModel.setUsuarioParaVisualizar(user)
                navController.navigate("searched-user-profile")
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
        ) {
            AsyncImage(
                model = user.imageUrl,
                contentDescription = "Foto de Perfil", // Descrição mais adequada
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                // Adicionar placeholder e error para melhor UX
                // placeholder = painterResource(id = R.drawable.ic_placeholder_profile),
                // error = painterResource(id = R.drawable.ic_error_profile)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = user.name,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontFamily = FontFamily(Font(R.font.lexend))
            )
            Text(
                text = "@${user.login}", // Adicionar @ para o login
                fontSize = 12.sp,
                color = Color.Gray,
                fontFamily = FontFamily(Font(R.font.lexend))
            )
        }
    }
}

@Composable
fun SearchUserScreen(navController: NavController, dataViewModel: DataViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val userSearchState = dataViewModel.userSearchUiStateResult
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        dataViewModel.resetUserSearchState()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BACKGROUND_COLOR)
    ) {
        DefaultBackIcon(navController = navController)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .background(color = WAVE_COLOR, shape = RoundedCornerShape(25.dp))
                        .padding(horizontal = 6.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Pesquisar",
                        tint = ACCENT_COLOR,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Digite o ID do usuário...", color = ACCENT_COLOR) },
                        shape = RoundedCornerShape(25.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            cursorColor = ACCENT_COLOR,
                            focusedContainerColor = WAVE_COLOR,
                            unfocusedContainerColor = WAVE_COLOR,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                val userId = searchQuery.trim().toIntOrNull() // .trim() para remover espaços
                                if (userId != null) {
                                    dataViewModel.searchUserById(userId)
                                } else {
                                    // Feedback para o usuário: ID inválido
                                    // Ex: Toast.makeText(context, "ID inválido", Toast.LENGTH_SHORT).show()
                                }
                                keyboardController?.hide()
                            }
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CORREÇÃO: Usar a variável 'state' correta do when
            when (val currentState = userSearchState) { // Renomear para currentState para evitar shadowing
                is UserSearchUiState.Idle -> {
                    Text(
                        "Digite um ID para buscar um usuário.",
                        color = Color.Gray,
                        fontFamily = FontFamily(Font(R.font.lexend))
                    )
                }
                is UserSearchUiState.Loading -> {
                    CircularProgressIndicator(color = ACCENT_COLOR)
                }
                is UserSearchUiState.Success -> {
                    // Mapear UsuarioDto para o seu modelo User local
                    val userLocal = User(currentState.user) // Mapeia o DTO para o User local
                    // Passar o User local para o item
                    SearchedUserProfileItem(
                        user = userLocal, // Passa o User local
                        navController = navController,
                        dataViewModel = dataViewModel // Passa o ViewModel
                    )
                }
                is UserSearchUiState.NotFound -> {
                    Text(
                        "Usuário não encontrado.",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily(Font(R.font.lexend))
                    )
                }
                is UserSearchUiState.Error -> {
                    Text(
                        "Erro: ${currentState.message}",
                        color = Color.Red,
                        fontFamily = FontFamily(Font(R.font.lexend))
                    )
                }
            }
        }
    }
}