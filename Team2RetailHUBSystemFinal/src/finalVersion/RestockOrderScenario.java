package finalVersion;

import java.util.List;
import java.util.Scanner;

class RestockOrderScenario {
    private static final int MaxRestock = 500;
    private InventoryManager inventory;
    private Scanner scanner;

    public RestockOrderScenario(InventoryManager inventory, Scanner scanner) {
        this.inventory = inventory;
        this.scanner = scanner;
    }

    public void execute() {
        System.out.println("--- Αναπλήρωση Αποθέματος Προϊόντος ---");
        List<Product> allProducts = inventory.getAllProducts();
        if (allProducts.isEmpty()) {
            System.out.println("Δεν υπάρχουν προϊόντα στο απόθεμα για αναπλήρωση.");
            System.out.println("---------------------------------------------------");
            return;
        }
        System.out.println("Διαθέσιμα Προϊόντα:");
        for (Product p : allProducts) {
            System.out.println("Κωδικός: " + p.getCode() + ", Όνομα: " + p.getName() + ", Απόθεμα: " + p.getQuantity());
        }
        System.out.println("--------------------------");

        Product selected = null;
        int code;
        do {
            System.out.print("Επιλέξτε κωδικό προϊόντος για αναπλήρωση: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Μη έγκυρη είσοδος. Εισάγετε αριθμό κωδικού: ");
                scanner.next();
            }
            code = scanner.nextInt();
            scanner.nextLine();
            selected = inventory.getProductByCode(code);
            if (selected == null)
                System.out.println("Κωδικός προϊόντος '" + code + "' δεν βρέθηκε. Προσπαθήστε ξανά.");
        } while (selected == null);

        int currentQuantity = selected.getQuantity();
        System.out.println("Τρέχον διαθέσιμο απόθεμα: " + currentQuantity);

        int qty;
        do {
            System.out.print("Ποσότητα αναπλήρωσης (1-" + MaxRestock + "): ");
            while (!scanner.hasNextInt()) {
                System.out.print("Μη έγκυρη είσοδος. Εισάγετε θετικό αριθμό: ");
                scanner.next();
            }
            qty = scanner.nextInt();
            scanner.nextLine();
            if (qty <= 0)
                System.out.println("Αρνητική ή μηδενική ποσότητα απαγορεύεται.");
            else if (qty > MaxRestock)
                System.out.println("Πάνω από το μέγιστο: " + MaxRestock);
        } while (qty <= 0 || qty > MaxRestock);

        String answer;
        do {
            System.out.print("Επιβεβαιώνετε την αναπλήρωση " + qty + " μονάδων για " + selected.getName() + " (y/n)? ");
            answer = scanner.nextLine().trim();
        } while (!answer.equalsIgnoreCase("y") && !answer.equalsIgnoreCase("n"));

        if (answer.equalsIgnoreCase("y")) {
            inventory.addStockToExisting(code, qty);
            System.out.println(">>> Επιτυχία! Προστέθηκαν " + qty + " μονάδες στο προϊόν '" + selected.getName() +
                               "' (Κωδικός: " + selected.getCode() + ").");
            System.out.println("Νέο διαθέσιμο απόθεμα: " + selected.getQuantity());
        } else {
            System.out.println("Η αναπλήρωση ακυρώθηκε.");
        }
        System.out.println("---------------------------------------------------");
    }
}
