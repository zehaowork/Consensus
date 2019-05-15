import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Coordinator{
    private Integer port;
    private Integer maxClients=0;
    private Integer numOfClients=0;
    private String [] options;
    private Map<String,DataOutputStream> listClient;
    private boolean isYelled = false;

    Coordinator(Integer port,Integer maxClients, String [] options){
        this.maxClients = maxClients;
        this.port = port;
        this.options = options;
        this.listClient = Collections.synchronizedMap(new HashMap<String,DataOutputStream>(maxClients));
    }

    class ServerThread extends Thread
    {
        private Socket clientSocket;
        private String clientName;
        private DataInputStream clientIn;
        private DataOutputStream clientOut;
        private String msgin="", msgout ="";


        ServerThread(Socket client) throws IOException
        {
            clientSocket = client;
            // Open I/O steams
            clientIn = new DataInputStream(clientSocket.getInputStream());
            clientOut = new DataOutputStream(clientSocket.getOutputStream());
            clientOut.writeUTF("Welcome to chat");
            clientOut.flush();
        }

        @Override
        public void run() {
            try {
                Token token = null;
                ReqTokenizer reqTokenizer = new ReqTokenizer();
                token = reqTokenizer.getToken(clientIn.readUTF());


                if (!(token instanceof JoinToken)) {
                    clientSocket.close();
                    return;
                }
                clientName = ((JoinToken)token)._name;
                System.out.println(clientName);

                if (!(register(clientName, clientOut))) {
                    clientSocket.close();

                    return;
                }

                if (numOfClients==maxClients && isYelled == false){
                    String details = DETAILS();
                    yell(details);
                    String vote_option = VOTE_OPTIONS();
                    yell(vote_option);
                    isYelled = true;
                }


                // If this succeeds, process requests until client exits.
                String commit = clientIn.readUTF();
                System.out.println(commit);

                while (!(token instanceof ExitToken)){
                    System.out.println("wait for commit");
                    token = reqTokenizer.getToken(clientIn.readUTF());
                    System.out.println(token._req);
                }
            }
            catch (Exception e){
                System.err.println("Yell not working");

            }

        }
    }


    boolean register(String name, DataOutputStream out)
    {

        if (numOfClients >= maxClients){
            return false;
        }
        if (listClient.containsKey(name)) {
            System.err.println("ChatServer: Name already joined.");
            return false;
        }
        try {
            listClient.put(name, out);
        }
        catch (NullPointerException e) {
            System.out.println("Error");
            return false;
        }
//        System.out.println("Registered");
        numOfClients+=1;
        return true;
    }

    synchronized void yell(String msg)
    {
        try{
            String txt = msg;
            Iterator iter = listClient.values().iterator();
            while (iter.hasNext()) {
                DataOutputStream dout = (DataOutputStream) iter.next();
                dout.writeUTF(txt);
                dout.flush();
            }
        }
        catch (Exception e){
            System.err.println("ERROR!");
        }

    }


     private void startServer(){
        try{
            ServerSocket serverSocket = new ServerSocket(port);
            while (true){
                    Socket socket = serverSocket.accept();
                    new ServerThread(socket).start();

            }
        }
        catch (Exception e){
            System.err.println("ERROR");
        }

    }


    public static void main(String[] args){
        
        Integer port = Integer.parseInt(args[0]);
        Integer maxClient = Integer.parseInt(args[1]);
        
        String [] options = new String [args.length-2];
        for(int x = 2;x<args.length;x++)
        {
            options[x-2] = args[x]; 
        }
        
        // System.out.println(String.join(" ", options));

        Coordinator coordinator = new Coordinator(port,maxClient,options);
        coordinator.startServer();
    }

    private String DETAILS(){
        String msg = "DETAILS";
        for (Map.Entry<String,DataOutputStream> entry : listClient.entrySet()){
            msg = msg+" "+entry.getKey();
        }
        return msg;
    }

    private String VOTE_OPTIONS(){
        String msg = "VOTE_OPTIONS";
        for (String s: options){
           msg = msg+" "+s;
        }
        return msg;
    }
}





