package com.hasanbilgin.kotlinmaps.view


import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.hasanbilgin.kotlinmaps.R
import com.hasanbilgin.kotlinmaps.databinding.ActivityMapsBinding
import com.hasanbilgin.kotlinmaps.model.Place
import com.hasanbilgin.kotlinmaps.roomdb.PlaceDao
import com.hasanbilgin.kotlinmaps.roomdb.PlaceDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

//GoogleMap.OnMapClickListener uzun tıklama için kondu her tıklamada aolan metotda içinde
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    //konum yöneticisi
    private lateinit var locationManager: LocationManager

    //konum dinleyicisi
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences
    private var trackBoolean: Boolean? = null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private lateinit var db: PlaceDatabase
    private lateinit var placeDao: PlaceDao
    val compositeDisposable = CompositeDisposable()
    var placeFromMain: Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerLauncher();
        sharedPreferences = this.getSharedPreferences("com.hasanbilgin.kotlinmaps", MODE_PRIVATE)
        trackBoolean = false
        selectedLatitude = 0.0
        selectedLongitude = 0.0

        db = Room.databaseBuilder(applicationContext, PlaceDatabase::class.java, "Places")
            //mainthread kullan söledik kullanıabilir ama küçük olduğu sorun yok
//            .allowMainThreadQueries()
            .build()

        placeDao = db.placeDao()

        binding.saveButton.isEnabled = false

    }

    //harita hazır olduğunda çalışan metot
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //uzun tıklanma için
        mMap.setOnMapLongClickListener(this)

        val intent = intent
        val info = intent.getStringExtra("info")

        if (info == "new") {

            binding.saveButton.visibility = View.VISIBLE
            binding.deleteButton.visibility = View.GONE

            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

            locationListener = object : LocationListener {

                override fun onLocationChanged(location: Location) {

                    trackBoolean = sharedPreferences.getBoolean("trackBoolean", false)

                    if (trackBoolean == false) {

                        val userLocation = LatLng(location.latitude, location.longitude);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                        sharedPreferences.edit().putBoolean("trackBoolean", true).apply()
                    }


                }

            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Snackbar.make(binding.root, "Permission needed for location", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission") {
                        //request permission
                        permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    }.show()
                } else {
                    //request permission
                    permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                }
            } else {
                //permission granted
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, locationListener)
                //en son locasyonu almak için
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null) {
                    val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15f))
                }
                mMap.isMyLocationEnabled = true
            }
        } else {
            mMap.clear()
            //?null olarak devam edicektir çökmeleri önlücektir,
            placeFromMain = intent.getSerializableExtra("selectedPlace") as? Place


            //null değilse anlamına geliyo
            placeFromMain?.let {
                val latLng = LatLng(it.latitude, it.longitude)

                mMap.addMarker(MarkerOptions().position(latLng).title(it.name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                binding.placeEdittext.setText(it.name)
                binding.saveButton.visibility = View.GONE
                binding.deleteButton.visibility = View.VISIBLE
            }

        }


    }

    private fun registerLauncher() {
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                //permission grandted
                //
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //bu illa üstteki ifi istyo
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, locationListener)
                    //en son locasyonu almak için
                    val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastLocation != null) {
                        val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15f))
                    }
                    //konumumu etkinleştirdikmi true diyerek etkinleştirdik demek mavi tik verdi //sadece iznimiz varsa yapabiliyoruz
                    mMap.isMyLocationEnabled = true
                }
            } else {
                //permission denied
                Toast.makeText(this@MapsActivity, "Permission needed", Toast.LENGTH_LONG).show()
            }
        }

    }

    //açılır
    override fun onMapLongClick(p0: LatLng) {
        //marker silicektir
        mMap.clear()

        mMap.addMarker(MarkerOptions().position(p0))

        selectedLatitude = p0.latitude
        selectedLongitude = p0.longitude

        binding.saveButton.isEnabled = true
    }

    fun saveButtonOnclick(view: View) {
        //main thread UI, Default -> Cpu (liste sıralası vs yoğun işler için), IO thread internet/Database
        //IO thread kullanıcaz oda rxjava(asenkron hale getirme için kullanılır daha çok) yada coroutines  ile
        //tabi kotlin için coroutinesuygun görüyolarmış javada rxjava tabiki hiç farkmez değişik kullanılabilir
        //rxjava endüstride daha çok kullanıyılıyormuş tabi compositeDisposable(kullan at) de beraber kullanılır
        if (selectedLatitude != null && selectedLongitude != null) {
            //!! demek bu null olamaz rahat sisteme dedirtmektir yada üst if konulabilir
            val place = Place(binding.placeEdittext.text.toString(), selectedLatitude!!, selectedLongitude!!)
//            placeDao.insert(place)
            //rxjavada yapılan işlemler bir disposible vericek
            compositeDisposable.add(placeDao.insert(place)
                //abone oluncak olan yeri söliyoruz yani ıo thread ulaşçaz
                .subscribeOn(Schedulers.io())
                //gözlemnen yeri giriyoruz
                .observeOn(AndroidSchedulers.mainThread())
                //sonunda ne yapılcak
                .subscribe(this::handleResponse)
            )
        }
    }

    //gelen cevabı ele genelde bu terim kullanılırmış
    private fun handleResponse() {
        val intent = Intent(this, MainActivity::class.java)
        //açık olan aktiviteleri kapatır
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    fun deleteButtonOnclick(view: View) {

        placeFromMain?.let {


            compositeDisposable.add(placeDao.delete(it).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(this::handleResponse))
        }


    }

    //ekran kapandığında çalışan method
    override fun onDestroy() {
        super.onDestroy()
        //temizleme yapıcaktır //ram temizliği
        compositeDisposable.clear()
    }

}
//video devamında key anahtarınız sınırlamak için işlemler mevcuttru bakabiliriz