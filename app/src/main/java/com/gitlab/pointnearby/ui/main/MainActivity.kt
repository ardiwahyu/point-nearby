package com.gitlab.pointnearby.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.gitlab.pointnearby.R
import com.gitlab.pointnearby.data.local.sp.StatusManager
import com.gitlab.pointnearby.data.remote.model.Location
import com.gitlab.pointnearby.databinding.ActivityMainBinding
import com.gitlab.pointnearby.utils.Constants.DEFAULT_COUNT_POINT
import com.gitlab.pointnearby.utils.SentryErrorReport
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Suppress("DEPRECATION", "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : DaggerAppCompatActivity(), OnMapReadyCallback,
    SeekBar.OnSeekBarChangeListener {

    @Inject
    lateinit var statusManager: StatusManager
    private lateinit var mMap: GoogleMap
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationNow: LatLng
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var circle: Circle
    private var circleInitialized = false
    private var isPointChange = true
    private var radius = 0.0
    private var listLocation = arrayListOf<Location>()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }

    @Inject
    lateinit var listPointAdapter: ListPointAdapter
    private lateinit var marker: Marker
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<NestedScrollView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        try{
            mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            //default location
            val location = statusManager.getLocation()
            locationNow = LatLng(location.latitude, location.longitude)
            mapFragment.getMapAsync(this)

            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            getLocation()
            initComponent()
        }catch (e: Exception){
            SentryErrorReport().reportException(e)
            SentryErrorReport().reportException(e.localizedMessage)
            SentryErrorReport().reportException(e.message)
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationNow, 15.0f))
        if (isPointChange) {
            mMap.clear()
            mMap.addMarker(
                MarkerOptions().position(locationNow).title("Your location now")
            ).position = locationNow
            statusManager.storeLocation(Location(locationNow.latitude, locationNow.longitude))
            for ((i, locationPoint) in listLocation.withIndex()) {
                locationPoint.name = "Titik ${i + 1}"
                val markerOptions = MarkerOptions().position(
                    LatLng(
                        locationPoint.latitude,
                        locationPoint.longitude
                    )
                )
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_location))
                    .title(locationPoint.name)
                marker = mMap.addMarker(markerOptions)
                locationPoint.marker = marker
                locationPoint.distance = calculateDistance(
                    locationPoint,
                    Location(locationNow.latitude, locationNow.longitude)
                )
            }
            checkInCircle(sbCircle.progress, true)
        }
        if (circleInitialized)
            circle.remove()
        circle = mMap.addCircle(
            CircleOptions().center(locationNow).radius(radius).strokeColor(Color.TRANSPARENT)
                .fillColor(R.color.grey_transparent)
        )
        isPointChange = false
        circleInitialized = true
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (location != null) {
                locationNow = LatLng(location.latitude, location.longitude)
                mapFragment.getMapAsync(this)
            }
            isPointChange = true
        }
    }

    private fun initComponent() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.content_loading)
        dialog.setCancelable(false)
        refreshLocation.setOnClickListener {
            getLocation()
        }
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        sbCircle.setOnSeekBarChangeListener(this)
        mainViewModel.location.observe(this, Observer {
            listLocation.clear()
            listLocation.addAll(it)
            mapFragment.getMapAsync(this)
            isPointChange = true
        })
        mainViewModel.loading.observe(this, Observer {
            if (it) dialog.show() else dialog.cancel()
        })
        mainViewModel.error.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        })
        requestPoint(DEFAULT_COUNT_POINT)
        tvPointCount.text = DEFAULT_COUNT_POINT.toString()
        val handlers = Handler()
        btnBefore.setOnClickListener {
            var countPoint = tvPointCount.text.toString().toInt()
            if (countPoint > 0) {
                countPoint--
                tvPointCount.text = countPoint.toString()
                val runnable = Runnable {
                    requestPoint(countPoint)
                }
                handlers.removeCallbacksAndMessages(null)
                handlers.postDelayed(runnable, 1500)
            } else {
                Toast.makeText(this, "min value is 0", Toast.LENGTH_SHORT).show()
            }

        }
        btnNext.setOnClickListener {
            var countPoint = tvPointCount.text.toString().toInt()
            if (countPoint < 15) {
                countPoint++
                tvPointCount.text = countPoint.toString()
                val runnable = Runnable {
                    requestPoint(countPoint)
                }
                handlers.removeCallbacksAndMessages(null)
                handlers.postDelayed(runnable, 1500)
            } else {
                Toast.makeText(this, "max value is 15", Toast.LENGTH_SHORT).show()
            }
        }

        rvPointInCircle.layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        rvPointInCircle.adapter = listPointAdapter
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> getLocation()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    @SuppressLint("SetTextI18n")
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, p2: Boolean) {
        radius = progress.toDouble()
        val km = ((radius / 1000) * 100).roundToInt() / 100.0
        tvRadius.text = "( $km km )"
        mapFragment.getMapAsync(this)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        checkInCircle(seekBar!!.progress, false)
    }

    private fun calculateDistance(location1: Location, location2: Location): Int {
        val distancePerDegree = 111320.0
        val distanceX = (location1.latitude - location2.latitude) * distancePerDegree
        val distanceY = (location1.longitude - location2.longitude) * distancePerDegree
        return sqrt(distanceX.pow(2.0) + distanceY.pow(2.0)).toInt()
    }

    private fun checkInCircle(radius: Int, initNew: Boolean) {
        val listInCircle = arrayListOf<Location>()
        for (location in listLocation) {
            if (location.distance <= radius) {
                listInCircle.add(location)
            }
        }
        if (listInCircle.isNotEmpty()) tvStatusList.visibility = View.GONE
        else tvStatusList.visibility = View.VISIBLE
        listPointAdapter.submitList(listInCircle)
        if (!initNew) bottomSheetBehavior.state = STATE_EXPANDED
    }

    private fun requestPoint(pointCount: Int) {
        if (isNetworkConnected()) {
            mainViewModel.getPoints(locationNow.latitude, locationNow.longitude, pointCount)
        } else {
            showDialogError(pointCount)
        }
    }

    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnected
    }

    private fun showDialogError(pointCount: Int) {
        val sweetAlertDialogError = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Request Point Error")
            .setContentText("Please check your internet connection or try again later")
            .setCancelButton("EXIT") { sw ->
                sw.dismissWithAnimation()
                finish()
            }
            .setConfirmButton("TRY AGAIN") { sw ->
                sw.dismissWithAnimation()
                requestPoint(pointCount)
            }
        sweetAlertDialogError.setCancelable(false)
        sweetAlertDialogError.show()
    }

    class OnItemClickHandlers @Inject constructor(private val mainActivity: MainActivity) {
        fun onItemClick(marker: Marker) {
            marker.showInfoWindow()
            val nestedScrollView = mainActivity.findViewById<NestedScrollView>(R.id.bottomSheet)
            val bottomSheetBehavior = BottomSheetBehavior.from(nestedScrollView)
            nestedScrollView.fullScroll(View.FOCUS_UP)
            bottomSheetBehavior.state = STATE_COLLAPSED
        }
    }
}
