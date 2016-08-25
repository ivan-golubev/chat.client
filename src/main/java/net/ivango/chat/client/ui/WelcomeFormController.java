package net.ivango.chat.client.ui;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import net.ivango.chat.client.misc.WelcomeCallback;

public class WelcomeFormController {

    @FXML
    private TextField userNameLabel;
    @FXML
    private TextField serverAdressLabel;
    @FXML
    private Button connectButton;

    private WelcomeCallback welcomeCallback;

    public void initialize(WelcomeCallback welcomeCallback) {
        this.welcomeCallback = welcomeCallback;
        this.connectButton.setOnKeyPressed(ke -> {
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

    @FXML
    public void connectPressed() {
        String userName = userNameLabel.getText();
        String serverAddress = serverAdressLabel.getText();

        String [] str = serverAddress.split(":");
        String hostname = str[0];
        int port = Integer.valueOf(str[1]);

        welcomeCallback.onConnectPressed(userName, hostname, port);
    }

}
