package com.yourpackage.services

import SharedPreferencesService
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

    fun getAllDocuments(
        collectionName: String,
        callback: (QuerySnapshot?) -> Unit
    ): ListenerRegistration {
        return firestore.collection(collectionName)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    callback(null)
                } else {
                    callback(snapshot)
                }
            }
    }


    fun getDocumentsWithEqualToConditions(
        collectionName: String,
        conditions: Map<String, Any>,
        callback: (QuerySnapshot?) -> Unit
    ): ListenerRegistration {

        var query: Query = firestore.collection(collectionName)

        conditions.forEach { (field, value) ->
            query = query.whereEqualTo(field, value)
        }

        return query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("FirestoreService", "Condition Error: ${e.message}")
                callback(null)
            } else {
                callback(snapshot)
            }
        }
    }

    fun getDocumentsWithEqualToConditionsOnce(
        collectionName: String,
        conditions: Map<String, Any>,
        callback: (QuerySnapshot?) -> Unit
    ) {
        var query: Query = firestore.collection(collectionName)

        conditions.forEach { (field, value) ->
            query = query.whereEqualTo(field, value)
        }

        query.get()
            .addOnSuccessListener { snapshot ->
                callback(snapshot)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreService", "Condition Error: ${e.message}")
                callback(null)
            }
    }

    suspend fun getDocumentsWithEqualToConditionsOnceSuspend(
        collectionName: String,
        conditions: Map<String, Any>
    ): QuerySnapshot? {
        return try {
            var query: Query = firestore.collection(collectionName)
            conditions.forEach { (field, value) ->
                query = query.whereEqualTo(field, value)
            }
            query.get().await()
        } catch (e: Exception) {
            Log.e("FirestoreService", "Condition Error: ${e.message}")
            null
        }
    }



    suspend fun updateDocument(
        collectionName: String,
        docId: String,
        data: Map<String, Any>
    ): Boolean {
        return try {
            firestore.collection(collectionName).document(docId).update(data).await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreService", "Update Document Error: ${e.message}")
            false
        }
    }

    suspend fun updateDocument(
        collectionName: String,
        docId: String,
        data: HashMap<String, Any?>,
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

    fun getDashboardStatistics(
        role: types.UserRole,
        houseNo: String? = null,
        callback: (Map<String, Int>) -> Unit
    ): List<ListenerRegistration> {
        val registrations = mutableListOf<ListenerRegistration>()
        val stats = mutableMapOf<String, Int>()

        val updateCallback = {
            callback(stats.toMap())
        }

        when (role) {
            types.UserRole.Admin -> {
                registrations.add(firestore.collection(DatabaseTableName.HOUSES).addSnapshotListener { v, _ ->
                    stats["totalHouses"] = v?.size() ?: 0
                    updateCallback()
                })
                registrations.add(firestore.collection(DatabaseTableName.SECURITY_GUARDS).addSnapshotListener { v, _ ->
                    stats["totalGuards"] = v?.size() ?: 0
                    updateCallback()
                })
                registrations.add(firestore.collection(DatabaseTableName.ANNOUNCEMENTS).addSnapshotListener { v, _ ->
                    stats["totalAnnouncements"] = v?.size() ?: 0
                    updateCallback()
                })
                registrations.add(firestore.collection(DatabaseTableName.VISITOR).addSnapshotListener { v, _ ->
                    stats["totalVisitors"] = v?.size() ?: 0
                    updateCallback()
                })
                registrations.add(firestore.collection(DatabaseTableName.COMPLAINT).addSnapshotListener { v, _ ->
                    stats["totalComplaints"] = v?.size() ?: 0
                    updateCallback()
                })
                registrations.add(firestore.collection(DatabaseTableName.VISITOR).whereEqualTo("isBlocked", true).addSnapshotListener { v, _ ->
                    stats["totalBlockedVisitors"] = v?.size() ?: 0
                    updateCallback()
                })
            }
            types.UserRole.HouseOwner -> {
                if (houseNo != null) {
                    registrations.add(firestore.collection(DatabaseTableName.VISITOR).whereEqualTo("houseNo", houseNo).addSnapshotListener { v, _ ->
                        stats["myVisitors"] = v?.size() ?: 0
                        updateCallback()
                    })
                    registrations.add(firestore.collection(DatabaseTableName.VISITOR).whereEqualTo("houseNo", houseNo).whereEqualTo("status", types.VisitorStatus.Pending.name).addSnapshotListener { v, _ ->
                        stats["pendingRequests"] = v?.size() ?: 0
                        updateCallback()
                    })
                    registrations.add(firestore.collection(DatabaseTableName.ANNOUNCEMENTS).addSnapshotListener { v, _ ->
                        stats["totalAnnouncements"] = v?.size() ?: 0
                        updateCallback()
                    })
                    registrations.add(firestore.collection(DatabaseTableName.COMPLAINT).whereEqualTo("createdHouseNo", houseNo).addSnapshotListener { v, _ ->
                        stats["myComplaints"] = v?.size() ?: 0
                        updateCallback()
                    })
                    registrations.add(firestore.collection(DatabaseTableName.VISITOR).whereEqualTo("houseNo", houseNo).whereEqualTo("isBlocked", true).addSnapshotListener { v, _ ->
                        stats["myBlockedVisitors"] = v?.size() ?: 0
                        updateCallback()
                    })
                    registrations.add(firestore.collection(DatabaseTableName.QR_CODES).whereEqualTo("houseOwnerEmail", SharedPreferencesService.getUserEmail()).addSnapshotListener { v, _ ->
                        stats["myActiveQRCode"] = v?.size() ?: 0
                        updateCallback()
                    })
                }
            }
            types.UserRole.SecurityGuard -> {
                registrations.add(firestore.collection(DatabaseTableName.VISITOR).whereEqualTo("status", types.VisitorStatus.Inside.name).addSnapshotListener { v, _ ->
                    stats["visitorsInside"] = v?.size() ?: 0
                    updateCallback()
                })

                val calendar = java.util.Calendar.getInstance()
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val todayStart = com.google.firebase.Timestamp(calendar.time)

                registrations.add(firestore.collection(DatabaseTableName.VISITOR).whereGreaterThanOrEqualTo("entryTime", todayStart).addSnapshotListener { v, _ ->
                    stats["totalVisitorsToday"] = v?.size() ?: 0
                    updateCallback()
                })

                registrations.add(firestore.collection(DatabaseTableName.ANNOUNCEMENTS).addSnapshotListener { v, _ ->
                    stats["totalAnnouncements"] = v?.size() ?: 0
                    updateCallback()
                })
            }
        }
        return registrations
    }
}
