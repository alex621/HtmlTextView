package com.lolapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.loopj.android.http.*;
import com.westkit.htmltextview.HtmlTextView;
import com.westkit.htmltextview.data.DataSupplier;
import com.westkit.htmltextview.data.ImgData;
import com.westkit.htmltextview.defaultadapater.HtmlTextViewDefaultAdapter;

import org.xml.sax.Attributes;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class RecyclerViewActivity extends AppCompatActivity {
    private static final String TAG = "RecyclerViewActivity";

    private MyAdapter adapter;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);

        final RecyclerView list = (RecyclerView) findViewById(R.id.list);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(layoutManager);
        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                final int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                final int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                for (int i = firstVisibleItemPosition; i <= lastVisibleItemPosition; ++i) {
                    MyViewHolder holder = (MyViewHolder) list.findViewHolderForAdapterPosition(i);
                    holder.content.recycleCheck();
                }
            }
        });

        adapter = new MyAdapter();
        list.setAdapter(adapter);

        String url = "https://api.tumblr.com/v2/blog/peacecorps.tumblr.com/posts/text?api_key=fuiKNFp9vQFvjLNvx4sUwti4Yb5yGutBN4Xh10LXZhhRKjWlV4&notes_info=true";

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Gson gson = new Gson();
                Data data = gson.fromJson(responseString, Data.class);

                ArrayList<Post> posts = data.response.posts;

                adapter.setData(posts);

                adapter.notifyDataSetChanged();
            }
        });

    }

    public static class MyAdapter extends RecyclerView.Adapter<MyViewHolder>{
        public ArrayList<Post> data = new ArrayList<>();

        public void setData(ArrayList<Post> data){
            this.data.addAll(data);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_view_item, parent, false);

            MyViewHolder vh = new MyViewHolder(v);

            return vh;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            Post post = data.get(position);

            holder.title.setText(post.title);
            holder.content.setHtml(post.body);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView title;
        public HtmlTextView content;

        public MyViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.title);
            content = (HtmlTextView) itemView.findViewById(R.id.content);

            content.setAdapter(new HtmlTextViewDefaultAdapter());
            content.setDataSupplier(new DataSupplier() {
                @Override
                public ImgData getImgData(String src, Attributes attributes) {
                    int width = Integer.parseInt(attributes.getValue("", "data-orig-width"));
                    int height = Integer.parseInt(attributes.getValue("", "data-orig-height"));
                    return new ImgData(width, height);
                }
            });
        }
    }

    public static class Data{
        public Response response;
    }

    public static class Response{
        public ArrayList<Post> posts;
    }

    public static class Post{
        public String body;
        public String title;
        public String type;
    }
}
