package com.sembozdemir.samplegooglefit.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by bozdemirs on 29/06/2016.
 */

public class ReadLastWeekStepsTask extends AsyncTask<Void, Void, Integer> {

    private static final String TAG = ReadLastWeekStepsTask.class.getSimpleName();

    private final GoogleApiClient client;
    private final Callback callback;

    public interface Callback {
        void onLastWeekStepsReceived(int steps);
    }

    public ReadLastWeekStepsTask(Callback callback, GoogleApiClient client) {
        this.client = client;
        this.callback = callback;
    }
    @Override
    protected Integer doInBackground(Void... params) {
        return getLastWeekSteps();
    }

    @Override
    protected void onPostExecute(Integer steps) {
        super.onPostExecute(steps);
        callback.onLastWeekStepsReceived(steps);
    }

    private int getLastWeekSteps() {
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

        PendingResult<DataReadResult> result = Fitness.HistoryApi.readData(client, dataReadRequest);

        DataReadResult readDataResult = result.await();
        if (readDataResult.getStatus().isSuccess()) {
            DataSet dataSet = readDataResult.getBuckets().get(0).getDataSet(DataType.TYPE_STEP_COUNT_DELTA);
            return dataSet.isEmpty()
                    ? 0
                    : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
        } else {
            Log.d(TAG, "Failure: " + readDataResult.getStatus().getStatusMessage());
            return -1;
        }
    }
}
