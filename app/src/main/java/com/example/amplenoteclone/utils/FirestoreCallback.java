package com.example.amplenoteclone.utils;

import java.util.ArrayList;

public interface FirestoreCallback<T> {
    void onCallback(ArrayList<T> data);
}
