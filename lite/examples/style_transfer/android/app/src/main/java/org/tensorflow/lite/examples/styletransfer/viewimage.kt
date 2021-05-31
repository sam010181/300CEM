package org.tensorflow.lite.examples.styletransfer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URL

class viewimage : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var textView: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewimage)
        textView = findViewById<View>(R.id.text1) as TextView
        val db = FirebaseFirestore.getInstance()
        val intent = intent
        val value = intent.getStringExtra("key")
        db.collection("Images").whereEqualTo("name", value)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        Log.d(TAG, document.id + " => " + document.data["name"])
                        val a = document.data["name"] as String?
                        val b = document.data["url"] as String?
                        textView!!.setText("name:"+a)
                        DownloadImageTask(
                            findViewById<View>(
                                R.id.img1
                            ) as ImageView
                        )
                            .execute(b)
                    }
                } else {
                    Log.w(TAG, "Error getting documents.", task.exception)
                }
            }
    }
}
private class DownloadImageTask(var bmImage: ImageView) :
    AsyncTask<String?, Void?, Bitmap?>() {
    protected override fun doInBackground(vararg params: String?): Bitmap? {
        val urldisplay = params[0]
        var mIcon11: Bitmap? = null
        try {
            val `in` = URL(urldisplay).openStream()
            mIcon11 = BitmapFactory.decodeStream(`in`)
        } catch (e: Exception) {
            Log.e("Error", e.message)
            e.printStackTrace()
        }
        return mIcon11
    }

    override fun onPostExecute(result: Bitmap?) {
        bmImage.setImageBitmap(result)
    }
}
