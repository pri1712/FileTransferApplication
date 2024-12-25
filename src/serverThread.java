import java.net.Socket;
import java.io.*;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class serverThread extends Thread {

    private final Socket clientSocket;
    private final ConcurrentHashMap<String, String> userPassword;
    public serverThread(Socket clientSocket,ConcurrentHashMap<String,String> userPassword){
        this.clientSocket=clientSocket;
        this.userPassword=userPassword;
    }
    public void run(){
        //each client thread starts from here.

        try {
            BufferedReader in=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));//reading info from client.
            PrintWriter out=new PrintWriter(clientSocket.getOutputStream(),true);//writing to the client.
            out.println("Please enter the word login followed by login details if you are an existing user. Otherwise sign up");

            String message;
            while ((message=in.readLine())!=null){
                if("login".equalsIgnoreCase(message)){
                    handleLogIn(in,out);
                }
                else if("signup".equalsIgnoreCase(message)) {

                    handleSignUp(in,out);
                }
                else if("exit".equalsIgnoreCase(message)){
                    out.println("Goodbye!");
                    return;
                }
                else {
                    //handle more complex file related commands here.
                    if("sendfile".equalsIgnoreCase(message)){
                        //logic for the server to receive a file and store it.
                        String[] command=message.split(" ",2);
                        serverReceievefile(command[1],clientSocket);
                    }
                    else if("receievefile".equalsIgnoreCase(message)){
                        //logic for the server to send a requested file.
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void handleLogIn(BufferedReader in,PrintWriter out){
        String username;String password;
        try {
            out.println("Enter username");
            username=in.readLine();
            if(!userPassword.containsKey(username)){
                out.println("The entered username doesn't exist");
                return;
            }
            out.println("Please enter password");
            password=in.readLine();
            if(!userPassword.get(username).equals(password)){
                out.println("Incorrect pw");
                return;
            }

            out.println("Logged in successfully");


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void handleSignUp(BufferedReader in,PrintWriter out){
        out.println("Enter the username you wish to use");
        try {
            String username=in.readLine();
            if(userPassword.containsKey(username)){
                out.println("Already taken, try again");
                return;
            }
            String password;String rePass;
            while (true) {
                out.println("Enter password");
                password = in.readLine();
                if (password.length()<=6){
                    out.println("Too short! Try again");
                }
                else break;
            }
            while (true) {
                out.println("Enter password again");
                rePass = in.readLine();
                if (!rePass.equals(password)) {
                    out.println("Not the same passwords , try again");
                }
                else break;
            }
            userPassword.put(username,password);
            out.println("Signed up successfully!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void serverReceievefile(String filename,Socket clientSocket){
        try {
            InputStream is=clientSocket.getInputStream();
            DataInputStream dis=new DataInputStream(is);
            PrintWriter os=new PrintWriter(clientSocket.getOutputStream());

            //read file from socket here.

            String fileName=dis.readUTF();
            long fileSize=dis.readLong();
            if(!fileName.equals(filename)){
                os.println("File names dont match, please check.");
                return;
            }

            try( FileOutputStream fos=new FileOutputStream(fileName)) {
                byte[] buffer=new byte[8192];
                long totalByte=0;
                int bytesRead;
                while (totalByte<fileSize && (bytesRead=dis.read(buffer))>=0 ){
                    totalByte+=bytesRead;
                    fos.write(buffer,0,bytesRead);
                }

                fos.close();
                os.println("Successfully received file");
            }
            catch (FileNotFoundException e){
                e.printStackTrace();
            }

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private void serverSendFile(String filename,Socket clientSocket){
        File file= new File(filename);
        try(FileInputStream fis=new FileInputStream(file)) {
            PrintWriter printer=new PrintWriter(clientSocket.getOutputStream());
            if(!file.exists()){
                printer.println("File not found");
            }

            OutputStream os=clientSocket.getOutputStream();
            DataOutputStream dos=new DataOutputStream(os);

            dos.writeUTF(file.getName());
            dos.writeLong(file.length());
            //file metadata.

            byte[] buffer=new byte[8192];
            int bytesRead;
            while ((bytesRead=fis.read(buffer))>=0){
                dos.write(buffer,0,bytesRead);
            }
            dos.flush();
            fis.close();
            dos.close();
            os.close();
            printer.println("File sent!");


            //logic for the server to send the file to the client.
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
