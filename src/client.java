import java.io.*;
import java.net.Socket;

public class client {
    public static void main(String[] args) {
        final int port=4444;
        final String host="localhost";
        try(Socket socket=new Socket(host,port)) {
            //trying to connect to the server on port 4444.
            System.out.println("Connected to the server");
            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out=new PrintWriter(socket.getOutputStream(),true);
            BufferedReader userMessage=new BufferedReader(new InputStreamReader(System.in));
            String message;
            boolean running=true;
            while (running){
                System.out.println(in);
                System.out.println("> ");
                message=userMessage.readLine();
                if("exit".equalsIgnoreCase(message)){
                    System.out.println("Exiting");
                    out.println("exit");
                    running=false;
                }
                else if(message.startsWith("sendfile")) {
                    //logic to send a file to the server.
                    out.println("sendfile");
                    String[] command = message.split(" ", 2);
                    if (command.length < 2) {
                        System.out.println("Usage: sendfile <filepath>");
                    }
                    else {
                        fileSender(command[1],socket);
                    }
                }
                else if(message.startsWith("receievefile")){
                    //logic to receive a file from the server.
                    String[] command=message.split(" ",2);
                    if(command.length<2){
                        System.out.println("Usage: receievefile <filename>");
                    }
                    else{
                        fileReceiever(command[1],socket);
                    }
                }
                else{
                    out.println(message);
                }

            }
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void fileSender(String filepath,Socket socket){

        try {
            File file =new File(filepath);
            if(!file.exists()){
                System.out.println("Filepath not found! please try again");
            }
            else{
                OutputStream outputstream=socket.getOutputStream();
                DataOutputStream datastream=new DataOutputStream(outputstream);//allows to read and write primitive data types to an output stream.

                datastream.writeUTF(file.getName());
                datastream.writeLong(file.length());
                //file metadata.

                try (FileInputStream fileReader = new FileInputStream(file)) {
                    //read using the fileReader object.

                    byte[] buffer=new byte[8192];
                    int bytesRead;
                    while ((bytesRead=fileReader.read(buffer))>0){
                        datastream.write(buffer,0,bytesRead);
                    }
                    fileReader.close();
                    System.out.println("File at "+ filepath+" sent!");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static void fileReceiever(String filename,Socket socket){
        try {
            InputStream inputstream=socket.getInputStream();
            DataInputStream datastream=new DataInputStream(inputstream);

            String fileName=datastream.readUTF();
            long fileSize=datastream.readLong();

            try(FileOutputStream fos=new FileOutputStream(fileName)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;
                while (totalBytes < fileSize && (bytesRead = datastream.read(buffer)) != 0) {
                    totalBytes += bytesRead;
                    fos.write(buffer,0,bytesRead);
                }
                fos.close();
                System.out.println("File received successfully");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
