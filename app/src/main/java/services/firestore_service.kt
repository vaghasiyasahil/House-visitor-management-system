package com.yourpackage.services

import com.google.firebase.firestore.*
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import constants.DatabaseTableName
import kotlinx.coroutines.tasks.await

class FirestoreService {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getCollection(collectionName: String): CollectionReference {
        return firestore.collection(collectionName)
    }

    suspend fun addDocument(
        collectionName: String,
        data: HashMap<String, Any?>,
        docId: String? = null
    ): String? {
        return try {
            val docRef: DocumentReference

            if (docId != null) {
                docRef = firestore.collection(collectionName).document(docId)
                data["id"] = docId
                docRef.set(data).await()
            } else {
                docRef = firestore.collection(collectionName).add(data).await()
                docRef.update("id", docRef.id).await()
            }
            docRef.id
        } catch (e: Exception) {
            Log.e("FirestoreService", "Add Document Error: ${e.message}")
            null
        }
    }

    suspend fun getDocument(
        collectionName: String,
        docId: String
    ): DocumentSnapshot? {
        return try {
            firestore.collection(collectionName).document(docId).get().await()
        } catch (e: Exception) {
            Log.e("FirestoreService", "Get Document Error: ${e.message}")
            null
        }
    }

    fun getAllDocuments(collectionName: String): ListenerRegistration {
        return firestore.collection(collectionName)
            .addSnapshotListener { _, e ->
                if (e != null) {
                    Log.e("FirestoreService", "Listen failed: ${e.message}")
                }
            }
    }

    fun getDocumentsWithCondition(
        collectionName: String,
        field: String,
        isEqualTo: Any,
        callback: (QuerySnapshot?) -> Unit
    ): ListenerRegistration {
        return firestore.collection(collectionName)
            .whereEqualTo(field, isEqualTo)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FirestoreService", "Condition Error: ${e.message}")
                    callback(null)
                } else {
                    callback(snapshot)
                }
            }
    }

    suspend fun updateDocument(
        collectionName: String,
        docId: String,
        data: HashMap<String, Any>
    ): Boolean {
        return try {
            firestore.collection(collectionName).document(docId).update(data).await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreService", "Update Document Error: ${e.message}")
            false
        }
    }

    suspend fun deleteDocument(
        collectionName: String,
        docId: String
    ): Boolean {
        return try {
            firestore.collection(collectionName).document(docId).delete().await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreService", "Delete Document Error: ${e.message}")
            false
        }
    }

    fun getDocumentsQuery(
        collectionName: String,
        orderByField: String? = null,
        descending: Boolean = false,
        limit: Long? = null,
        callback: (QuerySnapshot?) -> Unit
    ): ListenerRegistration {

        var query: Query = firestore.collection(collectionName)

        if (orderByField != null) {
            query = query.orderBy(
                orderByField,
                if (descending) Query.Direction.DESCENDING else Query.Direction.ASCENDING
            )
        }

        if (limit != null) {
            query = query.limit(limit)
        }

        return query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("FirestoreService", "Query Error: ${e.message}")
                callback(null)
            } else {
                callback(snapshot)
            }
        }
    }

    suspend fun loginUser(
        email: String,
        password: String
    ): DocumentSnapshot? {
        return try {
            val querySnapshot = firestore.collection(DatabaseTableName.USER)
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .limit(1)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                querySnapshot.documents[0]
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Login Error: ${e.message}")
            null
        }
    }

}
