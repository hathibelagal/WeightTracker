package com.tutsplus.weighttracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(FirebaseAuth.getInstance().currentUser == null) {
            startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(arrayListOf(
                                AuthUI.IdpConfig.GoogleBuilder().build()
                        )).build(),
                1
            )
        } else {
            showUI()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1) {
            if(resultCode == Activity.RESULT_OK
                    && FirebaseAuth.getInstance().currentUser != null) {
                showUI()
            } else {
                Toast.makeText(this, "You must sign in to continue",
                        Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun showUI() {
        val weightsView = findViewById<RecyclerView>(R.id.weights)
        weightsView.layoutManager = LinearLayoutManager(this)

        val query = getUserDocument().collection("weights")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(90)

        val options = FirestoreRecyclerOptions.Builder<WeightEntry>()
                            .setQuery(query, WeightEntry::class.java)
                            .setLifecycleOwner(this)
                            .build()

        val adapter = object:FirestoreRecyclerAdapter<WeightEntry,
                WeightEntryVH>(options) {
            override fun onBindViewHolder(holder: WeightEntryVH,
                                          position: Int, model: WeightEntry) {
                holder.weightView?.text = "${model.weight} lb"
                val formattedDate = DateUtils.formatDateTime(applicationContext,
                                    model.timestamp,
                                    DateUtils.FORMAT_SHOW_DATE or
                                    DateUtils.FORMAT_SHOW_TIME or
                                    DateUtils.FORMAT_SHOW_YEAR)
                holder.timeView?.text = "On $formattedDate"
            }

            override fun onCreateViewHolder(parent: ViewGroup?,
                                            viewType: Int): WeightEntryVH {
                val layout = layoutInflater.inflate(R.layout.weight_entry, null)
                return WeightEntryVH(layout)
            }
        }

        weightsView.adapter = adapter
    }

    fun addWeight(v: View) {
        MaterialDialog.Builder(this)
                .title("Add Weight")
                .content("What's your weight today?")
                .inputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
                .input("weight in pounds", "", false,
                        { _, weight ->
                            getUserDocument()
                                .collection("weights")
                                .add(
                                    WeightEntry(
                                        weight.toString().toDouble(),
                                        Date().time
                                    )
                                )
                        })
                .show()
    }

    private fun getUserDocument():DocumentReference {
        val db = FirebaseFirestore.getInstance()
        val users = db.collection("users")
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        return users.document(uid)
    }
}
