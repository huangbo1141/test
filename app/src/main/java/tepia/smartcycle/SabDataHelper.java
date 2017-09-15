package tepia.smartcycle;

import android.util.Log;

import java.util.Calendar;

/**
 * Created by Rameez Usmani on 6/21/2017.
 */

public class SabDataHelper {

    private static final String TAG=SabDataHelper.class.getName();

    public static SabData processSabData(byte[] data){
        SabData sData=new SabData();
        sData.currentDataBytes=data;
        if ((data[0] & 0xF0)==0xF0){
            sData.sendToServer=true;
        }
        sData.temperature=getTemperature(data[1]);
        sData.humidity=getHumidity(data[2]);
        sData.externTemperature=getTemperature(data[3]);
        sData.externHumidity=getHumidity(data[4]);
        sData.temperatureProbe1=getTemperature(data[5]);
        sData.humidityProbe1=getHumidity(data[6]);
        sData.temperatureProbe2=getTemperature(data[7]);
        sData.humidityProbe2=getHumidity(data[8]);
        sData.temperatureProbe3=getTemperature(data[9]);
        sData.humidityProbe3=getHumidity(data[10]);
        sData.fanSpeed=getFanSpeed(data[11]);
        sData.preheatTemperature=getPreheatTemperature(data[11]);
        sData.filterStatus=getFilterStatus(data[14]);
        sData.atmosphereStatus=getAtmosphereStatus(data[14]);
        sData.flagSet1=data[15];
        String str="Byte 12: "+String.valueOf(data[11]);
        str+=",Byte 15: "+String.valueOf(data[14]);
        str+=",Byte 16: "+String.valueOf(data[15]);
        //UIHelper.makeLongToast(str,AppCache.currentActivity);
        str="Atmosphere: "+String.valueOf(sData.atmosphereStatus);
        str+=",Preheat: "+String.valueOf(sData.preheatTemperature);
        str+=",Flagset1: "+String.valueOf(sData.flagSet1);
        //UIHelper.makeLongToast(str, AppCache.currentActivity);
        return sData;
    }

    public static int getFlagSet(byte bt){
        int flagSet=bt & 0xFF;
        return flagSet;
    }

    public static float getTemperature(byte bt){
        //bt=0x45;
        float temp=0;
        float maxTemp=20;
        float subtractThreshold=0.5f;
        int val=bt & 0xFF;
        if (val>0){
            temp=maxTemp-((val-1)*subtractThreshold);
            temp*=-1;
        }
        return temp;
    }

    public static int getHumidity(byte bt){
        int humidity=bt & 0xFF;
        return humidity;
    }

    public static int getFanSpeed(byte bt){
        int fanSpeed=bt & 0xF0;
        fanSpeed=fanSpeed>>4;
        return fanSpeed;
    }

    public static int getFilterStatus(byte bt){
        int filterStatus=bt & 0xF0;
        filterStatus=filterStatus>>4;
        return filterStatus;
    }

    public static int getPreheatTemperature(byte bt){
        int preHeat=bt & 0x0F;
        return preHeat;
    }

    public static int getAtmosphereStatus(byte bt){
        int atmosphere=bt & 0x0F;
        return atmosphere;
    }

    public static boolean isResponseOk(String response){
        return response.startsWith("#OK");
    }

    //whichOne = 1 for 1st , 2 for 2nd ....
    public static String getResponseParameter(String response,int whichOne){
        String[] vals=response.split("#");
        String param=null;
        //ignore 0,1 and 2 as they will be "" and "OK" and "Command"
        if (vals.length>=(3+whichOne)){
            param=vals[(3+whichOne)-1];
            if (param!=null){
                vals=param.split("@");
                param=vals[0];
            }
        }
        return param;
    }

    public static String createCommand(String commandName,String code,String[] params){
        Log.d(TAG,"createCommand");
        String command="#"+commandName+"#"+code;
        for (int a=0;a<params.length;a++){
            command+="#"+params[a];
        }
        command+="@";
        Log.d(TAG,"Command created: "+command);
        return command;
    }

    public static String getCommandFromResponse(String response){
        String[] vals=response.split("#");
        String param=null;
        //ignore 0 and 1 as they will be "" and "OK"
        if (vals.length>2){
            param=vals[2];
        }
        return param;
    }
    public static String getCurrentDate(){
        String cmd="";
        Calendar cld=Calendar.getInstance();
        String strDD=String.valueOf(cld.get(Calendar.DAY_OF_MONTH));
        if (strDD.length()==1){
            strDD="0"+strDD;
        }
        cmd+=strDD;
        String strMM=String.valueOf(cld.get(Calendar.MONTH));
        if (strMM.length()==1){
            strMM="0"+strMM;
        }
        cmd+=strMM;
        String strYY=String.valueOf(cld.get(Calendar.YEAR));
        if (strYY.length()==4){
            strYY=strYY.substring(2);
        }
        cmd+=strYY;
        return cmd;
    }

    public static String getSDForCurrent(String code){
        String cmd="#SD#"+code+"#";
        Calendar cld=Calendar.getInstance();
        String strDD=String.valueOf(cld.get(Calendar.DAY_OF_MONTH));
        if (strDD.length()==1){
            strDD="0"+strDD;
        }
        cmd+=strDD;
        String strMM=String.valueOf(cld.get(Calendar.MONTH));
        if (strMM.length()==1){
            strMM="0"+strMM;
        }
        cmd+=strMM;
        String strYY=String.valueOf(cld.get(Calendar.YEAR));
        if (strYY.length()==4){
            strYY=strYY.substring(2);
        }
        cmd+=strYY;
        cmd+="@";
        return cmd;
    }

    public static String getCurrentTime(){
        String cmd="";
        Calendar cld=Calendar.getInstance();
        String strDD=String.valueOf(cld.get(Calendar.HOUR_OF_DAY));
        if (strDD.length()==1){
            strDD="0"+strDD;
        }
        cmd+=strDD;
        String strMM=String.valueOf(cld.get(Calendar.MINUTE));
        if (strMM.length()==1){
            strMM="0"+strMM;
        }
        cmd+=strMM;
        String strYY=String.valueOf(cld.get(Calendar.SECOND));
        if (strYY.length()==1){
            strYY="0"+strYY;
        }
        cmd+=strYY;
        return cmd;
    }

    public static String getSTForCurrent(String code){
        String cmd="#ST#"+code+"#";
        Calendar cld=Calendar.getInstance();
        String strDD=String.valueOf(cld.get(Calendar.HOUR_OF_DAY));
        if (strDD.length()==1){
            strDD="0"+strDD;
        }
        cmd+=strDD;
        String strMM=String.valueOf(cld.get(Calendar.MINUTE));
        if (strMM.length()==1){
            strMM="0"+strMM;
        }
        cmd+=strMM;
        String strYY=String.valueOf(cld.get(Calendar.SECOND));
        if (strYY.length()==1){
            strYY="0"+strYY;
        }
        cmd+=strYY;
        cmd+="@";
        return cmd;
    }

    public static boolean isInstallerAllowed(String dt){
        if (dt==null){
            return false;
        }
        Calendar cld=Calendar.getInstance();
        if (dt.length()==6){
            int day=cld.get(Calendar.DAY_OF_MONTH);
            int month=cld.get(Calendar.MONTH)+1;
            int year=cld.get(Calendar.YEAR);
            String valDay=dt.substring(0,2);
            String valMonth=dt.substring(2,4);
            String valYear=dt.substring(4,6);
            Log.d(TAG,valDay+","+valMonth+","+valYear);
            int dtDay=Integer.valueOf(valDay);
            int dtMonth=Integer.valueOf(valMonth);
            int dtYear=2000+Integer.valueOf(valYear);
            if (dtYear<year){
                return false;
            }else if(dtYear==year){
                if (dtMonth<month){
                    return false;
                }else if (dtMonth==month){
                    if (dtDay<day){
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
