package com.arlind.portfolio;

import com.arlind.portfolio.http.PortfolioServer;
import com.arlind.portfolio.repository.InMemoryPortfolioRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SmokeTest {
    public static void main(String[] args) throws Exception {
        PortfolioServer server = new PortfolioServer(9090, new InMemoryPortfolioRepository());
        server.start();

        try (Socket socket = new Socket("127.0.0.1", 9090);
             OutputStream outputStream = socket.getOutputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            outputStream.write("GET /api/profile HTTP/1.1\r\nHost: 127.0.0.1\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        server.stop();
    }
}
