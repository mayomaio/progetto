import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerChat {

    private static final String SERVER_IP="192.168.1.16";
    private static final int PORT = 8080;

    private static Map<String, PrintWriter> clients = new HashMap<>();

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT, 8080, InetAddress.getByName(SERVER_IP))) {
            System.out.println("Server in ascolto su " + SERVER_IP + " su porta: " + PORT);


            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuova connessione: " + clientSocket);

                // Creiamo un nuovo thread per gestire il client
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter writer;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);

                // Leggiamo il nome del client
                clientName = reader.readLine();
                System.out.println("Nuovo client: " + clientName);

                // Aggiungiamo il writer alla mappa dei client
                clients.put(clientName, writer);

                // Leggiamo i messaggi dal client e li inoltriamo al client specificato
                String message;
                while ((message = reader.readLine()) != null) {
                    if (message.indexOf(":")!=-1){
                        System.out.println(clientName + ": " + message);
                        sendPrivateMessage(message);
                    } else {
                        for (PrintWriter clientWriter : clients.values()) {
                            clientWriter.println(clientName + ": " + message);
                        }

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Rimuoviamo il writer dalla mappa quando il client si disconnette
                clients.remove(clientName);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendPrivateMessage(String message) {
            // Estrai il destinatario e il messaggio dalla stringa ricevuta
            String[] parts = message.split(":", 2);
            if (parts.length == 2) {
                String recipient = parts[0].trim();
                String privateMessage = parts[1].trim();

                // Inoltra il messaggio al destinatario se esiste nella mappa dei client
                if (clients.containsKey(recipient)) {
                    clients.get(recipient).println(clientName + " ti dice: " + privateMessage);
                }
            }
        }
    }
}

