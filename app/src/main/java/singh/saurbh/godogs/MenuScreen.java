package singh.saurbh.godogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.parse.ParseUser;


public class MenuScreen extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, SearchView.OnQueryTextListener {

    // Our created menu to use
    private Menu mMenu;

    public Context mContext = this;
    protected NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    public int mFragmentNumber = 0;
    public PlaceholderFragment mPlaceHolderFragment = new PlaceholderFragment();
    private View layout_for_discussion_forum = null;
    private View layout_for_news = null;

    private DiscussionForum mDiscussionForum = null;
    private News mNews = null;
    private RoadMapCreation mRoadMapCreation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_screen);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        layout_for_discussion_forum = View.inflate(mContext, R.layout.activity_discussion_forum, null);
        layout_for_news = View.inflate(mContext, R.layout.activity_news_feed, null);

        mDiscussionForum = new DiscussionForum(this);
        mNews = new News(this);

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (ParseUser.getCurrentUser() == null)
            MenuScreen.this.finish();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        super.onBackPressed();
        MenuScreen.this.finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void addPost(View v) {
        Intent i = new Intent(MenuScreen.this, AddPost.class);
        finish();
        startActivity(i);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, mPlaceHolderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {

            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            if(mFragmentNumber == 1) {
                getMenuInflater().inflate(R.menu.menu_discussion_forum, menu);

                MenuItem searchItem = menu.findItem(R.id.action_search_discussion_forum);
                SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
                searchView.setQueryHint("Name/Title");
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        // perform query here
                        mDiscussionForum.searchPostTask(query);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (newText.length() > 4) {
                            mDiscussionForum.searchPostTask(newText);
                            return true;
                        } else {
                            if (newText.length() == 0) {
                                mDiscussionForum.startLoadCommentsTask();
                                return true;
                            }
                            return false;
                        }
                    }
                });

                MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        mDiscussionForum.startLoadCommentsTask();
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }
                });
            }
            if (mFragmentNumber == 2)
                getMenuInflater().inflate(R.menu.menu_news, menu);

            if (mFragmentNumber == 3) {
                getMenuInflater().inflate(R.menu.menu_roadmap, menu);
            }

            // We should save our menu so we can use it to reset our updater.
            mMenu = menu;

            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.delete_post) {
            mDiscussionForum.selectPostToDelete();
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
            dialog.dismiss();
            finish();
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

        if (item.getItemId() == R.id.action_sendAsPDF) {
            mRoadMapCreation.sendPDFasMail();
        }

        return super.onOptionsItemSelected(item);
    }

    public void resetUpdating() {
        // Get our refresh item from the menu
        MenuItem m = mMenu.findItem(R.id.action_refresh);
        if(m.getActionView()!=null)
        {
            // Remove the animation.
            m.getActionView().clearAnimation();
            m.setActionView(null);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        mDiscussionForum.searchPostTask(s);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
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

            if (mFragmentNumber == 1) {
//                layout_for_discussion_forum.buildLayer();
                if (mPlaceHolderFragment.isNetworkAvailable())
                    mDiscussionForum.startLoadCommentsTask();
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Dialog));
                    builder.setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(R.string.check_network)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            }).show();
                }

            }

            if (mFragmentNumber == 2) {
//                layout_for_news.buildLayer();
                if (mPlaceHolderFragment.isNetworkAvailable()) {
                    mNews.startLoadNewsTask();
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Dialog));
                    builder.setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(R.string.check_network)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            }).show();
                }
            }

            // Change the menu back
            ((MenuScreen) mCon).resetUpdating();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        private boolean isNetworkAvailable() {
            ConnectivityManager manager = (android.net.ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkinfo = manager.getActiveNetworkInfo();

            boolean isAvailable = false;
            if (networkinfo != null && networkinfo.isConnected())
                isAvailable = true;

            return isAvailable;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            int i = getArguments().getInt(ARG_SECTION_NUMBER);
            View rootView = null;

            switch (i) {
                case 1:
                    rootView = inflater.inflate(R.layout.activity_discussion_forum, container, false);
                    mFragmentNumber = 1;
                    if (isNetworkAvailable())
                        mDiscussionForum.startLoadCommentsTask();
                    else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Dialog));
                        builder.setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle(R.string.check_network)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                }).show();
                    }
                    break;

                case 2:
                    rootView = inflater.inflate(R.layout.activity_news_feed, container, false);
                    mFragmentNumber = 2;
                    if (isNetworkAvailable())
                        mNews.startLoadNewsTask();
                    else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Dialog));
                        builder.setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle(getString(R.string.check_network))
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                }).show();
                    }
                    break;

                case 3:
                    mFragmentNumber = 3;
                    rootView = inflater.inflate(R.layout.roadmap_layout, container, false);
                    mRoadMapCreation = new RoadMapCreation(getActivity(), rootView);
                    mRoadMapCreation.startRoadMapCreation();
                    break;

                default:
                    break;
            }
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MenuScreen) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

    }
}