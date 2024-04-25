package com.anhvu.web3authimagesharing.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anhvu.web3authimagesharing.models.ReceivedImage
import com.anhvu.web3authimagesharing.ui.theme.Web3AuthImageSharingTheme
import com.anhvu.web3authimagesharing.utils.CryptoManager
import com.anhvu.web3authimagesharing.utils.FILE_NAME
import com.anhvu.web3authimagesharing.utils.RECEIVED_IMAGE_COLLECTION
import com.anhvu.web3authimagesharing.utils.RECEIVER_EMAIL
import com.anhvu.web3authimagesharing.utils.SENDER_EMAIL
import com.anhvu.web3authimagesharing.utils.USER_COLLECTION
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlin.io.path.outputStream

class MainActivity : ComponentActivity() {

    private val db by lazy { Firebase.firestore }
    private val auth by lazy { Firebase.auth }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var receivedImages by remember {
                mutableStateOf(listOf<ReceivedImage>())
            }

            var userEmails by remember {
                mutableStateOf(listOf<String>())
            }

            var sendingStatusText: String? by remember {
                mutableStateOf(null)
            }

            var sendToEmail: String? by remember {
                mutableStateOf(null)
            }

            val launcher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == RESULT_OK) {
                        sendingStatusText = "Encrypting..."

                        val tempFile = kotlin.io.path.createTempFile()
                        val fileUri: Uri =
                            result.data?.data ?: return@rememberLauncherForActivityResult

                        fileUri.let { contentResolver.openInputStream(it) }.use { input ->
                            tempFile.outputStream().use { output ->
                                input?.copyTo(output)
                            }
                        }

                        val encryptedFileUri = Uri.fromFile(
                            CryptoManager().encryptFile(
                                this@MainActivity,
                                tempFile.toFile() ?: return@rememberLauncherForActivityResult
                            )
                        )

                        sendingStatusText = "Sending..."
                        val storageRef = Firebase.storage.reference

                        val fileName = System.currentTimeMillis().toString()

                        val imageRef = storageRef.child("images/$fileName")
                        imageRef.putFile(
                            encryptedFileUri
                        ).addOnSuccessListener { _ ->
                            sendingStatusText = null
                            db.collection(USER_COLLECTION).document(sendToEmail!!)
                                .collection(RECEIVED_IMAGE_COLLECTION).add(
                                    hashMapOf(
                                        FILE_NAME to fileName,
                                        SENDER_EMAIL to auth.currentUser?.email,
                                        RECEIVER_EMAIL to sendToEmail
                                    )
                                ).addOnCompleteListener {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Image encrypted and sent successfully", Toast.LENGTH_LONG
                                    ).show()
                                }
                        }.addOnFailureListener {
                            sendingStatusText = null
                            Toast.makeText(
                                this@MainActivity,
                                it.localizedMessage, Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }


            Web3AuthImageSharingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        MainBody(
                            currentUserEmail = auth.currentUser?.email,
                            sendToEmail = sendToEmail,
                            userEmails = userEmails,
                            receivedImages = receivedImages,
                            onPickImage = {
                                launcher.launch(Intent().apply {
                                    type = "image/*"
                                    action = Intent.ACTION_GET_CONTENT
                                })
                            },
                            sendingStatusText = sendingStatusText,
                            onDecrypt = {
                                startActivity(
                                    Intent(
                                        this@MainActivity,
                                        DecryptedImageActivity::class.java
                                    ).apply {
                                        putExtra(DecryptedImageActivity.FILE_NAME_EXT, it)
                                    })
                            },
                            onLogout = {
                                logout()
                            },
                            onSelectUser = {
                                sendToEmail = it
                            }
                        )
                    }
                }
            }

            LaunchedEffect(Unit) {
                auth.currentUser?.email?.let {
                    db.collection(USER_COLLECTION).document(it).collection(
                        RECEIVED_IMAGE_COLLECTION
                    )
                }?.addSnapshotListener { images, _ ->
                    receivedImages = images?.map { ReceivedImage(it) } ?: listOf()
                }

                db.collection(USER_COLLECTION).addSnapshotListener { users, error ->
                    val allUsers = (users?.map {
                        it.id
                    } ?: listOf())

                    userEmails = allUsers.dropWhile {
                        it == auth.currentUser?.email
                    }
                }
            }
        }
    }

    private fun logout() {
        startActivity(Intent(
            this@MainActivity, LoginActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK;
        })
    }
}

@Composable
fun MainBody(
    currentUserEmail: String?,
    sendToEmail: String?,
    receivedImages: List<ReceivedImage>,
    onPickImage: () -> Unit,
    onLogout: () -> Unit = {},
    onSelectUser: (String) -> Unit,
    onDecrypt: (String) -> Unit,
    sendingStatusText: String?,
    userEmails: List<String> = listOf()
) {

    Box(contentAlignment = Alignment.Center) {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "Current user: $currentUserEmail", fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Received Images (${receivedImages.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }

                Button(modifier = Modifier
                    .height(36.dp)
                    .align(Alignment.CenterVertically), onClick = {
                    Firebase.auth.signOut()
                    onLogout()
                }) {
                    Text(text = "Logout", fontSize = 16.sp)
                }
            }
            Box(modifier = Modifier.weight(1.0f)) {
                ReceivedImages(receivedImages, onDecrypt = onDecrypt)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.weight(1.0f)) {
                    var expanded by remember {
                        mutableStateOf(false)
                    }
                    Text(
                        text = "Send to: ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Row(modifier = Modifier.clickable {
                        expanded = true
                    }) {
                        Text(
                            text = sendToEmail ?: "Pick an user",
                        )
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        userEmails.map {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    expanded = false
                                    onSelectUser(it)
                                }
                            )
                        }
                    }
                }
                Button(
                    enabled = sendToEmail != null,
                    modifier = Modifier
                        .height(56.dp), onClick = onPickImage
                ) {
                    Text(text = sendingStatusText ?: "Encrypt and send")
                }
            }
        }
    }
}

@Composable
fun ReceivedImages(receivedImages: List<ReceivedImage>, onDecrypt: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(receivedImages) { image ->
            Box(modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, color = Color.Blue)
                .padding(16.dp)
                .clickable {
                    onDecrypt(image.fileName)
                }) {
                Column {
                    Text(text = "File name: ${image.fileName}")
                    Text(text = "Sender: ${image.senderEmail}")
                }
            }
        }

    }

}