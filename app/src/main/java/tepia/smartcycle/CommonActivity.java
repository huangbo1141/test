package tepia.smartcycle;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.usmani.android.UIHelper;

public class CommonActivity extends AppCompatActivity {

    private static final String TAG=CommonActivity.class.getName();

    protected ProgressDialog progressBar;

    protected DrawerLayout drawer;
    protected LinearLayout layoutSlider;
    protected LinearLayout slideMenuContainer;

    protected LocalStorage localStorage=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCache.currentActivity=this;
        localStorage=new LocalStorage(getApplicationContext());
        initializeProgressBar();
        getSupportActionBar().hide();

        /*Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT>=21) {
            // finally change the color
            window.setStatusBarColor(getResources().getColor(R.color.colorHeaderBar));
        }*/
    }

    @Override
    protected void onResume(){
        super.onResume();
        AppCache.currentActivity=this;
        slideMenuContainer=(LinearLayout)findViewById(R.id.slideMenuContainer);
        drawer=(DrawerLayout)findViewById(R.id.drawer_layout);
        layoutSlider=(LinearLayout)findViewById(R.id.layoutSlider);
        if (drawer!=null && layoutSlider!=null) {
            toggleSideMenuItems();
        }
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

    private void initializeProgressBar(){
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setMessage(getString(R.string.please_wait));
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setIndeterminate(true);
    }

    public void backClick(View v){
        Log.d(TAG, "backClick");
        finish();
    }

    public void toggleSideMenuItems(){
        if (layoutSlider==null){
            return;
        }
        View home=layoutSlider.findViewById(R.id.slideHome);
        View settings=layoutSlider.findViewById(R.id.slideSettings);
        View setup=layoutSlider.findViewById(R.id.slideSetup);

        if (home!=null && settings!=null && setup!=null) {

            TextView txtslidestatus=(TextView)home.findViewById(R.id.txtslidestatus);
            ImageView imgslidestatus=(ImageView)home.findViewById(R.id.slideIconHome);
            TextView txtslidesettings=(TextView)settings.findViewById(R.id.txtslidesettings);
            ImageView imgslidesettings=(ImageView)settings.findViewById(R.id.slideIconSettings);

            if (AppCache.sabHelper == null || !AppCache.sabHelper.isDeviceConnected()) {
                //home.setVisibility(View.VISIBLE);
                //settings.setVisibility(View.VISIBLE);
                txtslidestatus.setTextColor(Color.GRAY);
                txtslidesettings.setTextColor(Color.GRAY);
                imgslidestatus.setImageResource(R.drawable.dashboard_icon_grey);
                imgslidesettings.setImageResource(R.drawable.settings_icon_grey);
            } else {
                //home.setVisibility(View.GONE);
                //settings.setVisibility(View.GONE);
                txtslidestatus.setTextColor(Color.BLACK);
                txtslidesettings.setTextColor(Color.BLACK);
                imgslidestatus.setImageResource(R.drawable.dashboard_icon);
                imgslidesettings.setImageResource(R.drawable.settings_icon);
            }

            if (AppCache.showStatus){
                setup.setVisibility(View.VISIBLE);
            }else {
                setup.setVisibility(View.GONE);
            }
        }
    }

    private static final int FINAL_SETUP_COUNT=15;

    /*
    e.g.
5 times help
4 status
3 store
2 settings
1 connection
?
     */

    private void processLastSequence(int type){
        //type 1=status,2=settings,3=connection,4=store,5=help,6=setup
        int lastSequence=localStorage.getLastCount();
        Log.d(TAG,"Current sequence: "+String.valueOf(lastSequence));
        if (lastSequence<5 && type==5){
            Log.d(TAG,"Ok for help");
            lastSequence+=1;
        }else if (lastSequence>=5 && lastSequence<9 && type==1){
            Log.d(TAG,"Ok for status");
            lastSequence+=1;
        }else if (lastSequence>=9 && lastSequence<12 && type==4){
            Log.d(TAG,"Ok for store");
            lastSequence+=1;
        }else if (lastSequence>=12 && lastSequence<14 && type==2){
            Log.d(TAG,"Ok for settings");
            lastSequence+=1;
        }else if (lastSequence>=14 && lastSequence<15 && type==3){
            Log.d(TAG,"Ok for connection");
            lastSequence+=1;
        }else{
            lastSequence=0;
        }
        Log.d(TAG,"Next sequence: "+String.valueOf(lastSequence));
        localStorage.setLastCount(lastSequence);
        if (lastSequence==FINAL_SETUP_COUNT){
            AppCache.showStatus=true;
            localStorage.setShowStatus(true);
            /*try{
                Thread.sleep(800);
            }catch(Exception ex){}*/
        }
    }

    public void onSlideConnectionClick(View v){
        Log.d(TAG, "onSlideConnectionClick");
        toggleDrawer();
        processLastSequence(3);
        Intent i=new Intent(this,BluetoothDevicesActivity.class);
        startActivity(i);
    }

    public void onSlideStatusClick(View v){
        Log.d(TAG, "onSlideStatusClick");
        toggleDrawer();
        processLastSequence(1);
        if (AppCache.sabHelper == null || !AppCache.sabHelper.isDeviceConnected()) {
            UIHelper.makeLongToast(getString(R.string.connect_to_SAB_first),this,false);
            return;
        }
        Intent i=new Intent(this,StatusActivity.class);
        startActivity(i);
    }

    public void onSlideSettingsClick(View v){
        Log.d(TAG, "onSlideSettingsClick");
        toggleDrawer();
        processLastSequence(2);
        if (AppCache.sabHelper == null || !AppCache.sabHelper.isDeviceConnected()) {
            UIHelper.makeLongToast(getString(R.string.connect_to_SAB_first),this,false);
            return;
        }
        Intent i=new Intent(this,SettingsActivity.class);
        startActivity(i);
    }

    public void onSlideStoreClick(View v){
        Log.d(TAG, "onSlideStoreClick");
        toggleDrawer();
        processLastSequence(4);
        Intent i=new Intent(this,StoreActivity.class);
        startActivity(i);
    }

    public void onSlideHelpClick(View v){
        Log.d(TAG, "onSlideHelpClick");
        toggleDrawer();
        processLastSequence(5);
        Intent i=new Intent(this,HelpActivity.class);
        startActivity(i);
    }

    public void onSlideSetupClick(View v){
        Log.d(TAG, "onSlideSetupClick");
        toggleDrawer();
        Intent i=new Intent(this,InstallerActivity.class);
        startActivity(i);
    }

    /*public void doLogout(){
        Log.d(TAG, "doLogout");
        String messageToAsk="Are you sure you want to Logout?\n";
        UIHelper.confirmBox("", messageToAsk, "Logout", "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LocalStorage ls=new LocalStorage(getApplicationContext());
                ls.clear();
                Intent i=new Intent(CommonActivity.this,MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
        }, null, this);
    }*/

    public void navMenuClick(View v){
        Log.d(TAG, "navMenuClick");
        toggleDrawer();
    }

    private void toggleDrawer(){
        Log.d(TAG, "toggleDrawer");
        if (drawer!=null){
            if (drawer.isDrawerOpen(layoutSlider)){
                drawer.closeDrawer(layoutSlider);
            }else{
                drawer.openDrawer(layoutSlider);
            }
        }
    }
}
