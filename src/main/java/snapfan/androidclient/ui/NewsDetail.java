package snapfan.androidclient.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.*;
import net.pickapack.notice.model.news.NewsItem;
import snapfan.androidclient.R;
import snapfan.androidclient.api.ApiHelper;
import snapfan.androidclient.util.StringHelper;
import snapfan.androidclient.util.UIHelper;

public class NewsDetail extends Activity {
    private FrameLayout footer;
    private ImageView home;
    private ImageView refresh;
    private ProgressBar progressBar;
    private ScrollView scrollView;

    private TextView title;
    private TextView author;
    private TextView pubDate;

    private WebView webView;
    private Handler handler;
    private NewsItem newsItem;
    private long newsId;

    private final static int DATA_LOAD_ING = 0x001;
    private final static int DATA_LOAD_COMPLETE = 0x002;
    private final static int DATA_LOAD_FAIL = 0x003;

    private GestureDetector gestureDetector;
    private boolean fullScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_detail);

        this.initView();
        this.initData();

        this.registerOnDoubleTapEvent();
    }

    private void initView() {
        newsId = getIntent().getLongExtra("news_id", 0);

        footer = (FrameLayout) findViewById(R.id.news_detail_footer);
        home = (ImageView) findViewById(R.id.news_detail_home);
        refresh = (ImageView) findViewById(R.id.news_detail_refresh);
        progressBar = (ProgressBar) findViewById(R.id.news_detail_footer_progress);
        scrollView = (ScrollView) findViewById(R.id.news_detail_scrollview);

        title = (TextView) findViewById(R.id.news_detail_title);
        author = (TextView) findViewById(R.id.news_detail_author);
        pubDate = (TextView) findViewById(R.id.news_detail_date);

        webView = (WebView) findViewById(R.id.news_detail_webview);
        webView.getSettings().setJavaScriptEnabled(false);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDefaultFontSize(15);

        home.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                UIHelper.gotoHome(NewsDetail.this);
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                initData(newsId);
            }
        });
    }

    private void initData() {
        handler = new Handler() {
            public void handleMessage(Message message) {
                if (message.what == 1) {
                    headButtonSwitch(DATA_LOAD_COMPLETE);

                    title.setText(newsItem.getTitle());
                    author.setText(newsItem.getAuthor());
                    pubDate.setText(StringHelper.getFriendlyTime(newsItem.getCreateTime()));

                    String body = UIHelper.WEB_STYLE + newsItem.getBody();
                    boolean isLoadImage = true;
                    if (isLoadImage) {
                        //过滤掉 img标签的width,height属性
                        body = body.replaceAll("(<img[^>]*?)\\s+width\\s*=\\s*\\S+", "$1");
                        body = body.replaceAll("(<img[^>]*?)\\s+height\\s*=\\s*\\S+", "$1");
                    } else {
                        //过滤掉 img标签
                        body = body.replaceAll("<\\s*img\\s+([^>]*)\\s*>", "");
                    }

                    body += "<div style='margin-bottom: 80px'/>";

                    webView.loadDataWithBaseURL(null, body, "text/html", "utf-8", null);
                    webView.setWebViewClient(UIHelper.getWebViewClient());
                } else if (message.what == 0) {
                    headButtonSwitch(DATA_LOAD_FAIL);

                    UIHelper.showToastMessage(NewsDetail.this, R.string.msg_load_is_null);
                } else if (message.what == -1 && message.obj != null) {
                    headButtonSwitch(DATA_LOAD_FAIL);
                }
            }
        };

        initData(newsId);
    }

    private void initData(final long newsId) {
        headButtonSwitch(DATA_LOAD_ING);

        new Thread() {
            public void run() {
                Message message = new Message();
                newsItem = ApiHelper.getNewsItem(newsId);
                message.what = (newsItem != null && newsItem.getId() > 0) ? 1 : 0;
                message.obj = null;
                handler.sendMessage(message);
            }
        }.start();
    }

    private void headButtonSwitch(int type) {
        switch (type) {
            case DATA_LOAD_ING:
                scrollView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                refresh.setVisibility(View.GONE);
                break;
            case DATA_LOAD_COMPLETE:
                scrollView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                refresh.setVisibility(View.VISIBLE);
                break;
            case DATA_LOAD_FAIL:
                scrollView.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                refresh.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void registerOnDoubleTapEvent() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                fullScreen = !fullScreen;
                if (!fullScreen) {
                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    getWindow().setAttributes(params);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                    footer.setVisibility(View.VISIBLE);
                } else {
                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    getWindow().setAttributes(params);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                    footer.setVisibility(View.GONE);
                }
                return true;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }
}
