package com.example.mybestlocation.ui.home;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;

import com.example.mybestlocation.HttpHandler;
import com.example.mybestlocation.Position;
import com.example.mybestlocation.PositionAdapter;
import com.example.mybestlocation.databinding.FragmentHomeBinding;
import com.example.mybestlocation.JSONParser;
import com.example.mybestlocation.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeFragment extends Fragment {

    private ArrayList<Position> data = new ArrayList<>();
    private FragmentHomeBinding binding;
    private PositionAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set up the RecyclerView
        binding.rvpositions.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PositionAdapter(getActivity(), data);
        binding.rvpositions.setAdapter(adapter);

        // Start the data download
        new DownloadDataTask().execute();

        return root;
    }

    // kol me bech nstha9ou cnx lezm nstamlou thread hika aaleh amlna extend AsyncTask
//utilisé le thread pour la connection car il ya un autre thread qui occupe l'ecran de tel
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
        //second thread (run ta3na)
        @Override
        protected Void doInBackground(Void... voids) {
            // Fetch data in background
            JSONParser parser = new JSONParser();
            JSONObject response = parser.makeRequest(Config.Url_GetAll);

            try {
                if (response.getInt("success") == 1) {
                    JSONArray positions = response.getJSONArray("positions");
                    data.clear();
                    for (int i = 0; i < positions.length(); i++) {
                        JSONObject pos = positions.getJSONObject(i);
                        data.add(new Position(
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
            // Dismiss loading dialog
            if (alert != null && alert.isShowing()) {
                alert.dismiss();
            }
            adapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
