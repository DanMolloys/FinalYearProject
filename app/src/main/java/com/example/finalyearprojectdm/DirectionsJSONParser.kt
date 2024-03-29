package com.example.finalyearprojectdm

import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class DirectionsJSONParser {
    fun parse(jsonObject: JSONObject): List<List<HashMap<String, String>>> {
        val routes: MutableList<List<HashMap<String, String>>> = ArrayList()
        val routesJsonArray: JSONArray
        var legsJsonArray: JSONArray
        var stepsJsonArray: JSONArray
        try {
            routesJsonArray = jsonObject.getJSONArray("routes")
            for (i in 0 until routesJsonArray.length()) {
                legsJsonArray = (routesJsonArray[i] as JSONObject).getJSONArray("legs")
                val path: MutableList<HashMap<String, String>> = ArrayList()
                for (j in 0 until legsJsonArray.length()) {
                    stepsJsonArray = (legsJsonArray[j] as JSONObject).getJSONArray("steps")
                    for (k in 0 until stepsJsonArray.length()) {
                        var polyline: String
                        polyline =
                            ((stepsJsonArray[k] as JSONObject)["polyline"] as JSONObject)["points"] as String
                        val list = decodePolyline(polyline)
                        for (l in list.indices) {
                            val hm = HashMap<String, String>()
                            hm["lat"] = java.lang.Double.toString(list[l].latitude)
                            hm["lng"] = java.lang.Double.toString(list[l].longitude)
                            path.add(hm)
                        }
                    }
                    routes.add(path)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return routes
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly: MutableList<LatLng> = ArrayList()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val latLng = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(latLng)
        }
        return poly
    }
}

