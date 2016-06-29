package com.sembozdemir.samplegooglefit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import butterknife.OnClick;

public class RxMainActivity extends BaseRxFitActivity {

    private static final String TAG = RxMainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(toolbar);
        buttonSwitch.setText("Launch MainActivity");

        subscribeDailyTotalSteps();

        readDailyTotalSteps();
        readLastWeekSteps();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @OnClick(R.id.button_switch)
    public void onClickButtonSwitchActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
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
