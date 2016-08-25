package net.ivango.chat.client.ui;


import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import net.ivango.chat.client.misc.SendMessageCallback;

public class MainFormController {

    @FXML
    private Label userNameLabel;
    @FXML
    private Label serverAdressLabel;
    @FXML
    private ListView activeUsersList;
    @FXML
    private ComboBox receiverComboBox;
    @FXML
    private TextArea textArea;

    private SendMessageCallback callback;

    public void initialize (SendMessageCallback callback) {
        this.callback = callback;
        this.textArea.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                sendMessage();
            }
        });
    }

    public void fillUserInfo(String userName, String hostname, int port) {
        userNameLabel.setText(userName);
        serverAdressLabel.setText(hostname + ":" + port);
    }

    private void sendMessage() {
        String message = textArea.getText();
        textArea.clear();
        String receiver = receiverComboBox.getId();
        callback.onSendMessage(receiver, message, false);
    }

}
