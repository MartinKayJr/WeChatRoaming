package cn.martinkay.wechatroaming.settings.step;

import androidx.annotation.Nullable;

public interface Step extends Comparable<Step> {

    /**
     * Execute this step, de-obfuscate the dex file. This method takes a long time, so it should not be called in the
     * main thread.
     *
     * @return true if the step is done successfully, false otherwise
     */
    boolean step();

    boolean isDone();

    /**
     * Get the priority of this step.
     * <p>
     * Step with a higher priority will be executed earlier.
     */
    int getPriority();

    @Nullable
    String getDescription();

    @Override
    default int compareTo(Step o) {
        return this.getPriority() - o.getPriority();
    }
}
