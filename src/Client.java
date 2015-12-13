
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client {

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Musical Chairs");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(17, 40);

    /*
    Klassen Client gör så att när du trycker enter efter en rad text så skickas
    texten tiill servern. Men man kan inte skriva text i rutan förrän man har 
    fått NAMEACCEPTED från servern. 
     */
    public Client() {

        // Här är layoten till rutan
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        frame.pack();

        // ActionListener har som jobb att att reagera på när man trycker på enter
        // och skickar sedan texten (textField) till servern. Sedan rensar den
        // textField för att kunna ta emot ett nytt meddelande. 
        textField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
    }

 
    
    // Den här funktionen är den som hämtar namnet
    private String getName() {
        return JOptionPane.showInputDialog(
                frame,
                "Choose a screen name:",
                "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);
    }
    
      private void getNeedMorePlayers() {
         
          JOptionPane.showMessageDialog(frame, "There are not enough players yet", "Player info", JOptionPane.PLAIN_MESSAGE );     
        
    }
      
      private String getSitDown() {
        long startTimer = System.currentTimeMillis();
        JOptionPane.showMessageDialog(frame, "SIT DOWN!", "", JOptionPane.PLAIN_MESSAGE );
        long stopTimer = System.currentTimeMillis();
        long finalTimer = stopTimer - startTimer;
        String strLong = Long.toString(finalTimer);
           return strLong;
           //     System.exit(0); <-- då stängs hela rutan ned
    }
      
      private void getWelcome() {
          JOptionPane.showMessageDialog(frame, 
                  "Welcome to a game of musical chairs \n" +
                  "  ┈┏━┓┈┈┈┈┈┏╯┈┈┈┈┏╯┈ \n" + 
                  "  ┈┣━┫┈┈┈┈┈┣╯┈┈┈┈┣╯┈ \n" + 
                  "  ╭┫╭┫┈┈┃┈╭┫┈┈┃┈╭┫┈┈ \n" + 
                  "  ╰╯╰╯┈╭┫┈╰╯┈╭┫┈╰╯┈┈ \n" + 
                  "  ┈┈┈┈┈╰╯┈┈┈┈╰╯┈┈┈┈┈ \n", 
                  "Welcome message", 
                  JOptionPane.PLAIN_MESSAGE );
      }

    // Connectar till servern och går sen i processing loopen
    private void run() throws IOException {

        // Make connection and initialize streams
        InetAddress serverAddress = InetAddress.getLocalHost(); //"localHost"; //"192.168.0.23";
        Socket socket = new Socket(serverAddress, 8080);
        System.out.println("        Successfully created a [SOCKET]");
        //InetAddress my = InetAddress.getLocalHost();
        //System.out.println(my);

        in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        System.out.println("\n            [CLIENT] <---> [SERVER] \n"
                + "Successfully created streams in and out of the server");

        // Process all messages from server, according to the protocol.
        while (true) {
            String line = in.readLine();
            if (line.startsWith("WELCOME")) {
                getWelcome();
            }if (line.startsWith("SUBMITNAME")) {
                out.println(getName());
            }if (line.startsWith("KILL")) {
                messageArea.append("You lost and is no longer a part of the game");
            }if (line.startsWith("CONTINUE_GAME")) {
                out.println("CONTINUE");
            }if (line.startsWith("SIT_DOWN")) {
                out.println("TIMER" + getSitDown());
            }if (line.startsWith("EXIT")) {
                messageArea.append("You are now no longer part of the game, have a good day!");
            }if (line.startsWith("BEGIN")) {
                out.println("YOU_CAN_BEGIN");
            }if (line.startsWith("NOT_ENOUGH_PLAYERS")){               
                getNeedMorePlayers();       
            }else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
                messageArea.append("Welcome to a game of musical chairs! \n"
                        + "To leave this game just write exit \n"
                        + "The game will begin as soon as all the players have arrived \n"
                        + "Write yes and wait until all players have arrived  \n");
            } else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");
            }
        }
    }

    // Kör klienten ohc gör så att man kan stänga rutan
    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}
