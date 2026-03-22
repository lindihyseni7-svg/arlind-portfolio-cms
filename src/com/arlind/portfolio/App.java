package com.arlind.portfolio;

import com.arlind.portfolio.http.PortfolioServer;
import com.arlind.portfolio.repository.DatabaseConfig;
import com.arlind.portfolio.repository.InMemoryPortfolioRepository;
import com.arlind.portfolio.repository.JdbcPortfolioRepository;
import com.arlind.portfolio.repository.PortfolioRepository;

public class App {
    public static void main(String[] args) throws Exception {
        int requestedPort = Integer.parseInt(System.getenv().getOrDefault("PORTFOLIO_PORT", "9191"));
        PortfolioRepository repository = createRepository();
        PortfolioServer server = startOnAvailablePort(requestedPort, repository);
        int port = server.getPort();
        System.out.println("Portfolio is running at http://localhost:" + port);
    }

    private static PortfolioServer startOnAvailablePort(int startingPort, PortfolioRepository repository) throws Exception {
        for (int port = startingPort; port < startingPort + 25; port++) {
            try {
                PortfolioServer server = new PortfolioServer(port, repository);
                server.start();
                if (port != startingPort) {
                    System.out.println("Port " + startingPort + " ishte e zene. Serveri u nis ne portin " + port + ".");
                }
                return server;
            } catch (java.net.BindException ignored) {
                // Try the next port.
            }
        }

        throw new IllegalStateException("Nuk u gjet asnje porte e lire nga " + startingPort + " deri ne " + (startingPort + 24) + ".");
    }

    private static PortfolioRepository createRepository() {
        String mode = System.getenv().getOrDefault("PORTFOLIO_DATA_MODE", "memory");

        if ("mysql".equalsIgnoreCase(mode)) {
            DatabaseConfig config = DatabaseConfig.fromEnvironment();
            System.out.println("Data source: MySQL (" + config.url() + ")");
            return new JdbcPortfolioRepository(config);
        }

        System.out.println("Data source: in-memory sample data");
        return new InMemoryPortfolioRepository();
    }
}
