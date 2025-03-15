package com.example.amplenoteclone;

import android.os.Bundle;
import android.view.Menu;

public class NotesActivity extends DrawerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_notes, menu);
        return true;
    }

    @Override
    protected int getCurrentPageId() {
        return R.id.action_notes;
    }

    @Override
    protected String getToolbarTitle() {
        return "Notes";
    }
}
