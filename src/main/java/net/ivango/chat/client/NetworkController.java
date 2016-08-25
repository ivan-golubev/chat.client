package net.ivango.chat.client;

import com.google.gson.JsonSyntaxException;
import net.ivango.chat.client.misc.UserListUpdateCallback;
import net.ivango.chat.common.JSONMapper;
import net.ivango.chat.common.misc.HandlerMap;
import net.ivango.chat.common.misc.MessageHandler;
import net.ivango.chat.common.requests.GetUsersRequest;
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

    private AsynchronousSocketChannel channel;
    private HandlerMap handlerMap = new HandlerMap();
    private JSONMapper jsonMapper = new JSONMapper();

    private UserListUpdateCallback ulCallback;

    private void registerHandlers(){
        /* registering the read handler */
        ByteBuffer inputBuffer = ByteBuffer.allocate(2048);
        channel.read(inputBuffer, null, new Readhandler(channel, inputBuffer));

        handlerMap.put(GetTimeResponse.class, new MessageHandler<GetTimeResponse>() {
            @Override
            public void onMessageReceived(GetTimeResponse getTimeResponse, String address) {

            }
        });

        handlerMap.put(GetUsersResponse.class, new MessageHandler<GetUsersResponse>() {
            @Override
            public void onMessageReceived(GetUsersResponse message, String address) {
                System.out.println("GetUsers response received.");
                ulCallback.onUserListUpdated( message.getUsers() );
            }
        });

        handlerMap.put(IncomingMessage.class, new MessageHandler<IncomingMessage>() {
            @Override
            public void onMessageReceived(IncomingMessage message, String address) {
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

            if (bytesRead == -1) {
                System.out.println("EOS received. server disconnected.\n");
                return;
            }

            byte[] buffer = new byte[bytesRead];
            // Rewind the input buffer to read from the beginning
            inputBuffer.rewind();
            inputBuffer.get(buffer);
            String json = new String(buffer);

            socketChannel.read(inputBuffer, null, this);

            try {
                Message message = (Message) jsonMapper.fromJson(json);
                MessageHandler handler = handlerMap.get(message.getClass());
                handler.onMessageReceived(message, null);

            } catch (JsonSyntaxException ie) {

            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
            }
        }

        @Override
        public void failed(Throwable exc, Void attachment) {

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

    public void initConnection (String userName, String hostname, int port, UserListUpdateCallback ulCallback) throws IOException, ExecutionException, InterruptedException {
        channel = AsynchronousSocketChannel.open();
        Future f = channel.connect(new InetSocketAddress(hostname, port));
        f.get();

        System.out.println("client has started: " + channel.isOpen());
        this.ulCallback = ulCallback;
        registerHandlers();

        /* perform the login */
        sendJSON(new LoginRequest(userName));
        /* fetch users */
        sendJSON(new GetUsersRequest());


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
