package com.example.mybestlocation;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Position> positions;

    public PositionAdapter(Context context, ArrayList<Position> positions) {
        this.context = context;
        this.positions = positions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pos_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Position currentPosition = positions.get(position);

        holder.tvPositionName.setText(currentPosition.getPseudo());
        holder.tvCoordinates.setText("Lat: " + currentPosition.getLatitude() + ", Long: " + currentPosition.getLongitude());

        // Delete Button
        holder.btnDelete.setOnClickListener(v -> {
            new DeletePositionTask(position).execute(); // Call the AsyncTask to delete from the database
        });


        // Update Button
        holder.btnUpdate.setOnClickListener(v -> {
            // Placeholder for update functionality
            Toast.makeText(context, "Update feature not implemented yet", Toast.LENGTH_SHORT).show();
        });

        // Send SMS Button
        holder.btnSendSMS.setOnClickListener(v -> {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(currentPosition.getNumero(), null, "Send me your location.", null, null);
            Toast.makeText(context, "SMS Sent", Toast.LENGTH_SHORT).show();
        });

        // Open Map Button
        holder.btnOpenMap.setOnClickListener(v -> {
            String geoUri = "geo:" + currentPosition.getLatitude() + "," + currentPosition.getLongitude();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return positions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPositionName, tvCoordinates;
        ImageView btnDelete, btnUpdate, btnSendSMS, btnOpenMap;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPositionName = itemView.findViewById(R.id.tvPositionName);
            tvCoordinates = itemView.findViewById(R.id.tvCoordinates);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            btnSendSMS = itemView.findViewById(R.id.btnSendSMS);
            btnOpenMap = itemView.findViewById(R.id.btnOpenMap);
        }
    }


    private class DeletePositionTask extends AsyncTask<Void, Void, String> {
        private final int positionIndex;
        private final String deleteUrl = Config.URL_DELETE_POSITION; // Replace with your delete API URL

        public DeletePositionTask(int positionIndex) {
            this.positionIndex = positionIndex;
        }

        @Override
        protected String doInBackground(Void... voids) {
            Position positionToDelete = positions.get(positionIndex);
            String positionId = String.valueOf(positionToDelete.getIdpostition());

            System.out.println("Position Id: " + positionId);

            // Prepare request parameters
            HashMap<String, String> params = new HashMap<>();
            params.put("idposition", positionId);

            // Make the DELETE request
            HttpHandler httpHandler = new HttpHandler();
            return httpHandler.makePostRequest(deleteUrl, params); // Assuming the server handles deletion via POST
        }

        @Override
        protected void onPostExecute(String response) {

            //System.out.println("Server Response: " + response);

            try {
                JSONObject  jsonResponse = new JSONObject(response);
                int success = jsonResponse.getInt("success");
                String message = jsonResponse.getString("message");

                if (success==1) {
                    // Remove from the RecyclerView and notify the adapter
                    positions.remove(positionIndex);
                    notifyItemRemoved(positionIndex);
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            // Extract the "success" field

        }
    }

}


