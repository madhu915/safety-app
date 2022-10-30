package com.livinideas.googlemapsdirectionsample

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackForm: AppCompatActivity() {
    lateinit var db: DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        db = FirebaseFirestore.getInstance().document("FeedbackForm/User")

        val submit = findViewById<View>(R.id.submit) as Button

        submit.setOnClickListener { view: View? ->
            Submit()
        }
    }

    private fun Submit() {
        val user=findViewById<View>(R.id.user) as EditText
        val area = findViewById<View>(R.id.area) as EditText
        val pc = findViewById<View>(R.id.pc) as EditText
        val bar = findViewById<View>(R.id.bar) as EditText
        val time = findViewById<View>(R.id.time) as EditText
        val pf = findViewById<View>(R.id.pf) as EditText
        val zone=findViewById<View>(R.id.zone) as EditText

        val User=user.text.toString().trim()
        val Area1 = area.text.toString().trim()
        val PC1 = pc.text.toString().trim()
        val BAR1 = bar.text.toString().trim()
        val TIME1 = time.text.toString().trim()
        val PF1 = pf.text.toString().trim()
        val zone1=zone.text.toString().trim()

        if (!User.isEmpty() && !Area1.isEmpty() && !PC1.isEmpty() && !BAR1.isEmpty() && !TIME1.isEmpty() && !PF1.isEmpty() && !zone1.isEmpty()) {
            try {
                val items = HashMap<String, Any>()
                items.put("Is Police_Station", PC1)
                items.put("Is Bar", BAR1)
                items.put("Time", TIME1)
                items.put("People Frequency", PF1)
                items.put("Area",Area1)
                items.put("Zone",zone1)


                db.collection(User).document("${User}_${zone1}_${TIME1}").set(items).addOnSuccessListener {
                        void: Void? ->
                    Toast.makeText(this,"Successfully uploaded", Toast.LENGTH_LONG).show()
                    user.text.clear()
                    area.text.clear()
                    pc.text.clear()
                    bar.text.clear()
                    time.text.clear()
                    pf.text.clear()
                    zone.text.clear()
                }
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            }

        } else {
            Toast.makeText(this, "Please fill up this field", Toast.LENGTH_LONG).show()
        }
    }

}