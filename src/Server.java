
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;
import java.util.List;

public class Server {

    private static final int PORT = 8080;
    public static int SENDER=1;
    public static HashSet<String> names = new HashSet<String>();

    public static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    public static HashSet<Long> timers = new HashSet<Long>();

    static List<ThreadController> PLAYER_LIST = Collections.synchronizedList(new ArrayList<ThreadController>());

    // Vad den här ska gör är bara att lyssna på porten och skapa nya trådar
    public static int claimSENDER(){
        SENDER=0;
        return 1;
    }
    public static void giveBackSENDER(){
        SENDER=1;
    }
    public static void main(String[] args) throws Exception {
        System.out.println("            The server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        int id = 0;
        try {
            while (true) {
                ThreadController clientThread = new ThreadController(listener.accept(), id++);
                clientThread.start();
                PLAYER_LIST.add(clientThread);
            }
        } finally {
            listener.close();
        }
    }
}
