package com.thedirone.multiplayer_tic_tac_toe.features.ui.pages

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.thedirone.multiplayer_tic_tac_toe.core.utils.Server
import com.thedirone.multiplayer_tic_tac_toe.core.utils.Vertically
import com.thedirone.multiplayer_tic_tac_toe.features.ui.widgets.AppAlertDialog
import com.thedirone.multiplayer_tic_tac_toe.features.ui.widgets.GameBoard
import com.thedirone.multiplayer_tic_tac_toe.features.viewmodels.ServerViewModel


@Composable
fun ServerPageScreen(navController: NavController, ipAddr: String, context: Context) {
    Log.d("ServerPage", "is rendering...")
    val serverVM: ServerViewModel = viewModel()
    val statusMsgState = serverVM.serverStatus.observeAsState()
    val gameArray = serverVM.gameArrayInfo.observeAsState()
    val isOpponentWin = serverVM.isOpponentWon.observeAsState()
    val amIWon = serverVM.amIWon.observeAsState()
    val isMatchDraw = serverVM.isMatchDraw.observeAsState()
    val isConnectedWithClient = serverVM.isConnectedWithClinet.observeAsState()
    val shouldShowResetGameDialog = remember {
        mutableStateOf<Boolean>(false)
    }

    remember {
        serverVM.apply {
            startServer()
        }
        null
    }

    if (isConnectedWithClient.value == true) {

        if(serverVM.isServerTurn){
            Toast.makeText(context,"Your Turn!", Toast.LENGTH_SHORT).show()
        }

        if (shouldShowResetGameDialog.value) {
            AlertDialog(
                properties = DialogProperties(dismissOnBackPress = true,dismissOnClickOutside = true),
                icon = {
                    Icon(Icons.Rounded.Warning, contentDescription = "Dialog Icon")
                },
                title = {
                    Text(text = "Resetting Game!")
                },
                text = {
                    Text(text = "Are you sure?",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                onDismissRequest = {

                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            serverVM.sendResetGameRequest()
                            shouldShowResetGameDialog.value = false
                        }
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            shouldShowResetGameDialog.value = false
                        }
                    ) {
                        Text("No")
                    }
                }
            )
        }

        if (isOpponentWin.value == true) {
            AppAlertDialog(
                onPlayAgainRequest = {
                    serverVM.sendResetGameRequest()
                },
                onExitRequest = {
                    navController.navigateUp()
                    serverVM.closeServer()
                },
                dialogTitle = "You Loose!",
                dialogText = "😢Better luck next time😢",
                icon = Icons.Default.Warning
            )
        }

        if (amIWon.value == true) {
            AppAlertDialog(
                onPlayAgainRequest = {
                    serverVM.sendResetGameRequest()
                },
                onExitRequest = {
                    navController.navigateUp()
                    serverVM.closeServer()
                },
                dialogTitle = "Booyah!",
                dialogText = "You won the match✌️",
                icon = Icons.Default.Done
            )
        }

        if (isMatchDraw.value == true) {
            AppAlertDialog(
                onPlayAgainRequest = {
                    serverVM.sendResetGameRequest()
                },
                onExitRequest = {
                    navController.navigateUp()
                    serverVM.closeServer()
                },
                dialogTitle = "Oops!",
                dialogText = "The match is draw😜",
                icon = Icons.Default.Clear
            )
        }

        GameBoard(
            gameArr = gameArray.value ?: IntArray(9),
            statusMsg = statusMsgState.value,
            onClickedBtn = { pos ->
                if (serverVM.isServerTurn) {
                    serverVM.sendDataWithPositionToClient(pos = pos)
                } else {
                    Toast.makeText(context,"Opponent's Turn!", Toast.LENGTH_SHORT).show()
                }
                Log.d("SelectedPos", pos.toString())
            },
            onResetButtonClick = {
                shouldShowResetGameDialog.value = true
            }
        )
    } else if (isConnectedWithClient.value == false) {
        ServerQrShowPage(ipAddr = ipAddr)
    }

    // Handling onBackPressed
    BackHandler {
        serverVM.closeServer()
        navController.navigateUp()
    }
}