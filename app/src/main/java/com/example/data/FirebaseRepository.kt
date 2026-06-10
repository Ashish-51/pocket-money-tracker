package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseRepository {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun getAuthUserFlow(): Flow<com.google.firebase.auth.FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun getUserProfile(uid: String): User? {
        return try {
            val doc = db.collection("users").document(uid).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun login(email: String, pass: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signup(name: String, email: String, pass: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user
            if (user != null) {
                try {
                    db.collection("users").document(user.uid)
                        .set(User(user.uid, name, email)).await()
                } catch (e: Exception) {
                    // Ignore Firestore errors to not block signup
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user
            if (user != null) {
                try {
                    // Ensure user document exists
                    val doc = db.collection("users").document(user.uid).get(com.google.firebase.firestore.Source.SERVER).await()
                    if (!doc.exists()) {
                        val fallbackName = user.email?.substringBefore("@")?.split(Regex("[._]"))?.joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } } ?: "User"
                        val name = if (user.displayName.isNullOrBlank()) fallbackName else user.displayName
                        db.collection("users").document(user.uid)
                            .set(User(user.uid, name!!, user.email ?: "")).await()
                    }
                } catch (e: Exception) {
                    // Ignore Firestore validation errors, allow login
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Transactions
    fun getTransactions(uid: String): Flow<List<Transaction>> = callbackFlow {
        val listener = db.collection("transactions")
            .whereEqualTo("userId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val transactions = snapshot.toObjects(Transaction::class.java)
                    trySend(transactions)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addTransaction(transaction: Transaction) {
        val uid = currentUserId ?: return
        val txId = UUID.randomUUID().toString()
        val txWithId = transaction.copy(transactionId = txId, userId = uid)
        db.collection("transactions").document(txId).set(txWithId).await()
    }

    suspend fun updateTransaction(transaction: Transaction) {
        if (transaction.transactionId.isEmpty()) return
        db.collection("transactions").document(transaction.transactionId).set(transaction).await()
    }

    suspend fun deleteTransaction(transactionId: String) {
        db.collection("transactions").document(transactionId).delete().await()
    }

    suspend fun deleteAllTransactions() {
        val uid = currentUserId ?: return
        try {
            val snapshot = db.collection("transactions").whereEqualTo("userId", uid).get().await()
            if (!snapshot.isEmpty) {
                val batch = db.batch()
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Budgets
    fun getBudgets(uid: String): Flow<List<Budget>> = callbackFlow {
        val listener = db.collection("budgets")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, e ->
                 if (e != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                 }
                 if (snapshot != null) {
                    val budgets = snapshot.toObjects(Budget::class.java)
                    trySend(budgets)
                 }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addBudget(budget: Budget) {
        val uid = currentUserId ?: return
        val budgetId = UUID.randomUUID().toString()
        val budgetWithId = budget.copy(budgetId = budgetId, userId = uid)
        db.collection("budgets").document(budgetId).set(budgetWithId).await()
    }

    suspend fun deleteBudget(budgetId: String) {
        db.collection("budgets").document(budgetId).delete().await()
    }
}
