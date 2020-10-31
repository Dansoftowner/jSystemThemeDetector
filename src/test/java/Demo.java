import com.jthemedetecor.SystemThemeDetector;

import java.util.Scanner;

public class Demo {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Watching system theme change:");
        SystemThemeDetector detector = SystemThemeDetector.getDetector();
        detector.registerListener(isDark -> System.out.println(isDark ? "It's just switched to DARK" : "It's just switched to LIGHT"));
        while(true) {
            String line = scanner.nextLine();
            if (line.startsWith("q")) {
                System.exit(0);
            }
        }
    }
}
