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

    @Override
    public void start(Stage stage) {
        StringProperty labelValue = new SimpleStringProperty();

        stage.setScene(new Scene(buildRoot(labelValue)));
        stage.setWidth(800);
        stage.setHeight(600);
        stage.setTitle("Dark theme detection");
        stage.show();

        final OsThemeDetector detector = OsThemeDetector.getDetector();
        Consumer<Boolean> darkThemeListener = isDark -> {
            Platform.runLater(() -> {
                if (isDark) {
                    stage.getScene().getRoot().setStyle("-fx-base: #000000");
                    labelValue.set("The OS uses DARK THEME");
                } else {
                    stage.getScene().getRoot().setStyle("-fx-base: #d7d7d7");
                    labelValue.set("The OS uses LIGHT THEME");
                }
            });
        };
        darkThemeListener.accept(detector.isDark());
        detector.registerListener(darkThemeListener);
    }

    private Parent buildRoot(StringProperty labelValue) {
        Label label = new Label();
        label.textProperty().bind(labelValue);
        label.setFont(Font.font(50));
        return new StackPane(label);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
