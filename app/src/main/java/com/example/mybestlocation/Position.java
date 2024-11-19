package com.example.mybestlocation;

public class Position {
    int idpostition;
    String pseudo, numero,longitude, latitude;

    public Position(int idpostition, String pseudo, String numero, String longitude, String latitude) {
        this.idpostition = idpostition;
        this.pseudo = pseudo;
        this.numero = numero;
        this.longitude = longitude;
        this.latitude = latitude;
    }


    public Position(String pseudo, String numero, String longitude, String latitude) {
        this.pseudo = pseudo;
        this.numero = numero;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public int getIdpostition() {
        return idpostition;
    }

    public String getPseudo() {
        return pseudo;
    }

    public String getNumero() {
        return numero;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    @Override
    public String toString() {
        return "Position{" +
                "idpostition=" + idpostition +
                ", pseudo='" + pseudo + '\'' +
                ", numero='" + numero + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                '}';
    }
}
