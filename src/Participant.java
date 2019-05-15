

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


/**
 * The ChatServer is able to handle up to _MAXCLIENTS clients
 * simultaneously.
 *

 */

public class Participant {
    private Integer cport = 0;
    private Integer myport;
    private Integer failCond = 0;
    private Integer timeOut = 0;
    private Integer numConnected=0;
    private String myVote ="";
    private Integer roundCounter = 0;
    private ArrayList<VoteToken> oddVote = new ArrayList<>();
    private ArrayList<VoteToken> evenVote = new ArrayList<>();
    private CoordinateThread coordinateThread;
    private ServerSocket serverSocket;
    private Boolean commitReady=false;
    private String [] globalOptions;
    private String [] globalPorts;

    public Participant(Integer cport, Integer myport, Integer timeOut, Integer failCond){
        this.cport =cport;
        this.myport = myport;
        this.coordinateThread = new CoordinateThread();
        this.failCond = failCond;
        this.timeOut = timeOut;
        try{
            this.serverSocket = new ServerSocket(myport);
        }
        catch (Exception e){
            System.err.println("Port Unavailable!");
        }
    }



    public static void main(String [] args){
        Integer coord = Integer.parseInt(args[0]);
        Integer part = Integer.parseInt(args[1]);
        Integer timeOut = Integer.parseInt(args[2]);
        Integer failCond = Integer.parseInt(args[3]);
        System.out.println(coord);
        System.out.println(part);
        System.out.println(timeOut);
        System.out.println(failCond);
        Participant participant = new Participant(coord,part,timeOut,failCond);
        participant.coordinateThread.start();
        participant.sendVote();
        participant.isReadyToCommit();
    }

    void startServer(){
        while(numConnected<globalPorts.length-2){
            try {
                Socket accept = serverSocket.accept();
                new ServerThread(accept).start();
                numConnected+=1;
            }
            catch (Exception e){

                System.err.println("Connection Error");
            }

        }
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
        }

        @Override
        public void run() {
            try{
                while (true){

                    ReqTokenizer reqTokenizer = new ReqTokenizer();
                    Token token = reqTokenizer.getToken(clientIn.readUTF());;
                    while ((token instanceof )){

                    }
                }
            }
            catch (Exception e){


            }
        }

    }

    class CoordinateThread extends Thread {


        @Override
        public void run() {
            try {
               Socket socket = new Socket("localhost",cport);
               DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
               DataInputStream din = new DataInputStream(socket.getInputStream());
               String msgout="",msgin="";

                msgout = "JOIN "+myport.toString();
                dout.writeUTF(msgout);
                dout.flush();

                while (socket.isConnected()){
                    if (din.available()>1){
                        msgin = din.readUTF();
                        storeInfo(msgin);
                        System.out.println(msgin);
                    }

                    if (globalOptions!= null && globalPorts != null){
                        randomVote();
                        startServer();
                    }
                    if(commitReady==true){

                        msgout = decideOutcome();
                        if (msgout==null){
                            System.out.println("Commit");
                            dout.writeUTF("null");
                            dout.flush();
                        }
                        else {
                            System.out.println("Commit");
                            dout.writeUTF("OUTCOME "+msgout.toString()+" My Port:"+myport);
                            dout.flush();
                        }
                        commitReady = false;
                    }
                }
            }
            catch (Exception e){
                System.err.println("ERROR");
            }
        }

    }

   private void storeInfo(String msgin){
        Token token = null;
        ReqTokenizer reqTokenizer = new ReqTokenizer();
        token = reqTokenizer.getToken(msgin);

        if (token instanceof DetailsToken){
            String ports = ((DetailsToken)token).portList;
            System.out.println(ports);
            globalPorts = ports.split(" ");
        }
        if (token instanceof VoteOptionsToken){
            String options = ((VoteOptionsToken) token).optionList;
            System.out.println(options);
            globalOptions= options.split(" ");

        }
    }

   String Vote(){
       String voteMsg ="VOTE";
        if (roundCounter == 0 && myVote!=null){
            voteMsg = voteMsg+ " "+ myport+" "+myVote;
        }
        else if (roundCounter %2 == 0){
            for (VoteToken voteToken: evenVote){
                String[] strings = voteToken.info.split(" ");
                voteMsg = voteMsg+" "+strings[0]+" "+strings[1];
            }
        }
        else {
            for (VoteToken voteToken: oddVote){
                String[] strings = voteToken.info.split(" ");
                voteMsg = voteMsg+" "+strings[0]+" "+strings[1];
            }
        }
        return voteMsg;
    }
    void sendVote(){
        while (true){
            if (globalPorts!=null && globalOptions!=null){
                for (int x = 1;x<globalPorts.length;x++){
                    try {
                        if (failCond == 1 &&  x ==2){
                            System.exit(0);
                        }
                        if (!globalPorts[x].equals(this.myport.toString())){
                            System.out.println(globalPorts[x]);
                            Socket socket = new Socket("localhost",Integer.parseInt(globalPorts[x]));
                            DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                            dout.writeUTF(Vote());
                        }

                    }
                    catch (Exception e){
                        System.out.println(e.toString());
                        System.err.println("Cannot Vote");
                    }
                }
                setRoundCounter();
                break;
            }
            else {
                try{
                    Thread.sleep(10);
                }
                catch (Exception e){
                    System.err.println("Cannot Pause Thread");
                }
            }
        }
    }

    void isReadyToCommit(){
        long start = System.currentTimeMillis();
        long end = start+timeOut;
        while (true){
            System.out.println(oddVote);
            if (evenVote.size() == globalPorts.length-2 || oddVote.size() == globalPorts.length-2){
                commitReady = true;
                break;
            }
        }
    }

    String decideOutcome(){
        Integer numMax = 0;
        String voteresult="";
        ArrayList<VoteToken> voteTokens = new ArrayList<>();
        if (oddVote.size()>evenVote.size()){
            voteTokens = oddVote;
        }
        else {
            voteTokens = evenVote;
        }

        HashMap<String,Integer> voteCounter = new HashMap<>();
        voteCounter.put(myVote,1);
        for (VoteToken voteToken: voteTokens){
           String [] tokenString = voteToken.info.split(" ");
           if (voteCounter.containsKey(tokenString[2])){
               voteCounter.put(tokenString[2],voteCounter.get(tokenString[2])+1);
           }
           else {
               voteCounter.put(tokenString[2],1);
           }
        }
        int maxValue = Collections.max(voteCounter.values());
        for (Map.Entry<String , Integer> entry : voteCounter.entrySet()) {  // Iterate through hashmap
            if (entry.getValue()==maxValue) {
                voteresult=entry.getKey();
                numMax+=1;
            }
        }
        System.out.println(numMax);
        if (numMax==1){
            for (int x =1;x<globalPorts.length;x++){
                voteresult = voteresult+" "+globalPorts[x];
            }
            return voteresult;
        }
        else return null;

    }

   synchronized void randomVote(){

        Random random = new Random();
        Integer randIndex = random.nextInt((globalOptions.length-2)+1)+1;
        myVote = globalOptions[randIndex];
    }

    synchronized void setRoundCounter(){
        roundCounter =roundCounter +1;

    }
}