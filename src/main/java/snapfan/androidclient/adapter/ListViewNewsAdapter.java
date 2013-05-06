package snapfan.androidclient.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import net.pickapack.notice.model.news.NewsItem;
import snapfan.androidclient.R;
import snapfan.androidclient.util.StringUtils;

import java.util.List;

public class ListViewNewsAdapter extends BaseAdapter {
    private List<NewsItem> listItems;
    private LayoutInflater listContainer;
    private int itemViewResource;

    static class ListItemView {
        public TextView title;
        public TextView author;
        public TextView date;
        public TextView count;
        public ImageView flag;
    }

    public ListViewNewsAdapter(Context context, List<NewsItem> data, int resource) {
        this.listContainer = LayoutInflater.from(context);
        this.itemViewResource = resource;
        this.listItems = data;
    }

    public int getCount() {
        return listItems.size();
    }

    public Object getItem(int arg0) {
        return null;
    }

    public long getItemId(int arg0) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ListItemView listItemView;

        if (convertView == null) {
            convertView = listContainer.inflate(this.itemViewResource, null);

            listItemView = new ListItemView();
            listItemView.title = (TextView) convertView.findViewById(R.id.news_listitem_title);
            listItemView.author = (TextView) convertView.findViewById(R.id.news_listitem_author);
            listItemView.count = (TextView) convertView.findViewById(R.id.news_listitem_commentCount);
            listItemView.date = (TextView) convertView.findViewById(R.id.news_listitem_date);
            listItemView.flag = (ImageView) convertView.findViewById(R.id.news_listitem_flag);

            convertView.setTag(listItemView);
        } else {
            listItemView = (ListItemView) convertView.getTag();
        }

        NewsItem newsItem = listItems.get(position);

        listItemView.title.setText(newsItem.getTitle());
        listItemView.title.setTag(newsItem);
        listItemView.author.setText(newsItem.getAuthor());
        listItemView.date.setText(StringUtils.friendly_time(newsItem.getCreateTime()));
        listItemView.count.setText("<N/A>");
        listItemView.flag.setVisibility(StringUtils.isToday(newsItem.getCreateTime()) ? View.VISIBLE : View.GONE);

        return convertView;
    }
}