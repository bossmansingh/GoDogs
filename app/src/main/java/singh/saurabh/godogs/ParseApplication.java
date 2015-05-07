package singh.saurabh.godogs;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by ${SAURBAH} on ${10/29/14}.
 */
public class ParseApplication extends Application {
    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     * If you override this method, be sure to call super.onCreate().
     */
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this, Keys.APP_ID, Keys.CLIENT_ID);
    }
}
