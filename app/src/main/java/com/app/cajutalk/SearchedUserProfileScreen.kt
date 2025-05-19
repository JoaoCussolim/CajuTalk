package com.app.cajutalk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.app.cajutalk.ui.theme.ACCENT_COLOR
import com.app.cajutalk.ui.theme.BACKGROUND_COLOR
import com.app.cajutalk.ui.theme.HEADER_TEXT_COLOR
import com.app.cajutalk.ui.theme.WAVE_COLOR // Supondo que você queira usar para o card
import com.app.cajutalk.viewmodels.DataViewModel

@Composable
fun ProfileHeader(userName: String, userImageURL: String?, userLogin: String) { // Adicionado userLogin
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp), // Reduzido padding superior se o back icon estiver separado
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = userImageURL, // Usar um placeholder default
            contentDescription = "Imagem do usuário",
            modifier = Modifier
                .size(140.dp) // Ajustado tamanho
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant), // Cor de fundo mais sutil
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = userName,
            fontSize = 28.sp, // Ajustado tamanho
            fontFamily = FontFamily(Font(R.font.baloo_bhai)),
            fontWeight = FontWeight(700),
            color = HEADER_TEXT_COLOR
        )
        Text(
            text = "@${userLogin}",
            fontSize = 16.sp,
            fontFamily = FontFamily(Font(R.font.lexend)),
            color = Color.Gray
        )
    }
}

@Composable
fun SearchedUserProfileScreen(navController: NavController, dataViewModel: DataViewModel) {
    var userToDisplay by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) } // Adicionar estado de carregamento

    LaunchedEffect(Unit) {
        val fetchedUser = dataViewModel.getUsuarioParaVisualizar()
        userToDisplay = fetchedUser
        isLoading = false
        if (fetchedUser == null) {
            // Opcional: navegar de volta ou mostrar mensagem persistente
            // navController.popBackStack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BACKGROUND_COLOR)
    ) {
        // Ícone de Voltar sempre visível no topo
        DefaultBackIcon(navController = navController)

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = ACCENT_COLOR)
        } else if (userToDisplay == null) {
            Text(
                "Usuário não encontrado ou erro ao carregar.",
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center,
                fontFamily = FontFamily(Font(R.font.lexend)),
                color = Color.Gray
            )
        } else {
            // Conteúdo do perfil quando o usuário é carregado
            val user = userToDisplay!! // Sabemos que não é nulo aqui
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 56.dp) // Espaço para o ícone de voltar
                    .verticalScroll(rememberScrollState()) // Para permitir rolagem se o conteúdo for grande
            ) {
                ProfileHeader(
                    userName = user.name,
                    userImageURL = user.imageUrl,
                    userLogin = user.login // Passando o login para o header
                )

                Spacer(modifier = Modifier.height(24.dp)) // Espaço entre header e card

                Card(
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 0.dp, bottomEnd = 0.dp), // Cantos arredondados apenas no topo
                    colors = CardDefaults.cardColors(containerColor = WAVE_COLOR.copy(alpha = 0.9f)), // Usando WAVE_COLOR com transparência
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false) // Ocupa o espaço restante, mas não força preenchimento vertical
                        .padding(horizontal = 0.dp) // Remover padding horizontal do card se o conteúdo interno já tiver
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 32.dp), // Ajustado padding interno
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Recado",
                            fontSize = 22.sp, // Ajustado
                            fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                            fontWeight = FontWeight(400),
                            color = ACCENT_COLOR, // Usando ACCENT_COLOR
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = user.message.takeIf { it.isNotBlank() } ?: "Este usuário ainda não deixou um recado. ✨",
                            fontSize = 16.sp, // Ajustado
                            fontFamily = FontFamily(Font(R.font.lexend)),
                            fontWeight = FontWeight(400),
                            color = Color.Black.copy(alpha = 0.8f),
                            lineHeight = 24.sp
                        )
                        // Você pode adicionar mais informações do usuário aqui se desejar
                        // Ex: "Entrou em: [data]", "Salas em comum: [...]", etc.
                        // Spacer(modifier = Modifier.height(16.dp))
                        // Text("ID do Usuário: ${user.id}", fontSize = 14.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}