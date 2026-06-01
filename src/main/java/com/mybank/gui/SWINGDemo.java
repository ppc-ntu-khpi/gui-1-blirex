package com.mybank.gui;

import com.mybank.domain.Bank;
import com.mybank.domain.CheckingAccount;
import com.mybank.domain.Customer;
import com.mybank.domain.SavingsAccount;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class SWINGDemo {

    private final JEditorPane log;
    private final JButton show;
    private final JButton report;
    private final JComboBox<String> clients;

    public SWINGDemo() {
        log = new JEditorPane("text/html", "");
        log.setPreferredSize(new Dimension(500, 300));
        log.setEditable(false);

        show = new JButton("Show");
        report = new JButton("Report");
        clients = new JComboBox<>();

        for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
            clients.addItem(Bank.getCustomer(i).getLastName() + ", " + Bank.getCustomer(i).getFirstName());
        }
    }

    private void launchFrame() {
        JFrame frame = new JFrame("MyBank clients");
        frame.setLayout(new BorderLayout());

        JPanel cpane = new JPanel();
        cpane.setLayout(new GridLayout(1, 3));
        cpane.add(clients);
        cpane.add(show);
        cpane.add(report);

        frame.add(cpane, BorderLayout.NORTH);
        frame.add(new JScrollPane(log), BorderLayout.CENTER);

        show.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Customer current = Bank.getCustomer(clients.getSelectedIndex());
                StringBuilder sb = new StringBuilder();
                sb.append("<br>&nbsp;<b><span style=\"font-size:1.5em;\">")
                  .append(current.getLastName()).append(", ").append(current.getFirstName())
                  .append("</span></b><br><hr>");

                for (int i = 0; i < current.getNumberOfAccounts(); i++) {
                    String accType = current.getAccount(i) instanceof CheckingAccount ? "Checking" : "Savings";
                    sb.append("&nbsp;<b>Account ").append(i + 1).append(":</b> ")
                      .append(accType)
                      .append(" &nbsp; <b>Balance: <span style=\"color:red;\">$")
                      .append(String.format("%.2f", current.getAccount(i).getBalance()))
                      .append("</span></b><br>");
                }
                log.setText(sb.toString());
            }
        });

        report.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringBuilder sb = new StringBuilder();
                sb.append("<br>&nbsp;<b><span style=\"font-size:1.3em;\">CUSTOMER REPORT</span></b><br><hr>");

                for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
                    Customer cust = Bank.getCustomer(i);
                    sb.append("&nbsp;<b>").append(cust.getLastName()).append(", ").append(cust.getFirstName()).append("</b><br>");

                    for (int j = 0; j < cust.getNumberOfAccounts(); j++) {
                        String accType = cust.getAccount(j) instanceof CheckingAccount ? "Checking" : "Savings";
                        sb.append("&nbsp;&nbsp;&nbsp;")
                          .append(accType)
                          .append(" | Balance: <span style=\"color:green;\">$")
                          .append(String.format("%.2f", cust.getAccount(j).getBalance()))
                          .append("</span><br>");
                    }
                    sb.append("<br>");
                }
                log.setText(sb.toString());
            }
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setVisible(true);
    }

    private static String nextLine(BufferedReader br) throws IOException {
        String line;
        // Пропускаем пустые строки
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty()) return line;
        }
        return null;
    }

    private static void loadFromFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            int numCustomers = Integer.parseInt(nextLine(br));

            for (int i = 0; i < numCustomers; i++) {
                String line = nextLine(br);
                int lastSpace = line.lastIndexOf(' ');
                int numAccounts = Integer.parseInt(line.substring(lastSpace + 1).trim());
                String namePart = line.substring(0, lastSpace).trim();
                int nameSpace = namePart.indexOf(' ');
                String firstName = namePart.substring(0, nameSpace).trim();
                String lastName = namePart.substring(nameSpace + 1).trim();

                Bank.addCustomer(firstName, lastName);
                Customer customer = Bank.getCustomer(Bank.getNumberOfCustomers() - 1);

                for (int j = 0; j < numAccounts; j++) {
                    String[] acc = nextLine(br).split("\\s+");
                    String type = acc[0];
                    double balance = Double.parseDouble(acc[1]);
                    double extra = Double.parseDouble(acc[2]);

                    if (type.equals("S")) {
                        customer.addAccount(new SavingsAccount(balance, extra));
                    } else {
                        customer.addAccount(new CheckingAccount(balance, extra));
                    }
                }
            }
            System.out.println("Loaded from: " + filename);
        } catch (IOException ex) {
            System.err.println("File not found: " + filename + " — loading defaults");
            loadDefaultData();
        }
    }

    private static void loadDefaultData() {
        Bank.addCustomer("John", "Doe");
        Bank.addCustomer("Fox", "Mulder");
        Bank.addCustomer("Dana", "Scully");
        Bank.getCustomer(0).addAccount(new CheckingAccount(2000));
        Bank.getCustomer(1).addAccount(new SavingsAccount(1000, 3));
        Bank.getCustomer(2).addAccount(new CheckingAccount(1000, 500));
    }

    public static void main(String[] args) {
        loadFromFile("data/test.dat");
        SWINGDemo demo = new SWINGDemo();
        demo.launchFrame();
    }
}