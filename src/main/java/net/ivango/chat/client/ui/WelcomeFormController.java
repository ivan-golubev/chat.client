package net.ivango.chat.client.ui;


import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import net.ivango.chat.client.misc.ErrorDialogCallback;
import net.ivango.chat.client.misc.WelcomeCallback;

import java.net.InetSocketAddress;

/**
 * Controller used by the welcome UI form.
 * */
public class WelcomeFormController {

    @FXML
    private TextField userNameLabel;
    @FXML
    private TextField serverAdressLabel;
    @FXML
    private Button connectButton;

    private WelcomeCallback welcomeCallback;
    private ErrorDialogCallback errorDialogCallback;

    /**
     * Initializes the user interface.
     * */
    public void initialize(WelcomeCallback welcomeCallback, ErrorDialogCallback errorDialogCallback) {
        /* configure handlers and save callbacks */
        this.welcomeCallback = welcomeCallback;
        this.errorDialogCallback = errorDialogCallback;
        this.connectButton.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                connectPressed();
            }
        });
        this.userNameLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                connectPressed();
            }
        });
        this.serverAdressLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                connectPressed();
            }
        });
    }

    /**
     * Validates the input.
     * If input is correct – forwards the data to the network controller.
     * Else - shows an error dialogue.
     **/
    @FXML
    public void connectPressed() {
        /* performing the input validation */
        String userName = userNameLabel.getText();
        String serverAddress = serverAdressLabel.getText();

        boolean validInput = true;
        String errorMessage = "";

        if (userName == null || userName.isEmpty()) {
            errorMessage = "User name cannot be empty.\n";
            validInput = false;
        }
        if (serverAddress == null || serverAddress.isEmpty()) {
            errorMessage += "Server address cannot be empty.\n";
            validInput = false;
        }
        String hostname = "";
        int port = 0;

        try {
            String [] str = serverAddress.split(":");
            hostname = str[0];
            port = Integer.valueOf(str[1]);
            /* checking if the address is correct */
            InetSocketAddress address = new InetSocketAddress(hostname, port);
            if (address.isUnresolved()) {
                errorMessage += "Server is not reachable.\n";
                validInput = false;
            }
        } catch (Exception e) {
            errorMessage += "Server address is not valid.\n";
            validInput = false;
        }

        if ( validInput ) {
            /* forward the input to the network controller */
            welcomeCallback.onConnectPressed(userName, hostname, port);
        } else {
            /* Show error dialogue */
            String finalErrorMessage = errorMessage;
            Platform.runLater(new Task<Void>() {
                protected Void call() throws Exception {
                    errorDialogCallback.showFailedValidationDialog(finalErrorMessage);
                    return null;
                }
            });
        }
    }

}
