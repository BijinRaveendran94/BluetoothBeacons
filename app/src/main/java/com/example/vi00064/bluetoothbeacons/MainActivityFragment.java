// Copyright 2015 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.vi00064.bluetoothbeacons;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.IBeacon;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static java.lang.Math.pow;

public class MainActivityFragment extends Fragment {

  private static final String TAG = MainActivityFragment.class.getSimpleName();
  public static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
  private static final ScanSettings SCAN_SETTINGS =
    new ScanSettings.Builder().
      setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
      .setReportDelay(0)
      .build();

  private static final List<ScanFilter> SCAN_FILTERS = buildScanFilters();

  private static List<ScanFilter> buildScanFilters() {
    List<ScanFilter> scanFilters = new ArrayList<>();
    return scanFilters;
  }

  private AdapterBeacon arrayAdapter;
  private ScanCallback scanCallback;
  private BluetoothLeScanner scanner;
  ArrayList<HashMap<String, String>> beconsValues = new ArrayList<HashMap<String, String>>();
  ProgressDialog dialog;
  ListView listView;
  Button scanButton;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);



      scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
          int flag = 1;
            ScanRecord scanRecord = result.getScanRecord();
            List<ADStructure> structures = ADPayloadParser.getInstance().parse(scanRecord.getBytes());
            // For each AD structure contained in the payload.
            for (ADStructure structure : structures) {
                if (structure instanceof IBeacon)
                {
                  // iBeacon
                  IBeacon iBeacon = (IBeacon)structure;
                  HashMap <String,String> hashMap = new HashMap<>();
                  if(result.getDevice().toString().equals("F9:F4:1A:D8:4C:27")){
                    hashMap.put("name", "EST");
                    hashMap.put("power", String.valueOf(iBeacon.getPower()));
                  }
                  else {
                    hashMap.put("name", scanRecord.getDeviceName());
                    hashMap.put("power", String.valueOf(iBeacon.getPower()));
                  }
                  hashMap.put("uuid", String.valueOf(result.getDevice()));
                  String rssi = String.valueOf(result.getRssi());

                  Double distance = distance(Double.parseDouble(rssi), Integer.parseInt(hashMap.get("power")));
                  hashMap.put("distance", String.valueOf(distance));

                  if (beconsValues.size() > 0) {
                    for (int i = 0; i < beconsValues.size(); i++) {
                      if (beconsValues.get(i).get("uuid").equalsIgnoreCase(hashMap.get("uuid"))) {

                        flag = 2;
                        beconsValues.get(i).remove("distance");
                        Double newdistance = distance(Double.parseDouble(rssi), Integer.parseInt(hashMap.get("power")));
                        beconsValues.get(i).put("distance", String.valueOf(newdistance));
                      }
                    }
                    if (flag == 1) {
                      beconsValues.add(hashMap);
                    }
                  }
                  else {
                    beconsValues.add(hashMap);
                  }
                }
            }
            arrayAdapter = new AdapterBeacon(getActivity(), R.layout.list_beacons, beconsValues);
            listView.setAdapter(arrayAdapter);

            if (scanRecord == null) {
                Log.w(TAG, "Null ScanRecord for device " + result.getDevice().getAddress());
                return;
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
          Log.e(TAG, "onScanFailed errorCode " + errorCode);
        }
      };
    createScanner();
  }

  private void createScanner() {
    BluetoothManager btManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
    BluetoothAdapter btAdapter = btManager.getAdapter();
    if (btAdapter == null || !btAdapter.isEnabled()) {
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBtIntent, Constants.REQUEST_CODE_ENABLE_BLE);
    }
    if (btAdapter == null || !btAdapter.isEnabled()) {
      Log.e(TAG, "Can't enable Bluetooth");
      Toast.makeText(getActivity(), "Can't enable Bluetooth", Toast.LENGTH_SHORT).show();
      return;
    }
    scanner = btAdapter.getBluetoothLeScanner();
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
     if (requestCode == Constants.REQUEST_CODE_ENABLE_BLE) {
      if (resultCode == Activity.RESULT_OK) {
        createScanner();
      }
      else if (resultCode == Activity.RESULT_CANCELED) {
        Toast.makeText(getActivity(), "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
      }
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_main, container, false);

    final ProgressBar progressBar = (ProgressBar)rootView.findViewById(R.id.progressBar);
    progressBar.setProgress(0);
    progressBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
    dialog = new ProgressDialog(getActivity());
    dialog.setMessage("Loading");
    dialog.show();


    dialog.dismiss();
    listView = (ListView)rootView.findViewById(R.id.listView);
    scanButton = (Button)rootView.findViewById(R.id.scanButton);
    scanButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        scanner.startScan(SCAN_FILTERS, SCAN_SETTINGS, scanCallback);
      }
    });
    trilateral();
    return rootView;
  }

  public boolean checkLocationPermission() {
    // In Android 6.0 and higher you need to request permissions at runtime
    try {
      if (ContextCompat.checkSelfPermission(getActivity(),
              android.Manifest.permission.ACCESS_FINE_LOCATION)
              != PackageManager.PERMISSION_GRANTED) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    MY_PERMISSIONS_REQUEST_LOCATION);
          }
        } else {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    MY_PERMISSIONS_REQUEST_LOCATION);
          }
        }
        return false;
      } else {
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  public double distance (double rssi, int txPower) {
    double distance = 0;
    float i = (float) 0.75;

    if (rssi == 0) {
      return -1.0; // if we cannot determine accuracy, return -1.0
    }
    double ratio = rssi*1.0/txPower;
    if (ratio < 1.0) {
      distance =  pow(ratio,10);
    }
    else {
       distance =  (0.89976) * pow(ratio,7.7095) + 0.111;
    }
    if (distance < 0.1) {
      Log.e("Distance : " ,"low");
    }
    return distance;
  }

  public void trilateral(){

    float earthR = 6371;

    double LatA = 37.418436;
    double LonA = -121.963477;
    double DistA = 0.265710701754 ;

    double LatB = 37.417243;
    double LonB = -121.961889;
    double DistB = 0.234592423446 ;

    double LatC = 37.418692;
    double LonC = -121.960194;
    double DistC = 0.0548954278262;

    double xA = earthR *(Math.cos(Math.toRadians(LatA)) * Math.cos(Math.toRadians(LonA)));
    double yA = earthR *(Math.cos(Math.toRadians(LatA)) * Math.sin(Math.toRadians(LonA)));
    double zA = earthR *(Math.sin(Math.toRadians(LatA)));

    double xB = earthR *(Math.cos(Math.toRadians(LatB)) * Math.cos(Math.toRadians(LonB)));
    double yB = earthR *(Math.cos(Math.toRadians(LatB)) * Math.sin(Math.toRadians(LonB)));
    double zB = earthR *(Math.sin(Math.toRadians(LatB)));

    double xC = earthR *(Math.cos(Math.toRadians(LatC)) * Math.cos(Math.toRadians(LonC)));
    double yC = earthR *(Math.cos(Math.toRadians(LatC)) * Math.sin(Math.toRadians(LonC)));
    double zC = earthR *(Math.sin(Math.toRadians(LatC)));

    double[][] positions = new double[][] { { xA, yA, zA }, { xB, yB, zB }, { xC, yC, zC } };
    double[] distances = new double[] { DistA, DistB, DistC };

    NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
    LeastSquaresOptimizer.Optimum optimum = solver.solve();

    double[] centroid = optimum.getPoint().toArray();
    double value = centroid[2]/earthR;
    double lat = Math.toDegrees(Math.asin(centroid[2] / earthR));
    double lon = Math.toDegrees(Math.atan2(centroid[1],centroid[0])) ;

    Log.e("centroidvalues", centroid.toString());
  }
}
