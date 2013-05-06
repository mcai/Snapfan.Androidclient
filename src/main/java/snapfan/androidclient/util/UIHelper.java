package snapfan.androidclient.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import net.pickapack.notice.model.news.NewsItem;
import snapfan.androidclient.ui.Main;
import snapfan.androidclient.ui.NewsDetail;

public class UIHelper {
    public final static int LIST_VIEW_ACTION_INIT = 0x01;
    public final static int LIST_VIEW_ACTION_REFRESH = 0x02;
    public final static int LIST_VIEW_ACTION_SCROLL = 0x03;
    public final static int LIST_VIEW_ACTION_CHANGE_CATALOG = 0x04;

    public final static int LIST_VIEW_DATA_MORE = 0x01;
    public final static int LIST_VIEW_DATA_LOADING = 0x02;
    public final static int LIST_VIEW_DATA_FULL = 0x03;
    public final static int LIST_VIEW_DATA_EMPTY = 0x04;

    public final static String WEB_STYLE = "<style>* {font-size:16px;line-height:20px;} p {color:#333;} a {color:#3E62A6;} img {max-width:310px;} " +
            "img.alignleft {float:left;max-width:120px;margin:0 10px 5px 0;border:1px solid #ccc;background:#fff;padding:2px;} " +
            "pre {font-size:9pt;line-height:12pt;font-family:Courier New,Arial;border:1px solid #ddd;border-left:5px solid #6CE26C;background:#f6f6f6;padding:5px;} " +
            "a.tag {font-size:15px;text-decoration:none;background-color:#bbd6f3;border-bottom:2px solid #3E6D8E;border-right:2px solid #7F9FB6;color:#284a7b;margin:2px 2px 2px 0;padding:2px 4px;white-space:nowrap;}</style>";

    public static void showHome(Activity activity) {
        Intent intent = new Intent(activity, Main.class);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void showNewsDetail(Context context, long newsId) {
        Intent intent = new Intent(context, NewsDetail.class);
        intent.putExtra("news_id", newsId);
        context.startActivity(intent);
    }

    public static void showNewsRedirect(Context context, NewsItem news) {
        showNewsDetail(context, news.getId());
    }

    public static void showUrlRedirect(Context context, String url) {
        URLs urls = URLs.parseURL(url);
        if (urls != null) {
            showLinkRedirect(context, urls.getObjType(), urls.getObjId(), urls.getObjKey());
        } else {
            openBrowser(context, url);
        }
    }

    public static void showLinkRedirect(Context context, int objType, int objId, String objKey) {
        switch (objType) {
            case URLs.URL_OBJ_TYPE_NEWS:
                showNewsDetail(context, objId);
                break;
            case URLs.URL_OBJ_TYPE_OTHER:
                openBrowser(context, objKey);
                break;
        }
    }

    public static void openBrowser(Context context, String url) {
        try {
            Uri uri = Uri.parse(url);
            Intent it = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(it);
        } catch (Exception e) {
            e.printStackTrace();
            ToastMessage(context, "无法浏览此网页", 500);
        }
    }

    public static WebViewClient getWebViewClient() {
        return new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                showUrlRedirect(view.getContext(), url);
                return true;
            }
        };
    }

    public static void ToastMessage(Context cont, int msg) {
        Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
    }

    public static void ToastMessage(Context cont, String msg, int time) {
        Toast.makeText(cont, msg, time).show();
    }
}
