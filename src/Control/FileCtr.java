/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Control;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JTextArea;

/**
 *
 * @author olliv
 */
public class FileCtr extends Thread {

    public String folder_dir = "";

    FileInputStream fin;
    BufferedInputStream bin;
    DataInputStream din;
    OutputStream ostream;

    FileOutputStream fout;
    BufferedOutputStream bout;
    InputStream istream;
    DataOutputStream dout;

    String action = "";
    String directory = "";
    String file_to_send = null;
    String file_to_receive = null;
    Socket socket;

    JTextArea jta = new JTextArea();

    public void setAction(String action) {
        this.action = action;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setFile_to_send(String file_to_send) {
        this.file_to_send = file_to_send;
    }

    public void setFile_to_receive(String file_to_receive) {
        this.file_to_receive = file_to_receive;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setJtaData(String message) {
        String data = jta.getText();
        data += message + "\n";
        jta.setText(data);
    }

    public void setJta(JTextArea jta) {
        this.jta = jta;
    }

    // Show file dir
    public String showFiles() {
        File folder = new File(directory);
        File[] files = folder.listFiles();

        int indent = 0;
        for (File file : files) {
            get_sub_path(file, indent);
        }
        return folder_dir;
    }

    public void get_sub_path(File file, int indent) {
        String indent_string = get_indent_string(indent);
        String file_name = indent_string + "|---" + file.getName();

        if (file.isFile()) {
            folder_dir += file_name + "\n";
        } else if (file.isDirectory()) {
            folder_dir += file_name + "\n";
            File[] files_in_dir = file.listFiles();
            for (File files_in_dir1 : files_in_dir) {
                get_sub_path(files_in_dir1, indent + 1);
            }
        }
    }

    public static String get_indent_string(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("|    ");
        }
        return sb.toString();
    }

    public static String get_file_extension(File file) {
        try {
            String file_name = file.getName();
            int index = file_name.lastIndexOf('.');
            String extension = file_name.substring(index);
            return extension;
        } catch (Exception e) {

        }
        return null;
    }

    // Send file data
    public void sendData() {
        System.out.println("Runned");
        try {
            File fileToSend = new File(file_to_send);
            fin = new FileInputStream(fileToSend);
            bin = new BufferedInputStream(fin);
            ostream = socket.getOutputStream();

            byte[] buffer;
            long current = 0;
            long file_length = fileToSend.length();

            dout = new DataOutputStream(socket.getOutputStream());
            String fileLen = String.valueOf(file_length);
            dout.writeUTF(fileLen);

            while (current != file_length) {
                int size = 10000;
                if (file_length - current >= size) {
                    current += size;
                } else {
                    size = (int) (file_length - current);
                    current = file_length;
                }

                buffer = new byte[size];
                bin.read(buffer, 0, size);
                ostream.write(buffer);
                sleep(1000);
                String message = caculateTime(current, file_length);
                setJtaData(message);
            }
            ostream.flush();
            bin.close();
            fin.close();
        } catch (IOException | InterruptedException ex) {
            System.out.println("Error : " + ex.getMessage());
        }
    }

    // Receive data
    public void receiveData() {
        System.out.println("Runned");
        try {
            File fileToReceive = new File(file_to_receive);
            fout = new FileOutputStream(fileToReceive);
            bout = new BufferedOutputStream(fout);
            istream = socket.getInputStream();

            System.out.println(file_to_receive);
            din = new DataInputStream(socket.getInputStream());
            String fileLen = din.readUTF();
            long file_length = new Integer(fileLen);

            byte[] contents = new byte[10000];
            int byte_read = 0;
            long current = 0;

            while ((byte_read = istream.read(contents)) != -1) {
                bout.write(contents, 0, byte_read);
                sleep(1000);
                current = current + byte_read;
                String message = caculateTime(current, file_length);
                setJtaData(message);
            }
            bout.close();
            fout.close();
        } catch (IOException | InterruptedException ex) {
            System.out.println("Error : " + ex.getMessage());
        }
    }

    public String caculateTime(long current, long file_length) {
        long percent = (current * 100) / file_length;

        long minutes = (100 - percent) / 60;
        long seconds = (100 - percent) % 60;

        String message = "Sending file ... " + percent + "% complete!."
                + " Time left estimate : " + minutes + " and " + seconds;
        return message;
    }

    @Override
    public void run() {
        if (null != action) {
            switch (action) {
                case "Send":
                    sendData();
                    break;
                case "Receive":
                    receiveData();
                    break;
                default:
                    break;
            }
        }
    }
}
