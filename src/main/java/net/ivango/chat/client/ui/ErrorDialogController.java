package net.ivango.chat.client.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import net.ivango.chat.client.misc.CloseAppCallback;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Controller used by the error dialogue.
 * */
public class ErrorDialogController {

    @FXML
    private TextArea errorTextArea;

    private Stage dialog;
    private Stage primaryStage;
    private boolean exitOnClose = true;
    private CloseAppCallback closeAppCallback;

    /**
     * Initializes the user interface.
     * */
    public void initialize(CloseAppCallback closeAppCallback, Stage dialog, Stage primaryStage, String errorMessage) {
        this.dialog = dialog;
        this.primaryStage = primaryStage;
        this.closeAppCallback = closeAppCallback;
        errorTextArea.setText(errorMessage);
        this.errorTextArea.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ESCAPE) || ke.getCode().equals(KeyCode.ENTER)) {
                close();
            }
        });
    }

    /**
     * Initializes the user interface.
     * */
    public void initialize(CloseAppCallback closeAppCallback, Stage dialog, Stage primaryStage, String errorMessage, Exception ex) {
        initialize(closeAppCallback, dialog, primaryStage, errorMessage);
        if (ex != null) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String outputText = errorMessage + "\n\n" + sw.toString();
            errorTextArea.setText(outputText);
        } else {
            errorTextArea.setText(errorMessage);
        }
    }

    /**
     * Do not close the app upon dialogue closing.
     * */
    public void disableClosing() { exitOnClose = false; }

    /**
     * Fire the application closing event.
     * */
    @FXML
    public void close() {
        if (exitOnClose) { closeAppCallback.closeApp(); }
        else { dialog.hide(); }
    }

}
