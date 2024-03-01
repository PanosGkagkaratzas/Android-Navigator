package com.example.gpstracker;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterVehicle extends AppCompatActivity {

    TextView Manufactor,Model,Vehicle_plate,Category,Color,CC,HP,Year,con_year;
    EditText manufactor,model,vehicle_plate,color,cc,hp;
    Spinner category;
    Button register;
    String dat,date;
    DatePickerDialog.OnDateSetListener mDateSetListener;
    boolean flag = true;
    int check=0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vehicles_info);

        Manufactor = findViewById(R.id.Manufactor);
        Model = findViewById(R.id.Model);
        Vehicle_plate = findViewById(R.id.Plate);
        Category = findViewById(R.id.Category);
        Color = findViewById(R.id.Color);
        CC = findViewById(R.id.CC);
        HP = findViewById(R.id.HP);
        Year = findViewById(R.id.Year);
        con_year = findViewById(R.id.year);

        manufactor = findViewById(R.id.manufactor);
        model = findViewById(R.id.model);
        vehicle_plate = findViewById(R.id.plate);
        category = findViewById(R.id.category);
        color = findViewById(R.id.color);
        cc = findViewById(R.id.cc);
        hp = findViewById(R.id.hp);

        register = findViewById(R.id.register_button);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.spinner, getResources().getStringArray(R.array.list));
        adapter.setDropDownViewResource(R.layout.spinner);

        category.setAdapter(adapter);

        con_year.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        RegisterVehicle.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener, year,month,day);
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;

                String sday = null;
                String smonth = null;

                if (day >= 1 && day <= 9) {
                     sday = "0" + day;
                }
                else{
                    sday = String.valueOf(day);
                }

                if (month >= 1 && month <= 9) {
                    smonth = "0" + month;
                }
                else{
                    smonth = String.valueOf(month);
                }

                //dat = day + "/" + month + "/" + year;
                dat = sday + "/" + smonth + "/" + year;
                check = 1;
                con_year.setText(dat);
            }
        };

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String mnfctr = manufactor.getText().toString();
                String mdl = model.getText().toString();
                String plt = vehicle_plate.getText().toString();
                String ctgr = category.getSelectedItem().toString();
                String clr = color.getText().toString();
                String c = cc.getText().toString();
                String h = hp.getText().toString();

                Intent success = getIntent();
                String FirstName = success.getStringExtra("FirstName");
                String LastName = success.getStringExtra("LastName");
                String Type = "User";
                String Email = success.getStringExtra("Email");
                String Password = success.getStringExtra("Password");
                String ConfirmPassword = success.getStringExtra("Password2");
                String PIN = success.getStringExtra("Recovery_PIN");

                if(check==0)
                {
                    date = "null";
                }
                else{
                    date=dat;
                    date = date.replace("\"", "");
                }

                manufactor.setTextColor(getColor(R.color.black));
                model.setTextColor(getColor(R.color.black));
                vehicle_plate.setTextColor(getColor(R.color.black));
                color.setTextColor(getColor(R.color.black));
                cc.setTextColor(getColor(R.color.black));
                hp.setTextColor(getColor(R.color.black));

                // Get user's ID
                Call <ResponseBody> id = Api.ID().getID(Email);
                id.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        String  responseID = null;
                        try {
                            responseID = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String[] split = responseID.split(":");
                        String remove =  split[1].replaceAll("\"","");
                        String User_id = remove.replace("}","");

                        PostRegisterVehicle postRegisterVehicle = new PostRegisterVehicle(User_id, plt, mnfctr, mdl, ctgr, clr, c, h, date);

                        Call<ResponseBody> call2 = Api.Vehicle().vehicle(postRegisterVehicle);
                        call2.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call2, Response<ResponseBody> response) {

                                String result = null;
                                try {
                                    result = response.body().string();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                if (result.equals("Vehicle has been successfully registered!")) {

                                    if (flag == true) {
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.successful_registration), Toast.LENGTH_SHORT).show();

                                        Intent main = new Intent(RegisterVehicle.this, SignIn.class);
                                        startActivity(main);
                                        finish();
                                    } else {
                                        PostRegisterUser postRegisterUser = new PostRegisterUser(FirstName, LastName, Type, Email, Password, ConfirmPassword, PIN);

                                        Call<ResponseBody> call = Api.Register().register(postRegisterUser);
                                        call.enqueue(new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.successful_registration), Toast.LENGTH_SHORT).show();

                                                Intent main = new Intent(RegisterVehicle.this, SignIn.class);
                                                startActivity(main);
                                                finish();
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                                            }
                                        });

                                    }
                                } else {

                                    DeleteUser deleteUser = new DeleteUser(User_id);
                                    Call<ResponseBody> delete = Api.deleteUser().deletePost(deleteUser);
                                    delete.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    flag = false;

                                    switch (result) {
                                        case "Empty fields detected!":
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.empty_fields), Toast.LENGTH_LONG).show();
                                            break;
                                        case "Invalid vehicle plate detected!":
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_vehicle_plate), Toast.LENGTH_LONG).show();
                                            vehicle_plate.setTextColor(getColor(R.color.red));
                                            break;
                                        case "Invalid manufactor detected!":
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_manufactor), Toast.LENGTH_LONG).show();
                                            manufactor.setTextColor(getColor(R.color.red));
                                            break;
                                        case "Invalid model detected!":
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_model), Toast.LENGTH_LONG).show();
                                            model.setTextColor(getColor(R.color.red));
                                            break;
                                        case "Invalid color detected!":
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_color), Toast.LENGTH_LONG).show();
                                            color.setTextColor(getColor(R.color.red));
                                            break;
                                        case "Invalid CC detected!":
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_cc), Toast.LENGTH_LONG).show();
                                            cc.setTextColor(getColor(R.color.red));
                                            break;
                                        case "Invalid horse power detected!":
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_hp), Toast.LENGTH_LONG).show();
                                            hp.setTextColor(getColor(R.color.red));
                                            break;
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                            }
                        });

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_error), Toast.LENGTH_LONG).show();

                    }
                });
                //
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cancel_registration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cancel_reg:

                Intent success = getIntent();
                String Email = success.getStringExtra("Email");

                Call <ResponseBody> id = Api.ID().getID(Email);
                id.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        String responseID = null;
                        try {
                            responseID = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String[] split = responseID.split(":");
                        String remove = split[1].replaceAll("\"", "");
                        String User_id = remove.replace("}", "");

                        DeleteUser deleteUser = new DeleteUser(User_id);
                        Call<ResponseBody> delete = Api.deleteUser().deletePost(deleteUser);
                        delete.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.cancelling), Toast.LENGTH_LONG).show();

                                Intent back = new Intent(RegisterVehicle.this, Register.class);
                                startActivity(back);
                                finish();
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                            }
                        });

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_error), Toast.LENGTH_LONG).show();

                    }
                });

                flag=false;
                break;

            default:
                break;
        }
        return true;
    }
}
