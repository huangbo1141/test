package tepia.smartcycle;

import android.content.res.Resources;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.util.Log;

import com.usmani.android.DiskLruCache;
import com.usmani.android.UIHelper;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;



/**
 * Created by Rameez Usmani on 1/24/2016.
 */
public class SabBluetoothHelper
extends SabDataHelper{


    private static final String TAG=SabBluetoothHelper.class.getName();

    // public static String SERVICE_SESSION_DATA_UUID="0000180a-0000-1000-8000-00805f9b34fb";
    public static String SERVICE_SESSION_DATA_UUID="e1c8a0c8-367c-11e7-a919-92ebcb67fe30";
    //public static String CHARACTERISTIC_READ_DATA_UUID ="00002a24-0000-1000-8000-00805f9b34fb";
    public static String CHARACTERISTIC_READ_DATA_UUID= "e1c8a0c8-367c-11e7-a919-92ebcb67fe31";

    public static String SERVICE_COMMAND_UUID="49535343-fe7d-4ae5-8fa9-9fafd205e455";
    public static String CHARACTERISTIC_COMMAND_UUID="49535343-1e4d-4bd9-ba61-23c647249616";

    private List<OnSabEventListener> _listeners=new Vector<OnSabEventListener>();
    private Context context;
    private BluetoothDevice sabBtDevice;
    private BluetoothGatt sabBtConnecton;
    private String commandExecuted="";

    public BluetoothGattCharacteristic dataCharacteristic;
    public BluetoothGattCharacteristic commandCharacteristic;

    private boolean btOperationInProgress=false;
    private boolean btConnecting=false;
    private boolean deviceConnected=false;

    private List<BluetoothDevice> mDevices=new Vector<BluetoothDevice>();

    private BluetoothManager bluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private boolean mScanning = false;
    private BluetoothLeScanner leScanner = null;

    public SabBluetoothHelper(Context ctx){
        this.context=ctx;
    }

    public void addOnSabEventListener(OnSabEventListener listener) {
        _listeners.add(listener);
    }

    public void removeOnSabEventListener(OnSabEventListener listener){
        try {
            _listeners.remove(listener);
        }catch(Exception ex){}
    }

    public List<BluetoothDevice> getDeviceList(){
        return mDevices;
    }

    public void clearDeviceList(){
        mDevices.clear();
    }

    public void disconnect(){
        try{
            if (sabBtConnecton!=null){
                sabBtConnecton.close();
            }
        }catch(Exception ex){}

        btConnecting=false;
        deviceConnected=false;
    }

    public BluetoothDevice getDevice(){
        return sabBtDevice;
    }

    public BluetoothGatt getConnection(){
        return sabBtConnecton;
    }

    public boolean isBtOperationInProgress(){
        return btOperationInProgress;
    }

    public boolean isDeviceConnecting(){
        return btConnecting;
    }

    public boolean isDeviceConnected(){
        return deviceConnected;
    }

    public void reconnectRepeat(){
        disconnect();
        Thread thr=new Thread(){
            public void run(){
                while(true) {
                    //UIHelper.makeLongToast( getString(R.string.reconnecting_to_SAB), AppCache.currentActivity,false);
                    UIHelper.makeLongToast( context.getString(R.string.reconnecting_to_SAB), AppCache.currentActivity,false);
                    reconnectWithDevice();
                    try{
                        Thread.sleep(15000);
                    }catch(Exception ex){}
                    if (!isDeviceConnecting() && isDeviceConnected()){
                        break;
                    }
                }
            }
        };
        thr.start();
    }

    public void reconnectWithDevice(){
        deviceConnected=false;
        setDeviceAndConnect(sabBtDevice);
    }

    public boolean setDeviceAndConnect(BluetoothDevice dev){
        sabBtDevice=dev;
        sabBtConnecton = sabBtDevice.connectGatt(context, true, mGattCallback);
        boolean btVal=sabBtConnecton.connect();
        if (btVal) {
            btOperationInProgress = true;
            btConnecting = true;
        }else{
            sabBtDevice=null;
            try {
                sabBtConnecton.close();
            }catch(Exception ex){

            }
        }
        return btVal;
    }

    public void closeBtConnection(){
        deviceConnected=false;
        if (sabBtConnecton!=null){
            sabBtConnecton.close();
        }
    }

    public boolean writeCommand(byte[] bytesToWrite){
        boolean btVal=commandCharacteristic.setValue(bytesToWrite);
        if (!btVal){
            return false;
        }
        btVal=sabBtConnecton.writeCharacteristic(commandCharacteristic);
        if (btVal){
            commandExecuted=new String(bytesToWrite);
            btOperationInProgress=true;
        }
        return btVal;
    }

    public boolean writeCommand(String str){
        if (commandCharacteristic==null){
            return false;
        }
        //23474440
        //byte[] bytesToWrite=new byte[]{0x23,0x47,0x44,0x40};
        String bytesToWrite=str;
        boolean btVal=commandCharacteristic.setValue(bytesToWrite);
        if (!btVal){
            return false;
        }
        commandExecuted=str;
        btVal=sabBtConnecton.writeCharacteristic(commandCharacteristic);
        if (btVal){
            btOperationInProgress=true;
        }
        return btVal;
    }

    public boolean readSabData(){
        boolean btVal=sabBtConnecton.readCharacteristic(dataCharacteristic);
        if (btVal){
            btOperationInProgress=true;
        }
        return btVal;
    }

    public boolean discoverServices(){
        boolean btVal=sabBtConnecton.discoverServices();
        if (btVal){
            btOperationInProgress=true;
        }
        return btVal;
    }

    public boolean enableCharacteristicNotification(BluetoothGattCharacteristic characteristic){
        List<BluetoothGattDescriptor> descriptors=characteristic.getDescriptors();
        boolean btVal=true;
        for (int d=0;d<descriptors.size();d++){
            BluetoothGattDescriptor descr=descriptors.get(d);
            descr.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            btVal=sabBtConnecton.writeDescriptor(descr);
        }
        if (btVal) {
            btVal=sabBtConnecton.setCharacteristicNotification(characteristic, true);
        }
        return btVal;
    }

    // Various callback methods defined by the BLE API.
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            btOperationInProgress=false;
            btConnecting=false;
            Log.d(TAG,"onConnectionStateChange: "+String.valueOf(newState)+","+String.valueOf(status));
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                for (int a=0;a<_listeners.size();a++){
                    _listeners.get(a).onDeviceConnected(SabBluetoothHelper.this);
                }
                //UIHelper.makeLongToast("Connected to device.Discovering services...", AppCache.currentActivity);
                if (!discoverServices()){
                    for (int a=0;a<_listeners.size();a++){
                        _listeners.get(a).onError(SabBluetoothHelper.this,1);
                    }
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
                disconnect();
                for (int a=0;a<_listeners.size();a++){
                    _listeners.get(a).onDeviceDisconnected(SabBluetoothHelper.this);
                }
                reconnectRepeat();
            }
        }

        @Override
        // New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            btOperationInProgress=false;
            Log.d(TAG,"onServicesDiscovered");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = gatt.getServices();
                for (int l=0;l<_listeners.size();l++){
                    _listeners.get(l).onServicesFound(SabBluetoothHelper.this,services);
                }
                for (int a = 0; a < services.size(); a++) {
                    BluetoothGattService srv = services.get(a);
                    String str = srv.getUuid().toString().toLowerCase();
                    if (str.compareTo(SabBluetoothHelper.SERVICE_SESSION_DATA_UUID.toLowerCase()) == 0
                        ||str.compareTo(SabBluetoothHelper.SERVICE_COMMAND_UUID.toLowerCase()) == 0) {
                        List<BluetoothGattCharacteristic> charcs = srv.getCharacteristics();
                        for (int b = 0; b < charcs.size(); b++) {
                            BluetoothGattCharacteristic characteristic=charcs.get(b);
                            String cstr=characteristic.getUuid().toString().toLowerCase();
                            if (cstr.compareTo(SabBluetoothHelper.CHARACTERISTIC_READ_DATA_UUID.toLowerCase())==0){
                                dataCharacteristic=characteristic;
                            }else if (cstr.compareTo(SabBluetoothHelper.CHARACTERISTIC_COMMAND_UUID.toLowerCase())==0){
                                commandCharacteristic=characteristic;
                            }

                            for (int l=0;l<_listeners.size();l++){
                                _listeners.get(l).onCharacteristicFound(SabBluetoothHelper.this,characteristic);
                            }
                        }

                        deviceConnected=true;
                    }
                }
            }else{
                for (int l=0;l<_listeners.size();l++){
                    _listeners.get(l).onError(SabBluetoothHelper.this,status);
                }
            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic,int status) {
            btOperationInProgress=false;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                String uuid=characteristic.getUuid().toString().toLowerCase();
                if (uuid.compareTo(SabBluetoothHelper.CHARACTERISTIC_READ_DATA_UUID.toLowerCase())==0){
                    byte[] dataBytes=characteristic.getValue();
                    SabData sData=processSabData(dataBytes);
                    if (sData!=null){
                        for (int l=0;l<_listeners.size();l++){
                            _listeners.get(l).onSabDataReceived(SabBluetoothHelper.this,sData);
                        }
                    }else{
                        for (int l=0;l<_listeners.size();l++){
                            _listeners.get(l).onError(SabBluetoothHelper.this,status);
                        }
                    }
                }
            }else{
                for (int l=0;l<_listeners.size();l++){
                    _listeners.get(l).onError(SabBluetoothHelper.this,status);
                }
            }
        }

        @Override
        public void onCharacteristicChanged (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
            String uuid=characteristic.getUuid().toString().toLowerCase();
            if (uuid.compareTo(SabBluetoothHelper.CHARACTERISTIC_COMMAND_UUID.toLowerCase())==0){
                String strVal=characteristic.getStringValue(0);
                for (int l=0;l<_listeners.size();l++){
                    _listeners.get(l).onSabCommandResponseReceived(SabBluetoothHelper.this,commandExecuted,strVal);
                }
            }/*else if (uuid.compareTo(SabBluetoothHelper.CHARACTERISTIC_READ_DATA_UUID.toLowerCase())==0) {
                byte[] dataBytes = characteristic.getValue();
                SabData sData = processSabData(dataBytes);
                if (sData != null) {
                    for (int l = 0; l < _listeners.size(); l++) {
                        _listeners.get(l).onSabDataReceived(SabBluetoothHelper.this, sData);
                    }
                }
            }*/
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,int status){
            btOperationInProgress=false;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                String uuid = characteristic.getUuid().toString().toLowerCase();
                if (uuid.compareTo(SabBluetoothHelper.CHARACTERISTIC_COMMAND_UUID.toLowerCase()) == 0) {
                    String strVal = characteristic.getStringValue(0);
                    commandExecuted=strVal;
                    for (int l=0;l<_listeners.size();l++){
                        _listeners.get(l).onCommandExecuted(SabBluetoothHelper.this,strVal);
                    }
                }
            }else{
                for (int l=0;l<_listeners.size();l++){
                    _listeners.get(l).onError(SabBluetoothHelper.this,status);
                }
            }
        }
    };
}
