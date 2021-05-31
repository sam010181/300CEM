package org.tensorflow.lite.examples.styletransfer

import android.R
import android.app.ListActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.firebase.firestore.FirebaseFirestore
import org.tensorflow.lite.examples.styletransfer.Login
import java.util.*

class allimage : ListActivity() {
    private val TAG = "allimage"
    var arrayList1 = ArrayList<String?>()
    var arrayList2 = ArrayList<String?>()
    public override fun onCreate(icicle: Bundle?) {
        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, arrayList1)
        super.onCreate(icicle)
        val db = FirebaseFirestore.getInstance()
        db.collection("Images")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val a = document.data["name"] as String?
                        val b = document.data["url"] as String?
                        arrayList1.add(a)
                        arrayList2.add(b)
                        //new DownloadImageTask((ImageView) findViewById(R.id.img1))
                        //       .execute(arrayList2.get(0));
                        listAdapter = adapter
                    }
                } else {
                    Log.w(TAG, "Error getting documents.", task.exception)
                }
            }
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        val item = listAdapter.getItem(position) as String
        val intent = Intent(this, viewimage::class.java)
        intent.putExtra("key", item)
        startActivity(intent)
    }
}