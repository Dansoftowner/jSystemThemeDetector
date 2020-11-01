package com.jthemedetecor;

import java.util.*;
import java.util.function.Consumer;

/**
 * Thread implementation for listening for changes of the OS UI Theme
 *
 * @author Daniel Gyorffy
 */
class SystemThemeListenerThread extends Thread implements Thread.UncaughtExceptionHandler {

    private static final String NAME = "System Theme Listener Thread";

    private boolean lastValue;
    private boolean terminated;
    private final Set<Consumer<Boolean>> listeners;
    private final SystemThemeDetector systemThemeDetector;

    SystemThemeListenerThread(SystemThemeDetector systemThemeDetector) {
        this.systemThemeDetector = Objects.requireNonNull(systemThemeDetector);
        this.listeners = Collections.synchronizedSet(new HashSet<>());
        this.lastValue = this.systemThemeDetector.isDark();
        this.setName(NAME);
        this.setDaemon(true);
        this.setUncaughtExceptionHandler(this);
        this.setPriority(Thread.NORM_PRIORITY - 1);
    }

    public void addListener(Consumer<Boolean> listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Consumer<Boolean> listener) {
        this.listeners.remove(listener);
    }

    public void removeAllListener() {
        this.listeners.clear();
    }

    public boolean isTerminated() {
        return terminated;
    }

    @Override
    public void run() {
        while (!this.isInterrupted() && !listeners.isEmpty()) {
            boolean lastValue = systemThemeDetector.isDark();
            if (this.lastValue != lastValue) {
                listeners.forEach(listener -> listener.accept(lastValue));
            }

            this.lastValue = lastValue;
        }

        terminated = true;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        terminated = true;
    }
}
