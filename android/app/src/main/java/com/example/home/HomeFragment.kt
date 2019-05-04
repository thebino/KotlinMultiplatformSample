package com.example.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.*
import com.example.databinding.FragmentHomeBinding
import com.example.util.EventObserver
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.koin.androidx.viewmodel.ext.android.viewModel


private const val LOCATION_PERMISSION_REQUEST = 1234


class HomeFragment : Fragment() {
    val viewModel by viewModel<HomeViewModel>()

    private lateinit var mapView: MapView
    private var map: MapboxMap? = null
    private var loadedMapStyle: Style? = null
    private var locationComponent: LocationComponent? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(
            context!!,
            "pk.eyJ1IjoiZm9vYnlvbWRlIiwiYSI6ImNqdWw5OTZudTI0ZXQ0NW8ydzlpeHE2eGUifQ.cVs2kdMnaldukVnDcnApOw"
        )
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel.requestPermission.observe(viewLifecycleOwner, EventObserver {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", context?.packageName, null)
            intent.data = uri
            startActivity(intent)
        })

        viewModel.locationPermissionGranted.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it) {
                zoomToDeviceLocation()
            }
        })

        viewModel.events.observe(viewLifecycleOwner, Observer<ArrayList<Event>> { events ->
            if (map != null) {
                val markers = arrayListOf<Feature>()

                for (event in events) {
                    event.venue?.apply {
                        markers.add(Feature.fromGeometry(Point.fromLngLat(lon, lat)))
                    }
                }

                updateMarkers(markers)
            }
        })

        DataBindingUtil.inflate<FragmentHomeBinding>(inflater, R.layout.fragment_home, container, false).apply {
            lifecycleOwner = this@HomeFragment
            viewmodel = viewModel
            this@HomeFragment.mapView = this.mapView
            mapView.getMapAsync { map ->
                map.setStyle(Style.MAPBOX_STREETS) { style ->
                    // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                    this@HomeFragment.map = map
                    this@HomeFragment.loadedMapStyle = style

                    map.addOnMoveListener(object : MapboxMap.OnMoveListener {
                        override fun onMoveBegin(detector: MoveGestureDetector) = Unit

                        override fun onMove(detector: MoveGestureDetector) = Unit

                        override fun onMoveEnd(detector: MoveGestureDetector) {
                            val visibleRegion = map.projection.visibleRegion
                            val center = visibleRegion.latLngBounds.center
                            viewModel.refresh(center.latitude, center.longitude)
                        }
                    })

                    // default zoom to munich
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(48.1413532, 11.5585745), 10.0))

                    // init location component
                    locationComponent = map.locationComponent
                    locationComponent?.activateLocationComponent(
                        LocationComponentActivationOptions.builder(
                            context!!,
                            style
                        ).build()
                    )


                    initMeetupSymbolLayer(style)
                    zoomToDeviceLocation()
                }
            }

            return root
        }
    }


    override fun onStart() {
        super.onStart()

        mapView.onStart()
    }


    override fun onResume() {
        context?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (it.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    it.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ), LOCATION_PERMISSION_REQUEST
                    )
                } else {
                    viewModel.locationPermissionGranted.postValue(true)
                }
            } else {
                viewModel.locationPermissionGranted.postValue(true)
            }
        }
        mapView.onResume()
        zoomToDeviceLocation()

        super.onResume()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.locationPermissionGranted.postValue(true)
            } else {
                viewModel.locationPermissionGranted.postValue(false)
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun zoomToDeviceLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(context!!)) {
            locationComponent?.isLocationComponentEnabled = true
            locationComponent?.cameraMode = CameraMode.TRACKING
            locationComponent?.renderMode = RenderMode.NORMAL
            locationComponent?.zoomWhileTracking(10.0)

            val lastKnownLocation = locationComponent?.lastKnownLocation

            lastKnownLocation?.let {
                viewModel.refresh(it.latitude, it.longitude)
            }
        } else {
            warn("No location permission", null)
        }
    }


    private fun initMeetupSymbolLayer(style: Style) {
        style.addImage("meetup-icon-id", BitmapFactory.decodeResource(this.resources, R.drawable.meetup))

        style.addSource(
            GeoJsonSource(
                "source-id",
                GeoJsonOptions()
                    .withCluster(true)
                    .withClusterMaxZoom(15)
                    .withClusterRadius(30)
            )
        )

        style.addLayer(
            SymbolLayer("layer-id", "source-id").withProperties(
                iconImage("meetup-icon-id"),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconSize(.03f)
            )
        )
    }


    private fun updateMarkers(markers: ArrayList<Feature>) {
        // add markers as source to a new map layer
        loadedMapStyle?.let { style ->
            val source = style.getSourceAs<GeoJsonSource>("source-id")
            source?.setGeoJson(FeatureCollection.fromFeatures(markers.toTypedArray()))

        } ?: run {
            warn("no loaded map style found")
        }
    }
}
