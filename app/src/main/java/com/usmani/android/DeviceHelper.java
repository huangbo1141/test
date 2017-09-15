package com.usmani.android;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class DeviceHelper {
	
	private static final String TAG=DeviceHelper.class.getName();
	
	public static ActivityManager.MemoryInfo getSystemMemoryInfo(Context context){
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(memoryInfo);
		
		Log.i(TAG, " memoryInfo.availMem " + memoryInfo.availMem + "\n" );
		Log.i(TAG, " memoryInfo.lowMemory " + memoryInfo.lowMemory + "\n" );
		Log.i(TAG, " memoryInfo.threshold " + memoryInfo.threshold + "\n" );
		
		return memoryInfo;
	}
	
	public static android.os.Debug.MemoryInfo getMyMemoryInfo(Context context){
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
		for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses){
		    //pidMap.put(runningAppProcessInfo.pid, runningAppProcessInfo.processName);
			String pName=runningAppProcessInfo.processName;
			if (pName.compareTo("com.mobilepos")==0){
				int pids[] = new int[1];
				pids[0]=runningAppProcessInfo.pid;
				android.os.Debug.MemoryInfo[] memoryInfoArray = activityManager.getProcessMemoryInfo(pids);
				return memoryInfoArray[0];
			}
		}
		
		return null;

//		Collection<Integer> keys = pidMap.keySet();
//
//		for(int key : keys)
//		{
//		    int pids[] = new int[1];
//		    pids[0] = key;
//		    android.os.Debug.MemoryInfo[] memoryInfoArray = activityManager.getProcessMemoryInfo(pids);
//		    for(android.os.Debug.MemoryInfo pidMemoryInfo: memoryInfoArray)
//		    {
//		        Log.i(TAG, String.format("** MEMINFO in pid %d [%s] **\n",pids[0],pidMap.get(pids[0])));
//		        Log.i(TAG, " pidMemoryInfo.getTotalPrivateDirty(): " + pidMemoryInfo.getTotalPrivateDirty() + "\n");
//		        Log.i(TAG, " pidMemoryInfo.getTotalPss(): " + pidMemoryInfo.getTotalPss() + "\n");
//		        Log.i(TAG, " pidMemoryInfo.getTotalSharedDirty(): " + pidMemoryInfo.getTotalSharedDirty() + "\n");
//		    }
//		}
	}
	
	public static float readCPUUsage() {
	    try {
	    	Log.d(TAG,"readCPUUsage");
	        RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
	        Log.d(TAG,"reader created");
	        String load = reader.readLine();
	        //while(load!=null){
	        	Log.d(TAG,"readLine: "+load);
	        	//load=reader.readLine();
	        //}
	        
	        String[] toks = load.split(" ");
	        
	        for (int a=0;a<toks.length;a++){
	        	Log.d(TAG,"toks"+String.valueOf(a)+"="+toks[a]);
	        }
	        
//	        PrevIdle = previdle + previowait
//	        		Idle = idle + iowait
//
//	        		PrevNonIdle = prevuser + prevnice + prevsystem + previrq + prevsoftirq + prevsteal
//	        		NonIdle = user + nice + system + irq + softirq + steal
//
//	        		PrevTotal = PrevIdle + PrevNonIdle
//	        		Total = Idle + NonIdle
//
//	        		# differentiate: actual value minus the previous one
//	        		totald = Total - PrevTotal
//	        		idled = Idle - PrevIdle
//
//	        		CPU_Percentage = (totald - idled)/totald

//	        long idle1 = Long.parseLong(toks[5]);
//	        long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
//	              + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
	        
	        long idle1=Long.parseLong(toks[5])+Long.parseLong(toks[6]);
	        long cpu1=Long.parseLong(toks[2])+Long.parseLong(toks[3])+Long.parseLong(toks[4])+Long.parseLong(toks[7])
	        		+Long.parseLong(toks[8])+Long.parseLong(toks[9]);

//	        try {
//	            Thread.sleep(360);
//	        } catch (Exception e) {}
//
//	        reader.seek(0);
//	        load = reader.readLine();
//	        reader.close();
//
//	        toks = load.split(" ");
//
//	        long idle2 = Long.parseLong(toks[5]);
//	        long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
//	            + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

	        //return (float)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));
	        float res=(float)cpu1/(cpu1+idle1);
	        return res*100;
	        //return (float)(cpu1-idle1)/cpu1;
	    } catch (IOException ex) {
	    	Log.e("DeviceHelper","Error in cpu: "+ex.getMessage());
	        ex.printStackTrace();
	    }

	    return 0;
	}
	
	private static String getHumanReadableSize(long free){
		Log.d("DeviceHelper","free: "+String.valueOf(free));
		String suffix="b";
        double ffree=free;
        if (ffree>1024){
        	ffree=ffree/1024; //kb
        	suffix="kb";
        	if (ffree>1024){
        		ffree=ffree/1024; //mb
            	suffix="mb";
        		if (ffree>1024){
        			ffree=ffree/1024; //gb
                	suffix="gb";
        		}
        	}
        }
        Log.d("Device",String.valueOf(ffree));
        String strF=String.valueOf(ffree);
		String[] vals=strF.split("\\.");
		Log.d("DeviceHelper","Vals length: "+String.valueOf(vals.length));
		if (vals.length>1){
			String s=vals[1];
			Log.d("DeviceHelper","part: "+s);
			if(s.length()>2){
				s=s.substring(0,2);
				Log.d("DeviceHelper","Substr: "+s);
			}
			strF=vals[0]+"."+s;
			Log.d("DeviceHelper","New :"+strF);
		}
		return strF+suffix;
	}
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static long getFreeSize(StatFs statFs){
		long free=0;
		long blockSize=0L;
		long blockCount=0L;
		if (android.os.Build.VERSION.SDK_INT>=18){
			blockCount=statFs.getAvailableBlocksLong();
			blockSize=statFs.getBlockSizeLong();
		}else{
			blockCount=statFs.getAvailableBlocks();
			blockSize=statFs.getBlockSize();
		}
		free=blockSize*blockCount;
		return free;
	}
	
	@SuppressWarnings("deprecation")
	public static String getFreeMemory(){
	    StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        long free=getFreeSize(statFs);
        return getHumanReadableSize(free);
	}
	
	@SuppressWarnings("deprecation")
	public static String getFreeSDMemory(){
		Log.d("DeviceHelper","SDCard: "+Environment.getExternalStorageState());
		Log.d("DeviceHelper","pics Pub: "+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
	    StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
	    long free=getFreeSize(statFs);
        return getHumanReadableSize(free);
	}
}
