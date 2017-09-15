package com.usmani.android;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.Hashtable;

import android.util.Log;

public class HttpUtil {
	
	private static String MULTIPART_BOUNDARY="----------ydxdqdjimrsdamseeldsrefqnkgvhgyu";
	private static String MULTIPART_HEADER="multipart/form-data; boundary="+MULTIPART_BOUNDARY;
	
	private static final String TAG=HttpUtil.class.getName();
	public static final String DEFAULT_POST_CONTENT_TYPE="application/x-www-form-urlencoded";
	
	public static String HttpLog="";
	public static String authHeaderValue=null;
	
	public static int timeout=-1; //-1 is default
	public static int ctimeout=-1; //-1 is default

	private static final String DEFAULT_READ_CHARSET="UTF-8";

	public HttpURLConnection connection;
		
	public static String encodeString(String unEncodedString){
		return URLUTF8Encoder.encode(unEncodedString);
	}
		
    public static HttpURLConnection getHttpConnection(String u,boolean doInput,boolean doOutput,boolean useCaches)
    throws Exception{
    	URL url=new URL(u);
    	HttpLog+="url created.\n";
    	try{
    		HttpURLConnection conn=(HttpURLConnection)url.openConnection();
    		HttpLog+="openConnection() called.\n";
    		if (conn==null){
    			throw new Exception("Conn in getHttpConnection is null");
    		}
    		if (ctimeout!=-1){
    			conn.setConnectTimeout(ctimeout);
    		}else{
    			conn.setConnectTimeout(5000);
    		}
    		if (timeout!=-1){
    			conn.setReadTimeout(timeout);
    		}else{
    			conn.setReadTimeout(5000);
    		}
    		HttpLog+="conn is not null.\n";
    		try{
    			conn.setDoInput(doInput);
    			conn.setDoOutput(doOutput);
    		}catch(Exception ex){}
    		
    		HttpLog+="setDoIO called.\n";
    		if (authHeaderValue!=null){
    			//conn.setRequestProperty("Authorization",authHeaderValue);
    		}
    		
    		return conn;
    	}catch(Exception ex){
    		throw new Exception("Error in url.openConnection(): "+ex.getMessage());
    	}        
    }
    
    public static String encodeUTF8Data(String name,String val){
    	return name+"="+URLUTF8Encoder.encode(val);
    }
    
    private static String readResponseBodyAsString(HttpURLConnection sconn,boolean tryReadError,String url,String requestData)
    throws Exception {
    	InputStream is=null;//sconn.getInputStream();
    	try{
    		is=sconn.getInputStream();
    	}catch(Exception ex){
    		Log.e("HttpUtil","Getting is error");
    		if (tryReadError){
    			try{
    				is=sconn.getErrorStream();
    			}catch(Exception ex2){
    				throw new Exception("Could not connect to webservice");
    			}
    		}else{
    			throw new Exception("Could not connect to webservice");
    		}
    	}
    	int rCode=200;
    	try{
    		rCode=sconn.getResponseCode();   
    		Log.d(TAG,"responsecode: "+String.valueOf(rCode));
    	}catch(Exception ex){
    		throw new Exception("Connection timed out");
    	}
    	if (is==null){
    		throw new Exception("InputStream is null and status code is "+String.valueOf(rCode));
        }
		StringBuilder jsonString = new StringBuilder();
    	/*BufferedReader in = new BufferedReader(new InputStreamReader(is, DEFAULT_READ_CHARSET));
        String inputLine;
        try{
        	while ((inputLine = in.readLine()) != null) {
        		jsonString.append(inputLine);
        	}
        }catch(Exception ex){
        	try{
        		in.close();
        		is.close();
        	}catch(Exception ex2){}
        	throw new Exception ("Connection timed out");
        }
        in.close();*/
		try{
			int bRead=-1;
			byte[] buff=new byte[1024];
			while ((bRead = is.read(buff,0,1024))!=-1) {
				String str=new String(buff,0,bRead);
				jsonString.append(str);
			}
		}catch(Exception ex){
			try{
				is.close();
			}catch(Exception ex2){}
			throw new Exception ("Connection timed out");
		}
        is.close();
        String body = jsonString.toString();
        if (rCode!=200
				&& !tryReadError){
        	throw new Exception(body);
        }
        return body;
    }
    
    private static void setCommonHeaders(HttpURLConnection sconn,String method,String contentType,String contentLength)
    throws Exception{
    	sconn.setRequestMethod(method);
    	sconn.setRequestProperty("Cache-Control", "no-cache");
    	sconn.setRequestProperty("Connection","keep-alive");
    	sconn.setRequestProperty("Pragma","no-cache");
    	sconn.setRequestProperty("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
    	sconn.setRequestProperty("User-Agent", "Android");
    	//sconn.setRequestProperty("Content-Length",contentLength);
    	sconn.setRequestProperty("Content-Type",contentType);
    }

	public String getTokenFromHeader(){
		String str=connection.getHeaderField("Authorization");
		if (str!=null){
			String[] vals=str.split(" ");
			if (vals.length>0) {
				str = vals[1];
			}
		}
		return str;
	}
    
    public String getHttpResponseBody(String url,String contentType)
    throws Exception{

    	HttpURLConnection sconn = getHttpConnection(url,true,false,false);
		connection=sconn;
    	if (contentType!=null && contentType.compareTo("")!=0){
    		sconn.setRequestProperty("Content-Type",contentType);
    		sconn.setRequestProperty("Accept",contentType);
    	}
    	HttpLog+="getHttpConnection() called.\n";
    	if (sconn==null){
    		throw new Exception("sconn is null");    		
    	}    	
    	HttpLog+="sconn is not null: "+sconn.getRequestMethod()+".\n";
    	try{
    		String str=readResponseBodyAsString(sconn,true,url,"");
    		sconn.disconnect();
    		return str;
    	}catch(Exception ex){
    		sconn.disconnect();
    		throw ex;
    	}
    	
    }
    
    public String putDataAndGetResponse(String url,String data,String contentType)
    throws Exception {
        return putDataAndGetResponse(url,data.getBytes(),contentType,data);
    }
    
    public String putDataAndGetResponse(String url,byte[] data,String contentType,String origData)
    throws Exception {
    	
    	int length=data.length;
    	String sLength=String.valueOf(length);
    	
    	Log.d("HttpUtil","Length: "+sLength);

    	HttpURLConnection sconn=getHttpConnection(url,true,true,false);
    	setCommonHeaders(sconn,"PUT",contentType,sLength);
		connection=sconn;
    	
        OutputStream os=null;
    	try{
    		os=sconn.getOutputStream();
    		os.write(data);
    		String str= readResponseBodyAsString(sconn,true,url,origData);
    		os.close();
    		sconn.disconnect();
    		return str;
    	}catch(Exception ex){
    		if (os!=null){
    			try{
    				os.close();
    			}catch(Exception ex2){}
    		}
    		sconn.disconnect();
    		throw ex;
    	}
    }
    
    public String deleteDataAndGetResponse(String url,String data,String contentType)
    throws Exception {
        return deleteDataAndGetResponse(url,data.getBytes(),contentType,data);
    }
    
    public String deleteDataAndGetResponse(String url,byte[] data,String contentType,String origData)
    throws Exception {
    	
    	int length=data.length;
    	String sLength=String.valueOf(length);
    	
    	Log.d("HttpUtil","Length: "+sLength);

    	HttpURLConnection sconn=getHttpConnection(url,true,false,false);
    	setCommonHeaders(sconn,"DELETE",contentType,sLength);
		connection=sconn;
    	
        try{
    		String str= readResponseBodyAsString(sconn,true,url,origData);
    		sconn.disconnect();
    		return str;
    	}catch(Exception ex){
    		sconn.disconnect();
    		throw ex;
    	}
    }

	public String postDataAndGetResponse(String url,Hashtable<String,String> formData,String contentType)
			throws Exception {
		String data = "";
		Enumeration<String> en=formData.keys();
		while(en.hasMoreElements()){
			String objKey=en.nextElement();
			String objVal=formData.get(objKey);
			data+=encodeUTF8Data(objKey,objVal);
			data+="&";
		}
		Log.d("HttpUtil", data);
		return postDataAndGetResponse(url,data,contentType);
	}


	public String postDataAndGetResponse(String url,String data,String contentType)
    throws Exception {
        return postDataAndGetResponse(url,data.getBytes(),contentType,data);
    }
    
    public String postDataAndGetResponse(String url,byte[] data,String contentType,String origData)
    throws Exception {
    	
    	int length=data.length;
    	String sLength=String.valueOf(length);
    	
    	Log.d("HttpUtil","Length: "+sLength);

    	HttpURLConnection sconn=getHttpConnection(url,true,true,false);
    	setCommonHeaders(sconn,"POST",contentType,sLength);
		connection=sconn;
    	OutputStream os=null;
    	try{
    		os=sconn.getOutputStream();
    		os.write(data);
    		String str= readResponseBodyAsString(sconn,true,url,origData);
    		os.close();
    		sconn.disconnect();
    		return str;
    	}catch(Exception ex){
    		if (os!=null){
    			try{
    				os.close();
    			}catch(Exception ex2){}
    		}
    		sconn.disconnect();
    		throw ex;
    	}        
    }
}
