package com.learn.uberclone;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;

public class DriverRequestListActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private Button btnDriverReqList_GetRequests;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private ListView listView;
    private ArrayList<String> nearbyDriveRequests;
    private ArrayAdapter adapter;
    private ArrayList<Double> passengersLatitudes;
    private ArrayList<Double> passengersLongitudes;
    private ArrayList<String> requestCarUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_list);
        assignUI();
        initializeVars();

        // Call All OnClick Event
        callAllOnClickEvent();

        // Call All OnItemClick Event
        callOnItemClickEvent();
    }

    private void assignUI() {
        btnDriverReqList_GetRequests = findViewById(R.id.btnDriverReqList_GetRequests);
        listView = findViewById(R.id.lsvDriverReqList_AllList);
    }

    private void initializeVars() {
        nearbyDriveRequests = new ArrayList<>();
        passengersLatitudes = new ArrayList<>();
        passengersLongitudes = new ArrayList<>();
        requestCarUsername = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nearbyDriveRequests);

        listView.setAdapter(adapter);
        nearbyDriveRequests.clear();

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
//        if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//
//            locationListener = new LocationListener() {
//                @Override
//                public void onLocationChanged(Location location) {
//                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//                }
//
//                @Override
//                public void onStatusChanged(String provider, int status, Bundle extras) {
//
//                }
//
//                @Override
//                public void onProviderEnabled(String provider) {
//
//                }
//
//                @Override
//                public void onProviderDisabled(String provider) {
//
//                }
//            };
//
//        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logOut_DriverItem) {
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("Please wait...");
            dialog.show();

            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        dialog.dismiss();
                        finish();
                    }
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    private void callAllOnClickEvent() {
        btnDriverReqList_GetRequests.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.btnDriverReqList_GetRequests):
                onClick_GetAllRequests();
                break;
        }

    }

    private void onClick_GetAllRequests() {
//        if (Build.VERSION.SDK_INT < 23) {
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//            Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            updateRequestListView(currentDriverLocation);
//        } else if (Build.VERSION.SDK_INT >= 23) {
//            if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(DriverRequestListActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
//            } else {
//                // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//                Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                updateRequestListView(currentDriverLocation);
//            }
//        }

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(DriverRequestListActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
            } else {
                Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestListView(currentDriverLocation);
            }
        } else {
            Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateRequestListView(currentDriverLocation);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestListView(currentDriverLocation);
            }
        }
    }

    private void updateRequestListView(Location driverLocation) {
        if (driverLocation != null) {
            final ParseGeoPoint driverCurrentLocation = new ParseGeoPoint(driverLocation.getLatitude(), driverLocation.getLongitude());

            ParseQuery<ParseObject> requestCarQuery = ParseQuery.getQuery("RequestCar");
            requestCarQuery.whereNear("passengerLocation", driverCurrentLocation);
            requestCarQuery.whereDoesNotExist("driverOfMe");

            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("Getting data from server...");
            dialog.show();

            requestCarQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    dialog.dismiss();
                    if (e == null) {
                        if (objects.size() > 0) {
                            if (nearbyDriveRequests.size() > 0) {
                                nearbyDriveRequests.clear();
                            }
                            if (passengersLatitudes.size() > 0) {
                                passengersLatitudes.clear();
                            }
                            if (passengersLongitudes.size() > 0) {
                                passengersLongitudes.clear();
                            }
                            if (requestCarUsername.size() > 0) {
                                requestCarUsername.clear();
                            }

                            for (ParseObject nearRequest : objects) {
                                ParseGeoPoint pLocation = (ParseGeoPoint) nearRequest.get("passengerLocation");
                                Double milesDistanceToPassenger = driverCurrentLocation.distanceInMilesTo(pLocation);

                                float roundedDistanceValue = Math.round(milesDistanceToPassenger * 10) / 10;
                                nearbyDriveRequests.add("There are " + roundedDistanceValue + " miles to " + nearRequest.get("username"));

                                passengersLatitudes.add(pLocation.getLatitude());
                                passengersLongitudes.add(pLocation.getLongitude());
                                requestCarUsername.add(nearRequest.get("username") + "");
                            }
                        } else {
                            Toast.makeText(DriverRequestListActivity.this,
                                    "There are no requests yet",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private void callOnItemClickEvent() {
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location cdLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);

            if (cdLocation != null) {
                Intent intent = new Intent(this, ViewLocationMapActivity.class);
                intent.putExtra("dLatitude", cdLocation.getLatitude());
                intent.putExtra("dLongitude", cdLocation.getLongitude());
                intent.putExtra("pLatitude", passengersLatitudes.get(position));
                intent.putExtra("pLongitude", passengersLongitudes.get(position));
                intent.putExtra("rUsername", requestCarUsername.get(position));
                startActivity(intent);
            }
        }

    }
}
