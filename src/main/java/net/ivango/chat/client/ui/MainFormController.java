package net.ivango.chat.client.ui;


import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import net.ivango.chat.client.misc.SendMessageCallback;
import net.ivango.chat.client.misc.UserListUpdateCallback;
import net.ivango.chat.common.responses.User;

import java.util.List;

public class MainFormController implements UserListUpdateCallback {

    @FXML
    private Label userNameLabel;
    @FXML
    private Label serverAdressLabel;
    @FXML
    private ListView<User> activeUsersList;
    @FXML
    private ComboBox<User> receiverComboBox;
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

        activeUsersList.setCellFactory(new Callback<ListView<User>, ListCell<User>>() {
                @Override
                public ListCell<User> call(ListView<User> list) {
                    final ListCell<User> cell = new ListCell<User>() {
                        @Override
                        public void updateItem(User item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null) {
                                setText(item.getUserName() + " " + item.getAddress());
                            }
                        }
                    };
                    return cell;
                }
            }
        );
    }

    public void fillUserInfo(String userName, String hostname, int port) {
        userNameLabel.setText(userName);
        serverAdressLabel.setText(hostname + ":" + port);
    }

    @Override
    public void onUserListUpdated(List<User> users) {
        receiverComboBox.setItems(FXCollections.observableList(users));
        activeUsersList.setItems(FXCollections.observableList(users));
    }

    private void sendMessage() {
        String message = textArea.getText();
        textArea.clear();
        User receiver = receiverComboBox.getValue();
        callback.onSendMessage(receiver.getAddress(), message, false);
    }

}
