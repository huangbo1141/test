package com.usmani.android;

import com.usmani.android.MediaType;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class MediaAttachmentHelper {
	private static final String TAG="MediaAttachmentHelper";
	
	private Context context;
	private OnMediaAttachListener listener=null;
	private Uri attachmentUri;	
	
	public MediaAttachmentHelper(Context context){
		this.context=context;
	}
	
	public void setOnMediaAttachListener(OnMediaAttachListener listener){
		this.listener=listener;
	}
	
	public void handleActivityResult(Activity activity,int requestCode, int resultCode, Intent data) {
		Log.d(TAG,"handleActivityResult: "+String.valueOf(requestCode));
	    if (requestCode == MediaHelper.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE
	    		||requestCode == MediaHelper.SELECT_PICTURE_REQUEST_CODE
	    		||requestCode == MediaHelper.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {	    	
	        if (resultCode == Activity.RESULT_OK) {	        	
	        	handleResultOK(activity,requestCode,data);
	        	Log.d(TAG,"Result is OK");
	        } else if (resultCode == Activity.RESULT_CANCELED) {
	        	handleResultCancel(activity,requestCode,data);
	        	Log.d(TAG,"Result is CANCELED");
	        } else {
	        	Log.e(TAG,"Failure");
	        }
	    }
	}
	
	private void handleResultCancel(Activity activity,int requestCode,Intent data){
		if (listener!=null){
    		listener.onMediaAttachCancel();
    	}
	}
	
	private void handleResultOK(Activity activity,int requestCode,Intent data){
		MediaType mType=MediaType.Picture;
    	if (requestCode==MediaHelper.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE)
    		mType=MediaType.Video;
    	else if (requestCode==MediaHelper.SELECT_PICTURE_REQUEST_CODE)
    		attachmentUri=data.getData();    	
    	if (listener!=null){
    		listener.onMediaAttach(attachmentUri,mType);
    	}
	}
	
	public void launchAttachmentFlow(final Activity activity){
		Log.d(TAG,"launchAttachmentFlow");
		if (context==null){
			Log.e(TAG,"context is null");
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
	    builder.setTitle("Select media option")
	           .setItems(MediaHelper.imageOptions, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	            	   if (which==0){
	            		   attachImageFromCamera(activity);
	            	   }else if (which==1){
	            		   attachImageFromGallery(activity);
	            	   }else if (which==2){
	            		   attachVideoFromCamera(activity);
	            	   }
	           }
	    });
	    builder.setOnCancelListener(new OnCancelListener(){
			@Override
			public void onCancel(DialogInterface dialog) {
				Log.d(TAG,"on cancel");
				if(listener!=null){
					listener.onMediaAttachCancel();
				}
			}
	    	
	    });
	    builder.create().show();
	}
	
	public void attachImageFromGallery(Activity activity){
		MediaHelper.attachImageFromGallery(activity);
	}
	
	public void attachImageFromCamera(Activity activity){		
		attachmentUri = MediaHelper.getOutputMediaUri(MediaType.Picture);
		MediaHelper.attachImageFromCamera(activity,attachmentUri);
	}
	
	public void attachVideoFromCamera(Activity activity){
		attachmentUri = MediaHelper.getOutputMediaUri(MediaType.Video);
		MediaHelper.attachVideoFromCamera(activity,attachmentUri);
	}
}
