package com.sembozdemir.samplegooglefit;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.patloew.rxfit.RxFit;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by bozdemirs on 27/06/2016.
 */

public abstract class BaseRxFitActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RxFit.init(this,
                new Api[] {Fitness.HISTORY_API, Fitness.RECORDING_API},
                new Scope[]{new Scope(Scopes.FITNESS_ACTIVITY_READ)});
    }

    @Override
    protected void readDailyTotalSteps() {
        RxFit.History.readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA , 30, TimeUnit.SECONDS)
                .flatMapObservable(dataSet -> Observable.from(dataSet.getDataPoints()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> onErrorResponse(throwable))
                .subscribe(dataPoint -> onDailyTotalStepsReceived(dataPoint.getValue(Field.FIELD_STEPS).asInt()));
    }

    @Override
    protected void subscribeDailyTotalSteps() {
        RxFit.Recording.subscribe(DataType.TYPE_STEP_COUNT_DELTA)
                .doOnError(throwable -> onErrorResponse(throwable))
                .subscribe(this::onResponseDailyTotalStepsSubscription);
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
        RxFit.History.read(dataReadRequest, 30, TimeUnit.SECONDS)
                .flatMapObservable(dataReadResult -> Observable.from(dataReadResult.getBuckets()))
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> onErrorResponse(throwable))
                .subscribe(buckets -> {
                    int steps = 0;
                    for (Bucket bucket : buckets) {
                        DataSet dataSet = bucket.getDataSet(DataType.TYPE_STEP_COUNT_DELTA);
                        steps += dataSet.isEmpty()
                                ? 0
                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                    }
                    onWeeklyTotalStepsReceived(steps);
                });
    }

}
