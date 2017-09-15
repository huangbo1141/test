package tepia.smartcycle;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Hashtable;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.usmani.android.*;

/**
 * Created by Rameez Usmani on 12/7/2015.
 */
public class SabWebServer {

    private static final String TAG=SabWebServer.class.getName();

    private static final String SERVER_URL="http://testapp.fan4dry.com/";//testapp.php";

    public static String sendQCommand(String qCommand)
            throws Exception {
        Log.d(TAG, "sendSetupData");
        String url=SERVER_URL+"testapp.php";
        Hashtable<String,String> fData=new Hashtable<String,String>();
        fData.put("q",qCommand);
        HttpUtil httpUtil=new HttpUtil();
        String body=httpUtil.postDataAndGetResponse(url,fData,HttpUtil.DEFAULT_POST_CONTENT_TYPE);
        Log.d(TAG,"body: "+body);
        return body;
    }
}
