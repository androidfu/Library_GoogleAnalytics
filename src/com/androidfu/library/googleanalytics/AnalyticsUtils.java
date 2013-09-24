package com.androidfu.library.googleanalytics;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * Helper singleton class for the Google Analytics tracking library.
 */
public class AnalyticsUtils {

    @SuppressWarnings("unused")
    private static final String TAG = AnalyticsUtils.class.getSimpleName();
    private static final String FIRST_RUN_KEY = "first_run";
    private static final int VISITOR_SCOPE = 1;
    private static final boolean ANALYTICS_ENABLED = true;
    private static AnalyticsUtils _instance;
    private GoogleAnalyticsTracker _tracker;
    private Context _applicationContext;
    private static String _UACODE;

    /**
     * Call this from your application class before using trackPage() or
     * trackEvent()
     * 
     * @param uacode
     *            GoogleAnalytics key
     */
    public static void configure(String uacode) {
        _UACODE = uacode;
    }

    /**
     * Returns the global {@link AnalyticsUtils} singleton object, creating one
     * if necessary.
     */
    public static AnalyticsUtils getInstance(Context context) {
        if (!ANALYTICS_ENABLED) {
            return sEmptyAnalyticsUtils;
        }
        if (_instance == null) {
            if (context == null) {
                return sEmptyAnalyticsUtils;
            }
            _instance = new AnalyticsUtils(context);
        }
        return _instance;
    }

    @SuppressWarnings("deprecation")
    private AnalyticsUtils(Context context) {
        if (context == null) {
            // This should only occur for the empty AnalyticsUtils object.
            return;
        }
        _applicationContext = context.getApplicationContext();
        _tracker = GoogleAnalyticsTracker.getInstance();
        // Unfortunately this needs to be synchronous.
        _tracker.start(_UACODE, 300, _applicationContext);
        //Log.d(TAG, "Initializing Analytics");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_applicationContext);
        final boolean firstRun = prefs.getBoolean(FIRST_RUN_KEY, true);
        if (firstRun) {
            //Log.d(TAG, "Analytics firstRun");
            String apiLevel = Build.VERSION.SDK;
            String model = Build.MODEL;
            _tracker.setCustomVar(1, "apiLevel", apiLevel, VISITOR_SCOPE);
            _tracker.setCustomVar(2, "model", model, VISITOR_SCOPE);
            prefs.edit().putBoolean(FIRST_RUN_KEY, false).commit();
        }
    }

    public void trackEvent(final String category, final String action, final String label, final int value) {
        // We wrap the call in an AsyncTask since the Google Analytics library
        // writes to disk on its calling thread.
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    _tracker.trackEvent(category, action, label, value);
                    //Log.d(TAG, "Analytics trackEvent: " + category + " / " + action + " / " + label + " / " + value);
                } catch (Exception e) {
                    // We don't want to crash if there's an Analytics library
                    // exception.
                    //Log.w(TAG, "Analytics trackEvent error: " + category + " / " + action + " / " + label + " / " + value, e);
                }
                return null;
            }
        }.execute();
    }

    public void trackPageView(final String path) {
        // We wrap the call in an AsyncTask since the Google Analytics library
        // writes to disk on its calling thread.
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    _tracker.trackPageView(path);
                    //Log.d(TAG, "Analytics trackPageView: " + path);
                } catch (Exception e) {
                    // We don't want to crash if there's an Analytics library
                    // exception.
                    //Log.w(TAG, "Analytics trackPageView error: " + path, e);
                }
                return null;
            }
        }.execute();
    }

    /**
     * Empty instance for use when Analytics is disabled or there was no Context
     * available.
     */
    private static AnalyticsUtils sEmptyAnalyticsUtils = new AnalyticsUtils(null) {

        @Override
        public void trackEvent(String category, String action, String label, int value) {
        }

        @Override
        public void trackPageView(String path) {
        }
    };
}