package server.Gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientGuiView {
    private final ClientGuiController controller;

    private JFrame frame = new JFrame("Zazimsky Ruch");
    private JTextField textField = new JTextField(50);
    private JTextArea messages = new JTextArea(50, 50);
    private JTextArea users = new JTextArea(50, 10);

    public ClientGuiView(ClientGuiController controller) {
        this.controller = controller;
        initView();
    }

    private void initView() {
        textField.setEditable(false);
        messages.setEditable(false);
        users.setEditable(false);

        try {
            frame.setIconImage(ImageIO.read(ClientGuiView.class.getResourceAsStream("/icon.gif")));
        } catch (IOException ignored){
        }
        frame.setResizable(false);
        frame.getContentPane().add(textField, BorderLayout.NORTH);
        frame.getContentPane().add(new JScrollPane(messages), BorderLayout.WEST);
        frame.getContentPane().add(new JScrollPane(users), BorderLayout.EAST);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.sendTextMessage(textField.getText());
                textField.setText("");
            }
        });
    }

    public void helloMessage(){
        ImageIcon icon = null;
        try {
            icon = new ImageIcon(ImageIO.read(ClientGuiView.class.getResourceAsStream("/icon.gif")));
        } catch (IOException ignored){
        }
        JOptionPane.showMessageDialog(frame, "Добро пожаловать в чат \"Zazimsky Ruch\"\nВсе вопросы и пожелания писать в телеграм:\n@serega_vlk",
                "Добро пожаловать", JOptionPane.INFORMATION_MESSAGE, icon);
        new Thread(() -> {
            JOptionPane.showMessageDialog(frame, "Подключение к серверу...", "Подключение", JOptionPane.INFORMATION_MESSAGE);
        }).start();
    }

    public String getServerAddress() {
        String address = JOptionPane.showInputDialog(
                frame,
                "Введите адрес сервера:",
                "Конфигурация клиента",
                JOptionPane.QUESTION_MESSAGE);
        if (address == null) System.exit(-1);
        return address;
    }

    public int getServerPort() {
        while (true) {
            String port = JOptionPane.showInputDialog(
                    frame,
                    "Введите порт сервера:",
                    "Конфигурация клиента",
                    JOptionPane.QUESTION_MESSAGE);
            if (port == null) System.exit(-1);
            try {
                return Integer.parseInt(port.trim());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Был введен некорректный порт сервера. Попробуйте еще раз.",
                        "Конфигурация клиента",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public String getUserName() {
        return JOptionPane.showInputDialog(
                frame,
                "Введите ваше имя:",
                "Конфигурация клиента",
                JOptionPane.QUESTION_MESSAGE);
    }

    public void notifyConnectionStatusChanged(boolean clientConnected, String reason) {
        textField.setEditable(clientConnected);
        if (clientConnected) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Соединение с сервером установлено",
                    "Zazimsky Ruch",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            if (reason != null && reason.equals("version")){
                JOptionPane.showMessageDialog(
                        frame,
                        "Ваша версия не соответствует актуальной",
                        "Zazimsky Ruch",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(
                        frame,
                        "Клиент не подключен к серверу",
                        "Zazimsky Ruch",
                        JOptionPane.ERROR_MESSAGE);
            }
            System.exit(-1);
        }

    }

    public void userLeaves(){
        messages.append(controller.getModel().getLastLeavingUser() + " покинул чат.\n");
    }

    public void userJoin(){
        messages.append(controller.getModel().getLastJoinedUser() + " присоеденился к чату.\n");
    }

    public void refreshMessages() {
        messages.append(controller.getModel().getNewMessage() + "\n");
    }

    public void refreshUsers() {
        ClientGuiModel model = controller.getModel();
        StringBuilder sb = new StringBuilder();
        for (String userName : model.getAllUserNames()) {
            sb.append(userName).append("\n");
        }
        users.setText(sb.toString());
    }
}
