package tepia.smartcycle;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

/**
 * Created by Rameez Usmani on 6/4/2017.
 */

public class DefaultSabEventListener implements OnSabEventListener{
    @Override
    public void onDeviceConnected(SabBluetoothHelper sab) {
    }

    @Override
    public void onDeviceDisconnected(SabBluetoothHelper sab){

    }

    @Override
    public void onServicesFound(SabBluetoothHelper sab, List<BluetoothGattService> services) {
    }

    @Override
    public void onCharacteristicFound(SabBluetoothHelper sab, BluetoothGattCharacteristic characteristic) {
    }

    @Override
    public void onSabDataReceived(SabBluetoothHelper sab, SabData sData) {
    }

    @Override
    public void onCommandExecuted(SabBluetoothHelper sab, String command) {
    }

    @Override
    public void onSabCommandResponseReceived(SabBluetoothHelper sab, String lastCommandExecuted, String response) {
    }

    @Override
    public void onError(SabBluetoothHelper sab, int error) {
    }
}
