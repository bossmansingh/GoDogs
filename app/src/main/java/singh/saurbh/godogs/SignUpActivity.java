package singh.saurbh.godogs;

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
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class SignUpActivity extends ActionBarActivity {

    private Context mContext = this;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView, mFirstNameView, mLastNameView, mConfirmPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mFirstNameView = (EditText) findViewById(R.id.first_name_signUp);
        mLastNameView = (EditText) findViewById(R.id.last_name_signUp);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email_signUp);
        mPasswordView = (EditText) findViewById(R.id.password_signUp);
        mConfirmPasswordView = (EditText) findViewById(R.id.confirm_password_signUp);

        Button mEmailSignUnButton = (Button) findViewById(R.id.sign_up_button);
        mEmailSignUnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignUp();
            }
        });

        mLoginFormView = findViewById(R.id.login_form_signUp);
        mProgressView = findViewById(R.id.login_progress_signUp);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (android.net.ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkinfo != null && networkinfo.isConnected())
            isAvailable = true;

        return isAvailable;
    }

    public void attemptSignUp() {

        if (isNetworkAvailable()) {
            // Reset errors.
            mEmailView.setError(null);
            mPasswordView.setError(null);
            mFirstNameView.setError(null);
            mLastNameView.setError(null);
            mConfirmPasswordView.setError(null);

            // Store values at the time of the login attempt.
            String email = mEmailView.getText().toString();
            String password = mPasswordView.getText().toString();
            String confirmPassword = mConfirmPasswordView.getText().toString();
            String firstName = mFirstNameView.getText().toString();
            String lastName = mLastNameView.getText().toString();

            //to remove any trailing white spaces
            email = email.trim();
            password = password.trim();
            confirmPassword = confirmPassword.trim();
            firstName = firstName.trim();
            lastName = lastName.trim();

            boolean cancel = false;
            View focusView = null;

            // Check if first name was entered.
            if (TextUtils.isEmpty(firstName)) {
                mFirstNameView.setError(getString(R.string.error_field_required));
                focusView = mFirstNameView;
                cancel = true;
            }

            // Check if last name was entered.
            if (TextUtils.isEmpty(lastName)) {
                mLastNameView.setError(getString(R.string.error_field_required));
                if (focusView == null)
                    focusView = mLastNameView;
                cancel = true;
            }

            // Check if email is entered and if so then valid or not
            if (TextUtils.isEmpty(email)) {
                mEmailView.setError(getString(R.string.error_field_required));
                if (focusView == null)
                    focusView = mEmailView;
                cancel = true;
            } else if (!isEmailValid(email)) {
                mEmailView.setError(getString(R.string.error_invalid_email));
                if (focusView == null)
                    focusView = mEmailView;
                cancel = true;
            }

            // Check if password and confirm password is entered and if so then valid or not
            if (TextUtils.isEmpty(password)) {
                mPasswordView.setError(getString(R.string.error_field_required));
                if (focusView == null)
                    focusView = mPasswordView;
                cancel = true;
            } else if (password.length() < 5) {
                mPasswordView.setError(getString(R.string.error_password_too_short));
                if (focusView == null)
                    focusView = mPasswordView;
                cancel = true;
            }
            if (TextUtils.isEmpty(confirmPassword)) {
                mConfirmPasswordView.setError(getString(R.string.error_field_required));
                if (focusView == null)
                    focusView = mConfirmPasswordView;
                cancel = true;
            } else if (!isPasswordValid(password, confirmPassword)) {
                mConfirmPasswordView.setError(getString(R.string.error_invalid_password));
                if (focusView == null)
                    focusView = mConfirmPasswordView;
                cancel = true;
            }

            if (cancel) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView.requestFocus();
            } else {
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                showProgress(true);
                ParseUser newUser = new ParseUser();
                // Mandatory fields
                newUser.setUsername(email);
                newUser.setEmail(email);
                newUser.setPassword(password);
                newUser.isAuthenticated();
                //Extra Fields
                newUser.put("firstName", firstName);
                newUser.put("lastName", lastName);
                //Background task for sign up
                newUser.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        showProgress(false);
                        if (e == null) {
                            Toast.makeText(mContext, R.string.account_activation_link_sent, Toast.LENGTH_LONG).show();
                            SignUpActivity.this.finish();

                        } else {
                            AlertDialog.Builder d2 = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Dialog));
                            d2.setTitle(R.string.something_went_wrong)
                                    .setMessage(e.getMessage())
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
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

    private boolean isEmailValid(String email) {
        return email.contains("@mail.fresnostate.edu");
    }

    private boolean isPasswordValid(String password, String confirmPassword) {
        return (password.compareTo(confirmPassword) == 0);
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}