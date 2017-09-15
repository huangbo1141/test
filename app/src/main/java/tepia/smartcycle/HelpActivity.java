package tepia.smartcycle;

import android.os.Bundle;
import android.webkit.WebView;

public class HelpActivity extends CommonActivity {

    private WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        SabAppHelper.setHeaderTitle(getString(R.string.help),this);
        webview=(WebView)findViewById(R.id.webview);
        webview.loadUrl("file:///android_asset/local.html");
    }
}
