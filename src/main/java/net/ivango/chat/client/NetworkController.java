package net.ivango.chat.client;

import com.google.gson.JsonSyntaxException;
import net.ivango.chat.common.JSONMapper;
import net.ivango.chat.common.misc.HandlerMap;
import net.ivango.chat.common.misc.MessageHandler;
import net.ivango.chat.common.requests.LoginRequest;
import net.ivango.chat.common.requests.Message;
import net.ivango.chat.common.responses.GetTimeResponse;
import net.ivango.chat.common.responses.GetUsersResponse;
import net.ivango.chat.common.responses.IncomingMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class NetworkController {

//    public static final int PORT = 8989;
    private AsynchronousSocketChannel channel;
    private HandlerMap handlerMap = new HandlerMap();
    private JSONMapper jsonMapper = new JSONMapper();

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

    class Readhandler implements CompletionHandler<Integer, Void> {
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

    private void login(String userName) {
        /* perform the login */
        LoginRequest loginRequest = new LoginRequest(userName);
        String json = jsonMapper.toJSON(loginRequest);

        ByteBuffer buffer = ByteBuffer.wrap(json.getBytes());
        Future result = channel.write(buffer);

        while ( !result.isDone() ) {
            System.out.println("... ");
        }
        buffer.clear();
    }

    public void initConnection (String userName, String hostname, int port) throws IOException, ExecutionException, InterruptedException {
        channel = AsynchronousSocketChannel.open();
        Future f = channel.connect(new InetSocketAddress(hostname, port));
        f.get();

        System.out.println("client has started: " + channel.isOpen());
        registerHandlers();

        login(userName);

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
    }

}
