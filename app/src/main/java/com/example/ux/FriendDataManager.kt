package com.example.ux

import android.content.Context
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


object FriendDataManager {
    val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    suspend fun fetchFriendDataForUser(userId: String): List<FriendData> {
        return suspendCancellableCoroutine { continuation ->
            val friendDataList = mutableListOf<FriendData>()

            try {
                val dbRef = Firebase.database.reference.child("moi").child(userId).child("friend")
                dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val friendUids = dataSnapshot.children.mapNotNull { it.key }
                        val friendInfoSnapshots = friendUids.map {
                            Firebase.database.reference.child("moi").child(it)
                        }

                        var fetchedCount = 0

                        friendInfoSnapshots.forEach { friendInfoSnapshot ->
                            friendInfoSnapshot.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val friendUid = snapshot.key
                                    if (friendUid!="placeholder"){
                                        val name = snapshot.child("username").value.toString()
                                        val statusMsg = snapshot.child("statusMsg").value.toString()
                                        val profileColor = snapshot.child("profileColor").value.toString()

                                        if (!name.equals("null")){
                                            val friendData = FriendData(name, statusMsg, profileColor, null,friendInfoSnapshot.key ?: "")
                                            friendDataList.add(friendData)
                                            System.out.println("name : "+name)
                                        } else {
                                            System.out.println("placeholder : "+name)
                                        }
                                    }
                                    fetchedCount++
                                    if (fetchedCount == friendInfoSnapshots.size) {
                                        continuation.resume(friendDataList)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    continuation.resumeWithException(Exception("Error fetching friend data"))
                                }
                            })
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        continuation.resumeWithException(Exception("Error fetching friend data"))
                    }
                })
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }

        }
    }

    fun getDrawableIntByFileName(context: Context, fileName: String): Int {
        return context.resources.getIdentifier(fileName, "drawable", context.packageName)
    }
}