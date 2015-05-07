package singh.saurabh.godogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import singh.FeedParser.Message;
import singh.FeedParser.MessageList;

/**
 * Created by ${SAURBAH} on ${10/29/14}.
 */
public class News {

    private Activity mContext;

    // Variables for News Class
    private List<Message> messages;
    private ArrayList<HashMap<String, String>> mNewsList = new ArrayList<HashMap<String, String>>();
    private MessageList mMessageObject = new MessageList();
    private static final String NEWS_TITLE = "title";
    private static final String NEWS_DATE = "date";

    // Progress Dialog
    private ProgressDialog ppDialog;

    public News(Activity context) {
        this.mContext = context;
    }

    public void startLoadNewsTask() {
        new LoadNews().execute();
    }

    /**
     * Retrieves recent news from the fresno state rss feed url.
     */
    public boolean updateNews() {
        mNewsList = mMessageObject.startFunction();
        messages = mMessageObject.mMessages;

        if (mNewsList == null)
            return false;
        else
            return true;
    }

    /**
     * Inserts the parsed news data into the news list view.
     */
    private void updateNewsList() {
        String[] keys = {NEWS_TITLE, NEWS_DATE};
        int[] ids = {android.R.id.text1, android.R.id.text2};

        SimpleAdapter news_adapter = new SimpleAdapter(mContext, mNewsList, R.layout.single_news_post, keys, ids);

        if (mNewsList != null) {
            ListView newsList = (ListView)mContext.findViewById(android.R.id.custom);
            newsList.setAdapter(news_adapter);

            newsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Intent viewMessage = new Intent(Intent.ACTION_VIEW, Uri.parse(messages.get(position).getLink().toString()));
                    mContext.startActivity(viewMessage);
                }
            });
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Dialog));
            builder.setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.error_message_loading_data)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    }).show();
        }
    }

    public class LoadNews extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            ppDialog = new ProgressDialog(mContext);
            ppDialog.setMessage("Loading News...");
            ppDialog.setIndeterminate(false);
            ppDialog.setCancelable(true);
            ppDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return updateNews();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            ppDialog.dismiss();
            if (result)
                updateNewsList();
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Dialog));
                builder.setTitle(R.string.error_message_loading_data)
                        .setMessage("Please try refreshing the page")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        }).show();
            }

            super.onPostExecute(result);
        }
    }
}