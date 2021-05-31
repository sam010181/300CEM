package org.tensorflow.lite.examples.styletransfer

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import org.tensorflow.lite.examples.styletransfer.databinding.ActivityHomepageBinding

class homepage : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)
        }
    fun page1(view: View?){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
    fun page2(view:View?){
        val intent = Intent(this, allimage::class.java)
        startActivity(intent)
    }
    fun page3(view:View?){
        val intent = Intent(this, getselfimage::class.java)
        startActivity(intent)
    }
}

