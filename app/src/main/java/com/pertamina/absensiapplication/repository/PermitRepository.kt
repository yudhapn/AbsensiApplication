package com.pertamina.absensiapplication.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.pertamina.absensiapplication.api.PermitApi
import com.pertamina.absensiapplication.model.CounterGlobal
import com.pertamina.absensiapplication.model.Permit
import com.pertamina.absensiapplication.model.User
import kotlinx.coroutines.tasks.await

class PermitRepository(private val service: PermitApi) {
    private val tag = PermitRepository::class.java.simpleName

    private val firestore = Firebase.firestore
    private val usersRef = firestore.collection("branch/f303/users")
    private val permitsRef = firestore.collection("branch/f303/allpermits")
    private val counterRef = firestore.collection("branch/f303/counter")

    suspend fun createPermitManual(userId: String, permit: Permit) = service.createPermitManualAsync(userId, permit).await()

    suspend fun createPermit(uid: String, permit: Permit) {
        val permitId = usersRef.document(uid).collection("permits").document().id
        permit.permitId = permitId
        usersRef.document("$uid/permits/$permitId").set(permit).await()
        permitsRef.document(permitId).set(permit).await()
    }

    suspend fun updatePermit(uid: String, permitId: String, permit: Permit) {
        usersRef.document("$uid/permits/$permitId").set(permit, SetOptions.merge()).await()
        permitsRef.document(permitId).set(permit, SetOptions.merge()).await()
    }

    suspend fun createAccount(email: String, password: String) = service.createAccountAsync(email, password).await()

    suspend fun getOrganicLeave() = service.getOrganicLeave().await()

    suspend fun getTkjpLeave() = service.getTkjpLeave().await()

    suspend fun updateAccount(uid: String, email: String, password: String) =
        service.updateAccountAsync(uid, email, password).await()

    suspend fun createUserData(uid: String, user: User) = service.createUserDataAsync(uid, user).await()

    suspend fun updateUser(user: User) = usersRef.document(user.userId).set(user, SetOptions.merge()).await()

    suspend fun getPermitsFirestore(uid: String): List<Permit> {
        var permits = listOf<Permit>()
        try {
            permits =
                usersRef.document(uid).collection("permits").orderBy("createdOn", Query.Direction.DESCENDING).get()
                    .await().toObjects()
            Log.d(tag, "Size list permits: ${permits.size}")
        } catch (e: FirebaseFirestoreException) {
            Log.e(tag, e.toString())
        }
        return permits
    }

    suspend fun getCounterGlobal(): CounterGlobal? {
        var counterGlobal: CounterGlobal? = CounterGlobal()
        try {
            val docSnapshow = counterRef.document("counterGlobal").get().await()
            counterGlobal = docSnapshow.toObject()
        } catch (e: FirebaseFirestoreException) {
            Log.e(tag, e.toString())
        }
        return counterGlobal
    }

    suspend fun getPermitsRequest(uid: String): List<Permit> {
        var permits = listOf<Permit>()
        try {
            permits =
                usersRef.document(uid).collection("permits").whereEqualTo("status.request", true)
                    .orderBy("createdOn", Query.Direction.DESCENDING)
                    .get().await()
                    .toObjects()
        } catch (e: FirebaseFirestoreException) {
            Log.e(tag, e.toString())
        }
        return permits
    }

    suspend fun getPermitsComplete(uid: String): List<Permit> {
        var permits = listOf<Permit>()
        try {
            permits =
                usersRef.document(uid).collection("permits").whereEqualTo("status.complete", true)
                    .orderBy("createdOn", Query.Direction.DESCENDING)
                    .limit(8).get().await()
                    .toObjects()
        } catch (e: FirebaseFirestoreException) {
            Log.e(tag, e.toString())
        }
        return permits
    }

    suspend fun getPermitsConfirm(uid: String): List<Permit> {
        Log.d(tag, "Status = Confirm Permit uid: $uid")
        val permits = mutableListOf<Permit>()
        try {
            val user: User? = usersRef.document(uid).get().await().toObject()
            if (user?.operationHead?.length == 0) { //oh
                if (user.standIn.size > 0) { // oh becomes pjs senior
                    val permitsOHPjsSenior: List<Permit> =
                            permitsRef.whereEqualTo("senior", uid).whereEqualTo("status.confirmBySenior", false)
                                    .whereEqualTo("status.confirmByOH", false).whereEqualTo("status.request", true)
                                    .orderBy("createdOn", Query.Direction.DESCENDING).get()
                                    .await()
                                    .toObjects()
                    val permitsOH: List<Permit> =
                            permitsRef.whereEqualTo("operationHead", uid).whereEqualTo("status.confirmBySenior", true)
                                    .whereEqualTo("status.confirmByOH", false).whereEqualTo("status.request", true)
                                    .orderBy("createdOn", Query.Direction.DESCENDING).get()
                                    .await()
                                    .toObjects()
                    permits.addAll(permitsOHPjsSenior)
                    permits.addAll(permitsOH)
                } else { //pure OH
                    val permitsOH: List<Permit> =
                            permitsRef.whereEqualTo("operationHead", uid).whereEqualTo("status.confirmBySenior", true)
                                    .whereEqualTo("status.confirmByOH", false).whereEqualTo("status.request", true)
                                    .orderBy("createdOn", Query.Direction.DESCENDING).get()
                                    .await()
                                    .toObjects()
                    Log.d(tag, "Status = Confirm Permit uid: $uid size list oh: ${permitsOH.size}")
                    permits.addAll(permitsOH)
                }

            } else if (user?.operationHead?.length != 0 && user?.senior?.length == 0) { //senior
                val permitsSenior: List<Permit> =
                    permitsRef.whereEqualTo("senior", uid).whereEqualTo("status.confirmBySenior", false)
                        .whereEqualTo("status.confirmByOH", false).whereEqualTo("status.request", true)
                        .orderBy("createdOn", Query.Direction.DESCENDING).get().await()
                        .toObjects()
                permits.addAll(permitsSenior)
                val permitsPjs: List<Permit> =
                    permitsRef.whereEqualTo("operationHead", uid).whereEqualTo("status.confirmBySenior", true)
                        .whereEqualTo("status.confirmByOH", false).whereEqualTo("status.request", true)
                        .orderBy("createdOn", Query.Direction.DESCENDING).get().await()
                        .toObjects()
                permits.addAll(permitsPjs)
            } else if (user?.operationHead?.length != 0 && user?.senior?.length != 0) { //senior
                if (user?.standIn?.size!! > 0) {
                    val permitsSenior: List<Permit> =
                        permitsRef.whereEqualTo("senior", uid).whereEqualTo("status.confirmBySenior", false)
                            .whereEqualTo("status.confirmByOH", false).whereEqualTo("status.request", true)
                            .orderBy("createdOn", Query.Direction.DESCENDING).get()
                            .await()
                            .toObjects()
                    permits.addAll(permitsSenior)
                    val permitsPjs: List<Permit> =
                        permitsRef.whereEqualTo("operationHead", uid).whereEqualTo("status.confirmBySenior", true)
                            .whereEqualTo("status.confirmByOH", false).whereEqualTo("status.request", true)
                            .orderBy("createdOn", Query.Direction.DESCENDING).get()
                            .await()
                            .toObjects()
                    permits.addAll(permitsPjs)
                }
            }
        } catch (e: FirebaseFirestoreException) {
            Log.e(tag, e.toString())
        }
        return permits
    }

    suspend fun getUser(uid: String): User? {
        var user: User? = User()
        try {
            val docSnapshow = usersRef.document(uid).get().await()
            user = docSnapshow.toObject()
        } catch (e: FirebaseFirestoreException) {
            Log.e(tag, e.toString())
        }
        return user
    }

    suspend fun getUsers(type: String): List<User> {
        var users = listOf<User>()
        try {
            val querySnapshot = when (type) {
                "all" -> usersRef.orderBy("organic").orderBy("position", Query.Direction.DESCENDING).get().await()
                "organic" -> usersRef.whereEqualTo("organic", true).orderBy("position", Query.Direction.DESCENDING).get().await()
                "tkjp" -> usersRef.whereEqualTo("organic", false).orderBy("senior", Query.Direction.DESCENDING)
                    .orderBy("position", Query.Direction.DESCENDING).get().await()
                "activeOrganic" -> usersRef.whereEqualTo("organic", true).whereEqualTo(
                    "leave",
                    false
                ).orderBy("name").get().await()
                "senior" -> usersRef.whereEqualTo("senior", "").orderBy("name").get().await()
                "operationHead" -> usersRef.whereEqualTo("operationHead", "").orderBy("name").get().await()
                else -> usersRef.get().await()
            }
            users = querySnapshot.toObjects()
            Log.d(tag, "size list: ${users.size}")
        } catch (e: FirebaseFirestoreException) {
            Log.e(tag, e.toString())
        }
        return users
    }
}