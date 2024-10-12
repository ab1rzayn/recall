package com.example.recall

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recall.Pages.Chat.ChatScreen
import com.example.recalls.Auth.SignIn.SignInScreen
import com.example.recalls.Auth.SignUp.SignUpScreen
import com.example.recall.Pages.Home.HomeScreen
import com.example.recall.Pages.Profile.ProfileScreen
import com.example.recall.Pages.Splash.SplashScreen
//import com.google.firebase.auth.FirebaseAuth


@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun MainApp(){
    Surface(modifier = Modifier.fillMaxSize()) {
        val navController = rememberNavController()
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        val start = if (currentUser != null) "home" else "login"

        NavHost(navController = navController, startDestination = "splash") {


            composable("splash") {
                SplashScreen(navController)
            }
            composable("login") {
                SignInScreen(navController)
            }
            composable("signup") {
                SignUpScreen(navController)
            }
            composable("home") {
                HomeScreen(navController)
            }

            composable("profile") {
                ProfileScreen(navController)
            }

            composable(
                "chat/{channelId}&{channelName}",
                arguments = listOf(
                    navArgument("channelId") { type = NavType.StringType },
                    navArgument("channelName") { type = NavType.StringType }
                )
            ) {
                val channelId = it.arguments?.getString("channelId") ?: ""
                val channelName = it.arguments?.getString("channelName") ?: ""
                ChatScreen(navController, channelId, channelName)
            }
        }
    }
}




