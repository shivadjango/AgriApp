package com.shivaconsulting.agriapp.Home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shivaconsulting.agriapp.Adapter.AreaAdapter;
import com.shivaconsulting.agriapp.Adapter.PlacesAutoCompleteAdapter;
import com.shivaconsulting.agriapp.Adapter.TimeAdapter;
import com.shivaconsulting.agriapp.History.BookingHistoryActivity;
import com.shivaconsulting.agriapp.Models.Booking;
import com.shivaconsulting.agriapp.Profile.LoginActivity;
import com.shivaconsulting.agriapp.Profile.ProfileActivity;
import com.shivaconsulting.agriapp.R;
import com.vivekkaushik.datepicker.DatePickerTimeline;
import com.vivekkaushik.datepicker.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback , View.OnClickListener,TimeAdapter.OnItemSelectedListener,
AreaAdapter.OnAreaItemSelectedListener, PlacesAutoCompleteAdapter.ClickListener{

    //Const
    private static final String TAG = "MapsActivity";
    private Context mContext = MapsActivity.this;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final int LOCATION_SETTINGS_REQUEST = 4548;
    private static final float DEFAULT_ZOOM = 19f;

    //Vars
    private GoogleMap mMap;
    private Boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationManager locationManager;
    private LatLng mCenterLatLong;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private long selectedDate;
    private TimeAdapter timeAdapter;
    private List<Integer> timeList;
    private AreaAdapter areaAdapter;
    private List<Integer> areaList;
    private PlacesAutoCompleteAdapter mAutoCompleteAdapter;

    //Id's
    private ImageView home,booking_history,profile;
    private Button booking_button;
    private ImageView gps_button;
    private ConstraintLayout bookContraint;
    private CardView cardView1,cardView2,cardView3;
    private TextView combine_text,pick_time_text,pick_date_text,pick_area_text;
    private RecyclerView time_picker_recyclerview,area_picker_recyclerview,map_search_recyler;
    private ImageView tot_image_1,tot_image_2,belt_image_1,belt_image_2,belt_image_3,
            combine_image_3,combine_image_2,combine_image_1;
    private EditText autoCompleteTextView;
    private DatePickerTimeline datePickerTimeline;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        home = findViewById(R.id.home);
        booking_history = findViewById(R.id.booking_history);
        profile = findViewById(R.id.profile);
        gps_button = findViewById(R.id.gps_button);
        booking_button = findViewById(R.id.book_button);
        datePickerTimeline = findViewById(R.id.datePickerTimeline);
        bookContraint = findViewById(R.id.booking_constraint);
        cardView1 = findViewById(R.id.tot_type_cardview);
        cardView2 = findViewById(R.id.belt_type_cardview);
        cardView3 = findViewById(R.id.combine_type_cardview);
        combine_text = findViewById(R.id.combine_text);
        pick_time_text = findViewById(R.id.pick_time_text);
        time_picker_recyclerview = findViewById(R.id.time_picker_recyclerview);
        area_picker_recyclerview = findViewById(R.id.area_picker_recyclerview);
        pick_date_text = findViewById(R.id.pick_date_text);
        pick_area_text = findViewById(R.id.pick_area_text);
        combine_image_1 = findViewById(R.id.combine_image_1);
        combine_image_2 = findViewById(R.id.combine_image_2);
        combine_image_3 = findViewById(R.id.combine_image_3);
        tot_image_1 = findViewById(R.id.tot_image_1);
        tot_image_2 = findViewById(R.id.tot_image_2);
        belt_image_1 = findViewById(R.id.belt_image_1);
        belt_image_2 = findViewById(R.id.belt_image_2);
        belt_image_3 = findViewById(R.id.belt_image_3);
        map_search_recyler = findViewById(R.id.map_search_recyler);
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);

        Places.initialize(this, getResources().getString(R.string.google_maps_key));

        autoCompleteTextView.addTextChangedListener(filterTextWatcher);

        mAutoCompleteAdapter = new PlacesAutoCompleteAdapter(this);
        map_search_recyler.setLayoutManager(new LinearLayoutManager(this));
        mAutoCompleteAdapter.setClickListener(this);
        map_search_recyler.setAdapter(mAutoCompleteAdapter);
        mAutoCompleteAdapter.notifyDataSetChanged();


        datePickerTimeline.setInitialDate(2020, 9, 22);


        datePickerTimeline.setDateTextColor(Color.RED);
        datePickerTimeline.setDayTextColor(Color.RED);
        datePickerTimeline.setMonthTextColor(Color.RED);


        home.setOnClickListener(this);
        booking_history.setOnClickListener(this);
        profile.setOnClickListener(this);

        home.setImageResource(R.drawable.ic_baseline_home);

        setupFirebaseAuth();


        getLocationPermission();

        this.setFinishOnTouchOutside(true);
        locationManager = (LocationManager) MapsActivity.this.getSystemService(Context.LOCATION_SERVICE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        gps_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Clicked when gps is turned off");
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    Log.d(TAG, "onClick: Gps not enabled");
                    enableLoc();
                    //TODO:NEED TO IMPLEMENT LIKE SWIGGY ONCE GPS TURNED ON

                }

                else {
                    Log.d(TAG, "onClick: Clicked after Gps Is On");
                    getDeviceLocation();
                }
            }
        });

        combine_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Clicked");

                cardView1.setVisibility(View.GONE);
                cardView2.setVisibility(View.GONE);
                cardView3.setVisibility(View.GONE);
                tot_image_1.setVisibility(View.GONE);
                tot_image_2.setVisibility(View.GONE);
                belt_image_1.setVisibility(View.GONE);
                belt_image_2.setVisibility(View.GONE);
                belt_image_3.setVisibility(View.GONE);

                bookContraint.setVisibility(View.VISIBLE);
                datePickerTimeline.setOnDateSelectedListener(new OnDateSelectedListener() {
                    @Override
                    public void onDateSelected(int year, int month, int day, int dayOfWeek) {

                        selectedDate = year+month+day;
                        Log.d(TAG, "onDateSelected: date: " + year + month + day);
                        Log.d(TAG, "onDateSelected: SelectedDate reform: " + selectedDate);

                        datePickerTimeline.setVisibility(View.INVISIBLE);
                        area_picker_recyclerview.setVisibility(View.INVISIBLE);

                        time_picker_recyclerview.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onDisabledDateSelected(int year, int month, int day, int dayOfWeek, boolean isDisabled) {

                    }
                });


//                final long selectedDate = calendarView.getDate();

//                Log.d(TAG, "onClick: Booked Date :" + selectedDate);

                booking_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Toast.makeText(MapsActivity.this, "Service selected wait for cofirmation", Toast.LENGTH_SHORT).show();

                        cardView1.setVisibility(View.VISIBLE);
                        cardView2.setVisibility(View.VISIBLE);
                        cardView3.setVisibility(View.VISIBLE);

                        bookContraint.setVisibility(View.GONE);


                        String UUID = FirebaseAuth.getInstance().getUid();

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        Booking booking = new Booking();
                        booking.setDate(selectedDate);
                        booking.setService_name("shiva51");
                        booking.setStatus(false);
                        booking.setService_provider("test");

                        db.collection("Services").document(UUID).collection("List").document()
                                .set(booking).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MapsActivity.this, "Booked", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MapsActivity.this, "Failed to book!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext,LinearLayoutManager.HORIZONTAL, false);
        time_picker_recyclerview.setLayoutManager(linearLayoutManager);
        timeList = new ArrayList<>();
        timeAdapter = new TimeAdapter(timeList,mContext,this);
        time_picker_recyclerview.setAdapter(timeAdapter);
        timeList.add(6);
        timeList.add(7);
        timeList.add(8);
        timeList.add(9);
        timeList.add(10);
        timeList.add(11);
        timeList.add(12);
        timeList.add(1);
        timeList.add(2);
        timeList.add(3);
        timeList.add(4);
        timeList.add(5);
        timeList.add(6);
        timeList.add(7);
        timeList.add(8);
        pick_time_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pick_time_text.setTextSize(18);
                pick_time_text.setTextSize(14);
                pick_area_text.setTextSize(14);
                datePickerTimeline.setVisibility(View.INVISIBLE);
                area_picker_recyclerview.setVisibility(View.INVISIBLE);
                time_picker_recyclerview.setVisibility(View.VISIBLE);
            }
        });


        pick_date_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pick_time_text.setTextSize(14);
                pick_area_text.setTextSize(14);
                pick_date_text.setTextSize(18);
                datePickerTimeline.setVisibility(View.VISIBLE);
                time_picker_recyclerview.setVisibility(View.INVISIBLE);
                area_picker_recyclerview.setVisibility(View.INVISIBLE);
            }
        });


        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(mContext,LinearLayoutManager.HORIZONTAL,false);
        area_picker_recyclerview.setLayoutManager(linearLayoutManager1);
        areaList = new ArrayList<>();
        areaAdapter = new AreaAdapter(areaList,mContext,this);
        area_picker_recyclerview.setAdapter(areaAdapter);
        areaList.add(1);
        areaList.add(2);
        areaList.add(3);
        areaList.add(4);
        areaList.add(5);
        areaList.add(6);
        areaList.add(7);
        areaList.add(8);
        pick_area_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pick_time_text.setTextSize(14);
                pick_date_text.setTextSize(14);
                pick_area_text.setTextSize(18);
                datePickerTimeline.setVisibility(View.INVISIBLE);
                time_picker_recyclerview.setVisibility(View.INVISIBLE);
                area_picker_recyclerview.setVisibility(View.VISIBLE);
            }
        });




    }


    private void moveCamera(LatLng latLng, float zoom, String tittle){
        Log.d(TAG, "location: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
//        MarkerOptions options = new MarkerOptions()
//                .position(latLng)
//                .title(tittle);
//        mMap.addMarker(options);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
//            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setTiltGesturesEnabled(true);


            final Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    mCenterLatLong = mMap.getCameraPosition().target;
                    mMap.clear();

                    try {

                        Location mLocation = new Location("");
                        mLocation.setLatitude(mCenterLatLong.latitude);
                        mLocation.setLongitude(mCenterLatLong.longitude);

//                        mLocationMarkerText.setText("Lat : " + mCenterLatLong.latitude + "," + "Long : " + mCenterLatLong.longitude);


                        List<Address> myAddress = geocoder.getFromLocation(mCenterLatLong.latitude, mCenterLatLong.longitude, 1);
                        String address = myAddress.get(0).getAddressLine(0);
                        String city = myAddress.get(0).getSubLocality();
//                        mLocationCity.setText(city);
//                        mLocationAddress.setText(address);
//
//                        ConfirmLocation(city,address,mCenterLatLong.latitude,mCenterLatLong.longitude);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });



            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    bookContraint.setVisibility(View.GONE);
                    cardView1.setVisibility(View.VISIBLE);
                    cardView2.setVisibility(View.VISIBLE);
                    cardView3.setVisibility(View.VISIBLE);
                }
            });


        }

    }


    private TextWatcher filterTextWatcher = new TextWatcher() {
        public void afterTextChanged(Editable s) {
            if (!s.toString().equals("")) {
                mAutoCompleteAdapter.getFilter().filter(s.toString());
                if (map_search_recyler.getVisibility() == View.GONE) {map_search_recyler.setVisibility(View.VISIBLE);}
            } else {
                if (map_search_recyler.getVisibility() == View.VISIBLE) {map_search_recyler.setVisibility(View.GONE);}
            }
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        public void onTextChanged(CharSequence s, int start, int before, int count) { }
    };



    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapsActivity.this);

    }


    private void enableLoc() {



        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {


            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        MapsActivity.this,
                                        LOCATION_SETTINGS_REQUEST);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });
    }


    private void getDeviceLocation(){



        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted ){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM,"My Location");

                            tot_image_1.setVisibility(View.VISIBLE);
                            tot_image_2.setVisibility(View.VISIBLE);
                            combine_image_1.setVisibility(View.VISIBLE);
                            combine_image_2.setVisibility(View.VISIBLE);
                            combine_image_3.setVisibility(View.VISIBLE);
                            belt_image_1.setVisibility(View.VISIBLE);
                            belt_image_2.setVisibility(View.VISIBLE);
                            belt_image_3.setVisibility(View.VISIBLE);

                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }


    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

        /*
    ---------------------------------------BottomNavBar-------------------------------------------------
     */

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.booking_history:
               Intent intent = new Intent(mContext,BookingHistoryActivity.class);
               startActivity(intent);
               break;

            case R.id.profile:
                Intent intent1 = new Intent(mContext, ProfileActivity.class);
                startActivity(intent1);
                break;
        }

    }


      /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * checks to see if the @param 'user' is logged in
     * @param user
     */
    private void checkCurrentUser(FirebaseUser user){
        Log.d(TAG, "checkCurrentUser: checking if user is logged in.");

        if(user == null){
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }
    }
    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the user is logged in
                checkCurrentUser(user);

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        checkCurrentUser(mAuth.getCurrentUser());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void OnSelectedListener(Integer time_number) {

        Log.d(TAG, "OnSelectedListener: selected_time: " + time_number);

        datePickerTimeline.setVisibility(View.INVISIBLE);
        area_picker_recyclerview.setVisibility(View.VISIBLE);

        time_picker_recyclerview.setVisibility(View.INVISIBLE);
    }

    @Override
    public void OnSelectedAreaListener(Integer area_number) {

        Log.e(TAG, "OnSelectedAreaListener: Area_selected: " + area_number );
    }

    @Override
    public void click(Place place) {
        moveCamera(new LatLng(place.getLatLng().latitude,place.getLatLng().longitude), DEFAULT_ZOOM, "Selected Location");
    }
}