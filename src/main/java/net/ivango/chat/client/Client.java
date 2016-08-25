package net.ivango.chat.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;

import com.google.gson.JsonSyntaxException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import net.ivango.chat.client.misc.WelcomeCallback;
import net.ivango.chat.client.ui.WelcomeFormController;
import net.ivango.chat.common.JSONMapper;
import net.ivango.chat.common.misc.HandlerMap;
import net.ivango.chat.common.misc.MessageHandler;
import net.ivango.chat.common.requests.Message;
import net.ivango.chat.common.responses.GetTimeResponse;
import net.ivango.chat.common.responses.GetUsersResponse;
import net.ivango.chat.common.responses.IncomingMessage;

import static java.lang.System.out;

public class Client extends Application {

    public static final int PORT = 8989;
    private AsynchronousSocketChannel channel;

    private Stage primaryStage;
    private HandlerMap handlerMap = new HandlerMap();
    private JSONMapper jsonMapper = new JSONMapper();

    public static final String MAIN_FORM_VIEW_FXML = "ui/main_form.fxml",
                               WELCOME_FORM_VIEW_FXML = "ui/welcome_form.fxml";

    private void registerHandlers(){
        handlerMap.put(GetTimeResponse.class, new MessageHandler<GetTimeResponse>() {
            @Override
            public void onMessageReceived(GetTimeResponse getTimeResponse) {

            }
        });

        handlerMap.put(GetUsersResponse.class, new MessageHandler<GetUsersResponse>() {
            @Override
            public void onMessageReceived(GetUsersResponse getUsersResponse) {

            }
        });

        handlerMap.put(IncomingMessage.class, new MessageHandler<IncomingMessage>() {
            @Override
            public void onMessageReceived(IncomingMessage message) {
                System.out.format("Message from %s received: %s.\n", message.getFrom(), message.getMessage());
            }
        });
    }

    class Readhandler implements CompletionHandler<Integer, Void>{
        private AsynchronousSocketChannel socketChannel;
        private ByteBuffer inputBuffer;

        public Readhandler(AsynchronousSocketChannel socketChannel, ByteBuffer inputBuffer) {
            this.socketChannel = socketChannel;
            this.inputBuffer = inputBuffer;
        }

        @Override
        public void completed(Integer bytesRead, Void attachment) {
            byte[] buffer = new byte[bytesRead];
            inputBuffer.rewind();
            // Rewind the input buffer to read from the beginning

            inputBuffer.get(buffer);
            String json = new String(buffer);
//            System.out.println("Received message from the server: " + message);

            Message message = null;
            try {
                message = (Message) jsonMapper.fromJson(json);
                MessageHandler handler = handlerMap.get(message.getClass());
                handler.onMessageReceived(message);

            } catch (JsonSyntaxException ie) {

            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
            }

            socketChannel.read(inputBuffer, null, this);
        }

        @Override
        public void failed(Throwable exc, Void attachment) {

        }
    }

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

    WelcomeCallback callback = new WelcomeCallback() {
        @Override
        public void onConnectPressed(String userName, String serverAddress) {
            System.out.format("Connecting %s to %s.\n", userName, serverAddress);
        }
    };

    private void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader(Client.class.getResource(WELCOME_FORM_VIEW_FXML));
            Pane rootLayout = loader.load();

            Scene scene = new Scene(rootLayout);
//            scene.getStylesheets().addAll(Main.class.getResource(STYLES_MAIN_CSS).toExternalForm());

            primaryStage.setTitle("Welcome to chat");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

            // Give the controller access to the main app.
            WelcomeFormController controller = loader.getController();
            controller.initialize(callback);

//            controller.setMainApp(this, rootPath);
        } catch (IOException e) {
//            showErrorDialog("Failed to initialize root layout:", e);
            e.printStackTrace();
        }
    }

//    public void init () throws IOException, ExecutionException, InterruptedException {
//        channel = AsynchronousSocketChannel.open();
//        Future f = channel.connect(new InetSocketAddress("localhost", PORT));
//        f.get();
//
//        System.out.println("client has started: " + channel.isOpen());
//        registerHandlers();
//
//        /* registering the read handler */
//        ByteBuffer inputBuffer = ByteBuffer.allocate(2048);
//        channel.read(inputBuffer, null, new Readhandler(channel, inputBuffer));
//
//        System.out.println("Sending messages to server: ");
//
//        String [] messages = new String [] {"Time goes fast.", "What now?", "Bye."};
//
//        for (String m : messages) {
//
//            Message request = new SendMessageRequest("", m, true);
//            String json = jsonMapper.toJSON(request);
//
//            ByteBuffer buffer = ByteBuffer.wrap(json.getBytes());
//            Future result = channel.write(buffer);
//
//            while ( !result.isDone() ) {
//                System.out.println("... ");
//            }
//
//            System.out.println(m);
//            buffer.clear();
//            Thread.sleep(3000);
//        }
//        Thread.sleep(5000);
//        System.out.println("Closing the connection... ");
//        channel.close();
//    }

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
//        Client client = new Client();
//        client.init();
        launch();
    }
}
