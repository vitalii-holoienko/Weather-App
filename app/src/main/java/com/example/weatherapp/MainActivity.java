package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import android.location.Criteria;

import android.location.Location;
import android.location.LocationManager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.view.View;
import android.Manifest;

import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.location.LocationListener;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;

//imports for google maps and location track
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.format.TextStyle;
import java.util.Locale;

//import for weather API
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;

//Time
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class MainActivity extends AppCompatActivity{
    FusedLocationProviderClient fusedLocationProviderClient;

    String bestProvider;
    OtherDataInformation otherDataInformation = new OtherDataInformation();
    static Boolean successfulRequest = true;
    String arrowUp = "↑";
    String arrowDown = "↓";
    static JSONObject data = null;

    ImageView ifRainImage;

    ScrollView scrollView = null;
    LocationManager locationManager;
    LocationListener locationListener;
    LocationRequest locationRequest;
    LatLng userLatLng;
    Marker marker;

    class ForecastData{
        String time;
        String weather;
        Boolean raining;
        double temperature;

        int minTemperature;
        int maxTemperature;
        ForecastData(){
            this.time = "wead";
            this.weather = "";
            this.raining = false;
            this.temperature = 0;
            minTemperature= 9999;
            maxTemperature = -1;
        }
    }
    class OtherDataInformation{
        int pressure;
        int humidity;
        int visibility;
        double latitude;
        double longitude;

        String country;

        int timezone;
        OtherDataInformation(){
            pressure = 0;
            humidity = 0;
            visibility = 0;
            latitude = 0;
            longitude = 0;
            country = "";
            timezone = 0;
        }
    }
    ForecastData[] forecast;
    static double generalLongitude=30.523333;
    static double generalLatitude=50.450001;

    static double userLontitude=0;
    static double userLatitude=0;
    static Boolean successfulWeatherParse = true;
    private Handler mHandler= new Handler();
    Criteria criteria;
    //Views
    TextView currentTemperature;
    TextView time1;
    TextView humidity;
    TextView pressure;
    TextView visibility;
    TextView timezone;
    TextView country;
    TextView tvLatitude;
    TextView tvLongitude;
    TextView windDirection;
    TextView windSpeed;
    TextView deltaTemperature;
    TextView getWeather;
    ForecastData[] weekForecast;
    TextView weatherDescription;
    ImageView weatherIcon;
    SearchView enteredCity;
    ConstraintLayout constraintLayout;
    Drawable snowFlakeIcon;
    Drawable blobIcon;
    Drawable cloudIcon;

    Drawable cloudsWithRainIcon;

    ImageView bottomWeatherIcon;
    //Weather data
    static int todayTemperature = 0;
    static int todayDeltaMaxTemperature;
    static int todayDeltaMinTemperature;
    static String todayWindDirection;
    static int todayWindSpeed;
    static String todayWeatherDescription;

    LinearLayout forecastLayout;

    private final static int REQUEST_CODE = 100;
    private final static int REQUEST_CHECK_SETTING = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        java.util.Date date = new java.util.Date();
        Format formatter1 = new SimpleDateFormat("EEE,MMMdd,YYY");
        String time1s = formatter1.format(date);
        Format formatter2 = new SimpleDateFormat("H:mm");
        String time2s = formatter2.format(date);
        weekForecast = new ForecastData[5];
        forecast = new ForecastData[10];
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        scrollView = findViewById(R.id.ScrollView);
        time1 = findViewById(R.id.time1);

        currentTemperature = findViewById(R.id.currentTemperature);
        weatherIcon = findViewById(R.id.weatherIcon);
        forecastLayout = findViewById(R.id.forecastLayout);

        snowFlakeIcon = getResources().getDrawable(R.drawable.snowflake);
        blobIcon = getResources().getDrawable(R.drawable.blob);
        cloudIcon = getResources().getDrawable(R.drawable.cloud_icon);
        cloudsWithRainIcon = getResources().getDrawable(R.drawable.cloudswithrain);

        enteredCity = findViewById(R.id.enteredCity);
        constraintLayout = findViewById(R.id.layout);
        weatherDescription = findViewById(R.id.weatherDescription);
        deltaTemperature = findViewById(R.id.deltaTemperature);
        windSpeed = findViewById(R.id.windSpeed);
        windDirection = findViewById(R.id.windDirection);
        humidity = findViewById(R.id.humidity);
        pressure = findViewById(R.id.pressure);
        visibility = findViewById(R.id.visibility);
        timezone = findViewById(R.id.timezone);
        country = findViewById(R.id.country);
        tvLongitude = findViewById(R.id.longitude);
        tvLatitude = findViewById(R.id.latitude);
        bottomWeatherIcon = findViewById(R.id.bottomWeatherIcon);
        ifRainImage = findViewById(R.id.ifRainImage);
        ifRainImage.setImageDrawable(blobIcon);
        getWeather = findViewById(R.id.getWeather);

        time1.setText(time1s);

        if(!isLocationEnabled()){
            askResolutionForLocationTracking();
        }

        getWeather.setPaintFlags(getWeather.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        getJsonData(generalLatitude, generalLongitude);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                locationManager.removeUpdates(this);
                userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                getJsonData(userLatLng.latitude, userLatLng.longitude);
            }
        };
        getLastLocation();

        int searchPlateId = enteredCity.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = enteredCity.findViewById(searchPlateId);
        if (searchPlate!=null) {
            searchPlate.setBackgroundColor (Color.TRANSPARENT);
            int searchTextId = searchPlate.getContext ().getResources ().getIdentifier ("android:id/search_src_text", null, null);

        }

        getWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askResolutionForLocationTracking();
            }
        });

        enteredCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enteredCity.onActionViewExpanded();
            }
        });

        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enteredCity != null) {
                    enteredCity.setQuery("", false);
                    enteredCity.clearFocus();
                    enteredCity.onActionViewCollapsed();
                }
            }
        });

        enteredCity.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                getLatLonByCityName(query);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }
    @SuppressLint("SetTextI18n")
    private void displayMainInfo(){
        Log.i("DISPLAY INFO", "FFFF");
        currentTemperature.setText(String.valueOf(todayTemperature) + "°");
        weatherDescription.setText(todayWeatherDescription);
        deltaTemperature.setText(arrowUp + todayDeltaMaxTemperature + "° " + arrowDown + todayDeltaMinTemperature+"°");
        windDirection.setText("Wind Direction: " + todayWindDirection);
        windSpeed.setText("Wind Speed: " + todayWindSpeed + "km/h");
        enteredCity.setQueryHint("Enter the city");

        if(todayWeatherDescription.equals("Clouds")){
            bottomWeatherIcon.setImageDrawable(cloudIcon);
            weatherIcon.setImageDrawable(cloudIcon);
        }
        if(todayWeatherDescription.equals("Snow")){
            bottomWeatherIcon.setImageDrawable(snowFlakeIcon);
            weatherIcon.setImageDrawable(snowFlakeIcon);
        }
        if(todayWeatherDescription.equals("Rain")){
            bottomWeatherIcon.setImageDrawable(cloudsWithRainIcon);
            weatherIcon.setImageDrawable(cloudsWithRainIcon);
        }

        humidity.setText("Humidity(%): " + String.valueOf(otherDataInformation.humidity));
        pressure.setText("Pressure(hPa): " + String.valueOf(otherDataInformation.pressure));
        visibility.setText("Visibility(per meter): " + String.valueOf(otherDataInformation.visibility));
        timezone.setText("Timezone(from UTC): " + String.valueOf(otherDataInformation.timezone));
        country.setText("Country: " + otherDataInformation.country);
        tvLongitude.setText("Longitude: " + String.valueOf(otherDataInformation.longitude));
        tvLatitude.setText("Latitude: " + String.valueOf(otherDataInformation.latitude));

        for(int i = 0; i < 10;i++){
            HorizontalScrollView sc = (HorizontalScrollView) forecastLayout.getChildAt(0);
            LinearLayout temp = (LinearLayout) sc.getChildAt(0);
            LinearLayout ll = (LinearLayout) temp.getChildAt(i);
            TextView timeText = (TextView) ll.getChildAt(0);
            timeText.setText(forecast[i].time);

            ImageView weatherIcon = (ImageView) ll.getChildAt(1);
            ImageView ifRainIcon = (ImageView) ll.getChildAt(2);


            TextView temperature = (TextView) ll.getChildAt(3);
            temperature.setText(String.valueOf((int)forecast[i].temperature) + "°");
            Log.i("forecast[i].weather", forecast[i].weather);
            if(Objects.equals(forecast[i].weather, "Clouds"))
            {
                weatherIcon.setImageDrawable(cloudIcon);
            }
            if(Objects.equals(forecast[i].weather, "Snow"))
            {
                weatherIcon.setImageDrawable(snowFlakeIcon);
            }
            if(forecast[i].raining) ifRainIcon.setImageDrawable(blobIcon);
        }
        LinearLayout weekForecastLayout = findViewById(R.id.weekForecastLayout);
        for(int i = 0; i < weekForecast.length;i++){
            LinearLayout currentLayout = (LinearLayout) weekForecastLayout.getChildAt(i);
            TextView dateOfWeek = (TextView) currentLayout.getChildAt(0);
            ImageView icon = (ImageView) currentLayout.getChildAt(1);
            TextView deltaT = (TextView) currentLayout.getChildAt(2);
            dateOfWeek.setText(weekForecast[i].time);
            if(Objects.equals(weekForecast[i].weather, "Clouds"))
            {
                icon.setImageDrawable(cloudIcon);
            }
            if(Objects.equals(weekForecast[i].weather, "Snow"))
            {
                icon.setImageDrawable(snowFlakeIcon);
            }
            if(Objects.equals(weekForecast[i].weather, "Rain")){
                icon.setImageDrawable(cloudsWithRainIcon);
            }

            deltaT.setText(weekForecast[i].minTemperature + "°/" + weekForecast[i].maxTemperature + "°");

        }

    }
    @SuppressLint("StaticFieldLeak")
    public  void getLatLonByCityName(final String city) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected Void doInBackground(Void... params) {
                URL url1 = null;
                try {
                    Log.i("Start", "start");
                    url1 = new URL("http://api.openweathermap.org/data/2.5/weather?q="+city+"&APPID=8bcf47f34fc8660a21646c0c51572753");
                    successfulRequest = true;
                }
                catch(Exception e) {
                    successfulRequest = false;
                    return null;
                }
                try{
                    HttpURLConnection connection1 = (HttpURLConnection) url1.openConnection();

                    BufferedReader reader1 =
                            new BufferedReader(new InputStreamReader(connection1.getInputStream()));

                    StringBuffer json1 = new StringBuffer(1024);
                    String tmp = "";

                    while((tmp = reader1.readLine()) != null)
                        json1.append(tmp).append("\n");
                    reader1.close();
                    JSONObject j = new JSONObject(json1.toString());

                    data = j;

                    //Getting user position from API
                    JSONObject coord = j.getJSONObject("coord");
                    generalLongitude  = coord.getDouble("lon");
                    generalLatitude = coord.getDouble("lat");
                    return null;

                } catch (Exception e) {
                    Log.i("Exception", e.getMessage());
                    return null;
                }

            }

            @Override
            protected void onPostExecute(Void Void) {
                if(successfulRequest){
                    if(generalLongitude != 0 && generalLatitude != 0){
//                        currentDisplayedCity =  query;
                        getJsonData(generalLatitude, generalLongitude);
                    }
                }

            }
        }.execute();
    }
    @SuppressLint("StaticFieldLeak")
    public  void getJsonData(double lat, double lon) {
        new AsyncTask<Void, Void, Void>() {


            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String t = "http://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&appid=8bcf47f34fc8660a21646c0c51572753";
                    Log.i("Link", t);

                    URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&appid=8bcf47f34fc8660a21646c0c51572753");

                    HttpURLConnection connection1 = (HttpURLConnection) url.openConnection();

                    BufferedReader reader1 =
                            new BufferedReader(new InputStreamReader(connection1.getInputStream()));

                    StringBuffer json1 = new StringBuffer(1024);
                    String tmp = "";

                    while((tmp = reader1.readLine()) != null)
                        json1.append(tmp).append("\n");
                    reader1.close();
                    JSONObject j = new JSONObject(json1.toString());

                    data = j;
                    JSONArray mainListOfData = j.getJSONArray("list");

                    if(mainListOfData == null){
                        successfulWeatherParse = false;
                        return null;
                    }

                    JSONObject todayWeatherData = mainListOfData.getJSONObject(0);

                    todayTemperature = (int)kelvinToCelsius(todayWeatherData.getJSONObject("main").getDouble("temp"));
                    todayDeltaMaxTemperature = (int) kelvinToCelsius(todayWeatherData.getJSONObject("main").getDouble("temp_max")) - todayTemperature;
                    todayDeltaMinTemperature = todayTemperature - (int) kelvinToCelsius(todayWeatherData.getJSONObject("main").getDouble("temp_min"));
                    todayWeatherDescription = todayWeatherData.getJSONArray("weather").getJSONObject(0).getString("main");
                    todayWindSpeed = (int) (todayWeatherData.getJSONObject("wind").getDouble("speed")*3.6f);
                    todayWindDirection = GetWindDirectionByDegrees(todayWeatherData.getJSONObject("wind").getInt("deg"));

                    otherDataInformation.latitude = j.getJSONObject("city").getJSONObject("coord").getDouble("lat");
                    otherDataInformation.longitude = j.getJSONObject("city").getJSONObject("coord").getDouble("lon");
                    otherDataInformation.pressure =todayWeatherData.getJSONObject("main").getInt("pressure");
                    otherDataInformation.country = j.getJSONObject("city").getString("country");
                    otherDataInformation.visibility = mainListOfData.getJSONObject(0).getInt("visibility");
                    otherDataInformation.humidity =todayWeatherData.getJSONObject("main").getInt("humidity");
                    otherDataInformation.timezone = j.getJSONObject("city").getInt("timezone");

                    for(int i = 0; i < 10;i++){
                        forecast[i] = new ForecastData();
                        forecast[i].time = mainListOfData
                                .getJSONObject(i)
                                .getString("dt_txt")//"2023-11-29 00:00:00"
                                .split("\\s")[1]//"10:00:00"
                                .substring(0,5);//"10:00"
                        forecast[i].weather = mainListOfData
                                .getJSONObject(i)
                                .getJSONArray("weather")
                                .getJSONObject(0)
                                .getString("main");
                        forecast[i].raining = forecast[i].weather.equals("Rain");

                        forecast[i].temperature = (int)kelvinToCelsius(mainListOfData
                                .getJSONObject(i)
                                .getJSONObject("main")
                                .getDouble("temp"));
                    }

                    String iD = mainListOfData
                            .getJSONObject(0)
                            .getString("dt_txt");
                    // Define the input format
                    DateTimeFormatter iF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                    // Parse the input string
                    LocalDateTime dT = LocalDateTime.parse(iD, iF);

                    // Get the day of the week
                    String currentDay = dT.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

                    int minT=9999;
                    int maxT=-9999;
                    Boolean rain = false;
                    Boolean snow = false;
                    int x = 0;
                    for(int i = 0; i < mainListOfData.length();i++){
                        weekForecast[x] = new ForecastData();
                        String inputDate = mainListOfData
                                .getJSONObject(i)
                                .getString("dt_txt");
                        // Define the input format
                        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                        // Parse the input string
                        LocalDateTime dateTime = LocalDateTime.parse(inputDate, inputFormatter);

                        // Get the day of the week
                        String dayOfWeek = dateTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);


                        Log.i(dayOfWeek, "DAYOFWEEK");
                        if(dayOfWeek.equals(currentDay)){
                            if((int) kelvinToCelsius(mainListOfData.getJSONObject(i).getJSONObject("main").getDouble("temp_min")) < minT){
                                minT = (int) kelvinToCelsius(mainListOfData.getJSONObject(i).getJSONObject("main").getDouble("temp_min"));
                            }
                            if((int) kelvinToCelsius(mainListOfData.getJSONObject(i).getJSONObject("main").getDouble("temp_max")) > maxT){
                                maxT = (int) kelvinToCelsius(mainListOfData.getJSONObject(i).getJSONObject("main").getDouble("temp_max"));
                            }

                            String w = mainListOfData
                                    .getJSONObject(i)
                                    .getJSONArray("weather")
                                    .getJSONObject(0)
                                    .getString("main");

                            if(w.equals("Snow")) snow = true;
                            if(w.equals("Rain")) rain = true;
                        }
                        else{

                            weekForecast[x].time = currentDay;
                            Log.i("AAAA", weekForecast[x].time);
                            weekForecast[x].minTemperature = minT;
                            weekForecast[x].maxTemperature = maxT;
                            weekForecast[x].weather = rain ? "Rain" : snow ? "Snow" : "Clouds";

                            currentDay = dayOfWeek;
                            minT = 999;
                            maxT = -999;
                            rain = false;
                            snow = false;
                            if(x == 4) break;
                            x++;

                        }

                    }
                    return null;

                } catch (Exception e) {
                    Log.i("Exception", e.getMessage());
                    return null;
                }

            }

            @Override
            protected void onPostExecute(Void Void) {
                displayMainInfo();
            }
        }.execute();

    }

    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {

                @Override
                public void onSuccess(Location location)
                {
                    if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    criteria = new Criteria();
                    bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();

                    Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if(lastLocation!=null)
                    {
                        userLatitude = lastLocation.getLatitude();
                        userLontitude = lastLocation.getLongitude();
                        getJsonData(userLatitude, userLontitude);
                        userLatLng = new LatLng(userLatitude, userLontitude);
                    }
                    else
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, locationListener);
                }
            });
        }
        else
            askPermission();

    }
    private static double kelvinToCelsius(double n){
        return n - 273.15;
    }
    private static String GetWindDirectionByDegrees(int d){
        if(d >= 337.5 || d <= 22.5) return "North";
        if(d >= 22.5 || d <= 67.5) return "North-East";
        if(d >= 67.5 || d <= 112.5) return "East";
        if(d >= 112.5 || d <= 157.5) return "South-East";
        if(d >= 157.5 || d <= 202.5) return "South";
        if(d >= 202.5 || d <= 247.5) return "South-West";
        if(d >= 247.5 || d <= 292.5) return "West";
        if(d >= 292.5 || d <= 337.5) return "North-West";
        return "";
    }
    private void askPermission(){
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private  void askResolutionForLocationTracking(){
        //ASK RESOLUTION FOR TRACK CURRENT LOCATION
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices
                .getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>()
        {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try
                {
                    //if location is already turned on
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MainActivity.this, "We are already displaying weather in your location", Toast.LENGTH_SHORT).show();
                }
                catch (ApiException e)
                {
                    switch (e.getStatusCode()) {

                        // if we need resolution
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {

                                ResolvableApiException resolvableApiException = (ResolvableApiException)e;
                                resolvableApiException.startResolutionForResult(MainActivity.this,REQUEST_CHECK_SETTING);
                            } catch (IntentSender.SendIntentException ex) {

                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLastLocation();
            }else{
                Toast.makeText(this, "Required Permisson", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private Boolean isLocationEnabled() { return ( locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ); }
}