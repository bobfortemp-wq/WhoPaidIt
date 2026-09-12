package com.bob.whopaidit.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.bob.whopaidit.R
import com.bob.whopaidit.data.model.User
import com.bob.whopaidit.data.preferences.UserPreferences
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    val userPreferences: UserPreferences,
) {

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    suspend fun signUp(fullName: String, email: String, password: String): User =
        withContext(Dispatchers.IO) {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = authResult.user ?: throw Exception("User creation failed")

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(fullName.trim())
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            val user = User(
                uid = firebaseUser.uid,
                fullName = fullName.trim(),
                email = email.trim(),
                createdAt = System.currentTimeMillis(),
            )

            saveUserToFirestore(user)
            user
        }

    suspend fun login(email: String, password: String): User =
        withContext(Dispatchers.IO) {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = authResult.user ?: throw Exception("Authentication failed")

            fetchUserFromFirestore(firebaseUser.uid) ?: run {
                val user = User(
                    uid = firebaseUser.uid,
                    fullName = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: email.trim(),
                    createdAt = System.currentTimeMillis(),
                )
                saveUserToFirestore(user)
                user
            }
        }

    suspend fun sendPasswordResetEmail(email: String): Unit =
        withContext(Dispatchers.IO) {
            auth.sendPasswordResetEmail(email.trim()).await()
        }

    suspend fun signInWithGoogle(context: Context): User =
        withContext(Dispatchers.IO) {
            val webClientId = context.getString(R.string.default_web_client_id)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts = false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(autoSelectEnabled = false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(context)
            val result = credentialManager.getCredential(context = context, request = request)

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val idToken = googleIdTokenCredential.idToken

            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val firebaseUser = authResult.user ?: throw Exception("Google Authentication failed")

            val user = User(
                uid = firebaseUser.uid,
                fullName = firebaseUser.displayName ?: googleIdTokenCredential.displayName ?: "",
                email = firebaseUser.email ?: googleIdTokenCredential.id,
                createdAt = System.currentTimeMillis(),
            )

            saveUserToFirestore(user)
            user
        }

    private suspend fun saveUserToFirestore(user: User) =
        withContext(Dispatchers.IO) {
            firestore.collection("users")
                .document(user.uid)
                .set(user)
                .await()
        }

    private suspend fun fetchUserFromFirestore(uid: String): User? =
        withContext(Dispatchers.IO) {
            val snapshot = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            if (snapshot.exists()) {
                snapshot.toObject(User::class.java)
            } else {
                null
            }
        }

    fun logout() {
        auth.signOut()
    }
}
