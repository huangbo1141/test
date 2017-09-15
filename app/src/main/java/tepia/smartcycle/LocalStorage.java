package tepia.smartcycle;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.usmani.android.LocationEntity;
import com.usmani.android.RameezFileReader;
import com.usmani.android.UIHelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.util.ArraySet;
import android.util.Log;

public class LocalStorage {
	
	private static final String TAG="LocalStorage";

	private SharedPreferences prefs;
	private Context context;
	
	public LocalStorage(Context ctx){
		prefs=PreferenceManager.getDefaultSharedPreferences(ctx);
		Log.d(TAG, "Got prefs");
		context=ctx;
	}
	
	public void clear(){
		SharedPreferences.Editor et=prefs.edit();
		et.clear();
		et.commit();
	}
	
	private void save(String key,boolean value){
		SharedPreferences.Editor et=prefs.edit();
		et.putBoolean(key, value);
		et.commit();
	}
	
	private boolean restore(String key,boolean defVal){
		return prefs.getBoolean(key,defVal);
	}

    private void save(String key,int value){
        SharedPreferences.Editor et=prefs.edit();
        et.putInt(key,value);
        et.commit();
    }

    private int restore(String key,int defVal){
        return prefs.getInt(key,defVal);
    }
	
	private void save(String key,String value){
		SharedPreferences.Editor et=prefs.edit();
		et.putString(key, value);
		et.commit();
	}
	
	private String restore(String key,String defVal){
		return prefs.getString(key, defVal);
	}

    private void saveStringSet(String key,Set<String> value){
        SharedPreferences.Editor et=prefs.edit();
        et.putStringSet(key, value);
        et.commit();
    }

    private Set<String> getStringSet(String key,Set<String> defVal){
        return prefs.getStringSet(key,defVal);
    }
	
	private void saveL(String key,long value){
		SharedPreferences.Editor et=prefs.edit();
		et.putLong(key, value);
		et.commit();
	}
	
	private long restoreL(String key,long defVal){
		return prefs.getLong(key, defVal);
	}
	
	public void setUsername(String uname){
		save("username", uname);
	}

	public String getUsername(){
		return restore("username", null);
	}

	public boolean getShowHumidityOnStatus(){
		return restore("show_humidity_on_status",true);
	}

	public void setShowHumidityOnStatus(boolean bs){
		save("show_humidity_on_status",bs);
	}

    public void saveSabData(SabData sData){
        Gson gs=new Gson();
        String str=gs.toJson(sData);
        save("sab",str);
    }

    public SabData getSabData(){
        String strSetup=restore("sab",null);
        SabData sData=null;
        if (strSetup!=null) {
            Gson gs = new Gson();
            sData=gs.fromJson(strSetup,SabData.class);
        }
        return sData;
    }

    public void saveSetupData(SetupData sData){
        Gson gs=new Gson();
        String str=gs.toJson(sData);
        save("setup",str);
    }

    public SetupData getSetupData(){
        String strSetup=restore("setup",null);
        SetupData sData=null;
        if (strSetup!=null) {
            Gson gs = new Gson();
            sData=gs.fromJson(strSetup,SetupData.class);
        }
        return sData;
    }

	public void setInstallerDate(String dt){
		save("installer_date",dt);
	}

	public String getInstallerDate(){
		return restore("installer_date",null);
	}

    public void clearQueue(){
        Log.d(TAG,"clearQueue");
        save("queue",null);
    }

    public void saveQueue(List<String> str){
        Log.d(TAG,"saveQueue");
        String cmd="";
        for (int a=0;a<str.size();a++){
            cmd+=str.get(a)+"|||";
        }
        Log.d(TAG,cmd);
        save("queue",cmd);
    }

    public List<String> getQueuedCommands(){
        Log.d(TAG,"getQueuedCommands");
        String sts=restore("queue",null);
        List<String> queue=new Vector<String>();
        if (sts!=null){
            Log.d(TAG,sts);
            String[] vals=sts.split("\\|\\|\\|");
            for (int a=0;a<vals.length;a++){
                if (vals[a].trim().compareTo("")!=0){
                    queue.add(vals[a].trim());
                }
            }
        }
        return queue;
    }

    public void addCommandToQueue(String cmd){
        Log.d(TAG,"addCommandToQueue: "+cmd);
        String stQ=restore("queue","");
        stQ+=cmd+"|||";
        Log.d(TAG,stQ);
        save("queue",stQ);
    }

    public void removeCommandFromQueue(String cmd){
        Log.d(TAG,"removeCommandFromQueue: "+cmd);
        List<String> queue=getQueuedCommands();
        for (int a=0;a<queue.size();a++){
            if (queue.get(a).trim().compareTo(cmd.trim())==0){
                queue.remove(a);
                break;
            }
        }
        saveQueue(queue);
    }

    /*public Set<String> getQueuedCommands(){
        LinkedHashSet<String> defVals=new LinkedHashSet<>();
        Set<String> sts=getStringSet("queue",defVals);
        return sts;
    }

    public void addCommandToQueue(String cmd){
        LinkedHashSet<String> defVals=new LinkedHashSet<>();
        Set<String> sts=getStringSet("queue",defVals);
        sts.add(cmd);
        saveStringSet("queue",sts);
    }

    public void removeCommandFromQueue(String cmd){
        LinkedHashSet<String> defVals=new LinkedHashSet<>();
        Set<String> sts=getStringSet("queue",defVals);
        try{
            sts.remove(cmd);
        }catch(Exception ex){

        }
        saveStringSet("queue",sts);
    }*/

    public void setDeviceId(String id){
        save("ble_device_id",id);
    }

    public String getDeviceId(){
        return restore("ble_device_id",null);
    }

    public String getPinCode(){
        return restore("ble_pin_code",null);
    }

    public void setPinCode(String pc){
        save("ble_pin_code",pc);
    }

    public void setDataInterval(long interval){
        saveL("data_interval",interval);
    }

    public long getDataInterval(){
        return restoreL("data_interval",15);
    }

    public void setCWInterval(long interval){
        saveL("cw_interval",interval);
    }

    public long getCWInterval(){
        return restoreL("cw_interval",0);
    }

    public int getLastCount(){
        return restore("last_sequence",0);
    }

    public void setLastCount(int l){
        save("last_sequence",l);
    }

    public boolean showStatus(){
        return restore("show_status",false);
    }

    public void setShowStatus(boolean b){
        save("show_status",b);
    }
}