package finalVersion;

public class Product {
    private int code;
    private String name;
    private double price;
    private int quantity;         // Συνολικό φυσικό απόθεμα που υπάρχει
    private int reservedQuantity; // Ποσότητα που έχει δεσμευτεί για παραγγελίες

    public Product(int code, String name, double price, int quantity) {
        if (code <= 0) throw new IllegalArgumentException("Ο κωδικός πρέπει να είναι θετικός!");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Το όνομα δεν μπορεί να είναι κενό!");
        if (price < 0) throw new IllegalArgumentException("Η τιμή δεν μπορεί να είναι αρνητική!");
        if (quantity < 0) throw new IllegalArgumentException("Η ποσότητα δεν μπορεί να είναι αρνητική!");
        this.code = code;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.reservedQuantity = 0; 
    }

    // --- Getters 
    public int getCode() { return code; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; } // Επιστρέφει το συνολικό απόθεμα
    public int getReservedQuantity() { return reservedQuantity; }

    // Η μέθοδος που υπολογίζει και επιστρέφει το πραγματικά διαθέσιμο απόθεμα
    public int getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    // --- Setters
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Το όνομα δεν μπορεί να είναι κενό!");
        this.name = name;
    }

    public void setPrice(double price) {
        if (price < 0) throw new IllegalArgumentException("Η τιμή δεν μπορεί να είναι αρνητική!");
        this.price = price;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) throw new IllegalArgumentException("Η ποσότητα δεν μπορεί να είναι αρνητική!");
        this.quantity = quantity;
    }

    public void setReservedQuantity(int reservedQuantity) {
        if (reservedQuantity < 0 || reservedQuantity > quantity) { 
            throw new IllegalArgumentException("Η δεσμευμένη ποσότητα δεν είναι έγκυρη!");
        }
        this.reservedQuantity = reservedQuantity;
    }


    // 1.Μέθοδος για προσθήκη νέου stock (Restock)
    public void addStock(int qty) {
        if (qty > 0) {
            this.quantity += qty; // Αυξάνουμε το συνολικό φυσικό απόθεμα
            System.out.println("Product " + this.name + " added " + qty + " stock. Total: " + this.quantity + ", Available: " + getAvailableQuantity());
        }
    }

    // 2.Μέθοδος για δέσμευση αποθέματος (όταν μπαίνει μια παραγγελία)
    // Αυξάνει το reservedQuantity και μειώνει το availableQuantity
    public int reserve(int requestedQty) {
        if (requestedQty < 0) {
            throw new IllegalArgumentException("Η ζητούμενη ποσότητα για δέσμευση δεν μπορεί να είναι αρνητική!");
        }
        int available = getAvailableQuantity(); // Παίρνουμε το πόσο είναι διαθέσιμο
        int toReserve = Math.min(requestedQty, available); // Δεσμεύουμε μόνο όσα είναι διαθέσιμα

        if (toReserve > 0) {
            this.reservedQuantity += toReserve; // Αυξάνουμε το δεσμευμένο
            // Το 'quantity' (συνολικό) δεν μειώνεται εδώ, καθώς το προϊόν δεν έχει φύγει ακόμα από την αποθήκη.
            // Το 'availableQuantity' μειώνεται αυτόματα μέσω του getter (quantity - reservedQuantity).
        }
        System.out.println("Product " + this.name + " reserved " + toReserve + ". New Total: " + this.quantity + ", New Reserved: " + this.reservedQuantity + ", New Available: " + getAvailableQuantity());
        return toReserve;
    }

    // 3.Μέθοδος για την ολοκλήρωση της πώλησης (όταν παραδίδεται η παραγγελία)
    public void fulfillAndRelease(int qtyToFulfill) {
        if (qtyToFulfill <= 0) {
            throw new IllegalArgumentException("Η ποσότητα προς εκπλήρωση πρέπει να είναι θετική.");
        }
   
        int actualFulfilled = Math.min(qtyToFulfill, this.reservedQuantity);

        this.reservedQuantity -= actualFulfilled; // Μειώνει τη δεσμευμένη ποσότητα
        this.quantity -= actualFulfilled;       // Μειώνει το συνολικό απόθεμα

        this.reservedQuantity = Math.max(0, this.reservedQuantity);
        this.quantity = Math.max(0, this.quantity);

        System.out.println("Product " + this.name + " fulfilled " + actualFulfilled + ". New Total: " + this.quantity + ", New Reserved: " + this.reservedQuantity + ", New Available: " + getAvailableQuantity());
    }


    public void unreserve(int qtyToUnreserve) {
        if (qtyToUnreserve < 0) {
            throw new IllegalArgumentException("Η ποσότητα προς αποδέσμευση δεν μπορεί να είναι αρνητική!");
        }
        int actualUnreserved = Math.min(qtyToUnreserve, reservedQuantity);
        this.reservedQuantity -= actualUnreserved;

        System.out.println("Product " + this.name + " unreserved " + actualUnreserved + ". New Reserved: " + this.reservedQuantity + ", New Available: " + getAvailableQuantity());
    }


    @Override
    public String toString() {
        return "Προϊόν: " + name + " (Κωδ: " + code + "), Τιμή: " + String.format("%.2f€", price) +
               ", Συνολικό Απόθεμα: " + quantity + ", Δεσμευμένο: " + reservedQuantity + ", Διαθέσιμο: " + getAvailableQuantity();
    }

    public void displayInfo() {
        System.out.println(this.toString());
    }
}
