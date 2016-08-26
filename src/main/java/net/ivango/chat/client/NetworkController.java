package net.ivango.chat.client;

import com.google.gson.JsonSyntaxException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import net.ivango.chat.client.misc.ErrorDialogCallback;
import net.ivango.chat.client.misc.IncomingMessageCallback;
import net.ivango.chat.client.misc.ServerTimeMessageCallback;
import net.ivango.chat.client.misc.UserListUpdateCallback;
import net.ivango.chat.client.ui.MainFormController;
import net.ivango.chat.common.JSONMapper;
import net.ivango.chat.common.misc.HandlerMap;
import net.ivango.chat.common.misc.MessageHandler;
import net.ivango.chat.common.requests.*;
import net.ivango.chat.common.responses.GetTimeResponse;
import net.ivango.chat.common.responses.GetUsersResponse;
import net.ivango.chat.common.responses.IncomingMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.*;

public class NetworkController {

    private AsynchronousSocketChannel channel;
    private HandlerMap handlerMap = new HandlerMap();
    private JSONMapper jsonMapper = new JSONMapper();

    private UserListUpdateCallback ulCallback;
    private IncomingMessageCallback imCallback;
    private ServerTimeMessageCallback stCallback;
    private ErrorDialogCallback errorDialogCallback;

    private ExecutorService threadPool = Executors.newSingleThreadScheduledExecutor();
    private static final int SLEEP_INTERVAL = 5;
    private static final String MESSAGE_TEMPLATE = "NetworkController background worker thread: %s";

    private void registerHandlers(){
        /* registering the read handler */
        ByteBuffer inputBuffer = ByteBuffer.allocate(2048);
        channel.read(inputBuffer, null, new Readhandler(channel, inputBuffer));

        handlerMap.put(GetTimeResponse.class, (getTimeResponse, address) -> {
            Task<Void> task = new Task<Void>() {
                protected Void call() throws Exception {
                    stCallback.onServerTimeReceived( getTimeResponse.getUtcServerTime() );
                    return null;
                }
            };

            Platform.runLater(task);
        });

        handlerMap.put(GetUsersResponse.class, (message, address) -> {
            System.out.println("GetUsers response received: " + message.getUsers().toString());
            Task<Void> task = new Task<Void>() {
                protected Void call() throws Exception {
                    ulCallback.onUserListUpdated( message.getUsers() );
                    return null;
                }
            };

            Platform.runLater(task);
        });

        handlerMap.put(IncomingMessage.class, (message, address) -> {
            System.out.format("Message from %s received: %s.\n", message.getFrom(), message.getMessage());
            Task<Void> task = new Task<Void>() {
                protected Void call() throws Exception {
                    imCallback.onMessageReceived(message.getSenderName(), message.getMessage(), message.isBroadcast());
                    return null;
                }
            };

            Platform.runLater(task);
        });

        Runnable updateUserListTask = () -> {
            try {
                while (!Thread.interrupted()) {
//                        logger.info(String.format(MESSAGE_TEMPLATE, "thread awakened."));
                    /* fetch users */
                    sendJSON(new GetUsersRequest());

                    /* wait for a while */
//                        logger.info(String.format(MESSAGE_TEMPLATE, "thread goes to sleep."));
                    TimeUnit.SECONDS.sleep(SLEEP_INTERVAL);
                }
            } catch (InterruptedException ie) {
                // That's OK: somebody has stopped the app.
//                    logger.info(String.format(MESSAGE_TEMPLATE, "thread interrupted."));
            }
//                logger.info(String.format(MESSAGE_TEMPLATE, "thread stopped."));
        };

        threadPool.execute(updateUserListTask);
    }

    class Readhandler implements CompletionHandler<Integer, Void> {
        private AsynchronousSocketChannel socketChannel;
        private ByteBuffer inputBuffer;

        public Readhandler(AsynchronousSocketChannel socketChannel, ByteBuffer inputBuffer) {
            this.socketChannel = socketChannel;
            this.inputBuffer = inputBuffer;
        }

        @Override
        public void completed(Integer bytesRead, Void attachment) {

            if (bytesRead == -1) {
                System.out.println("EOS received. server disconnected.\n");
                return;
            }

            byte[] buffer = new byte[bytesRead];
            // Rewind the input buffer to read from the beginning
            inputBuffer.rewind();
            inputBuffer.get(buffer);
            String json = new String(buffer);

            try {
                Message message = (Message) jsonMapper.fromJson(json);
                MessageHandler handler = handlerMap.get(message.getClass());
                handler.onMessageReceived(message, null);

            } catch (JsonSyntaxException ie) {
                errorDialogCallback.showErrorDialog("Failed to parse the input JSON", ie);
            } catch (ClassNotFoundException e) {
                errorDialogCallback.showErrorDialog("Failed to map the input JSON, class not found", e);
            }
            inputBuffer.clear();
            socketChannel.read(inputBuffer, null, this);
        }

        @Override
        public void failed(Throwable exc, Void attachment) {
            System.out.println("Failed to read the input message.");
        }
    }

    private void sendJSON(Message message) {
        String json = jsonMapper.toJSON(message);

        ByteBuffer buffer = ByteBuffer.wrap(json.getBytes());
        Future result = channel.write(buffer);

        while ( !result.isDone() ) {
            System.out.println("... ");
        }
        buffer.clear();
    }

    public void sendMessage(String receiver, String message, boolean broadcast) {
        sendJSON(new SendMessageRequest(receiver, message, broadcast));
    }

    public void onApplicationClose() {
        try {
            threadPool.shutdownNow();
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initConnection (String userName,
                                String hostname,
                                int port,
                                MainFormController controller,
                                ErrorDialogCallback errorDialogCallback) throws IOException, ExecutionException, InterruptedException {
        channel = AsynchronousSocketChannel.open();
        Future f = channel.connect(new InetSocketAddress(hostname, port));
        f.get();

        System.out.println("client has started: " + channel.isOpen());
        this.ulCallback = controller;
        this.imCallback = controller;
        this.stCallback = controller;
        this.errorDialogCallback = errorDialogCallback;
        registerHandlers();

        /* perform the login */
        sendJSON(new LoginRequest(userName));

        /* request the server time */
        sendJSON(new GetTimeRequest());
    }

}
