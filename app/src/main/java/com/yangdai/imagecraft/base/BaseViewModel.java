package com.yangdai.imagecraft.base;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BaseViewModel extends AndroidViewModel {
    private final List<Uri> uriList = new ArrayList<>();
    private final MutableLiveData<Integer> taskDone = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> finished = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> cancelled = new MutableLiveData<>(false);
    private int taskCount;
    private Runnable runnable = null;
    private boolean firstInit = true;
    private boolean isRunning = false;
    private final MyHandlerThread myHandlerThread;
    public final ExecutorService executor = Executors.newWorkStealingPool();
    public List<Future<Void>> futures = new ArrayList<>();

    public BaseViewModel(@NonNull Application application) {
        super(application);
        myHandlerThread = new MyHandlerThread();
    }

    public Handler getHandler() {
        return myHandlerThread.getHandler();
    }

    public Context getContext() {
        return getApplication().getApplicationContext();
    }

    public void setRunnable(Runnable runnable) {
        if (this.runnable == null) {
            this.runnable = runnable;
        }
    }

    public void start() {
        if (!isRunning) {
            getHandler().post(runnable);
            isRunning = true;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // 移除所有未处理的消息并退出 HandlerThread
        getHandler().removeCallbacks(runnable);
        myHandlerThread.quit();
    }

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public List<Uri> getUriList() {
        return uriList;
    }

    public MutableLiveData<Integer> getTaskDone() {
        return taskDone;
    }

    public void addTaskDone() {
        Integer value = taskDone.getValue();
        if (value != null) {
            taskDone.postValue(value + 1);
        }
    }

    public MutableLiveData<Boolean> getCancelStatus() {
        return cancelled;
    }

    public boolean isCancelled() {
        return Boolean.TRUE.equals(cancelled.getValue());
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled.setValue(cancelled);
    }

    public boolean isFirstInitiated() {
        return firstInit;
    }

    public void setFirstInit(boolean firstInit) {
        this.firstInit = firstInit;
    }

    public MutableLiveData<Boolean> getFinishStatus() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished.postValue(finished);
    }
}
