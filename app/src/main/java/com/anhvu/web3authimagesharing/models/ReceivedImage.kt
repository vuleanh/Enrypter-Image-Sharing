package com.anhvu.web3authimagesharing.models

import com.anhvu.web3authimagesharing.utils.FILE_NAME
import com.anhvu.web3authimagesharing.utils.SENDER_EMAIL
import com.google.firebase.firestore.QueryDocumentSnapshot

data class ReceivedImage(
    val fileName: String,
    val senderEmail: String,
) {
    constructor(snapshot: QueryDocumentSnapshot) : this(
        snapshot[FILE_NAME] as String,
        snapshot[SENDER_EMAIL] as String
    )
}
