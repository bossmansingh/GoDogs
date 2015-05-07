package singh.saurabh.godogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;


public class ForgotPassword extends ActionBarActivity {

    private Context mContext = this;

    private AutoCompleteTextView mEmailView;
    private View mProgressView;
    private View mRecoverPasswordFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.password_recovery_email);
        mRecoverPasswordFormView = findViewById(R.id.recover_password_form);
        mProgressView = findViewById(R.id.login_progress_recover_password);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (android.net.ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkinfo != null && networkinfo.isConnected())
            isAvailable = true;

        return isAvailable;
    }

    public void sendVerificationCodeEmail(View v) {
        final String emailAddress = mEmailView.getText().toString();

        if (isNetworkAvailable()) {
            // Reset errors.
            mEmailView.setError(null);

            // Store values at the time of the login attempt.
            String email = mEmailView.getText().toString();

            boolean cancel = false;
            View focusView = null;

            // Check for a valid email address.
            if (TextUtils.isEmpty(email)) {
                mEmailView.setError(getString(R.string.error_field_required));
                focusView = mEmailView;
                cancel = true;
            }
            if (cancel) {
                // form field with an error.
                focusView.requestFocus();
            } else {
                // Show a progress spinner, and kick off a background task to
                // perform the email verification attempt.
                showProgress(true);
                ParseQuery<ParseUser> queryToGetEmailAddress = ParseUser.getQuery();
                queryToGetEmailAddress.whereEqualTo("username", emailAddress);
                queryToGetEmailAddress.getFirstInBackground(new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        showProgress(false);
                        if (e == null) {
                            parseUser.requestPasswordResetInBackground(emailAddress, new RequestPasswordResetCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Toast.makeText(mContext, R.string.password_reset_link_sent, Toast.LENGTH_LONG).show();
                                        ForgotPassword.this.finish();
                                    } else {
                                        Toast.makeText(mContext, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        Log.e("ERROR: ", e.getMessage());
                                    }
                                }
                            });
                        } else {
                            // Something went wrong.
                            AlertDialog.Builder user_not_found_error = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Dialog));
                            user_not_found_error.setTitle(R.string.error_email_does_not_exist)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setMessage("Please check the email address you entered")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    }).show();
                        }
                    }
                });
            }
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

            mRecoverPasswordFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRecoverPasswordFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRecoverPasswordFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mRecoverPasswordFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_forgot_password, menu);
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