package com.jthemedetecor;

import com.jthemedetecor.util.OsInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * For detecting the theme (dark/light) used by the Operating System.
 *
 * @author Daniel Gyorffy
 */
public abstract class OsThemeDetector {

    private static OsThemeDetector osThemeDetector;

    OsThemeDetector() {
    }

    @NotNull
    public static synchronized OsThemeDetector getDetector() {
        if (osThemeDetector != null) {
            return osThemeDetector;
        } else if (OsInfo.isWindows10OrLater()) {
            return osThemeDetector = new WindowsThemeDetector();
        } else if (OsInfo.isLinux()) {
            return osThemeDetector = new LinuxThemeDetector();
        } else if (OsInfo.isMacOsMojaveOrLater()) {
            return osThemeDetector = new MacOSThemeDetector();
        } else {
            return osThemeDetector = new EmptyDetector();
        }
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
    public abstract void registerListener(@NotNull Consumer<Boolean> darkThemeListener);

    /**
     * Removes the listener.
     */
    public abstract void removeListener(@Nullable Consumer<Boolean> darkThemeListener);


    private static final class EmptyDetector extends OsThemeDetector {
        @Override
        public boolean isDark() {
            return false;
        }

        @Override
        public void registerListener(@NotNull Consumer<Boolean> darkThemeListener) {
        }

        @Override
        public void removeListener(@Nullable Consumer<Boolean> darkThemeListener) {
        }
    }
}
