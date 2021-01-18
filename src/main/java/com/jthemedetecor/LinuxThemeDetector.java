package com.jthemedetecor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Used for detecting the dark theme on a Linux (GNOME/GTK) system.
 * Tested on Ubuntu.
 *
 * @author Daniel Gyorffy
 */
class LinuxThemeDetector extends OsThemeDetector {

    private static final Logger logger = LoggerFactory.getLogger(LinuxThemeDetector.class);

    private static final String MONITORING_CMD = "gsettings monitor org.gnome.desktop.interface gtk-theme";
    private static final String GET_CMD = "gsettings get org.gnome.desktop.interface gtk-theme";

    private final Set<Consumer<Boolean>> listeners = Collections.synchronizedSet(new HashSet<>());
    private final Pattern darkThemeNamePattern = Pattern.compile(".*dark.*", Pattern.CASE_INSENSITIVE);

    private DetectorThread detectorThread;

    @Override
    public boolean isDark() {
        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(GET_CMD);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String readLine = reader.readLine();
                if (readLine != null) {
                    return isDarkTheme(readLine);
                }
            }
        } catch (IOException e) {
            logger.error("Couldn't detect Linux OS theme", e);
        }
        return false;
    }

    private boolean isDarkTheme(String gtkTheme) {
        return darkThemeNamePattern.matcher(gtkTheme).matches();
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
     * Thread implementation for detecting the actually changed theme
     */
    private static final class DetectorThread extends Thread {

        private final LinuxThemeDetector detector;
        private boolean lastValue;

        DetectorThread(@NotNull LinuxThemeDetector detector) {
            this.detector = detector;
            this.lastValue = detector.isDark();
            this.setName("GTK Theme Detector Thread");
            this.setDaemon(true);
            this.setPriority(Thread.NORM_PRIORITY - 1);
        }

        @Override
        public void run() {
            try {
                Runtime runtime = Runtime.getRuntime();
                Process monitoringProcess = runtime.exec(MONITORING_CMD);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(monitoringProcess.getInputStream()))) {
                    while (!this.isInterrupted()) {
                        //Expected input = gtk-theme: '$GtkThemeName'
                        String readLine = reader.readLine();
                        String[] keyValue = readLine.split("\\s");
                        String value = keyValue[1];
                        boolean currentDetection = detector.isDarkTheme(value);
                        logger.debug("Theme changed detection, dark: {}", currentDetection);
                        if (currentDetection != lastValue) {
                            lastValue = currentDetection;
                            for (Consumer<Boolean> listener : detector.listeners) {
                                try {
                                    listener.accept(currentDetection);
                                } catch (RuntimeException e) {
                                    logger.error("Caught exception during listener notifying ", e);
                                }
                            }
                        }
                    }
                    logger.debug("ThemeDetectorThread has been interrupted!");
                    if (monitoringProcess.isAlive()) {
                        monitoringProcess.destroy();
                        logger.debug("Monitoring process has been destroyed!");
                    }
                }
            } catch (IOException e) {
                logger.error("Couldn't start monitoring process ", e);
            } catch (ArrayIndexOutOfBoundsException e) {
                logger.error("Couldn't parse command line output", e);
            }
        }
    }
}
