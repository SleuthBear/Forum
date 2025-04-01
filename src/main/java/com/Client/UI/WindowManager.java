package com.Client.UI;
import com.Client.Client;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class WindowManager {
    JFrame window;
    JPanel messagePanel;
    JTextField newMessage = new JTextField("");
    JButton sendButton = new JButton("Send");
    Client client;

    public WindowManager(Client _client) throws IOException {
        client = _client;
        createWindow();
    }

    public void createWindow() {
        window = new JFrame();
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    client.closeConnection();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        window.setVisible(true);
        window.setTitle("Chat");
        window.setPreferredSize(new Dimension(800, 600));
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        Container content = window.getContentPane();
        content.setLayout(new BorderLayout());

        messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(messagePanel);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        content.add(scroll, BorderLayout.CENTER);

        JPanel newMessagePanel = new JPanel();
        newMessagePanel.setLayout(new BorderLayout());
        newMessage.setPreferredSize(new Dimension(500, 30));
        newMessagePanel.add(newMessage, BorderLayout.CENTER);
        sendButton.addActionListener(e -> {
            try {
                if(!newMessage.getText().isBlank()) {
                    client.con.sendMessage(client.username + "%:%" + newMessage.getText());
                    addUserMessage(newMessage.getText());
                    newMessage.setText("");
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        newMessagePanel.add(sendButton, BorderLayout.EAST);
        content.add(newMessagePanel, BorderLayout.SOUTH);
        window.pack();
        window.setLocationRelativeTo(null);

    }

    public String getUsername() throws IOException {
        String username = JOptionPane.showInputDialog(window,
                "What is your name?", null);
        return username;
    }

    public void addOtherMessage(String username, String message) {
        JPanel container = new JPanel();
        container.setLayout(new FlowLayout(FlowLayout.LEFT));
        JPanel panel = new RoundedPanel(15);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setBackground(new Color(119, 252, 123));

        JLabel usernameLabel = new JLabel(username);
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(usernameLabel);

        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(messageLabel);

        container.add(panel);
        messagePanel.add(container);
        messagePanel.revalidate();
        messagePanel.repaint();

        window.pack();

        // Auto-scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, messagePanel);
            if (scrollPane != null) {
                JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
                scrollBar.setValue(scrollBar.getMaximum());
            }
        });
    }

    private void addUserMessage(String message) {
        JPanel container = new JPanel();
        container.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JPanel panel = new RoundedPanel(15);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setBackground(new Color(50, 152, 253));

        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(messageLabel);

        container.add(panel);
        messagePanel.add(container);
        messagePanel.revalidate();
        messagePanel.repaint();

        window.pack();

        // Auto-scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, messagePanel);
            if (scrollPane != null) {
                JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
                scrollBar.setValue(scrollBar.getMaximum());
            }
        });
    }
}


