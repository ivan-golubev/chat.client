package net.ivango.chat.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.ivango.chat.client.misc.CloseAppCallback;
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

/**
 * Main class of the application:
 * 1. initializes the UI.
 * 2. notifies the network controller to establish the connection / disconnect.
 * 3. links listeners with the UI/ controllers.
 * */
public class ClientUI extends Application {

    private Stage primaryStage;
    private NetworkController networkController;
    private SendMessageCallback sendMessageCallback;

    /** JAVA FX2 UI resources */
    public static final String MAIN_FORM_VIEW_FXML      = "ui/main_form.fxml",
                               WELCOME_FORM_VIEW_FXML   = "ui/welcome_form.fxml",
                               ERROR_DIALOG             = "ui/error_dialog.fxml",
                               INPUT_VALIDATION_DIALOG  = "ui/input_validation_dialog.fxml";


    /** Called upon application exit */
    private CloseAppCallback closeAppCallback = new CloseAppCallback(){
        public void closeApp() {
            primaryStage.hide();
            networkController.onApplicationClose();
            Platform.exit();
            System.exit(0);
        }
    };

    /** User-facing error handling: show popup with an error message */
    private ErrorDialogCallback errorDialogCallback = new ErrorDialogCallback() {
        /* show the "input validation failed" dialogue */
        public void showFailedValidationDialog(String errorMessage) {
            Task<Void> task = new Task<Void>() {
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

                        controller.initialize(closeAppCallback, dialog, primaryStage, errorMessage);
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

        /* show an error dialogue and the close the application when user hits "OK" */
        public void showErrorDialog(String errorMessage, Exception ex){
            Task<Void> task = new Task<Void>() {
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

                        controller.initialize(closeAppCallback, dialog, primaryStage, errorMessage, ex);
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
    /**
     * Starts the Java FX2 application.
     * */
    public void start(Stage primaryStage) {
        try {
            this.primaryStage = primaryStage;
            this.networkController = new NetworkController();
            this.sendMessageCallback = (receiver, message, broadcast) ->
                    networkController.sendMessage(receiver, message, broadcast);

            /* graceful termination upon program exit */
            primaryStage.setOnCloseRequest(t -> {
                closeAppCallback.closeApp();
            });

            out.println("Initializing the layout ...");
            Task<Void> task = new Task<Void>(){
                protected Void call() throws Exception {
                    showWelcomeDialogue();
                    return null;
                }
            };
            /* draw the main stage */
            Platform.runLater( task );
        } catch (Exception e) {
            errorDialogCallback.showErrorDialog("Failed to initialize the application:", e);
        }
    }

    /**
     * This function is called when user completes the first form and tries to establish a connection to server.
     * */
    private WelcomeCallback welcomeCallback = new WelcomeCallback() {
        public void onConnectPressed(String userName, String hostname, int port) {
            Task<Void> task = new Task<Void>() {
                protected Void call() throws Exception {
                    try {
                        /* hide the first form */
                        primaryStage.hide();
                        /* show the main panel */
                        MainFormController controller = switchToMainLayout(userName, hostname, port);
                        System.out.format("Connecting %s to %s.\n", userName, hostname);
                        /* establish a connection to server and perform initial requests */
                        networkController.initConnection(userName, hostname, port, controller, errorDialogCallback);
                    } catch (ConnectException ce) {
                        errorDialogCallback.showErrorDialog("Server is not reachable.", null);
                    } catch (IOException | ExecutionException | InterruptedException e) {
                        String errorMessage = "Failed to establish connection to server.\n"
                                + "Check the address and whether server is up and running.";
                        errorDialogCallback.showErrorDialog(errorMessage, null);
                    }
                    return null;
                }
            };
            Platform.runLater(task);
        }
    };

    /**
     * Switches the application to the main UI panel.
     * */
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

    /**
     * Shows the first welcome dialogue.
     * */
    private void showWelcomeDialogue() {
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

    /**
     * Launches the application by invoking the start() method.
     * */
    public static void main(String[] args) {
        launch();
    }
}