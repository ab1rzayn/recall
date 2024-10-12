package com.example.recall.Pages.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.sharp.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastCbrt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.recall.MainActivity
import com.example.recall.Pages.Chat.CallButton
import com.example.recall.app_ID
import com.example.recall.app_Sign

import com.example.recall.ui.theme.TextHighlight1
import com.example.recall.ui.theme.bubbleColorOne
import com.example.recall.ui.theme.buttonColor
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current as MainActivity
    LaunchedEffect(Unit) {
        Firebase.auth.currentUser?.let {
            context.initZegoService(
                appID = app_ID,
                appSign = app_Sign,
                userID = it.email!!,
                userName = it.email!!
            )
        }
    }

    val viewModel = hiltViewModel<HomeViewModel>()
    val channels = viewModel.channels.collectAsState()
    val addChannel = remember {
        mutableStateOf(false)
    }
    val sheetState = rememberModalBottomSheetState()
    Scaffold(
        floatingActionButton = {
        Box(modifier = Modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(TextHighlight1)
            .clickable {
                addChannel.value = true
            }) {
            Text(
                text = "  + Channel  ", modifier = Modifier.padding(16.dp), color = Color.Black
            )
        }
    },
        bottomBar = {
            BottomNavigation(navController = navController)
        }


    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()

        ) {
            LazyColumn {
                //Showing up the App name
                item{
                    Text(
                        text = "Recall",
                        color = TextHighlight1,
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Black),
                        modifier = Modifier.padding(16.dp)
                    )
                }

                item {
                    TextField(value = "",
                        onValueChange = {},
                        placeholder = { Text(text = "Search...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(
                                RoundedCornerShape(30.dp)
                            ),
                        textStyle = TextStyle(color = Color.LightGray),
                        colors = TextFieldDefaults.colors().copy(
                            focusedContainerColor = Color.DarkGray,
                            unfocusedContainerColor = Color.DarkGray,
                            focusedTextColor = Color.Gray,
                            unfocusedTextColor = Color.Gray,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray,
                            focusedIndicatorColor = Color.Gray
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search, contentDescription = null
                            )
                        })
                }

                item{
                    Spacer(Modifier.padding(20.dp))
                }


                // Fetching the channels from the database
                items(channels.value) { channel ->
                    Column {
                        ChannelItem(
                            channelName = channel.name,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                            shouldShowCallButtons = false,
                            backButtonEnabled = false, // Enable the back button
                            onClick = {
                                navController.navigate("chat/${channel.id}&${channel.name}")
                            },
                            onCall = {},
                            onBackClick = {}
                        )
                    }
                }

                }
            }
        }


    if (addChannel.value) {
        ModalBottomSheet(onDismissRequest = { addChannel.value = false }, sheetState = sheetState) {
            AddChannelDialog {
                viewModel.addChannel(it)
                addChannel.value = false
            }
        }
    }

}

@Composable
fun ChannelItem(
    channelName: String,
    modifier: Modifier,
    shouldShowCallButtons: Boolean = false,
    backButtonEnabled: Boolean = false, // New parameter to enable back button
    onClick: () -> Unit,
    onCall: (ZegoSendCallInvitationButton) -> Unit,
    onBackClick: () -> Unit // New parameter for back button click action
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.DarkGray.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clickable {
                    onClick()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (backButtonEnabled) {
                IconButton(onClick = { onBackClick() }) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowLeft, // Choose an appropriate icon
                        contentDescription = "Back",
                        Modifier.size(30.dp),
                        tint = Color.White // Color for back button
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(bubbleColorOne)

            )
            {
                Text(
                    text = channelName[0].uppercase(),
                    color = Color.White,
                    style = TextStyle(fontSize = 35.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }


            Text(text = channelName, modifier = Modifier.padding(8.dp), color = Color.White)
        }
        if (shouldShowCallButtons) {
            Row(
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                CallButton(isVideoCall = true, onCall)
                CallButton(isVideoCall = false, onCall)
            }
        }
    }
}


/**
 * A composable that displays a modal bottom sheet dialog allowing the user to input a name for a new channel.
 * The dialog is displayed when [addChannel] is true and hidden when false.
 * The dialog contains a text field for the user to input the channel name and a button to add the channel.
 * When the button is clicked, the dialog is closed and the [onAddChannel] callback is invoked with the channel name.
 */
@Composable
fun AddChannelDialog(onAddChannel: (String) -> Unit) {
    val channelName = remember {
        mutableStateOf("")
    }
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Add Channel")
        Spacer(modifier = Modifier.padding(8.dp))
        TextField(value = channelName.value, onValueChange = {
            channelName.value = it
        }, label = { Text(text = "Channel Name") }, singleLine = true)
        Spacer(modifier = Modifier.padding(8.dp))
        Button(onClick = { onAddChannel(channelName.value) }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Add")
        }
    }
}






@Composable
fun BottomNavigation(navController: NavController) {
    val items = listOf(
        BottomNavItem("Home", Icons.Sharp.Home, "home"),
        BottomNavItem("Profile", Icons.Filled.Person, "profile")
    )
    var selectedItem by remember { mutableStateOf(0) }

    NavigationBar (
        containerColor = Color.Transparent,
        tonalElevation = 0.dp
    ){
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.name, ) },
                label = { Text(item.name) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    navController.navigate(item.route)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = buttonColor,
                    selectedTextColor = TextHighlight1,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.DarkGray.copy(0.4f)
                ),
            )
        }
    }
}

data class BottomNavItem(
    val name: String,
    val icon: ImageVector,
    val route: String
)