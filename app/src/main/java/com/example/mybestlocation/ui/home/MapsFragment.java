package com.example.mybestlocation.ui.home;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.CameraPosition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import com.example.mybestlocation.Config;
import com.example.mybestlocation.HttpHandler;
import com.example.mybestlocation.JSONParser;
import com.example.mybestlocation.Position;
import com.example.mybestlocation.R;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<Position> positions = new ArrayList<>();
    private String title, mode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_maps, container, false);

        // Retrieve arguments (title and mode)
        if (getArguments() != null) {
            title = getArguments().getString("title", "Map");
            mode = getArguments().getString("mode", "get");
        }

        // Set map title
        TextView titleTextView = root.findViewById(R.id.tv_title_map);
        titleTextView.setText(title);

        // Initialize and load the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        // Fetch and display data
        new DownloadDataTask().execute();

        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Configure map settings
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // Set default camera position
        LatLng defaultLocation = new LatLng(0, 0);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(defaultLocation)
                        .zoom(2)
                        .tilt(30)
                        .build()
        ));

        if ("add".equals(mode)) {
            mMap.setOnMapClickListener(this::showLocationDialog);
        }
    }

    private void showLocationDialog(LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Location");

        // Inflate the custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.add_location_map, null);
        builder.setView(dialogView);

        EditText inputPseudo = dialogView.findViewById(R.id.inputPseudoMap);
        EditText inputPhoneNumber = dialogView.findViewById(R.id.inputPhoneNumberMap);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String pseudo = inputPseudo.getText().toString().trim();
            String phoneNumber = inputPhoneNumber.getText().toString().trim();

            if (pseudo.isEmpty() || phoneNumber.isEmpty()) {
                Toast.makeText(getContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add location
            new AddLocationTask(pseudo, phoneNumber, String.valueOf(latLng.longitude), String.valueOf(latLng.latitude)).execute();
            Toast.makeText(getContext(), "Location saved!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
//recuperer la listes de ^positions
    private class DownloadDataTask extends AsyncTask<Void, Void, Void> {
        private AlertDialog alert;

        @Override
        protected void onPreExecute() {
            // Show loading dialog
            alert = new AlertDialog.Builder(getActivity())
                    .setTitle("Downloading")
                    .setMessage("Please wait...")
                    .create();
            alert.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Fetch data
            JSONParser parser = new JSONParser();
            JSONObject response = parser.makeRequest(Config.Url_GetAll);
            try {
                if (response.getInt("success") == 1) {
                    JSONArray positionsArray = response.getJSONArray("positions");
                    positions.clear();
                    for (int i = 0; i < positionsArray.length(); i++) {
                        JSONObject pos = positionsArray.getJSONObject(i);
                        positions.add(new Position(
                                pos.getInt("idposition"),
                                pos.getString("pseudo"),
                                pos.getString("numero"),
                                pos.getString("longitude"),
                                pos.getString("latitude")
                        ));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            if (alert.isShowing()) alert.dismiss();
            addMarkersToMap();
        }
    }

    private void addMarkersToMap() {
        if (mMap == null) return;
        mMap.clear();

        for (Position pos : positions) {
            LatLng location = new LatLng(
                    Double.parseDouble(pos.getLatitude()),
                    Double.parseDouble(pos.getLongitude())
            );
            mMap.addMarker(new MarkerOptions()
                    .position(location)

            );
        }
    }


    private class AddLocationTask extends AsyncTask<Void, Void, String> {
        private final String pseudo, phoneNumber, longitude, latitude;

        AddLocationTask(String pseudo, String phoneNumber, String longitude, String latitude) {
            this.pseudo = pseudo;
            this.phoneNumber = phoneNumber;
            this.longitude = longitude;
            this.latitude = latitude;
        }

        @Override
        protected String doInBackground(Void... voids) {
            HashMap<String, String> params = new HashMap<>();
            params.put("pseudo", pseudo);
            params.put("numero", phoneNumber);
            params.put("longitude", longitude);
            params.put("latitude", latitude);
            return new HttpHandler().makePostRequest(Config.URL_ADD_LOCATION, params);
        }

        @Override
        protected void onPostExecute(String response) {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                if (jsonResponse.getInt("success") == 1) {
                    LatLng newLocation = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                    mMap.addMarker(new MarkerOptions()
                            .position(newLocation)

                    );
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
