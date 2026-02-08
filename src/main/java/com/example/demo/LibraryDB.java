import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.Vector;
import java.util.ArrayList;
import java.util.List;

//Made by Fahim Tanvir and Khalid Issa using youtube videos and SQL Documentation. 
//Jessica Chen, Jason Lopez and Abul Hassan are the other members of the group and helpedwith SQL implementation and initial testing.

public class LibraryDB extends JFrame {
    // UI 
    private JTable dataTable;
    private JTextField searchField;
    private JComboBox<String> viewSelector;

    private String currentTable = "Books";

    
    /**
 * This is IMPORTANT. We used our own password and db name based on what we already setup. 
 *  Anyone using this can use whatever, just make sure its reflected here.  
 * 
 * Also you cannot delete member names if theyre connected to a checkout order. Same applies for books in a checkout
 * order. The system will detect that you are messing
 * with something regarding partial and foreign keys, in this case prerequesitess(in this case, 
 * if you somehow deleted a book checked by Person A, what exactly did they check out?).
 * Other logic like that exists here. 
 * 
 */
    private static final String DB_URL = "jdbc:mysql://localhost:XXXX/XXXX"; //Change last thing. this is where your database is hooked up 
    private static final String DB_USER = "XXXX"; //Replace this for username setup
    private static final String DB_PASS = "XXXX"; // Replace and for the love of god use a good password 

    // Get database connection
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // Execute a query
    private static ResultSet executeQuery(Connection conn, String query, Object... params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(query);
        for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
        return stmt.executeQuery();
    }

    // Execute an update query using our SQL (INSERT, UPDATE, DELETE)
    private static int executeUpdate(Connection conn, String query, Object... params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(query);
        for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
        return stmt.executeUpdate();
    }

    // Build a table from our preloaded SQL
    private static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();
        DefaultTableModel model = new DefaultTableModel();
        for (int i = 1; i <= cols; i++) model.addColumn(meta.getColumnName(i));
        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            for (int i = 1; i <= cols; i++) row.add(rs.getObject(i));
            model.addRow(row);
        }
        return model;
    }
    
    // Constructor to create GUI
    public LibraryDB() {
        setTitle("Georgia Tech Library Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.setToolTipText("Search by Title or Author");
        ToolTipManager.sharedInstance().setInitialDelay(0); 
        topPanel.add(searchField);
        JButton searchButton = new JButton("Search");
        topPanel.add(searchButton);
        viewSelector = new JComboBox<>(new String[]{
                "Books", "Members + Cards", "Librarians", "CheckDescriptions", "CheckOuts", "Overdue Books"
        });
        topPanel.add(viewSelector);
        add(topPanel, BorderLayout.NORTH);

        dataTable = new JTable();
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
        JPanel actionButtons = new JPanel();
        JButton addBtn = new JButton("Add Entry");
        JButton deleteBtn = new JButton("Delete Entry");
        actionButtons.add(addBtn);
        actionButtons.add(deleteBtn);
        bottomPanel.add(new JPanel());
        bottomPanel.add(actionButtons);
        add(bottomPanel, BorderLayout.SOUTH);

        loadTable("Books");

        viewSelector.addActionListener(e -> {
            currentTable = viewSelector.getSelectedItem().toString();
            if (currentTable.equals("Overdue Books")) {
                loadOverdueBooks();
            } else {
                loadTable(currentTable);
            }
        });
        searchButton.addActionListener(e -> performSearch());
        addBtn.addActionListener(e -> showAddDialog(currentTable));
        deleteBtn.addActionListener(e -> deleteSelectedRow(currentTable));
        setVisible(true);
    }

    private void loadTable(String table) {
        try (Connection conn = getConnection()) {
            String query = getQueryForTable(table);
            if (query.isEmpty()) return;
            ResultSet rs = executeQuery(conn, query);
            dataTable.setModel(buildTableModel(rs));
        } catch (SQLException e) {
            showError("Error loading table: " + e.getMessage());
        }
    }
    private String getQueryForTable(String table) {
        return switch (table) {
            case "Books" -> "SELECT * FROM Books";
            case "Members + Cards" -> "SELECT m.SSN, m.MemberName, m.Role, m.Address, c.CardNumber, c.CardName, c.ExpirationDate FROM Member m JOIN Card c ON m.SSN = c.SSN";
            case "Librarians" -> "SELECT * FROM Librarian";
            case "CheckDescriptions" -> "SELECT * FROM CheckDescription";
            case "CheckOuts" -> "SELECT * FROM CheckOut";
            default -> "";
        };
    }

    private void loadOverdueBooks() {
        String query = "SELECT c.COMemberName, c.COMemberNumber, c.COBookTitle, c.COISBN, c.CheckoutDate, c.ReturnDeadline, m.Address FROM CheckOut c JOIN Member m ON c.COMemberName = m.MemberName WHERE c.ReturnDeadline < ? ORDER BY c.ReturnDeadline";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            dataTable.setModel(buildTableModel(stmt.executeQuery()));
        } catch (SQLException e) {
            showError("Error loading overdue books: " + e.getMessage());
        }
    }

    private void performSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) { loadTable(currentTable); return; }
        try (Connection conn = getConnection()) {
            String query = getSearchQuery(currentTable);
            if (query == null) { JOptionPane.showMessageDialog(this, "Search is only available for Books and Members + Cards.", "Search Not Supported", JOptionPane.INFORMATION_MESSAGE); return; }
            ResultSet rs = executeQuery(conn, query, "%" + keyword + "%", "%" + keyword + "%");
            dataTable.setModel(buildTableModel(rs));
        } catch (SQLException e) {
            showError("Error performing search: " + e.getMessage());
        }
    }

    private String getSearchQuery(String table) {
        return switch (table) {
            case "Books" -> "SELECT * FROM Books WHERE Title LIKE ? OR Author LIKE ?";
            case "Members + Cards" -> "SELECT m.SSN, m.MemberName, m.Role, m.Address, c.CardNumber, c.CardName, c.ExpirationDate FROM Member m JOIN Card c ON m.SSN = c.SSN WHERE m.MemberName LIKE ?";
            default -> null;
        };
    }

    private void showAddDialog(String table) {
        switch (table) {
            case "Books": showForm("Add Book", "Books", new String[]{"ISBN", "Author", "Title", "NumOfCopies", "Binding", "Description", "Language"}, new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR}); break;
            // Modified: Removed "SSN" from the Card table fields in the UI.
            case "Members + Cards": showForm("Add Member + Card", "Member", new String[]{"SSN", "MemberName", "Address", "Role"}, new int[]{Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR}, "Card", new String[]{"CardNumber", "CardName", "Photo", "ExpirationDate"}, new int[]{Types.INTEGER, Types.VARCHAR, Types.BLOB, Types.DATE}); break;
            case "Librarians": showForm("Add Librarian", "Librarian", new String[]{"EMPLID", "Position", "LibrarianName"}, new int[]{Types.INTEGER, Types.VARCHAR, Types.VARCHAR}); break;
            case "CheckDescriptions": showForm("Add Check Description", "CheckDescription", new String[]{"CDEMPLID", "CDLibrarianName", "CDMemberName", "BookDescription", "CDISBN", "Position"}, new int[]{Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR}); break;
            case "CheckOuts": showCheckOutForm(); break;
            case "Overdue Books": JOptionPane.showMessageDialog(this, "Adding directly to 'Overdue Books' view is not supported. Please use 'CheckOuts' to add new checkouts.", "Information", JOptionPane.INFORMATION_MESSAGE); break;
        }
    }
    
    // showForm now handles forms, specifically Member and card inputs. This helps us keep thins streamlined.
    private void showForm(String title, String table1Name, String[] fieldNames1, int[] fieldTypes1, String table2Name, String[] fieldNames2, int[] fieldTypes2) {
        JTextField[] fields1 = new JTextField[fieldNames1.length];
        Object[] formFields = new Object[fieldNames1.length * 2 + (table2Name != null ? fieldNames2.length * 2 : 0)];
        int k = 0;
        for (int i = 0; i < fieldNames1.length; i++) {
            fields1[i] = new JTextField();
            formFields[k++] = fieldNames1[i] + ":";
            formFields[k++] = fields1[i];
        }

        JTextField[] fields2 = null;
        if (table2Name != null) {
            fields2 = new JTextField[fieldNames2.length];
            for (int i = 0; i < fieldNames2.length; i++) {
                fields2[i] = new JTextField(fieldTypes2[i] == Types.DATE ? LocalDate.now().plusYears(2).toString() : "");
                formFields[k++] = fieldNames2[i] + ":";
                formFields[k++] = fields2[i];
            }
        }

        if (JOptionPane.showConfirmDialog(this, formFields, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false); // Start transaction
                try {
                    // Insert into table1 (e.g., Member)
                    insertIntoTable(conn, table1Name, fieldNames1, fieldTypes1, fields1);

                    if (table2Name != null) {
                        // For the Card table, the SSN needs to be taken from the Member's SSN
                        String memberSsn = fields1[0].getText(); 

                        // Create a temporary text box  
                        // to include the Member's SSN for the Card table insertion.
                        JTextField[] finalCardFields = new JTextField[fieldNames2.length + 1]; 
                        String[] finalCardFieldNames = new String[fieldNames2.length + 1];
                        int[] finalCardFieldTypes = new int[fieldTypes2.length + 1];

                        // Copy existing Card fields
                        int cardFieldIdx = 0;
                        for (JTextField field : fields2) {
                            finalCardFields[cardFieldIdx] = field;
                            finalCardFieldNames[cardFieldIdx] = fieldNames2[cardFieldIdx];
                            finalCardFieldTypes[cardFieldIdx] = fieldTypes2[cardFieldIdx];
                            cardFieldIdx++;
                        }
                        // Allow appends to the Member's SSN
                        finalCardFields[cardFieldIdx] = new JTextField(memberSsn);
                        finalCardFieldNames[cardFieldIdx] = "SSN";
                        finalCardFieldTypes[cardFieldIdx] = Types.INTEGER; 

                        insertIntoTable(conn, table2Name, finalCardFieldNames, finalCardFieldTypes, finalCardFields);
                    }
                    conn.commit(); // Commit any modifications  if all successful
                    JOptionPane.showMessageDialog(this, "Entry added successfully.");
                    loadTable(currentTable.equals("Members + Cards") ? currentTable : table1Name); // Reload correct table
                } catch (SQLException | NumberFormatException ex) {
                    conn.rollback(); // Rollback on error
                    throw ex; // 
                } finally {
                    conn.setAutoCommit(true); // Always restore auto-commit mode. This just helps our program remain error and hassle free with modifying  data. 
                }
            } catch (SQLException e) {
                showError("Error adding entry: " + e.getMessage());
            } catch (NumberFormatException e) {
                showError("Invalid number format: " + e.getMessage());
            }
        }
    }
    

    // Overloaded showForm for single-table inserts
    private void showForm(String title, String tableName, String[] fieldNames, int[] fieldTypes) {
        showForm(title, tableName, fieldNames, fieldTypes, null, null, null);
    }

    private void insertIntoTable(Connection conn, String tableName, String[] fieldNames, int[] fieldTypes, JTextField[] fields) throws SQLException {
        StringBuilder queryBuilder = new StringBuilder("INSERT INTO " + tableName + " (");
        for (String fieldName : fieldNames) queryBuilder.append(fieldName).append(", ");
        queryBuilder.delete(queryBuilder.length() - 2, queryBuilder.length()).append(") VALUES (");
        for (int i = 0; i < fieldNames.length; i++) queryBuilder.append("?, ");
        queryBuilder.delete(queryBuilder.length() - 2, queryBuilder.length()).append(")");
        PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString());

        for (int i = 0; i < fields.length; i++) {
            String text = fields[i].getText();
            if (fieldTypes[i] == Types.INTEGER) stmt.setInt(i + 1, Integer.parseInt(text));
            else if (fieldTypes[i] == Types.DATE) stmt.setDate(i + 1, Date.valueOf(text));
            else if (fieldTypes[i] == Types.BLOB) stmt.setNull(i + 1, Types.BLOB); // For Photo (Members + Cards)
            else stmt.setString(i + 1, text);
        }
        stmt.executeUpdate();
    }

    //Process for checkouts
    private void showCheckOutForm() {
        JTextField memName = new JTextField(), cardNo = new JTextField(), title = new JTextField(), isbn = new JTextField(),
                coDate = new JTextField(LocalDate.now().toString()), due = new JTextField(LocalDate.now().plusDays(14).toString());
        Object[] fields = {"Member Name:", memName, "Card Number:", cardNo, "Book Title:", title, "Book ISBN:", isbn, "Checkout Date (YYYY-MM-DD):", coDate, "Return Deadline (YYYY-MM-DD):", due};
        if (JOptionPane.showConfirmDialog(this, fields, "Add CheckOut", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false); // Start transaction
                try {
                    int memberCardNum = Integer.parseInt(cardNo.getText());
                    String bookISBN = isbn.getText();
                    String bookTitle = title.getText();
                    String memberName = memName.getText();

                    validateMemberCard(conn, memberName, memberCardNum);
                    int availableCopies = validateBookAvailability(conn, bookISBN, bookTitle);
                    checkMemberCheckoutLimit(conn, memberCardNum);
                    checkDuplicateCheckout(conn, memberCardNum, bookISBN);
                    executeUpdate(conn, "INSERT INTO CheckOut (COMemberName, COMemberNumber, COBookTitle, COISBN, CheckoutDate, ReturnDeadline) VALUES (?, ?, ?, ?, ?, ?)",
                            memberName, memberCardNum, bookTitle, bookISBN, Date.valueOf(coDate.getText()), Date.valueOf(due.getText()));
                    executeUpdate(conn, "UPDATE Books SET NumOfCopies = ? WHERE ISBN = ?", availableCopies - 1, bookISBN);

                    conn.commit(); // Commit final results
                    JOptionPane.showMessageDialog(this, "Book checked out successfully.");
                    loadTable("CheckOuts");
                } catch (SQLException | NumberFormatException ex) {
                    conn.rollback(); // Rollback on error if something is wrong
                    showError("Error checking out book: " + ex.getMessage());
                } finally {
                    conn.setAutoCommit(true); 
                }
            } catch (SQLException e) { showError("Database connection error: " + e.getMessage()); }
        }
    }

    private void validateMemberCard(Connection conn, String memberName, int cardNumber) throws SQLException {
        ResultSet rs = executeQuery(conn, "SELECT m.SSN FROM Member m JOIN Card c ON m.SSN = c.SSN WHERE m.MemberName = ? AND c.CardNumber = ?", memberName, cardNumber);
        if (!rs.next()) throw new SQLException("Member not found or card number doesn't match member.");
    }
    private int validateBookAvailability(Connection conn, String isbn, String title) throws SQLException {
        ResultSet rs = executeQuery(conn, "SELECT NumOfCopies FROM Books WHERE ISBN = ? AND Title = ?", isbn, title);
        if (!rs.next()) throw new SQLException("Book not found or ISBN/Title mismatch.");
        int availableCopies = rs.getInt("NumOfCopies");
        if (availableCopies <= 0) throw new SQLException("No copies of this book are currently available for checkout.");
        return availableCopies;
    }

    //Limiter to make sure members can have 5 books per person. 
    private void checkMemberCheckoutLimit(Connection conn, int memberCardNum) throws SQLException {
        ResultSet rs = executeQuery(conn, "SELECT COUNT(*) FROM CheckOut WHERE COMemberNumber = ?", memberCardNum);
        rs.next();
        if (rs.getInt(1) >= 5) throw new SQLException("Member has reached the maximum checkout limit (5 books).");
    }
    private void checkDuplicateCheckout(Connection conn, int memberCardNum, String bookISBN) throws SQLException {
        ResultSet rs = executeQuery(conn, "SELECT COUNT(*) FROM CheckOut WHERE COMemberNumber = ? AND COISBN = ?", memberCardNum, bookISBN);
        rs.next();
        if (rs.getInt(1) > 0) throw new SQLException("This specific book (ISBN: " + bookISBN + ") is already checked out by this member.");
    }

    private void deleteSelectedRow(String table) {
        int row = dataTable.getSelectedRow();
        if (row == -1) { showError("Select a row to delete."); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the selected entry?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            try {
                String query = getDeleteQuery(table);
                if (query == null) {
                    showError("Deletion not supported for this view.");
                    conn.rollback(); // Rollback if query is null
                    return;
                }
                List<String> primaryKeys = getPrimaryKeys(table, row);
                if (primaryKeys.isEmpty()) {
                    showError("Cannot determine primary key for deletion.");
                    conn.rollback(); // Rollback if primary keys are empty
                    return;
                }

                int rowsAffected = 0;
                if (table.equals("Members + Cards")) {
                    // Special handling for Members + Cards: delete Card first
                    String memberSsn = primaryKeys.get(0);

                    executeUpdate(conn, "DELETE FROM Card WHERE SSN = ?", memberSsn);
                    // Then delete from Member table
                    rowsAffected = executeUpdate(conn, query, memberSsn); 
                } else {
                    // General case for other tables
                    rowsAffected = executeUpdate(conn, query, primaryKeys.toArray());
                }

                if (rowsAffected > 0) {
                    // If a checkout was deleted, increment book copies back
                    if (table.equals("CheckOuts") || table.equals("Overdue Books")) {
                        String isbn = (String) dataTable.getValueAt(row, dataTable.getColumn("COISBN").getModelIndex());
                        executeUpdate(conn, "UPDATE Books SET NumOfCopies = NumOfCopies + ? WHERE ISBN = ?", 1, isbn);
                    }
                    conn.commit(); 
                    loadTable(table); 
                    JOptionPane.showMessageDialog(this, "Entry deleted successfully.");
                } else {
                    conn.rollback(); // Rollback if no rows affected (deletion might have failed silently due to Foreign keys interupting or affecting as a prereq)
                    showError("Failed to delete entry. It might not exist, keys are incorrect, or related records (e.g., checkouts, descriptions) still exist for this member.");
                }
            } catch (SQLException ex) {
                conn.rollback(); // Rollback on any SQL error that is met with user trying to attempt to put stuff in
                showError("Error deleting entry: " + ex.getMessage());
            } finally {
                conn.setAutoCommit(true); 
            }
        } catch (SQLException e) { showError("Database connection error: " + e.getMessage()); }
    }


    private String getDeleteQuery(String table) {
        return switch (table) {
            case "Books" -> "DELETE FROM Books WHERE ISBN = ?";
            case "Members + Cards" -> "DELETE FROM Member WHERE SSN = ?";
            case "Librarians" -> "DELETE FROM Librarian WHERE EMPLID = ?";
            case "CheckDescriptions" -> "DELETE FROM CheckDescription WHERE CDEMPLID = ? AND CDISBN = ?";
            case "CheckOuts", "Overdue Books" -> "DELETE FROM CheckOut WHERE COMemberNumber = ? AND COISBN = ?";
            default -> null; // Deletion not supported for this table. Not gona lie, we could've just made the delete entry button dissapear when user goes to overdue table, but didn't get around to implementing. 
        };
    }


    private List<String> getPrimaryKeys(String table, int row) {
        List<String> pKeys = new ArrayList<>();
        switch (table) {
            case "Books": pKeys.add(dataTable.getValueAt(row, 0).toString()); break; // Assuming ISBN is 1st col
            case "Members + Cards": pKeys.add(dataTable.getValueAt(row, 0).toString()); break; // Assuming SSN is 1st col
            case "Librarians": pKeys.add(dataTable.getValueAt(row, 0).toString()); break; // Assuming EMPLID is 1st col
            case "CheckDescriptions": pKeys.add(dataTable.getValueAt(row, 0).toString()); pKeys.add(dataTable.getValueAt(row, 4).toString()); break; // Assuming CDEMPLID and CDISBN
            case "CheckOuts", "Overdue Books": pKeys.add(dataTable.getValueAt(row, dataTable.getColumn("COMemberNumber").getModelIndex()).toString()); pKeys.add(dataTable.getValueAt(row, dataTable.getColumn("COISBN").getModelIndex()).toString()); break;
        }
        return pKeys;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }




//This runs and stores the database GUI

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) { e.printStackTrace(); } // Catch generic Exception for simplicity
        new LibraryDB();
    }
}