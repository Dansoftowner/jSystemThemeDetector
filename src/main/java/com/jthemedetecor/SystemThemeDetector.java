package com.jthemedetecor;

import com.registry.RegistryKey;
import com.registry.RegistryValue;
import com.registry.RegistryWatcher;
import com.registry.event.RegistryListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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

        private final Map<Consumer<Boolean>, RegistryListener> listeners;
        private final RegistryKey registryPath;

        private WindowsThemeDetector() {
            this.registryPath = new RegistryKey(REGISTRY_PATH);
            this.listeners = Collections.synchronizedMap(new HashMap<>());
        }

        @Override
        public boolean isDark() {
            RegistryValue registryValue = registryPath.getValue(REGISTRY_VALUE);
            if (registryValue != null) {
                byte[] byteData = registryValue.getByteData();
                if (byteData.length > 0) {
                    int value = byteData[0];
                    return value == 0;
                }
            }
            return false;
        }

        @Override
        public void registerListener(Consumer<Boolean> darkThemeListener) {
            RegistryListener registryListener = registryEvent -> {
                RegistryKey key = registryEvent.getKey();
                if (key.equals(registryPath)) {
                    darkThemeListener.accept(isDark());
                }
            };
            RegistryWatcher.addRegistryListener(registryListener);
            RegistryWatcher.watchKey(registryPath);
            listeners.put(darkThemeListener, registryListener);

        }

        @Override
        public void removeListener(Consumer<Boolean> darkThemeListener) {
            RegistryListener removed = listeners.remove(darkThemeListener);
            RegistryWatcher.removeRegistryListener(removed);
            if (listeners.isEmpty()) {
                RegistryWatcher.removeKey(registryPath);
            }
        }

        @Override
        public void removeAllListeners() {
            this.listeners.values().forEach(RegistryWatcher::removeRegistryListener);
            this.listeners.clear();
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
