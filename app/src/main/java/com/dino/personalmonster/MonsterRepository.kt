package com.dino.personalmonster

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MonsterRepository {
    private val db = FirebaseFirestore.getInstance()

    fun loadOwnedMonsterIds(onLoaded: (Set<String>) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        db.collection("results")
            .document(userId)
            .collection("ownedMonsters")
            .get()
            .addOnSuccessListener { snapshot ->
                val ownedIds = snapshot.documents.map { it.id }.toSet()
                onLoaded(ownedIds)
            }
            .addOnFailureListener {
                onLoaded(emptySet())
            }

    }

}