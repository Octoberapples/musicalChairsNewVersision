
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
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
    private int MAXIMUM_PLAYERS = 2;
    public boolean ROUND_STARTED = false;
    public Long TIMER = (long) 0;
    public int ID;
    

    public ThreadController(Socket socket, int id) {
        this.socket = socket;
        this.ID = id;
    }

    public void writeToAllPlayers(String s) {
        for (int i = 0; i < Server.PLAYER_LIST.size(); i++) {
            Server.PLAYER_LIST.get(i).out.println("MESSAGE " + s);
        }
    }

   
    public void writeToAllClients(String s) {
        for (int i = 0; i < Server.PLAYER_LIST.size(); i++) {
            Server.PLAYER_LIST.get(i).out.println(s);
        }
    }

    public void writeToTheCurrentClient(String s) {
        out.println(s);
    }

    public String whoLost() {
        Long tmpSlowest = (long) 0;
        String nameOfClientThatLost = "";
        for (int i = 0; i < Server.PLAYER_LIST.size(); i++) {
            if (tmpSlowest < Server.PLAYER_LIST.get(i).TIMER) {
                tmpSlowest = Server.PLAYER_LIST.get(i).TIMER;

            }

        }
        for (int i = 0; i < Server.PLAYER_LIST.size(); i++) {
            if (tmpSlowest == Server.PLAYER_LIST.get(i).TIMER) {
                System.out.println("This client lost: " + Server.PLAYER_LIST.get(i).name);
                nameOfClientThatLost = Server.PLAYER_LIST.get(i).name;

            }
        }
        return nameOfClientThatLost;
    }

    public void writeToTheCurrentPlayer(String s) {
        out.println("MESSAGE " + s);
    }

    public void killTheClient() {
        if (name != null) {
            Server.names.remove(name);
            for (int i = 0; i < Server.PLAYER_LIST.size(); i++) {
                if (Server.PLAYER_LIST.get(i).name == name) {
                    Server.PLAYER_LIST.remove(Server.PLAYER_LIST.get(i));
                }
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
        }
    }

    public String namePlayers() {
        String pNames="";
        for (int i = 0; i < Server.PLAYER_LIST.size(); i++) {
           pNames = pNames.concat(" "+Server.PLAYER_LIST.get(i).name);
        }
        return pNames;
    }

    //MÅSTE KOMMA IHÅG ATT RENSA TIMERS
    public String getTimer() throws IOException {
        String input = in.readLine();
        String s = input.toUpperCase();
        String whoLost = "";

        if (s.startsWith("TIMER")) {
            //writeToTheCurrentPlayer(s.substring(6) + "\n");
            Long l = Long.parseLong(s.substring(6));
            TIMER = l;
            synchronized (Server.timers) {
                Server.timers.add(l);
                TIMER = l;
            }
            while (!(Server.PLAYER_LIST.size() == Server.timers.size())) {

            }
            whoLost = whoLost();
        } else {
            System.out.println("CURRPUPT: " + name);
        }

        return whoLost;
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
                Server.names.add(name);
                if (name == null) {
                    return;
                }
                break;
            }
            

            // när man lyckats skriva i ett unikt namn skickas 
            // NAMEACCEPT till clienten och därefter kan spelaren
            // börja skriva i rutan
            // Lägger också  till spelaren så att den kan få meddelanden
            // från alla. 
            out.println("NAMEACCEPTED");
            System.out.println(Server.names.size());

            //nu vill jag göra en whileloop som väntar till rätt antal spelare ha anlänt
            while (Server.names.size() != MAXIMUM_PLAYERS) {
                String input = in.readLine();
                System.out.println(input);

            }

            writeToTheCurrentClient("BEGIN");

            //en loop som skriver till spelarna att alla spelarna är närvarande
            //samt namnet på spelarna
            if (ROUND_STARTED == false) {

                for (int i = 0; i < Server.PLAYER_LIST.size(); i++) {
                    Server.PLAYER_LIST.get(i).ROUND_STARTED = true;
                }

                String input = in.readLine();
                String s = input.toUpperCase();
                if (s.matches("YOU_CAN_BEGIN")) {
                    Thread.sleep(1000);
                    String string = "All players have arrived. Let us play a game of musical chairs! \n";
                    writeToAllPlayers(string);
                    Thread.sleep(1000);
                    writeToAllPlayers("These are the players in the game: " + namePlayers());
                    Thread.sleep(1000);
                    writeToAllPlayers("The game will begin now \n");

                }
            }
            // Accept messages from this client and broadcast them.
            // Ignore other clients that cannot be broadcasted to.
            writeToAllClients("CONTINUE_GAME");
            System.out.println(ROUND_STARTED);

            while (ROUND_STARTED == true) {

                if (Server.claimSENDER() == 1) {

                    Thread.sleep(1000);
                    writeToAllPlayers("Get ready!");
                    Thread.sleep(1000);
                    writeToAllPlayers("The music starts to play");
                    //random tid egentligen
                    writeToAllClients("SIT_DOWN");
                    String loser = getTimer();
                    if (!(Server.PLAYER_LIST.size() == 1)) {
                        writeToAllPlayers(loser + " lost this round. " + namePlayers() + " are still playing. \n");
                        killTheClient(loser);
                        for (int i = 0; i < Server.PLAYER_LIST.size(); i++) {
                            Server.PLAYER_LIST.get(i).TIMER = (long) 0;
                            Server.timers.clear();
                        }
                    } else {
                        writeToAllPlayers("You are the winner!");
                    }
                }
                Server.giveBackSENDER();
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
                for (int i = 0; i < Server.PLAYER_LIST.size(); i++) {
                    if (Server.PLAYER_LIST.get(i).name == name) {
                        Server.PLAYER_LIST.remove(Server.PLAYER_LIST.get(i));
                    }
                }
            }
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    private void killTheClient(String loser) {
        for (int i = 0; i < Server.PLAYER_LIST.size(); i++) {
            if (Server.PLAYER_LIST.get(i).name == loser) {
                Server.PLAYER_LIST.get(i).killTheClient();
                Server.names.remove(loser);
            }
            

        }

    }
}
