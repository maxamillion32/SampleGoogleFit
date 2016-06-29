package com.sembozdemir.samplegooglefit.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.result.DailyTotalResult;

import java.util.concurrent.TimeUnit;

/**
 * Created by bozdemirs on 29/06/2016.
 */

public class ReadDailyTotalStepsTask extends AsyncTask<Void,Void,Integer> {

    private static final String TAG = ReadDailyTotalStepsTask.class.getSimpleName();

    private final GoogleApiClient client;
    private final Callback callback;

    public interface Callback {
        void onDailyTotalStepsReceived(int steps);
    }

    public ReadDailyTotalStepsTask(Callback callback, GoogleApiClient client) {
        this.callback = callback;
        this.client = client;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        return getDailyTotalSteps();
    }

    @Override
    protected void onPostExecute(Integer steps) {
        super.onPostExecute(steps);
        callback.onDailyTotalStepsReceived(steps);
    }

    private int getDailyTotalSteps() {
        PendingResult<DailyTotalResult> result = Fitness.HistoryApi.
                readDailyTotal(client, DataType.TYPE_STEP_COUNT_DELTA);

        DailyTotalResult totalResult = result.await(30, TimeUnit.SECONDS);
        if (totalResult.getStatus().isSuccess()) {
            DataSet totalSet = totalResult.getTotal();
            return totalSet.isEmpty()
                                ? 0
                                : totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
        } else {
            Log.d(TAG, "Failure: " + totalResult.getStatus().getStatusMessage());
            return -1;
        }
    }
}
