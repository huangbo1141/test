package tepia.smartcycle;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.usmani.android.UIHelper;

import java.util.List;
import java.util.Vector;


public class BluetoothDevicesActivity4 extends CommonActivity {

    private static final String TAG = BluetoothDevicesActivity4.class.getName();

    private static final int REQUEST_COARSE_LOCATION_PERMISSION = 123;
    private static final int REQUEST_ENABLE_BT = 1245;
    private static final int SCAN_DURATION = 10000;

    BluetoothManager bluetoothManager = null;
    BluetoothAdapter mBluetoothAdapter = null;
    private SabDevicesAdapter mLeDeviceListAdapter;
    private List<BluetoothDevice> mDevices;
    private Handler mHandler;
    private boolean mScanning = false;
    private BluetoothLeScanner leScanner = null;
    private ListView listdevices;
    private boolean listInDeviceMode = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_devices);
        SabAppHelper.setHeaderTitle(getString(R.string.connection), this);
        listdevices = (ListView) findViewById(R.id.listdevices);
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mDevices = new Vector<BluetoothDevice>();
        //mLeDeviceListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new Vector<String>());
        mLeDeviceListAdapter=new SabDevicesAdapter(this,R.layout.layout_device,mDevices);
        listdevices.setAdapter(mLeDeviceListAdapter);
        mHandler = new Handler(getMainLooper());

        listdevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                if (!listInDeviceMode) {
                    return;
                }
                final BluetoothDevice device = mDevices.get(index);
                onDeviceSelected(device);
            }
        });

        if (AppCache.sabHelper == null) {
            AppCache.sabHelper = new SabBluetoothHelper(getApplicationContext());
        }

        displayCurrentDevice();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            requestEnableBluetooth();
        } else {
            initScanning();
        }
    }

    private String m_Text = "";

    private void sendPinCommandToDevice(String pin){
        try{
            String cmd="#SC#"+AppCache.currentCommandCode+"#"+pin+"@";
            UIHelper.makeLongToast("Sending "+cmd,AppCache.currentActivity);
            if (!AppCache.sabHelper.writeCommand(cmd)){
                UIHelper.makeLongToast("Error in writing command",AppCache.currentActivity);
            }else{
                localStorage.setPinCode(pin);
            }
        }catch(Exception ex){
            UIHelper.makeLongToast("Exception: "+ex.getMessage(),AppCache.currentActivity);
        }
    }

    private void showPinInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Device pin code");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        //input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                sendPinCommandToDevice(m_Text);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void onDeviceSelected(final BluetoothDevice device) {
        UIHelper.makeLongToast(getString(R.string.Connection_to) + device.getName(), BluetoothDevicesActivity4.this, false);
        if (AppCache.sabHelper.isDeviceConnected()) {
            try {
                AppCache.sabHelper.disconnect();
            } catch (Exception ex) {
            }
        }
        //mLeDeviceListAdapter.clear();
        //mLeDeviceListAdapter.notifyDataSetChanged();
        //listInDeviceMode = false;
        //progressBar.show();
        Thread thr = new Thread() {
            public void run() {
                try {
                    if (!AppCache.sabHelper.setDeviceAndConnect(device)) {
                        UIHelper.msbox("Error", "Unable to start connection to device", BluetoothDevicesActivity4.this);
                    }
                } catch (Exception ex) {
                    progressBar.dismiss();
                    UIHelper.msbox("Error", "Error in connect: " + ex.getMessage(), BluetoothDevicesActivity4.this);
                }
            }
        };
        thr.start();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "posting from handler");
                if (AppCache.sabHelper.isDeviceConnecting()) {
                    progressBar.dismiss();
                    AppCache.sabHelper.closeBtConnection();
                    UIHelper.msbox("Error", "Error connecting to device", AppCache.currentActivity);
                }
            }
        }, 60000);
    }

    private void requestEnableBluetooth() {
        UIHelper.makeLongToast(getString(R.string.bluetooth_not_enable_plesse_connect), BluetoothDevicesActivity4.this, false);
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppCache.sabHelper.removeOnSabEventListener(sabEventListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        AppCache.sabHelper.addOnSabEventListener(sabEventListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        AppCache.sabHelper.removeOnSabEventListener(sabEventListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                UIHelper.makeLongToast(getString(R.string.Bluetooth_not_enabled_onActivityResult), BluetoothDevicesActivity4.this, false);
            } else {
                startScanningDevices();
            }
        } else if (requestCode == REQUEST_COARSE_LOCATION_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    startScanningDevices();
                } else {
                    UIHelper.makeLongToast(getString(R.string.Location_permission_denied), BluetoothDevicesActivity4.this);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void displayCurrentDevice(){
        View vt = findViewById(R.id.layoutcurrentdevice);
        TextView txt = (TextView) findViewById(R.id.txtcurrentdevice);

        if (AppCache.sabHelper.isDeviceConnected()) {
            vt.setVisibility(View.VISIBLE);
            BluetoothDevice bdev = AppCache.sabHelper.getDevice();
            txt.setText("Connected to: " + bdev.getName());
            vt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPinInputDialog();
                }
            });
        } else {
            vt.setVisibility(View.GONE);
        }
    }

    public void continueClick(View v) {
        Log.d(TAG, "continueClick");
        Intent i = new Intent(this, StatusActivity.class);
        startActivity(i);
        finish();
    }

    public void onDeviceFound(BluetoothDevice device, String from) {
        String txt = device.getName();
        if (txt == null) {
            return;
        }
        for (int a = 0; a < mDevices.size(); a++) {
            BluetoothDevice dv = mDevices.get(a);
            if (device.getAddress().compareTo(dv.getAddress()) == 0) {
                return;
            }
        }
        txt += "\n" + device.getAddress();
        //mLeDeviceListAdapter.add(txt);
        mDevices.add(device);
        mLeDeviceListAdapter.notifyDataSetChanged();
    }

    private void initScanning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION_PERMISSION);
        } else {
            startScanningDevices();
        }
    }

    public void startScanningDevices() {
        progressBar.show();
        startScanning();
    }

    private void startScanning() {

        //Set<BluetoothDevice> devis=mBluetoothAdapter.getBondedDevices();
        //for (int a=0;a<devis.size();a++){

        //}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UIHelper.makeLongToast("Lollipop or above", BluetoothDevicesActivity4.this);
            startLollipopLeScanning();
        } else {
            UIHelper.makeLongToast("Below lollipop", BluetoothDevicesActivity4.this);
            startBelowLollipopLeScanning();
        }
    }

    private void scanningFinished() {
        if (!AppCache.sabHelper.isDeviceConnected()) {
            String devId = localStorage.getDeviceId();
            if (devId != null) {
                UIHelper.makeLongToast("Old device was: " + devId, AppCache.currentActivity);
                devId = devId.toLowerCase();
                for (int a = 0; a < mDevices.size(); a++) {
                    String rDevID = mDevices.get(a).getAddress().toLowerCase();
                    if (rDevID.compareTo(devId.toLowerCase()) == 0) {
                        UIHelper.makeLongToast("Device found in list: " + rDevID, AppCache.currentActivity);
                        onDeviceSelected(mDevices.get(a));
                        break;
                    }
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startLollipopLeScanning() {
        leScanner = mBluetoothAdapter.getBluetoothLeScanner();
        leScanner.startScan(lollipopLeCallback);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "posting from handler");
                mScanning = false;
                leScanner.stopScan(lollipopLeCallback);
                progressBar.dismiss();
                Log.d(TAG, "Stopped scanning");
                scanningFinished();
            }
        }, SCAN_DURATION);
        Log.d(TAG, "Started scanning");
        mScanning = true;
    }

    //this for lollipop and below
    private void startBelowLollipopLeScanning() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "posting from handler");
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                progressBar.dismiss();
                Log.d(TAG, "Stopped scanning");
                scanningFinished();
            }
        }, SCAN_DURATION);
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        Log.d(TAG, "Started scanning");
        mScanning = true;
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onDeviceFound(device, "LE Below Lollipop");
                        }
                    });
                }
            };

    private ScanCallback lollipopLeCallback = new ScanCallback() {
        private final String TAG = "lollipopLeCallback";

        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
            Log.d(TAG, "onBatchScanResults");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int a = 0; a < results.size(); a++) {
                        onDeviceFound(results.get(a).getDevice(), "LE Lollipop- batch");
                    }
                }
            });
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "onScanFailed");
            UIHelper.makeLongToast("onScanFailed: " + String.valueOf(errorCode), BluetoothDevicesActivity4.this);
            progressBar.dismiss();
        }

        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            Log.d(TAG, "onScanResult");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onDeviceFound(result.getDevice(), "LE Lollipop");
                }
            });
        }
    };


    /*****
     * OnSabEventListener start
     ******/

    DefaultSabEventListener sabEventListener = new DefaultSabEventListener() {
        @Override
        public void onDeviceConnected(SabBluetoothHelper sab) {
            UIHelper.makeLongToast(getString(R.string.connecting_to_device), AppCache.currentActivity,false);
            localStorage.setDeviceId(sab.getDevice().getAddress().toLowerCase());
            if (!AppCache.sabHelper.discoverServices()) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        progressBar.dismiss();
                    }
                });
                UIHelper.msbox("Error", "Error in discovering services", AppCache.currentActivity);
            }
        }

        @Override
        public void onServicesFound(SabBluetoothHelper sab, List<BluetoothGattService> services) {
            UIHelper.makeLongToast("BDA: Services found for SAB", AppCache.currentActivity);
            runOnUiThread(new Runnable() {
                public void run() {
                    progressBar.dismiss();
                }
            });
        }

        @Override
        public void onCharacteristicFound(SabBluetoothHelper sab, BluetoothGattCharacteristic characteristic) {
            String uuid = characteristic.getUuid().toString().toLowerCase();
            if (uuid.compareTo(SabBluetoothHelper.CHARACTERISTIC_COMMAND_UUID.toLowerCase()) == 0) {
                if (!AppCache.sabHelper.enableCharacteristicNotification(characteristic)) {
                    UIHelper.makeLongToast("BDA: Error in enabling notification for characteristic", AppCache.currentActivity);
                }
            }

            if (sab.commandCharacteristic != null && sab.dataCharacteristic != null
                    && AppCache.readThread == null) {
                AppCache.readThread = new SabReadThread();
                AppCache.readThread.startReading();
                runOnUiThread(new Runnable(){
                    public void run(){
                        displayCurrentDevice();
                        continueClick(null);
                    }
                });

            }
        }

        /*@Override
        public void onCommandExecuted(SabBluetoothHelper sab, String command) {
            UIHelper.makeLongToast("BDA: Command "+command+" executed",AppCache.currentActivity);
        }

        @Override
        public void onSabCommandResponseReceived(SabBluetoothHelper sab, String lastCommandExecuted, String response) {
            UIHelper.msbox("Response","BDA: Response of "+lastCommandExecuted+" is "+response,AppCache.currentActivity);
        }*/

        @Override
        public void onError(SabBluetoothHelper sab, int error) {
            UIHelper.makeLongToast("BDA: Error in Bluetooth operation", AppCache.currentActivity);
            runOnUiThread(new Runnable() {
                public void run() {
                    progressBar.dismiss();
                }
            });
        }
    };

    /***** OnSabEventListener end ******/
}
