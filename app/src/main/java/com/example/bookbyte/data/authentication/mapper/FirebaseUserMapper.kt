package com.example.bookbyte.data.authentication.mapper

import com.example.bookbyte.domain.userManagementModule.models.User
import com.google.firebase.auth.FirebaseUser

class FirebaseUserMapper {
    fun toUser(firebaseUser: FirebaseUser): User {
        return User (
            userId = firebaseUser.uid,
            username = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: ""
        )
    }
}