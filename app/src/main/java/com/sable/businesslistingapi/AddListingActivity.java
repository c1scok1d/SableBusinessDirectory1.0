package com.sable.businesslistingapi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 *
 */
public class AddListingActivity extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {


    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;


    String address, state, country, zipcode, city, street, bldgNo;

    private Double latitude = 0.00;
    private Double longitude = 0.00;
    /*
    objects of text view and button widgets.
     */
    TextView tvAddress, tvStreet, tvZip, tvState, tvCity, tvBldgNo, tvCountry;
    EditText etName, etDescription, etPhone, etEmail, etWebsite, etHours, etTwitter, etFacebook;
    Button btnNext;
    Spinner spnCategory;
    ArrayList<String> category = new ArrayList<>();
    ArrayList<ListingsAddModel> locationAdd = new ArrayList<>();


    //ImageView uploadImage1, uploadImage2, uploadImage3;


    /**
     * @param savedInstanceState
     */
    @SuppressLint("MissingPermission")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_listing);


        tvAddress = findViewById(R.id.tvAddress);
        tvZip = findViewById(R.id.tvZip);
        tvState = findViewById(R.id.tvState);
        tvCity = findViewById(R.id.tvCity);
        tvStreet = findViewById(R.id.tvStreet);
        tvBldgNo = findViewById(R.id.tvBldgNo);
        tvCountry = findViewById(R.id.tvCountry);
        btnNext = findViewById(R.id.btnNext);
        spnCategory = findViewById(R.id.spnCategory);
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etWebsite = findViewById(R.id.etWebsite);
        etTwitter  = findViewById(R.id.etTwitter);
        etFacebook = findViewById(R.id.etFacebook);
        // category = new ArrayList<>();
        // locationAdd = new ArrayList<>();


        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
        getRetrofitCategories();

        /**
         * OnClick take user to add location page
         */

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (etName.getText().toString().isEmpty() || spnCategory == null && spnCategory.getSelectedItem() == null || etDescription.getText().toString().isEmpty()) {
                    Toast.makeText(AddListingActivity.this, "Please fill all sections...", Toast.LENGTH_LONG).show();
                } else {

                    getRetrofitCategories();

                    final String name = etName.getText().toString();
                    final String description = etDescription.getText().toString();
                    final String category = spnCategory.getSelectedItem().toString();
                    //addCategory = addCategory;
                    final String phone = etPhone.getText().toString();
                    final String email = etEmail.getText().toString();
                    final String website = etWebsite.getText().toString();
                    final String twitter = etTwitter.getText().toString();
                    final String facebook = etFacebook.getText().toString();


                    locationAdd.add(new ListingsAddModel(ListingsAddModel.IMAGE_TYPE,
                            name,
                            category,
                            addCategory,
                            description,
                            longitude,
                            latitude,
                            address,
                            state,
                            country,
                            zipcode,
                            city,
                            bldgNo,
                            street,
                            phone,
                            email,
                            website,
                            twitter,
                            facebook));

                    Intent LocationAdd = new Intent(AddListingActivity.this, ReviewActivity.class);
                    Bundle locationAddBundle = new Bundle();
                    locationAddBundle.putParcelableArrayList("locationAddBundle", locationAdd);
                    LocationAdd.putExtra("locationAdd", locationAdd);
                    startActivity(LocationAdd);
                }
            }
        });

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        enableMyLocation();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000,
                400, LocationListener);

        spnCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!spnCategory.getSelectedItem().toString().equals("Category")) {

                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    // set your desired log level
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                    OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
// add your other interceptors …
// add logging as last interceptor
                    httpClient.addInterceptor(logging);  // <-- this is the important line!
                    // set your desired log level
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
// add your other interceptors …
// add logging as last interceptor
                    httpClient.addInterceptor(logging);  // <-- this is the important line!
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(baseURL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(httpClient.build())
                            .build();
                    RetrofitArrayApi service = retrofit.create(RetrofitArrayApi.class);
                    // pass JSON data to BusinessListings class for filtering
                    Call<List<ListingsCategories>> call = service.getCategory();

                    // get filtered data from BusinessListings class and add to recyclerView adapter for display on screen
                    call.enqueue(new Callback<List<ListingsCategories>>() {
                        @Override
                        public void onResponse(Call<List<ListingsCategories>> call, Response<List<ListingsCategories>> response) {
                            // addCategory = null;
                            // loop through JSON response get parse and output to log
                            for (int i = 0; i < response.body().size(); i++) {
                                String selected = spnCategory.getSelectedItem().toString();
                                String replied = response.body().get(i).getName();

                                if (spnCategory.getSelectedItem().toString().equals(response.body().get(i).getName())) {
                                    addCategory = (response.body().get(i).getId());
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<List<ListingsCategories>> call, Throwable t) {
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // TODO Auto-generated method stub
            }
        });
    }

    android.location.LocationListener LocationListener = new LocationListener() {

        /**
         * @param location
         */
        @Override
        public void onLocationChanged(Location location) {

            // zoom to current location on map
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            setAddress(latitude, longitude);  // method to reverse geocode to physical address
        }

        /**
         * @param provider
         * @param status
         * @param extras
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        /**
         * @param provider
         */
        @Override
        public void onProviderEnabled(String provider) {}

        /**
         * @param provider
         */
        @Override
        public void onProviderDisabled(String provider) {}

    };



    /**
     * @param map
     */
    public void onMapReady(GoogleMap map) {
        mMap = map;

        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);
        enableMyLocation(); //permission check
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        setAddress(latitude, longitude);
    }

    /**
     *
     * Enables the My Location layer if the fine location permission has been granted.
     */
    public void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    /**
     * @return
     */
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Getting current location...", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    /**
     * @param location
     */
    @Override
    public void onMyLocationClick(Location location) {
        //get location address
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();

        // zoom to current location on map
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        setAddress(latitude, longitude);
    }

    /**
     * Returns users GPS lat/lng
     * @param location
     */

    /**
     *
     */
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     *
     */
    /*
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    /**
     * @param latitude
     * @param longitude
     */
    private void setAddress(Double latitude, Double longitude){
        this.latitude = latitude;
        this.longitude = longitude;

        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(addresses.size() > 0) {
            Log.d("max", " " + addresses.get(0).getMaxAddressLineIndex());

            String maddress = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()/*String city = addresses.get(0).getLocality();
            String mbldgNo = addresses.get(0).getSubThoroughfare();
            String mstreet = addresses.get(0).getThoroughfare();
            String mcity = addresses.get(0).getLocality();
            String mstate = addresses.get(0).getAdminArea();
            String mzipcode = addresses.get(0).getPostalCode();
            String mcountry = addresses.get(0).getCountryName();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
            String lat = Double.toString(latitude);
            String lng = Double.toString(longitude);


//        tvAddress.setText(address);
            tvZip.setText(mzipcode);
            tvState.setText(mstate);
            tvCity.setText(mcity);
            tvStreet.setText(mstreet);
            tvBldgNo.setText(mbldgNo);
            tvCountry.setText(mcountry);


            addresses.get(0).getAdminArea();
            address = maddress;
            bldgNo = mbldgNo;
            street = mstreet;
            state = mstate;
            city = mcity;
            zipcode = mzipcode;
            country = mcountry;
//            tvLng.setText(lng);
        }

    }

    /**
     * Query API for listings data
     * set URL and make call to API
     *
     */
    private String baseURL = "https://www.thesablebusinessdirectory.com";
    Integer addCategory;
    public void getRetrofitCategories() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
// add your other interceptors …
// add logging as last interceptor
        httpClient.addInterceptor(logging);  // <-- this is the important line!
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
// add your other interceptors …
// add logging as last interceptor
        httpClient.addInterceptor(logging);  // <-- this is the important line!
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        RetrofitArrayApi service = retrofit.create(RetrofitArrayApi.class);
        // pass JSON data to BusinessListings class for filtering
        Call<List<ListingsCategories>> call = service.getCategory();

        // get filtered data from BusinessListings class and add to recyclerView adapter for display on screen
        call.enqueue(new Callback<List<ListingsCategories>>() {
            @Override
            public void onResponse(Call<List<ListingsCategories>> call, Response<List<ListingsCategories>> response) {
                Log.e("main_activity", " response " + response.body());
                // mListPost = response.body();
                //progressBar.setVisibility(View.GONE); //hide progressBar
              category.add("Category"); //add heading to category spinner
                // loop through JSON response get parse and output to log
                for (int i = 0; i < response.body().size(); i++) {
                    //ifStatement to skip json object from array if value is empty/null
                    //parse response based on ListingsModel class and add to list array ( get category name, description and image)
                    // add category name from array to spinner
                    category.add(response.body().get(i).getName());
                    //category.add(response.body().get(i).getId().toString());
                    // display category array list in spinner
                    spnCategory.setAdapter(new ArrayAdapter<>(AddListingActivity.this, android.R.layout.simple_spinner_dropdown_item, category));
                    Log.e("main ", " Category: " + response.body().get(i).getName());
                }
               // adapter.notifyDataSetChanged();
            }
            @Override
            public void onFailure(Call<List<ListingsCategories>> call, Throwable t) {
            }
        });
    }
}