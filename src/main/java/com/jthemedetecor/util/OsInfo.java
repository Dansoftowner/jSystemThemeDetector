package com.jthemedetecor.util;

import oshi.PlatformEnum;
import oshi.SystemInfo;
import oshi.software.os.OperatingSystem;

public class OsInfo {

    private static final PlatformEnum platformType;
    private static final String version;

    static {
        final SystemInfo systemInfo = new SystemInfo();
        final OperatingSystem osInfo = systemInfo.getOperatingSystem();
        final OperatingSystem.OSVersionInfo osVersionInfo = osInfo.getVersionInfo();

        platformType = SystemInfo.getCurrentPlatformEnum();
        version = osVersionInfo.getVersion();
    }

    public static boolean isWindows10OrLater() {
        return hasTypeAndVersionOrHigher(PlatformEnum.WINDOWS, "10");
    }

    public static boolean isLinux() {
        return hasType(PlatformEnum.LINUX);
    }

    public static boolean isMacOsMojaveOrLater() {
        return hasTypeAndVersionOrHigher(PlatformEnum.MACOSX, "10.14");
    }

    public static boolean hasType(PlatformEnum platformType) {
        return OsInfo.platformType.equals(platformType);
    }

    public static boolean hasVersionOrHigher(String version) {
        return parseVersion(OsInfo.version) >= parseVersion(version);
    }

    public static boolean hasTypeAndVersionOrHigher(PlatformEnum platformType, String version) {
        return hasType(platformType) && hasVersionOrHigher(version);
    }

    private static int parseVersion(String version) {
        try {
            return Integer.parseInt(version.replace(".", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private OsInfo() {
    }
}
