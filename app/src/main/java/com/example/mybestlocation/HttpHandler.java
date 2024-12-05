package com.example.mybestlocation;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class HttpHandler {
    public String makePostRequest(String requestURL, HashMap<String, String> postDataParams) {
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            StringBuilder params = new StringBuilder();
            for (HashMap.Entry<String, String> entry : postDataParams.entrySet()) {
                if (params.length() != 0) params.append("&");
//                Ajoute la clé et la valeur au format clé=valeur
                params.append(entry.getKey()).append("=").append(entry.getValue());
            }
            os.write(params.toString().getBytes());
            os.flush();
            os.close();
//            Crée un lecteur tamponné pour lire la réponse reçue du serveur.
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
//            Lit les lignes de la réponse tant qu'il y en a.
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.toString();
    }
}


