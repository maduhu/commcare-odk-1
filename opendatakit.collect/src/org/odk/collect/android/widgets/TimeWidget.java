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

package org.odk.collect.android.widgets;

import java.util.Date;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
import org.odk.collect.android.listeners.WidgetChangedListener;

import android.content.Context;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

/**
 * Displays a TimePicker widget.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class TimeWidget extends QuestionWidget implements OnTimeChangedListener {

    private TimePicker mTimePicker;


    public TimeWidget(Context context, final FormEntryPrompt prompt) {
        super(context, prompt);

        mTimePicker = new TimePicker(getContext());
        mTimePicker.setFocusable(!prompt.isReadOnly());
        mTimePicker.setEnabled(!prompt.isReadOnly());

        mTimePicker.setOnTimeChangedListener(this);
        
        String clockType =
            android.provider.Settings.System.getString(context.getContentResolver(),
                android.provider.Settings.System.TIME_12_24);
        if (clockType == null || clockType.equalsIgnoreCase("24")) {
            mTimePicker.setIs24HourView(true);
        }
        
        // If there's an answer, use it.
        setAnswer();

        setGravity(Gravity.LEFT);
        addView(mTimePicker);

    }
    
    public void setAnswer() {
        // If there's an answer, use it.
        if (mPrompt.getAnswerValue() != null) {

            // create a new date time from date object using default time zone
            DateTime ldt =
                new DateTime(((Date) ((TimeData) getCurrentAnswer()).getValue()).getTime());
            System.out.println("retrieving:" + ldt);

            int altVal = ldt.getHourOfDay() == 1 ? 2 : 1;
            mTimePicker.setCurrentHour(altVal);
            mTimePicker.setCurrentHour(ldt.getHourOfDay());
            
            altVal = ldt.getMinuteOfHour() == 1 ? 2 : 1;
            mTimePicker.setCurrentMinute(altVal);
            mTimePicker.setCurrentMinute(ldt.getMinuteOfHour());
        } else {
            // create time widget with current time as of right now
            clearAnswer();
        }
    }


    /*
     * (non-Javadoc)
     * @see org.odk.collect.android.widgets.QuestionWidget#clearAnswer()
     * Resets time to today.
     */
    @Override
    public void clearAnswer() {
        DateTime ldt = new DateTime();
        mTimePicker.setCurrentHour(ldt.getHourOfDay());
        mTimePicker.setCurrentMinute(ldt.getMinuteOfHour());
    }

    /*
     * (non-Javadoc)
     * @see org.odk.collect.android.widgets.QuestionWidget#getAnswer()
     */
    @Override
    public IAnswerData getAnswer() {
        mTimePicker.clearFocus();
        // use picker time, convert to epoch date (for TZ clarity), store as utc
        
        //CTS - 8/22/2021 : Adjusted this to store as the time past the Epoch, since the app otherwise can have conflicting
        //timezones with the JavaRosa Time storage, which is always stored against the epoch.
        DateTime ldt =
            (new DateTime(0)).withTime(mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute(),
                0, 0);
        //DateTime utc = ldt.withZone(DateTimeZone.forID("UTC"));
        System.out.println("storing:" + ldt);
        return new TimeData(ldt.toDate());
    }


    /*
     * (non-Javadoc)
     * @see org.odk.collect.android.widgets.QuestionWidget#setFocus(android.content.Context)
     */
    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }


    /*
     * (non-Javadoc)
     * @see org.odk.collect.android.widgets.QuestionWidget#setOnLongClickListener(android.view.View.OnLongClickListener)
     */
    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mTimePicker.setOnLongClickListener(l);
    }


    /*
     * (non-Javadoc)
     * @see org.odk.collect.android.widgets.QuestionWidget#cancelLongPress()
     */
    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mTimePicker.cancelLongPress();
    }

    /*
     * (non-Javadoc)
     * @see android.widget.TimePicker.OnTimeChangedListener#onTimeChanged(android.widget.TimePicker, int, int)
     */
    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        this.widgetEntryChanged();
    }
}
