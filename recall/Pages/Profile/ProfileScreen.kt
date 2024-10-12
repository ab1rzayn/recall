package com.example.recall.Pages.Profile

import android.util.Log
import android.widget.Space
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.recall.R
import com.example.recall.model.User
import com.example.recall.ui.theme.TextHighlight1
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


//AppBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val userProfile by viewModel.userProfile.collectAsState()


    // Log the userProfile data
    Log.d("ProfileScreen", "User Profile: $userProfile")

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = { Text(text = "", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = TextHighlight1
                        )
                    }
                },

                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ProfileCard(
                userProfile = userProfile, paddingValue = innerPadding, navController = navController
            )
        }
    }
}






@Composable
fun ProfileCard(userProfile: User?, paddingValue: PaddingValues, navController: NavController) {
    val navController = navController

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Profile Picture with Gradient Shadow
        Box(
            contentAlignment = Alignment.Center, // Align the image in the center of the gradient
            modifier = Modifier
                .size(130.dp) // Make the box slightly larger to account for the shadow
                .background(Color.Transparent) // Transparent background for the outer box
        ) {

            // Profile Image
            Image(
                painter = painterResource(id = R.drawable.profille_img),
                contentDescription = "Profile Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(130.dp) // Size of the profile image
                    .clip(CircleShape) // Clip to circle
                    .border(3.dp, TextHighlight1, CircleShape) // Optional border
            )
        }

        // Display User Name
        userProfile?.name?.let {
            Text(
                text = it,
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.opensans_medium)),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 5.dp)
            )
        }

        // Display Joining Date
        userProfile?.CreatedAt?.let {
            val date = Date(it)
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            Text(
                text = "Active Since : ${dateFormat.format(date)}",
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.opensans_regular)),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        PersonalInformation(
            paddingValue = paddingValue,
            viewModel = hiltViewModel(),
            navController = navController,
            userProfile = userProfile
        )
    }
}

@Composable
fun UpdateProfileDialog(viewModel: ProfileViewModel, onDismiss: () -> Unit) {

    val username = remember {
        mutableStateOf("")
    }
    val email = remember {
        mutableStateOf("")
    }
    val phone = remember {
        mutableStateOf("")
    }


    Column(
        modifier = Modifier.padding(5.dp) .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Update Profile")
        Spacer(modifier = Modifier.height(15.dp))
        TextField(
            value = username.value,
            onValueChange = { username.value =it },
            label = { Text("Name") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(5.dp))
        TextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(5.dp))
        TextField(
            value = phone.value,
            onValueChange = { phone.value = it },
            label = { Text("Phone") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = {
            //Update the profile
            viewModel.updateUserProfile(
                name = username.value.ifEmpty { null },
                email = email.value.ifEmpty { null },
                phone = phone.value.ifEmpty { null }
            )
            onDismiss()
        }, modifier = Modifier.fillMaxWidth().padding(30.dp)
        ) {
            Text("Update")
        }
    }
}
@Composable
fun InfoItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(Color.DarkGray.copy(alpha = 0.4f))
            .padding(19.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = TextHighlight1,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = TextStyle(fontFamily = FontFamily(Font(R.font.opensans_regular))),
                color = Color(0xFF9E9E9E),
                fontSize = 14.sp
            )
            Text(
                text = value,
                style = TextStyle(fontFamily = FontFamily(Font(R.font.opensans_regular))),
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInformation(paddingValue: PaddingValues,
                        viewModel: ProfileViewModel,
                        navController: NavController,
                        userProfile: User?
                        ) {


    val userEmail = userProfile?.email ?: ""
    val userPhone = userProfile?.phone ?: ""

    var showMdalBottomSheet by remember {
        mutableStateOf(false)
    }

    Log.d("ProfileScreen", "User Email: $userEmail")
    Log.d("ProfileScreen", "User Phone: $userPhone")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp, top = 40.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Personal Information",
                color = Color.White,
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.opensans_medium))
                ),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = { showMdalBottomSheet = true }) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit",
                    tint = TextHighlight1
                )
            }

        }
        Spacer(modifier = Modifier.height(15.dp))

        //Get user email
        InfoItem(icon = Icons.Filled.Email, label = "Email", value = userEmail)

        //Get user phone number
        Spacer(modifier = Modifier.height(2.dp))
        InfoItem(icon = Icons.Filled.Phone, label = "Phone", value = userPhone)


        Spacer(modifier = Modifier.height(2.dp))
        InfoItem(
            icon = Icons.Filled.CheckCircle,
            label = "Website",
            value = "www.randomweb.com"
        )
        Spacer(modifier = Modifier.height(2.dp))
        InfoItem(icon = Icons.Filled.LocationOn, label = "Location", value = "Chittagong")
        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = "Utilities",
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.opensans_medium))
            ),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(15.dp))

        //Log Out Button
        OutlinedButton(
            onClick = {
                viewModel.logout()
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
            },
            border = BorderStroke(1.dp, TextHighlight1),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = "Log Out",
                tint = TextHighlight1
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                "Log Out",
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.opensans_medium))
                ),
                color = TextHighlight1
            )
        }

        if (showMdalBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showMdalBottomSheet = false }
            ) {
                UpdateProfileDialog(viewModel = viewModel,
                    onDismiss ={showMdalBottomSheet =false})
            }


        }
    }


}
