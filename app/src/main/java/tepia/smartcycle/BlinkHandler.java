package tepia.smartcycle;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.usmani.android.UIHelper;

import java.util.List;
import java.util.Vector;

/**
 * Created by Rameez Usmani on 6/17/2017.
 */

public class BlinkHandler
extends Thread {

    private Activity activity;

    private boolean continueBlinking=true;

    public volatile List<ImageView> viewsToBlink=new Vector<ImageView>();
    private boolean blinkOn=true;

    public BlinkHandler(Activity act){
        activity=act;
    }

    public void stopBlinking(){
        continueBlinking=false;
        viewsToBlink.clear();
    }

    public void startBlinking(){
        continueBlinking=true;
        blinkOn=true;
        start();
    }

    public void run(){
        while(continueBlinking){
            if (viewsToBlink.size()>0) {
                activity.runOnUiThread(new Runnable(){
                    public void run(){
                        for (int a = 0; a < viewsToBlink.size(); a++) {
                            ImageView imgV = viewsToBlink.get(a);
                            Bitmap bmp=null;
                            if (blinkOn) {
                                //imgV.setImageResource(R.drawable.balanced);
                                bmp= UIHelper.decodeSampledBitmapFromDrawable(activity.getResources(),R.drawable.balanced,78,70);
                            } else {
                                //imgV.setImageResource(R.drawable.balanced_grey);
                                bmp= UIHelper.decodeSampledBitmapFromDrawable(activity.getResources(),R.drawable.balanced_grey,78,70);
                            }
                            imgV.setImageBitmap(bmp);
                        }
                        blinkOn = !blinkOn;
                    }
                });
            }

            try{
                Thread.sleep(500);
            }catch(Exception ex){

            }
        }
    }
}
