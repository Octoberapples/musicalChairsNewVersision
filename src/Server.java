

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;
import java.util.List;


public class Server {

    private static final int PORT = 8080;
    
    // Alla namn är sparade i den här så att man kan kolla att ett namn inte redan finns
    public static HashSet<String> names = new HashSet<String>();
    
    // Den här används för att ha alla print writers till klienterna
    // det gör det enklare att skicka meddelanden till allla spelarna
    public static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    public static HashSet<Long> timers = new HashSet<Long>();
    
    static List<ThreadController> PLAYER_LIST = Collections.synchronizedList(new ArrayList<ThreadController>());
    
    // Vad den här ska gör är bara att lyssna på porten och skapa nya trådar
    public static void main(String[] args) throws Exception {
        System.out.println("            The server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while(true) {
            ThreadController clientThread = new ThreadController(listener.accept());
            clientThread.start();
            PLAYER_LIST.add(clientThread);
            }
        } finally {
            listener.close();
        }
    }
}
