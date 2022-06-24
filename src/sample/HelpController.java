package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class HelpController {

    @FXML
    private Label closeLabel;

    @FXML
    void closeWindow(MouseEvent event) {
        Stage stage = (Stage) closeLabel.getScene().getWindow();
        stage.close();
    }

}