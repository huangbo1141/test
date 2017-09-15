package tepia.smartcycle;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Rameez Usmani on 6/20/2017.
 */

public class SabDevicesAdapter
extends ArrayAdapter<BluetoothDevice> {
    public SabDevicesAdapter(Context context, int resource, List<BluetoothDevice> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position,View convertView,ViewGroup parent){
        View view=convertView;
        if (view==null){
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view=inflater.inflate(R.layout.layout_device,parent,false);
        }
        TextView txtcname=(TextView)view.findViewById(R.id.txtdevicename);
        BluetoothDevice device=getItem(position);
        String txt = device.getName();
        txt += "\n" + device.getAddress();
        txtcname.setText(txt);

        return view;
    }
}
