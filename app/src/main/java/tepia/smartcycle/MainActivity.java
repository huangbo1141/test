package tepia.smartcycle;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;

import com.usmani.android.HttpUtil;
import com.usmani.android.UIHelper;

import java.util.List;

public class MainActivity extends CommonActivity {

    private static final String TAG=MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCache.currentActivity=this;

        UIHelper.initializeBitmapCache(getApplicationContext(),getSupportFragmentManager());
        setContentView(R.layout.activity_loggedin_main);

        //AppCache.sabData=SabBluetoothHelper.processSabData(AppCache.getDummyBytes());
        //AppCache.setDummyData();

        WebView webview=(WebView)findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl("file:///android_asset/splash.html");

        //localStorage.setLastCount(0);
        //localStorage.setShowStatus(false);

        AppCache.showStatus=localStorage.showStatus();

        Thread thr = new Thread() {
            public void run() {

                //SabReadThread.runDDCommand();

                LocalStorage ls=new LocalStorage(getApplicationContext());
                SetupData sData=ls.getSetupData();
                if (sData!=null){
                    AppCache.currentSetupData=sData;
                    String qCommand="#IR#";
                    qCommand+=String.valueOf(sData.code)+"#";
                    qCommand+=sData.username+"#";
                    qCommand+=sData.password+"@";
                    Log.d(TAG,"Command: "+qCommand);
                    UIHelper.makeLongToast("Sending to server: "+qCommand, AppCache.currentActivity);
                    try{
                        String resp=SabWebServer.sendQCommand(qCommand);
                        Log.d(TAG,"Response: "+resp);
                        UIHelper.makeLongToast("Server response: "+resp,AppCache.currentActivity);
                        if (SabBluetoothHelper.isResponseOk(resp)){
                            String dt=SabBluetoothHelper.getResponseParameter(resp,1);
                            Log.d(TAG,"Date returned: "+dt);
                            ls.setInstallerDate(dt);
                        }
                    }catch(Exception ex){
                        Log.e(TAG,"Error: "+ex.getMessage());
                        UIHelper.makeLongToast("Server error: "+ex.getMessage(),AppCache.currentActivity);
                    }

                }else {
                    try {
                        Thread.sleep(3000);
                    } catch (Exception ex) {
                    }
                }

                if (AppCache.readThread!=null && AppCache.readThread.cwSent){
                    AppCache.readThread.cwSent=false;
                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        Intent i = new Intent(MainActivity.this, BluetoothDevicesActivity.class);
                        startActivity(i);
                        finish();
                    }
                });
            }
        };
        thr.start();

        SabReadThread.startServerQueueThread();

        //SabReadThread srt=new SabReadThread();
        //srt.startReading();
    }

    @Override
    protected void onPause(){
        super.onPause();
        AppCache.currentActivity=null;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        AppCache.currentActivity=null;
    }
}
