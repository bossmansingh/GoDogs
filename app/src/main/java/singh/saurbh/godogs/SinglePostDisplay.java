package singh.saurbh.godogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ActionMode;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class SinglePostDisplay extends ActionBarActivity {

    private Context mContext = this;

    private String objectId;
    protected ArrayList<HashMap<String, String>> replyList_for_delete = null;
    private List<ParseObject> arr_of_reply_objects_to_delete;

    private TextView mTitle, mAuthor, mMessage, mDateTime;
    private EditText mReplytextView;
    private ActionMode mActionMode = null;
    private View mProgressView;
    private View mSinglePostDisplayView;
    private View mScrollView;

    // Our created menu to use
    private Menu mMenu;

    private View layout = null;
    private Boolean zero_post_to_delete = true;
    private static int delete_post_counter;
    public Boolean[] checkList;
    public Boolean refresh_required = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post_display);

        mSinglePostDisplayView = findViewById(R.id.container_for_title_name_date);
        mScrollView = findViewById(R.id.container_for_scrollView);
        mProgressView = findViewById(R.id.progressBar_for_single_post);
        layout = findViewById(R.id.container_for_date);

        mTitle = (TextView) findViewById(R.id.view_title);
        mAuthor = (TextView) findViewById(R.id.view_author);
        mMessage = (TextView) findViewById(R.id.view_message);
        mDateTime = (TextView) findViewById(R.id.date_time_single_post_display);
        mReplytextView = (EditText) findViewById(R.id.editText_reply);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            objectId = extras.getString("objectId");
        }
        loadPost();
    }

    public void loadPost() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Post");
        query.whereEqualTo("objectId", objectId);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {

                    Date createdAt = parseObject.getCreatedAt();
                    String posted_on = createdAt.toString();
                    SimpleDateFormat sdf1 = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzzz yyyy");
                    Date d1 = null;
                    try {
                        d1 = sdf1.parse(posted_on);
                    } catch (java.text.ParseException ee) {
                        ee.printStackTrace();
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy   h:mm a");
                    posted_on = sdf.format(d1);

                    layout.setVisibility(View.VISIBLE);
                    mTitle.setText(parseObject.get("title").toString());
                    mAuthor.setText(parseObject.get("firstName").toString());
                    mMessage.setText(parseObject.get("body").toString());
                    mDateTime.setText(posted_on);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Dialog));
                    builder.setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Post is not available anymore")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent i = new Intent(mContext, MenuScreen.class);
                                    finish();
                                    startActivity(i);
                                }
                            }).show();
                }
            }
        });
        populateReplyList();
    }

    public void AddReply(View v) {
        mReplytextView.setError(null);
        if (mReplytextView.length() == 0) {
            mReplytextView.setError(getString(R.string.field_is_required));
            View focusView = mReplytextView;
            focusView.requestFocus();
        } else {

            ParseUser currentUser = ParseUser.getCurrentUser();
            String message = mReplytextView.getText().toString();
            ParseObject replyPostObject = new ParseObject("Replies");
            replyPostObject.put("firstName", currentUser.get("firstName").toString());
            replyPostObject.put("replyMessage", message);
            replyPostObject.put("replyUser", currentUser);
            replyPostObject.put("parent", objectId);
            replyPostObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(mContext, "Reply successfully added", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mContext, SinglePostDisplay.class);
                        i.putExtra("objectId", objectId);
                        finish();
                        startActivity(i);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Dialog));
                        builder.setTitle(getString(R.string.some_error_occured))
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                }).show();
                    }
                }
            });
        }
    }

    private void populateReplyList() {
        if (refresh_required)
            showProgress(true);
        // Find all posts
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Replies");
        query.whereEqualTo("parent", objectId);
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                if (refresh_required)
                    showProgress(false);
                if (e == null) {
                    arr_of_reply_objects_to_delete = parseObjects;
                    int length = parseObjects.size();
                    final ArrayList<HashMap<String, String>> replyList;

                    if (length > 0)
                        replyList = new ArrayList<>();
                    else
                        replyList = null;

                    for (int i = 0; i < length; i++) {
                        ParseObject obj = parseObjects.get(i);
                        String firstName = obj.get("firstName").toString();
                        String replyMessage = obj.get("replyMessage").toString();
                        ParseUser replyUserId = obj.getParseUser("replyUser");
                        Date createdAt = obj.getCreatedAt();
                        String posted_on = createdAt.toString();
                        SimpleDateFormat sdf1 = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzzz yyyy");
                        Date d1 = null;
                        try {
                            d1 = sdf1.parse(posted_on);
                        } catch (java.text.ParseException ee) {
                            ee.printStackTrace();
                        }
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy   h:mm a");
                        posted_on = sdf.format(d1);

                        HashMap<String, String> dataList = new HashMap<>();
                        dataList.put("firstName", firstName);
                        dataList.put("replyMessage", replyMessage);
                        dataList.put("createdAt", posted_on);
                        dataList.put("replyUser", replyUserId.getObjectId());
                        replyList.add(dataList);
                    }

                    ListView lv = (ListView) findViewById( R.id.list_for_replies);
                    View footerView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate( R.layout.footer_for_reply_list, null, false);
                    if (replyList != null) {
                        replyList_for_delete = replyList;
                        ArrayAdapter<HashMap<String, String>> adapter = new InteractiveArrayAdapterForReplyList(SinglePostDisplay.this, replyList);
                        lv.setAdapter(adapter);
                        lv.setOnTouchListener(new ListView.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                int action = event.getAction();
                                switch (action) {
                                    case MotionEvent.ACTION_DOWN:
                                        // Disallow ScrollView to intercept touch events.
                                        v.getParent().requestDisallowInterceptTouchEvent(true);
                                        break;

                                    case MotionEvent.ACTION_UP:
                                        // Allow ScrollView to intercept touch events.
                                        v.getParent().requestDisallowInterceptTouchEvent(false);
                                        break;
                                }

                                // Handle ListView touch events.
                                v.onTouchEvent(event);
                                return true;
                            }
                        });
                    } else {

                    }
                } else {
                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    public class InteractiveArrayAdapterForReplyList extends ArrayAdapter<HashMap<String, String>> {

        private final ArrayList<HashMap<String, String>> list;
        private final Activity context;
        private ViewHolder holder;
        private int j = 0;

        public InteractiveArrayAdapterForReplyList(Activity context,
                                                   ArrayList<HashMap<String, String>> list) {
            super(context, R.layout.single_post, list);
            this.context = context;
            this.list = list;
            checkList = new Boolean[list.size()];
            for(int i = 0; i < list.size(); i++)
                checkList[i] = false;
        }

        class ViewHolder {
            protected TextView first_name, message, published_date;
            protected CheckBox checkbox;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View single_reply_for_list_view ;
            if (convertView == null) {

                LayoutInflater inflator = context.getLayoutInflater();
                single_reply_for_list_view = inflator.inflate(R.layout.single_reply_post, null);
                final ViewHolder viewHolder = new ViewHolder();
                viewHolder.first_name = (TextView) single_reply_for_list_view.findViewById(R.id.first_name_for_single_reply_post);
                viewHolder.message = (TextView) single_reply_for_list_view.findViewById(R.id.message_for_single_reply_post);
                viewHolder.published_date = (TextView) single_reply_for_list_view.findViewById(R.id.date_time_single_reply_post);
                viewHolder.checkbox = (CheckBox) single_reply_for_list_view.findViewById(R.id.checkBox_reply);

                if (list.get(position).get("replyUser").compareTo(ParseUser.getCurrentUser().getObjectId()) == 0) {
                    viewHolder.checkbox.setVisibility(View.VISIBLE);
                    viewHolder.checkbox
                            .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    checkList[position] = isChecked;
                                    if (mActionMode == null)
                                        mActionMode = startActionMode(new ActionBarCallBack());

                                    for (int i = 0; i < list.size(); i++) {
                                        if (checkList[i]) {
                                            j = 1;
                                            break;
                                        } else {
                                            j = 0;
                                        }
                                    }
                                    if (j == 0) {
                                        if (mActionMode != null) {
                                            refresh_required = false;
                                            mActionMode.finish();
                                            mActionMode = null;
                                            refresh_required = true;
                                        }
                                    }
                                }
                            });
                } else {
                    viewHolder.checkbox.setVisibility(View.INVISIBLE);
                }
                single_reply_for_list_view.setTag(viewHolder);
                viewHolder.checkbox.setTag(list.get(position));
            } else {
                single_reply_for_list_view = convertView;
                ((ViewHolder) single_reply_for_list_view.getTag()).checkbox.setTag(list.get(position));
            }
            holder = (ViewHolder) single_reply_for_list_view.getTag();
            holder.message.setText(list.get(position).get("replyMessage"));
            holder.first_name.setText(list.get(position).get("firstName"));
            holder.published_date.setText(list.get(position).get("createdAt"));

            return single_reply_for_list_view;
        }
    }

    class ActionBarCallBack implements ActionMode.Callback {

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();

            if (id == R.id.delete_sign) {
                delete_post_counter = 0;
                if (replyList_for_delete != null) {
                    for(int i = 0; i < replyList_for_delete.size(); i++) {
                        if (checkList[i])
                            zero_post_to_delete = false;
                    }
                    if (zero_post_to_delete)
                        Toast.makeText(mContext, "Select a reply first", Toast.LENGTH_SHORT).show();
                    else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext,android.R.style.Theme_Holo_Dialog));
                        builder.setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Are you sure?")
                                .setMessage("Once you hit delete there's no coming back.")
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, int id) {
                                        if (refresh_required)
                                            showProgress(true);
                                        List<ParseObject> tempArr = new ArrayList<>(replyList_for_delete.size());
                                        for (int i = 0; i < replyList_for_delete.size(); i++) {
                                            if (checkList[i]) {
                                                zero_post_to_delete = false;
                                                delete_post_counter++;
                                                tempArr.add(arr_of_reply_objects_to_delete.get(i));
                                            }
                                        }
                                        ParseObject.deleteAllInBackground(tempArr, new DeleteCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (refresh_required)
                                                    showProgress(false);
                                                if (e == null) {
                                                    if (delete_post_counter > 1)
                                                        Toast.makeText(mContext, (delete_post_counter) + " replies deleted", Toast.LENGTH_SHORT).show();
                                                    else
                                                        Toast.makeText(mContext, (delete_post_counter) + " reply deleted", Toast.LENGTH_SHORT).show();
                                                    mActionMode.finish();
                                                } else {
                                                    Toast.makeText(mContext,R.string.some_error_occured, Toast.LENGTH_SHORT).show();
                                                }
                                                Intent i = new Intent(mContext, SinglePostDisplay.class);
                                                i.putExtra("objectId", objectId);
                                                finish();
                                                startActivity(i);
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
                ListView lv = (ListView) findViewById( R.id.list_for_replies);
                int length = replyList_for_delete.size();
                if (item.isChecked()) {
                    item.setChecked(false);
                    item.setIcon(android.R.drawable.checkbox_off_background);
                    for (int i = 0; i < length; i++) {
                        LinearLayout childView = (LinearLayout)lv.getChildAt(i);
                        if (findViewById( R.id.checkBox_reply) != null) {
                            CheckBox checkBox_for_reply = (CheckBox) childView.findViewById( R.id.checkBox_reply);
                            checkBox_for_reply.setChecked(false);
                        }
                    }
                } else {
                    item.setChecked(true);
                    item.setIcon(android.R.drawable.checkbox_on_background);
                    for (int i = 0; i < length; i++) {
                        LinearLayout childView = (LinearLayout) lv.getChildAt(i);
                        if (findViewById( R.id.checkBox_reply) != null) {
                            CheckBox checkBox_for_reply = (CheckBox) childView.findViewById( R.id.checkBox_reply);
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
            mActionMode = null;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            mode.setTitle(R.string.title_for_delete);
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_single_post_display, menu);

        // We should save our menu so we can use it to reset our updater.
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.delete_for_singal_post) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext,android.R.style.Theme_Holo_Dialog));
            builder.setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Are you sure?")
                    .setMessage("Once you hit delete there's no coming back.")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            if (refresh_required)
                                showProgress(true);
                            ParseQuery<ParseObject> query = ParseQuery.getQuery("Post");
                            query.whereEqualTo("objectId", objectId);
                            query.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
                                    if (refresh_required)
                                        showProgress(false);
                                    if (e == null) {
                                        if (parseObject.getParseObject("user").getObjectId().compareTo(ParseUser.getCurrentUser().getObjectId()) == 0) {
                                            parseObject.deleteInBackground(new DeleteCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (refresh_required)
                                                        showProgress(false);
                                                    if (e == null) {
                                                        Intent i = new Intent(mContext, MenuScreen.class);
                                                        finish();
                                                        startActivity(i);
                                                    } else {
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Dialog));
                                                        builder.setIcon(android.R.drawable.ic_dialog_alert)
                                                                .setTitle(R.string.something_went_wrong)
                                                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int id) {

                                                                    }
                                                                }).show();
                                                    }
                                                }
                                            });
                                        } else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Dialog));
                                            builder.setIcon(android.R.drawable.ic_dialog_alert)
                                                    .setTitle("You are not authorized to delete this post")
                                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {

                                                        }
                                                    }).show();
                                        }
                                    } else {

                                    }
                                }
                            });
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    }).show();

            return true;
        }

        if (id == R.id.action_logOut) {
            ProgressDialog dialog;
            dialog = new ProgressDialog(mContext);
            dialog.setMessage("Logging out...");
            dialog.setIndeterminate(false);
            dialog.setCancelable(true);
            dialog.show();
            ParseUser.logOut();
            ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null
            finish();
            Intent i = new Intent(mContext, LoginActivity.class);
            startActivity(i);
            return true;
        }

        if (item.getItemId() == R.id.action_refresh) {
            LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ImageView iv = (ImageView)inflater.inflate(R.layout.iv_refresh, null);
            Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
            rotation.setRepeatCount(Animation.INFINITE);
            iv.startAnimation(rotation);
            item.setActionView(iv);

            new UpdateTask(this).execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (android.net.ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkinfo != null && networkinfo.isConnected())
            isAvailable = true;

        return isAvailable;
    }

    public void resetUpdating() {
        // Get our refresh item from the menu
        MenuItem m = mMenu.findItem(R.id.action_refresh);
        if (m.getActionView() != null) {
            // Remove the animation.
            m.getActionView().clearAnimation();
            m.setActionView(null);
        }
    }

    public class UpdateTask extends AsyncTask<Void, Void, Void> {

        private Context mCon;

        public UpdateTask(Context con)
        {
            mCon = con;
        }

        @Override
        protected Void doInBackground(Void... nope) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void nope) {

            if (isNetworkAvailable())
                loadPost();
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Dialog));
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.check_network)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        }).show();
            }
            // Change the menu back
            ((SinglePostDisplay) mCon).resetUpdating();
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mSinglePostDisplayView.setVisibility(show ? View.GONE : View.VISIBLE);
            mSinglePostDisplayView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSinglePostDisplayView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mScrollView.setVisibility(show ? View.GONE : View.VISIBLE);
            mScrollView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mScrollView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mSinglePostDisplayView.setVisibility(show ? View.GONE : View.VISIBLE);
            mScrollView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
