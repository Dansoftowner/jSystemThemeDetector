import com.jthemedetecor.OsThemeDetector;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class GuiDemo extends Application {

    private static final String WINDOW_TITLE = "Dark Theme Detection";
    private static final double WINDOW_WIDTH = 800;
    private static final double WINDOW_HEIGHT = 600;

    private static final String LIGHT_THEME_LABEL = "The OS uses LIGHT THEME";
    private static final String DARK_THEME_LABEL = "The OS uses DARK THEME";
    private static final double FONT_SIZE = 50;

    private static final String DARK_STYLE = "-fx-base: #000000";
    private static final String LIGHT_STYLE = "-fx-base: #d7d7d7";

    @Override
    public void start(Stage stage) {
        StringProperty labelValue = new SimpleStringProperty();

        stage.setScene(new Scene(buildRoot(labelValue)));
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);
        stage.setTitle(WINDOW_TITLE);
        stage.show();

        final OsThemeDetector detector = OsThemeDetector.getDetector();
        Consumer<Boolean> darkThemeListener = isDark -> {
            Platform.runLater(() -> {
                if (isDark) {
                    stage.getScene().getRoot().setStyle(DARK_STYLE);
                    labelValue.set(DARK_THEME_LABEL);
                } else {
                    stage.getScene().getRoot().setStyle(LIGHT_STYLE);
                    labelValue.set(LIGHT_THEME_LABEL);
                }
            });
        };
        darkThemeListener.accept(detector.isDark());
        detector.registerListener(darkThemeListener);
    }

    private Parent buildRoot(StringProperty labelValue) {
        Label label = new Label();
        label.textProperty().bind(labelValue);
        label.setFont(Font.font(FONT_SIZE));
        return new StackPane(label);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
