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
    String title, mode;
    Bundle bundle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_maps, container, false);

        bundle = getArguments();
        if (bundle != null) {
            title = bundle.getString("title", "Map");
            mode = bundle.getString("mode", "get");
        }

        // Set the title above the map
        TextView titleTextView = root.findViewById(R.id.tv_title_map);
        titleTextView.setText(title);


        // Initialize MapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this); // Trigger map load

        // Start downloading data and adding markers
        new DownloadDataTask().execute();

        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        System.out.println(bundle != null);
        // Customize map settings
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); // Can be NORMAL, TERRAIN, SATELLITE, or HYBRID
        mMap.getUiSettings().setZoomControlsEnabled(true); // Enable zoom controls
        mMap.getUiSettings().setCompassEnabled(true); // Enable compass
        mMap.getUiSettings().setMapToolbarEnabled(true); // Enable map toolbar
        mMap.getUiSettings().setScrollGesturesEnabled(true); // Allow scrolling
        mMap.getUiSettings().setTiltGesturesEnabled(true); // Allow tilt gestures
        mMap.getUiSettings().setRotateGesturesEnabled(true); // Allow rotation gestures

        // Optionally set default map settings
        LatLng defaultLocation = new LatLng(0, 0);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(defaultLocation)
                .zoom(2) // Zoom level
                .tilt(30) // Tilt the camera for 3D effect
                .bearing(0) // Direction the camera is facing
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        if (bundle != null && mode.equals("add")) {
            googleMap.setOnMapClickListener(latLng -> showLocationDialog(latLng));
        }

    }

    private void showLocationDialog(LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Location");

        // Inflate the custom layout
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

            // Create a new Position object
            Position newPosition = new Position(pseudo, phoneNumber,
                    String.valueOf(latLng.longitude),
                    String.valueOf(latLng.latitude));

            // Add to RecyclerView and notify adapter
            //data.add(newPosition);
            //adapter.notifyDataSetChanged();

            // Optionally, save to the database
            new AddLocationTask(pseudo, phoneNumber, String.valueOf(latLng.longitude), String.valueOf(latLng.latitude)).execute();

            Toast.makeText(getContext(), "Location saved!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private class DownloadDataTask extends AsyncTask<Void, Void, Void> {

        AlertDialog alert;

        @Override
        protected void onPreExecute() {
            // Show loading dialog
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle("Downloading");
            dialog.setMessage("Please wait...");
            alert = dialog.create();
            alert.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Fetch data in background
            JSONParser parser = new JSONParser();
            JSONObject response = parser.makeRequest(Config.Url_GetAll);

            try {
                if (response.getInt("success") == 1) {
                    JSONArray positionsArray = response.getJSONArray("positions");
                    positions.clear();
                    for (int i = 0; i < positionsArray.length(); i++) {
                        JSONObject positionObj = positionsArray.getJSONObject(i);
                        positions.add(new Position(
                                positionObj.getInt("idposition"),
                                positionObj.getString("pseudo"),
                                positionObj.getString("numero"),
                                positionObj.getString("longitude"),
                                positionObj.getString("latitude")
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
            // Dismiss loading dialog
            if (alert != null && alert.isShowing()) {
                alert.dismiss();
            }

            // Add markers to the map
            addMarkersToMap();
        }
    }

    private void addMarkersToMap() {
        if (mMap == null) return;

        // Clear existing markers
        mMap.clear();

        // Add markers for each position
        for (Position position : positions) {
            double lat = Double.parseDouble(position.getLatitude());
            double lon = Double.parseDouble(position.getLongitude());
            LatLng location = new LatLng(lat, lon);

            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(position.getPseudo())
                    .snippet("Number: " + position.getNumero())); // Add extra details
        }

        // Zoom to fit all markers
        if (!positions.isEmpty()) {
            LatLng firstPosition = new LatLng(
                    Double.parseDouble(positions.get(0).getLatitude()),
                    Double.parseDouble(positions.get(0).getLongitude()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstPosition, 12));
        }
    }


    private class AddLocationTask extends AsyncTask<Void, Void, String> {
        private final String pseudo, phoneNumber, longitude, latitude;
        private final String url = Config.URL_ADD_LOCATION; // Replace with the correct URL

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

            HttpHandler httpHandler = new HttpHandler();
            return httpHandler.makePostRequest(url, params);
        }

        @Override
        protected void onPostExecute(String response) {
            try {
                JSONObject jsonResponse = new JSONObject(response);

                // Extract the "success" field
                int success = jsonResponse.getInt("success");
                String message = jsonResponse.getString("message");

                if (success == 1) {
                    // Add the marker to the map
                    LatLng newLocation = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                    mMap.addMarker(new MarkerOptions()
                            .position(newLocation)
                            .title(pseudo)
                            .snippet("Number: " + phoneNumber));
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to add location: " + message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException ex) {
                throw new RuntimeException(ex);
            }
        }

    }
}
