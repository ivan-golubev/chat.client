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

import java.util.ArrayList;
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

    private class BroadCastUser extends User {
        private BroadCastUser() {
            super("Everyone", "255.255.255.255");
        }
    }

    public void initialize (SendMessageCallback callback) {
        this.callback = callback;
        this.textArea.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                sendMessage();
                ke.consume();
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
        ObservableList<User> observable = FXCollections.observableList(new ArrayList<>(users));
        observable.add(new BroadCastUser());
        receiverComboBox.setItems(observable);
        if (currentValue != null && users.contains(currentValue)) {
            receiverComboBox.setValue(currentValue);
        }
    }

    private void addOwnMessage(String message, boolean broadcast) {
        if (broadcast) {
            messages.add("Me to all: " + message);
        } else {
            messages.add("Me: " + message);
        }
    }

    private void sendMessage() {
        String message = textArea.getText();
        User receiver = receiverComboBox.getValue();
        if (receiver == null) {
          receiverComboBox.show();
        } else if (!message.isEmpty()) {
            textArea.setText("");
            if (receiver instanceof BroadCastUser) {
                callback.onSendMessage("", message, true);
                addOwnMessage(message, true);
            } else {
                callback.onSendMessage(receiver.getAddress(), message, false);
                addOwnMessage(message, false);
            }

        }
    }

    @Override
    public void onMessageReceived(String sender, String message, boolean broadcast) {
        if (broadcast) {
            messages.add(sender + " to all: " + message);
        } else {
            messages.add(sender + ": " + message);
        }
    }
}
