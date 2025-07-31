package com.example.androidfinalproject;

public class Receipt {
    int id;
    String vendor;
    String date;
    double total;

    public Receipt(int id, String vendor, String date, double total) {
        this.id = id;
        this.vendor = vendor;
        this.date = date;
        this.total = total;
    }
}
