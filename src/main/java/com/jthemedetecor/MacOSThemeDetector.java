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

import com.sun.jna.Callback;
import de.jangassen.jfa.foundation.Foundation;
import de.jangassen.jfa.foundation.ID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Determines the dark/light theme on Mac System through the <i>Apple Foundation framework</i>.
 *
 * <p>
 * This solution was written based on the <a href="https://github.com/JetBrains/intellij-community/blob/5c7e93fa35cb544e87c283888f5d92a001fa7260/platform/platform-impl/src/com/intellij/ide/ui/laf/SystemDarkThemeDetector.kt">INTELLIJ IDEA'S MacOS DARK THEME DETECTION IMPLEMENTATION</a>.
 *
 * @author Daniel Gyorffy
 */
class MacOSThemeDetector extends OsThemeDetector {

    private static final Logger logger = LoggerFactory.getLogger(MacOSThemeDetector.class);

    private final Set<Consumer<Boolean>> listeners = Collections.synchronizedSet(new HashSet<>());
    private final Pattern themeNamePattern = Pattern.compile(".*dark.*", Pattern.CASE_INSENSITIVE);

    private final Callback themeChangedCallback = new Callback() {
        @SuppressWarnings("unused")
        public void callback() {
            notifyListeners();
        }
    };

    MacOSThemeDetector() {
        initObserver();
    }

    private void initObserver() {
        final Foundation.NSAutoreleasePool pool = new Foundation.NSAutoreleasePool();
        try {
            final ID delegateClass = Foundation.allocateObjcClassPair(Foundation.getObjcClass("NSObject"), "NSColorChangesObserver");
            if (!ID.NIL.equals(delegateClass)) {
                if (!Foundation.addMethod(delegateClass, Foundation.createSelector("handleAppleThemeChanged:"), themeChangedCallback, "v@")) {
                    throw new RuntimeException("Observer method cannot be added");
                }
                Foundation.registerObjcClassPair(delegateClass);
            }

            final ID delegate = Foundation.invoke("NSColorChangesObserver", "new");
            Foundation.invoke(
                    Foundation.invoke("NSDistributedNotificationCenter", "defaultCenter"),
                    "addObserver:selector:name:object:",
                    delegate,
                    Foundation.createSelector("handleAppleThemeChanged:"),
                    Foundation.nsString("AppleInterfaceThemeChangedNotification"),
                    ID.NIL);
        } finally {
            pool.drain();
        }
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public boolean isDark() {
        final Foundation.NSAutoreleasePool pool = new Foundation.NSAutoreleasePool();
        try {
            final ID userDefaults = Foundation.invoke("NSUserDefaults", "standardUserDefaults");
            final String appleInterfaceStyle = Foundation.toStringViaUTF8(Foundation.invoke(userDefaults, "objectForKey:", Foundation.nsString("AppleInterfaceStyle")));
            return isDarkTheme(appleInterfaceStyle);
        } catch (RuntimeException e) {
            logger.error("Couldn't execute theme name query with the Os", e);
        } finally {
            pool.drain();
        }
        return false;
    }

    private boolean isDarkTheme(String themeName) {
        return themeNamePattern.matcher(themeName).matches();
    }

    @Override
    public synchronized void registerListener(@NotNull Consumer<Boolean> darkThemeListener) {
        listeners.add(darkThemeListener);
    }

    @Override
    public synchronized void removeListener(@Nullable Consumer<Boolean> darkThemeListener) {
        listeners.remove(darkThemeListener);
    }

    private void notifyListeners() {
        listeners.forEach(listener -> listener.accept(isDark()));
    }
}

