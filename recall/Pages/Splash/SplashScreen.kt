package com.example.recall.Pages.Splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.recall.R
import com.example.recall.ui.theme.background_dark
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen (navController: NavController){
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = background_dark
    ) {
        Box(
          contentAlignment = Alignment.Center,
        ){
            Image(
                painter = painterResource(id = R.drawable.recall),
                contentDescription = "Recall Logo",
                Modifier.size(200.dp)
            )
        }
    }

    LaunchedEffect(Unit) {
        delay(1200)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            navController.navigate("home") {
                popUpTo("splash") {
                    inclusive = true
                }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") {
                    inclusive = true
                }
            }
        }
    }

}


