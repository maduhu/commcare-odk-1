/*
 * Copyright (C) 2009 University of Washington
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

package org.odk.collect.android.activities;

import java.text.DecimalFormat;
import java.util.Set;

import org.odk.collect.android.R;
import org.odk.collect.android.application.GeoProgressDialog;
import org.odk.collect.android.utilities.GeoUtils;
import org.odk.collect.android.listeners.TimerListener;
import org.odk.collect.android.utilities.ODKTimer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class GeoPointActivity extends Activity implements LocationListener, TimerListener {
    private GeoProgressDialog mLocationDialog;
    private LocationManager mLocationManager;
    private Location mLocation;
    private Set<String> mProviders;

    private int millisToWait = 60000; //allow to accept location after 60 seconds

    private ODKTimer mTimer;

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.get_location));

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        mProviders = GeoUtils.evaluateProviders(mLocationManager);
        
        setupLocationDialog();
        long mLong = -1;
        if(savedInstanceState != null){
            mLong = savedInstanceState.getLong("millisRemaining",-1);
        }
        if(mLong > 0){
            mTimer = new ODKTimer(mLong, this);
        }else{
            mTimer = new ODKTimer(millisToWait, this);
        }
        mTimer.start();

    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();

        // stops the GPS. Note that this will turn off the GPS if the screen goes to sleep.
        mLocationManager.removeUpdates(this);

        // We're not using managed dialogs, so we have to dismiss the dialog to prevent it from
        // leaking memory.
        if (mLocationDialog != null && mLocationDialog.isShowing())
            mLocationDialog.dismiss();
    }


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        mProviders = GeoUtils.evaluateProviders(mLocationManager);
        if (mProviders.isEmpty()) {
            DialogInterface.OnCancelListener onCancelListener = new DialogInterface.OnCancelListener() {
                /*
                 * (non-Javadoc)
                 * @see android.content.DialogInterface.OnCancelListener#onCancel(android.content.DialogInterface)
                 */
                @Override
                public void onCancel(DialogInterface dialog) {
                    mLocation = null;
                    GeoPointActivity.this.finish();
                }
            };

            DialogInterface.OnClickListener onChangeListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int i) {
                    switch (i) {
                        case DialogInterface.BUTTON_POSITIVE:
                            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            mLocation = null;
                            GeoPointActivity.this.finish();
                            break;
                    }
                }
            };
            
            GeoUtils.showNoGpsDialog(this, onChangeListener, onCancelListener);
        } else {
            for (String provider : mProviders) {
                mLocationManager.requestLocationUpdates(provider, 0, 0, this);            
            }
            mLocationDialog.show();
        }
    }


    /**
     * Sets up the look and actions for the progress dialog while the GPS is searching.
     */
    private void setupLocationDialog() {
        // dialog displayed while fetching gps location
        
        OnClickListener cancelButtonListener = new OnClickListener() {
            /*
             * (non-Javadoc)
             * @see android.view.View.OnClickListener#onClick(android.view.View)
             */
            @Override
            public void onClick(View v){
                mLocation = null;
                finish();
            }
        };

        OnClickListener okButtonListener = new OnClickListener() {
            public void onClick(View v){
                returnLocation();
            }
        };

        mLocationDialog = new GeoProgressDialog(this, getString(R.string.found_location), getString(R.string.finding_location));

        // back button doesn't cancel
        mLocationDialog.setCancelable(false);
        mLocationDialog.setImage(getResources().getDrawable(R.drawable.green_check_mark));
        mLocationDialog.setMessage(getString(R.string.please_wait_long));
        mLocationDialog.setOKButton(getString(R.string.accept_location),
            okButtonListener);
        mLocationDialog.setCancelButton(getString(R.string.cancel_location),
            cancelButtonListener);
    }


    private void returnLocation() {
        if (mLocation != null) {
            Intent i = new Intent();
            i.putExtra(FormEntryActivity.LOCATION_RESULT, GeoUtils.locationToString(mLocation));
            setResult(RESULT_OK, i);
        }
        finish();
    }


    /*
     * (non-Javadoc)
     * @see android.location.LocationListener#onLocationChanged(android.location.Location)
     */
    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        if (mLocation != null) {
            mLocationDialog.setMessage(getString(R.string.location_provider_accuracy,
                mLocation.getProvider(), truncateDouble(mLocation.getAccuracy())));

            // If location is accurate, we're done
            if (mLocation.getAccuracy() <= GeoUtils.GOOD_ACCURACY) {
                returnLocation();
            }
            
            // If location isn't great but might be acceptable, notify
            // the user and let them decide whether or not to record it
            mLocationDialog.setLocationFound(
                mLocation.getAccuracy() < GeoUtils.ACCEPTABLE_ACCURACY
                || mTimer.getMillisUntilFinished() == 0
            );
        }
    }


    private String truncateDouble(float number) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(number);
    }


    /*
     * (non-Javadoc)
     * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
     */
    @Override
    public void onProviderDisabled(String provider) {

    }


    /*
     * (non-Javadoc)
     * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
     */
    @Override
    public void onProviderEnabled(String provider) {

    }


    /*
     * (non-Javadoc)
     * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                if (mLocation != null) {
                    mLocationDialog.setMessage(getString(R.string.location_accuracy,
                        (int) mLocation.getAccuracy()));
                }
                break;
            case LocationProvider.OUT_OF_SERVICE:
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                break;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.odk.collect.android.listeners.TimerListener#notifyTimerFinished()
     */
    @Override
    public void notifyTimerFinished() {
        onLocationChanged(mLocation);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong("millisRemaining",mTimer.getMillisUntilFinished());
        super.onSaveInstanceState(savedInstanceState);  
    }
}
