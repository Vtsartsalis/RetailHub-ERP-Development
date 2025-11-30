package finalVersion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger; // Χρήση AtomicInteger για thread-safe IDs

public class Order {

    private static final AtomicInteger idCounter = new AtomicInteger(1000);
    private final int id;
    private final Customer customer;
    private final List<OrderItem> items; // Λίστα με OrderItem objects
    private OrderStatus status;          // Κατάσταση της παραγγελίας (PENDING, FULFILLED, CANCELED)
    private final Date timestamp;        // Χρόνος δημιουργίας παραγγελίας

    public Order(Customer customer, List<OrderItem> items) {
        if (customer == null) {
            throw new IllegalArgumentException("Η παραγγελία πρέπει να έχει πελάτη.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Η παραγγελία πρέπει να έχει τουλάχιστον ένα είδος.");
        }

        this.id = idCounter.incrementAndGet(); // Αναθέτουμε έναν μοναδικό ID
        this.customer = customer;
        // Δημιουργούμε ένα νέο ArrayList για να μην έχουμε απευθείας αναφορά στην εξωτερική λίστα
        this.items = new ArrayList<>(items); // Defensive copy
        this.timestamp = new Date(); // Τρέχουσα ημερομηνία/ώρα
        this.status = OrderStatus.PENDING;
    }

    // --- Getters ---
    public int getId() { return id; }
    public Customer getCustomer() { return customer; }
    public OrderStatus getStatus() { return status; }
    public Date getTimestamp() { return new Date(timestamp.getTime()); } // Επιστρέφουμε αντίγραφο για ασφάλεια

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items); 
    }

    // --- Setters (για την κατάσταση) ---

    public void setStatus(OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Η κατάσταση της παραγγελίας δεν μπορεί να είναι null.");
        }
        this.status = status;
    }

    // --- Βοηθητικές Μέθοδοι για την Κατάσταση και τους Υπολογισμούς ---

    public boolean hasBackorderedItems() {
        return items.stream().anyMatch(item -> item.getBackorderedQuantity() > 0);
    }

    public boolean isFullyReserved() {
        return items.stream().allMatch(item -> item.getReservedQuantity() == item.getRequestedQty());
    }

    public double getTotalValue() {
        return items.stream()
                    .mapToDouble(item -> item.getRequestedQty() * item.getPriceAtSale())
                    .sum();
    }

    public boolean isCancelled() {
        return status == OrderStatus.CANCELED;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(
            String.format("Order #%d (Πελάτης: %s) [Κατάσταση: %s] | Ημερομηνία: %s%n",
                          id, customer.getName(), status, timestamp)
        );
        sb.append("Λεπτομέρειες Ειδών:%n");
        items.forEach(it ->
            sb.append(String.format("  - %s (Κωδ: %d): Ζητήθηκαν: %d, Δεσμεύτηκαν: %d, Εκκρεμούν: %d%n",
                                     it.getProduct().getName(),
                                     it.getProduct().getCode(),
                                     it.getRequestedQty(),
                                     it.getReservedQuantity(),
                                     it.getBackorderedQuantity()))
        );
        sb.append(String.format("Συνολική Αξία Παραγγελίας: %.2f€%n", getTotalValue()));
        return sb.toString();
    }
}
