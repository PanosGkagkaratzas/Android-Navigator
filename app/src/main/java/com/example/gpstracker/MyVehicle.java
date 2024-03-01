package com.example.gpstracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyVehicle extends AppCompatActivity {

    TextView vhcl_plate, vhcl_manufactor, vhcl_model, vhcl_type, vhcl_color, vhcl_cc, vhcl_hp, vhcl_date,list_of_plates;
    Button ChangeVehicle;

    String id;
    String plate;
    String manufactor;
    String model;
    String type;
    String color;
    String cc;
    String hp;
    String date_of_con;

    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_vehicle);

        Intent vehicle = getIntent();
        id = vehicle.getStringExtra("user_id");
        plate = vehicle.getStringExtra("vehicle_plate");
        manufactor = vehicle.getStringExtra("manufactor");
        model = vehicle.getStringExtra("model");
        type = vehicle.getStringExtra("type");
        color = vehicle.getStringExtra("color");
        cc = vehicle.getStringExtra("cc");
        hp = vehicle.getStringExtra("hp");
        date_of_con = vehicle.getStringExtra("date_of_con");

        vhcl_plate = findViewById(R.id.vhcl_plate);
        vhcl_manufactor = findViewById(R. id.vhcl_manufactor);
        vhcl_model = findViewById(R.id.vhcl_model);
        vhcl_type = findViewById(R.id.vhcl_type);
        vhcl_color = findViewById(R.id.vhcl_color);
        vhcl_cc = findViewById(R.id.vhcl_cc);
        vhcl_hp = findViewById(R.id.vhcl_hp);
        vhcl_date = findViewById(R.id.vhcl_date);
        ChangeVehicle = findViewById(R.id.change_vehicle);
        list_of_plates = findViewById(R.id.user_plate_list);

        vhcl_plate.setText(plate);
        vhcl_manufactor.setText(manufactor);
        vhcl_model.setText(model);
        vhcl_type.setText(type);
        vhcl_color.setText(color);
        vhcl_cc.setText(cc);
        vhcl_hp.setText(hp);
        vhcl_date.setText(date_of_con);

        ChangeVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Call<List<GetVehiclePlate>> getPlate = Api.getVehiclePlate().getVehiclePlate(id);
                getPlate.enqueue(new Callback<List<GetVehiclePlate>>() {
                    @Override
                    public void onResponse(Call<List<GetVehiclePlate>> call, Response<List<GetVehiclePlate>> response) {

                        List<GetVehiclePlate> plate_list = response.body();

                        ArrayList<String> plates = new ArrayList<>();

                        for (int i = 0; i < plate_list.size(); i++) {
                            plates.add(plate_list.get(i).getPlate());
                        }

                        if (plates.size() < 2) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.vehicles_less_than_two), Toast.LENGTH_LONG).show();
                        } else {

                            final int[] checkedItem = {-1};
                            String[] dialog_list_of_plates = new String[plates.size()];

                            for (int i = 0; i < plates.size(); i++) {
                                dialog_list_of_plates[i] = plates.get(i);
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(MyVehicle.this);

                            builder.setTitle(R.string.select_plate);
                            builder.setSingleChoiceItems(dialog_list_of_plates, checkedItem[0], new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    checkedItem[0] = which;
                                    list_of_plates.setText(plates.get(which));
                                }
                            });

                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    which = checkedItem[0];

                                    if (which == -1) {
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.null_select_plate), Toast.LENGTH_LONG).show();
                                    } else {

                                        plate = plates.get(which);

                                        Call<List<GetVehicle>> vehicle = Api.getVehicleInfo().getVehicle(plates.get(which));
                                        vehicle.enqueue(new Callback<List<GetVehicle>>() {
                                            @Override
                                            public void onResponse(Call<List<GetVehicle>> call, Response<List<GetVehicle>> response) {
                                                List<GetVehicle> vehicles = response.body();

                                                vhcl_plate.setText(plate);
                                                vhcl_manufactor.setText(vehicles.get(0).getMnfctr());
                                                vhcl_model.setText(vehicles.get(0).getMdl());
                                                vhcl_type.setText(vehicles.get(0).getTp());
                                                vhcl_color.setText(vehicles.get(0).getClr());
                                                vhcl_cc.setText(vehicles.get(0).getCc());
                                                vhcl_hp.setText(vehicles.get(0).getHp());
                                                vhcl_date.setText(vehicles.get(0).getYr());

                                            }

                                            @Override
                                            public void onFailure(Call<List<GetVehicle>> call, Throwable t) {
                                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                                            }
                                        });

                                    }
                                }
                            });

                            AlertDialog mDialog = builder.create();
                            mDialog.show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<GetVehiclePlate>> call, Throwable t) {

                    }
                });

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
                Intent back = new Intent(MyVehicle.this, User.class);
                back.putExtra("user_id", id);
                back.putExtra("vehicle_plate", plate);
                startActivity(back);
                finish();
                break;
        }
        return true;
    }
}
