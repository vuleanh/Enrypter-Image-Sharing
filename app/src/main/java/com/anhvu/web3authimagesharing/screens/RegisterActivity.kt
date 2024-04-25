package com.anhvu.web3authimagesharing.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.anhvu.web3authimagesharing.ui.theme.Web3AuthImageSharingTheme
import com.anhvu.web3authimagesharing.utils.EMAIL
import com.anhvu.web3authimagesharing.utils.USER_COLLECTION
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class RegisterActivity : ComponentActivity() {

    private val auth: FirebaseAuth by lazy { Firebase.auth }
    private val db by lazy { Firebase.firestore }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Web3AuthImageSharingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(contentAlignment = Alignment.Center) {

                        var loading by remember {
                            mutableStateOf(false)
                        }

                        AuthForm(
                            buttonTitle = "Register",
                            loading = loading,
                            onButtonPressed = { email, password ->
                                loading = true
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnSuccessListener { _ ->
                                        loading = false
                                        val userEmail =
                                            auth.currentUser?.email
                                                ?: return@addOnSuccessListener
                                        db.collection(USER_COLLECTION).document(userEmail).set(
                                            hashMapOf(
                                                EMAIL to userEmail
                                            )
                                        ).addOnSuccessListener {
                                            startActivity(
                                                Intent(
                                                    this@RegisterActivity,
                                                    MainActivity::class.java
                                                ).apply {
                                                    flags =
                                                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK;
                                                }
                                            )
                                        }.addOnFailureListener {
                                            Toast.makeText(
                                                this@RegisterActivity,
                                                it.localizedMessage, Toast.LENGTH_LONG
                                            ).show()
                                        }

                                    }.addOnFailureListener {
                                        loading = false
                                        Toast.makeText(
                                            this@RegisterActivity,
                                            it.localizedMessage, Toast.LENGTH_LONG
                                        ).show()
                                    }
                            })
                    }
                }
            }
        }
    }
}