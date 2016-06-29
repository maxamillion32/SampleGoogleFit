package com.sembozdemir.samplegooglefit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResult;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by bozdemirs on 27/06/2016.
 */

public abstract class BaseFitActivity extends BaseActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = BaseFitActivity.class.getSimpleName();

    private GoogleApiClient client = null;

    @Override
    protected void onResume() {
        super.onResume();

        buildFitnessClient();
    }

    protected void buildFitnessClient() {
        if (client == null) {
            client = new GoogleApiClient.Builder(this)
                    .addApi(Fitness.HISTORY_API)
                    .addApi(Fitness.RECORDING_API)
                    .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                    .addConnectionCallbacks(this)
                    .enableAutoManage(this, 0, this)
                    .build();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Now you can make calls to the Fitness APIs
        onFitnessApiAvailable();
    }

    protected abstract void onFitnessApiAvailable();

    @Override
    public void onConnectionSuspended(int cause) {
        // If your connection to the sensor gets lost at some point,
        // you'll be able to determine the reason and react to it here.
        if (cause == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
            Log.i(TAG, "Connection lost.  Cause: Network Lost.");
        } else if (cause
                == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
            Log.i(TAG,
                    "Connection lost.  Reason: Service Disconnected");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "Google Play services connection failed. Cause: " +
                result.toString());
    }

    @Override
    protected void subscribeDailyTotalSteps() {
        Fitness.RecordingApi.subscribe(client, DataType.TYPE_STEP_COUNT_DELTA)
                .setResultCallback(status -> onResponseDailyTotalStepsSubscription(status));
    }

    @Override
    protected void readDailyTotalSteps() {
        Fitness.HistoryApi.readDailyTotal(client, DataType.TYPE_STEP_COUNT_DELTA)
                .setResultCallback(new ResultCallback<DailyTotalResult>() {
            @Override
            public void onResult(@NonNull DailyTotalResult result) {
                int steps = -1;
                if (result.getStatus().isSuccess()) {
                    DataSet totalSet = result.getTotal();
                    steps = totalSet.isEmpty()
                            ? 0
                            : totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                } else {
                    Log.d(TAG, "Failure: " + result.getStatus().getStatusMessage());
                }

                onDailyTotalStepsReceived(steps);
            }
        }, 30, TimeUnit.SECONDS);
    }

    @Override
    protected void readLastWeekSteps() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        DataReadRequest dataReadRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.HistoryApi.readData(client, dataReadRequest)
                .setResultCallback(new ResultCallback<DataReadResult>() {
            @Override
            public void onResult(@NonNull DataReadResult result) {
                int steps = 0;
                if (result.getStatus().isSuccess()) {
                    if (result.getBuckets().size() > 0) {
                        for (Bucket bucket : result.getBuckets()) {
                            DataSet dataSet = bucket.getDataSet(DataType.TYPE_STEP_COUNT_DELTA);
                            steps += dataSet.isEmpty()
                                    ? 0
                                    : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                        }
                    }
                } else {
                    Log.d(TAG, "Failure: " + result.getStatus().getStatusMessage());
                }

                onWeeklyTotalStepsReceived(steps);
            }
        });
    }
}
