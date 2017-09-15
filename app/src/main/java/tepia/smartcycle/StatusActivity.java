package tepia.smartcycle;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.usmani.android.UIHelper;
import com.usmani.gif.GifAnimationDrawable;

import java.io.IOException;

public class StatusActivity extends CommonActivity {

    private static final String TAG=StatusActivity.class.getName();

    private TextView txtdate,txttime;
    private TextView txtatmosphere;
    private ViewGroup atmospherecontainer;
    private TextView txtprobe1temperature;
    private TextView txtpreheattemperature;
    private TextView txtfanspeed;
    private TextView txtfilterstatus;
    private ProgressBar progressfilter;

    private BlinkHandler blinkHandler=null;//new BlinkHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        SabAppHelper.setHeaderTitle(getString(R.string.status),this);
        txtdate=(TextView)findViewById(R.id.txtdate);
        txttime=(TextView)findViewById(R.id.txttime);
        atmospherecontainer=(ViewGroup)findViewById(R.id.atmospherecontainer);
        txtatmosphere=(TextView)findViewById(R.id.txtatmosphere);
        txtprobe1temperature=(TextView)findViewById(R.id.txtprobe1temperature);
        txtpreheattemperature=(TextView)findViewById(R.id.txtpreheattemperature);
        txtfanspeed=(TextView)findViewById(R.id.txtfanspeed);
        txtfilterstatus=(TextView)findViewById(R.id.txtfilterstatus);
        progressfilter=(ProgressBar)findViewById(R.id.progressfilter);

        //blinkHandler.startBlinking();

        //UIHelper.makeLongToast("Atmosphere value: "+String.valueOf(AppCache.sabData.atmosphereStatus),AppCache.currentActivity);
        //UIHelper.makeLongToast("Preheat value: "+String.valueOf(AppCache.sabData.preheatTemperature),AppCache.currentActivity);
        UIHelper.makeLongToast("Server value: "+String.valueOf(AppCache.sabData.sendToServer),AppCache.currentActivity);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        blinkHandler.stopBlinking();
        try {
            AppCache.sabHelper.removeOnSabEventListener(sabEventListener);
        }catch(Exception ex){}
    }

    @Override
    public void onResume(){
        super.onResume();
        if (blinkHandler!=null){
            try {
                blinkHandler.stopBlinking();
            }catch(Exception ex){
                Log.e(TAG,"Exception in stopBlinking: "+ex.getMessage());
            }
            blinkHandler=null;
        }
        blinkHandler=new BlinkHandler(this);
        try {
            AppCache.sabHelper.addOnSabEventListener(sabEventListener);
            if (!AppCache.sabData.sendToServer) {
                refreshData(AppCache.sabData);
            }
        }catch(Exception ex){
            Log.e(TAG,"Exception: "+ex.getMessage());
        }
        try {
            blinkHandler.startBlinking();
        }catch(Exception ex){
            Log.e(TAG,"Exception in startBlinking: "+ex.getMessage());
        }
    }

    @Override
    public void onPause(){
        Log.d(TAG,"onPause");
        super.onPause();
        try {
            AppCache.sabHelper.removeOnSabEventListener(sabEventListener);
        }catch(Exception ex){}
        blinkHandler.stopBlinking();
    }

    private void addAtmosphereIcons(int resourceId,int count,boolean blink){
        LayoutInflater inflater=getLayoutInflater();
        for (int a=1;a<=count;a++){
            View vt=inflater.inflate(R.layout.icon_container,atmospherecontainer,false);
            ImageView img=(ImageView)vt.findViewById(R.id.imgicon);
            Bitmap bmp=UIHelper.decodeSampledBitmapFromDrawable(getResources(),resourceId,78,70);
            //img.setImageResource(resourceId);
            img.setImageBitmap(bmp);
            //if (a>1) {
                RelativeLayout.LayoutParams lps = (RelativeLayout.LayoutParams) img.getLayoutParams();
                //lps.leftMargin = 10;
            lps.rightMargin = 10;
                img.setLayoutParams(lps);
            //}
            atmospherecontainer.addView(vt);

            if (blink){
                blinkHandler.viewsToBlink.add(img);
            }
        }
    }

    private void refreshAtmosphere(SabData sData){
        //sData.atmosphereStatus=2;
        atmospherecontainer.removeAllViews();
        blinkHandler.viewsToBlink.clear();
        //UIHelper.makeLongToast("Atmosphere: "+String.valueOf(sData.atmosphereStatus),AppCache.currentActivity);
        if (sData.atmosphereStatus==1){
            txtatmosphere.setText("Very Humid");
            addAtmosphereIcons(R.drawable.balanced,3,false);
            addAtmosphereIcons(R.drawable.balanced_grey,2,true);
        }else if (sData.atmosphereStatus==2){
            txtatmosphere.setText("Humid");
            addAtmosphereIcons(R.drawable.balanced,4,false);
            addAtmosphereIcons(R.drawable.balanced_grey,1,true);
        }else if (sData.atmosphereStatus==3){
            txtatmosphere.setText("Balanced");
            addAtmosphereIcons(R.drawable.balanced,5,false);
            addAtmosphereIcons(R.drawable.balanced_happy,1,false);
        }else if (sData.atmosphereStatus==4){
            txtatmosphere.setText("Dry");
            addAtmosphereIcons(R.drawable.balanced_grey,1,true);
            addAtmosphereIcons(R.drawable.balanced,4,false);
        }else if (sData.atmosphereStatus==5){
            txtatmosphere.setText("Very dry");
            addAtmosphereIcons(R.drawable.balanced_grey,2,true);
            addAtmosphereIcons(R.drawable.balanced,3,false);
        }
    }

    private void refreshFilterStatus(SabData sData){

        //sData.filterStatus=6;

        ImageView imgfilter=(ImageView)findViewById(R.id.imgfilter);
        progressfilter.setProgress(0);

        if (sData.filterStatus<=0) {
            txtfilterstatus.setText("N/A");
            progressfilter.setProgress(0);

            txtfilterstatus.setVisibility(View.GONE);
            progressfilter.setVisibility(View.GONE);
            imgfilter.setVisibility(View.GONE);
        }else {
            txtfilterstatus.setVisibility(View.GONE);
            progressfilter.setVisibility(View.VISIBLE);
            imgfilter.setVisibility(View.VISIBLE);

            if (sData.filterStatus>0 && sData.filterStatus<=5){
                txtfilterstatus.setText("Good");
            }else if (sData.filterStatus>5 && sData.filterStatus<=10){
                txtfilterstatus.setText("Dusty");
            }else if (sData.filterStatus>10){
                txtfilterstatus.setText("V.Dusty");
            }
            progressfilter.setProgress(sData.filterStatus);
        }
    }

    private void refreshModes(SabData sData){
        ImageView imgmanual,imgauto,imgpause,imgboost,imgwinter,imgsummer,imgtest;
        imgmanual=(ImageView)findViewById(R.id.imgmanual);
        imgauto=(ImageView)findViewById(R.id.imgauto);
        imgpause=(ImageView)findViewById(R.id.imgpause);
        imgboost=(ImageView)findViewById(R.id.imgboost);
        imgwinter=(ImageView)findViewById(R.id.imgwinter);
        imgsummer=(ImageView)findViewById(R.id.imgsummer);
        imgtest=(ImageView)findViewById(R.id.imgtest);

        //sData.flagSet1=(byte)0xFE;

        if ((sData.flagSet1 & 0x01) > 0){
            imgauto.setVisibility(View.VISIBLE);
            imgmanual.setVisibility(View.GONE);
            //Bitmap bmp=UIHelper.decodeSampledBitmapFromDrawable(getResources(),R.drawable.auto,50,35);
            //imgauto.setImageResource(R.drawable.auto);
            //imgauto.setImageBitmap(bmp);
            //AppCache.currentSettings.fanSpeedManual=false;
        }else{
            imgauto.setVisibility(View.GONE);
            imgmanual.setVisibility(View.VISIBLE);
            //Bitmap bmp=UIHelper.decodeSampledBitmapFromDrawable(getResources(),R.drawable.manual,50,50);
            //imgauto.setImageResource(R.drawable.manual);
            //imgauto.setImageBitmap(bmp);
            AppCache.currentSettings.fanSpeedManual=true;
        }
        if ((sData.flagSet1 & 0x02) > 0){
            imgpause.setVisibility(View.VISIBLE);
            AppCache.currentSettings.pauseSab=true;
        }else{
            imgpause.setVisibility(View.GONE);
            AppCache.currentSettings.pauseSab=false;
        }
        if ((sData.flagSet1 & 0x04) > 0){
            imgboost.setVisibility(View.VISIBLE);
            AppCache.currentSettings.boostMode=true;
        }else{
            imgboost.setVisibility(View.GONE);
            AppCache.currentSettings.boostMode=false;
        }
        if ((sData.flagSet1 & 0x08) > 0){
            imgsummer.setVisibility(View.VISIBLE);
        }else{
            imgsummer.setVisibility(View.GONE);
        }
        if ((sData.flagSet1 & 0x10) > 0){
            imgwinter.setVisibility(View.VISIBLE);
        }else{
            imgwinter.setVisibility(View.GONE);
        }
        if ((sData.flagSet1 & 0x20) > 0){
            imgtest.setVisibility(View.VISIBLE);
        }else{
            imgtest.setVisibility(View.GONE);
        }
    }

    private void refreshPreheatTemperature(SabData sData){
        ImageView imgpreheat=(ImageView)findViewById(R.id.imgpreheattemperature);
        //sData.preheatTemperature=0;
        int temp=sData.preheatTemperature;
        Bitmap bmp=null;
        if (temp>0){
            //imgpreheat.setImageResource(R.drawable.preheat_temperature);
            bmp=UIHelper.decodeSampledBitmapFromDrawable(getResources(),R.drawable.preheat_temperature,72,60);
            temp+=10;
            txtpreheattemperature.setVisibility(View.VISIBLE);
        }else{
            //imgpreheat.setImageResource(R.drawable.preheat_temperature_disabled);
            bmp=UIHelper.decodeSampledBitmapFromDrawable(getResources(),R.drawable.preheat_temperature_disabled,72,60);
            txtpreheattemperature.setVisibility(View.GONE);
        }
        imgpreheat.setImageBitmap(bmp);
        txtpreheattemperature.setText(String.valueOf(temp)+"\u2103");
    }

    private void refreshFanSpeed(SabData sData){
        txtfanspeed.setText(String.valueOf(sData.fanSpeed));
        ImageView ivGif=(ImageView)findViewById(R.id.imgfan);
        if (sData.fanSpeed>0) {
            GifAnimationDrawable gif = null;
            try {
                gif = new GifAnimationDrawable(getResources().openRawResource(R.raw.spinning_fan));
                gif.setOneShot(false);
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (gif != null) {
                gif.start();
                ivGif.setImageDrawable(gif);
            }
            gif.setVisible(true, true);
        }else{
            Bitmap bmp=UIHelper.decodeSampledBitmapFromDrawable(getResources(),R.drawable.fan,64,64);
            //ivGif.setImageResource(R.drawable.fan);
            ivGif.setImageBitmap(bmp);
        }
    }

    private void refreshData(SabData sData){
        refreshAtmosphere(sData);
        refreshFilterStatus(sData);
        refreshModes(sData);
        refreshPreheatTemperature(sData);
        refreshFanSpeed(sData);
        txtprobe1temperature.setText(String.valueOf(sData.temperatureProbe1)+"\u2103");
    }

    /***** OnSabEventListener start ******/
    DefaultSabEventListener sabEventListener=new DefaultSabEventListener(){

        @Override
        public void onDeviceConnected(SabBluetoothHelper sab){
            runOnUiThread(new Runnable(){
                public void run(){
                    toggleSideMenuItems();
                }
            });
        }

        @Override
        public void onDeviceDisconnected(SabBluetoothHelper sab){
            UIHelper.makeLongToast(getString(R.string.SAB_disconnected),AppCache.currentActivity,false);
            if (sab!=null){
                runOnUiThread(new Runnable(){
                    public void run(){
                        toggleSideMenuItems();
                    }
                });
            }
        }

        @Override
        public void onSabDataReceived(SabBluetoothHelper sab, SabData sData) {
            UIHelper.makeLongToast("STAT: Sab data received: "+String.valueOf(sData.temperatureProbe1),AppCache.currentActivity);
            runOnUiThread(new Runnable(){
                public void run(){
                    if (!AppCache.sabData.sendToServer) {
                        refreshData(AppCache.sabData);
                    }
                    toggleSideMenuItems();
                }
            });

        }

        @Override
        public void onSabCommandResponseReceived(SabBluetoothHelper sab, String lastCommandExecuted, String response) {
            //UIHelper.makeLongToast("STAT: Response for "+lastCommandExecuted+": "+response,AppCache.currentActivity);
            runOnUiThread(new Runnable(){
                public void run(){
                    if (!AppCache.sabData.sendToServer) {
                        refreshData(AppCache.sabData);
                    }
                    toggleSideMenuItems();
                }
            });
        }
    };
}
