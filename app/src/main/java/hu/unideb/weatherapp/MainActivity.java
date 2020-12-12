package hu.unideb.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private ViewFlipper viewFlipper;

    private LocationManager locationManager;

    private ImageView appLogo;

    private TextView temperature;

    private TextView city;

    private TextView humidity;

    private TextView description;

    private TextView windSpeed;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewFlipper = findViewById(R.id.view_flipper);

        appLogo = (ImageView) findViewById(R.id.appLogo);

        city = (TextView) findViewById(R.id.city_name);

        humidity = (TextView) findViewById(R.id.humidity);

        temperature = (TextView) findViewById(R.id.temperature);

        description = (TextView) findViewById(R.id.description);

        windSpeed = (TextView) findViewById(R.id.wind_speed);

        GetWeatherData getWeather = new GetWeatherData();

        Animation inFade = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);

        Animation outFade = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);

        appLogo.startAnimation(inFade);

        new Handler().postDelayed(() -> appLogo.startAnimation(outFade), 2000);

        new Handler().postDelayed(() -> appLogo.startAnimation(inFade), 4000);

        new Handler().postDelayed(() -> viewFlipper.showNext(), 8000);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();

        String provider = locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Location location = locationManager.getLastKnownLocation(provider);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        onLocationChanged(location);

        double longitude = location.getLongitude();

        double latitude = location.getLatitude();

        try {
            String content = getWeather.execute("http://api.openweathermap.org/data/2.5/weather?lat=" +
                    latitude + "&lon=" + longitude + "&appid=7fc0f22248a923d5272b6836b3fdc96a").get();

            JSONObject jsonObject = new JSONObject(content);

            Gson gson = new Gson();

            Weather weather = gson.fromJson(content, Weather.class);

            Sys sys = gson.fromJson(jsonObject.getJSONObject("sys").toString(), Sys.class);

            Wind wind = gson.fromJson(jsonObject.getString("wind"), Wind.class);

            Main main = gson.fromJson(jsonObject.getString("main"), Main.class);

            JSONArray jsonArray = jsonObject.getJSONArray("weather");

            double temp = (main.getTemp() - 273.15);

            double windS = (wind.getSpeed() * 3.6);

            temp = Math.round(temp * 100);

            windS = Math.round(windS * 100);

            temp = temp / 100;

            windS = windS / 100;

            city.setText(weather.getName() + " " + sys.getCountry());

            temperature.setText(temp + " Celsius degrees");

            humidity.setText("humidity: " + main.getHumidity() + "%");

            description.setText(jsonArray.getJSONObject(0).getString("description"));

            windSpeed.setText("wind speed: " + windS + " km/h");

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        locationManager.removeUpdates(this);

        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}
