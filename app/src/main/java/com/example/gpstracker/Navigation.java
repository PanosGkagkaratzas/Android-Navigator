package com.example.gpstracker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.graphics.Color;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;

import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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


public class Navigation extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener{

    ScheduledExecutorService scheduler,scheduler2;
    ScheduledFuture<?> scheduler_for_location,scheduler_for_post;
    Runnable location_scheduler,post_scheduler;

    MapView map;
    MapboxMap mapboxMap;
    PermissionsManager permissionsManager;
    LocationComponent locationComponent;
    Point start,origin,destination,destinationPosition;
    NavigationMapRoute navigationMapRoute;
    DirectionsRoute currentRoute;
    TextView location_list;
    ImageButton BtnStart,remove;
    FloatingActionButton search;
    List<Point> mission = new ArrayList<>();
    Geocoder geocoder;
    List<Address> addresses = null;
    List<Address> current;
    List<Address> Start;
    boolean active = false;
    Context context;
    String plate,user_id;
    String current_loc;
    int x=0,y=0;

    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    String endTime = "In progress";

    FusedLocationProviderClient fusedLocationProviderClient;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.navigation);
        context = this;

        map = findViewById(R.id.map);
        remove = findViewById(R.id.btnRemove);
        location_list = findViewById(R.id.list);
        map.getMapAsync(this); // This is a callback which will be triggered when the Mapbox map is ready.

        geocoder = new Geocoder(this, Locale.getDefault());

        Intent data = getIntent();
        user_id = data.getStringExtra("user_id");
        plate = data.getStringExtra("vehicle_plate");

        new AlertDialog.Builder(Navigation.this)
                .setTitle(R.string.nvgt_title)
                .setMessage(R.string.nvgt_info)
                .setPositiveButton("OK", new
                        DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            }
                        })
                .show();

        BtnStart = findViewById(R.id.btnStart);
        BtnStart.setEnabled(false);
        BtnStart.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {

                if(mission.size()==0)
                {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.nvgt_null_missions),Toast.LENGTH_LONG).show();
                }
                else {
                    mission.add(destination);
                    getNavigation(start, mission);
                }
            }
        });

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mission.size()==0)
                {
                    new AlertDialog.Builder(Navigation.this)
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
                    removeFunction();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_back, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back:
                scheduler_for_location.cancel(true);
                scheduler.shutdown();

                Intent back = new Intent(Navigation.this, User.class);
                back.putExtra("user_id", user_id);
                back.putExtra("vehicle_plate", plate);
                startActivity(back);
                finish();
                break;
        }
        return true;
    }

    private void removeFunction() {
        String[] address = new String[mission.size()];
        final int[] checkedItem = {-1};

        for (int i=0; i<mission.size(); i++)
        {
            try {
                addresses = geocoder.getFromLocation(mission.get(i).latitude(), mission.get(i).longitude(), 1);
                address[i] = addresses.get(0).getAddressLine(0);
            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //Setting AlertDialog Characteristics
        builder.setTitle(R.string.nvgt_remove_title);
        builder.setSingleChoiceItems(address, checkedItem[0], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                checkedItem[0] = which;
                location_list.setText(address[which]);
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                which = checkedItem[0];
                Log.e("Which: ", String.valueOf(which));

                if (which == -1) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.nvgt_null_which), Toast.LENGTH_LONG).show();
                }
                else {
                    mission.remove(which);
                    navigationMapRoute.removeRoute();

                    if (mission.size() > 0) {
                        origin = start;
                        getRoute(origin, mission);
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.nvgt_empty_info), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog mDialog = builder.create();
        mDialog.show();
    }

    private void getNavigation(Point start, List<Point> mission) {

        String[] stops = new String[mission.size() - 1];
        String[] LatLng = new String[mission.size()+1];

        NavigationRoute.Builder builder = NavigationRoute.builder(context)  // Initializing the route builder
                .accessToken(Mapbox.getAccessToken())
                .origin(start); // Since we are creating a route with the same start and end points

        for (int i = 0; i < mission.size() -1; i++) { // Adding all the waypoints

            builder.addWaypoint(mission.get(i));

            try {
                addresses = geocoder.getFromLocation(mission.get(i).latitude(), mission.get(i).longitude(), 1);

                while (addresses == null) {
                    addresses = geocoder.getFromLocation(mission.get(i).latitude(), mission.get(i).longitude(), 1);
                }

                stops[i] = addresses.get(0).getAddressLine(0);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < mission.size()-1; i++) { // Adding all the waypoints
            LatLng[i+1] = mission.get(i).latitude() + "," + mission.get(i).longitude();
        }

        active = true;

        current_loc = String.valueOf(current.get(0).getAddressLine(0));

        //------------------------------------------------------------------------------

        try {


            Start = geocoder.getFromLocation(start.latitude(), start.longitude(), 1);
            String starting_point = Start.get(0).getAddressLine(0);

            LatLng[0] = String.valueOf(Start.get(0).getLatitude()) + ',' + String.valueOf(Start.get(0).getLongitude());

            //---------------------------------------------------------------
            if (y==0)
            {
                PostMission postMission = new PostMission(user_id, plate, starting_point, "", LatLng[0], active, endTime);

                Call<ResponseBody> call = Api.Mission().mission(postMission);
                call.enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        System.out.println("Data added to API!");
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                    }
                });
                y++;
            }
        }catch (Exception e){
            System.out.println("Error: "+ e);
        }

        //------------------------------------------------------------------------------
        post_scheduler = new Thread(){
            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                //if(current_loc.equals(stops[mission.size()-1])){
                                if (x == LatLng.length - 3) {
                                    calendar = Calendar.getInstance();
                                    simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                                    endTime = simpleDateFormat.format(calendar.getTime()).toString();
                                    active = false;
                                }

                                Start = geocoder.getFromLocation(start.latitude(), start.longitude(), 1);
                                String starting_point = Start.get(0).getAddressLine(0);

                                LatLng[0] = String.valueOf(Start.get(0).getLatitude()) + ',' + String.valueOf(Start.get(0).getLongitude());

                                PostMission postMission = new PostMission(user_id , plate, starting_point, stops[x], LatLng[x+1], active, endTime);

                                Call<ResponseBody> call = Api.Mission().mission(postMission);
                                call.enqueue(new Callback<ResponseBody>() {

                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        System.out.println("Data added to API!");
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    }
                                });

                                if (scheduler_for_post != null && x == mission.size() - 1) {
                                    scheduler_for_post.cancel(true);
                                    scheduler2.shutdown();
                                }
                                x++;

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
        scheduler_for_post = scheduler2.scheduleAtFixedRate(post_scheduler, 5, 5, TimeUnit.SECONDS);

        builder.build() // Building the route
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        // Make sure that we got a response
                        if (response.body() == null) { // If there is no response
                            Toast.makeText(Navigation.this, getResources().getString(R.string.nvgt_no_response), Toast.LENGTH_SHORT).show();
                            return;
                        } else if (response.body().routes().size() == 0) { // If there is a response but there is no route
                            Toast.makeText(Navigation.this, getResources().getString(R.string.nvgt_no_route), Toast.LENGTH_SHORT).show();

                            return;
                        }

                        // Now we have at least 1 route
                        currentRoute = response.body().routes().get(0); // Getting the best route

                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                                .directionsRoute(currentRoute)
                                .shouldSimulateRoute(false)
                                .build();
                        NavigationLauncher.startNavigation(Navigation.this, options);

                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        //Toast.makeText(Navigation.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        mission.remove(mission.size() - 1);
    }

    // The map is ready and we can perform location related activities.
    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {

        this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {

            enableLocationComponent(style); // show the blue user location icon
            mapboxMap.addOnMapClickListener(this);
            SearchFunction();
        });
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        destinationPosition = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        mission.add(destinationPosition);

        origin = start;

        while(origin==null)
        {
            origin = start;
        }

        BtnStart.setEnabled(true);
        getRoute(origin, mission);

        return true;
    }

    private void SearchFunction(){
        search = findViewById(R.id.btnSearch);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent find = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken())
                        .placeOptions(PlaceOptions.builder().backgroundColor(Color.parseColor("#EEEEEE"))
                        .limit(10).build(PlaceOptions.MODE_CARDS))
                        .build(Navigation.this);

                startActivityForResult(find, 7171);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK && requestCode == 7171){

            // Retrieve selected locations CarmenFeature.
            // CarmenFeature is the name of the class that holds the information from a Mapbox Geocoding API response.

            CarmenFeature carmenFeature = PlaceAutocomplete.getPlace(data);

            destination =  Point.fromLngLat(((Point)carmenFeature.geometry()).longitude(), ((Point)carmenFeature.geometry()).latitude());
            mission.add(destination);

            origin = start;

            while(origin==null)
            {
                origin = start;
            }

            BtnStart.setEnabled(true);
            getRoute(origin, mission);

            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    GeoJsonSource source = style.getSourceAs("geojsonSourceLayerId");
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[] {Feature.fromJson(carmenFeature.toJson())}));
                    }

                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(((Point) carmenFeature.geometry()).latitude(),
                                            ((Point) carmenFeature.geometry()).longitude()))
                                    .zoom(14)
                                    .build()), 4000);
                }
            }
        }
    }

    /*
      Make a request to the Mapbox Directions API. Once successful, pass the route to the route layer.
      origin is the starting point of the route
      destination is the desired finish point of the route
     */
    private void getRoute(Point origin, List<Point>mission){

        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                thread1(origin, mission);
                return null;
            }
        };

        asyncTask.execute();
    }

    private void thread1(Point origin, List<Point> mission) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                NavigationRoute.Builder builder = NavigationRoute.builder(context)  // Initializing the route builder
                        .accessToken(Mapbox.getAccessToken())
                        .origin(origin);

                for (int i = 0; i < mission.size(); i++) { // Adding all the waypoints as pitstops to the route
                    builder.addWaypoint(mission.get(i));
                }

                builder.build() // Building the route
                        .getRoute(new Callback<DirectionsResponse>() {
                            @Override
                            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                                // Make sure that we got a response
                                if (response.body() == null) { // If there is no response
                                    Toast.makeText(Navigation.this, getResources().getString(R.string.nvgt_no_response), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                else if (response.body().routes().size() == 0) { // If there is a response but there is no route
                                    Toast.makeText(Navigation.this, getResources().getString(R.string.nvgt_no_route), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Now we have at least 1 route
                                currentRoute = response.body().routes().get(0); // Getting the best route

                                if (navigationMapRoute != null) { // If there is already a route, remove it
                                    navigationMapRoute.removeRoute();
                                }
                                else { // If there is no route, create one
                                    navigationMapRoute = new NavigationMapRoute(null, map, mapboxMap);
                                }

                                navigationMapRoute.addRoute(currentRoute);

                            }

                            @Override
                            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                                // If route creation is unsuccessful
                                Log.i("Error:" , t.getMessage()); // Logging the error message
                            }
                        });
            }
        }).start();
    }

    // This is where we will enable location tracking to locate the users current location.
    @SuppressLint({"MissingPermission", "WrongConstant"})
    private void enableLocationComponent(Style loadedMapStyle) {
        // Permission check
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Activate MapboxMap LocationComponent to show user location
            locationComponent = mapboxMap.getLocationComponent();

            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build()
            );

            locationComponent.setLocationComponentEnabled(true);

            locationComponent.setCameraMode(CameraMode.TRACKING);

            locationComponent.setRenderMode(RenderMode.COMPASS);


        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Navigation.this);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {

            @Override
            public void onSuccess(Location location) {

                location_scheduler = new Runnable() {
                    @Override
                    public void run() {
                        //we have the permissions
                        if (location != null) {
                            start = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                            try {
                                current = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                                //ThreadWork el = new ThreadWork(location, null, null, null, null, null, geocoder, plate);
                                //el.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };

                scheduler = Executors.newScheduledThreadPool(1);
                scheduler_for_location = scheduler.scheduleAtFixedRate(location_scheduler, 5, 5, TimeUnit.SECONDS);
            }
        });
    }

    // Provide an explanation to the user for why we need the permission.
    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, getResources().getString(R.string.nvgt_permission_info), Toast.LENGTH_LONG).show();
    }

    // Check whether the permission was granted or not by the user, and how the app will handle that action.
    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent(mapboxMap.getStyle());
        } else {
            Toast.makeText(this, getResources().getString(R.string.nvgt_permission_info), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // It’s the one that handles all the permissions related work.
    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected void onStart() {
        super.onStart();
        map.onStart();
    }

    protected void onStop() {
        super.onStop();
        map.onStop();
    }

    public void onLowMemory() {
        super.onLowMemory();
        map.onLowMemory();
    }

    protected void onDestroy() {
        super.onDestroy();
        map.onDestroy();
    }
}