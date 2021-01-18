import com.jthemedetecor.OsThemeDetector;

import java.util.Scanner;

public class OsThemeDetectorDemo {
    public static void main(String[] args) {
        final OsThemeDetector detector = OsThemeDetector.getDetector();
        System.out.println(detector.isDark());
        detector.registerListener(isDark -> System.out.println("OS is dark: " + isDark));

        System.out.println("Listening to system ui theme change... (Press E for exit)");
        Scanner scanner = new Scanner(System.in);
        while(!scanner.nextLine().toLowerCase().startsWith("e"));
    }
}
