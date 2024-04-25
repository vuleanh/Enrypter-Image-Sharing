package com.anhvu.web3authimagesharing.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.anhvu.web3authimagesharing.ui.theme.Web3AuthImageSharingTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : ComponentActivity() {

    private val auth: FirebaseAuth by lazy { Firebase.auth }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var showLogin by remember { mutableStateOf(false) }
            Web3AuthImageSharingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    if (showLogin) Box(contentAlignment = Alignment.Center) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            var loading by remember {
                                mutableStateOf(false)
                            }
                            AuthForm(onButtonPressed = { email, password ->
                                loading = true
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnSuccessListener { _ ->
                                        loading = false
                                        startActivity(Intent(
                                            this@LoginActivity, MainActivity::class.java
                                        ).apply {
                                            flags =
                                                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK;
                                        })
                                    }.addOnFailureListener {
                                        loading = false
                                        Toast.makeText(
                                            this@LoginActivity,
                                            it.localizedMessage, Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }, loading = loading)

                            Text(text = "Don't have account? Sign up.",
                                modifier = Modifier.clickable {
                                    startActivity(
                                        Intent(
                                            this@LoginActivity, RegisterActivity::class.java
                                        )
                                    )
                                })
                        }
                    }
                }
            }

            LaunchedEffect(Unit) {
                if (auth.currentUser != null) {
                    startActivity(Intent(
                        this@LoginActivity, MainActivity::class.java
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK;
                    })
                } else {
                    showLogin = true
                }
            }
        }

    }
}

@Composable
fun AuthForm(
    buttonTitle: String = "Login", onButtonPressed: (String, String) -> Unit, loading: Boolean = false
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TextField(value = email, label = {
            Text(text = "Email")
        }, onValueChange = {
            email = it

        })

        TextField(label = {
            Text(text = "Password")
        }, value = password, onValueChange = {
            password = it
        })

        Button(modifier = Modifier.height(56.dp), onClick = {
            onButtonPressed(email, password)
        }) {
            if (loading) CircularProgressIndicator(
                color = Color.White
            )
            else Text(text = buttonTitle)
        }
    }

}