package finalVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import finalVersion.OrderItem; // Import της ξεχωριστής κλάσης OrderItem


public class CustomerOrderScenario {
    private InventoryManager inventory;
    private CustomerManager customerManager; 
    private Scanner scanner;


    public CustomerOrderScenario(InventoryManager inventory, CustomerManager customerManager, Scanner scanner) {
        this.inventory = inventory;
        this.customerManager = customerManager;
        this.scanner = scanner;
    }

    public void execute() {
        System.out.println("--- Καταχώρηση Παραγγελίας Πελάτη ---");
        // 1. Επιλογή πελάτη
        int customerId;
        Customer customer = null;
        do {
            System.out.print("Customer ID (αριθμός): ");
            while (!scanner.hasNextInt()) {
                System.out.print("Μη έγκυρη είσοδος. Εισάγετε αριθμητικό ID: ");
                scanner.next(); // Καταναλώνει τη μη έγκυρη είσοδο
            }
            customerId = scanner.nextInt();
            scanner.nextLine(); // Καταναλώνει το newline
            customer = customerManager.findById(customerId); 
            if (customer == null) {
                System.out.println("Δεν βρέθηκε πελάτης με ID '" + customerId + "'. Παρακαλώ δοκιμάστε ξανά.");
                continue;
            }
        } while (customer == null);
        System.out.println("Βρέθηκε πελάτης: " + customer.getName());

        // 2. Επιλογή ειδών
        List<OrderItem> orderItems = new ArrayList<>();
        boolean adding = true;
        while (adding) {
            System.out.println("\n--- Επιλογή Προϊόντος ---");
            inventory.displayInventory();
            System.out.print("Εισάγετε κωδικό (0 για έξοδο): ");
            int code = parseIntInput();
            if (code == 0) {
                adding = false; // Έξοδος από την επιλογή προϊόντων
                break;
            }
            Product p = inventory.getProductByCode(code);
            if (p == null) {
                System.out.println("Μη έγκυρος κωδικός.");
                continue;
            }
            System.out.print("Ποσότητα: ");
            int qty = parseIntInput();
            if (qty <= 0) {
                System.out.println("Ποσότητα πρέπει να είναι θετικός ακέραιος.");
                continue;
            }

            System.out.print("Δεσμεύουμε ακόμη κι αν υπάρχει αποθεμα? (y/n): ");
            String ans = scanner.nextLine().trim();

            // Έλεγχος διαθέσιμου αποθέματος
            if (!ans.equalsIgnoreCase("y") && qty > p.getAvailableQuantity()) {
                System.out.println("Δεν επαρκεί διαθέσιμο απόθεμα και δεν επιθυμείτε κράτηση. Διαθέσιμο: " + p.getAvailableQuantity());
                continue;
            }

            OrderItem oi = findItem(orderItems, code);
            if (oi != null) {
                oi.setRequestedQty(oi.getRequestedQty() + qty);
            } else {
                orderItems.add(new OrderItem(p, qty));
            }
        }

        // 3. Αν δεν υπάρχουν είδη, ακύρωση
        if (orderItems.isEmpty()) {
            System.out.println("Ακυρώθηκε: Κενή παραγγελία.");
            return;
        }

        // 4. Δέσμευση αποθέματος
        double totalValue = 0;
        System.out.println("\n--- Περίληψη Παραγγελίας ---");
        for (OrderItem oi : orderItems) {
            // Δεσμεύουμε μόνο από το διαθέσιμο απόθεμα
            oi.setReservedQuantity(oi.getProduct().reserve(oi.getRequestedQty())); 
            oi.setBackorderedQuantity(oi.getRequestedQty() - oi.getReservedQuantity());

            double lineValue = oi.getProduct().getPrice() * oi.getRequestedQty();
            totalValue += lineValue;

            System.out.printf("%s: Ζητήθηκαν %d, Δεσμεύτηκαν %d%s @ %.2f€/τμχ\n",
                oi.getProduct().getName(), oi.getRequestedQty(), oi.getReservedQuantity(),
                (oi.getBackorderedQuantity() > 0 ? ", Λείπουν " + oi.getBackorderedQuantity() : ""), oi.getProduct().getPrice());
        }
        System.out.printf("Συνολική Αξία Παραγγελίας (εκτίμηση): %.2f€\n", totalValue);

        // 5. Τελική Επιβεβαίωση
        String confirm;
        do {
            System.out.print("Επιβεβαίωση καταχώρησης παραγγελίας (y/n)? ");
            confirm = scanner.nextLine().trim();
        } while (!confirm.equalsIgnoreCase("y") && !confirm.equalsIgnoreCase("n"));

        if (confirm.equalsIgnoreCase("n")) {
            // Απελευθέρωση δεσμευμένων
            for (OrderItem oi : orderItems) {
                oi.getProduct().unreserve(oi.getReservedQuantity()); 
            }
            System.out.println("Η παραγγελία ακυρώθηκε.");
            return;
        }

        System.out.println("Η παραγγελία καταχωρήθηκε. Πώληση μόνο κατά παραλαβή/αποστολή.");
    }

    // Βοηθητική μέθοδος για ασφαλή λήψη ακέραιων τιμών
    private int parseIntInput() {
        while (!scanner.hasNextInt()) {
            System.out.print("Μη έγκυρη είσοδος. Εισάγετε αριθμητική τιμή: ");
            scanner.next(); // Καταναλώνει τη μη έγκυρη είσοδο
        }
        int val = scanner.nextInt();
        scanner.nextLine(); // Καταναλώνει το newline μετά τον ακέραιο
        return val;
    }

    // Βοηθητική μέθοδος για εύρεση OrderItem στη λίστα
    private OrderItem findItem(List<OrderItem> list, int code) {
        for (OrderItem oi : list) {
            if (oi.getProduct().getCode() == code) {
                return oi;
            }
        }
        return null;
    }
}
