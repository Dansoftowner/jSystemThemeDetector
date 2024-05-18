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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Used for detecting the dark theme on a Linux KDE desktop environment.
 * Tested on Ubuntu KDE Plasma (kde-plasma-desktop).
 *
 * @see GnomeThemeDetector
 */
public class KdeThemeDetector extends OsThemeDetector {
    private static final Logger logger = LoggerFactory.getLogger(KdeThemeDetector.class);
    /**
     * List of the known look and feel packages, if the user use a package that is not listed in this list
     * then will check if the package name contains `dark` regardless of the case sensitivity
     * */
    private static final String[] darkLookAndFeelPackages = {
            "org.kde.breezedark.desktop", "org.kde.oxygen", "org.kde.arc-dark",
            "org.kde.numix-dark", "org.kde.papirus-dark", "org.kde.suru-dark"
    };

    @Override
    public boolean isDark() {
        try {
            String currentLookAndFeelPackageName = getCurrentLookAndFeelPackageName();

            if (Arrays.asList(darkLookAndFeelPackages).contains(currentLookAndFeelPackageName)) {
                return true;
            }

            return currentLookAndFeelPackageName.toLowerCase().contains("dark".toLowerCase());

        } catch (IOException e) {
            logger.error("Couldn't detect Linux OS theme", e);
            return false;
        }
    }

    private String getCurrentLookAndFeelPackageName() throws IOException {
        String filePath;

        // Get user's home directory
        Path homeDir = Paths.get(System.getProperty("user.home"));

        // Build the complete file path
        filePath = homeDir.resolve(".config/kdeglobals").toString();

        // Read the file and get the value of the property LookAndFeelPackage
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String lookAndFeelPackage = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("LookAndFeelPackage=")) {
                    lookAndFeelPackage = line.substring(line.indexOf("=") + 1);
                    break;
                }
            }
            return lookAndFeelPackage;
        }
    }

    // TODO: Add support for the listeners, don't forgot to update the README.md if you did

    @Override
    public synchronized void registerListener(@NotNull Consumer<Boolean> darkThemeListener) {

    }

    @Override
    public synchronized void removeListener(@Nullable Consumer<Boolean> darkThemeListener) {

    }
}
