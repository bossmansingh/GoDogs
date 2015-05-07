package singh.saurabh.godogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextThemeWrapper;
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
import java.util.Locale;

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
    private View footerView;

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
            SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzzz yyyy", Locale.US);
            Date d1 = null;
            try {
                d1 = simpleDateFormatter.parse(posted_on);
            } catch (ParseException ee) {
                ee.printStackTrace();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy   h:mm a", Locale.US);
            posted_on = sdf.format(d1);
            HashMap<String, String> dataList = new HashMap<>();
            dataList.put("firstName", firstName);
            dataList.put("title", title);
            dataList.put("createdAt", posted_on);
            if (postList != null) {
                postList.add(dataList);
            }
        }
        ListView lv = (ListView) mContext.findViewById(android.R.id.list);
        View view = View.inflate(mContext, R.layout.activity_discussion_forum, null);
        TextView empty_text = (TextView) view.findViewById(android.R.id.empty);
        if (postList != null) {
            ArrayAdapter<HashMap<String, String>> adapter = new InteractiveArrayAdapter(mContext, postList, flag_for_checkbox);
            lv.setAdapter(adapter);
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
            SimpleDateFormat sdf1 = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzzz yyyy", Locale.US);
            Date d1 = null;
            try {
                d1 = sdf1.parse(posted_on);
            } catch (ParseException ee) {
                ee.printStackTrace();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy   h:mm a", Locale.US);
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
                                        final List<ParseObject> tempArr = new ArrayList<>(postList_for_delete.size());
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
                                                    final Intent i = new Intent(mContext, MenuScreen.class);
                                                    mContext.finish();
                                                    mContext.startActivity(i);
                                                    mActionMode.finish();
//
//                                                    for (int x = 0; x < postList_for_delete.size(); x++) {
//                                                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Replies");
//                                                        query.whereEqualTo("parent", tempArr.get(x));
//                                                        query.findInBackground(new FindCallback<ParseObject>() {
//                                                            @Override
//                                                            public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
//                                                                if (e == null) {
//                                                                    Log.d(parseObjects.size()+" ", "replies found");
//                                                                    ParseObject.deleteAllInBackground(parseObjects, new DeleteCallback() {
//                                                                        @Override
//                                                                        public void done(com.parse.ParseException e) {
//                                                                            if (e != null)
//                                                                                Log.e("Error deleting replies", e.getMessage());
//                                                                            else {
//                                                                                Log.d("No Error", "");
//                                                                                mContext.finish();
//                                                                                mContext.startActivity(i);
//                                                                                mActionMode.finish();
//                                                                            }
//                                                                        }
//                                                                    });
//                                                                } else
//                                                                    Log.e("Error finding replies", e.getMessage());
//                                                            }
//                                                        });
//                                                    }

                                                } else {
                                                    Toast.makeText(mContext, R.string.some_error_occurred, Toast.LENGTH_SHORT).show();
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
                        Log.d("LENGTH", i+"..");
                        LinearLayout childView = (LinearLayout)lv.getChildAt(i);
                        if (childView != null) {
                            CheckBox checkBox_for_reply = (CheckBox)childView.findViewById(R.id.checkBox_comment);
                            checkBox_for_reply.setChecked(false);
                        }
                    }
                } else {
                    item.setChecked(true);
                    item.setIcon(android.R.drawable.checkbox_on_background);
                    for (int i = 0; i < length; i++) {
                        Log.d("LENGTH", i+"..");
                        LinearLayout childView = (LinearLayout) lv.getChildAt(i);
                        if (childView != null) {
                            CheckBox checkBox_for_reply = (CheckBox) childView.findViewById(R.id.checkBox_comment);
                            checkBox_for_reply.setChecked(true);
                        }
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
}