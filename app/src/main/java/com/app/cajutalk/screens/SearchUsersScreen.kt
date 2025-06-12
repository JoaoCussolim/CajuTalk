package com.app.cajutalk.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.app.cajutalk.ui.theme.WAVE_COLOR
import com.app.cajutalk.viewmodels.DataViewModel
import com.app.cajutalk.viewmodels.UserViewModel

@Composable
fun SearchedUserProfileItem(user: UsuarioDto, navController : NavController, dataViewModel: DataViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 4.dp)
            .background(color = WAVE_COLOR, shape = RoundedCornerShape(12.dp))
            .clickable {
                dataViewModel.usuarioProcurado = user
                navController.navigate("searched-user-profile")
            }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.FotoPerfilURL?.replace("http://", "https://"),
            contentDescription = "Foto de ${user.NomeUsuario}",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.placeholder_image)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = user.NomeUsuario ?: "Usuário sem nome",
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontFamily = FontFamily(Font(R.font.lexend))
            )
            Text(
                text = "@${user.LoginUsuario ?: "login.indisponivel"}",
                fontSize = 12.sp,
                color = Color.Gray,
                fontFamily = FontFamily(Font(R.font.lexend))
            )
        }
    }
}

@Composable
fun SearchUserScreen(
    navController: NavController,
    dataViewModel: DataViewModel,
    userViewModel: UserViewModel
) {
    var searchTerm by remember { mutableStateOf("") }
    val allUsersResult by userViewModel.allUsers.observeAsState()
    val isLoading by userViewModel.isLoading.observeAsState(false)

    LaunchedEffect(Unit) {
        userViewModel.getAllUsers()
    }

    val filteredUsers by remember(searchTerm, allUsersResult) {
        derivedStateOf {
            val allUsers = allUsersResult?.getOrNull() ?: emptyList()

            if (searchTerm.isBlank()) {
                allUsers // Se a busca for vazia, retorna todos
            } else {
                val searchTermLower = searchTerm.lowercase()
                allUsers.filter { user ->
                    val nameMatch = user.NomeUsuario?.lowercase()?.contains(searchTermLower) == true
                    val loginMatch = user.LoginUsuario?.lowercase()?.contains(searchTermLower) == true
                    nameMatch || loginMatch
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BACKGROUND_COLOR)
    ) {
        DefaultBackIcon(navController)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Barra de busca
            OutlinedTextField(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Digite para buscar...", color = ACCENT_COLOR) },
                shape = RoundedCornerShape(25.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Pesquisar",
                        tint = ACCENT_COLOR
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = WAVE_COLOR,
                    focusedBorderColor = ACCENT_COLOR,
                    cursorColor = ACCENT_COLOR,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lógica de exibição dos resultados
            if (isLoading && filteredUsers.isEmpty()) {
                CircularProgressIndicator()
            } else {
                allUsersResult?.let { result ->
                    result.onSuccess {
                        // O 'derivedStateOf' já cuida da filtragem
                        if (filteredUsers.isEmpty()) {
                            Text(
                                text = "Nenhum usuário encontrado",
                                color = Color.Gray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val usersToShow = filteredUsers.filter { it.ID != dataViewModel.usuarioLogado?.ID }
                                items(usersToShow, key = { it.ID }) { user ->
                                    SearchedUserProfileItem(user, navController, dataViewModel)
                                }
                            }
                        }
                    }.onFailure { error ->
                        Text(
                            text = "Erro ao carregar usuários: ${error.message}",
                            color = Color.Red,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}