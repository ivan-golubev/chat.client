package net.ivango.chat.client.misc;


public interface IncomingMessageCallback {
    public void onMessageReceived(String sender, String message, boolean broadcast);
}
