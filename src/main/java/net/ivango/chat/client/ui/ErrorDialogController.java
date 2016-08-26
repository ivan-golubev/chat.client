package net.ivango.chat.client.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorDialogController {

    @FXML
    private TextArea errorTextArea;

    private Stage dialog;
    private Stage primaryStage;
    private boolean exitOnClose = true;

    public void initialize(Stage dialog, Stage primaryStage, String errorMessage) {
        this.dialog = dialog;
        this.primaryStage = primaryStage;
        errorTextArea.setText(errorMessage);
        this.errorTextArea.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ESCAPE) || ke.getCode().equals(KeyCode.ENTER)) {
                close();
            }
        });
    }

    public void initialize(Stage dialog, Stage primaryStage, String errorMessage, Exception ex) {
        initialize(dialog, primaryStage, errorMessage);
        if (ex != null) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String outputText = errorMessage + "\n\n" + sw.toString();
        } else {
            errorTextArea.setText(errorMessage);
        }
    }

    public void disableClosing() { exitOnClose = false; }

    @FXML
    public void close() {
        if (exitOnClose) { primaryStage.hide(); }
        else { dialog.hide(); }
    }

}
