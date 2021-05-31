package org.tensorflow.lite.examples.styletransfer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class register : AppCompatActivity() {
    private var textView: TextView? = null
    private var mDatabase: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var EditText: EditText? = null
    private var EditText2: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mDatabase = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        textView = findViewById<View>(R.id.text1) as TextView
        EditText = findViewById<View>(R.id.email) as EditText
        EditText2 = findViewById<View>(R.id.password) as EditText
    }

    fun register(view: View?) {
        mAuth!!.createUserWithEmailAndPassword(
            EditText!!.text.toString(),
            EditText2!!.text.toString()
        )        .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Toast.makeText(baseContext, "Successful.",
                    Toast.LENGTH_SHORT).show()
                val intent = Intent(this,Startpage::class.java)
                startActivity(intent)
            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(baseContext, "Authentication failed.",
                    Toast.LENGTH_SHORT).show()
            }
        }

    }
}
