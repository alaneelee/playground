package com.example.playground;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BlockNonBlockExamples {
    @Test
    public void blockExample() {
        int port = 8080;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Started server and port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connected Client: " + clientSocket.getInetAddress());

                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String inputLine = reader.readLine();
                System.out.println("Received message: " + inputLine);

                clientSocket.close();
                System.out.println("Closed Client: " + clientSocket.getInetAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void nonBlockExample() {
        int port = 8090;
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            System.out.println("Started server and port " + port);

            while (true) {
                System.out.println(LocalDateTime.now());

                SocketChannel clientChannel = serverSocketChannel.accept();
                if (clientChannel == null) {
                    Thread.sleep(500);
                    continue;
                }

                System.out.println("Closed Client: " + clientChannel.getRemoteAddress());

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                int bytesRead = clientChannel.read(buffer);

                if (bytesRead != -1) {
                    buffer.flip();
                    byte[] data = new byte[buffer.limit()];
                    buffer.get(data);
                    System.out.println("Received message: " + new String(data));
                }

                if (clientChannel.isOpen()) {
                    System.out.println("Closing Client: " + clientChannel.getRemoteAddress()
                        clientChannel.close());
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
