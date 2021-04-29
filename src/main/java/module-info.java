module com.jthemedetector {
    requires com.sun.jna.platform;
    requires org.slf4j;
    requires jfa;
    requires org.jetbrains.annotations;
    requires com.sun.jna;
    requires com.github.oshi;
    requires versioncompare;

    exports com.jthemedetecor;
    
    opens com.jthemedetecor to com.sun.jna;
}