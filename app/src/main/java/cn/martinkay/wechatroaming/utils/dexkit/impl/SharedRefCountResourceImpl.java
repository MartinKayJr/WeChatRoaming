package cn.martinkay.wechatroaming.utils.dexkit.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class SharedRefCountResourceImpl<R> {

    private R mResources = null;
    private final ReadWriteLock mReleaseLock = new ReentrantReadWriteLock();
    private final Object mLock = new Object();
    private int mRefCount = 0;

    public ReadWriteLock increaseRefCount() {
        synchronized (mLock) {
            if (mResources == null) {
                mResources = openResourceInternal();
            }
            mRefCount++;
            return mReleaseLock;
        }
    }

    @NonNull
    protected abstract R openResourceInternal();

    protected abstract void closeResourceInternal(@NonNull R res);

    public void decreaseRefCount() {
        synchronized (mLock) {
            mRefCount--;
            if (mRefCount == 0) {
                Lock lock = mReleaseLock.writeLock();
                lock.lock();
                try {
                    closeResourceInternal(mResources);
                    mResources = null;
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    @Nullable
    public R getResources() {
        return mResources;
    }

}
