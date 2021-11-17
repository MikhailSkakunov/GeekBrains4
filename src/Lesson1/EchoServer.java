package Lesson1;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


    public class EchoServer {
        private static final int PORT = 9000;


        private static class TaskHandler implements Runnable {
            private final SocketChannel clientChannel;
            public TaskHandler(SocketChannel clientChannel) {
                this.clientChannel = clientChannel;
            }

            @Override
            public void run() {
                ByteBuffer buffer = ByteBuffer.allocate(256); //создаем буфер для записи сообщений
                try {
                    boolean flag = true;
                    while (flag) {
                        buffer.clear();
                        int read = clientChannel.read(buffer); //читаем передаваемое сообщение в буфер
                        String readMessage = new String(buffer.array(), 0, read);
                        String writeMessage = null;
                        if ("bye".equalsIgnoreCase(readMessage)) {
                            writeMessage = "[Exit] bye bye!!!" + "\n";
                            flag = false;
                        }
                        else writeMessage = "[Echo] " + readMessage + "\n";

                        // записываем возвращаемые данные
                        buffer.clear();
                        buffer.put(writeMessage.getBytes());
                        buffer.flip (); // сбросываем буфер, чтобы позволить ему выводить
                        clientChannel.write(buffer);
                    }
                    clientChannel.close();
                }   catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public static void main(String[] args) throws Exception {
            ExecutorService executorService = Executors.newCachedThreadPool();

            Selector selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress("localhost", PORT));

            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println ("EchoServer started");

            while (selector.select() > 0) {
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = keys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isAcceptable()) {
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        if (socketChannel != null) {
                            // Отправить задачу на обработку
                            executorService.submit(new TaskHandler(socketChannel));
                    }
                }
            }
        }
    }
}
