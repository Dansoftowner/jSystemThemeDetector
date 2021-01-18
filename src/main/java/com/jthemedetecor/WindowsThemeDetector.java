package com.jthemedetecor;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Determines the dark/light theme by the windows registry values through JNA.
 * Works on a Windows 10 system.
 *
 * @author Daniel Gyorffy
 */
class WindowsThemeDetector extends OsThemeDetector {

    private static final Logger logger = LoggerFactory.getLogger(WindowsThemeDetector.class);

    private static final String REGISTRY_PATH = "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";
    private static final String REGISTRY_VALUE = "AppsUseLightTheme";

    private final Set<Consumer<Boolean>> listeners = Collections.synchronizedSet(new HashSet<>());
    private DetectorThread detectorThread;

    WindowsThemeDetector() {
    }

    @Override
    public boolean isDark() {
        return Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, REGISTRY_PATH, REGISTRY_VALUE) &&
                Advapi32Util.registryGetIntValue(WinReg.HKEY_CURRENT_USER, REGISTRY_PATH, REGISTRY_VALUE) == 0;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void registerListener(@NotNull Consumer<Boolean> darkThemeListener) {
        Objects.requireNonNull(darkThemeListener);
        final boolean listenerAdded = listeners.add(darkThemeListener);
        final boolean singleListener = listenerAdded && listeners.size() == 1;
        final boolean threadInterrupted = detectorThread != null && detectorThread.isInterrupted();

        if (singleListener || threadInterrupted) {
            this.detectorThread = new DetectorThread(this);
            this.detectorThread.start();
        }
    }

    @Override
    public void removeListener(@Nullable Consumer<Boolean> darkThemeListener) {
        listeners.remove(darkThemeListener);
        if (listeners.isEmpty()) {
            this.detectorThread.interrupt();
            this.detectorThread = null;
        }
    }

    /**
     * Thread implementation for detecting the theme changes
     */
    private static final class DetectorThread extends Thread {

        private final Object lock = new Object();
        private final WindowsThemeDetector themeDetector;

        private boolean lastValue;

        DetectorThread(WindowsThemeDetector themeDetector) {
            this.themeDetector = themeDetector;
            this.lastValue = themeDetector.isDark();
            this.setName("Windows 10 Theme Detector Thread");
            this.setDaemon(true);
            this.setPriority(Thread.NORM_PRIORITY - 1);
        }

        @Override
        public void run() {
            while (!this.isInterrupted()) {
                boolean currentDetection = themeDetector.isDark();
                if (currentDetection != this.lastValue) {
                    lastValue = currentDetection;
                    logger.debug("Theme change detected: dark: {}", currentDetection);
                    for (Consumer<Boolean> listener : themeDetector.listeners) {
                        try {
                            listener.accept(currentDetection);
                        } catch (RuntimeException e) {
                            logger.error("Caught exception during listener notifying ", e);
                        }
                    }
                }

                synchronized (lock) {
                    try {
                        lock.wait(1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }
    }
}
