package org.tensorflow.lite.examples.styletransfer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class Login : AppCompatActivity() {
    private var TextView: TextView? = null
    private var TextView2: TextView? = null
    private var TextView3: TextView? = null
    private var mDatabase: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var EditText: EditText? = null
    private var EditText2: EditText? = null
    private val TAG = "MainActivity"
    var storage = FirebaseStorage.getInstance()
    var storageRef = storage.reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mDatabase = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        EditText = findViewById<View>(R.id.email) as EditText
        EditText2 = findViewById<View>(R.id.password) as EditText
    }

    fun example(view: View?) {
        mAuth!!.signInWithEmailAndPassword(EditText!!.text.toString(), EditText2!!.text.toString())
            .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val intent = Intent(this, homepage::class.java)
                startActivity(intent)
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "createUserWithEmail:success")
                val user = mAuth!!.currentUser
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                Toast.makeText(baseContext, "Authentication failed.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}