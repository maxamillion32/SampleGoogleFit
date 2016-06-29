package com.sembozdemir.samplegooglefit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.FitnessStatusCodes;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by bozdemirs on 27/06/2016.
 */

public abstract class BaseActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.textview_daily_total_step)
    TextView textViewDailyTotalSteps;

    @BindView(R.id.textview_weekly_steps)
    TextView textViewWeeklySteps;

    @BindView(R.id.button_switch)
    Button buttonSwitch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        ButterKnife.bind(this);
    }

    protected abstract int getLayoutResId();

    protected void onResponseDailyTotalStepsSubscription(Status status) {
        if (status.isSuccess()) {
            if (status.getStatusCode() == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                Log.d("RecordingApi", "Already subscribed to the Recording API");
            } else {
                Log.d("RecordingApi", "Subscribed to the Recording API");
            }
        }
    }

    protected abstract void subscribeDailyTotalSteps();

    protected abstract void readDailyTotalSteps();

    protected abstract void readLastWeekSteps();

    protected abstract void onDailyTotalStepsReceived(int steps);

    protected abstract void onWeeklyTotalStepsReceived(int steps);

    protected abstract void onErrorResponse(Throwable throwable);
}
