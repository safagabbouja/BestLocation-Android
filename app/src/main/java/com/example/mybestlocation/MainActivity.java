package com.example.mybestlocation;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

import com.example.mybestlocation.ui.home.HomeFragment;
import com.example.mybestlocation.ui.home.MapsFragment;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Position> data = new ArrayList<>();
    private HomeFragment homeFragment;
    private Button btnAddPosition, btnOpenMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
// Log initial
        Log.d("MainActivity", "onCreate called - Application started");
        // Initialize HomeFragment
        homeFragment = new HomeFragment();



        // Buttons
        btnAddPosition = findViewById(R.id.btn_add_pos);
        btnOpenMap = findViewById(R.id.btnOpenMap);

        if ( btnAddPosition == null || btnOpenMap == null) {
            Log.e("MainActivity", "Buttons not found in layout!");
            return;
        }

        // Set initial button visibility
        loadFragment(homeFragment);


        // Button to show RecyclerView (HomeFragment)
        btnAddPosition.setOnClickListener(v -> {
            MapsFragment mapsFragment = new MapsFragment();

            // Pass title to MapsFragment
            Bundle bundle = new Bundle();
            bundle.putString("title", "Select a new position to add");
            bundle.putString("mode", "add");
            mapsFragment.setArguments(bundle);

            loadFragment(mapsFragment);
        });

        // Button to show MapFragment with locations
        btnOpenMap.setOnClickListener(v -> {
            MapsFragment map = new MapsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("title", "All Positions");
            bundle.putString("mode", "get");
            map.setArguments(bundle);
            loadFragment(map);
        });


    }


    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


}
