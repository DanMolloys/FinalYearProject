package com.example.finalyearprojectdm

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Arrays
import android.Manifest
import android.content.ContentValues.TAG
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.firebase.firestore.FieldPath


class MapsViewActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest

    private var mLastPolyline: Polyline? = null

    private val markerDirectionsDisplayed: HashMap<String, Boolean> = HashMap()

    private lateinit var groupId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapsview)

        // Get the group ID from the Intent
        groupId = intent.getStringExtra("GROUP_ID").toString()
        if (groupId.isNullOrEmpty()) {
            // Handle the error
            finish()
            return
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val mapSettings = mMap.uiSettings
        mapSettings.isZoomControlsEnabled = true
        mapSettings.isCompassEnabled = true
        mapSettings.isMyLocationButtonEnabled = true

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(applicationContext, "No permission", Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            mFusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 13f))
                }
            }
        }

        getLocation()
        displayAllUsersLocations()
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(applicationContext, "No permission", Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            mMap.isMyLocationEnabled = true
            mLocationRequest = LocationRequest()
            mLocationRequest.interval = 10000
            mLocationRequest.fastestInterval = 5000
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
            mFusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)

                        // Safely unwrap the lastLocation
                        val location = locationResult.lastLocation
                        if (location != null) {
                            val lat = location.latitude
                            val lng = location.longitude
                            val currentLocation = LatLng(lat, lng)
                            displayLocation(currentLocation)

                            val db = FirebaseFirestore.getInstance()
                            val userId = FirebaseAuth.getInstance().currentUser!!.uid
                            val userLocation: MutableMap<String, Any> = HashMap()
                            userLocation["latitude"] = lat
                            userLocation["longitude"] = lng
                            db.collection("users").document(userId)
                                .update(userLocation)
                                .addOnSuccessListener {
                                    Log.d(TAG, "User location updated successfully!")
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error updating user location", e)
                                }
                        } else {
                            Log.w(TAG, "No last location available")
                        }
                    }
                },
                null
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation()
                }
            }
        }
    }

    private fun displayAllUsersLocations() {
        //get group ID and take the userIds
        GroupChatUtil.getGroupUsers(groupId) { userIds ->
            val db = FirebaseFirestore.getInstance()
            db.collection("users").whereIn(FieldPath.documentId(), userIds).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            val lat = document.getDouble("latitude")
                            val lng = document.getDouble("longitude")
                            Log.d(TAG, "User ID: ${document.id}, Latitude: $lat, Longitude: $lng")
                            if (lat != null && lng != null) {
                                val latlng = LatLng(lat, lng)
                                val longImageResourceId = document.getLong("imageResourceId")
                                var icon: BitmapDescriptor? = null
                                if (longImageResourceId != null) {
                                    val imageResourceId = longImageResourceId.toInt()
                                    val originalIcon =
                                        BitmapFactory.decodeResource(resources, imageResourceId)
                                    val newWidth = 150
                                    val newHeight = 150
                                    val resizedIcon = Bitmap.createScaledBitmap(
                                        originalIcon,
                                        newWidth,
                                        newHeight,
                                        false
                                    )
                                    icon = BitmapDescriptorFactory.fromBitmap(resizedIcon)
                                }

                                /*
                                mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
                                    override fun getInfoWindow(marker: Marker): View? {
                                        // Return null to use the default window frame with custom contents.
                                        return null
                                    }

                                    override fun getInfoContents(marker: Marker): View {
                                        // Inflate a custom view for the info window contents.
                                        val view = layoutInflater.inflate(R.layout.custom_info_window, null)
                                        val textView = view.findViewById<TextView>(R.id.infoWindowText)
                                        textView.text = "${marker.snippet}\nget directions?"
                                        return view
                                    }
                                })

                                 */

                                mMap.setOnMarkerClickListener { marker ->
                                    // If the marker already has a polyline, remove it.
                                    if (markerDirectionsDisplayed[marker.id] == true) {
                                        removeRouteToLocation()
                                        markerDirectionsDisplayed[marker.id] = false
                                    }
                                    // Otherwise, draw the route to the location.
                                    else {
                                        val destination = marker.position
                                        drawRouteToLocation(destination)
                                        markerDirectionsDisplayed[marker.id] = true
                                    }
                                    true
                                }

                                val markerOptions = MarkerOptions()
                                    .position(latlng)
                                    .title(document.getString("first"))
                                if (icon != null) {
                                    markerOptions.icon(icon)
                                }
                                // Add the marker to the map
                                mMap.addMarker(markerOptions)
                            }
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.exception)
                    }
                }
        }
    }

    private fun removeRouteToLocation() {
        mLastPolyline?.remove()
        mLastPolyline = null
    }

    private fun displayLocation(latlng: LatLng) {
        if (mMap != null) {
            val coder = Geocoder(this)
            try {
                val locations: List<Address>? = coder.getFromLocation(latlng.latitude, latlng.longitude, 1)
                if (locations != null) {
                    val add1: String = locations[0].getAddressLine(0)
                    val db = FirebaseFirestore.getInstance()
                    val userId = FirebaseAuth.getInstance().currentUser!!.uid
                    db.collection("users").document(userId)
                        .get()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val document: DocumentSnapshot? = task.result
                                if (document != null && document.exists()) {
                                    val longImageResourceId = document.getLong("imageResourceId")
                                    if (longImageResourceId != null) {
                                        val imageResourceId = longImageResourceId.toInt()
                                        val originalIcon = BitmapFactory.decodeResource(resources, imageResourceId)
                                        val newWidth = 150
                                        val newHeight = 150
                                        val resizedIcon = Bitmap.createScaledBitmap(originalIcon, newWidth, newHeight, false)
                                        val icon = BitmapDescriptorFactory.fromBitmap(resizedIcon)
                                        mMap.addMarker(
                                            MarkerOptions()
                                                .position(latlng)
                                                .title("Current location")
                                                .snippet(add1)
                                                .icon(icon)
                                        )
                                    }
                                }
                            }
                        }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun drawRouteToLocation(destination: LatLng) {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(applicationContext, "No permission", Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            mFusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val origin = LatLng(location.latitude, location.longitude)
                    val url = getDirectionsUrl(origin, destination)
                    FetchUrl().execute(url)
                }
            }
        }
    }

    private fun getDirectionsUrl(origin: LatLng, dest: LatLng): String {
        val strOrigin = "origin=" + origin.latitude + "," + origin.longitude
        val strDest = "destination=" + dest.latitude + "," + dest.longitude
        val sensor = "sensor=false"
        val parameters = "$strOrigin&$strDest&$sensor"
        val output = "json"
        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters&key=AIzaSyBe5JONAPznlBu5PsbqBABZEUhjwt_fRa0"
    }

    private inner class FetchUrl : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg url: String): String {
            var data = ""
            try {
                data = downloadUrl(url[0])
            } catch (e: Exception) {
                Log.d("Background Task", e.toString())
            }
            return data
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            val parserTask = ParserTask()
            parserTask.execute(result)
        }
    }

    @Throws(IOException::class)
    private fun downloadUrl(strUrl: String): String {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(strUrl)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connect()
            iStream = urlConnection.inputStream
            val br = BufferedReader(InputStreamReader(iStream))
            val sb = StringBuilder()
            var line: String?
            while (br.readLine().also { line = it } != null) {
                sb.append(line)
            }
            data = sb.toString()
            br.close()
        } catch (e: Exception) {
            Log.d("Exception downloading", e.toString())
        } finally {
            iStream?.close()
            urlConnection?.disconnect()
        }
        return data
    }

    private inner class ParserTask : AsyncTask<String, Int, List<List<HashMap<String, String>>>>() {
        override fun doInBackground(vararg jsonData: String): List<List<HashMap<String, String>>>? {
            var jObject: JSONObject? = null
            var routes: List<List<HashMap<String, String>>>? = null
            try {
                jObject = JSONObject(jsonData[0])
                val parser = DirectionsJSONParser()
                routes = parser.parse(jObject)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return routes
        }

        override fun onPostExecute(result: List<List<HashMap<String, String>>>?) {
            var points: ArrayList<LatLng>? = null
            var lineOptions: PolylineOptions? = null
            for (i in result!!.indices) {
                points = ArrayList()
                lineOptions = PolylineOptions()
                val path: List<HashMap<String, String>> = result[i]
                for (j in path.indices) {
                    val point: HashMap<String, String> = path[j]
                    val lat = java.lang.Double.parseDouble(point["lat"]!!)
                    val lng = java.lang.Double.parseDouble(point["lng"]!!)
                    val position = LatLng(lat, lng)
                    points.add(position)
                }
                lineOptions.addAll(points)
                lineOptions.width(12f)
                lineOptions.color(Color.rgb(255, 0, 0))
                val pattern: List<PatternItem> = Arrays.asList(Dash(30f), Gap(20f))
                lineOptions.pattern(pattern)
                lineOptions.startCap(RoundCap())
                lineOptions.endCap(RoundCap())
                lineOptions.geodesic(true)
            }
            if (mLastPolyline != null) {
                mLastPolyline?.remove()
            }
            mLastPolyline = lineOptions?.let { mMap.addPolyline(it) }
        }
    }

    companion object {
        private const val TAG = "MapsViewActivity"
    }
}