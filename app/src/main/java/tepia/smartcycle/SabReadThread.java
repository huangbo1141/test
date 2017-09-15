package tepia.smartcycle;

import android.util.Log;

import com.usmani.android.UIHelper;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * Created by Rameez Usmani on 6/5/2017.
 */

public class SabReadThread
extends Thread{

    private static final String TAG=SabReadThread.class.getName();

    enum SabReadThreadState {READING,READ_DONE,WRITING_GD,WRITING_GT,WRITING_COMMANDS};

    public static long SAB_DATA_READ_DELAY=30000; //30 seconds

    public boolean keepRunning=true;
    private SabReadThreadState currentState;
    private List<String> commands=new Vector<String>();
    private int commandIndex=0;

    private static LocalStorage lStorage=new LocalStorage(AppCache.currentActivity);

    public void startReading(){
        keepRunning=true;
        if (AppCache.sabHelper!=null) {
            AppCache.sabHelper.addOnSabEventListener(sabEventListener);
        }
        this.start();
    }

    public void stopReading(){
        keepRunning=false;
        if (AppCache.sabHelper!=null){
            AppCache.sabHelper.removeOnSabEventListener(sabEventListener);
        }
    }

    public static void startServerQueueThread(){
        Thread thr=new Thread(){
            public void run(){
                try{
                    Thread.sleep(3000);
                }catch(Exception ex){}

                handleCommandsQueue();
            }
        };
        thr.start();
    }

    private static void handleCommandsQueue(){
        List<String> comms=lStorage.getQueuedCommands();
        for (int a=0;a<comms.size();a++){
            String cmd=comms.get(a);
            if (sendToServer(cmd)){
                try {
                    lStorage.removeCommandFromQueue(cmd);
                }catch(Exception ex){
                    Log.e(TAG,"Exception in removeComamndFromQueue: "+ex.getMessage());
                }
            }
        }
    }

    private static boolean sendToServer(String qCommand){
        try{
            UIHelper.makeLongToast("Sending to server: "+qCommand,AppCache.currentActivity);
            String resp=SabWebServer.sendQCommand(qCommand);
            Log.d(TAG,"Response: "+resp);
            UIHelper.makeLongToast("Server response: "+resp,AppCache.currentActivity);
            if (SabBluetoothHelper.isResponseOk(resp)){
                return true;
            }
            return false;
        }catch(Exception ex){
            Log.e(TAG,"Error: "+ex.getMessage());
            UIHelper.makeLongToast("Server error: "+ex.getMessage(),AppCache.currentActivity);
            return false;
        }
    }

    public List<String> runCWCommand() {
        List<String> cmds=new Vector<String>();
        try {
            SetupData sData = AppCache.currentSetupData;
            String qCommand = "#CW#" + String.valueOf(sData.code) + "#"+AppCache.sabData.identifier+"@";
            UIHelper.makeLongToast("Sending to server: " + qCommand, AppCache.currentActivity);
            String resp = SabWebServer.sendQCommand(qCommand);
            Log.d(TAG, "Response: " + resp);
            UIHelper.makeLongToast("Server response: " + resp, AppCache.currentActivity);
            String[] vals = resp.split("\n");
            if (vals.length > 1) {
                for (int a = 1; a < vals.length; a++) {
                    if (vals[a].trim().compareTo("") != 0) {
                        String cmd = vals[a].trim();
                        Log.d(TAG, "Command: " + cmd);
                        if (cmd.startsWith("#HS#")) {
                            Log.d(TAG, "Got HS: " + cmd);
                            String str = SabBluetoothHelper.getResponseParameter(cmd, 1);
                            Log.d(TAG, "Parameter: " + str);
                            try {
                                long lt = Long.valueOf(str);
                                Log.d(TAG, "Converted to long");
                                lStorage.setCWInterval(lt);
                            } catch (Exception ex) {
                            }
                        } else if (cmd.startsWith("#HD#")) {
                            Log.d(TAG, "Got HD: " + cmd);
                            String str = SabBluetoothHelper.getResponseParameter(cmd, 1);
                            Log.d(TAG, "Parameter: " + str);
                            try {
                                long lt = Long.valueOf(str);
                                Log.d(TAG, "Converted to long");
                                lStorage.setDataInterval(lt);
                            } catch (Exception ex) {
                            }
                        } else {
                            cmds.add(cmd);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Exception CW Thread: " + ex.getMessage());
            UIHelper.makeLongToast("CW: Server error: " + ex.getMessage(), AppCache.currentActivity);
        }
        return cmds;
    }

    private static String getHexString(byte[] buff,int len){
        String hexStr="";
        for (int a=0;a<len;a++){
            hexStr+=String.format("%02x",buff[a]);
        }
        hexStr= hexStr.toUpperCase();
        Log.d(TAG,"Hex: "+hexStr);
        return hexStr;
    }

    public static void runDDCommand() {
        try {
            String qCommand = "#DD#" + String.valueOf(AppCache.currentCommandCode) + "#"+AppCache.sabData.identifier;
            qCommand+="#"+SabBluetoothHelper.getCurrentDate()+"#"+SabBluetoothHelper.getCurrentTime();
            qCommand+="#"+getHexString(AppCache.sabData.currentDataBytes,20)+"@";
            UIHelper.makeLongToast("Sending to server: " + qCommand, AppCache.currentActivity);
            String resp = SabWebServer.sendQCommand(qCommand);
            Log.d(TAG, "Response: " + resp);
            UIHelper.makeLongToast("Server response: " + resp, AppCache.currentActivity);
        } catch (Exception ex) {
            Log.e(TAG, "Exception DD Thread: " + ex.getMessage());
            UIHelper.makeLongToast("DD: Server error: " + ex.getMessage(), AppCache.currentActivity);
        }
    }

    private boolean firstTimeCommands=true;
    public boolean cwSent=false;

    public void run() {
        boolean canReadData=true;
        while (keepRunning) {
            long delay = lStorage.getDataInterval();
            if (delay == 0) {
                canReadData=false;
            }else{
                canReadData=true;
            }
            SAB_DATA_READ_DELAY = (delay * 1000);

            commands.clear();
            if (firstTimeCommands) {
                String pinCode = lStorage.getPinCode();
                if (pinCode == null) {
                    pinCode = "0000";
                }
                String cmd = "#PS#" + AppCache.currentCommandCode + "#" + pinCode + "@";
                commands.add(cmd);
                commands.add("#GI#" + AppCache.currentCommandCode + "@");
                commands.add("#GN#" + AppCache.currentCommandCode + "@");
                commands.add("#GV#" + AppCache.currentCommandCode + "@");
                commands.add("#GD#" + AppCache.currentCommandCode + "@");
                commands.add("#GT#" + AppCache.currentCommandCode + "@");
                commands.add("#GS#" + AppCache.currentCommandCode + "@");
            }

            try {
                Thread.sleep(1000);
            } catch (Exception ex) {
            }

            if (AppCache.sabHelper != null) {
                if (AppCache.sabHelper.isDeviceConnected()
                        && !AppCache.sabHelper.isBtOperationInProgress()
                        && !AppCache.isApplyingSettings) {
                    commandIndex = 0;
                    if (canReadData) {
                        currentState = SabReadThreadState.READING;
                        if (!AppCache.sabHelper.readSabData()) {
                            //UIHelper.makeLongToast("RT: Error in readSabData",AppCache.currentActivity);
                        }
                    }else if (firstTimeCommands){
                        currentState=SabReadThreadState.READ_DONE;
                        applyCommand();
                    }
                }
            }

            firstTimeCommands = false;

            if (canReadData){
                try{
                    Thread.sleep(SAB_DATA_READ_DELAY);
                }catch(Exception ex){}
            }else{
                break;
            }
        }

        //AppCache.sabHelper.removeOnSabEventListener(sabEventListener);
        try {
            Thread.sleep(SAB_DATA_READ_DELAY);
        } catch (Exception ex) {
        }
    }

    private void applyCommand() {
        try {
            Thread.sleep(100);
        } catch (Exception ex) {
        }

        if (commandIndex < commands.size()) {
            String str = commands.get(commandIndex);
            if (!AppCache.sabHelper.writeCommand(str)) {
                UIHelper.makeLongToast("RT: Error in writeCommand " + str, AppCache.currentActivity);
            }
        } else {
            commands.clear();
        }
    }

    /***** OnSabEventListener start ******/

    DefaultSabEventListener sabEventListener=new DefaultSabEventListener(){
        @Override
        public void onSabDataReceived(SabBluetoothHelper sab, SabData sData) {
            if (!AppCache.isApplyingSettings) {
                //UIHelper.makeLongToast("RT: Sab data received",AppCache.currentActivity);
                AppCache.sabData.setData(sData);
                if (currentState == SabReadThreadState.READING) {
                    currentState= SabReadThreadState.READ_DONE;
                    if (cwSent){
                        if (sData.sendToServer){
                            Thread thr=new Thread(){
                                public void run(){
                                    runDDCommand();
                                }
                            };
                            thr.start();
                        }
                    }
                    applyCommand();
                }
            }
        }

        @Override
        public void onSabCommandResponseReceived(SabBluetoothHelper sab, String lastCommandExecuted, String response) {
            lastCommandExecuted = SabBluetoothHelper.getCommandFromResponse(response);
            if (lastCommandExecuted != null) {
                if (!AppCache.isApplyingSettings) {
                    //UIHelper.makeLongToast("RT: Response for "+lastCommandExecuted+": "+response,AppCache.currentActivity);
                    if (lastCommandExecuted.compareTo("GD") == 0) {
                        //get ddmmyy and set to AppCache.sabData
                        if (SabBluetoothHelper.isResponseOk(response)) {
                            String date = SabBluetoothHelper.getResponseParameter(response, 1);
                            AppCache.sabData.date = date;
                        }
                    } else if (lastCommandExecuted.compareTo("GT") == 0) {
                        //get ddmmyy and set to AppCache.sabData
                        if (SabBluetoothHelper.isResponseOk(response)) {
                            String date = SabBluetoothHelper.getResponseParameter(response, 1);
                            AppCache.sabData.time = date;
                        }
                    } else if (lastCommandExecuted.compareTo("GI") == 0) {
                        //UIHelper.makeLongToast("RT: Response for GI: " + response, AppCache.currentActivity);
                        if (SabBluetoothHelper.isResponseOk(response)) {
                            AppCache.sabData.identifier = SabBluetoothHelper.getResponseParameter(response, 1);
                            //UIHelper.makeLongToast("RT:Identifier: "+AppCache.sabData.identifier,AppCache.currentActivity);
                            if (!cwSent){
                                List<String> cwCommands=runCWCommand();
                                for (int a=0;a<cwCommands.size();a++){
                                    commands.add(cwCommands.get(a));
                                }
                                cwCommands.clear();

                                //if sabData.sendToServer
                                if (AppCache.sabData.sendToServer){
                                    Thread thr=new Thread(){
                                        public void run(){
                                            runDDCommand();
                                        }
                                    };
                                    thr.start();
                                }

                                cwSent=true;
                            }
                        }
                    } else if (lastCommandExecuted.compareTo("GV") == 0) {
                        //UIHelper.makeLongToast("RT:Response for GV: " + response, AppCache.currentActivity);
                        if (SabBluetoothHelper.isResponseOk(response)) {
                            AppCache.sabData.firmwareVersion = SabBluetoothHelper.getResponseParameter(response, 1);
                            //UIHelper.makeLongToast("RT:Firmware: "+AppCache.sabData.firmwareVersion,AppCache.currentActivity);
                        }
                    }else if (lastCommandExecuted.compareTo("GN") == 0) {
                        //UIHelper.makeLongToast("RT:Response for GN: " + response, AppCache.currentActivity);
                        if (SabBluetoothHelper.isResponseOk(response)) {
                            AppCache.sabData.sabName = SabBluetoothHelper.getResponseParameter(response, 1);
                            //UIHelper.makeLongToast("RT:SAB Name: "+AppCache.sabData.sabName,AppCache.currentActivity);
                        }
                    }else if (lastCommandExecuted.compareTo("GS") == 0) {
                        //UIHelper.makeLongToast("RT:Response for GS: " + response, AppCache.currentActivity);
                        if (SabBluetoothHelper.isResponseOk(response)) {
                            String str=SabBluetoothHelper.getResponseParameter(response,2);
                            try{
                                AppCache.currentSettings.minimumFanValue=Integer.valueOf(str);
                            }catch(Exception ex){}
                            str=SabBluetoothHelper.getResponseParameter(response,1);
                            try{
                                AppCache.currentSettings.fanSpeedValue=Integer.valueOf(str);
                            }catch(Exception ex){}
                        }
                    }
                    commandIndex++;
                    applyCommand();
                }
            }
        }

        @Override
        public void onCommandExecuted(SabBluetoothHelper sab, String command) {
            if (!AppCache.isApplyingSettings) {
                //UIHelper.makeLongToast("RT: Command " + command + " executed", AppCache.currentActivity);
            }
        }

        @Override
        public void onError(SabBluetoothHelper sab, int error) {
            if (!AppCache.isApplyingSettings) {
                UIHelper.makeLongToast("RT: Error in Bluetooth operation", AppCache.currentActivity);
            }
        }
    };
}
