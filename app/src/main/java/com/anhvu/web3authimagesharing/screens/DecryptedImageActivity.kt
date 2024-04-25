package com.anhvu.web3authimagesharing.screens

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.anhvu.web3authimagesharing.ui.theme.Web3AuthImageSharingTheme
import com.anhvu.web3authimagesharing.utils.CryptoManager
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import java.io.File

class DecryptedImageActivity : ComponentActivity() {

    private val storageRef by lazy { Firebase.storage.reference }

    private val fileName by lazy {
        intent.getStringExtra(FILE_NAME_EXT) ?: ""
    }

    companion object {
        const val FILE_NAME_EXT = "file_name_ext"
    }

    @OptIn(ExperimentalCoilApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            var decryptedFilePath: String? by remember {
                mutableStateOf(null)
            }
            Web3AuthImageSharingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (decryptedFilePath != null)
                        Image(
                            painter = rememberImagePainter(
                                File(
                                    filesDir,
                                    decryptedFilePath!!
                                )
                            ), contentDescription = ""
                        )
                }
            }

            LaunchedEffect(Unit) {
                val firebaseFileRef = storageRef.child("images/$fileName")

                val dir = File(filesDir, "encrypted")

                if (!dir.exists()) {
                    dir.mkdir()
                }

                val localFile = File(dir, fileName)

                firebaseFileRef.getFile(localFile).addOnSuccessListener {
                    CryptoManager().decrypt(this@DecryptedImageActivity, localFile, fileName)
                    Toast.makeText(
                        this@DecryptedImageActivity,
                        "Image Decrypted successfully", Toast.LENGTH_LONG
                    ).show()
                    decryptedFilePath = "decrypted/$fileName.jpg"
                }
            }
        }
    }
}