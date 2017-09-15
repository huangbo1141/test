package tepia.smartcycle;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class StoreActivity extends CommonActivity {

    private WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        SabAppHelper.setHeaderTitle(getString(R.string.store),this);
        webview=(WebView)findViewById(R.id.webview);
        webview.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                // do your handling codes here, which url is the requested url
                // probably you need to open that url rather than redirect:
                view.loadUrl(url);
                return false; // then it is not handled by default action
            }
        });
        webview.loadUrl("https://acuasec.com");
    }
}
