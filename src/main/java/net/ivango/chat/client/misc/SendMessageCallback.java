package net.ivango.chat.client.misc;

public interface SendMessageCallback {

    public void onSendMessage(String receiver, String message, boolean broadcast);
}
