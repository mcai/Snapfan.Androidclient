package snapfan.androidclient.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.*;
import net.pickapack.notice.model.news.NewsItem;
import snapfan.androidclient.R;
import snapfan.androidclient.SnapfanApplication;
import snapfan.androidclient.adapter.ListViewNewsAdapter;
import snapfan.androidclient.api.ApiHelper;
import snapfan.androidclient.util.StringUtils;
import snapfan.androidclient.util.UIHelper;
import snapfan.androidclient.widget.NewDataToast;
import snapfan.androidclient.widget.PullToRefreshListView;
import snapfan.androidclient.widget.ScrollLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Main extends Activity {
    private ScrollLayout mScrollLayout;
    private RadioButton[] mButtons;
    private String[] mHeadTitles;
    private int mViewCount;
    private int mCurSel;

    private ImageView mHeadLogo;
    private TextView mHeadTitle;
    private ProgressBar mHeadProgress;

    private PullToRefreshListView lvNews;

    private ListViewNewsAdapter lvNewsAdapter;

    private List<NewsItem> lvNewsData = new ArrayList<NewsItem>();

    private Handler lvNewsHandler;

    private int lvNewsSumData;

    private RadioButton fbNews;

    private Button framebtn_News_lastest;

    private View lvNews_footer;

    private TextView lvNews_foot_more;

    private ProgressBar lvNews_foot_progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        this.initHeadView();
        this.initFootBar();
        this.initPageScroll();
        this.initFrameButton();
        this.initFrameListView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mViewCount == 0) mViewCount = 1;
        if (mCurSel == 0 && !fbNews.isChecked()) {
            fbNews.setChecked(true);
        }
        mScrollLayout.setIsScroll(false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getBooleanExtra("NOTICE", false)) {
            mScrollLayout.scrollToScreen(3);
        }
    }

    private void initFrameListView() {
        this.initNewsListView();
        this.initFrameListViewData();
    }

    private void initFrameListViewData() {
        lvNewsHandler = this.getListViewHandler(lvNews, lvNewsAdapter, lvNews_foot_more, lvNews_foot_progress, SnapfanApplication.PAGE_SIZE);

        if (lvNewsData.isEmpty()) {
            loadListViewNewsData(0, lvNewsHandler, UIHelper.LISTVIEW_ACTION_INIT);
        }
    }

    private void initNewsListView() {
        lvNewsAdapter = new ListViewNewsAdapter(this, lvNewsData, R.layout.news_listitem);
        lvNews_footer = getLayoutInflater().inflate(R.layout.listview_footer, null);
        lvNews_foot_more = (TextView) lvNews_footer.findViewById(R.id.listview_foot_more);
        lvNews_foot_progress = (ProgressBar) lvNews_footer.findViewById(R.id.listview_foot_progress);
        lvNews = (PullToRefreshListView) findViewById(R.id.frame_listview_news);
        lvNews.addFooterView(lvNews_footer);
        lvNews.setAdapter(lvNewsAdapter);
        lvNews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0 || view == lvNews_footer) return;

                NewsItem news = null;
                if (view instanceof TextView) {
                    news = (NewsItem) view.getTag();
                } else {
                    TextView tv = (TextView) view.findViewById(R.id.news_listitem_title);
                    news = (NewsItem) tv.getTag();
                }
                if (news == null) return;

                UIHelper.showNewsRedirect(view.getContext(), news);
            }
        });
        lvNews.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                lvNews.onScrollStateChanged(view, scrollState);

                if (lvNewsData.isEmpty()) return;

                boolean scrollEnd = false;
                try {
                    if (view.getPositionForView(lvNews_footer) == view.getLastVisiblePosition())
                        scrollEnd = true;
                } catch (Exception e) {
                    scrollEnd = false;
                }

                int lvDataState = StringUtils.toInt(lvNews.getTag());
                if (scrollEnd && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
                    lvNews.setTag(UIHelper.LISTVIEW_DATA_LOADING);
                    lvNews_foot_more.setText(R.string.load_ing);
                    lvNews_foot_progress.setVisibility(View.VISIBLE);
                    int pageIndex = lvNewsSumData / SnapfanApplication.PAGE_SIZE;
                    loadListViewNewsData(pageIndex, lvNewsHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
                }
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lvNews.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        });
        lvNews.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
                loadListViewNewsData(0, lvNewsHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
            }
        });
    }

    private void initHeadView() {
        mHeadLogo = (ImageView) findViewById(R.id.main_head_logo);
        mHeadTitle = (TextView) findViewById(R.id.main_head_title);
        mHeadProgress = (ProgressBar) findViewById(R.id.main_head_progress);
    }

    private void initFootBar() {
        fbNews = (RadioButton) findViewById(R.id.main_footbar_news);
    }

    private void initPageScroll() {
        mScrollLayout = (ScrollLayout) findViewById(R.id.main_scrolllayout);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.main_linearlayout_footer);
        mHeadTitles = getResources().getStringArray(R.array.head_titles);
        mViewCount = mScrollLayout.getChildCount();
        mButtons = new RadioButton[mViewCount];

        for (int i = 0; i < mViewCount; i++) {
            mButtons[i] = (RadioButton) linearLayout.getChildAt(i * 2);
            mButtons[i].setTag(i);
            mButtons[i].setChecked(false);
            mButtons[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int pos = (Integer) (v.getTag());
                    if (mCurSel == pos) {
                        switch (pos) {
                            case 0:
                                lvNews.clickRefresh();
                                break;
                        }
                    }
                    mScrollLayout.snapToScreen(pos);
                }
            });
        }

        mCurSel = 0;
        mButtons[mCurSel].setChecked(true);

        mScrollLayout.SetOnViewChangeListener(new ScrollLayout.OnViewChangeListener() {
            public void OnViewChange(int viewIndex) {
                switch (viewIndex) {
                    case 0://资讯
                        if (lvNews.getVisibility() == View.VISIBLE) {
                            if (lvNewsData.isEmpty()) {
                                loadListViewNewsData(0, lvNewsHandler, UIHelper.LISTVIEW_ACTION_INIT);
                            }
                        }
                        break;
                }
                setCurPoint(viewIndex);
            }
        });
    }

    private void setCurPoint(int index) {
        if (index < 0 || index > mViewCount - 1 || mCurSel == index)
            return;

        mButtons[mCurSel].setChecked(false);
        mButtons[index].setChecked(true);
        mHeadTitle.setText(mHeadTitles[index]);
        mCurSel = index;
    }

    private void initFrameButton() {
        framebtn_News_lastest = (Button) findViewById(R.id.frame_btn_news_lastest);
        framebtn_News_lastest.setEnabled(false);
        framebtn_News_lastest.setOnClickListener(frameNewsBtnClick(framebtn_News_lastest));
    }

    private View.OnClickListener frameNewsBtnClick(final Button btn) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                framebtn_News_lastest.setEnabled(btn != framebtn_News_lastest);

                if (btn == framebtn_News_lastest) {
                    lvNews.setVisibility(View.VISIBLE);
                    loadListViewNewsData(0, lvNewsHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
                }
            }
        };
    }

    private Handler getListViewHandler(final PullToRefreshListView lv, final BaseAdapter adapter, final TextView more, final ProgressBar progress, final int pageSize) {
        return new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what >= 0) {
                    handleListViewData(msg.what, msg.obj, msg.arg1);

                    if (msg.what < pageSize) {
                        lv.setTag(UIHelper.LISTVIEW_DATA_FULL);
                        adapter.notifyDataSetChanged();
                        more.setText(R.string.load_full);
                    } else if (msg.what == pageSize) {
                        lv.setTag(UIHelper.LISTVIEW_DATA_MORE);
                        adapter.notifyDataSetChanged();
                        more.setText(R.string.load_more);
                    }
                } else if (msg.what == -1) {
                    lv.setTag(UIHelper.LISTVIEW_DATA_MORE);
                    more.setText(R.string.load_error);
                }
                if (adapter.getCount() == 0) {
                    lv.setTag(UIHelper.LISTVIEW_DATA_EMPTY);
                    more.setText(R.string.load_empty);
                }
                progress.setVisibility(ProgressBar.GONE);
                mHeadProgress.setVisibility(ProgressBar.GONE);
                if (msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH) {
                    lv.onRefreshComplete(getString(R.string.pull_to_refresh_update) + new Date().toLocaleString());
                    lv.setSelection(0);
                } else if (msg.arg1 == UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG) {
                    lv.onRefreshComplete();
                    lv.setSelection(0);
                }
            }
        };
    }

    private void handleListViewData(int what, Object obj, int actionType) {
        switch (actionType) {
            case UIHelper.LISTVIEW_ACTION_INIT:
            case UIHelper.LISTVIEW_ACTION_REFRESH:
            case UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG:
                int newdata = 0;//新加载数据-只有刷新动作才会使用到
                NewsItem[] nlist = (NewsItem[]) obj;
                lvNewsSumData = what;
                if (actionType == UIHelper.LISTVIEW_ACTION_REFRESH) {
                    if (lvNewsData.size() > 0) {
                        for (NewsItem news1 : nlist) {
                            boolean b = false;
                            for (NewsItem news2 : lvNewsData) {
                                if (news1.getId() == news2.getId()) {
                                    b = true;
                                    break;
                                }
                            }
                            if (!b) newdata++;
                        }
                    } else {
                        newdata = what;
                    }
                }
                lvNewsData.clear();
                lvNewsData.addAll(Arrays.asList(nlist));
                if (actionType == UIHelper.LISTVIEW_ACTION_REFRESH) {
                    if (newdata > 0) {
                        NewDataToast.makeText(this, getString(R.string.new_data_toast_message, newdata), true).show();
                    } else {
                        NewDataToast.makeText(this, getString(R.string.new_data_toast_none), false).show();
                    }
                }
                break;
            case UIHelper.LISTVIEW_ACTION_SCROLL:
                NewsItem[] list = (NewsItem[]) obj;
                lvNewsSumData += what;
                if (lvNewsData.size() > 0) {
                    for (NewsItem news1 : list) {
                        boolean b = false;
                        for (NewsItem news2 : lvNewsData) {
                            if (news1.getId() == news2.getId()) {
                                b = true;
                                break;
                            }
                        }
                        if (!b) lvNewsData.add(news1);
                    }
                } else {
                    lvNewsData.addAll(Arrays.asList(list));
                }
                break;
        }
    }

    private void loadListViewNewsData(final int pageIndex, final Handler handler, final int action) {
        mHeadProgress.setVisibility(ProgressBar.VISIBLE);
        new Thread() {
            public void run() {
                Message msg = new Message();
                NewsItem[] list = ApiHelper.getNewsItems(pageIndex);
                if(list == null) {
                    list = new NewsItem[]{};
                }
                msg.what = list.length;
                msg.obj = list;
                msg.arg1 = action;
                handler.sendMessage(msg);
            }
        }.start();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
}
