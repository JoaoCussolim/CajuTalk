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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.app.cajutalk.R
import com.app.cajutalk.classes.User
import com.app.cajutalk.ui.theme.BACKGROUND_COLOR
import com.app.cajutalk.ui.theme.WAVE_COLOR

@Composable
fun RoomMemberItem(user: User, navController : NavController, dataViewModel: DataViewModel) {
    var expanded by remember { mutableStateOf(false) }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(8.dp)
            .background(color = WAVE_COLOR, shape = RoundedCornerShape(25.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .clickable {
                    dataViewModel.usuarioProcurado = user
                    navController.navigate("searched-user-profile")
                }
        ) {
            AsyncImage(
                model = user.imageUrl,
                contentDescription = "Ícone da Sala",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable {
                    dataViewModel.usuarioProcurado = user
                    navController.navigate("searched-user-profile")
                }
        ) {
            Text(
                text = user.name,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontFamily = FontFamily(Font(R.font.lexend))
            )
            Text(
                text = user.login,
                fontSize = 12.sp,
                color = Color.Gray,
                fontFamily = FontFamily(Font(R.font.lexend))
            )
        }

        if(dataViewModel.estadoSala.sala?.criador == mainUser && mainUser != user) {
            Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(text = {
                            Text(
                                text = "Remover da Sala",
                                fontFamily = FontFamily(Font(R.font.lexend))
                            )
                        }, onClick = {
                            expanded = false
                        })
                        DropdownMenuItem(text = {
                            Text(
                                text = "Banir da Sala",
                                fontFamily = FontFamily(Font(R.font.lexend))
                            )
                        }, onClick = {
                            expanded = false
                        })
                    }
                }
        }
    }
}


@Composable
fun RoomMembersScreen(navController: NavController, dataViewModel: DataViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BACKGROUND_COLOR)
    ) {
        DefaultBackIcon(navController)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp, start = 16.dp, end = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(dataViewModel.estadoSala.membros) { user ->
                    RoomMemberItem(user, navController, dataViewModel)
                }
            }
        }
    }
}