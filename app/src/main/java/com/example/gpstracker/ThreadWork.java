package com.example.gpstracker;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThreadWork extends Thread {

    public ThreadWork(Location location, TextView tv_lat, TextView tv_lon, TextView tv_accuracy, TextView tv_speed, TextView tv_address, Geocoder geocoder) {

        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                runthread1(location, tv_lat, tv_lon, tv_accuracy, tv_speed, tv_address, geocoder);

                return null;
            }
        };

        asyncTask.execute();

    }

    public void runthread1(Location location, TextView tv_lat, TextView tv_lon, TextView tv_accuracy, TextView tv_speed, TextView tv_address, Geocoder geocoder) {

        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {

            @Override
            public void run() {

                List<Address> addresses = null;
                String speed = String.valueOf(location.getSpeed());

                tv_lat.setText(String.valueOf(location.getLatitude()));
                tv_lon.setText(String.valueOf(location.getLongitude()));
                tv_accuracy.setText(String.valueOf(location.getAccuracy())); //describes the deviation in meters. So, the smaller the number, the better the accuracy.

                if (location.hasSpeed()) {
                    tv_speed.setText(speed);
                } else {
                    tv_speed.setText(R.string.null_speed);
                }

                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                } catch (Exception e) {
                    System.out.println(addresses);
                }

                if (addresses == null) {
                    tv_address.setText(R.string.null_address);
                } else {
                    String address = String.valueOf(addresses.get(0).getAddressLine(0));
                    tv_address.setText(address);
                }
            }
        });
    }
}