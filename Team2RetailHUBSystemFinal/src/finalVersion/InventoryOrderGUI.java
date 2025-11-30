package finalVersion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections; 
import java.util.Date;
import java.time.ZoneId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;


public class InventoryOrderGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private InventoryManager inventoryManager;
    private CustomerManager customerManager;
    private OrderManager orderManager;
    private List<Sale> salesHistory;

    // GUI Components
    private JTabbedPane tabbedPane;
    private JTextArea outputArea; 
    private JTable productTable; 
    private JTable customerTable; 
    private JTable orderTable; 
    private JTable currentOrderItemsTable; 
    private JTable salesHistoryTable; 

    // Fields for New Product Panel
    private JTextField newProductCodeField;
    private JTextField newProductNameField;
    private JTextField newProductPriceField;
    private JTextField newProductQuantityField;

    // Fields for Stock Management Panel
    private JTextField stockCodeField;
    private JTextField stockQuantityField;
    private JTextField searchInputField; 

    // Fields for New Customer Panel
    private JTextField newCustomerIdField;
    private JTextField newCustomerNameField;

    // Fields for Customer Order Panel
    private JComboBox<String> customerOrderCustomerChooser;
 
    private JTextField customerOrderIdField; 
    private JComboBox<String> customerOrderProductChooser;
    private JTextField customerOrderProductCodeField; 
    private JTextField customerOrderQuantityField;
    private JCheckBox allowBackorderCheckbox; 
    private List<OrderItem> currentOrderItems; 
    private JTextField orderIdField;
    private JComboBox<String> backorderProductChooser;
    private JTextField salesCustomerSearchField;
	private BorderFactory BorderBorderFactory;
	private Component selectedProductLabel;
	private Component customerOrderProductCodeSearchField;
	private Component selectedCustomerLabel;
	private Component customerOrderIdSearchField;

    public InventoryOrderGUI() {
        super("Inventory and Order Management System"); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        setSize(1200, 800); 
        setLocationRelativeTo(null); 

        // Initialize backend managers
        inventoryManager = new InventoryManager();
        customerManager = new CustomerManager();
        orderManager = new OrderManager(inventoryManager);
        salesHistory = new ArrayList<>(); 

        // Add some initial data for demonstration
        addInitialData();

        // Initialize GUI components
        tabbedPane = new JTabbedPane();
        outputArea = new JTextArea(5, 50);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane outputScrollPane = new JScrollPane(outputArea);

        // Add tabs 
        tabbedPane.addTab("Inventory Management", createInventoryPanel());
        tabbedPane.addTab("Customer Management", createCustomerPanel());
        tabbedPane.addTab("Customer Order", createCustomerOrderPanel());
        tabbedPane.addTab("Order Management", createOrderManagementPanel());
        tabbedPane.addTab("Backorder Allocation", createBackorderAllocationPanel());
        tabbedPane.addTab("Sales History", createSalesHistoryPanel()); 

        // Set up main content pane
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(tabbedPane, BorderLayout.CENTER);
        contentPane.add(outputScrollPane, BorderLayout.SOUTH);

        populateCustomerProductChoosers();
        refreshAllTables();
    }

    private void refreshAllTables() {
        updateProductTable();
        updateCustomerTable();
        updateOrderTable();
        updateCurrentOrderItemsTable(); 
        updateSalesHistoryTable(null); 
        populateCustomerProductChoosers(); 
    }

    private void addInitialData() {
        inventoryManager.addNewProduct(101, "Laptop", 1200.00, 10);
        inventoryManager.addNewProduct(102, "Mouse", 25.00, 50);
        inventoryManager.addNewProduct(103, "Keyboard", 75.00, 30);
        inventoryManager.addNewProduct(104, "Monitor", 300.00, 5);

        customerManager.addCustomer(new Customer(1, "Alice Smith", "alice@example.com", "123-456-7890", "123 Main St", 30));
        customerManager.addCustomer(new Customer(2, "Bob Johnson", "bob@example.com", "987-654-3210", "456 Oak Ave", 45));
        customerManager.addCustomer(new Customer(3, "Charlie Brown", "charlie@example.com", "555-111-2222", "789 Pine Ln", 22));

        // "Walk-in Customer" for direct sales (ID 9999 is used)
        customerManager.addCustomer(new Customer(9999, "Walk-in Customer", "walkin@example.com", "N/A", "Store Front", 0));
    }


    private void deleteSelectedCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int customerId = (int) customerTable.getValueAt(selectedRow, 0);
            
            if (customerId == 9999) {
                JOptionPane.showMessageDialog(this, "The Walk-in Customer cannot be deleted.", "Protected Customer", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete customer with ID: " + customerId + "? This cannot be undone.",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (customerManager.deleteCustomer(customerId)) {
                    outputArea.append("Customer with ID " + customerId + " deleted successfully.\n");
                    refreshAllTables();
                } else {
                    outputArea.append("Failed to delete customer with ID " + customerId + ". They may have existing orders.\n");
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error deleting customer: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    
    private void updateProductTable() {
        String[] columns = {"Code", "Name", "Price", "Total Qty", "Reserved Qty", "Available Qty"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (Product p : inventoryManager.getAllProducts()) {
            model.addRow(new Object[]{
                p.getCode(),
                p.getName(),
                String.format("%.2f€", p.getPrice()),
                p.getQuantity(),       // Συνολική ποσότητα
                p.getReservedQuantity(), // Δεσμευμένη ποσότητα
                p.getAvailableQuantity() // Διαθέσιμη ποσότητα
            });
        }
        productTable.setModel(model);
        styleTable(productTable);
    }

    private void updateCustomerTable() {
        String[] columns = {"ID", "Name", "Email", "Phone", "Address", "Age"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (Customer c : customerManager.getAllCustomers()) {
            model.addRow(new Object[]{c.getId(), c.getName(), c.getEmail(),
                                     c.getPhone(), c.getAddress(), c.getAge()});
        }
        customerTable.setModel(model);
        styleTable(customerTable);
    }

    private void updateOrderTable() {
        String[] columns = {"Order ID", "Customer", "Date", "Total Value", "Status", "Backordered Items"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        // Χρησιμοποιούμε getAllOrders()
        for (Order order : orderManager.getAllOrders()) {
            String status = order.getStatus().toString();
            String backorderedStatus = order.hasBackorderedItems() ? "YES" : "NO";


            Date oldDate = order.getTimestamp();
            LocalDateTime localDateTime = oldDate.toInstant()
                                                 .atZone(ZoneId.systemDefault())
                                                 .toLocalDateTime();

            model.addRow(new Object[]{
                order.getId(),
                order.getCustomer().getName() + " (ID: " + order.getCustomer().getId() + ")",
                localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                String.format("%.2f€", order.getTotalValue()).replace('.', ','),
                status,
                backorderedStatus
            });
        }
        orderTable.setModel(model);
        styleTable(orderTable);
    }

    private void updateCurrentOrderItemsTable() {
        String[] columns = {"Product Code", "Product Name", "Requested Qty"};
                                                                           
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (OrderItem item : currentOrderItems) {
            model.addRow(new Object[]{
                item.getProduct().getCode(),
                item.getProduct().getName(),
                item.getRequestedQty()
            });
        }
        currentOrderItemsTable.setModel(model);
        styleTable(currentOrderItemsTable);
    }

    private void updateSalesHistoryTable(List<Sale> filteredSales) {
        String[] columns = {"Sale ID", "Order ID", "Customer", "Sale Date", "Total Value", "Items Sold"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        List<Sale> list = (filteredSales != null) ? filteredSales : salesHistory;

        for (Sale sale : list) {
            StringBuilder details = new StringBuilder();
            for (OrderItem item : sale.getSoldItems()) {
                details.append(item.getProduct().getName())
                       .append(" x")
                       .append(item.getRequestedQty())
                       .append("; ");
            }
            if (details.length() >= 2) details.setLength(details.length() - 2);

            String dt = sale.getSaleDate().toInstant()
                           .atZone(ZoneId.systemDefault())
                           .toLocalDateTime()
                           .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            model.addRow(new Object[]{
                sale.getSaleId(),
                sale.getOrderId(),
                sale.getCustomer().getName() + " (ID: " + sale.getCustomer().getId() + ")",
                dt,
                String.format("%.2f€", sale.getTotalSaleValue()).replace('.', ','),
                details.toString()
            });
        }

        salesHistoryTable.setModel(model);
        styleTable(salesHistoryTable);
    }

    private void populateCustomerProductChoosers() {
        // --- Customers ---
        customerOrderCustomerChooser.removeAllItems();
        allCustomerItems.clear();
        for (Customer c : customerManager.getAllCustomers()) {
            String display = c.getName() + " (ID: " + c.getId() + ")";
            customerOrderCustomerChooser.addItem(display);
            allCustomerItems.add(display);
        }

        // Ensure searchable behavior is attached
        customerOrderCustomerChooser.setEditable(false);

        // --- Products ---
        customerOrderProductChooser.removeAllItems();
        backorderProductChooser.removeAllItems();
        allProductItems.clear();
        for (Product p : inventoryManager.getAllProducts()) {
            String display = p.getName() + " (Code: " + p.getCode() + ")";
            customerOrderProductChooser.addItem(display);
            backorderProductChooser.addItem(display);
            allProductItems.add(display);
        }

        // Ensure searchable behavior is attached
        customerOrderProductChooser.setEditable(false);
    }

    // --- GUI Panel Creation Methods ---

    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Product Table Display ---
        productTable = new JTable();
        JScrollPane productTableScrollPane = new JScrollPane(productTable);
        panel.add(productTableScrollPane, BorderLayout.CENTER);

        // Κουμπί διαγραφής προϊόντος
        JButton deleteProductButton = createStyledButton("Delete Selected Product");
        deleteProductButton.addActionListener(e -> deleteProduct());

        JPanel tableBottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tableBottomPanel.add(deleteProductButton);
        panel.add(tableBottomPanel, BorderLayout.SOUTH);


        // --- Controls Panel (North) ---
        JPanel controlsPanel = new JPanel(new GridLayout(4, 1, 10, 10));

        // Section 1: Add New Product
        JPanel newProductPanel = new JPanel(new GridBagLayout());
        newProductPanel.setBorder(BorderFactory.createTitledBorder("Add New Product"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        newProductCodeField = new JTextField(10);
        newProductNameField = new JTextField(15);
        newProductPriceField = new JTextField(10);
        newProductQuantityField = new JTextField(10);

        gbc.gridx = 0; gbc.gridy = 0; newProductPanel.add(new JLabel("Code:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; newProductPanel.add(newProductCodeField, gbc);
        gbc.gridx = 2; gbc.gridy = 0; newProductPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; newProductPanel.add(newProductNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; newProductPanel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; newProductPanel.add(newProductPriceField, gbc);
        gbc.gridx = 2; gbc.gridy = 1; newProductPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; newProductPanel.add(newProductQuantityField, gbc);

        JButton addNewProductButton = createStyledButton("Add Product");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; newProductPanel.add(addNewProductButton, gbc);
        addNewProductButton.addActionListener(e -> addNewProduct());
        controlsPanel.add(newProductPanel);

        // Section 2: Manage Stock (Add/Remove)
        JPanel stockManagementPanel = new JPanel(new GridBagLayout());
        stockManagementPanel.setBorder(BorderFactory.createTitledBorder("Manage Stock (Add/Remove)"));
        gbc.gridwidth = 1; // Reset gridwidth

        stockCodeField = new JTextField(10);
        stockQuantityField = new JTextField(10);

        gbc.gridx = 0; gbc.gridy = 0; stockManagementPanel.add(new JLabel("Product Code:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; stockManagementPanel.add(stockCodeField, gbc);
        gbc.gridx = 2; gbc.gridy = 0; stockManagementPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; stockManagementPanel.add(stockQuantityField, gbc);

        JButton addStockButton = createStyledButton("Add Stock");
        gbc.gridx = 0; gbc.gridy = 1; stockManagementPanel.add(addStockButton, gbc);
        addStockButton.addActionListener(e -> addStockToExisting());

        JButton removeStockButton = createStyledButton("Remove Stock");
        gbc.gridx = 1; gbc.gridy = 1; stockManagementPanel.add(removeStockButton, gbc);
        removeStockButton.addActionListener(e -> removeStock());
        controlsPanel.add(stockManagementPanel);

        // Section 3: Search Product
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Product by Name/Code"));
        searchInputField = new JTextField(20);
        JButton searchButton = createStyledButton("Search");
        searchPanel.add(new JLabel("Search Query:"));
        searchPanel.add(searchInputField);
        searchPanel.add(searchButton);
        searchButton.addActionListener(e -> searchProduct(searchInputField.getText()));
        controlsPanel.add(searchPanel);

        // Section 4: Direct Sale Button (New)
        JPanel directSalePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        directSalePanel.setBorder(BorderFactory.createTitledBorder("Direct Sale"));
        JButton recordDirectSaleButton = createStyledButton("Record Direct Sale");
        directSalePanel.add(recordDirectSaleButton);
        recordDirectSaleButton.addActionListener(e -> recordDirectSale());
        controlsPanel.add(directSalePanel); 

        panel.add(controlsPanel, BorderLayout.NORTH);

        return panel;
    }

    private void addNewProduct() {
        try {
            int code = Integer.parseInt(newProductCodeField.getText());
            String name = newProductNameField.getText();
            double price = Double.parseDouble(newProductPriceField.getText());
            int quantity = Integer.parseInt(newProductQuantityField.getText());

            if (inventoryManager.addNewProduct(code, name, price, quantity)) {
                outputArea.append("Product added: " + name + "\n");
                clearNewProductFields();
                refreshAllTables();
            } else {
                outputArea.append("Failed to add product (code might exist or invalid data).\n");
            }
        } catch (NumberFormatException ex) {
            outputArea.append("Invalid input for product code, price, or quantity.\n");
        }
    }

    private void addStockToExisting() {
        try {
            int code = Integer.parseInt(stockCodeField.getText());
            int quantity = Integer.parseInt(stockQuantityField.getText());

            if (inventoryManager.increaseProductStock(code, quantity)) {
                outputArea.append("Stock added for product code " + code + ": " + quantity + "\n");
                clearStockFields();
                refreshAllTables();
            } else {
                outputArea.append("Failed to add stock (product not found or invalid quantity).\n");
            }
        } catch (NumberFormatException ex) {
            outputArea.append("Invalid input for product code or quantity.\n");
        }
    }

    private void removeStock() {
        try {
            int code = Integer.parseInt(stockCodeField.getText());
            int quantity = Integer.parseInt(stockQuantityField.getText());

            if (inventoryManager.decreaseProductStock(code, quantity)) {
                outputArea.append("Stock removed for product code " + code + ": " + quantity + "\n");
                clearStockFields();
                refreshAllTables();
            } else {
                outputArea.append("Failed to remove stock (product not found, insufficient stock, or invalid quantity).\n");
            }
        } catch (NumberFormatException ex) {
            outputArea.append("Invalid input for product code or quantity.\n");
        }
    }

    /**
     * Διαγράφει το επιλεγμένο προϊόν από τον πίνακα.
     */
    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product from the table to delete.", "No Product Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Η πρώτη στήλη (index 0) είναι ο κωδικός του προϊόντος
            int productCode = (int) productTable.getValueAt(selectedRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete product with Code: " + productCode + "? This action cannot be undone.",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (inventoryManager.deleteProduct(productCode)) {
                    outputArea.append("Product with Code " + productCode + " deleted successfully.\n");
                    refreshAllTables(); 
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete product with Code: " + productCode + ".\nIt might be associated with existing orders or not found.", "Deletion Failed", JOptionPane.ERROR_MESSAGE);
                    outputArea.append("Failed to delete product with Code " + productCode + ". Product not found or associated with orders.\n");
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred during product deletion: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }


    private void searchProduct(String query) {
        if (query.trim().isEmpty()) {
            updateProductTable(); // Show all products if search query is empty
            return;
        }

        List<Product> searchResults = inventoryManager.searchByNameOrCode(query);

        String[] columns = {"Code", "Name", "Price", "Total Qty", "Reserved Qty", "Available Qty"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        if (searchResults.isEmpty()) {
            outputArea.append("No products found for query: " + query + "\n");
        } else {
            for (Product p : searchResults) {
                model.addRow(new Object[]{p.getCode(), p.getName(),
                                         String.format("%.2f€", p.getPrice()),
                                         p.getQuantity(),
                                         p.getReservedQuantity(),
                                         p.getAvailableQuantity()
                                        });
            }
            outputArea.append(searchResults.size() + " products found for query: " + query + "\n");
        }
        productTable.setModel(model);
        styleTable(productTable);
    }

    /**
     * Handles recording a direct sale, bypassing the order creation process.
     * Prompts for product code, quantity, and customer ID.
     */
    private void recordDirectSale() {
        String productCodeStr = JOptionPane.showInputDialog(this, "Enter Product Code for Direct Sale:");
        if (productCodeStr == null || productCodeStr.trim().isEmpty()) {
            outputArea.append("Direct sale cancelled or no product code entered.\n");
            return;
        }

        String quantityStr = JOptionPane.showInputDialog(this, "Enter Quantity for Direct Sale:");
        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            outputArea.append("Direct sale cancelled or no quantity entered.\n");
            return;
        }

        String customerIdStr = JOptionPane.showInputDialog(this, "Enter Customer ID for Direct Sale (Optional, leave blank for Walk-in):");
        Customer customer = null;

        try {
            int productCode = Integer.parseInt(productCodeStr);
            int quantity = Integer.parseInt(quantityStr);

            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Product product = inventoryManager.getProductByCode(productCode);
            if (product == null) {
                JOptionPane.showMessageDialog(this, "Product with code " + productCode + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if there's enough stock for a direct sale (using availableQuantity)
            if (product.getAvailableQuantity() < quantity) {
                JOptionPane.showMessageDialog(this, "Insufficient available stock for direct sale. Available: " + product.getAvailableQuantity(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (customerIdStr != null && !customerIdStr.trim().isEmpty()) {
                int customerId = Integer.parseInt(customerIdStr);
                customer = customerManager.getCustomerById(customerId);
                if (customer == null) {
                    JOptionPane.showMessageDialog(this, "Customer with ID " + customerId + " not found. Using 'Walk-in Customer'.", "Warning", JOptionPane.WARNING_MESSAGE);
                    customer = customerManager.getCustomerById(9999); // Fallback to pre-defined Walk-in Customer
                }
            } else {
                customer = customerManager.getCustomerById(9999); // Default to Walk-in Customer if ID is blank
            }

            // Ensure customer is not null at this point
            if (customer == null) {
                JOptionPane.showMessageDialog(this, "Critical: 'Walk-in Customer' (ID 9999) not found. Cannot proceed with sale.", "Error", JOptionPane.ERROR_MESSAGE);
                outputArea.append("Error: 'Walk-in Customer' (ID 9999) not found. Please ensure it's added during initialization.\n");
                return;
            }

            // Create an OrderItem for the direct sale
            OrderItem directSaleItem = new OrderItem(product, quantity);
            directSaleItem.setReservedQuantity(quantity);
            directSaleItem.setBackorderedQuantity(0);

            // Create a temporary list containing only this one saleItem
            List<OrderItem> directSaleItemsList = new ArrayList<>();
            directSaleItemsList.add(directSaleItem);

            // Create a temporary Order object for the direct sale
            Order directSaleOrder = new Order(customer, directSaleItemsList);
            directSaleOrder.setStatus(OrderStatus.FULFILLED); // Mark as fulfilled immediately for direct sale

            // Add the "sale" to sales history
            salesHistory.add(new Sale(directSaleOrder.getId(), directSaleOrder.getCustomer(),
                                      directSaleOrder.getTotalValue(), new Date(), directSaleOrder.getItems()));


            inventoryManager.decreaseProductStock(productCode, quantity); // Decrement physical stock

            outputArea.append("Direct sale of " + quantity + " " + product.getName() + " recorded successfully.\n");
            refreshAllTables();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input for product code, quantity, or customer ID.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred during direct sale: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); 
        }
    }

    /**
     * Creates the panel for Customer Management.
     * Includes sections for adding new customers and displaying existing customers.
     */
    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Customer Table Display ---
        customerTable = new JTable();
        JScrollPane customerTableScrollPane = new JScrollPane(customerTable);
        panel.add(customerTableScrollPane, BorderLayout.CENTER);

        // --- Form Panel (Top section with form + buttons) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Customer Management"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        newCustomerIdField = new JTextField(10);
        newCustomerNameField = new JTextField(15);
        JTextField newCustomerEmailField = new JTextField(15);
        JTextField newCustomerPhoneField = new JTextField(15);
        JTextField newCustomerAddressField = new JTextField(20);
        JTextField newCustomerAgeField = new JTextField(5);

        // Row 0
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1;
        formPanel.add(newCustomerIdField, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 3;
        formPanel.add(newCustomerNameField, gbc);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        formPanel.add(newCustomerEmailField, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 3;
        formPanel.add(newCustomerPhoneField, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        formPanel.add(newCustomerAddressField, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("Age:"), gbc);
        gbc.gridx = 3;
        formPanel.add(newCustomerAgeField, gbc);

        // Row 3: Buttons in a horizontal sub-panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton addCustomerButton = createStyledButton("Add Customer");
        JButton deleteCustomerButton = createStyledButton("Delete Selected Customer");
        buttonPanel.add(addCustomerButton);
        buttonPanel.add(deleteCustomerButton);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        formPanel.add(buttonPanel, gbc);

        // --- Button Actions ---
        addCustomerButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(newCustomerIdField.getText());
                String name = newCustomerNameField.getText();
                String email = newCustomerEmailField.getText();
                String phone = newCustomerPhoneField.getText();
                String address = newCustomerAddressField.getText();
                int age = Integer.parseInt(newCustomerAgeField.getText());

                Customer newCustomer = new Customer(id, name, email, phone, address, age);
                if (customerManager.addCustomer(newCustomer)) {
                    outputArea.append("Customer added: " + name + "\n");
                    clearNewCustomerFields();
                    refreshAllTables();
                } else {
                    outputArea.append("Failed to add customer (ID might exist or invalid data).\n");
                }
            } catch (NumberFormatException ex) {
                outputArea.append("Invalid input for customer ID or age.\n");
            } catch (IllegalArgumentException ex) {
                outputArea.append("Error adding customer: " + ex.getMessage() + "\n");
            }
        });

        deleteCustomerButton.addActionListener(e -> deleteSelectedCustomer());

        // Add the form panel to the top of the main panel
        panel.add(formPanel, BorderLayout.NORTH);

        return panel;
    }

    private List<String> allCustomerItems = new ArrayList<>();
    private List<String> allProductItems = new ArrayList<>();
    private JPanel createCustomerOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        currentOrderItems = new ArrayList<>();

        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Create New Order"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Initialize components ---
        customerOrderCustomerChooser = new JComboBox<>();
        customerOrderCustomerChooser.setEditable(false);

        customerOrderProductChooser = new JComboBox<>();
        customerOrderProductChooser.setEditable(false);

        customerOrderQuantityField = new JTextField(5);
        allowBackorderCheckbox = new JCheckBox("Allow Backorder (if not enough stock)");
        allowBackorderCheckbox.setSelected(true);

        // --- Customer row (Row 0) ---
        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("Select Customer:"), gbc);
        gbc.gridx = 1;
        topPanel.add(customerOrderCustomerChooser, gbc);

        gbc.gridx = 2;
        topPanel.add(new JLabel("Search ID or Name:"), gbc);
        JTextField customerSearchField = new JTextField(10);
        gbc.gridx = 3;
        topPanel.add(customerSearchField, gbc);

        JButton customerSearchButton = createStyledButton("Search");
        gbc.gridx = 4;
        topPanel.add(customerSearchButton, gbc);

        JButton resetCustomerListButton = createStyledButton("Reset");
        gbc.gridx = 5;
        topPanel.add(resetCustomerListButton, gbc);

        // --- Product row (Row 1) ---
        gbc.gridx = 0; gbc.gridy = 1;
        topPanel.add(new JLabel("Select Product:"), gbc);
        gbc.gridx = 1;
        topPanel.add(customerOrderProductChooser, gbc);

        gbc.gridx = 2;
        topPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 3;
        topPanel.add(customerOrderQuantityField, gbc);

        gbc.gridx = 4;
        topPanel.add(allowBackorderCheckbox, gbc);

        // --- Product search (Row 2) ---
        gbc.gridx = 0; gbc.gridy = 2;
        topPanel.add(new JLabel("Search Product (Code or Name):"), gbc);
        JTextField productSearchField = new JTextField(10);
        gbc.gridx = 1;
        topPanel.add(productSearchField, gbc);

        JButton productSearchButton = createStyledButton("Search");
        gbc.gridx = 2;
        topPanel.add(productSearchButton, gbc);

        JButton resetProductListButton = createStyledButton("Reset");
        gbc.gridx = 3;
        topPanel.add(resetProductListButton, gbc);

        // --- Add Item Button (Row 3) ---
        JButton addItemToOrderButton = createStyledButton("Add Item to Current Order");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 5;
        topPanel.add(addItemToOrderButton, gbc);
        addItemToOrderButton.addActionListener(e -> addItemToCurrentOrder());

        // --- Clear Order Button (Row 4) ---
        JButton clearCurrentOrderButton = createStyledButton("Clear Current Order");
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 5;
        topPanel.add(clearCurrentOrderButton, gbc);
        clearCurrentOrderButton.addActionListener(e -> clearCurrentOrder());

        panel.add(topPanel, BorderLayout.NORTH);

        // --- Order items table ---
        currentOrderItemsTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(currentOrderItemsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // --- Place Order button ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton placeOrderButton = createStyledButton("Place Order");
        bottomPanel.add(placeOrderButton);
        placeOrderButton.addActionListener(e -> placeOrder());
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // --- Customer Search Logic ---
        customerSearchButton.addActionListener(e -> {
            String query = customerSearchField.getText().trim();
            if (query.isEmpty()) {
                outputArea.append("Enter a name or ID to search for a customer.\n");
                return;
            }

            List<Customer> matches;
            try {
                int id = Integer.parseInt(query);
                Customer c = customerManager.getCustomerById(id);
                matches = (c != null) ? List.of(c) : new ArrayList<>();
            } catch (NumberFormatException ex) {
                matches = customerManager.getAllCustomers().stream()
                        .filter(c -> c.getName().toLowerCase().contains(query.toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (matches.isEmpty()) {
                outputArea.append("No customers found for: " + query + "\n");
            } else {
                customerOrderCustomerChooser.removeAllItems();
                for (Customer c : matches) {
                    customerOrderCustomerChooser.addItem(c.getName() + " (ID: " + c.getId() + ")");
                }
                outputArea.append(matches.size() + " customer(s) found for: " + query + "\n");
            }
        });

        resetCustomerListButton.addActionListener(e -> {
            populateCustomerProductChoosers();
            outputArea.append("Customer list reset.\n");
        });

        // --- Product Search Logic ---
        productSearchButton.addActionListener(e -> {
            String query = productSearchField.getText().trim();
            if (query.isEmpty()) {
                outputArea.append("Enter a product code or name to search.\n");
                return;
            }

            List<Product> matches;
            try {
                int code = Integer.parseInt(query);
                Product p = inventoryManager.getProductByCode(code);
                matches = (p != null) ? List.of(p) : new ArrayList<>();
            } catch (NumberFormatException ex) {
                matches = inventoryManager.getAllProducts().stream()
                        .filter(p -> p.getName().toLowerCase().contains(query.toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (matches.isEmpty()) {
                outputArea.append("No products found for: " + query + "\n");
            } else {
                customerOrderProductChooser.removeAllItems();
                for (Product p : matches) {
                    customerOrderProductChooser.addItem(p.getName() + " (Code: " + p.getCode() + ")");
                }
                outputArea.append(matches.size() + " product(s) found for: " + query + "\n");
            }
        });

        resetProductListButton.addActionListener(e -> {
            populateCustomerProductChoosers();
            outputArea.append("Product list reset.\n");
        });

        return panel;
    }

    private Object selectCustomerForOrder() {
	
		return null;
	}

	private Object selectProductForOrder() {

		return null;
	}

	private void addItemToCurrentOrder() {
        // Parse selected product
        String selectedProductString = (String) customerOrderProductChooser.getSelectedItem();
        if (selectedProductString == null) {
            outputArea.append("Παρακαλώ επιλέξτε ένα προϊόν.\n");
            return;
        }
        int productCode = extractCodeFromChooser(selectedProductString);
        Product product = inventoryManager.getProductByCode(productCode);
        if (product == null) {
            outputArea.append("Σφάλμα: Το προϊόν δεν βρέθηκε.\n");
            return;
        }

        // Parse quantity
        int quantity;
        try {
            quantity = Integer.parseInt(customerOrderQuantityField.getText());
            if (quantity <= 0) {
                outputArea.append("Η ποσότητα πρέπει να είναι θετικός αριθμός.\n");
                return;
            }
        } catch (NumberFormatException ex) {
            outputArea.append("Μη έγκυρη ποσότητα.\n");
            return;
        }

        // Check if item already exists in current order and update quantity
        Optional<OrderItem> existingItem = currentOrderItems.stream()
                .filter(item -> item.getProduct().getCode() == productCode)
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().addQuantity(quantity);
            outputArea.append("Προστέθηκε επιπλέον ποσότητα " + quantity + " για το " + product.getName() + ". Συνολική ζητούμενη: " + existingItem.get().getRequestedQty() + "\n");
        } else {
            currentOrderItems.add(new OrderItem(product, quantity));
            outputArea.append("Προστέθηκε " + quantity + " x " + product.getName() + " στην τρέχουσα παραγγελία.\n");
        }

        updateCurrentOrderItemsTable();
        customerOrderQuantityField.setText(""); // Clear quantity field
    }

    private void clearCurrentOrder() {
        currentOrderItems.clear();
        updateCurrentOrderItemsTable();
        outputArea.append("Η τρέχουσα παραγγελία εκκαθαρίστηκε.\n");
    }

    private void placeOrder() {
        if (currentOrderItems.isEmpty()) {
            outputArea.append("Η παραγγελία είναι κενή. Προσθέστε προϊόντα.\n");
            return;
        }

        String selectedCustomerString = (String) customerOrderCustomerChooser.getSelectedItem();
        if (selectedCustomerString == null) {
            outputArea.append("Παρακαλώ επιλέξτε έναν πελάτη.\n");
            return;
        }
        int customerId = extractIdFromChooser(selectedCustomerString);
        Customer customer = customerManager.getCustomerById(customerId);
        if (customer == null) {
            outputArea.append("Σφάλμα: Ο επιλεγμένος πελάτης δεν βρέθηκε.\n");
            return;
        }

        boolean allowBackorder = allowBackorderCheckbox.isSelected();

        // Καλούμε τον OrderManager να δημιουργήσει την παραγγελία.
  
        Order newOrder = orderManager.createOrder(customer, currentOrderItems, allowBackorder);

        if (newOrder != null) {
            outputArea.append("Παραγγελία #" + newOrder.getId() + " δημιουργήθηκε επιτυχώς.\n");

            // Η πώληση θα καταχωρείται από το κουμπί "Fulfill Order" όταν η κατάσταση είναι READY_TO_BE_DELIVERED.

            // Εμφανίζουμε την αρχική κατάσταση της παραγγελίας
            if (newOrder.getStatus() == OrderStatus.READY_TO_BE_DELIVERED) {
                outputArea.append("Η παραγγελία #" + newOrder.getId() + " είναι άμεσα Έτοιμη για Παράδοση.\n");
            } else if (newOrder.getStatus() == OrderStatus.PARTIALLY_FULFILLED) {
                outputArea.append("Η παραγγελία #" + newOrder.getId() + " είναι Μερικώς Εκπληρωμένη.\n");
            } else {
                outputArea.append("Η παραγγελία #" + newOrder.getId() + " εκκρεμεί.\n");
            }

            clearCurrentOrder(); // Καθαρίζουμε τα τρέχοντα στοιχεία παραγγελίας
            refreshAllTables();  // Ανανεώνουμε όλους τους πίνακες
        } else {
            outputArea.append("Αποτυχία δημιουργίας παραγγελίας.\n");
        }
    }


    /**
     * Creates the panel for Order Management.
     * Allows fulfilling and canceling orders.
     */
    private JPanel createOrderManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Order Table Display ---
        orderTable = new JTable();
        JScrollPane orderTableScrollPane = new JScrollPane(orderTable);
        panel.add(orderTableScrollPane, BorderLayout.CENTER);

        // --- Controls Panel (North) ---
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controlsPanel.setBorder(BorderFactory.createTitledBorder("Manage Orders"));

        orderIdField = new JTextField(10);
        controlsPanel.add(new JLabel("Order ID:"));
        controlsPanel.add(orderIdField);

        JButton fulfillOrderButton = createStyledButton("Fulfill Order");
        controlsPanel.add(fulfillOrderButton);
        fulfillOrderButton.addActionListener(e -> fulfillOrder());

        JButton cancelOrderButton = createStyledButton("Cancel Order");
        controlsPanel.add(cancelOrderButton);
        cancelOrderButton.addActionListener(e -> cancelOrder());

        panel.add(controlsPanel, BorderLayout.NORTH);

        return panel;
    }

    private void fulfillOrder() {
        try {
            int orderId = Integer.parseInt(orderIdField.getText());
            Order order = orderManager.findById(orderId);

            if (order == null) {
                outputArea.append("Σφάλμα: Παραγγελία με ID " + orderId + " δεν βρέθηκε.\n");
                return;
            }

            // Ελέγχουμε την κατάσταση της παραγγελίας
            if (order.getStatus() == OrderStatus.FULFILLED || order.getStatus() == OrderStatus.CANCELED) {
                outputArea.append("Η παραγγελία " + order.getId() + " έχει ήδη παραδοθεί ή ακυρωθεί. Δεν απαιτείται περαιτέρω ενέργεια.\n");
                return;
            }

            // Αν η παραγγελία είναι READY_TO_BE_DELIVERED, τότε κάνουμε την "παράδοση" και καταγραφή πώλησης
            if (order.getStatus() == OrderStatus.READY_TO_BE_DELIVERED) {
                outputArea.append("Η παραγγελία " + order.getId() + " είναι ήδη Έτοιμη για Παράδοση. Ολοκλήρωση πώλησης...\n");

                // Καταγραφή Πώλησης
                salesHistory.add(new Sale(order.getId(), order.getCustomer(),
                                          order.getTotalValue(), new Date(), order.getItems()));
                outputArea.append("Πώληση για παραγγελία " + order.getId() + " καταχωρήθηκε επιτυχώς.\n");

                // Τελική αλλαγή κατάστασης σε FULFILLED (Παραδόθηκε)
                order.setStatus(OrderStatus.FULFILLED);
                outputArea.append("Παραγγελία " + order.getId() + " παραδόθηκε επιτυχώς.\n");

                for (OrderItem item : order.getItems()) {
                    Product product = item.getProduct();
                    if (product != null) {
                        product.fulfillAndRelease(item.getRequestedQty());
                    }
                }
    
            } else {
                // Αν η παραγγελία δεν είναι READY_TO_BE_DELIVERED (είναι PENDING ή PARTIALLY_FULFILLED)
                outputArea.append("Προσπάθεια εκπλήρωσης παραγγελίας " + order.getId() + "...\n");
                if (orderManager.fulfillOrder(order)) { 
                    outputArea.append("Παραγγελία " + order.getId() + " εκπληρώθηκε επιτυχώς.\n");
                    if (order.getStatus() == OrderStatus.READY_TO_BE_DELIVERED) {
                        outputArea.append("Η παραγγελία " + order.getId() + " είναι τώρα Έτοιμη για Παράδοση.\n");
                    } else if (order.getStatus() == OrderStatus.PARTIALLY_FULFILLED) {
                        outputArea.append("Η παραγγελία " + order.getId() + " εκπληρώθηκε μερικώς.\n");
                    } else {
                        outputArea.append("Η παραγγελία " + order.getId() + " παραμένει σε εκκρεμότητα.\n");
                    }
                } else {
                    outputArea.append("Αποτυχία εκπλήρωσης παραγγελίας " + order.getId() + ". Ελέγξτε την κατάσταση ή το απόθεμα.\n");
                }
            }

            clearOrderIdField();
            refreshAllTables();
        } catch (NumberFormatException ex) {
            outputArea.append("Μη έγκυρο Order ID.\n");
        }
    }
    private void cancelOrder() {
        try {
            int orderId = Integer.parseInt(orderIdField.getText());
            Order order = orderManager.findById(orderId);

            if (order == null) {
                outputArea.append("Σφάλμα: Παραγγελία με ID " + orderId + " δεν βρέθηκε.\n");
                return;
            }

            if (order.getStatus() == OrderStatus.CANCELED) {
                outputArea.append("Η παραγγελία " + orderId + " έχει ήδη ακυρωθεί.\n");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to cancel Order ID: " + orderId + "?\nReserved stock will be released back to availability.",
                    "Confirm Cancellation",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {

                // Release reserved stock (make it available again)
                for (OrderItem item : order.getItems()) {
                    int reservedQty = item.getReservedQuantity();
                    Product product = item.getProduct();

                    if (reservedQty > 0 && product != null) {
                        // Reduce product's reserved quantity
                        product.setReservedQuantity(product.getReservedQuantity() - reservedQty);
                        item.setReservedQuantity(0);
                    }

                    item.setBackorderedQuantity(0); // Clear backorders
                }

                order.setStatus(OrderStatus.CANCELED);

                outputArea.append("Παραγγελία " + order.getId() + " ακυρώθηκε επιτυχώς και το απόθεμα αποδεσμεύτηκε.\n");
                clearOrderIdField();
                refreshAllTables();

            } else {
                outputArea.append("Ακύρωση παραγγελίας " + order.getId() + " ανακλήθηκε από τον χρήστη.\n");
            }

        } catch (NumberFormatException ex) {
            outputArea.append("Μη έγκυρο Order ID.\n");
        } catch (Exception ex) {
            outputArea.append("Σφάλμα κατά την ακύρωση παραγγελίας: " + ex.getMessage() + "\n");
            ex.printStackTrace();
        }
    }


    /**
     * Creates the panel for Backorder Allocation.
     * Allows allocating newly arrived stock to existing backorders.
     */
    private JPanel createBackorderAllocationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Allocate Stock to Backorders"));

        backorderProductChooser = new JComboBox<>();
        JButton allocateButton = createStyledButton("Allocate Stock to Backorders");
        
        panel.add(new JLabel("Select Product:"));
        panel.add(backorderProductChooser);
        panel.add(allocateButton);

        allocateButton.addActionListener(e -> allocateBackorders());

        return panel;
    }

    private void allocateBackorders() {
        String selectedProductString = (String) backorderProductChooser.getSelectedItem();
        if (selectedProductString == null) {
            outputArea.append("Παρακαλώ επιλέξτε ένα προϊόν για κατανομή backorder.\n");
            return;
        }

        int productCode = extractCodeFromChooser(selectedProductString);

        int allocatedQty = orderManager.allocateBackorderedItems(productCode);

        if (allocatedQty > 0) {
            outputArea.append("Συνολικά κατανεμήθηκαν " + allocatedQty + " τεμάχια για το προϊόν " + productCode + " από backorders.\n");
        } else {
            outputArea.append("Δεν κατανεμήθηκε απόθεμα για το προϊόν " + productCode + ". Είτε δεν υπάρχουν backorders, είτε δεν υπάρχει διαθέσιμο απόθεμα.\n");
        }
        refreshAllTables(); // Ενημέρωση όλων των πινάκων μετά την κατανομή
    }

    private JPanel createSalesHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        salesHistoryTable = new JTable();
        panel.add(new JScrollPane(salesHistoryTable), BorderLayout.CENTER);

        JPanel ctrls = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
        ctrls.setBorder(BorderFactory.createTitledBorder("Search Sales History"));

        JTextField salesSearchField = new JTextField(15);
        JButton searchBtn = createStyledButton("Search by ID or Name");
        JButton resetBtn = createStyledButton("Show All");

        ctrls.add(new JLabel("Customer:"));
        ctrls.add(salesSearchField);
        ctrls.add(searchBtn);
        ctrls.add(resetBtn);

        searchBtn.addActionListener(e -> {
            String q = salesSearchField.getText().trim();
            if (q.isEmpty()) {
                outputArea.append("Enter a customer name or ID to search sales.\n");
                return;
            }
            List<Sale> filtered;
            try {
                int cid = Integer.parseInt(q);
                filtered = salesHistory.stream()
                    .filter(s -> s.getCustomer() != null && s.getCustomer().getId() == cid)
                    .collect(Collectors.toList());
            } catch (NumberFormatException ex) {
                filtered = salesHistory.stream()
                    .filter(s -> s.getCustomer() != null &&
                        s.getCustomer().getName().toLowerCase().contains(q.toLowerCase()))
                    .collect(Collectors.toList());
            }
            outputArea.append(filtered.isEmpty()
                ? "No sales found for: " + q + "\n"
                : "Displaying " + filtered.size() + " sale(s) for: " + q + "\n");
            updateSalesHistoryTable(filtered);
        });

        resetBtn.addActionListener(e -> {
            salesSearchField.setText("");
            updateSalesHistoryTable(null);
            outputArea.append("Showing all sales.\n");
        });

        panel.add(ctrls, BorderLayout.NORTH);
        return panel;
    }


    // --- Helper Methods ---

    private void styleTable(JTable table) {
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(70, 130, 180)); // Steel Blue
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 100, 150), 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    

    private void clearNewProductFields() {
        newProductCodeField.setText("");
        newProductNameField.setText("");
        newProductPriceField.setText("");
        newProductQuantityField.setText("");
    }

    private void clearStockFields() {
        stockCodeField.setText("");
        stockQuantityField.setText("");
    }

    private void clearNewCustomerFields() {
        newCustomerIdField.setText("");
        newCustomerNameField.setText("");
      
    }

    private void clearOrderIdField() {
        orderIdField.setText("");
    }

    private int extractIdFromChooser(String chooserString) {
        int startIndex = chooserString.indexOf("(ID: ") + 5;
        int endIndex = chooserString.indexOf(")");
        return Integer.parseInt(chooserString.substring(startIndex, endIndex));
    }

    private int extractCodeFromChooser(String chooserString) {
        int startIndex = chooserString.indexOf("(Code: ") + 7;
        int endIndex = chooserString.indexOf(")");
        return Integer.parseInt(chooserString.substring(startIndex, endIndex));
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new InventoryOrderGUI().setVisible(true);
        });
    }
}