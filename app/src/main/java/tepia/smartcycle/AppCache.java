package tepia.smartcycle;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by Rameez Usmani on 1/22/2016.
 */
public class AppCache {

    public static String currentCommandCode="234";

    public static SetupData currentSetupData=new SetupData();

    public static BluetoothDevice sabDevice;
    public static BluetoothGatt sabDeviceConnection;
    public static BluetoothGattCharacteristic sabReadDataCharacteristic;
    public static BluetoothGattCharacteristic sabCommandCharacteristic;

    public static SabBluetoothHelper sabHelper=null;
    public static Activity currentActivity;
    public static SabData sabData=new SabData();

    public static SabReadThread readThread=null;
    public static SabSettings currentSettings=new SabSettings();
    public static boolean isApplyingSettings=false;

    public static boolean showStatus=false;

    public static byte[] getDummyBytes(){
        //F00000000033010000000000000014FE00000000
        //F0 00 00 00 00 33 01 00 00 00 00 00 00 00 14 FE 00 00 00 00
        //byte[] bt=new byte[]{0x11,0x00,0x00,0x00,0x00,(byte)0x88,0x45,0x00,0x00,0x00,0x00,(byte)0x88,0x09,(byte)0xC4,0x14,(byte)0x05,00,00,00,00};
        byte[] bt=new byte[]{(byte)0xF0,0x00,0x00,0x00,0x00,0x01,0x01,0x00,0x00,0x00,0x00,0x11,0x00,0x00,(byte)0x14,(byte)0xFF,0x00,0x00,0x00,0x00};
        return bt;
    }

    public static void setDummyData(){
        sabData.sabName="Name";
        sabData.identifier="Identifier";
        sabData.firmwareVersion="Firmware";
        sabData.date="111117";
        sabData.time="143222";
    }
}