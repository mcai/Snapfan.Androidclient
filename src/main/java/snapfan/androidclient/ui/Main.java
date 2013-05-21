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
import snapfan.androidclient.util.StringHelper;
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
        this.listViewNewsHandler = this.getListViewHandler(this.listViewNews, this.listViewNewsAdapter, this.listViewNewsFootMore, this.listViewNewsFootProgress, PAGE_SIZE);

        if (this.listViewNewsData.isEmpty()) {
            loadListViewNewsData(0, this.listViewNewsHandler, UIHelper.LIST_VIEW_ACTION_INIT);
        }
    }

    private void initNewsListView() {
        this.listViewNewsAdapter = new ListViewNewsAdapter(this, this.listViewNewsData, R.layout.news_listitem);
        this.listViewNewsFooter = getLayoutInflater().inflate(R.layout.listview_footer, null);
        this.listViewNewsFootMore = (TextView) this.listViewNewsFooter.findViewById(R.id.listview_foot_more);
        this.listViewNewsFootProgress = (ProgressBar) this.listViewNewsFooter.findViewById(R.id.listview_foot_progress);
        this.listViewNews = (PullToRefreshListView) findViewById(R.id.frame_listview_news);
        this.listViewNews.addFooterView(this.listViewNewsFooter);
        this.listViewNews.setAdapter(this.listViewNewsAdapter);
        this.listViewNews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0 || view == listViewNewsFooter) return;

                NewsItem newsItem;
                if (view instanceof TextView) {
                    newsItem = (NewsItem) view.getTag();
                } else {
                    TextView tv = (TextView) view.findViewById(R.id.news_listitem_title);
                    newsItem = (NewsItem) tv.getTag();
                }
                if (newsItem == null) return;

                //TODO
                if(newsItem.getAttachmentUrls().isEmpty()) {
                    UIHelper.gotoNewsDetail(view.getContext(), newsItem);
                }
                else {
                    UIHelper.gotoImageGallery(view.getContext(), newsItem);
                }
            }
        });
        this.listViewNews.setOnScrollListener(new AbsListView.OnScrollListener() {
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

                int lvDataState = StringHelper.toInteger(listViewNews.getTag());
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
        this.listViewNews.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
                loadListViewNewsData(0, listViewNewsHandler, UIHelper.LIST_VIEW_ACTION_REFRESH);
            }
        });
    }

    private Handler getListViewHandler(final PullToRefreshListView listView, final BaseAdapter adapter, final TextView more, final ProgressBar progress, final int pageSize) {
        return new Handler() {
            public void handleMessage(Message message) {
                if (message.what >= 0) {
                    handleListViewData(message.what, message.obj, message.arg1);

                    if (message.what < pageSize) {
                        listView.setTag(UIHelper.LIST_VIEW_DATA_FULL);
                        adapter.notifyDataSetChanged();
                        more.setText(R.string.load_full);
                    } else if (message.what == pageSize) {
                        listView.setTag(UIHelper.LIST_VIEW_DATA_MORE);
                        adapter.notifyDataSetChanged();
                        more.setText(R.string.load_more);
                    }
                } else if (message.what == -1) {
                    listView.setTag(UIHelper.LIST_VIEW_DATA_MORE);
                    more.setText(R.string.load_error);
                }
                if (adapter.getCount() == 0) {
                    listView.setTag(UIHelper.LIST_VIEW_DATA_EMPTY);
                    more.setText(R.string.load_empty);
                }
                progress.setVisibility(ProgressBar.GONE);
                if (message.arg1 == UIHelper.LIST_VIEW_ACTION_REFRESH) {
                    listView.onRefreshComplete(getString(R.string.pull_to_refresh_update) + new Date().toLocaleString());
                    listView.setSelection(0);
                }
            }
        };
    }

    private void handleListViewData(int what, Object obj, int actionType) {
        switch (actionType) {
            case UIHelper.LIST_VIEW_ACTION_INIT:
            case UIHelper.LIST_VIEW_ACTION_REFRESH:
                NewsItem[] newsItems = (NewsItem[]) obj;
                listViewNewsSumData = what;
                listViewNewsData.clear();
                listViewNewsData.addAll(Arrays.asList(newsItems));
                break;
            case UIHelper.LIST_VIEW_ACTION_SCROLL:
                NewsItem[] list = (NewsItem[]) obj;
                listViewNewsSumData += what;
                if (listViewNewsData.size() > 0) {
                    for (NewsItem newsItem : list) {
                        boolean b = false;
                        for (NewsItem news2 : listViewNewsData) {
                            if (newsItem.getId() == news2.getId()) {
                                b = true;
                                break;
                            }
                        }
                        if (!b) listViewNewsData.add(newsItem);
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
                NewsItem[] newsItems = ApiHelper.getNewsItems(pageIndex, PAGE_SIZE);
                if(newsItems == null) {
                    newsItems = new NewsItem[]{};
                }
                message.what = newsItems.length;
                message.obj = newsItems;
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
