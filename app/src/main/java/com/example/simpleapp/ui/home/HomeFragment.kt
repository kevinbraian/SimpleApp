package com.example.simpleapp.ui.home

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.simpleapp.R
import com.example.simpleapp.objects.Shop
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    val TAG = "HomeFragment"
    private var db = FirebaseFirestore.getInstance()
    private var arShops = arrayListOf<Shop>()
    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */

        var latLng = LatLng(-31.411665522206032, -64.1913843825139)
        val placesClient : PlacesClient = Places.createClient(requireActivity())
        //permissions and get location
        // Use fields to define the data types to return.
        val placeFields: List<Place.Field> = listOf(Place.Field.NAME)

        val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        // Use the builder to create a FindCurrentPlaceRequest.
        val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)
        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (ContextCompat.checkSelfPermission(requireActivity(), ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), ACCESS_FINE_LOCATION)) {
                Toast.makeText(activity, "Para una mejor experiencia se necesitan los permisos", Toast.LENGTH_SHORT).show()
            } else {
                // No explanation needed, we can request the permission.

                val locationRequest = LocationRequest.create()
                val REQUEST_CHECK_SETTINGS = 0x1
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                locationRequest.interval = 10000
                locationRequest.fastestInterval = 10000 / 2

                val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                builder.setAlwaysShow(true)

                val task = LocationServices.getSettingsClient(requireActivity()).checkLocationSettings(builder.build())

                task.addOnSuccessListener { _ ->
                    // All location settings are satisfied. The client can initialize
                    // location requests here.
                    ActivityCompat.requestPermissions(requireActivity(),
                        arrayOf(ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
                }

                task.addOnFailureListener { exception ->
                    if (exception is ResolvableApiException){
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            exception.startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS)
                        } catch (sendEx: IntentSender.SendIntentException) {
                            // Ignore the error.
                        }
                    }
                }
            }
        }

        val placeResponse = placesClient.findCurrentPlace(request)
        placeResponse.addOnCompleteListener { task1 ->
            if (task1.isSuccessful) {
                val response = task1.result
                for (placeLikelihood: PlaceLikelihood in response?.placeLikelihoods
                    ?: emptyList()) {
                    latLng = placeLikelihood.place.latLng!!
                    break
                }
            } else {
                Toast.makeText(activity, "Hubo un error obteniendo tu ubicacion", Toast.LENGTH_SHORT).show()
            }
        }
        googleMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))

        db.collection("shops")
            .addSnapshotListener { result, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed", e)
                    return@addSnapshotListener
                }
                arShops.clear()
                arShops.addAll(result!!.toObjects(Shop::class.java))

                for (shop in arShops) {
                    val geoPot = LatLng(shop.lat, shop.long)
                    googleMap.addMarker(MarkerOptions().position(geoPot).title(shop.name))
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val apiKey = "@strings/api_key"

        if(!Places.isInitialized()){
            Places.initialize(activity?.applicationContext!!, apiKey)
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }


}