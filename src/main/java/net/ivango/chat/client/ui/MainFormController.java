package net.ivango.chat.client.ui;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import net.ivango.chat.client.misc.IncomingMessageCallback;
import net.ivango.chat.client.misc.SendMessageCallback;
import net.ivango.chat.client.misc.UserListUpdateCallback;
import net.ivango.chat.common.responses.User;

import java.util.List;

public class MainFormController implements UserListUpdateCallback, IncomingMessageCallback {

    @FXML
    private Label userNameLabel;
    @FXML
    private Label serverAdressLabel;
    @FXML
    private ListView<User> activeUsersList;
    @FXML
    private ListView<String> messageList;
    @FXML
    private ComboBox<User> receiverComboBox;
    @FXML
    private TextArea textArea;

    private SendMessageCallback callback;

    private ObservableList<String> messages = FXCollections.observableArrayList();

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
        messageList.setItems(messages);
    }

    public void fillUserInfo(String userName, String hostname, int port) {
        userNameLabel.setText(userName);
        serverAdressLabel.setText(hostname + ":" + port);
    }

    @Override
    public void onUserListUpdated(List<User> users) {
        activeUsersList.setItems(FXCollections.observableList(users));

        User currentValue = receiverComboBox.getValue();
        receiverComboBox.setItems(FXCollections.observableList(users));
        if (currentValue != null && users.contains(currentValue)) {
            receiverComboBox.setValue(currentValue);
        }
    }

    private void addOwnMessage(String message) {
        messages.add("Me: " + message);
    }

    private void sendMessage() {
        String message = textArea.getText();
        User receiver = receiverComboBox.getValue();
        if (receiver != null && !message.isEmpty()) {
            textArea.setText("");
            callback.onSendMessage(receiver.getAddress(), message, false);
            addOwnMessage(message);
        }
    }

    @Override
    public void onMessageReceived(String sender, String message, boolean broadcast) {
        messages.add(sender + ": " + message);
    }
}
