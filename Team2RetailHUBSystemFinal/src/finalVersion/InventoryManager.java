package finalVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections; 

public class InventoryManager {
    // Χρησιμοποιούμε List για την αποθήκευση προϊόντων
    private List<Product> products;

    public InventoryManager() {
        this.products = new ArrayList<>();
    }

    public boolean addNewProduct(int code, String name, double price, int quantity) {
        if (getProductByCode(code) != null) { // Ελέγχουμε αν υπάρχει ήδη με αναζήτηση στη λίστα
            System.out.println("Προϊόν με κωδικό " + code + " υπάρχει ήδη. Δεν είναι δυνατή η προσθήκη.");
            return false;
        }
        try {
            Product newProduct = new Product(code, name, price, quantity);
            products.add(newProduct);
            System.out.println("Προϊόν προστέθηκε: " + name + " (Κωδ: " + code + ", Απόθεμα: " + quantity + ").");
            return true;
        } catch (IllegalArgumentException e) {
            System.err.println("Σφάλμα κατά την προσθήκη προϊόντος: " + e.getMessage());
            return false;
        }
    }

    public Product getProductByCode(int code) {
        for (Product p : products) {
            if (p.getCode() == code) {
                return p;
            }
        }
        return null;
    }

    public List<Product> searchByNameOrCode(String query) {
        List<Product> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        String lowerCaseQuery = query.toLowerCase();

        for (Product p : products) {
            // Ελέγχουμε αν ταιριάζει με τον κωδικό (ακριβής αριθμητική σύγκριση)
            if (String.valueOf(p.getCode()).equals(query)) { // Χρησιμοποιούμε equals για ακριβή σύγκριση κωδικού
                results.add(p);
            } else if (p.getName().toLowerCase().contains(lowerCaseQuery)) {
                // Ελέγχουμε αν το όνομα περιέχει το query (case-insensitive)
                results.add(p);
            }
        }
        return results;
    }

    public void displayInventory() {
        if (products.isEmpty()) {
            System.out.println("Δεν υπάρχουν προϊόντα στο απόθεμα.");
        } else {
            System.out.println("--- Τρέχον Απόθεμα ---");
            for (Product p : products) {
                p.displayInfo();
            }
            System.out.println("---------------------");
        }
    }

    public boolean increaseProductStock(int code, int quantityToAdd) {
        Product product = getProductByCode(code);
        if (product != null && quantityToAdd > 0) {
            product.setQuantity(product.getQuantity() + quantityToAdd); // Αυξάνουμε το συνολικό stock
            System.out.println("Απόθεμα αυξήθηκε για " + product.getName() + " κατά " + quantityToAdd + ". Νέο συνολικό απόθεμα: " + product.getQuantity());
            return true;
        }
        System.out.println("Αποτυχία αύξησης αποθέματος για κωδικό προϊόντος " + code + ". Το προϊόν δεν βρέθηκε ή η ποσότητα είναι άκυρη.");
        return false;
    }

    public boolean decreaseProductStock(int code, int quantityToSubtract) {
        Product product = getProductByCode(code);
        if (product != null && quantityToSubtract > 0) {
            if (product.getQuantity() >= quantityToSubtract) {
                product.setQuantity(product.getQuantity() - quantityToSubtract); // Μειώνουμε το συνολικό stock
                System.out.println("Απόθεμα μειώθηκε για " + product.getName() + " κατά " + quantityToSubtract + ". Νέο συνολικό απόθεμα: " + product.getQuantity());
                return true;
            } else {
                System.out.println("Αποτυχία μείωσης αποθέματος για " + product.getName() + ". Ανεπαρκές απόθεμα. Διαθέσιμο: " + product.getQuantity() + ", Ζητούμενο: " + quantityToSubtract);
            }
        }
        System.out.println("Αποτυχία μείωσης αποθέματος για κωδικό προϊόντος " + code + ". Το προϊόν δεν βρέθηκε ή η ποσότητα είναι άκυρη.");
        return false;
    }

    public boolean productExists(int productCode) {
        return getProductByCode(productCode) != null; // Απλά ελέγχουμε αν η μέθοδος βρήκε το προϊόν
    }

    public List<Product> getAllProducts() {
        return Collections.unmodifiableList(new ArrayList<>(products)); // Επιστρέφουμε ένα αντίγραφο
    }

    public int getProductAvailableQuantity(int productCode) {
        Product p = getProductByCode(productCode);
        if (p != null) {
            return p.getAvailableQuantity(); // Χρησιμοποιούμε τη μέθοδο της Product
        }
        return 0;
    }

    public boolean deleteProduct(int productCode) {
        // Ελέγχουμε αν υπάρχει το προϊόν και το αφαιρούμε από τη λίστα
        Product productToRemove = null;
        for (Product p : products) {
            if (p.getCode() == productCode) {
                productToRemove = p;
                break;
            }
        }

        if (productToRemove != null) {
            products.remove(productToRemove);
            System.out.println("Προϊόν με κωδικό " + productCode + " διαγράφηκε.");
            return true;
        }
        System.out.println("Προϊόν με κωδικό " + productCode + " δεν βρέθηκε για διαγραφή.");
        return false;
    }

	public void addStockToExisting(int code, int qty) {
		
	}
}