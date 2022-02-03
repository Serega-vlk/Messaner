package server.Gui;

import server.Connection;
import server.ConsoleHelper;
import server.Message;
import server.MessageType;

import javax.sql.rowset.serial.SerialException;
import java.io.*;
import java.net.Socket;

public class Client {
    public static final int ClientVersion = 2;
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    public class SocketThread extends Thread{
        @Override
        public void run() {
            try {
                String address = getServerAddress();
                int port = getServerPort();
                Socket socket = new Socket(address, port);
                Client.this.connection = new Connection(socket);
                if (!clientCheckVersion()) throw new SerialException();
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e){
                notifyConnectionStatusChanged(false, null);
            } catch (SerialException e){
                notifyConnectionStatusChanged(false, "version");
            }
        }

        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage(String.format("%s присоеденилмя к чату.", userName));
        }

        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage(String.format("%s покинул чат.", userName));
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected, String reason){
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected boolean clientCheckVersion() throws IOException, ClassNotFoundException {
            connection.send(new Message(MessageType.VERSION_REQUEST, Integer.toString(ClientVersion)));
            Message answer = connection.receive();
            return answer.getType() == MessageType.VERSION_ACCEPTED;
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException{
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.NAME_REQUEST) {
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                } else if (message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true, null);
                    break;
                } else throw new IOException("Unexpected MessageType");
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            while (true){
                Message message = connection.receive();
                if (message.getType() == null) throw new IOException("Unexpected MessageType");
                switch (message.getType()){
                    case TEXT:{
                        processIncomingMessage(message.getData());
                        break;
                    } case USER_ADDED:{
                        informAboutAddingNewUser(message.getData());
                        break;
                    } case USER_REMOVED:{
                        informAboutDeletingNewUser(message.getData());
                        break;
                    } default: {
                        throw new IOException("Unexpected MessageType");
                    }
                }
            }
        }
    }

    public void run(){
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("Произошла ошибка");
            return;
        }
        if (clientConnected){
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
        } else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }
        while (clientConnected){
            String text = ConsoleHelper.readString();
            if (text.equals("exit")){
                break;
            }
            if (shouldSendTextFromConsole()){
                sendTextMessage(text);
            }
        }
    }

    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Введите адрес сервера:");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Введите порт сервера:");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Введите ваше имя:");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole(){
        return true;
    }

    protected SocketThread getSocketThread(){
        return new SocketThread();
    }

    protected void sendTextMessage(String text){
        try {
            if (!text.equals(""))
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Ошибка отправки сообщения");
            clientConnected = false;
        }
    }
}
