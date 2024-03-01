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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyProfile extends AppCompatActivity {

    TextView usrname, usr_surname, usr_email, usr_reg;
    EditText current_password,change_password, change_password2;
    Button ChangePassword;

    String id;
    String name;
    String surname;
    String plate;
    String email;
    String reg_time;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_profile);

        Intent success = getIntent();
        id = success.getStringExtra("user_id");
        name = success.getStringExtra("name");
        surname = success.getStringExtra("surname");
        plate = success.getStringExtra("vehicle_plate");
        email = success.getStringExtra("email");
        reg_time = success.getStringExtra("reg_time");

        usrname = findViewById(R.id.usrname);
        usr_surname = findViewById(R. id.usr_surname);
        usr_email = findViewById(R.id.usr_mail);
        usr_reg = findViewById(R.id.usr_reg);
        ChangePassword = findViewById(R.id.change_pass);

        usrname.setText(name);
        usr_surname.setText(surname);
        usr_email.setText(email);
        usr_reg.setText(reg_time);

        ChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(MyProfile.this);
                builder.setTitle(R.string.change_pass);

                // set the custom layout
                final View pass_recovery = getLayoutInflater().inflate(R.layout.change_pass, null);
                builder.setView(pass_recovery);

                builder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                current_password = pass_recovery.findViewById(R.id.old_password);
                                change_password = pass_recovery.findViewById(R.id.change_password);
                                change_password2 = pass_recovery.findViewById(R.id.change_password2);

                                String old_pass = current_password.getText().toString();
                                String new_pass = change_password.getText().toString();
                                String new_pass2 = change_password2.getText().toString();

                                PostChangePass postChangePass = new PostChangePass(email, old_pass, new_pass, new_pass2);

                                Call<ResponseBody> call = Api.ChangePass().change_pass(postChangePass);
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
                                            case "Empty fields detected!":
                                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.empty_fields), Toast.LENGTH_LONG).show();
                                                break;
                                            case "Invalid email detected!":
                                                new AlertDialog.Builder(MyProfile.this)
                                                        .setMessage(R.string.invalid_email)
                                                        .setPositiveButton("OK", new
                                                                DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                                                    }
                                                                }).show();
                                                break;
                                            case "Invalid password form detected!":
                                                new AlertDialog.Builder(MyProfile.this)
                                                        .setMessage(R.string.invalid_pass)
                                                        .setPositiveButton("OK", new
                                                                DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                                                    }
                                                                }).show();
                                                break;
                                            case "Password fields must have the same password!":
                                                new AlertDialog.Builder(MyProfile.this)
                                                        .setMessage(R.string.pass_match)
                                                        .setPositiveButton("OK", new
                                                                DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                                                    }
                                                                }).show();
                                                break;
                                            case "Password changed successfully":
                                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.successful_recovery), Toast.LENGTH_LONG).show();

                                                break;
                                            case "Email not found!":
                                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.email_not_found), Toast.LENGTH_LONG).show();
                                                break;
                                            case "Wrong password...":
                                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.wrong_password), Toast.LENGTH_LONG).show();
                                                break;
                                        }

                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.password_change_fail), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        })
                        .setNegativeButton(R.string.cancel, new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.password_change_cancel), Toast.LENGTH_LONG).show();
                                    }

                                });
                AlertDialog dialog = builder.create();
                dialog.show();
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
                Intent back = new Intent(MyProfile.this, User.class);
                back.putExtra("user_id", id);
                back.putExtra("vehicle_plate", plate);
                startActivity(back);
                finish();
                break;
        }
        return true;
    }
}
