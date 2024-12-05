package com.example.mybestlocation;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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


        // Vérification de la permission SEND_SMS
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.SEND_SMS},
                    1);
        }
        Log.d("MainActivity", "onCreate called - Application started");


        homeFragment = new HomeFragment();



        // Buttons
        btnAddPosition = findViewById(R.id.btn_add_pos);
        btnOpenMap = findViewById(R.id.btnOpenMap);

        if ( btnAddPosition == null || btnOpenMap == null) {
            Log.e("MainActivity", "Buttons not found in layout!");
            return;
        }

        loadFragment(homeFragment);


        btnAddPosition.setOnClickListener(v -> {
            MapsFragment mapsFragment = new MapsFragment();

            Bundle bundle = new Bundle();
            bundle.putString("mode", "add");
            mapsFragment.setArguments(bundle);

            loadFragment(mapsFragment);
        });

        btnOpenMap.setOnClickListener(v -> {
            MapsFragment map = new MapsFragment();
            Bundle bundle = new Bundle();
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) { // Code correspondant à la demande SEND_SMS
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission pour envoyer des SMS accordée", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission pour envoyer des SMS refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }




}
