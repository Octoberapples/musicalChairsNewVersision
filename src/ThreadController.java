
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

// Den här klassen har som uppgift att hantera en enda klient samt att 
// skicka sina meddelanden till alla
public class ThreadController extends Thread {
    
    //nollställa trådens timer 

    private String name;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private int HOW_MANY_PLAYERS = 1;

    public ThreadController(Socket socket) {
        this.socket = socket;
    }

    public void writeToAllPlayers(String s) {
        Server.writers.stream().forEach((writer) -> {
            writer.println("MESSAGE " + s);
        });
    }

    public void writeToTheCurrentPlayer(String s) {
        out.println("MESSAGE " + s);
    }

    public void killTheClient() {
        if (name != null) {
            Server.names.remove(name);
        }
        if (out != null) {
            Server.writers.remove(out);
        }
        try {
            socket.close();
        } catch (IOException e) {
        }
    }

    public String namePlayers() {
        String pNames = Server.names.toString();
        String pNames1 = pNames.replaceAll("\\p{P}", "");
        return pNames1;
    }

    // Först ber den efter namn, sedan broadcastar den inputs 
    public void run() {
        try {

            // Skapar strömmar för socketen
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("\n            [CLIENT] <---> [SERVER] \n"
                    + "Successfully created streams in and out of the client");

            while (true) {
                out.println("WELCOME");
                break;
            }

            // Requestar ett namn från den här klienten
            while (true) {
                out.println("SUBMITNAME");
                name = in.readLine();
                if (name == null) {
                    return;
                }
                synchronized (Server.names) {
                    if (!Server.names.contains(name)) {
                        Server.names.add(name);
                        break;
                    }
                }
            }

            // när man lyckats skriva i ett unikt namn skickas 
            // NAMEACCEPT till clienten och därefter kan spelaren
            // börja skriva i rutan
            // Lägger också  till spelaren så att den kan få meddelanden
            // från alla. 
            out.println("NAMEACCEPTED");
            Server.writers.add(out);

            //nu vill jag göra en whileloop som väntar till rätt antal spelare ha anlänt
            while (Server.names.size() != HOW_MANY_PLAYERS) {
                String input = in.readLine();
                System.out.println(input);
                break;
            }

            //en loop som skriver till spelarna att alla spelarna är närvarande
            //samt namnet på spelarna
            while (Server.names.size() == HOW_MANY_PLAYERS) {
                String input = in.readLine();
                String s = input.toUpperCase();

                if (s.matches("EXIT")) {
                    out.println("EXIT ");
                    killTheClient();
                }

                String string = "All players have arrived. Let us play a game of musical chairs! \n";
                writeToAllPlayers(string);
                Thread.sleep(1000);
                writeToAllPlayers("These are the players in the game: " + namePlayers());
                Server.writers.stream().forEach((writer) -> {
                    writer.println("CONTINUE_GAME" + s);
                });
                break;
            }

            // Accept messages from this client and broadcast them.
            // Ignore other clients that cannot be broadcasted to.
            while (true) {
                String input = in.readLine();
                String s = input.toUpperCase();

                if (s.matches("CONTINUE")) {
                    Thread.sleep(1000);
                    writeToAllPlayers("Get ready the game will begin now!");
                    Thread.sleep(1000);
                    writeToAllPlayers("The music starts to play");
                    Server.writers.stream().forEach((writer) -> {
                        writer.println("SIT_DOWN");
                    });

                }
                if (s.startsWith("TIMER")) {
                    writeToTheCurrentPlayer(s.substring(6) + "\n");                   
                    Long l = Long.parseLong(s.substring(6));
                    synchronized (Server.timers) {
                        Server.timers.add(l);                        
                  }
                    
                }

                if (input == null) {
                    return;
                }

                if (s.matches("EXIT")) {
                    out.println("EXIT ");
                    killTheClient();
                }
                if (s.matches("HOW MANY")) {
                    int i = Server.names.size();
                    String str = Integer.toString(i);
                    writeToTheCurrentPlayer(str + " Player/s exist");
                }

            }
        } catch (IOException e) {
            System.out.println(e);
        } catch (InterruptedException ex) {
            Logger.getLogger(ThreadController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            // This client is going down!  Remove its name and its print
            // writer from the sets, and close its socket.
            if (name != null) {
                Server.names.remove(name);
            }
            if (out != null) {
                Server.writers.remove(out);
            }
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }
}
