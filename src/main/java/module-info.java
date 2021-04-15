module com.jthemedetector {
    requires com.sun.jna.platform;
    requires org.slf4j;
    requires jfa;
    requires org.jetbrains.annotations;
    requires com.sun.jna;

    exports com.jthemedetecor;
    
    opens com.jthemedetecor to com.sun.jna;
}