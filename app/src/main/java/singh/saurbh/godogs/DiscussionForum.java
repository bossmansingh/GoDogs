package singh.saurbh.godogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.ActionMode;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class DiscussionForum {

    private Activity mContext;
    private ArrayList<HashMap<String, String>> postList_for_delete = null;
    private List<ParseObject> arr_of_objects_to_delete;

    // Flag for checkbox
    private Boolean flag;

    private ProgressDialog dialog;
    private ActionMode mActionMode;
    private int delete_post_counter = 0;
    private Boolean zero_post_to_delete = true;

    public DiscussionForum(Activity con) {
        this.mContext = con;
        dialog = new ProgressDialog(mContext);
        dialog.setMessage("Loading posts...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(true);
    }

    public void startLoadCommentsTask() {
        dialog.show();
        flag = false;
        // Find all posts
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Post");
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                dialog.dismiss();
                if (e == null) {
                    updateList(parseObjects, flag);
                } else {
                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /*
    * Function to update list with post returned by query
    * @param: list of parseObjects returned by search query
    */
    public void updateList(final List<ParseObject> parseObjects, boolean flag_for_checkbox) {
        int length = parseObjects.size();

        final ArrayList<HashMap<String, String>> postList;

        if (length > 0)
            postList = new ArrayList<>();
        else
            postList = null;

        for (int i = length-1; i >= 0; i--) {
            ParseObject obj = parseObjects.get(i);
            String firstName = obj.get("firstName").toString();
            String title = obj.get("title").toString();
            Date createdAt = obj.getCreatedAt();
            String posted_on = createdAt.toString();
            SimpleDateFormat sdf1 = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzzz yyyy");
            Date d1 = null;
            try {
                d1 = sdf1.parse(posted_on);
            } catch (ParseException ee) {
                ee.printStackTrace();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy   h:mm a");
            String dateWithoutTime = sdf.format(d1);
            posted_on = dateWithoutTime;
            HashMap<String, String> dataList = new HashMap<>();
            dataList.put("firstName", firstName);
            dataList.put("title", title);
            dataList.put("createdAt", posted_on);
            postList.add(dataList);
        }

        View footerView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_for_discussion_forum_list, null, false);
        if (postList != null) {
            ArrayAdapter<HashMap<String, String>> adapter = new InteractiveArrayAdapter(mContext, postList, flag_for_checkbox);
            ListView lv = (ListView) mContext.findViewById(android.R.id.list);
            lv.setAdapter(adapter);
            if (lv.getFooterViewsCount() == 0)
                lv.addFooterView(footerView);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    if ((postList.size() - 1) - position >= 0) {
                        ParseObject obj = parseObjects.get((postList.size() - 1) - position);
                        String objectId = obj.getObjectId();
                        Intent i = new Intent(mContext, SinglePostDisplay.class);
                        i.putExtra("objectId", objectId);
                        mContext.startActivity(i);
                    }
                }
            });
        } else {
            ListView lv = (ListView) mContext.findViewById(android.R.id.list);
            View view = View.inflate(mContext, R.layout.activity_discussion_forum, null);
            TextView empty_text = (TextView) view.findViewById(android.R.id.empty);
            lv.setEmptyView(empty_text);
        }
    }

    public void searchPostTask(final String query) {
        dialog.show();
        flag = false;

        ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Post");
        query1.whereMatches("title", query, "im");

        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Post");
        query2.whereMatches("firstName", query, "im");

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(query1);
        queries.add(query2);

        ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
        mainQuery.orderByAscending("createdAt");
        mainQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                dialog.dismiss();
                if (parseObjects.size() > 0) {
                    if (e == null) {
                        updateList(parseObjects, flag);
                    } else {
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    updateList(parseObjects, flag);
                    Toast.makeText(mContext, "No results found!!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void selectPostToDelete() {
        dialog.show();
        flag = true;

        // Find post(s) of current user only for deletion
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Post");
        query.whereEqualTo("user", currentUser);
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                dialog.dismiss();
                if (parseObjects.size() > 0) {
                    if (e == null) {
                        mActionMode = mContext.startActionMode(new ActionBarCallBack());
                        arr_of_objects_to_delete = parseObjects;
                        updateList_for_delete(parseObjects, flag);
                    } else {
                        Toast.makeText(mContext, "No post(s) to delete", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(mContext, "No post(s) to delete", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void updateList_for_delete(List<ParseObject> parseObjects, boolean flag_for_checkbox) {
        int length = parseObjects.size();

        if (length > 0)
            postList_for_delete = new ArrayList<>();
        else
            postList_for_delete = null;

        for (int i = 0; i < length; i++) {
            ParseObject obj = parseObjects.get(i);
            String firstName = obj.get("firstName").toString();
            String title = obj.get("title").toString();

            Date createdAt = obj.getCreatedAt();
            String posted_on = createdAt.toString();
            SimpleDateFormat sdf1 = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzzz yyyy");
            Date d1 = null;
            try {
                d1 = sdf1.parse(posted_on);
            } catch (ParseException ee) {
                ee.printStackTrace();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy   h:mm a");
            posted_on = sdf.format(d1);

            HashMap<String, String> dataList = new HashMap<>();
            dataList.put("firstName", firstName);
            dataList.put("title", title);
            dataList.put("createdAt", posted_on);
            postList_for_delete.add(dataList);
        }

        if (postList_for_delete != null) {
            ArrayAdapter<HashMap<String, String>> adapter = new InteractiveArrayAdapter(mContext, postList_for_delete, flag_for_checkbox);
            ListView lv = (ListView) mContext.findViewById(android.R.id.list);
            lv.setAdapter(adapter);
        } else {
            ListView lv = (ListView) mContext.findViewById(android.R.id.list);
            View view = View.inflate(mContext, R.layout.activity_discussion_forum, null);
            TextView empty_text = (TextView) view.findViewById(android.R.id.empty);
            lv.setEmptyView(empty_text);
        }
    }



    class ActionBarCallBack implements ActionMode.Callback {

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();

            if (id == R.id.delete_sign) {
                delete_post_counter = 0;

                final ProgressDialog dialogg = new ProgressDialog(mContext);
                dialogg.setMessage("Deleting posts...");
                dialogg.setIndeterminate(false);
                dialogg.setCancelable(true);

                if (postList_for_delete != null) {
                    for(int i = 0; i < postList_for_delete.size(); i++) {
                        if (InteractiveArrayAdapter.checkList[i])
                            zero_post_to_delete = false;
                    }

                    if (zero_post_to_delete)
                        Toast.makeText(mContext, "Select a post first", Toast.LENGTH_SHORT).show();
                    else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext,android.R.style.Theme_Holo_Dialog));
                        builder.setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Are you sure?")
                                .setMessage("Once you hit delete there's no coming back.")
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, int id) {
                                        dialogg.show();
                                        List<ParseObject> tempArr = new ArrayList<>(postList_for_delete.size());
                                        for (int i = 0; i < postList_for_delete.size(); i++) {
                                            if (InteractiveArrayAdapter.checkList[i]) {
                                                zero_post_to_delete = false;
                                                delete_post_counter++;
                                                tempArr.add(arr_of_objects_to_delete.get(i));
                                            }
                                        }
                                        ParseObject.deleteAllInBackground(tempArr, new DeleteCallback() {
                                            @Override
                                            public void done(com.parse.ParseException e) {
                                                dialogg.dismiss();
                                                if (e == null) {
                                                    if (delete_post_counter > 1)
                                                        Toast.makeText(mContext, (delete_post_counter) + " posts deleted", Toast.LENGTH_SHORT).show();
                                                    else
                                                        Toast.makeText(mContext, (delete_post_counter) + " post deleted", Toast.LENGTH_SHORT).show();
                                                    Intent i = new Intent(mContext, MenuScreen.class);
                                                    mContext.finish();
                                                    mContext.startActivity(i);
                                                    mActionMode.finish();
                                                } else {
                                                    Toast.makeText(mContext, R.string.some_error_occured, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                }).show();
                    }
                } else {
                    Toast.makeText(mContext, "No post to delete", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            if (id == R.id.mark_all) {
                ListView lv = (ListView)mContext.findViewById(android.R.id.list);
                int length = postList_for_delete.size();
                if (item.isChecked()) {
                    item.setChecked(false);
                    item.setIcon(android.R.drawable.checkbox_off_background);
                    for (int i = 0; i < length; i++) {
                        LinearLayout childView = (LinearLayout)lv.getChildAt(i);
                        CheckBox checkBox_for_reply = (CheckBox)childView.findViewById(R.id.checkBox_comment);
                        checkBox_for_reply.setChecked(false);
                    }
                } else {
                    item.setChecked(true);
                    item.setIcon(android.R.drawable.checkbox_on_background);
                    for (int i = 0; i < length; i++) {
                        LinearLayout childView = (LinearLayout) lv.getChildAt(i);
                        CheckBox checkBox_for_reply = (CheckBox) childView.findViewById(R.id.checkBox_comment);
                        checkBox_for_reply.setChecked(true);
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.contextual_menu, menu);

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            startLoadCommentsTask();
            LinearLayout layout_for_add_new_post = (LinearLayout)mContext.findViewById(R.id.add_new_post_container);
            layout_for_add_new_post.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            mode.setTitle(R.string.title_for_delete);
            return false;
        }
    }

//    public void startSearchCommentsTask(String query, Boolean delete_search_check, Boolean flag) {
//        this.delete_search_check = delete_search_check;
//        this.flag = flag;
//        new searchComments().execute(query);
//    }
//
//
//    public class searchComments extends AsyncTask<String, Void, Boolean> {
//
//        JSONParser jParser;
//        JSONArray productList;
//        ProgressDialog pd;
//
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            productList = new JSONArray();
//            jParser = new JSONParser();
//            pd = new ProgressDialog(mContext);
//            pd.setCancelable(false);
//            pd.setMessage("Searching...");
//            pd.show();
//        }
//
//        @Override
//        protected Boolean doInBackground(String... sText) {
//            return getSearchedPostList(post_search_url, sText[0]);
//        }
//
//        public Boolean getSearchedPostList(String url, String textSearch) {
//            int j = 0;
//
//            // Instantiate the array list to contain all the JSON data.
//            // we are going to use a bunch of key-value pairs, referring
//            // to the json element name, and the content, for example,
//            // message it the tag, and "I'm awesome" as the content..
//
//            mCommentListResults = new ArrayList<HashMap<String, String>>();
//
//            List<NameValuePair> params = new ArrayList<NameValuePair>();
//            params.add(new BasicNameValuePair("str", textSearch));
//
//            // The jParser comments url as argument, and gives us
//            // back a JSON object.
//            JSONParser jParser = new JSONParser();
//
//            JSONObject json = jParser.makeHttpRequest(url, "GET", params);
//
//
//            // when parsing JSON stuff, we should probably
//            // try to catch any exceptions:
//            try {
//                success = json.getInt(TAG_SUCCESS);
//                Err = json.getString(TAG_MESSAGE);
//
//                if (success == 1) {
//
//                    mComments = json.getJSONArray(TAG_POSTS);
//                    arr_for_filtered_comments = new String[mComments.length()];
//                    // looping through all posts according to the json object returned
//                    for (int i = mComments.length()-1; i >= 0; i--) {
//                        JSONObject c = mComments.getJSONObject(i);
//
//                        post_id = c.getString(TAG_POST_ID);
//                        arr_for_filtered_comments[j] = post_id;
//
//                        String title = c.getString(TAG_TITLE);
//                        String pid = c.getString(TAG_POST_ID);
//                        title = Html.fromHtml(title).toString();
//                        String posted_on = c.getString(TAG_DATE_TIME);
//                        String first_name = c.getString(TAG_FIRST_NAME);
//
//                        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//                        Date d1 = null;
//                        try {
//                            d1 = sdf1.parse(posted_on);
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy   h:mm a");
//                        String dateWithoutTime = sdf.format(d1);
//                        posted_on = dateWithoutTime;
//
//                        // creating new HashMap
//                        HashMap<String, String> map = new HashMap<String, String>();
//
//                        map.put(TAG_TITLE, title);
//                        map.put(TAG_POST_ID, pid);
//                        map.put(TAG_FIRST_NAME, first_name);
//                        map.put(TAG_DATE_TIME, posted_on);
//
//                        // adding HashList to ArrayList
//                        mCommentListResults.add(map);
//                        j++;
//                        // and, the JSON data is up to date same with our array
//                        // list
//                    }
//                }
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            return success == 1;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean s) {
//            super.onPostExecute(s);
//            pd.dismiss();
//
//            if (!s) {
//                toastFunction("No post found!!");
//            }
//            else {
//                if (delete_search_check)
//                    mActionMode = mContext.startActionMode(new ActionBarCallBack());
//                ArrayAdapter<HashMap<String, String>> filteredListAdapter =
//                        new InteractiveArrayAdapter(mContext, mCommentListResults, flag, arr_for_filtered_comments);
//
//                if (mCommentListResults != null) {
//                    ListView lv = (ListView)mContext.findViewById(android.R.id.list);
//                    lv.setAdapter(filteredListAdapter);
//
//                    // The onClick method makes call to the activity single post display
//                    // which displays one complete post along with the message
//                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//                        @Override
//                        public void onItemClick(AdapterView<?> parent, View view,
//                                                int position, long id) {
//                            Intent i = new Intent(mContext,SinglePostDisplay.class);
//                            i.putExtra("post_id",arr_for_filtered_comments[position]);
//                            mContext.startActivity(i);
//                        }
//                    });
//                }
//                else {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Dialog));
//                    builder.setIcon(android.R.drawable.ic_dialog_alert)
//                            .setTitle(Err)
//                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//
//                                }
//                            }).show();
//                }
//                delete_search_check = false;
//            }
//        }
//    }
//
//    public class delete_post extends AsyncTask<String, Void, Boolean> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
////            ppDialog = new ProgressDialog(mContext);
////            ppDialog.setMessage("Deleting post...");
////            ppDialog.setIndeterminate(false);
////            ppDialog.setCancelable(true);
////            ppDialog.show();
//        }
//
//        @Override
//        protected Boolean doInBackground(String... str) {
//            try {
//                // Building Parameters
//                List<NameValuePair> params = new ArrayList<NameValuePair>();
//                params.add(new BasicNameValuePair("post_id", str[0]));
//
//                JSONParser jsonParser = new JSONParser();
//                //Posting user data to script
//                JSONObject json = jsonParser.makeHttpRequest(post_delete_url, "GET", params);
//
//                int success = json.getInt(TAG_SUCCESS);
//
//                if (success == 1)
//                    return true;
//                else
//                    return false;
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            super.onPostExecute(result);
////            ppDialog.dismiss();
//
//        }
//    }
}
