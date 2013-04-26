package snapfan.androidclient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import net.pickapack.model.ModelElement;
import net.pickapack.notice.model.forum.Forum;
import net.pickapack.notice.model.forum.ForumThread;
import net.pickapack.notice.model.forum.ForumThreadMessage;
import snapfan.androidclient.util.ApiHelper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MainListActivity extends SherlockListActivity {
    private PullToRefreshListView pullToRefreshView;
    private List<ModelElement> items;
    private ArrayAdapter<ModelElement> adapter;
    private ModelElement selectedModelElement;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        pullToRefreshView = (PullToRefreshListView) findViewById(R.id.pull_to_refresh_listview);

        pullToRefreshView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                new GetDataTask().execute();
            }
        });

        pullToRefreshView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                Toast.makeText(MainListActivity.this, "End of List!", Toast.LENGTH_SHORT).show();
            }
        });

        items = new LinkedList<ModelElement>();

        gotoElement(selectedModelElement);

        adapter = new ArrayAdapter<ModelElement>(this, R.layout.main_list_view_item, R.id.label, items);
        pullToRefreshView.getRefreshableView().setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_account:
                Toast.makeText(MainListActivity.this, "Android Menu example", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_item_preference:
                Toast.makeText(MainListActivity.this, "Basic Settings is Selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_item_about:
                Toast.makeText(MainListActivity.this, "Android Menu example", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ModelElement element = (ModelElement) l.getItemAtPosition(position);
        gotoElement(element);
        adapter.notifyDataSetChanged();
        super.onListItemClick(l, v, position, id);
    }

    private synchronized void gotoElement(ModelElement element) {
        if(element == null) {
            Forum[] forums = ApiHelper.getAllForums();
            if (forums != null) {
                items.clear();
                items.addAll(Arrays.asList(forums));
            }
            setSelectedModelElement(element);
        }
        else if(element instanceof Forum) {
            Forum selectedForum = (Forum) element;
            ForumThread[] forumThreads = ApiHelper.getForumThreadsByParentId(selectedForum.getId());
            if (forumThreads != null) {
                items.clear();
                items.addAll(Arrays.asList(forumThreads));
                setSelectedModelElement(element);
            }
        }
        else if(element instanceof ForumThread) {
            ForumThread selectedForumThread = (ForumThread) element;
            ForumThreadMessage[] forumThreadMessages = ApiHelper.getForumThreadMessagesByParentId(selectedForumThread.getId());
            if (forumThreadMessages != null) {
                items.clear();
                items.addAll(Arrays.asList(forumThreadMessages));
                setSelectedModelElement(element);
            }
        }
        else if(element instanceof ForumThreadMessage) {
            //TODO
        }
    }

    @Override
    public void onBackPressed() {
        if(selectedModelElement != null) {
            if(selectedModelElement instanceof Forum) {
                gotoElement(ApiHelper.getParentByForumId(selectedModelElement.getId()));
            }
            else if(selectedModelElement instanceof ForumThread) {
                gotoElement(ApiHelper.getParentByForumThreadId(selectedModelElement.getId()));
            }
            else if(selectedModelElement instanceof ForumThreadMessage) {
                gotoElement(ApiHelper.getParentByForumThreadMessageId(selectedModelElement.getId()));
            }

            adapter.notifyDataSetChanged();
            return;
        }

        super.onBackPressed();
    }

    private void setSelectedModelElement(ModelElement element) {
        this.selectedModelElement = element;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitle(selectedModelElement == null ? getString(R.string.app_name) : selectedModelElement + "");
            }
        });
    }

    private class GetDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            gotoElement(selectedModelElement);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            adapter.notifyDataSetChanged();
            pullToRefreshView.onRefreshComplete();
            super.onPostExecute(result);
        }
    }
}