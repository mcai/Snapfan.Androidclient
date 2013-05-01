package snapfan.androidclient.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.*;
import net.pickapack.notice.model.news.NewsItem;
import snapfan.androidclient.R;
import snapfan.androidclient.api.ApiHelper;
import snapfan.androidclient.util.StringUtils;
import snapfan.androidclient.util.UIHelper;

public class NewsDetail extends Activity {
    private FrameLayout mHeader;
    private LinearLayout mFooter;
    private ImageView mHome;
    private ImageView mRefresh;
    private TextView mHeadTitle;
    private ProgressBar mProgressbar;
    private ScrollView mScrollView;
    private ViewSwitcher mViewSwitcher;

    private ImageView mDetail;

    private TextView mTitle;
    private TextView mAuthor;
    private TextView mPubDate;

    private WebView mWebView;
    private Handler mHandler;
    private NewsItem newsDetail;
    private long newsId;

    private final static int VIEWSWITCH_TYPE_DETAIL = 0x001;
    private final static int VIEWSWITCH_TYPE_COMMENTS = 0x002;

    private final static int DATA_LOAD_ING = 0x001;
    private final static int DATA_LOAD_COMPLETE = 0x002;
    private final static int DATA_LOAD_FAIL = 0x003;

    private ViewSwitcher mFootViewSwitcher;
    private ImageView mFootEditebox;
    private EditText mFootEditer;
    private InputMethodManager imm;

    private GestureDetector gd;
    private boolean isFullScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_detail);

        this.initView();
        this.initData();

        this.regOnDoubleEvent();
    }

    private void initView() {
        newsId = getIntent().getLongExtra("news_id", 0);

        mHeader = (FrameLayout) findViewById(R.id.news_detail_header);
        mFooter = (LinearLayout) findViewById(R.id.news_detail_footer);
        mHome = (ImageView) findViewById(R.id.news_detail_home);
        mRefresh = (ImageView) findViewById(R.id.news_detail_refresh);
        mHeadTitle = (TextView) findViewById(R.id.news_detail_head_title);
        mProgressbar = (ProgressBar) findViewById(R.id.news_detail_head_progress);
        mViewSwitcher = (ViewSwitcher) findViewById(R.id.news_detail_viewswitcher);
        mScrollView = (ScrollView) findViewById(R.id.news_detail_scrollview);

        mDetail = (ImageView) findViewById(R.id.news_detail_footbar_detail);

        mTitle = (TextView) findViewById(R.id.news_detail_title);
        mAuthor = (TextView) findViewById(R.id.news_detail_author);
        mPubDate = (TextView) findViewById(R.id.news_detail_date);

        mDetail.setEnabled(false);

        mWebView = (WebView) findViewById(R.id.news_detail_webview);
        mWebView.getSettings().setJavaScriptEnabled(false);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDefaultFontSize(15);

        mHome.setOnClickListener(homeClickListener);
        mRefresh.setOnClickListener(refreshClickListener);
        mDetail.setOnClickListener(detailClickListener);

        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        mFootViewSwitcher = (ViewSwitcher) findViewById(R.id.news_detail_foot_viewswitcher);
        mFootEditebox = (ImageView) findViewById(R.id.news_detail_footbar_editebox);
        mFootEditebox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mFootViewSwitcher.showNext();
                mFootEditer.setVisibility(View.VISIBLE);
                mFootEditer.requestFocus();
                mFootEditer.requestFocusFromTouch();
            }
        });
        mFootEditer = (EditText) findViewById(R.id.news_detail_foot_editer);
        mFootEditer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    imm.showSoftInput(v, 0);
                } else {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
        mFootEditer.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mFootViewSwitcher.getDisplayedChild() == 1) {
                        mFootViewSwitcher.setDisplayedChild(0);
                        mFootEditer.clearFocus();
                        mFootEditer.setVisibility(View.GONE);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void initData() {
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    headButtonSwitch(DATA_LOAD_COMPLETE);

                    mTitle.setText(newsDetail.getTitle());
                    mAuthor.setText(newsDetail.getAuthor());
                    mPubDate.setText(StringUtils.friendly_time(newsDetail.getCreateTime()));

                    String body = UIHelper.WEB_STYLE + newsDetail.getBody();
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

                    mWebView.loadDataWithBaseURL(null, body, "text/html", "utf-8", null);
                    mWebView.setWebViewClient(UIHelper.getWebViewClient());
                } else if (msg.what == 0) {
                    headButtonSwitch(DATA_LOAD_FAIL);

                    UIHelper.ToastMessage(NewsDetail.this, R.string.msg_load_is_null);
                } else if (msg.what == -1 && msg.obj != null) {
                    headButtonSwitch(DATA_LOAD_FAIL);
                }
            }
        };

        initData(newsId);
    }

    private void initData(final long news_id) {
        headButtonSwitch(DATA_LOAD_ING);

        new Thread() {
            public void run() {
                Message msg = new Message();
                newsDetail = ApiHelper.getNewsItem(news_id);
                msg.what = (newsDetail != null && newsDetail.getId() > 0) ? 1 : 0;
                msg.obj = null;//通知信息
                mHandler.sendMessage(msg);
            }
        }.start();
    }

    private void viewSwitch(int type) {
        switch (type) {
            case VIEWSWITCH_TYPE_DETAIL:
                mDetail.setEnabled(false);
                mHeadTitle.setText(R.string.news_detail_head_title);
                mViewSwitcher.setDisplayedChild(0);
                break;
            case VIEWSWITCH_TYPE_COMMENTS:
                mDetail.setEnabled(true);
                mHeadTitle.setText(R.string.comment_list_head_title);
                mViewSwitcher.setDisplayedChild(1);
                break;
        }
    }

    private void headButtonSwitch(int type) {
        switch (type) {
            case DATA_LOAD_ING:
                mScrollView.setVisibility(View.GONE);
                mProgressbar.setVisibility(View.VISIBLE);
                mRefresh.setVisibility(View.GONE);
                break;
            case DATA_LOAD_COMPLETE:
                mScrollView.setVisibility(View.VISIBLE);
                mProgressbar.setVisibility(View.GONE);
                mRefresh.setVisibility(View.VISIBLE);
                break;
            case DATA_LOAD_FAIL:
                mScrollView.setVisibility(View.GONE);
                mProgressbar.setVisibility(View.GONE);
                mRefresh.setVisibility(View.VISIBLE);
                break;
        }
    }

    private View.OnClickListener homeClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            UIHelper.showHome(NewsDetail.this);
        }
    };

    private View.OnClickListener refreshClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            initData(newsId);
        }
    };

    private View.OnClickListener detailClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (newsId == 0) {
                return;
            }
            viewSwitch(VIEWSWITCH_TYPE_DETAIL);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        if (data == null) return;

        viewSwitch(VIEWSWITCH_TYPE_COMMENTS);
    }

    private void regOnDoubleEvent() {
        gd = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                isFullScreen = !isFullScreen;
                if (!isFullScreen) {
                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    getWindow().setAttributes(params);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                    mHeader.setVisibility(View.VISIBLE);
                    mFooter.setVisibility(View.VISIBLE);
                } else {
                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    getWindow().setAttributes(params);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                    mHeader.setVisibility(View.GONE);
                    mFooter.setVisibility(View.GONE);
                }
                return true;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        gd.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }
}
