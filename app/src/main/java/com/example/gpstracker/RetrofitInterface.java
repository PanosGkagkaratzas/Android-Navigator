package com.example.gpstracker;

import java.util.List;

import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RetrofitInterface {

    @POST("/users/login")
    Call<ResponseBody> login(@Body PostLogin log);

    @POST("/users/register")
    Call<ResponseBody> register(@Body PostRegisterUser reg);

    @POST("/users/register/vehicle")
    Call<ResponseBody> vehicle(@Body PostRegisterVehicle vehResult);

    @POST("/users/login/stats/missions")
    Call<ResponseBody> mission(@Body PostMission miss);

    @POST("/users/recovery")
    Call<ResponseBody> recovery(@Body PostRecoverPassword recoveryResult);

    @POST("/users/changePass")
    Call<ResponseBody> change_pass(@Body PostChangePass changePass);

    @GET("/users/getID/{Email}")
    Call<ResponseBody> getID(@Path("Email") String email);

    @GET("/users/login/vehicle/getVehiclePlate/{User_id}")
    Call<List<GetVehiclePlate>> getVehiclePlate(@Path("User_id") String id);

    @GET("/users/login/username/{ID}")
    Call<GetUsername> getUsername(@Path("ID") String id);

    @GET("/users/login/getAccount/{ID}")
    Call<GetAccountInfo> getAccount(@Path("ID") String id);

    @GET("/users/login/vehicle/getVehicle/mob/{Vehicle_plate}")
    Call<List<GetVehicle>> getVehicle(@Path("Vehicle_plate") String plate);

    @GET("/users/login/stats/missions/mob/{Vehicle_plate}")
    Call<List<GetMissions>> getMissions(@Path("Vehicle_plate") String plate);

    @HTTP(method = "DELETE" , path="/users/register/vehicle/deleteUser", hasBody = true)
    Call<ResponseBody> deletePost(@Body DeleteUser del);

    @HTTP(method = "DELETE" , path="/users/login/completeDelete", hasBody = true)
    Call<ResponseBody> deleteUser(@Body DeleteUser del);

}
