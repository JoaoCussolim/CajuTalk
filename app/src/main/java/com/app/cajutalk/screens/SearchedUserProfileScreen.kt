package com.app.cajutalk.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.app.cajutalk.R
import com.app.cajutalk.ui.theme.BACKGROUND_COLOR
import com.app.cajutalk.ui.theme.HEADER_TEXT_COLOR

@Composable
fun ProfileHeader(userName: String, userImageURL: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = userName,
            fontSize = 30.sp,
            fontFamily = FontFamily(Font(R.font.baloo_bhai)),
            fontWeight = FontWeight(700),
            color = HEADER_TEXT_COLOR
        )
        AsyncImage(
            model = userImageURL,
            contentDescription = "Imagem do usuário",
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFDDC1)),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun SearchedUserProfileScreen(navController: NavController, dataViewModel: DataViewModel) {
    val user = dataViewModel.usuarioProcurado

    if (user == null) {
        Text("Erro: usuário  não encontrado")
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BACKGROUND_COLOR)
    ) {
        DefaultBackIcon(navController)
        ProfileHeader(user.name, user.imageUrl)

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x9EFFFAFA)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(480.dp)
                .align(alignment = Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Recado",
                    fontSize = 24.sp,
                    fontFamily = FontFamily(Font(R.font.baloo_bhai)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFF08080),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = user.message.ifBlank { "Nenhum recado disponível" },
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.lexend)),
                    fontWeight = FontWeight(400),
                    color = Color.Black
                )
            }
        }
    }
}
