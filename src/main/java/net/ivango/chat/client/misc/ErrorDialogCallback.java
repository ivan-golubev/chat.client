package net.ivango.chat.client.misc;

public interface ErrorDialogCallback {

    void showErrorDialog(String errorMessage, Exception ex);
    void showFailedValidationDialog(String errorMessage);
}
