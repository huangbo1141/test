package tepia.smartcycle;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.usmani.android.UIHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class InstallerActivity extends CommonActivity {

    private static final String TAG= InstallerActivity.class.getName();

    private boolean applyingCommands=false;
    private int commandIndex=0;
    private List<String> commands=new Vector<String>();

    private SeekBar spinnerfanspeed,spinnersabnum;
    private EditText txtusername,txtpassword,txtcontractname;
    private TextView txtfanspeed,txtsabnum;

    private int minimumFan=0;
    private int maximumFan=15;
    private int minimumSabNum=1;
    private int maximumSabNum=10;

    private LocalStorage lStorage=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installer);
        lStorage=new LocalStorage(getApplicationContext());
        SabAppHelper.setHeaderTitle(getString(R.string.setup),this);
        spinnerfanspeed=(SeekBar)findViewById(R.id.spinnerfanspeed);
        spinnersabnum=(SeekBar)findViewById(R.id.spinnersabnum);
        txtusername=(EditText)findViewById(R.id.txtusername);
        txtpassword=(EditText)findViewById(R.id.txtpassword);
        txtcontractname=(EditText)findViewById(R.id.txtcontractname);
        txtfanspeed=(TextView)findViewById(R.id.txtfanspeed);
        txtsabnum=(TextView)findViewById(R.id.txtsabnum);

        spinnerfanspeed.setMax(maximumFan);
        spinnersabnum.setMax(maximumSabNum);

        if (AppCache.currentSetupData.fanSpeed<minimumFan){
            spinnerfanspeed.setProgress(minimumFan);
        }else {
            spinnerfanspeed.setProgress(AppCache.currentSetupData.fanSpeed);
        }
        txtfanspeed.setText(String.valueOf(spinnerfanspeed.getProgress()));
        spinnerfanspeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (spinnerfanspeed.getProgress()<minimumFan){
                    spinnerfanspeed.setProgress(minimumFan);
                }else {
                    txtfanspeed.setText(String.valueOf(spinnerfanspeed.getProgress()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (AppCache.currentSetupData.sabNumber<minimumSabNum){
            spinnersabnum.setProgress(minimumSabNum);
        }else {
            spinnersabnum.setProgress(AppCache.currentSetupData.sabNumber);
        }
        txtsabnum.setText(String.valueOf(spinnersabnum.getProgress()));
        spinnersabnum.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (spinnersabnum.getProgress()<minimumSabNum){
                    spinnersabnum.setProgress(minimumSabNum);
                }else {
                    txtsabnum.setText(String.valueOf(spinnersabnum.getProgress()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        txtusername.setText(AppCache.currentSetupData.username);
        txtpassword.setText(AppCache.currentSetupData.password);
        //txtcontractname.setText(AppCache.currentSetupData.contractName);

        String dt=lStorage.getInstallerDate();
        if (SabBluetoothHelper.isInstallerAllowed(dt)) {
            txtusername.setEnabled(false);
            txtpassword.setEnabled(false);
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
        str="#SL#"+AppCache.currentCommandCode+"#"+String.valueOf(spinnerfanspeed.getProgress())+"@";
        commands.add(str);

        str=SabBluetoothHelper.getSDForCurrent(AppCache.currentCommandCode);
        commands.add(str);

        str=SabBluetoothHelper.getSTForCurrent(AppCache.currentCommandCode);
        commands.add(str);

        str="#SN#"+AppCache.currentCommandCode+"#SAB"+String.valueOf(spinnersabnum.getProgress())+"@";
        commands.add(str);

        for (int a=0;a<commands.size();a++){
            Log.d(TAG,commands.get(a));
        }

        applyingCommands=true;
        commandIndex=0;

        AppCache.currentSetupData.username=txtusername.getText().toString();
        AppCache.currentSetupData.password=txtpassword.getText().toString();
        AppCache.currentSetupData.contractName=txtcontractname.getText().toString();
        AppCache.currentSetupData.sabNumber=spinnersabnum.getProgress();
        AppCache.currentSetupData.fanSpeed=spinnerfanspeed.getProgress();

        lStorage.saveSetupData(AppCache.currentSetupData);

        Thread thr=new Thread(){
            public void run(){
                String dt=lStorage.getInstallerDate();
                if (SabBluetoothHelper.isInstallerAllowed(dt)) {
                    sendQCommand();
                    applyCommand();
                }else{
                    sendNACommand();
                }
            }
        };
        thr.start();
    }

    private String appendRandomToCommand(String qCommand){
        return qCommand+"||"+String.valueOf(new java.util.Date().getTime());
    }

    private void sendNACommand(){
        Log.d(TAG,"sendNACommand");
        String qCommand="";
        try{
            qCommand="#NA#"+String.valueOf(AppCache.currentSetupData.code)+"#";
            qCommand+=AppCache.currentSetupData.username+"#";
            qCommand+=AppCache.currentSetupData.password+"#";
            qCommand+="SAB"+String.valueOf(AppCache.currentSetupData.sabNumber)+"#";
            UIHelper.makeLongToast("Sending to server: "+qCommand,AppCache.currentActivity);
            String resp=SabWebServer.sendQCommand(qCommand);
            Log.d(TAG,"Response: "+resp);
            UIHelper.makeLongToast("Server response: "+resp,AppCache.currentActivity);
            if (!SabBluetoothHelper.isResponseOk(resp)){
                lStorage.addCommandToQueue(appendRandomToCommand(qCommand));
                UIHelper.makeLongToast("Added in queue: "+qCommand,AppCache.currentActivity);
            }
        }catch(Exception ex){
            Log.e(TAG,"Error: "+ex.getMessage());
            UIHelper.makeLongToast("Error in server: "+ex.getMessage(),AppCache.currentActivity);
            lStorage.addCommandToQueue(appendRandomToCommand(qCommand));
            UIHelper.makeLongToast("Added in queue: "+qCommand,AppCache.currentActivity);
        }
    }

    private void sendQCommand(){
        Log.d(TAG,"sendQCommand");
        String qCommand="";
        try{
            qCommand="#SP#"+String.valueOf(AppCache.currentSetupData.code)+"#";
            qCommand+=AppCache.currentSetupData.username+"#";
            qCommand+="SAB"+String.valueOf(AppCache.currentSetupData.sabNumber)+"#";
            qCommand+=AppCache.currentSetupData.contractName+"#";
            qCommand+=AppCache.sabData.identifier+"#";
            //qCommand+="F0D1552D456A"+"#";
            qCommand+=AppCache.sabData.firmwareVersion+"#";
            qCommand+=String.valueOf(spinnerfanspeed.getProgress())+"#";
            qCommand+="1.0#";
            qCommand+=SabBluetoothHelper.getCurrentDate()+"#";
            qCommand+=SabBluetoothHelper.getCurrentTime()+"@";
            UIHelper.makeLongToast("Sending to server: "+qCommand,AppCache.currentActivity);
            String resp=SabWebServer.sendQCommand(qCommand);
            Log.d(TAG,"Response: "+resp);
            UIHelper.makeLongToast("Server response: "+resp,AppCache.currentActivity);
            /*if (resp.startsWith("#OK")){
                //save to local storage
                LocalStorage ls=new LocalStorage(getApplicationContext());
                ls.saveSetupData(AppCache.currentSetupData);
            }*/
            if (!SabBluetoothHelper.isResponseOk(resp)){
                lStorage.addCommandToQueue(appendRandomToCommand(qCommand));
                UIHelper.makeLongToast("Added in queue: "+qCommand,AppCache.currentActivity);
            }
        }catch(Exception ex){
            Log.e(TAG,"Error: "+ex.getMessage());
            UIHelper.makeLongToast("Error in server: "+ex.getMessage(),AppCache.currentActivity);
            lStorage.addCommandToQueue(appendRandomToCommand(qCommand));
            UIHelper.makeLongToast("Added in queue: "+qCommand,AppCache.currentActivity);
        }
    }

    private void applyCommand(){
        if (!AppCache.sabHelper.isDeviceConnected()){
            UIHelper.makeLongToast("Not connected to bluetooth",AppCache.currentActivity);
            return;
        }
        if (commandIndex<commands.size()){
            String str=commands.get(commandIndex);
            if (!AppCache.sabHelper.writeCommand(str)){
                UIHelper.makeLongToast("SETT: Error in writeCommand "+str,AppCache.currentActivity);
            }
        }else{
            UIHelper.msbox(getString(R.string.applied),getString(R.string.all_applied),AppCache.currentActivity);
            //sendQCommand();
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
            UIHelper.makeLongToast("SETUP: onSabCommandResponseReceived: "+response,AppCache.currentActivity);
            commandIndex++;
            try{
                Thread.sleep(500);
            }catch(Exception ex){}
            applyCommand();
        }

        @Override
        public void onError(SabBluetoothHelper sab, int error) {
            commandIndex++;
            applyCommand();
        }

        @Override
        public void onCommandExecuted(SabBluetoothHelper sab, String command) {
            UIHelper.makeLongToast("SETT: Command executed: "+command,AppCache.currentActivity);
        }
    };
}
