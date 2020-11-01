package com.jthemedetecor;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import java.util.*;
import java.util.function.Consumer;

class SystemInfo {
    static boolean isWin10() {
        return System.getProperty("os.name").startsWith("Windows") &&
                Double.parseDouble(System.getProperty("os.version")) >= 10;
    }
}

/**
 * A {@link SystemThemeDetector} can detect whether the current system uses dark theme or not.
 *
 * @author Daniel Gyorffy
 */
public abstract class SystemThemeDetector {

    private static SystemThemeDetector detector;

    /**
     * Don't let anyone to instantiate or inherit this class.
     */
    private SystemThemeDetector() {
    }

    /**
     * Returns that the os using a dark or a light theme.
     *
     * @return {@code true} if the os uses dark theme; {@code false} otherwise.
     */
    public abstract boolean isDark();

    /**
     * Registers a {@link Consumer} that will listen to a theme-change.
     *
     * @param darkThemeListener the {@link Consumer} that accepts a {@link Boolean} that represents
     *                          that the os using a dark theme or not
     */
    public abstract void registerListener(Consumer<Boolean> darkThemeListener);

    /**
     * Removes the listener.
     */
    public abstract void removeListener(Consumer<Boolean> darkThemeListener);

    /**
     * Removes all listeners.
     */
    public abstract void removeAllListeners();

    /**
     * Returns the right {@link SystemThemeDetector} designed for the current OS.
     *
     * @return the {@link SystemThemeDetector} implementation
     */
    public synchronized static SystemThemeDetector getDetector() {
        if (detector != null) return detector;
        else if (SystemInfo.isWin10()) return detector = new WindowsThemeDetector();
        else return detector = new EmptyThemeDetector();
    }

    private static final class WindowsThemeDetector extends SystemThemeDetector {

        private static final String REGISTRY_PATH = "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";
        private static final String REGISTRY_VALUE = "AppsUseLightTheme";

        private final Set<Consumer<Boolean>> listeners;
        private SystemThemeListenerThread listenerThread;

        private WindowsThemeDetector() {
            this.listeners = Collections.synchronizedSet(new HashSet<>());
            this.listenerThread = new SystemThemeListenerThread(this);
        }

        @Override
        public boolean isDark() {
            try {
                return Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, REGISTRY_PATH, REGISTRY_VALUE) &&
                        Advapi32Util.registryGetIntValue(WinReg.HKEY_CURRENT_USER, REGISTRY_PATH, REGISTRY_VALUE) == 0;
            } catch (RuntimeException e) {
                return false;
            }
        }

        @Override
        public void registerListener(Consumer<Boolean> darkThemeListener) {
            Objects.requireNonNull(darkThemeListener, "Listener shouldn't be null");
            if (this.listeners.add(darkThemeListener)) {
                startWorker(darkThemeListener);
            }
        }

        @Override
        public void removeListener(Consumer<Boolean> darkThemeListener) {
            this.removeFromWorker(darkThemeListener);
            this.listeners.remove(darkThemeListener);
        }

        @Override
        public void removeAllListeners() {
            removeAllListenersFromWorker();
            this.listeners.clear();
        }

        private void startWorker(Consumer<Boolean> listener) {
            if (this.listenerThread.isTerminated() || this.listenerThread.isInterrupted()) {
                this.listenerThread = new SystemThemeListenerThread(this);
                this.listenerThread.addListener(listener);
                this.listenerThread.start();
            } else if (!this.listenerThread.isAlive()){
                this.listenerThread.addListener(listener);
                this.listenerThread.start();
            }
        }

        private void removeFromWorker(Consumer<Boolean> listener) {
            if (!this.listenerThread.isTerminated())
                this.listenerThread.removeListener(listener);
        }

        private void removeAllListenersFromWorker() {
            if (!this.listenerThread.isTerminated())
                this.listenerThread.removeAllListener();
        }
    }

    private static final class EmptyThemeDetector extends SystemThemeDetector {
        @Override
        public boolean isDark() {
            return false;
        }

        @Override
        public void registerListener(Consumer<Boolean> darkThemeListener) {
        }

        @Override
        public void removeListener(Consumer<Boolean> darkThemeListener) {
        }

        @Override
        public void removeAllListeners() {
        }
    }

}
