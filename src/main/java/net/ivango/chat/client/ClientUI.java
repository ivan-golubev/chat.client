package net.ivango.chat.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import net.ivango.chat.client.misc.SendMessageCallback;
import net.ivango.chat.client.misc.WelcomeCallback;
import net.ivango.chat.client.ui.MainFormController;
import net.ivango.chat.client.ui.WelcomeFormController;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static java.lang.System.out;

public class ClientUI extends Application {

    private Stage primaryStage;

    private NetworkController networkController = new NetworkController();

    public static final String MAIN_FORM_VIEW_FXML = "ui/main_form.fxml",
                               WELCOME_FORM_VIEW_FXML = "ui/welcome_form.fxml";

    @Override
    public void start(Stage primaryStage) {
        try {
            this.primaryStage = primaryStage;
            out.println("Initializing the layout ...");
            initRootLayout();
        } catch (Exception e) {
//            showErrorDialog("Failed to initialize application:", e);
        }
    }

    private WelcomeCallback welcomeCallback = new WelcomeCallback() {
        @Override
        public void onConnectPressed(String userName, String hostname, int port) {
            primaryStage.hide();
            System.out.format("Connecting %s to %s.\n", userName, hostname);
            try {
                networkController.initConnection(userName, hostname, port);
                switchToMainLayout(userName, hostname, port);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private SendMessageCallback sendMessageCallback = new SendMessageCallback() {
        @Override
        public void onSendMessage(String receiver, String message, boolean broadcast) {
            System.out.format("Sending message %s to %s.\n", message, receiver);
        }
    };

    private void switchToMainLayout(String userName, String hostname, int port) {
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
        } catch (IOException e) {
//            showErrorDialog("Failed to initialize root layout:", e);
            e.printStackTrace();
        }
    }

    private void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader(ClientUI.class.getResource(WELCOME_FORM_VIEW_FXML));
            Pane rootLayout = loader.load();

            Scene scene = new Scene(rootLayout);
//            scene.getStylesheets().addAll(Main.class.getResource(STYLES_MAIN_CSS).toExternalForm());

            primaryStage.setTitle("Welcome to chat");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

            WelcomeFormController controller = loader.getController();
            controller.initialize(welcomeCallback);

//            controller.setMainApp(this, rootPath);
        } catch (IOException e) {
//            showErrorDialog("Failed to initialize root layout:", e);
            e.printStackTrace();
        }
    }



    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        launch();
    }
}
