/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.jthemedetecor;

import com.jthemedetecor.util.OsInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * For detecting the theme (dark/light) used by the Operating System.
 *
 * @author Daniel Gyorffy
 */
public abstract class OsThemeDetector {

    private static final Logger logger = LoggerFactory.getLogger(OsThemeDetector.class);

    private static OsThemeDetector osThemeDetector;

    OsThemeDetector() {
    }

    @NotNull
    public static synchronized OsThemeDetector getDetector() {
        if (osThemeDetector != null) {
            return osThemeDetector;
        } else if (OsInfo.isWindows10OrLater()) {
            logDetection("Windows 10", WindowsThemeDetector.class);
            return osThemeDetector = new WindowsThemeDetector();
        } else if (OsInfo.isGnome()) {
            logDetection("Gnome", GnomeThemeDetector.class);
            return osThemeDetector = new GnomeThemeDetector();
        } else if (OsInfo.isMacOsMojaveOrLater()) {
            logDetection("MacOS", MacOSThemeDetector.class);
            return osThemeDetector = new MacOSThemeDetector();
        } else {
            logger.debug("Theme detection is not supported on the system: {} {}", OsInfo.getFamily(), OsInfo.getVersion());
            logger.debug("Creating empty detector...");
            return osThemeDetector = new EmptyDetector();
        }
    }

    private static void logDetection(String desktop, Class<? extends OsThemeDetector> detectorClass) {
        logger.debug("Supported Desktop detected: {}", desktop);
        logger.debug("Creating {}...", detectorClass.getName());
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

    public static boolean isSupported() {
        return OsInfo.isWindows10OrLater() || OsInfo.isMacOsMojaveOrLater() || OsInfo.isGnome();
    }

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
