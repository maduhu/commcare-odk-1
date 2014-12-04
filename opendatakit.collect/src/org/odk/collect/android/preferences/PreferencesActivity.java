/*
 * Copyright (C) 2011 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.preferences;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.AccountList;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.UrlUtils;
import org.odk.collect.android.utilities.WebUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.MediaStore.Images;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Toast;

/**
 * @author yanokwa
 */
public class PreferencesActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    protected static final int IMAGE_CHOOSER = 0;

    public static String KEY_LAST_VERSION = "lastVersion";
    public static String KEY_FIRST_RUN = "firstRun";
    public static String KEY_SHOW_SPLASH = "showSplash";
    public static String KEY_SPLASH_PATH = "splashPath";
    public static String KEY_FONT_SIZE = "font_size";
    public static String KEY_SELECTED_GOOGLE_ACCOUNT = "selected_google_account";
    public static String KEY_GOOGLE_SUBMISSION = "google_submission_id";

    public static String KEY_SERVER_URL = "server_url";
    public static String KEY_USERNAME = "username";
    public static String KEY_PASSWORD = "password";

    public static String KEY_PROTOCOL = "protocol";
    public static String KEY_FORMLIST_URL = "formlist_url";
    public static String KEY_SUBMISSION_URL = "submission_url";

    public static String KEY_COMPLETED_DEFAULT = "default_completed";
    
    public static String KEY_SHOW_START_SCREEN = "odk_show_entry_screen";
    public static String KEY_HELP_MODE_TRAY = "help_mode_tray";
    public static String KEY_PROGRESS_BAR = "progress_bar";
    public static String KEY_NAVIGATION_BAR = "pref_nav_bar";
    

    public static String KEY_AUTH = "auth";
    public static String KEY_ACCOUNT = "account";
    
    public static String KEY_SERVER_PREFS = "serverprefs";

    public static String googleServerBaseUrl = "https://gather.apis.google.com/odk/n/";
    
    private PreferenceScreen mSplashPathPreference;
    private EditTextPreference mSubmissionUrlPreference;
    private EditTextPreference mFormListUrlPreference;
    private EditTextPreference mServerUrlPreference;
    private EditTextPreference mUsernamePreference;
    private EditTextPreference mPasswordPreference;
    private PreferenceScreen mSelectedGoogleAccountPreference;
    private EditTextPreference mGoogleCollectionEffortPreference;
    private Context mContext;


    /*
     * (non-Javadoc)
     * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mContext = this;

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.general_preferences));

        if(Collect.getInstance() != null) {
            setupSplashPathPreference();
            setupSelectedGoogleAccountPreference();
    
            updateServerUrl();
    
            updateUsername();
            updatePassword();
    
            updateFormListUrl();
            updateSubmissionUrl();
    
            updateSplashPath();
            
            updateProtocol();
            updateSelectedGoogleAccount();
            updateGoogleCollectionEffort();
        } else {
            //If there's no collect instance we're running in a library, so we should
            //hide everything that's irrelevant
            this.getPreferenceScreen().removePreference(this.findPreference(KEY_SERVER_PREFS));
        }
        updateFontSize();
        updateShowStart();
    }


    private void setupSplashPathPreference() {
        mSplashPathPreference = (PreferenceScreen) findPreference(KEY_SPLASH_PATH);

        if (mSplashPathPreference != null) {
            mSplashPathPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                private void launchImageChooser() {
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.setType("image/*");
                    startActivityForResult(i, PreferencesActivity.IMAGE_CHOOSER);
                }


                /*
                 * (non-Javadoc)
                 * @see android.preference.Preference.OnPreferenceClickListener#onPreferenceClick(android.preference.Preference)
                 */
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // if you have a value, you can clear it or select new.
                    CharSequence cs = mSplashPathPreference.getSummary();
                    if (cs != null && cs.toString().contains("/")) {

                        final CharSequence[] items =
                            {
                                    getString(R.string.select_another_image),
                                    getString(R.string.use_odk_default)
                            };

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle(getString(R.string.change_splash_path));
                        builder.setNeutralButton(getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                        		/*
                        		 * (non-Javadoc)
                        		 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
                        		 */
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                        	/*
                        	 * (non-Javadoc)
                        	 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
                        	 */
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if (items[item].equals(getString(R.string.select_another_image))) {
                                    launchImageChooser();
                                } else {
                                    setSplashPath(getString(R.string.default_splash_path));

                                }
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();

                    } else {
                        launchImageChooser();
                    }

                    return true;
                }
            });
        }
    }


    private void setSplashPath(String path) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SPLASH_PATH, path);
        editor.commit();
    }


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
            this);
    }


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        
        if(Collect.getInstance() != null) {
            
            updateServerUrl();
    
            updateUsername();
            updatePassword();
    
            updateFormListUrl();
            updateSubmissionUrl();
    
            updateSplashPath();
    
            updateProtocol();
            updateSelectedGoogleAccount();
            updateGoogleCollectionEffort();
        }
        updateFontSize();
        updateShowStart();
    }


    /*
     * (non-Javadoc)
     * @see android.preference.PreferenceActivity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_CANCELED) {
            // request was canceled, so do nothing
            return;
        }

        switch (requestCode) {
            case IMAGE_CHOOSER:
                String sourceImagePath = null;

                // get gp of chosen file
                Uri uri = intent.getData();
                if (uri.toString().startsWith("file")) {
                    sourceImagePath = uri.toString().substring(6);
                } else {
                    String[] projection = {
                        Images.Media.DATA
                    };
                    Cursor c = managedQuery(uri, projection, null, null, null);
                    startManagingCursor(c);
                    int i = c.getColumnIndexOrThrow(Images.Media.DATA);
                    c.moveToFirst();
                    sourceImagePath = c.getString(i);
                }

                // setting image path
                setSplashPath(sourceImagePath);
                updateSplashPath();
                break;
        }
    }


    /*
     * (non-Javadoc)
     * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PROTOCOL)) {
            updateProtocol();
            updateSelectedGoogleAccount();
            updateGoogleCollectionEffort();
            updateServerUrl();
            updateUsername();
            updatePassword();
            updateFormListUrl();
            updateSubmissionUrl();
        } else if (key.equals(KEY_SELECTED_GOOGLE_ACCOUNT)) {
            updateSelectedGoogleAccount();
            updateGoogleCollectionEffort();
            updateServerUrl();
        } else if (key.equals(KEY_GOOGLE_SUBMISSION)) {
            updateSelectedGoogleAccount();
            updateGoogleCollectionEffort();
            updateServerUrl();
        } else if (key.equals(KEY_SERVER_URL)) {
            updateServerUrl();
        } else if (key.equals(KEY_FORMLIST_URL)) {
            updateFormListUrl();
        } else if (key.equals(KEY_SUBMISSION_URL)) {
            updateSubmissionUrl();
        } else if (key.equals(KEY_USERNAME)) {
            updateUsername();
        } else if (key.equals(KEY_PASSWORD)) {
            updatePassword();
        } else if (key.equals(KEY_SPLASH_PATH)) {
            updateSplashPath();
        } else if (key.equals(KEY_FONT_SIZE)) {
            updateFontSize();
        } else if(key.equals(KEY_SHOW_START_SCREEN)) {
            updateShowStart();
        }
    }


    private void validateUrl(EditTextPreference preference) {
        if (preference != null) {
            String url = preference.getText();
            if (UrlUtils.isValidUrl(url)) {
                preference.setText(url);
                preference.setSummary(url);
            } else {
                // preference.setText((String) preference.getSummary());
                Toast.makeText(getApplicationContext(), getString(R.string.url_error),
                    Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void updateServerUrl() {
        mServerUrlPreference = (EditTextPreference) findPreference(KEY_SERVER_URL);

        // remove all trailing "/"s
        while (mServerUrlPreference.getText().endsWith("/")) {
            mServerUrlPreference.setText(mServerUrlPreference.getText().substring(0,
                mServerUrlPreference.getText().length() - 1));
        }
        validateUrl(mServerUrlPreference);
        mServerUrlPreference.setSummary(mServerUrlPreference.getText());

        mServerUrlPreference.getEditText().setFilters(new InputFilter[] {
            getReturnFilter()
        });
    }


    private void updateSplashPath() {
        mSplashPathPreference = (PreferenceScreen) findPreference(KEY_SPLASH_PATH);
        mSplashPathPreference.setSummary(mSplashPathPreference.getSharedPreferences().getString(
            KEY_SPLASH_PATH, getString(R.string.default_splash_path)));
    }


    private void updateUsername() {
        mUsernamePreference = (EditTextPreference) findPreference(KEY_USERNAME);
        mUsernamePreference.setSummary(mUsernamePreference.getText());

        mUsernamePreference.getEditText().setFilters(new InputFilter[] {
            getWhitespaceFilter()
        });

        WebUtils.clearAllCredentials();
    }


    private void updatePassword() {
        mPasswordPreference = (EditTextPreference) findPreference(KEY_PASSWORD);
        if (mPasswordPreference.getText() != null && mPasswordPreference.getText().length() > 0) {
            mPasswordPreference.setSummary("********");
        } else {
            mPasswordPreference.setSummary("");

        }
        mPasswordPreference.getEditText().setFilters(new InputFilter[] {
            getWhitespaceFilter()
        });

        WebUtils.clearAllCredentials();
    }


    private void updateFormListUrl() {
        mFormListUrlPreference = (EditTextPreference) findPreference(KEY_FORMLIST_URL);
        mFormListUrlPreference.setSummary(mFormListUrlPreference.getText());

        mFormListUrlPreference.getEditText().setFilters(new InputFilter[] {
            getReturnFilter()
        });
    }


    private void updateSubmissionUrl() {
        mSubmissionUrlPreference = (EditTextPreference) findPreference(KEY_SUBMISSION_URL);
        mSubmissionUrlPreference.setSummary(mSubmissionUrlPreference.getText());

        mSubmissionUrlPreference.getEditText().setFilters(new InputFilter[] {
            getReturnFilter()
        });
    }


    private void updateFontSize() {
        ListPreference lp = (ListPreference) findPreference(KEY_FONT_SIZE);
        lp.setSummary(lp.getEntry());
    }
    
    private void updateShowStart() {
//        ListPreference lp = (ListPreference) findPreference(KEY_SHOW_START_SCREEN);
//        lp.setSummary(lp.getEntry());
    }


    private void updateSelectedGoogleAccount() {
        mSelectedGoogleAccountPreference =
            (PreferenceScreen) findPreference(KEY_SELECTED_GOOGLE_ACCOUNT);
        mSelectedGoogleAccountPreference.setSummary(mSelectedGoogleAccountPreference
                .getSharedPreferences().getString(KEY_ACCOUNT, ""));
    }


    private void updateGoogleCollectionEffort() {
        mGoogleCollectionEffortPreference =
            (EditTextPreference) findPreference(KEY_GOOGLE_SUBMISSION);
        mGoogleCollectionEffortPreference.setSummary(mGoogleCollectionEffortPreference
                .getSharedPreferences().getString(KEY_GOOGLE_SUBMISSION, ""));

        // We have a fixed URL for using Google's service.
        if (((ListPreference) findPreference(KEY_PROTOCOL)).getValue().equals("google")) {
            String submissionId =
                ((EditTextPreference) findPreference(KEY_GOOGLE_SUBMISSION)).getText();
            mServerUrlPreference.setText(googleServerBaseUrl + submissionId);
            updateServerUrl();
        }
    }


    private void setupSelectedGoogleAccountPreference() {
        mSelectedGoogleAccountPreference =
            (PreferenceScreen) findPreference(KEY_SELECTED_GOOGLE_ACCOUNT);

        if (mSelectedGoogleAccountPreference == null) {
            return;
        }

        updateSelectedGoogleAccount();

        mSelectedGoogleAccountPreference
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                	/*
                	 * (non-Javadoc)
                	 * @see android.preference.Preference.OnPreferenceClickListener#onPreferenceClick(android.preference.Preference)
                	 */
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent i = new Intent(getApplicationContext(), AccountList.class);
                        startActivity(i);
                        return true;
                    }
                });
    }


    private void updateProtocol() {
        ListPreference lp = (ListPreference) findPreference(KEY_PROTOCOL);
        lp.setSummary(lp.getEntry());

        String protocol = lp.getValue();
        if (protocol.equals("odk_default")) {
            if (mGoogleCollectionEffortPreference != null) {
                mGoogleCollectionEffortPreference.setEnabled(false);
            }
            if (mSelectedGoogleAccountPreference != null) {
                mSelectedGoogleAccountPreference.setEnabled(false);
            }
            if (mServerUrlPreference != null) {
                mServerUrlPreference.setEnabled(true);
            }
            if (mUsernamePreference != null) {
                mUsernamePreference.setEnabled(true);
            }
            if (mPasswordPreference != null) {
                mPasswordPreference.setEnabled(true);
            }
            if (mFormListUrlPreference != null) {
                mFormListUrlPreference.setText(getText(R.string.default_odk_formlist).toString());
                mFormListUrlPreference.setEnabled(false);
            }
            if (mSubmissionUrlPreference != null) {
                mSubmissionUrlPreference.setText(getText(R.string.default_odk_submission)
                        .toString());
                mSubmissionUrlPreference.setEnabled(false);
            }

        } else if (protocol.equals("google")) {
            if (mGoogleCollectionEffortPreference != null) {
                mGoogleCollectionEffortPreference.setEnabled(true);
            }
            if (mSelectedGoogleAccountPreference != null) {
                mSelectedGoogleAccountPreference.setEnabled(true);
            }
            if (mServerUrlPreference != null) {
                mServerUrlPreference.setEnabled(false);
            }
            if (mUsernamePreference != null) {
                mUsernamePreference.setEnabled(false);
            }
            if (mPasswordPreference != null) {
                mPasswordPreference.setEnabled(false);
            }
            if (mFormListUrlPreference != null) {
                mFormListUrlPreference.setEnabled(false);
            }
            if (mSubmissionUrlPreference != null) {
                mSubmissionUrlPreference.setEnabled(false);
            }

            updateSelectedGoogleAccount();
            updateGoogleCollectionEffort();

        } else {
            if (mGoogleCollectionEffortPreference != null) {
                mGoogleCollectionEffortPreference.setEnabled(false);
            }
            if (mSelectedGoogleAccountPreference != null) {
                mSelectedGoogleAccountPreference.setEnabled(false);
            }
            if (mServerUrlPreference != null) {
                mServerUrlPreference.setEnabled(true);
            }
            if (mUsernamePreference != null) {
                mUsernamePreference.setEnabled(true);
            }
            if (mPasswordPreference != null) {
                mPasswordPreference.setEnabled(true);
            }
            if (mFormListUrlPreference != null) {
                mFormListUrlPreference.setEnabled(true);
            }
            if (mSubmissionUrlPreference != null) {
                mSubmissionUrlPreference.setEnabled(true);
            }

        }

    }


    private InputFilter getWhitespaceFilter() {
        InputFilter whitespaceFilter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                    int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (Character.isWhitespace(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        return whitespaceFilter;
    }


    private InputFilter getReturnFilter() {
        InputFilter returnFilter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                    int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (Character.getType((source.charAt(i))) == Character.CONTROL) {
                        return "";
                    }
                }
                return null;
            }
        };
        return returnFilter;
    }
    
    public enum ProgressBarMode {
        None (false, false),
        ProgressOnly (true, false),
        NavBar (true, true),
        //NOTE: We can't actually handle this yet
        NavBarNoProgress (false, true);
        
        boolean progress = false;
        boolean nav = false;
        
        private ProgressBarMode(boolean progress, boolean nav) { this.progress = progress; this.nav = nav; }
        
        public boolean useNavigationBar() { return nav;} 
        public boolean useProgressBar() { return progress; }
    }
    
    public static ProgressBarMode getProgressBarMode(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean navBar = prefs.getBoolean(KEY_NAVIGATION_BAR, false);
        boolean progressBar = prefs.getBoolean(KEY_PROGRESS_BAR, true);
        if(!navBar && !progressBar) { return ProgressBarMode.None;}
        else if(!navBar && progressBar) { return ProgressBarMode.ProgressOnly;}
        else if(navBar && !progressBar) { return ProgressBarMode.NavBarNoProgress;}
        else { return ProgressBarMode.NavBar;}
    }


    public static boolean showFirstScreen(Context context) {
        return !PreferencesActivity.getProgressBarMode(context).useNavigationBar() && 
                PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesActivity.KEY_SHOW_START_SCREEN, true);
    }
    
        
}
