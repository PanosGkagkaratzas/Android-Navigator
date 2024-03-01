package com.example.gpstracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class User extends AppCompatActivity {

    ScheduledExecutorService scheduler,scheduler2;
    ScheduledFuture<?> scheduler_for_location,scheduler_for_gps;
    Runnable location_scheduler,gps_check;

    private static final int PERMISSIONS_FINE_LOCATION = 99;
    TextView tv_title, tv_lat, tv_lon, tv_accuracy, tv_speed, tv_gps, tv_tracking, tv_address;
    Switch sw_tracking;
    Button navigation;
    ImageView warning;

    LocationRequest locationRequest;
    LocationCallback locationCallBack;

    //Google's API for location services
    FusedLocationProviderClient fusedLocationProviderClient;

    Geocoder geocoder;

    String UserID, plate, FirstName, LastName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user);

        //give each UI variable a value
        tv_title = findViewById(R.id.name);
        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_gps = findViewById(R.id.tv_gps);
        tv_tracking = findViewById(R.id.tv_tracking);
        tv_address = findViewById(R.id.tv_address);
        sw_tracking = findViewById(R.id.sw_tracking);
        navigation = findViewById(R.id.navigation);
        warning = findViewById(R.id.warning);

        GPS_Permissions();
        GPS();

        Intent success = getIntent();
        UserID = success.getStringExtra("user_id");
        plate = success.getStringExtra("vehicle_plate");

        Call<GetUsername> usrnm = Api.User().getUsername(UserID);
        usrnm.enqueue(new Callback<GetUsername>() {
            @Override
            public void onResponse(Call<GetUsername> call, Response<GetUsername> response) {
                try {
                    if (response.body() != null) {

                        GetUsername answer = response.body();

                        FirstName = answer.getFirstname();
                        LastName = answer.getLastname();
                        String user = FirstName + " " + LastName;

                        tv_title.setText(user);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<GetUsername> call, Throwable t) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_error), Toast.LENGTH_LONG).show();
            }
        });

        tv_address.setText(R.string.location_tracking_off);
        tv_address.setGravity(Gravity.CENTER_HORIZONTAL);

        //set all properties of LocationRequest
        locationRequest = new LocationRequest();
        //how often we obtain the location
        locationRequest.setInterval(5000);
        //for max power and max accuracy
        locationRequest.setFastestInterval(1000);

        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallBack = new LocationCallback() {

            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
            }
        };

        navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (sw_tracking.isChecked()) {
                    String flag = getResources().getString(R.string.searching);
                    String test = tv_address.getText().toString();
                    if (test.equals(flag)) {
                        new AlertDialog.Builder(User.this)
                                .setMessage(R.string.tracking_not_completed)
                                .setPositiveButton("OK", new
                                        DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                            }
                                        })
                                .show();
                    } else {
                        scheduler_for_location.cancel(true);
                        scheduler.shutdown();

                        if (scheduler_for_gps != null) {
                            scheduler_for_gps.cancel(true);
                            scheduler2.shutdown();
                        }

                        Intent nav = new Intent(User.this, Navigation.class);
                        nav.putExtra("user_id", UserID);
                        nav.putExtra("vehicle_plate", plate);
                        startActivity(nav);
                        finish();
                    }
                } else {
                    new AlertDialog.Builder(User.this)
                            .setMessage(R.string.tracking_off)
                            .setPositiveButton("OK", new
                                    DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                        }
                                    })
                            .show();
                }
            }
        });

        sw_tracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_tracking.isChecked()) {

                    warning.setVisibility(View.GONE);
                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    boolean gps_enabled = false;

                    tv_lat.setText(R.string.searching);
                    tv_lon.setText(R.string.searching);
                    tv_accuracy.setText(R.string.searching);
                    tv_speed.setText(R.string.searching);
                    tv_address.setText(R.string.searching);
                    tv_address.setGravity(Gravity.CENTER_HORIZONTAL);

                    try {
                        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    } catch (Exception ex) {
                        System.out.println("GPS error");
                    }

                    if (!gps_enabled) {
                        new AlertDialog.Builder(User.this)
                                .setMessage(R.string.gps_enable)
                                .setPositiveButton(R.string.settings, new
                                        DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                                                stopLocationUpdates();
                                            }
                                        })
                                .setNegativeButton(R.string.cancel, new
                                        DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                                                new AlertDialog.Builder(User.this)
                                                        .setTitle(R.string.caution)
                                                        .setMessage(R.string.caution_info)
                                                        .setPositiveButton("OK", new
                                                                DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                                                        stopLocationUpdates();
                                                                    }
                                                                }).show();
                                            }
                                        })
                                .show();
                    } else {

                        //tracking is ON
                        startLocationUpdates();

                        //we are using gps (most accurate)
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        tv_gps.setText(R.string.gps_on);
                    }

                } else {
                    //tracking is OFF
                    stopLocationUpdates();
                }
            }
        });
    }

    private void GPS_Permissions() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(User.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else {
            //we are checking the number of our os if is sufficient
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //if we have the correct version of os we can request the permissions
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout:

                stopLocationUpdates();

                if (scheduler_for_gps != null) {
                    scheduler_for_gps.cancel(true);
                    scheduler2.shutdown();
                }

                Intent back = new Intent(User.this, SignIn.class);
                back.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(back);
                finish();
                break;

            case R.id.profile:

                stopLocationUpdates();

                if (scheduler_for_gps != null) {
                    scheduler_for_gps.cancel(true);
                    scheduler2.shutdown();
                }

                Call<GetAccountInfo> call = Api.getUser().getAccount(UserID);
                call.enqueue(new Callback<GetAccountInfo>() {
                    @Override
                    public void onResponse(Call<GetAccountInfo> call, Response<GetAccountInfo> response) {
                        GetAccountInfo answer = response.body();

                        String FirstName = answer.getName();
                        String LastName = answer.getSurname();
                        String Email = answer.getEmail();
                        String RegTime = answer.getTime();

                        Intent profile = new Intent(User.this, MyProfile.class);
                        profile.putExtra("user_id", UserID);
                        profile.putExtra("name", FirstName);
                        profile.putExtra("surname", LastName);
                        profile.putExtra("vehicle_plate", plate);
                        profile.putExtra("email", Email);
                        profile.putExtra("reg_time", RegTime);
                        startActivity(profile);
                        finish();
                    }

                    @Override
                    public void onFailure(Call<GetAccountInfo> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                    }
                });
                break;

            case R.id.vehicle:

                stopLocationUpdates();

                if (scheduler_for_gps != null) {
                    scheduler_for_gps.cancel(true);
                    scheduler2.shutdown();
                }

                //Get plate based on UsersID
                Call<List<GetVehicle>> vehicle = Api.getVehicleInfo().getVehicle(plate);
                vehicle.enqueue(new Callback<List<GetVehicle>>() {
                    @Override
                    public void onResponse(Call<List<GetVehicle>> call, Response<List<GetVehicle>> response) {
                        List<GetVehicle> vehicles = response.body();

                        String Manufactor = vehicles.get(0).getMnfctr();
                        String Model = vehicles.get(0).getMdl();
                        String Type = vehicles.get(0).getTp();
                        String Color = vehicles.get(0).getClr();
                        String CC = vehicles.get(0).getCc();
                        String HP = vehicles.get(0).getHp();
                        String Date_of_con = vehicles.get(0).getYr();

                        Intent vehicle = new Intent(User.this, MyVehicle.class);
                        vehicle.putExtra("user_id", UserID);
                        vehicle.putExtra("vehicle_plate", plate);
                        vehicle.putExtra("manufactor", Manufactor);
                        vehicle.putExtra("model", Model);
                        vehicle.putExtra("type", Type);
                        vehicle.putExtra("color", Color);
                        vehicle.putExtra("cc", CC);
                        vehicle.putExtra("hp", HP);
                        vehicle.putExtra("date_of_con", Date_of_con);
                        startActivity(vehicle);
                        finish();
                    }

                    @Override
                    public void onFailure(Call<List<GetVehicle>> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                    }
                });

                break;

            case R.id.mission:

                stopLocationUpdates();

                if (scheduler_for_gps != null) {
                    scheduler_for_gps.cancel(true);
                    scheduler2.shutdown();
                }

                //Get plate based on UsersID
                Call<List<GetMissions>> missions = Api.getMissionInfo().getMissions(plate);
                missions.enqueue(new Callback<List<GetMissions>>() {
                    @Override
                    public void onResponse(Call<List<GetMissions>> call, Response<List<GetMissions>> response) {

                        int j = 0;
                        List<GetMissions> mission_list = response.body();

                        if(mission_list.size()==0)
                        {
                            new AlertDialog.Builder(User.this)
                                    .setMessage(R.string.nvgt_empty_list)
                                    .setPositiveButton("OK", new
                                            DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                                }
                                            })
                                    .show();
                        }
                        else {
                            ArrayList<String> MissionID = new ArrayList<>();
                            ArrayList<String> Time = new ArrayList<>();
                            ArrayList<String> Start = new ArrayList<>();

                            List<String> stops = null;
                            ArrayList<List<String>> stops2 = new ArrayList<>();
                            ArrayList<String> FinalStop = new ArrayList<>();

                            for (int i = 0; i < mission_list.size(); i++) {

                                MissionID.add(mission_list.get(i).getID());

                                Time.add(mission_list.get(i).getStartTime());

                                Start.add(mission_list.get(i).getStart());

                                stops = (Arrays.asList(mission_list.get(i).getStops()));
                                stops2.add(stops);

                            }

                            while (j < mission_list.size()) {
                                FinalStop.add(stops2.get(j).get(stops2.get(j).size() - 1));
                                j++;
                            }

                            Intent mission = new Intent(User.this, MyMissions.class);
                            mission.putExtra("user_id", UserID);
                            mission.putExtra("vehicle_plate", plate);
                            mission.putExtra("mission_id", MissionID);
                            mission.putExtra("time", Time);
                            mission.putExtra("start", Start);
                            mission.putExtra("final_stop", FinalStop);

                            startActivity(mission);
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<GetMissions>> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                    }
                });

                break;

            case R.id.delete:

                stopLocationUpdates();

                if (scheduler_for_gps != null) {
                    scheduler_for_gps.cancel(true);
                    scheduler2.shutdown();
                }

                new AlertDialog.Builder(User.this)
                        .setTitle(R.string.caution)
                        .setMessage(R.string.delete_confirm)
                        .setPositiveButton("OK", new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                                        DeleteUser deleteUser = new DeleteUser(UserID);
                                        Call<ResponseBody> delete = Api.completeDelete().deleteUser(deleteUser);
                                        delete.enqueue(new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                                                try {
                                                    String result = response.body().string();

                                                    if (result.equals("User deleted successfully!")) {
                                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.delete_done), Toast.LENGTH_LONG).show();

                                                        Intent back2 = new Intent(User.this, SignIn.class);
                                                        startActivity(back2);
                                                        finish();
                                                    }
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                                            }
                                        });

                                    }
                                })
                        .setNegativeButton(R.string.cancel, new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    }
                                })
                        .show();
                break;
        }
        return true;
    }

    private void startLocationUpdates() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(User.this);

        tv_tracking.setTextColor(this.getResources().getColor(R.color.green));
        tv_tracking.setText(R.string.location_tracked);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);

            location_scheduler = new Runnable() {
                @Override
                public void run() {
                    updateGPS();
                }
            };

            scheduler = Executors.newScheduledThreadPool(2);
            scheduler_for_location = scheduler.scheduleAtFixedRate(location_scheduler, 0, 5, TimeUnit.SECONDS);

        } else {
            Toast.makeText(this, getResources().getString(R.string.permission_error), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void stopLocationUpdates() {
        tv_tracking.setTextColor(this.getResources().getColor(R.color.red));
        tv_tracking.setText(R.string.location_not_tracked);
        tv_lat.setText(R.string.location_tracking_off);
        tv_lon.setText(R.string.location_tracking_off);
        tv_accuracy.setText(R.string.location_tracking_off);
        tv_speed.setText(R.string.location_tracking_off);
        tv_address.setGravity(Gravity.CENTER);
        tv_address.setText(R.string.location_tracking_off);

        //take out location tracking
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);

        if (scheduler_for_location != null) {
            scheduler_for_location.cancel(true);
            scheduler.shutdown();
        }

        sw_tracking.setChecked(false);
        warning.setVisibility(View.VISIBLE);
    }

    //trigger this method after permission is granted (request this permissions)
    //requestCode must be 99 in order to work our method
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, getResources().getString(R.string.permission_error), Toast.LENGTH_LONG).show();
                    finish();
                }
        }
    }

    @SuppressLint("MissingPermission")
    private void updateGPS() {

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {

            @Override
            public void onSuccess(Location location) {

                //we have the permissions
                if (location != null) {
                    updateValues(location);
                }
            }
        });
    }

    private void GPS(){

        gps_check = new Thread(){
            @Override
            public void run() {
                try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    System.out.println("Running...");
                                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                                    boolean gps_enabled = false;

                                    try {
                                        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                                    } catch (Exception ex) {
                                    }

                                    if (!gps_enabled) {
                                        tv_gps.setText(R.string.gps_off);
                                        stopLocationUpdates();
                                    } else {
                                        tv_gps.setText(R.string.gps_on);
                                    }
                                }catch (Exception e){
                                    System.out.println("Error: "+ e);
                                }
                            }
                        });

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        };
        scheduler2 = Executors.newScheduledThreadPool(1);
        scheduler_for_gps = scheduler2.scheduleAtFixedRate(gps_check, 0, 5, TimeUnit.SECONDS);
    }

    private void updateValues(Location location) {

        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        ThreadWork object = new ThreadWork(location, tv_lat, tv_lon, tv_accuracy, tv_speed, tv_address, geocoder);
        object.start();
    }

}
