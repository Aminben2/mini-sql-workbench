import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Workbench extends JFrame {
    JTextField serverInput,passwordInput,usernameInput;
    JTextArea queryInput,resInput;
    JPanel northPanel,westPanel,centerPanel;
    JLabel usernameLabel,serverLabel,passwordLabel,databasesLabel,tableListLabel,resLabel,queryLabel;

    JButton serverButton,dataBaseButton,queryButton;
    JList<String> tablesList;
    JComboBox<String> databasesList;

    Connection connection;
    ResultSet data;
    ResultSet dbs;
    Statement statement;

    String database;

    public void connectToServer(){
        try{
            databasesList.removeAllItems();
            if (connection == null){
                Class.forName("com.mysql.cj.jdbc.Driver");
                String serverAddress = serverInput.getText();
                String username = usernameInput.getText();
                String password = passwordInput.getText();
                connection = DriverManager.getConnection(serverAddress,username,password);
                statement = connection.createStatement();
                dbs = statement.executeQuery("SHOW DATABASES;");
                while (dbs.next()){
                    databasesList.addItem(dbs.getNString("Database"));
                }
            }else {
                JOptionPane.showMessageDialog(Workbench.this, "Already connected to server. Now it will disconnect");
                connection.close();
                connection = null;
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public void useDatabase(String databaseName){
        try{
            statement = connection.createStatement();
            String sql = "USE " + databaseName + ";";
            statement.executeUpdate(sql);
            database = databaseName;
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public void getTableInfo(String tableName){
        try{
            statement = connection.createStatement();
            String sql = "select * from "+tableName+" limit 1";
            data = statement.executeQuery(sql);
            ResultSetMetaData resultSetMetaData = data.getMetaData();
            int columnsNB = resultSetMetaData.getColumnCount();
            String schema = "Table : " + tableName + "\n";
            for(int i = 1; i <= columnsNB; i++) {
                schema += "    | ---- "+resultSetMetaData.getColumnName(i)+" : "+resultSetMetaData.getColumnTypeName(i)+" ("+resultSetMetaData.getPrecision(i)+")\n";
            }
            resInput.setText(schema);
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public void getDatabaseTables(){
        try{
            if(connection != null && database != null){
                PreparedStatement st = connection.prepareStatement(
                        "SELECT table_name FROM information_schema.tables WHERE table_type='BASE TABLE' AND table_schema = ?"
                );
                st.setString(1, database);
                ResultSet tables = st.executeQuery();

                DefaultListModel<String> model = new DefaultListModel<>();
                while (tables.next()){
                    model.addElement(tables.getString("table_name"));
                }
                tablesList.setModel(model);
                Workbench.this.validate();
            }else{
                JOptionPane.showMessageDialog(Workbench.this, "No database selected/No connection established");
            }
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public void queryDatabase(){
        try{
            String q = queryInput.getText();
            if(q != ""){
                statement = connection.createStatement();
                boolean res = statement.execute(q);
                if (res){
                    data = statement.getResultSet();
                    while (data.next()){

                    }
                }else{
                    resInput.setText(statement.getUpdateCount() +" Rows affected");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
  }

    public Workbench(){
        super("SQl-mini-workbench");
        this.setSize(700,700);
        this.setLayout(new BorderLayout());
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        northPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        serverLabel = new JLabel("Server :");
        usernameLabel = new JLabel("Username :");
        passwordLabel = new JLabel("Password :");
        databasesLabel = new JLabel("Databases :");

        serverInput = new JTextField("jdbc:mysql://localhost:3306/",15);
        usernameInput = new JTextField("amine",15);
        passwordInput = new JTextField("AMIN@ben1234",15);

        serverButton = new JButton("Connect to serer");
        serverButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                connectToServer();
            }
        });
        dataBaseButton = new JButton("Connect to database");
        databasesList = new JComboBox<>();
        dataBaseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (connection != null){
                    String selectedValue = (String) databasesList.getSelectedItem();
                    if (selectedValue != null) {
                        useDatabase(selectedValue);
                        getDatabaseTables();
                    } else {
                        JOptionPane.showMessageDialog(Workbench.this, "No database selected.");
                    }
                }else {
                    JOptionPane.showMessageDialog(Workbench.this, "No server selected. ");
                }
            }
        });

        northPanel.add(serverLabel);
        northPanel.add(serverInput);
        northPanel.add(usernameLabel);
        northPanel.add(usernameInput);
        northPanel.add(passwordLabel);
        northPanel.add(passwordInput);
        northPanel.add(new JLabel(""));
        northPanel.add(serverButton);
        northPanel.add(databasesLabel);
        northPanel.add(new JLabel(""));
        northPanel.add(databasesList);
        northPanel.add(dataBaseButton);

        this.add(northPanel,BorderLayout.NORTH);

        westPanel = new JPanel(new BorderLayout());
        tablesList = new JList<>();
        tableListLabel = new JLabel("Tables :");
        tablesList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                getTableInfo(tablesList.getSelectedValue());
            }
        });
        westPanel.add(tableListLabel,BorderLayout.NORTH);
        westPanel.add(new JScrollPane(tablesList), BorderLayout.CENTER);
        westPanel.add(tablesList,BorderLayout.CENTER);
        westPanel.setSize( 400, 300);
        westPanel.setPreferredSize(new Dimension(130, 500));

        this.add(westPanel,BorderLayout.WEST);

        centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        queryLabel = new JLabel("Query :");
        resLabel = new JLabel("Result :");
        queryButton = new JButton("Execute");
        queryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {

            }
        });
        queryButton.setPreferredSize(new Dimension(100, 30));
        queryInput = new JTextArea(5, 50);
        resInput = new JTextArea(15, 50);
        resInput.setEnabled(false);

        JScrollPane queryScrollPane = new JScrollPane(queryInput);
        JScrollPane resScrollPane = new JScrollPane(resInput);

        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(queryLabel, gbc);
        gbc.gridy++;
        centerPanel.add(queryScrollPane, gbc);
        gbc.gridy++;
        centerPanel.add(queryButton, gbc);
        gbc.gridy++;
        centerPanel.add(resLabel, gbc);
        gbc.gridy++;
        centerPanel.add(resScrollPane, gbc);

        this.add(centerPanel,BorderLayout.CENTER);
        this.setVisible(true);
    }
}
