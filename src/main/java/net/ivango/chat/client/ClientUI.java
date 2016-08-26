package net.ivango.chat.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.ivango.chat.client.misc.ErrorDialogCallback;
import net.ivango.chat.client.misc.SendMessageCallback;
import net.ivango.chat.client.misc.WelcomeCallback;
import net.ivango.chat.client.ui.ErrorDialogController;
import net.ivango.chat.client.ui.MainFormController;
import net.ivango.chat.client.ui.WelcomeFormController;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.ExecutionException;

import static java.lang.System.out;

public class ClientUI extends Application {

    private Stage primaryStage;

    private NetworkController networkController;
    private SendMessageCallback sendMessageCallback;

    public static final String MAIN_FORM_VIEW_FXML = "ui/main_form.fxml",
                               WELCOME_FORM_VIEW_FXML = "ui/welcome_form.fxml",
                               ERROR_DIALOG = "ui/error_dialog.fxml",
                               INPUT_VALIDATION_DIALOG = "ui/input_validation_dialog.fxml";

    private ErrorDialogCallback errorDialogCallback = new ErrorDialogCallback() {
        @Override
        public void showFailedValidationDialog(String errorMessage) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        FXMLLoader loader = new FXMLLoader(ClientUI.class.getResource(INPUT_VALIDATION_DIALOG));
                        Pane rootPane = loader.load();
                        ErrorDialogController controller = loader.getController();

                        Stage dialog = new Stage();
                        dialog.initModality(Modality.APPLICATION_MODAL);
                        dialog.initOwner(primaryStage);
                        Scene scene = new Scene(rootPane, 520, 180);
                        dialog.setScene(scene);
                        dialog.setResizable(false);

                        controller.initialize(dialog, primaryStage, errorMessage);
                        controller.disableClosing();

                        dialog.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };

            Platform.runLater( task );
        }

        @Override
        public void showErrorDialog(String errorMessage, Exception ex){
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        FXMLLoader loader = new FXMLLoader(ClientUI.class.getResource(ERROR_DIALOG));
                        Pane rootPane = loader.load();
                        ErrorDialogController controller = loader.getController();

                        Stage dialog = new Stage();
                        dialog.initModality(Modality.APPLICATION_MODAL);
                        dialog.initOwner(primaryStage);
                        Scene scene = new Scene(rootPane, 520, 480);
                        dialog.setScene(scene);
                        dialog.setResizable(false);

                        controller.initialize(dialog, primaryStage, errorMessage, ex);
                        dialog.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };

            Platform.runLater( task );
        }

    };

    @Override
    public void start(Stage primaryStage) {
        try {
            this.primaryStage = primaryStage;

            networkController = new NetworkController();

            sendMessageCallback = (receiver, message, broadcast) ->
                    networkController.sendMessage(receiver, message, broadcast);

            primaryStage.setOnCloseRequest(t -> {
                networkController.onApplicationClose();
                Platform.exit();
                System.exit(0);
            });

            out.println("Initializing the layout ...");
            Task<Void> task = new Task<Void>(){
                @Override
                protected Void call() throws Exception {
                    initRootLayout();
                    return null;
                }
            };

            Platform.runLater( task );
        } catch (Exception e) {
            errorDialogCallback.showErrorDialog("Failed to initialize the application:", e);
        }
    }

    private WelcomeCallback welcomeCallback = new WelcomeCallback() {
        @Override
        public void onConnectPressed(String userName, String hostname, int port) {
            primaryStage.hide();

                Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        try {
                            MainFormController controller = switchToMainLayout(userName, hostname, port);
                            System.out.format("Connecting %s to %s.\n", userName, hostname);
                            networkController.initConnection(userName, hostname, port, controller, errorDialogCallback);

                        } catch (ConnectException ce) {
                            errorDialogCallback.showErrorDialog("Server is not reachable.", null);
                        } catch (IOException | ExecutionException | InterruptedException e) {
                            errorDialogCallback.showErrorDialog("Failed to establish connection to server.", e);
                        }
                        return null;
                    }
                };
                Platform.runLater(task);
        }
    };

    private MainFormController switchToMainLayout(String userName, String hostname, int port) {
        try {
            FXMLLoader loader = new FXMLLoader(ClientUI.class.getResource(MAIN_FORM_VIEW_FXML));
            Pane rootLayout = loader.load();

            Scene scene = new Scene(rootLayout);
            primaryStage.setTitle("Welcome to chat");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

            MainFormController controller = loader.getController();
            controller.initialize(sendMessageCallback);
            controller.fillUserInfo(userName, hostname, port);
            return controller;
        } catch (IOException e) {
            errorDialogCallback.showErrorDialog("Failed to initialize root layout:", e);
            return null;
        }
    }

    private void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader(ClientUI.class.getResource(WELCOME_FORM_VIEW_FXML));
            Pane rootLayout = loader.load();

            Scene scene = new Scene(rootLayout);

            primaryStage.setTitle("Welcome to chat");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

            WelcomeFormController controller = loader.getController();
            controller.initialize(welcomeCallback, errorDialogCallback);
        } catch (IOException e) {
            errorDialogCallback.showErrorDialog("Failed to initialize root layout:", e);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
