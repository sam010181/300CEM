package org.tensorflow.lite.examples.styletransfer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class Startpage : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startpage)
    }
    fun page1(view: View?){
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
    }
    fun page2(view:View?){
        val intent = Intent(this, register::class.java)
        startActivity(intent)
    }
}

