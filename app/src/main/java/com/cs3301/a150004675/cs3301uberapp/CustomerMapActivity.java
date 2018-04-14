package com.cs3301.a150004675.cs3301uberapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;

    private Button mLogout, mRequest, mGetClosestDriver,mSettings, mHistory, mConfirm, mCancel, mMoreInfo, mContact;

    private LatLng pickupLocation;

    private Boolean driverFound = false;

    private Boolean moreInfoOpen = false;

    private Marker pickupMarker,mDriverMarker;

    private SupportMapFragment mapFragment;

    private String destination, requestService;

    private LatLng destinationLatLng;

    private LinearLayout mDriverInfo;

    private ImageView mDriverProfileImage;

    private TextView mDriverName, mDriverPhone, mDriverCar, mFareETA;

    private RadioGroup mRadioGroup;

    private RatingBar mRatingBar;

    private int radius = 1;

    private String driverFoundID;

    private  HashMap<String,MarkerOptions> markerHashMap = new HashMap<>();

    GeoQuery geoQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mDriverInfo = findViewById(R.id.driverInfo);
        mDriverProfileImage = findViewById(R.id.driverProfileImage);
        mDriverName = findViewById(R.id.driverName);
        mDriverPhone = findViewById(R.id.driverPhone);
        mDriverCar = findViewById(R.id.driverCar);
        mFareETA = findViewById(R.id.fareETA);
        //hiding extra information from start
        mFareETA.setVisibility(View.GONE);
        mRatingBar = findViewById(R.id.ratingBar);
        mRatingBar.setEnabled(false);

        mRadioGroup = findViewById(R.id.radioGroup);
        mRadioGroup.check(R.id.UberX);

        mLogout = findViewById(R.id.logout);
        mRequest = findViewById(R.id.request);
        mGetClosestDriver = findViewById(R.id.getClosestUber);
        mGetClosestDriver.setVisibility(View.GONE);
        mSettings = findViewById(R.id.settings);
        mHistory = findViewById(R.id.history);
        mMoreInfo = findViewById(R.id.moreInfo);
        mContact = findViewById(R.id.contact);
        mConfirm = findViewById(R.id.confirm);
        mCancel = findViewById(R.id.cancel);

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CustomerMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    int selectId = mRadioGroup.getCheckedRadioButtonId();

                    final RadioButton radioButton = findViewById(selectId);

                    if (radioButton.getText() == null){
                        return;
                    }

                    requestService = radioButton.getText().toString();

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerRequest");
                    System.out.println("customer request ref: " + ref.toString());
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                    pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup_location_foreground)));

                    //Here, introduce fake drivers into the database according to the location of the customer
                    createFakeDriversOnFirebase(pickupLocation.latitude + 0.005, pickupLocation.longitude+0.005);
                    createFakeDriversOnFirebase(pickupLocation.latitude - 0.010, pickupLocation.longitude+0.010);
                    createFakeDriversOnFirebase(pickupLocation.latitude - 0.002, pickupLocation.longitude-0.002);
                    mRequest.setVisibility(View.GONE);
                    mGetClosestDriver.setVisibility(View.VISIBLE);
                }
        });

        mGetClosestDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getClosestDriver();
            }
        });

        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerMapActivity.this, ProfileSettingActivity.class);
                startActivity(intent);
                return;
            }
        });

        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerMapActivity.this, HistoryActivity.class);
                intent.putExtra("customerOrDriver", "Customers");
                startActivity(intent);
                return;
            }
        });

        mMoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!moreInfoOpen){
                    //predefine a fake info
                    mFareETA.setText("Fare: $20; ETA: 10 min");
                    mFareETA.setVisibility(View.VISIBLE);
                    moreInfoOpen = true;
                }else{
                    mFareETA.setVisibility(View.GONE);
                    moreInfoOpen = false;
                }
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Called End Ride");
                endRide();
                Intent intent = new Intent(CustomerMapActivity.this, RideSummaryActivity.class);
                startActivity(intent);
                return;
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String driverId: markerHashMap.keySet()){
                    mMap.clear();
                }
                markerHashMap.clear();
                mMap.clear();
                mRequest.setText("call Uber");
                mRequest.setVisibility(View.VISIBLE);
                mGetClosestDriver.setVisibility(View.GONE);
                mDriverInfo.setVisibility(View.GONE);
                return;
            }
        });

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                destination = place.getName().toString();
                destinationLatLng = place.getLatLng();
            }
            @Override
            public void onError(Status status) {
            }
        });

    }
    private void getClosestDriver(){
        mGetClosestDriver.setVisibility(View.GONE);
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("DriversAvailable");
        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                //if no driver found, keep searching
                if (!driverFound){
                    DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(key);
                    mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();
                                if (driverFound){
                                    return;
                                }

                                if(driverMap.get("service").equals(requestService)){
                                    driverFound = true;
                                    driverFoundID = dataSnapshot.getKey();

                                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("customerRequest");
                                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap map = new HashMap();
                                    map.put("customerRideId", customerId);
                                    map.put("destination", destination);
                                    map.put("destinationLat", destinationLatLng.latitude);
                                    map.put("destinationLng", destinationLatLng.longitude);
                                    driverRef.updateChildren(map);

                                    List<Double> coords = (List<Double>) driverMap.get("coords");
                                    LatLng driverLatLng = new LatLng(coords.get(0),coords.get(1));
                                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("your driver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car_model_round)));
                                    getDriverInfo();
                                    mRequest.setVisibility(View.GONE);
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound)
                {
                    radius+=100;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void getDriverInfo(){
        mDriverInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    if(dataSnapshot.child("name")!=null){
                        mDriverName.setText("Driver Name: " + dataSnapshot.child("name").getValue().toString());
                    }
                    if(dataSnapshot.child("phone")!=null){
                        mDriverPhone.setText("Contact Number: " + dataSnapshot.child("phone").getValue().toString());
                    }
                    if(dataSnapshot.child("car")!=null){
                        mDriverCar.setText("Car Type: " + dataSnapshot.child("car").getValue().toString());
                    }
                    if(dataSnapshot.child("profileImageUrl").getValue()!=null){
                        Glide.with(getApplication()).load(dataSnapshot.child("profileImageUrl").getValue().toString()).into(mDriverProfileImage);
                    }

                    int ratingSum = 0;
                    float ratingsTotal = 0;
                    float ratingsAvg = 0;
                    for (DataSnapshot child : dataSnapshot.child("rating").getChildren()){
                        ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                        ratingsTotal++;
                    }
                    if(ratingsTotal!= 0){
                        ratingsAvg = ratingSum/ratingsTotal;
                        mRatingBar.setRating(ratingsAvg);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void endRide(){
        System.out.println("End Ride Called");
        geoQuery.removeAllListeners();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference summaryRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId).child("rideSummary");
        System.out.println("summaryRef: " + summaryRef);
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId).child("history");
        System.out.println("historyRef: " + historyRef);
        Map summaryInfo = new HashMap();
        summaryInfo.put("totalCost", 20);
        summaryInfo.put("distanceOfJourney", 30);
        summaryInfo.put("driverId",driverFoundID);
        summaryRef.updateChildren(summaryInfo);
        historyRef.push().setValue(summaryInfo);

        if (driverFoundID != null){
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("customerRequest");
            driverRef.removeValue();
            driverFoundID = null;
        }

        driverFound = false;
        radius = 1;
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(requestRef);
        geoFire.removeLocation(userId);

        if(pickupMarker != null){
            pickupMarker.remove();
        }
        if (mDriverMarker != null){
            mDriverMarker.remove();
        }
        mRequest.setText("call Uber");
        mRequest.setVisibility(View.VISIBLE);
        mDriverInfo.setVisibility(View.GONE);
        mDriverName.setText("");
        mDriverPhone.setText("");
        mDriverCar.setText("");
        mDriverProfileImage.setImageResource(R.mipmap.ic_user_profile);
        mGetClosestDriver.setVisibility(View.GONE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }else{
                checkLocationPermission();
            }
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){
                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                }
            }
        }
    };


    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private void createFakeDriversOnFirebase(double latitude, double longitude){
        String name = generateRandomName();
        String phone = generateRandomPhoneNumber();
        String car = generateRandomCar();
        List<Double> coords = new ArrayList<>();
        coords.add(latitude);
        coords.add(longitude);

        int selectId = mRadioGroup.getCheckedRadioButtonId();

        final RadioButton radioButton = (RadioButton) findViewById(selectId);

        if (radioButton.getText() == null){
            return;
        }

        String service = radioButton.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", name);
        userInfo.put("phone", phone);
        userInfo.put("car", car);
        userInfo.put("service", service);
        userInfo.put("coords", coords);

        String driverId = generateRandomDriverId();
        DatabaseReference currentDriverRf = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId);
        System.out.println("current driver database: " + currentDriverRf);
        currentDriverRf.updateChildren(userInfo);
        DatabaseReference ratingRf = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("rating");
        ratingRf.push().setValue(4.0);
        ratingRf.push().setValue(3.0);
        ratingRf.push().setValue(2.0);


        DatabaseReference driversAvailable = FirebaseDatabase.getInstance().getReference("DriversAvailable");
        GeoFire geoFireAvailable = new GeoFire(driversAvailable);
        geoFireAvailable.setLocation(driverId, new GeoLocation(latitude, longitude));
        LatLng driverLatLng = new LatLng(coords.get(0),coords.get(1));
        MarkerOptions marker = new MarkerOptions().position(driverLatLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        //show the drivers on the map
        mMap.addMarker(marker);
        markerHashMap.put(driverId,marker);
    }

    private String generateRandomPhoneNumber(){
        int num1, num2, num3;
        int set2, set3;

        Random generator = new Random();

        num1 = generator.nextInt(7) + 1; //add 1 so there is no 0 to begin
        num2 = generator.nextInt(8); //randomize to 8 becuase 0 counts as a number in the generator
        num3 = generator.nextInt(8);

        set2 = generator.nextInt(643) + 100;
        set3 = generator.nextInt(8999) + 1000;

        return Integer.toString(num1) + Integer.toString(num2) + Integer.toString(num3) + Integer.toString(set2) + Integer.toString(set3);
    }

    private String generateRandomName(){
        Random generator = new Random();
        return "Driver" + generator.nextInt(1000);
    }

    private String generateRandomCar(){
        Random generator = new Random();
        return "Car" + generator.nextInt(1000);
    }

    private String generateRandomDriverId(){
        Random generator = new Random();
        return "id" + generator.nextInt(1000);
    }
}