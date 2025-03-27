package com.example.amplenoteclone.utils;

import java.util.ArrayList;

public interface FirestoreListCallback<T> {
    void onCallback(ArrayList<T> data);
}

