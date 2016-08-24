package chat.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Client {

    public static final int PORT = 8989;
    private AsynchronousSocketChannel channel;

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
            String message = new String(buffer);
            System.out.println("Received message from the server: " + message);

            socketChannel.read(inputBuffer, null, this);
        }

        @Override
        public void failed(Throwable exc, Void attachment) {

        }
    }

    public void init () throws IOException, ExecutionException, InterruptedException {
        channel = AsynchronousSocketChannel.open();
        Future f = channel.connect(new InetSocketAddress("localhost", PORT));
        f.get();

        System.out.println("client has started: " + channel.isOpen());

        /* registering the read handler */
        ByteBuffer inputBuffer = ByteBuffer.allocate(2048);
        channel.read(inputBuffer, null, new Readhandler(channel, inputBuffer));

        System.out.println("Sending messages to server: ");

        String [] messages = new String [] {"Time goes fast.", "What now?", "Bye."};

        for (String m : messages) {

            ByteBuffer buffer = ByteBuffer.wrap(m.getBytes());
            Future result = channel.write(buffer);

            while ( !result.isDone() ) {
                System.out.println("... ");
            }

            System.out.println(m);
            buffer.clear();
            Thread.sleep(3000);
        }
        Thread.sleep(5000);
        System.out.println("Closing the connection... ");
        channel.close();
    }

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        Client client = new Client();
        client.init();
    }
}
