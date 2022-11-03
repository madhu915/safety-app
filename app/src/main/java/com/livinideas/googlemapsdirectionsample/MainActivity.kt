package com.livinideas.googlemapsdirectionsample

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDateTime
import java.util.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val intent = intent
//        val value = intent.getStringExtra("key1")
        // Initialize Google Maps and its callbacks
        idFABFeedback.setOnClickListener {
            Toast.makeText(this,"Please fill the feedback form",Toast.LENGTH_SHORT).show()
            val myIntent = Intent(this, FeedbackForm::class.java)
//            myIntent.putExtra("key1", "login")
            this.startActivity(myIntent)
        }
        val mapFragment = 
            supportFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap
//        added clear view option
        clearBtn.setOnClickListener {
            this.googleMap!!.clear()
            routeInfo.isVisible = !routeInfo.isVisible
        }

//        added search route option
        searchBtn.setOnClickListener {
            routeInfo.isVisible = !routeInfo.isVisible
            var start = startAddress.text.toString()
            var destination = destinationAddress.text.toString()
            Log.d("TAG", "onMapReady: $start")
            Log.d("TAG", "onMapReady: $destination")

            var latLngOrigin = getLocationFromAddress(start)
            var latLngDestination = getLocationFromAddress(destination)

            this.googleMap!!.addMarker(MarkerOptions().position(latLngOrigin).title(start))
            this.googleMap!!.addMarker(MarkerOptions().position(latLngDestination)
                .title(destination))
            this.googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 14.5f))
//            val path: MutableList<List<LatLng>> = ArrayList()
            val urlDirections = 
                "https://maps.googleapis.com/maps/api/directions/json?origin=${latLngOrigin?.latitude},${latLngOrigin?.longitude}&destination=${latLngDestination?.latitude},${latLngDestination?.longitude}&mode=driving&alternatives=true&key=AIzaSyDxRfxqFdgUlccCFu65mDq2C9Ao33G9q4A"
//            Log.d("latlg","${latLngDestination?.latitude}+${latLngDestination?.longitude}")
            val directionsRequest = @SuppressLint("SetTextI18n")
            object : StringRequest(Request.Method.GET, 
                urlDirections, 
                Response.Listener<String> { response ->
                    val jsonResponse = JSONObject(response)
                // Get routes
                val routes = jsonResponse.getJSONArray("routes")
                Log.d("size",
                    "${routes.length()}+${latLngOrigin?.latitude}+${latLngOrigin?.longitude}+${latLngDestination?.latitude}+${latLngDestination?.longitude}")
                //maximum five possible routes
                
                if (routes.length()==1) {
                    Log.d("size", "${routes.getJSONObject(0).get("summary")}")
                    val legs = routes.getJSONObject(0).getJSONArray("legs")
                    val dist = legs.getJSONObject(0).getJSONObject("distance").getString("text")
                    val eta = legs.getJSONObject(0).getJSONObject("duration").getString("text")

                    //log ETA and distance

                    var result = ""

                    Log.d("mul", "${dist}+${eta}")
                    result = StringBuilder().append("Distance covered: ").append(dist)
                        .append(" ETA: ").append(eta).append("\n").toString()

                    val steps = legs.getJSONObject(0).getJSONArray("steps")
                    val path: MutableList<List<LatLng>> = ArrayList()
                    for (i in 0 until steps.length()) {
                        val points =
                            steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                        path.add(PolyUtil.decode(points))
                    }
                    Log.d("psize", "${path.size}")
                    for (i in 0 until path.size) {
                        this.googleMap!!.addPolyline(PolylineOptions().addAll(path[i])
                            .color(Color.RED))

                    }
                    val texts = findViewById<TextView>(R.id.routeInfo)
                    texts.movementMethod = ScrollingMovementMethod()
                    Log.d("ress", "${result}")

                    val word: Spannable = SpannableString(result)

                    word.setSpan(ForegroundColorSpan(Color.parseColor("#FF8BC34A")),
                        0,
                        word.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    texts.setText(word)
                }

                if(routes.length()>1) {
                    val cols =
                        arrayOf(Color.parseColor("#FF8BC34A"),
                                Color.RED,
                                Color.BLUE,
                                Color.BLACK,
                                Color.CYAN)
                    var counter=-1
                    var mresult: String = ""

                    val texts = findViewById<TextView>(R.id.routeInfo)
                    val word: Spannable = SpannableString(mresult)

                    word.setSpan(ForegroundColorSpan(Color.parseColor("#FF8BC34A")),
                        0,
                        word.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    texts.setText(word)

                    //index for saferoute
                    val res = findSafestRoute(routes)


                    for (j in 0 until routes.length()) {
                        //maximum 5 routes allowed
                        if(j==5){
                            break;
                        }
                        val jsonObject = routes.getJSONObject(j)
                        val legs = jsonObject.getJSONArray("legs")
                        val dist= 
                            legs.getJSONObject(0).getJSONObject("distance").getString("text")
                        val eta=
                            legs.getJSONObject(0).getJSONObject("duration").getString("text")

                        //log ETA and distance

                        Log.d("mul","${dist}+${eta}")
                        counter += 1

                            val routeInfo =
                                StringBuilder().append("Distance covered: ").append(dist)
                                    .append(" ETA: ").appendLine(eta).appendLine().toString()
                            val wordTwo: Spannable = SpannableString(routeInfo)

                            wordTwo.setSpan(ForegroundColorSpan(cols[counter]),
                                0,
                                wordTwo.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            texts.append(wordTwo)



                        Log.d("countss","${counter}")
                        val steps = legs.getJSONObject(0).getJSONArray("steps")
                        val path: MutableList<List<LatLng>> = ArrayList()

                        for (i in 0 until steps.length()) {
                            val points =
                                steps.getJSONObject(i).getJSONObject("polyline")
                                    .getString("points")
                            path.add(PolyUtil.decode(points))
                        }
                        Log.d("psize", "${path.size}")
                        for (i in 0 until path.size) {
                            this.googleMap!!.addPolyline(PolylineOptions().addAll(path[i])
                                .color(cols[counter]))
                        }
                    }
                }

            }, 
            Response.ErrorListener { _ ->
            }) {}

            val requestQueue = Volley.newRequestQueue(this)
            requestQueue.add(directionsRequest)

        }
    }

    fun getLocationFromAddress(strAddress: String): LatLng? {
        val coder = Geocoder(this)
        val address: List<Address>?
        var p1: LatLng? = null
        try {
            address = coder.getFromLocationName(strAddress, 5)
            if (address == null) {
                return null
            }
            val location: Address = address[0]
            p1 = LatLng(location.latitude, location.longitude)

            return p1
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}



