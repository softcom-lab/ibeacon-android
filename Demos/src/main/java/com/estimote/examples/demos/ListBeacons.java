package com.estimote.examples.demos;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.estimote.sdk.utils.L;

import java.util.Collections;
import java.util.List;

/**
 * Displays list of found beacons sorted by RSSI.
 * Starts new activity with selected beacon if activity was provided.
 *
 * @author wiktorgworek@google.com (Wiktor Gworek)
 */
public class ListBeacons extends Activity {

    private static final String TAG = ListBeacons.class.getSimpleName();

    public static final String EXTRAS_TARGET_ACTIVITY = "extrasTargetActivity";
    public static final String EXTRAS_BEACON = "extrasBeacon";

    private static final int REQUEST_ENABLE_BT = 1234;
    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);

    private BeaconManager beaconManager;
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Configure device list.
        adapter = new Adapter(this);
        ListView list = (ListView) findViewById(R.id.device_list);
        list.setAdapter(adapter);
        //list.setOnItemClickListener(createOnItemClickListener());

        // Configure verbose debug logging.
        L.enableDebugLogging(true);

        // Configure BeaconManager.
        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                // Note that results are not delivered on UI thread.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        for( int a = 0 ; a < adapter.beacons.size(); a++){
                            double distance = Math.min(Utils.computeAccuracy(adapter.beacons.get(a)), 6.0);
                            int major = adapter.beacons.get(a).getMajor();
                            if(distance < .3 && major == 16664){
                                String url = "http://i2.kym-cdn.com/entries/icons/original/000/000/426/Woll_Smoth_original.jpg";
                                Uri s = Uri.parse(url);

                                System.out.println(s);

                                startActivity(new Intent(Intent.ACTION_VIEW, s));
                            }
                            else  if(distance < .3 && major == 2239){
                                String url = "http://www.google.com";
                                Uri s = Uri.parse(url);

                                System.out.println(s);

                                startActivity(new Intent(Intent.ACTION_VIEW, s));
                            }
                            else  if(distance < .3 && major == 40611){
                                String url = "http://www.facebook.com";
                                Uri s = Uri.parse(url);

                                System.out.println(s);

                                startActivity(new Intent(Intent.ACTION_VIEW, s));
                            }


                        }
                        // Note that beacons reported here are already sorted by estimated
                        // distance between device and beacon.
                        getActionBar().setSubtitle("Found beacons: " + beacons.size());
                        adapter.replaceWith(beacons);
                    }
                });
            }
        });
    }




    @Override
    protected void onDestroy() {
        beaconManager.disconnect();

        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if device supports Bluetooth Low Energy.
        if (!beaconManager.hasBluetooth()) {
            Toast.makeText(this, "Device does not have Bluetooth Low Energy", Toast.LENGTH_LONG).show();
            return;
        }

        // If Bluetooth is not enabled, let user enable it.
        if (!beaconManager.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            connectToService();
        }
    }

    @Override
    protected void onStop() {
        try {
            beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
        } catch (RemoteException e) {
            Log.d(TAG, "Error while stopping ranging", e);
        }

        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                connectToService();
            } else {
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_LONG).show();
                getActionBar().setSubtitle("Bluetooth not enabled");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void connectToService() {
        getActionBar().setSubtitle("Scanning...");
        adapter.replaceWith(Collections.<Beacon>emptyList());
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
                } catch (RemoteException e) {
                    Toast.makeText(ListBeacons.this, "Cannot start ranging, something terrible happened",
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Cannot start ranging", e);
                }
            }
        });
    }



}
