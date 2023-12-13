import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
public class ClientChat {


    public static void main(String[] args) {
        String serverAddress = "192.168.1.16";
        int serverPort = 8080;

        try (Socket socket = new Socket(serverAddress, serverPort);
             BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connesso al server. Inserisci il tuo nome:");
            String name = userInput.readLine();
            out.println(name);
            System.out.println("Inizia conversazione in Chat:");
            // Thread per la lettura dei messaggi dal server
            new Thread(() -> {
                try {
                    String message;
                    while ((message = serverIn.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Invio dei messaggi al server
            String userMessage;
            while ((userMessage = userInput.readLine()) != null) {
                out.println(userMessage);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
