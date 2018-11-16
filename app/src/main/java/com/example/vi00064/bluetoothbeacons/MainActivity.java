package com.example.vi00064.bluetoothbeacons;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//        Fragment fragment = new MainActivityFragment();
//        fragmentTransaction.replace(R.id.container, fragment);
//        fragmentTransaction.commit();

        Fragment fragment = new MainActivityFragment();
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.containers, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }
}
