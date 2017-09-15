package tepia.smartcycle;

/**
 * Created by Rameez Usmani on 6/1/2017.
 */

public class SabData {

    public byte[] currentDataBytes;

    public boolean sendToServer=false;
    public float temperature;
    public int humidity;
    public float externTemperature;
    public int externHumidity;
    public float temperatureProbe1;
    public int humidityProbe1;
    public float temperatureProbe2;
    public int humidityProbe2;
    public float temperatureProbe3;
    public int humidityProbe3;
    public int fanSpeed;
    public int preheatTemperature;
    public int filterStatus;
    public int atmosphereStatus;
    public byte flagSet1;
    public String date="",time="";
    public String identifier="";
    public String firmwareVersion="";
    public String sabName="";

    public void setData(SabData sData){
        this.currentDataBytes=sData.currentDataBytes;
        this.sendToServer=sData.sendToServer;
        this.temperature=sData.temperature;
        this.humidity=sData.humidity;
        this.temperatureProbe1=sData.temperatureProbe1;
        this.humidityProbe1=sData.humidityProbe1;
        this.temperatureProbe2=sData.temperatureProbe2;
        this.humidityProbe2=sData.humidityProbe2;
        this.temperatureProbe3=sData.temperatureProbe3;
        this.humidityProbe3=sData.humidityProbe3;
        this.externTemperature=sData.externTemperature;
        this.externHumidity=sData.externHumidity;
        this.fanSpeed=sData.fanSpeed;
        this.filterStatus=sData.filterStatus;
        this.preheatTemperature=sData.preheatTemperature;
        this.flagSet1=sData.flagSet1;
        this.atmosphereStatus=sData.atmosphereStatus;
    }

    public float getTemperature(int index){
        if (index==0) {
            return temperature;
        }else if (index==1){
            return (externTemperature);
        } else if (index==2){
            return (temperatureProbe1);
        }else if (index==3){
            return (temperatureProbe2);
        }else if (index==4){
            return (temperatureProbe3);
        }
        return -1;
    }

    public int getHumidity(int index){
        if (index==0) {
            return humidity;
        }else if (index==1){
            return (externHumidity);
        } else if (index==2){
            return (humidityProbe1);
        }else if (index==3){
            return (humidityProbe2);
        }else if (index==4){
            return (humidityProbe3);
        }
        return -1;
    }
}
