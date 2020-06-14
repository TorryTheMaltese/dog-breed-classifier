package com.example.dogbreedclassifier;

import android.net.Uri;

public class RecyclerView {
    private String dog_name;
    private String dog_age;
    private String dog_weight;
    private String dog_size;
    private String dog_fur;
    private String dog_result;
    private Uri dog_image;
    private int id;

    public String getDog_name() {
        return dog_name;
    }

    public void setDog_name(String dog_name) {
        this.dog_name = dog_name;
    }

    public String getDog_age() {
        return dog_age;
    }

    public void setDog_age(String dog_age) {
        this.dog_age = dog_age;
    }

    public String getDog_weight() {
        return dog_weight;
    }

    public void setDog_weight(String dog_weight) {
        this.dog_weight = dog_weight;
    }

    public String getDog_size() {
        return dog_size;
    }

    public void setDog_size(String dog_size) {
        this.dog_size = dog_size;
    }

    public String getDog_fur() {
        return dog_fur;
    }

    public void setDog_fur(String dog_fur) {
        this.dog_fur = dog_fur;
    }

    public Uri getDog_image() {
        return dog_image;
    }

    public void setDog_image(Uri dog_image) {
        this.dog_image = dog_image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDog_result() {
        return dog_result;
    }

    public void setDog_result(String dog_result) {
        this.dog_result = dog_result;
    }

    public RecyclerView() {
        this.dog_name = dog_name;
        this.dog_age = dog_age;
        this.dog_weight = dog_weight;
        this.dog_size = dog_size;
        this.dog_fur = dog_fur;
        this.dog_image = dog_image;
        this.id = id;
        this.dog_result = dog_result;
    }
}
