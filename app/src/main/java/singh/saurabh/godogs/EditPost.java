package singh.saurabh.godogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;


public class EditPost extends ActionBarActivity {

    private Context mContext = this;
    private String objectId;
    private String mTitle;
    private String mMessage;
    private TextView title, message;
    private View mProgressView;
    private View mEditPostContainer;
    private View focusView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        title = (EditText)findViewById(R.id.editPostTitle);
        message = (EditText)findViewById(R.id.edit_message);

        mProgressView = findViewById(R.id.editPost_progressBar);
        mEditPostContainer = findViewById(R.id.editPostContainer);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            objectId = extras.getString("objectId");
            mTitle = extras.getString("title");
            mMessage = extras.getString("message");
        }

        title.setText(mTitle);
        message.setText(mMessage);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (android.net.ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkinfo != null && networkinfo.isConnected())
            isAvailable = true;

        return isAvailable;
    }

    public void SubmitEditPost(View v) {
        if (isNetworkAvailable()) {
            // Resetting errors for both editText
            title.setError(null);
            message.setError(null);

            if (title.length() == 0) {
                title.setError(getString(R.string.field_is_required));
                focusView = title;
            } else if (message.length() == 0) {
                message.setError(getString(R.string.field_is_required));
                focusView = message;
            } else {
                postEditedComment();
            }
            if (title.length() == 0 || message.length() == 0)
                focusView.requestFocus();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Dialog));
            builder.setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.check_network)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    }).show();
        }
    }

    private void postEditedComment() {
        showProgress(true);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Post");
        query.getInBackground(objectId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    parseObject.put("title", title.getText().toString());
                    parseObject.put("body", message.getText().toString());
                    parseObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(mContext, "Post updated successfully", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(mContext, SinglePostDisplay.class);
                                i.putExtra("objectId", objectId);
                                startActivity(i);
                                finish();
                            }
                        }
                    });
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Dialog));
                    builder.setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(R.string.error_message_posting_data)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            }).show();
                }
            }
        });
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

            mEditPostContainer.setVisibility(show ? View.GONE : View.VISIBLE);
            mEditPostContainer.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mEditPostContainer.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mEditPostContainer.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_edit_post, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
