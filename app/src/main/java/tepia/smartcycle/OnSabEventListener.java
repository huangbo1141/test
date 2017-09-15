package tepia.smartcycle;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

/**
 * Created by Rameez Usmani on 6/4/2017.
 */

public interface OnSabEventListener {
    void onDeviceConnected(SabBluetoothHelper sab);
    void onDeviceDisconnected(SabBluetoothHelper sab);
    void onServicesFound(SabBluetoothHelper sab, List<BluetoothGattService> services);
    void onCharacteristicFound(SabBluetoothHelper sab,BluetoothGattCharacteristic characteristic);
    void onSabDataReceived(SabBluetoothHelper sab,SabData sData);
    void onCommandExecuted(SabBluetoothHelper sab,String command);
    void onSabCommandResponseReceived(SabBluetoothHelper sab,String lastCommandExecuted,String response);
    void onError(SabBluetoothHelper sab,int error);
}
