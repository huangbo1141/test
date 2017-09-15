package com.usmani.android;

import java.io.File;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import com.usmani.android.MediaEntity;

public class MediaHelper {
	
	public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	public static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE=102;
	public static final int SELECT_PICTURE_REQUEST_CODE=101;
	
	public static String rootPath=Environment.getExternalStorageDirectory().getPath();
	public static String[] imageOptions=new String[]{"Take picture","Gallery","Record Video"};
	
	public static MediaEntity createMediaEntity(Uri uri,String caption,MediaType mType){
		MediaEntity m=new MediaEntity();
		m.setCaption(caption);
		m.setPath(uri.toString());
		m.setMediaType(mType);
		m.setId(new java.util.Date().getTime());
		return m;
	}
	
	public static Uri getOutputMediaUri(MediaType mType){
		if (mType==MediaType.Picture)
			return getOutputImageFileUri();
		else if (mType==MediaType.Video)
			return getOutputVideoFileUri();
		return null;		
	}
	
	public static String getImageFile(){
		return getMediaFilePath("jpg");
	}
	
	public static String getVideoFile(){
		return getMediaFilePath("mp4");
	}
	
	private static String getMediaFilePath(String ext){
		String path=rootPath+"/coasis_"+(String.valueOf((new java.util.Date().getTime())))+"."+ext;
		return path;
	}
	
	public static Uri getOutputImageFileUri(){
		return getUriFromFilePath(getImageFile());		
	}
	
	public static Uri getOutputVideoFileUri(){
		return getUriFromFilePath(getVideoFile());		
	}
	
	public static Uri getUriFromFilePath(String path){
		Uri u=null;
		File f=new File(getVideoFile());
		u=Uri.fromFile(f);
		return u;
	}	
	
	public static void attachVideoFromCamera(Activity activity,Uri fileUri){
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		if (fileUri!=null){				
			Log.i("fileUri","it is: "+fileUri.toString());
			intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
			intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,30);				
			activity.startActivityForResult(intent, MediaHelper.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
		}else{
			Log.e("fileuri","It is null");				
		}		
	}
	
	public static void attachImageFromGallery(Activity activity){
		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), MediaHelper.SELECT_PICTURE_REQUEST_CODE);
	}
	
	public static void attachImageFromCamera(Activity activity,Uri fileUri){		
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);	    
		if (fileUri!=null){
			Log.i("fileUri","it is: "+fileUri.toString());			
			intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
			activity.startActivityForResult(intent, MediaHelper.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
		}else{
			Log.e("fileuri","It is null");
		}		
	}
}
