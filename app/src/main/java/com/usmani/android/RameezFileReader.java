package com.usmani.android;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import android.content.Context;
import android.net.Uri;

public class RameezFileReader {
	
	private static int BUFFER_SIZE=2048;
	
	public static String readStringData(InputStream inputStream)
	throws Exception{
		byte[] buffer = new byte[BUFFER_SIZE];
		StringBuilder result=new StringBuilder();
		while (true) {
			int bytesRead = inputStream.read( buffer, 0, BUFFER_SIZE );
			if (bytesRead == -1)
				break;
			result.append(new String(buffer,0,bytesRead));
		}
		return result.toString();
	}
	
	public static byte[] readData(InputStream inputStream)
	throws Exception {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[BUFFER_SIZE];
		while (true) {
			int bytesRead = inputStream.read( buffer, 0, BUFFER_SIZE );
			if (bytesRead == -1)
				break;
			byteArrayOutputStream.write( buffer, 0, bytesRead );
		}
		byteArrayOutputStream.flush();
		byte[] result = byteArrayOutputStream.toByteArray();
		byteArrayOutputStream.close();
		inputStream.close();
		return result;
	}
	
	public static byte[] readFile(File file)
	throws Exception {
		InputStream inputStream = null;
		inputStream = new FileInputStream(file);
		return readData(inputStream);
	}			
	
	public static byte[] readFile(String filePath)
	throws Exception {
		InputStream inputStream = null;
		inputStream = new FileInputStream(filePath);
		return readData(inputStream);
	}
	
	public static byte[] readFileFromContent(Uri filePath,Context act)
	throws Exception{
		InputStream inputStream=null;
		inputStream=act.getContentResolver().openInputStream(filePath);
		return readData(inputStream);
	}
	
	public static byte[] readFile(Uri filePath,Context ctx)
	throws Exception {		
		if (filePath.getScheme().contains("content")){
			return readFileFromContent(filePath,ctx);
		}	
		//assume it is file scheme
		return readFile(new File(filePath.getPath()));		
	}
}
