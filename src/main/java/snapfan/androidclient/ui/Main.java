package snapfan.androidclient.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.*;
import net.pickapack.notice.model.news.NewsItem;
import snapfan.androidclient.R;
import snapfan.androidclient.adapter.ListViewNewsAdapter;
import snapfan.androidclient.api.ApiHelper;
import snapfan.androidclient.util.StringUtils;
import snapfan.androidclient.util.UIHelper;
import snapfan.androidclient.widget.PullToRefreshListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Main extends Activity {
    private PullToRefreshListView listViewNews;

    private ListViewNewsAdapter listViewNewsAdapter;

    private List<NewsItem> listViewNewsData = new ArrayList<NewsItem>();

    private Handler listViewNewsHandler;

    private int listViewNewsSumData;

    private View listViewNewsFooter;

    private TextView listViewNewsFootMore;

    private ProgressBar listViewNewsFootProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        this.initFrameListView();
    }

    private void initFrameListView() {
        this.initNewsListView();
        this.initFrameListViewData();
    }

    private void initFrameListViewData() {
        listViewNewsHandler = this.getListViewHandler(listViewNews, listViewNewsAdapter, listViewNewsFootMore, listViewNewsFootProgress, PAGE_SIZE);

        if (listViewNewsData.isEmpty()) {
            loadListViewNewsData(0, listViewNewsHandler, UIHelper.LIST_VIEW_ACTION_INIT);
        }
    }

    private void initNewsListView() {
        listViewNewsAdapter = new ListViewNewsAdapter(this, listViewNewsData, R.layout.news_listitem);
        listViewNewsFooter = getLayoutInflater().inflate(R.layout.listview_footer, null);
        listViewNewsFootMore = (TextView) listViewNewsFooter.findViewById(R.id.listview_foot_more);
        listViewNewsFootProgress = (ProgressBar) listViewNewsFooter.findViewById(R.id.listview_foot_progress);
        listViewNews = (PullToRefreshListView) findViewById(R.id.frame_listview_news);
        listViewNews.addFooterView(listViewNewsFooter);
        listViewNews.setAdapter(listViewNewsAdapter);
        listViewNews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0 || view == listViewNewsFooter) return;

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
        listViewNews.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                listViewNews.onScrollStateChanged(view, scrollState);

                if (listViewNewsData.isEmpty()) return;

                boolean scrollEnd = false;
                try {
                    if (view.getPositionForView(listViewNewsFooter) == view.getLastVisiblePosition())
                        scrollEnd = true;
                } catch (Exception e) {
                    scrollEnd = false;
                }

                int lvDataState = StringUtils.toInt(listViewNews.getTag());
                if (scrollEnd && lvDataState == UIHelper.LIST_VIEW_DATA_MORE) {
                    listViewNews.setTag(UIHelper.LIST_VIEW_DATA_LOADING);
                    listViewNewsFootMore.setText(R.string.load_ing);
                    listViewNewsFootProgress.setVisibility(View.VISIBLE);
                    int pageIndex = listViewNewsSumData / PAGE_SIZE;
                    loadListViewNewsData(pageIndex, listViewNewsHandler, UIHelper.LIST_VIEW_ACTION_SCROLL);
                }
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                listViewNews.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        });
        listViewNews.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
                loadListViewNewsData(0, listViewNewsHandler, UIHelper.LIST_VIEW_ACTION_REFRESH);
            }
        });
    }

    private Handler getListViewHandler(final PullToRefreshListView lv, final BaseAdapter adapter, final TextView more, final ProgressBar progress, final int pageSize) {
        return new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what >= 0) {
                    handleListViewData(msg.what, msg.obj, msg.arg1);

                    if (msg.what < pageSize) {
                        lv.setTag(UIHelper.LIST_VIEW_DATA_FULL);
                        adapter.notifyDataSetChanged();
                        more.setText(R.string.load_full);
                    } else if (msg.what == pageSize) {
                        lv.setTag(UIHelper.LIST_VIEW_DATA_MORE);
                        adapter.notifyDataSetChanged();
                        more.setText(R.string.load_more);
                    }
                } else if (msg.what == -1) {
                    lv.setTag(UIHelper.LIST_VIEW_DATA_MORE);
                    more.setText(R.string.load_error);
                }
                if (adapter.getCount() == 0) {
                    lv.setTag(UIHelper.LIST_VIEW_DATA_EMPTY);
                    more.setText(R.string.load_empty);
                }
                progress.setVisibility(ProgressBar.GONE);
                if (msg.arg1 == UIHelper.LIST_VIEW_ACTION_REFRESH) {
                    lv.onRefreshComplete(getString(R.string.pull_to_refresh_update) + new Date().toLocaleString());
                    lv.setSelection(0);
                } else if (msg.arg1 == UIHelper.LIST_VIEW_ACTION_CHANGE_CATALOG) {
                    lv.onRefreshComplete();
                    lv.setSelection(0);
                }
            }
        };
    }

    private void handleListViewData(int what, Object obj, int actionType) {
        switch (actionType) {
            case UIHelper.LIST_VIEW_ACTION_INIT:
            case UIHelper.LIST_VIEW_ACTION_REFRESH:
            case UIHelper.LIST_VIEW_ACTION_CHANGE_CATALOG:
                NewsItem[] newsItemList = (NewsItem[]) obj;
                listViewNewsSumData = what;
                listViewNewsData.clear();
                listViewNewsData.addAll(Arrays.asList(newsItemList));
                break;
            case UIHelper.LIST_VIEW_ACTION_SCROLL:
                NewsItem[] list = (NewsItem[]) obj;
                listViewNewsSumData += what;
                if (listViewNewsData.size() > 0) {
                    for (NewsItem news1 : list) {
                        boolean b = false;
                        for (NewsItem news2 : listViewNewsData) {
                            if (news1.getId() == news2.getId()) {
                                b = true;
                                break;
                            }
                        }
                        if (!b) listViewNewsData.add(news1);
                    }
                } else {
                    listViewNewsData.addAll(Arrays.asList(list));
                }
                break;
        }
    }

    private void loadListViewNewsData(final int pageIndex, final Handler handler, final int action) {
        new Thread() {
            public void run() {
                Message message = new Message();
                NewsItem[] list = ApiHelper.getNewsItems(pageIndex, PAGE_SIZE);
                if(list == null) {
                    list = new NewsItem[]{};
                }
                message.what = list.length;
                message.obj = list;
                message.arg1 = action;
                handler.sendMessage(message);
            }
        }.start();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    private static final int PAGE_SIZE = 20;
}
