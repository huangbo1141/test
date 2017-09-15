package tepia.smartcycle;

import android.media.audiofx.BassBoost;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.usmani.android.UIHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class SettingsActivity extends CommonActivity {

    private static final String TAG= SettingsActivity.class.getName();

    private boolean applyingCommands=false;
    private int commandIndex=0;
    private List<String> commands=new Vector<String>();

    private SeekBar spinnerfanspeed;
    private SeekBar spinnerpreheattemperature;
    private SeekBar spinnerpausesab;
    private Switch btnfanspeed;
    private Switch btnpreheattemperature;
    private Switch btnpausesab;
    private Switch btnboost;
    private Switch btnsynctime;

    private TextView txtfanspeed,txtpreheattemperature,txtpausesab;

    private boolean fanSpeedManual=AppCache.currentSettings.fanSpeedManual;
    private boolean boostMode=AppCache.currentSettings.boostMode;
    private boolean preheatTemperature=AppCache.currentSettings.preheatTemperature;
    private boolean pauseSab=AppCache.currentSettings.pauseSab;

    private boolean askedForInfo=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SabAppHelper.setHeaderTitle(getString(R.string.settings),this);
        btnfanspeed=(Switch)findViewById(R.id.btnfanspeed);
        spinnerfanspeed=(SeekBar)findViewById(R.id.spinnerfanspeed);
        txtfanspeed=(TextView)findViewById(R.id.txtfanspeed);

        btnpreheattemperature=(Switch)findViewById(R.id.btnpreheattemperature);
        spinnerpreheattemperature=(SeekBar)findViewById(R.id.spinnerpreheattemperature);
        txtpreheattemperature=(TextView)findViewById(R.id.txtpreheattemperature);

        btnpausesab=(Switch)findViewById(R.id.btnpausesab);
        spinnerpausesab=(SeekBar)findViewById(R.id.spinnerpausesab);
        txtpausesab=(TextView)findViewById(R.id.txtpausesab);

        btnboost=(Switch)findViewById(R.id.btnboost);
        btnboost.setChecked(boostMode);
        //UIHelper.makeLongToast("Boost mode set",AppCache.currentActivity);
        btnboost.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //UIHelper.makeLongToast("Boost mode checked change",AppCache.currentActivity);
                boostMode=b;
            }
        });

        btnsynctime=(Switch)findViewById(R.id.btnsynctime);

        //UIHelper.makeLongToast("Setting fan progress",AppCache.currentActivity);
        if (AppCache.currentSettings.fanSpeedValue<AppCache.currentSettings.minimumFanValue){
            spinnerfanspeed.setProgress(AppCache.currentSettings.minimumFanValue);
        }else {
            spinnerfanspeed.setProgress(AppCache.currentSettings.fanSpeedValue);
        }
        //UIHelper.makeLongToast("Setting fan speed text",AppCache.currentActivity);
        txtfanspeed.setText(String.valueOf(spinnerfanspeed.getProgress()));
        btnfanspeed.setChecked(!fanSpeedManual);
        //UIHelper.makeLongToast("Fan speed checked",AppCache.currentActivity);
        btnfanspeed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //UIHelper.makeLongToast("Fan speed checked change",AppCache.currentActivity);
                fanSpeedManual=!b;
                refreshFanSpeedUi();
                //UIHelper.makeLongToast("Fan UI refreshed after checked change",AppCache.currentActivity);
            }
        });
        spinnerfanspeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //UIHelper.makeLongToast("Fan progress changed",AppCache.currentActivity);
                if (spinnerfanspeed.getProgress()<AppCache.currentSettings.minimumFanValue){
                    spinnerfanspeed.setProgress(AppCache.currentSettings.minimumFanValue);
                }else {
                    txtfanspeed.setText(String.valueOf(spinnerfanspeed.getProgress()));
                }
                //UIHelper.makeLongToast("Fan text set",AppCache.currentActivity);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        refreshFanSpeedUi();
        //UIHelper.makeLongToast("Fan UI refreshed",AppCache.currentActivity);

        if (AppCache.sabData.preheatTemperature==0){
            spinnerpreheattemperature.setProgress(18-11);
        }else {
            spinnerpreheattemperature.setProgress(AppCache.sabData.preheatTemperature-11);
        }
        //UIHelper.makeLongToast("Preheat progress set",AppCache.currentActivity);
        txtpreheattemperature.setText(String.valueOf(spinnerpreheattemperature.getProgress()+11));
        //UIHelper.makeLongToast("Preheat text set",AppCache.currentActivity);
        btnpreheattemperature.setChecked(AppCache.currentSettings.preheatTemperature);
        //UIHelper.makeLongToast("Preheat checked",AppCache.currentActivity);
        btnpreheattemperature.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //UIHelper.makeLongToast("Preheat checked change",AppCache.currentActivity);
                preheatTemperature=b;
                refreshPreheatTemperatureUi();
                //UIHelper.makeLongToast("Preheat UI refreshed after check change",AppCache.currentActivity);
            }
        });
        spinnerpreheattemperature.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //UIHelper.makeLongToast("Preheat progress change",AppCache.currentActivity);
                txtpreheattemperature.setText(String.valueOf(spinnerpreheattemperature.getProgress()+11));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        refreshPreheatTemperatureUi();
        //UIHelper.makeLongToast("Preheat UI refreshed",AppCache.currentActivity);

        spinnerpausesab.setProgress(AppCache.currentSettings.pauseSabValue);
        //UIHelper.makeLongToast("Pause progress set",AppCache.currentActivity);
        txtpausesab.setText(String.valueOf(spinnerpausesab.getProgress()));
        //UIHelper.makeLongToast("Pause text set",AppCache.currentActivity);
        btnpausesab.setChecked(AppCache.currentSettings.pauseSab);
        //UIHelper.makeLongToast("Pause checked",AppCache.currentActivity);
        btnpausesab.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //UIHelper.makeLongToast("Pause checked change",AppCache.currentActivity);
                pauseSab=b;
                refreshPauseUi();
                //UIHelper.makeLongToast("Pause UI refreshed after check change",AppCache.currentActivity);
            }
        });
        spinnerpausesab.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //UIHelper.makeLongToast("Pause progress changed",AppCache.currentActivity);
                txtpausesab.setText(String.valueOf(spinnerpausesab.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        refreshPauseUi();
        //UIHelper.makeLongToast("Pause UI refreshed",AppCache.currentActivity);

        TextView txtsabname=(TextView)findViewById(R.id.txtsabname);
        TextView txtfirmware=(TextView)findViewById(R.id.txtfirmware);
        TextView txtidentifier=(TextView)findViewById(R.id.txtidentifier);
        TextView txtdate=(TextView)findViewById(R.id.txtdate);
        TextView txttime=(TextView)findViewById(R.id.txttime);

        txtsabname.setText(AppCache.sabData.sabName);
        txtfirmware.setText(AppCache.sabData.firmwareVersion);
        txtidentifier.setText(AppCache.sabData.identifier);
        txtdate.setText(getDisplayableDate(AppCache.sabData.date));
        txttime.setText(getDisplayableTime(AppCache.sabData.time));

        commands.add("#GD#" + AppCache.currentCommandCode + "@");
        commands.add("#GT#" + AppCache.currentCommandCode + "@");
        commands.add("#GS#" + AppCache.currentCommandCode + "@");
        commands.add("#GH#" + AppCache.currentCommandCode + "@");

        AppCache.isApplyingSettings=true;
        applyingCommands=false;
    }

    private void refreshTheValues(){
        runOnUiThread(new Runnable(){
            public void run() {
                try {
                    TextView txtdate = (TextView) findViewById(R.id.txtdate);
                    TextView txttime = (TextView) findViewById(R.id.txttime);
                    txtdate.setText(getDisplayableDate(AppCache.sabData.date));
                    txttime.setText(getDisplayableTime(AppCache.sabData.time));

                    if (AppCache.sabData.preheatTemperature == 0) {
                        //spinnerpreheattemperature.setProgress(18 - 11);
                        spinnerpreheattemperature.setProgress(0);
                        btnpreheattemperature.setChecked(false);
                    } else {
                        spinnerpreheattemperature.setProgress(AppCache.sabData.preheatTemperature - 11);
                        btnpreheattemperature.setChecked(true);
                    }
                    txtpreheattemperature.setText(String.valueOf(spinnerpreheattemperature.getProgress() + 11));

                    if (AppCache.currentSettings.fanSpeedValue<AppCache.currentSettings.minimumFanValue){
                        spinnerfanspeed.setProgress(AppCache.currentSettings.minimumFanValue);
                    }else {
                        spinnerfanspeed.setProgress(AppCache.currentSettings.fanSpeedValue);
                    }
                    txtfanspeed.setText(String.valueOf(spinnerfanspeed.getProgress()));
                }catch(Exception ex){
                    UIHelper.makeLongToast("Error in refreshTheValues: "+ex.getMessage(),AppCache.currentActivity);
                }
            }
        });
    }

    private String getDisplayableTime(String date){
        String dtStr="";
        if (date.length()>1){
            dtStr=date.substring(0,2);
        }
        if (date.length()>2){
            dtStr+=":";
            dtStr+=date.substring(2,4);
        }
        if (date.length()>3){
            dtStr+=":";
            dtStr+=date.substring(4,6);
        }
        return dtStr;
    }

    private String getDisplayableDate(String date){
        String dtStr="";
        if (date.length()>1){
            dtStr=date.substring(0,2);
        }
        if (date.length()>2){
            dtStr+="/";
            dtStr+=date.substring(2,4);
        }
        if (date.length()>3){
            dtStr+="/";
            dtStr+=date.substring(4,6);
        }
        return dtStr;
    }

    private void refreshFanSpeedUi(){
        if (fanSpeedManual){
            spinnerfanspeed.setEnabled(true);
            txtfanspeed.setVisibility(View.VISIBLE);
        }else{
            spinnerfanspeed.setEnabled(false);
            txtfanspeed.setVisibility(View.INVISIBLE);
        }
    }

    private void refreshPreheatTemperatureUi(){
        if (preheatTemperature){
            spinnerpreheattemperature.setEnabled(true);
            txtpreheattemperature.setVisibility(View.VISIBLE);
        }else{
            spinnerpreheattemperature.setEnabled(false);
            txtpreheattemperature.setVisibility(View.INVISIBLE);
        }
    }

    private void refreshPauseUi(){
        if (pauseSab){
            spinnerpausesab.setEnabled(true);
            txtpausesab.setVisibility(View.VISIBLE);
        }else{
            spinnerpausesab.setEnabled(false);
            txtpausesab.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        AppCache.isApplyingSettings=false;
        AppCache.sabHelper.removeOnSabEventListener(sabEventListener);
    }

    @Override
    public void onResume(){
        super.onResume();
        AppCache.isApplyingSettings=true;
        AppCache.sabHelper.addOnSabEventListener(sabEventListener);
        //askedForInfo=true;
        if (!askedForInfo){
            Thread thr=new Thread(){
                public void run(){
                    try{
                        Thread.sleep(1000);
                    }catch(Exception ex){}
                    try {
                        applyCommand();
                    }catch(Exception ex){
                        UIHelper.makeLongToast("Exception: "+ex.getMessage(),AppCache.currentActivity);
                    }
                }
            };
            thr.start();
            askedForInfo=true;
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        AppCache.isApplyingSettings=false;
        AppCache.sabHelper.removeOnSabEventListener(sabEventListener);
    }

    public void applyClick(View v){
        Log.d(TAG,"applyClick");
        commands.clear();
        String str="";
        if (!fanSpeedManual){
            str="#SA#"+AppCache.currentCommandCode+"@";
            AppCache.currentSettings.fanSpeedManual=false;
        }else{
            int fanSpeedVal=spinnerfanspeed.getProgress();
            str="#SM#"+AppCache.currentCommandCode+"#"+String.valueOf(fanSpeedVal)+"@";
            AppCache.currentSettings.fanSpeedValue=fanSpeedVal;
            AppCache.currentSettings.fanSpeedManual=true;
        }
        commands.add(str);
        AppCache.currentSettings.preheatTemperature=preheatTemperature;
        if (preheatTemperature){
            int preheatTempVal=spinnerpreheattemperature.getProgress();
            preheatTempVal+=11;
            str="#SH#"+AppCache.currentCommandCode+"#"+String.valueOf(preheatTempVal)+"@";
            commands.add(str);
        }else{
            int preheatTempVal=0;
            str="#SH#"+AppCache.currentCommandCode+"#"+String.valueOf(preheatTempVal)+"@";
            commands.add(str);
        }
        AppCache.currentSettings.pauseSab=pauseSab;
        if (pauseSab){
            int pauseVal=spinnerpausesab.getProgress();
            str="#SP#"+AppCache.currentCommandCode+"#"+String.valueOf(pauseVal)+"@";
            commands.add(str);
        }
        AppCache.currentSettings.boostMode=boostMode;
        if (boostMode){
            str="#SB#"+AppCache.currentCommandCode+"@";
            commands.add(str);
        }

        if (btnsynctime.isChecked()){
            str=SabBluetoothHelper.getSDForCurrent(AppCache.currentCommandCode);
            commands.add(str);

            str=SabBluetoothHelper.getSTForCurrent(AppCache.currentCommandCode);
            commands.add(str);
        }

        for (int a=0;a<commands.size();a++){
            Log.d(TAG,commands.get(a));
        }

        applyingCommands=true;
        commandIndex=0;

        Thread thr=new Thread(){
            public void run(){
                applyCommand();
            }
        };
        thr.start();
    }

    private void applyCommand(){
        if (commandIndex<commands.size()){
            String str=commands.get(commandIndex);
            if (!AppCache.sabHelper.writeCommand(str)){
                UIHelper.makeLongToast("SETT: Error in writeCommand "+str,AppCache.currentActivity);
            }
        }else{
            if (applyingCommands) {
                UIHelper.msbox("Applied", "All commands applied", AppCache.currentActivity);
            }
        }
    }


    /***** OnSabEventListener start ******/

    DefaultSabEventListener sabEventListener=new DefaultSabEventListener(){
        @Override
        public void onSabDataReceived(SabBluetoothHelper sab, SabData sData) {
            AppCache.sabData.setData(sData);
        }

        @Override
        public void onSabCommandResponseReceived(SabBluetoothHelper sab, String lastCommandExecuted, String response) {
            lastCommandExecuted = SabBluetoothHelper.getCommandFromResponse(response);
            if (lastCommandExecuted != null) {
                if (AppCache.isApplyingSettings) {
                    UIHelper.makeLongToast("SETT: Response for "+lastCommandExecuted+": "+response,AppCache.currentActivity);
                    if (lastCommandExecuted.compareTo("GD") == 0) {
                        if (SabBluetoothHelper.isResponseOk(response)) {
                            String date = SabBluetoothHelper.getResponseParameter(response, 1);
                            AppCache.sabData.date = date;
                            refreshTheValues();
                        }
                    } else if (lastCommandExecuted.compareTo("GT") == 0) {
                        if (SabBluetoothHelper.isResponseOk(response)) {
                            String date = SabBluetoothHelper.getResponseParameter(response, 1);
                            AppCache.sabData.time = date;
                            refreshTheValues();
                        }
                    } else if (lastCommandExecuted.compareTo("GH") == 0) {
                        if (SabBluetoothHelper.isResponseOk(response)) {
                            String str=SabBluetoothHelper.getResponseParameter(response,1);
                            try{
                                AppCache.sabData.preheatTemperature=Integer.valueOf(str);
                                refreshTheValues();
                            }catch(Exception ex){}
                        }
                    } else if (lastCommandExecuted.compareTo("GS") == 0) {
                        if (SabBluetoothHelper.isResponseOk(response)) {
                            String str=SabBluetoothHelper.getResponseParameter(response,2);
                            try{
                                AppCache.currentSettings.minimumFanValue=Integer.valueOf(str);
                            }catch(Exception ex){}
                            str=SabBluetoothHelper.getResponseParameter(response,1);
                            try{
                                AppCache.currentSettings.fanSpeedValue=Integer.valueOf(str);
                                refreshTheValues();
                            }catch(Exception ex){}
                            refreshTheValues();
                        }
                    }
                    commandIndex++;
                    applyCommand();
                }
            }
        }

        @Override
        public void onError(SabBluetoothHelper sab, int error) {
            UIHelper.makeLongToast("SETT: onError: "+String.valueOf(error),AppCache.currentActivity);
            //commandIndex++;
            //applyCommand();
        }

        @Override
        public void onCommandExecuted(SabBluetoothHelper sab, String command) {
            UIHelper.makeLongToast("SETT: Command executed: "+command,AppCache.currentActivity);
        }
    };
}
