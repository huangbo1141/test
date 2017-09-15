package com.usmani.android;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class AndroidHelper {
	
	private static final String TAG="com.usmani.android.AndroidHelper";
	
	public static void callNumber(String num,Context context){
		Intent intent = new Intent(Intent.ACTION_CALL);
		intent.setData(Uri.parse("tel:" + num));
		context.startActivity(intent);
	}
	
	public static boolean isServiceRunning(String serviceName,Context context) {
	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    	Log.d(TAG,service.service.getClassName());
	        if (serviceName.equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public static void startActivity(final Activity ctx,final Intent intent){
		ctx.runOnUiThread(new Runnable(){
			public void run(){
				try{
					ctx.startActivity(intent);
				}catch(Exception ex){
					UIHelper.makeLongToast("Error: "+ex.getMessage(),ctx,false);
				}
			}
		});
	}
	
	public static void startActivity(final Activity ctx,final Intent intent,final boolean finish){
		ctx.runOnUiThread(new Runnable(){
			public void run(){
				try{
					ctx.startActivity(intent);
					if (finish){
						ctx.finish();
					}
				}catch(Exception ex){
					UIHelper.makeLongToast("Error: "+ex.getMessage(),ctx,false);
				}
			}
		});
	}
}
