package com.example.gpstracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Register extends AppCompatActivity {

    Button next_button,cancel_button;
    EditText firstname,lastname,mail,password,confirm_password,register_pin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        next_button = findViewById(R.id.next_button);
        cancel_button = findViewById(R.id.cancel_button);

        firstname = findViewById(R.id.firstname);
        lastname = findViewById(R.id.lastname);
        mail = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirm_password = findViewById(R.id.password2);
        register_pin = findViewById(R.id.pin);

        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = firstname.getText().toString();
                String lname = lastname.getText().toString();
                String type = "User";
                String email = mail.getText().toString();
                String pass = password.getText().toString();
                String c_pass = confirm_password.getText().toString();
                String pin = register_pin.getText().toString();

                firstname.setTextColor(Color.BLACK);
                lastname.setTextColor(Color.BLACK);
                mail.setTextColor(Color.BLACK);
                password.setTextColor(Color.BLACK);
                confirm_password.setTextColor(Color.BLACK);
                register_pin.setTextColor(Color.BLACK);

                PostRegisterUser postRegisterUser = new PostRegisterUser(name, lname, type, email, pass, c_pass, pin);

                Call<ResponseBody> call = Api.Register().register(postRegisterUser);

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        String result = null;
                        try {
                            result = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        switch (result) {
                            case "User has been successfully registered!":
                                Intent vehicle = new Intent(Register.this, RegisterVehicle.class);
                                vehicle.putExtra("FirstName", name);
                                vehicle.putExtra("LastName", lname);
                                vehicle.putExtra("Email", email);
                                vehicle.putExtra("Password", pass);
                                vehicle.putExtra("Password2", c_pass);
                                vehicle.putExtra("Recovery_PIN", pin);
                                startActivity(vehicle);
                                finish();
                                break;
                            case "Empty fields detected!":
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.empty_fields), Toast.LENGTH_LONG).show();
                                break;
                            case "Invalid first name detected!":
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_first_name), Toast.LENGTH_LONG).show();
                                firstname.setTextColor(Color.RED);
                                break;
                            case "Invalid last name detected!":
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_last_name), Toast.LENGTH_LONG).show();
                                lastname.setTextColor(Color.RED);
                                break;
                            case "Invalid email detected!":
                                new AlertDialog.Builder(Register.this)
                                        .setMessage(R.string.invalid_email)
                                        .setPositiveButton("OK", new
                                                DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                                        mail.setTextColor(Color.RED);
                                                    }
                                                }).show();
                                break;
                            case "Invalid password detected!":
                                new AlertDialog.Builder(Register.this)
                                        .setMessage(R.string.invalid_pass)
                                        .setPositiveButton("OK", new
                                                DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                                        password.setTextColor(Color.RED);
                                                    }
                                                }).show();
                                break;
                            case "Password fields must have the same password!":
                                new AlertDialog.Builder(Register.this)
                                        .setMessage(R.string.pass_match)
                                        .setPositiveButton("OK", new
                                                DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                                        password.setTextColor(Color.RED);
                                                        confirm_password.setTextColor(Color.RED);
                                                    }
                                                }).show();
                                break;
                            case "Invalid recovery PIN detected!":
                                new AlertDialog.Builder(Register.this)
                                        .setMessage(R.string.invalid_pin)
                                        .setPositiveButton("OK", new
                                                DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                                        register_pin.setTextColor(Color.RED);
                                                    }
                                                }).show();
                                break;
                            case "User with this email already exists!":
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.email_in_use), Toast.LENGTH_LONG).show();
                                mail.setTextColor(Color.RED);
                                break;
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent(Register.this, SignIn.class);
                startActivity(back);
                finish();
            }
        });
    }
}
