package tepia.smartcycle;

import android.app.Activity;
import android.widget.TextView;

/**
 * Created by Rameez Usmani on 3/8/2016.
 */
public class SabAppHelper {
    public static void setHeaderTitle(String title,Activity act){
        TextView txttitle=(TextView)act.findViewById(R.id.txtheader);
        if (txttitle!=null){
            //txttitle.setText(title.toUpperCase());
            txttitle.setText(title);
        }
    }

    public static long getTimestamp(){
        long ts=new java.util.Date().getTime();
        return (ts/1000);
    }
}
