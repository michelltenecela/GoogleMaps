package com.example.googlemaps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import WebService.WebService;
import WebService.Asynchtask;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        Asynchtask
{
    GoogleMap map;
    ArrayList<LatLng> lista = new ArrayList<>();
    PolylineOptions lineas = new PolylineOptions();

    String apiKey = "AIzaSyBXUM9EXmYwGQVXTSzAglVoMSWn8I-lO9I";
    String origins;
    String destinations;
    String units = "metric";
    WebService ws;
    double distancia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        lineas.width(8);
        lineas.color(Color.RED);
    }

    @Override
    public void processFinish(String result) throws JSONException {
        String lista = "";
        JSONObject objecto = null;
        try {
                JSONObject jsonResponse = new JSONObject(result);
                JSONArray rows = jsonResponse.getJSONArray("rows");
                JSONObject firstRow = rows.getJSONObject(0);
                JSONArray elements = firstRow.getJSONArray("elements");
                JSONObject firstElement = elements.getJSONObject(0);

                String distanceText = firstElement.getJSONObject("distance").getString("text");
                distancia += firstElement.getJSONObject("distance").getDouble("value");
            Toast.makeText(this, "La Distancia en metros es: " +distancia, Toast.LENGTH_SHORT).show();
        }
        catch (JSONException e)
        {
            Toast.makeText(this.getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
    map=googleMap;

        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        map.getUiSettings().setZoomControlsEnabled(true);

        CameraUpdate camUpd1 =
                CameraUpdateFactory
                        .newLatLngZoom(new LatLng(40.6892532, -74.0445481714432), 20);
        map.moveCamera(camUpd1);

        LatLng madrid = new LatLng(40.6892532, -74.0445481714432);
        CameraPosition camPos = new CameraPosition.Builder()
                .target(madrid)
                .zoom(19)
                .bearing(45) //noreste arriba
                .tilt(0) //punto de vista de la cámara 70 grados
                .build();
        CameraUpdate camUpd3 =
                CameraUpdateFactory.newCameraPosition(camPos);
        map.animateCamera(camUpd3);
        map.setOnMapClickListener(this);
    }
    MarkerOptions marcador;


    @Override
    public void onMapClick(@NonNull LatLng latLng) {

        marcador = new MarkerOptions();
        marcador.position(latLng);
        marcador.title("punto");
        map.addMarker(marcador);
        lineas.add(latLng);
        map.addPolyline(lineas);

        if(lineas.getPoints().size()==6){

            lineas.add(lineas.getPoints().get(0));
            map.addPolyline(lineas);

            for (int i = 0; i < 6; i++) {
                origins = String.valueOf(lineas.getPoints().get(i).latitude) +","+ String.valueOf(lineas.getPoints().get(i).longitude);
                destinations = String.valueOf(lineas.getPoints().get(i+1).latitude) +","+ String.valueOf(lineas.getPoints().get(i+1).longitude);
                Map<String, String> datos = new HashMap<String, String>();
                ws= new WebService("https://maps.googleapis.com/maps/api/distancematrix/json" +
                        "?key=" + apiKey +
                        "&origins=" + origins +
                        "&destinations=" + destinations +
                        "&units=" + units, datos, MainActivity.this, MainActivity.this);
                ws.execute("GET");
            }
            Toast.makeText(this, "La Distancia en total es: " +distancia, Toast.LENGTH_SHORT).show();
            lineas.getPoints().clear();
            }
        }
    }