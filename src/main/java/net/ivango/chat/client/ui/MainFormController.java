package net.ivango.chat.client.ui;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import net.ivango.chat.client.misc.IncomingMessageCallback;
import net.ivango.chat.client.misc.SendMessageCallback;
import net.ivango.chat.client.misc.ServerTimeMessageCallback;
import net.ivango.chat.client.misc.UserListUpdateCallback;
import net.ivango.chat.common.responses.BroadCastUser;
import net.ivango.chat.common.responses.User;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Controller used by the main chat panel.
 * */
public class MainFormController implements UserListUpdateCallback, IncomingMessageCallback, ServerTimeMessageCallback {

    @FXML
    private Label userNameLabel;
    @FXML
    private Label serverAdressLabel;
    @FXML
    private Label serverTime;
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
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm, dd MMM yy");

    /**
     * Initializes the user interface.
     * */
    public void initialize (SendMessageCallback callback) {
        this.callback = callback;
        /* message is being sent upon pressing Enter */
        this.textArea.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                sendMessage();
                /* consume the event to cancel the default "Enter" pressing behavior */
                ke.consume();
            }
        });

        /* customize the user list view */
        activeUsersList.setCellFactory(new Callback<ListView<User>, ListCell<User>>() {
                public ListCell<User> call(ListView<User> list) {
                    final ListCell<User> cell = new ListCell<User>() {
                        @Override
                        public void updateItem(User item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setText(null);
                                setGraphic(null);
                            } else {
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

    /**
     * Fills the initial user info: name, hist and port.
     * */
    public void fillUserInfo(String userName, String hostname, int port) {
        userNameLabel.setText(userName);
        serverAdressLabel.setText(hostname + ":" + port);
    }

    @Override
    /**
     * Update the active user list and the receiver combobox.
     * The lists are sorted alphabetically.
     * A synthetic broadcast user is added to the list.
     * */
    public void onUserListUpdated(List<User> users) {
        Collections.sort(users, (o1, o2) -> o1.getUserName().compareTo(o2.getUserName()));

        activeUsersList.setItems(FXCollections.observableList(users));

        User currentValue = receiverComboBox.getValue();
        ArrayList<User> receiverList = new ArrayList<>(users);
        receiverList.add(BroadCastUser.INSTANCE);
        Collections.sort(receiverList, (o1, o2) -> o1.getUserName().compareTo(o2.getUserName()));

        receiverComboBox.setItems(FXCollections.observableList(receiverList));
        if (currentValue != null && receiverList.contains(currentValue)) {
            receiverComboBox.setValue(currentValue);
        }
    }

    /**
     * Shows the message sent by the user himself.
     * */
    private void addOwnMessage(String message, boolean broadcast) {
        if (broadcast) {
            messages.add("Me to all: " + message);
        } else {
            messages.add("Me: " + message);
        }
    }

    /**
     * Passes the message and recipient to the network controller to send.
     * */
    private void sendMessage() {
        String message = textArea.getText();
        User receiver = receiverComboBox.getValue();
        /* if user has not specified the receiver */
        if (receiver == null) {
            /* prompt user to select the receiver */
            receiverComboBox.show();
        } else if ( !message.isEmpty() ) {
            /* clear the text are */
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

    /**
     * Shows the incoming message in the message box.
     * */
    @Override
    public void onMessageReceived(String sender, String message, boolean broadcast) {
        if (broadcast) {
            messages.add(sender + " to all: " + message);
        } else {
            messages.add(sender + ": " + message);
        }
    }

    /**
     * Shows the server time when connection was established.
     * */
    @Override
    public void onServerTimeReceived(long utcTimestamp) {
        serverTime.setText(dateFormat.format(new Date(utcTimestamp)));
    }
}