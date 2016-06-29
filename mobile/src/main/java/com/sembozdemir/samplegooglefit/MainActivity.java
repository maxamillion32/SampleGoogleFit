package com.sembozdemir.samplegooglefit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import butterknife.OnClick;

/**
 * Created by bozdemirs on 29/06/2016.
 */

public class MainActivity extends BaseFitActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(toolbar);
        buttonSwitch.setText("Launch RxMainActivity");
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @OnClick(R.id.button_switch)
    public void onClickButtonSwitchActivity() {
        Intent i = new Intent(this, RxMainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onFitnessApiAvailable() {
        subscribeDailyTotalSteps();

        readDailyTotalSteps();
        readLastWeekSteps();
    }

    @Override
    protected void onDailyTotalStepsReceived(int steps) {
        textViewDailyTotalSteps.setText(String.valueOf(steps));
    }

    @Override
    protected void onWeeklyTotalStepsReceived(int steps) {
        textViewWeeklySteps.setText(String.valueOf(steps));
    }

    @Override
    protected void onErrorResponse(Throwable throwable) {
        Log.e(TAG, "Error getting fit data. " + throwable.getMessage());
    }
}
