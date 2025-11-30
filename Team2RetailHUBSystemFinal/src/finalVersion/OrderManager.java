package finalVersion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Date; 


public class OrderManager {
    private List<Order> orders = new ArrayList<>();
    private InventoryManager inventoryManager;

    public OrderManager(InventoryManager inventoryManager) {
        if (inventoryManager == null) {
            throw new IllegalArgumentException("Ο InventoryManager δεν μπορεί να είναι null.");
        }
        this.inventoryManager = inventoryManager;
    }

    public void addOrder(Order order) {
        if (order != null && !orders.contains(order)) { // Αποφυγή διπλοτύπων
            orders.add(order);
        }
    }

    public Order findById(int id) {
        for (Order order : orders) {
            if (order.getId() == id) {
                return order;
            }
        }
        return null;
    }

    public List<Order> getAllOrders() { 
        return Collections.unmodifiableList(new ArrayList<>(orders));
    }

    public List<Order> getOrdersForCustomer(Customer customer) {
        if (customer == null) {
            return new ArrayList<>();
        }
        List<Order> customerOrders = new ArrayList<>();
        for (Order order : orders) {
            // Ελέγχουμε αν ο πελάτης της παραγγελίας είναι ο ίδιος
            if (order.getCustomer() != null && order.getCustomer().equals(customer)) {
                customerOrders.add(order);
            }
        }
        return customerOrders;
    }

    public Order createOrder(Customer customer, List<OrderItem> itemsToOrder, boolean allowBackorder) {
        if (customer == null || itemsToOrder == null || itemsToOrder.isEmpty()) {
            System.err.println("Σφάλμα: Δεν μπορεί να δημιουργηθεί η παραγγελία. Ελλιπή στοιχεία (πελάτης ή είδη).");
            return null;
        }

        // Δημιουργούμε την παραγγελία με τα αρχικά OrderItems
        Order newOrder = new Order(customer, itemsToOrder);

        boolean allItemsFullyReserved = true; // Για να ελέγξουμε την κατάσταση της παραγγελίας

        // Περνάμε από κάθε OrderItem για να δεσμεύσουμε απόθεμα
        for (OrderItem item : newOrder.getItems()) { // Χρησιμοποιούμε τα OrderItems της νέας παραγγελίας
            Product product = item.getProduct();
            if (product == null) {
                System.err.println("Προσοχή: Το προϊόν για το OrderItem δεν βρέθηκε.");
                continue;
            }

            int availableForReservation = product.getAvailableQuantity();
            int requestedQty = item.getRequestedQty();

            int reservedNow = 0;
            int backorderedNow = 0;

            if (availableForReservation >= requestedQty) {
                // Υπάρχει αρκετό απόθεμα για πλήρη δέσμευση
                reservedNow = product.reserve(requestedQty); // Δεσμεύουμε την ποσότητα
                item.setReservedQuantity(reservedNow);
                item.setBackorderedQuantity(0);
                System.out.println("Προϊόν " + product.getName() + ": Δεσμεύτηκαν πλήρως " + reservedNow + " τεμάχια.");
            } else {
                // Δεν υπάρχει αρκετό απόθεμα
                allItemsFullyReserved = false; // Τουλάχιστον ένα είδος δεν καλύφθηκε πλήρως

                if (availableForReservation > 0) {
                    reservedNow = product.reserve(availableForReservation); // Δεσμεύουμε ό,τι υπάρχει
                    item.setReservedQuantity(reservedNow);
                    System.out.println("Προϊόν " + product.getName() + ": Δεσμεύτηκαν " + reservedNow + " τεμάχια.");
                } else {
                    item.setReservedQuantity(0); // Τίποτα δεν δεσμεύτηκε
                    System.out.println("Προϊόν " + product.getName() + ": Δεν υπάρχει διαθέσιμο απόθεμα για δέσμευση.");
                }

                if (allowBackorder) {
                    backorderedNow = requestedQty - reservedNow;
                    item.setBackorderedQuantity(backorderedNow);
                    System.out.println("Προσοχή: Για το προϊόν " + product.getName() + " δημιουργήθηκε backorder " + backorderedNow + " τεμαχίων.");
                } else {
                    item.setBackorderedQuantity(0); // Δεν επιτρέπεται backorder
                    System.out.println("Προσοχή: Για το προϊόν " + product.getName() + " διατέθηκαν μόνο " + reservedNow + " τεμάχια (backorder δεν επιτράπηκε).");
                }
            }
        }

        // Καθορισμός της τελικής κατάστασης της παραγγελίας
        if (allItemsFullyReserved) {
            newOrder.setStatus(OrderStatus.READY_TO_BE_DELIVERED);
            System.out.println("Νέα παραγγελία " + newOrder.getId() + " δημιουργήθηκε ως: READY_TO_BE_DELIVERED.");
        } else if (newOrder.hasBackorderedItems()) { // Αν έχει backorders, αλλά δεν έχει καλυφθεί πλήρως
            newOrder.setStatus(OrderStatus.PARTIALLY_FULFILLED);
            System.out.println("Νέα παραγγελία " + newOrder.getId() + " δημιουργήθηκε ως: PARTIALLY_FULFILLED."); 
        } else {
            newOrder.setStatus(OrderStatus.PENDING);
            System.out.println("Νέα παραγγελία " + newOrder.getId() + " δημιουργήθηκε ως: PENDING.");
        }

        orders.add(newOrder);
        System.out.println("Η παραγγελία " + newOrder.getId() + " δημιουργήθηκε επιτυχώς για τον πελάτη " + customer.getName() + ". Κατάσταση: " + newOrder.getStatus());
        return newOrder;
    }

    public boolean fulfillOrder(Order order) {
        if (order == null || !orders.contains(order)) {
            System.err.println("Σφάλμα: Η παραγγελία δεν βρέθηκε.");
            return false;
        }

        // Αν είναι ήδη "FULFILLED" ή ακυρωμένη, δεν την ξανα επεξεργαζόμαστε
        if (order.getStatus() == OrderStatus.FULFILLED || order.getStatus() == OrderStatus.CANCELED) {
            System.err.println("Η παραγγελία " + order.getId() + " έχει ήδη παραδοθεί ή ακυρωθεί.");
            return false;
        }
        // Αν είναι ήδη "Ready to be Delivered", δεν χρειάζεται να την "εκπληρώσουμε" ξανά
        if (order.getStatus() == OrderStatus.READY_TO_BE_DELIVERED) {
             System.out.println("Η παραγγελία " + order.getId() + " είναι ήδη Έτοιμη για Παράδοση.");
        }


        boolean anyReservedInThisAttempt = false; 

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product == null) continue;

            int alreadyReserved = item.getReservedQuantity();
            int requested = item.getRequestedQty();
            int remainingToReserve = requested - alreadyReserved;

            if (remainingToReserve > 0) {
                int available = product.getAvailableQuantity();
                int reserveNow = Math.min(available, remainingToReserve);
                if (reserveNow > 0) {
                    int actuallyReserved = product.reserve(reserveNow); // Μειώνει το διαθέσιμο απόθεμα και να αυξάνει το δεσμευμένο
                    item.setReservedQuantity(alreadyReserved + actuallyReserved);
                    item.setBackorderedQuantity(requested - item.getReservedQuantity());
                    System.out.println("Προϊόν " + product.getName() + ": επιπλέον δεσμεύτηκαν " + actuallyReserved + " τεμάχια. Συνολικά δεσμευμένα: " + item.getReservedQuantity());
                    anyReservedInThisAttempt = true; 
                }
            }
        }

    
        if (order.getItems().stream().allMatch(i -> i.getReservedQuantity() == i.getRequestedQty())) {
            order.setStatus(OrderStatus.READY_TO_BE_DELIVERED);
            System.out.println("DEBUG: Η παραγγελία " + order.getId() + " είναι τώρα: READY_TO_BE_DELIVERED.");
        } else if (anyReservedInThisAttempt || order.getStatus() == OrderStatus.PARTIALLY_FULFILLED) {
            order.setStatus(OrderStatus.PARTIALLY_FULFILLED);
            System.out.println("DEBUG: Η παραγγελία " + order.getId() + " είναι τώρα: PARTIALLY_FULFILLED.");
        } else {
            order.setStatus(OrderStatus.PENDING);
            System.err.println("DEBUG: Η παραγγελία " + order.getId() + " παραμένει: PENDING (δεν υπήρχε διαθέσιμο απόθεμα).");
            return false; 
        }

        System.out.println("Η παραγγελία " + order.getId() + " ολοκληρώθηκε με κατάσταση: " + order.getStatus());
        return true; // Επιστρέφουμε true αν η κατάσταση ενημερώθηκε (ακόμα και σε PARTIALLY)
    }

    public boolean cancelOrder(Order order) {
        if (order == null || !orders.contains(order)) {
            System.err.println("Σφάλμα: Η παραγγελία δεν βρέθηκε.");
            return false;
        }
        if (order.getStatus() == OrderStatus.FULFILLED || order.getStatus() == OrderStatus.CANCELED) {
            System.err.println("Η παραγγελία " + order.getId() + " έχει ήδη εκπληρωθεί ή ακυρωθεί. Δεν μπορεί να ακυρωθεί ξανά.");
            return false;
        }

        // Επιστρέφουμε το δεσμευμένο απόθεμα
        for (OrderItem item : order.getItems()) {
            if (item.getReservedQuantity() > 0) {
                item.getProduct().unreserve(item.getReservedQuantity()); // Αποδεσμεύουμε από το προϊόν
                System.out.println("Επιστράφηκαν " + item.getReservedQuantity() + " δεσμευμένα τεμάχια για το προϊόν " + item.getProduct().getName() + " (Παραγγελία " + order.getId() + ").");
            }
            // Μηδενίζουμε τυχόν backordered ποσότητες για το ακυρωμένο OrderItem
            item.setBackorderedQuantity(0); // Μηδενίζει αυτόματα και το reservedQty μέσω setter
        }

        order.setStatus(OrderStatus.CANCELED); 
        System.out.println("Η παραγγελία " + order.getId() + " ακυρώθηκε επιτυχώς.");
        return true;
    }


    public int allocateBackorderedItems(int productCode) {
        int totalAllocated = 0;
        Product product = inventoryManager.getProductByCode(productCode);

        if (product == null) {
            System.err.println("Σφάλμα: Το προϊόν με κωδικό " + productCode + " δεν βρέθηκε για κατανομή backorder.");
            return 0;
        }

        System.out.println("Εκτέλεση κατανομής backorder για προϊόν: " + product.getName() + " (Διαθέσιμο: " + product.getAvailableQuantity() + ").");

  
        List<Order> ordersToProcess = new ArrayList<>(orders); // Δημιουργούμε ένα αντίγραφο για ασφαλή επανάληψη
        for (Order order : ordersToProcess) {
            if (order.getStatus() == OrderStatus.CANCELED ||
                order.getStatus() == OrderStatus.FULFILLED || // FULFILLED = Delivered/Completed
                order.getStatus() == OrderStatus.READY_TO_BE_DELIVERED) { 
                continue; 
            }


            if (product.getAvailableQuantity() <= 0) {
                System.out.println("Δεν υπάρχει άλλο διαθέσιμο απόθεμα για κατανομή backorder για το προϊόν " + product.getName() + ".");
                break; // Δεν υπάρχει άλλο διαθέσιμο απόθεμα
            }

            boolean orderProgressedInThisAllocation = false; 
            for (OrderItem item : order.getItems()) {
                // Βρίσκουμε OrderItems που ανήκουν στο συγκεκριμένο προϊόν και έχουν backorder
                if (item.getProduct().getCode() == productCode && item.getBackorderedQuantity() > 0) {
                    if (product.getAvailableQuantity() <= 0) { 
                        break;
                    }

                    int qtyToAllocate = Math.min(item.getBackorderedQuantity(), product.getAvailableQuantity());

                    // Χρησιμοποιούμε τη μέθοδο allocate του OrderItem για να μετακινήσουμε από backordered σε reserved
                    // και δεσμεύουμε την ποσότητα στο Product (από το συνολικό διαθέσιμο απόθεμα)
                    int actualAllocatedForOrderItem = item.allocate(qtyToAllocate); 
                    product.reserve(actualAllocatedForOrderItem); 

                    totalAllocated += actualAllocatedForOrderItem;
                    orderProgressedInThisAllocation = true;

                    System.out.println("Κατανεμήθηκαν " + actualAllocatedForOrderItem + " τεμάχια για το backordered είδος " + item.getProduct().getName() + " σε παραγγελία " + order.getId() + ".");

                    if (item.isFullyReserved()) {
                        System.out.println("Το είδος " + item.getProduct().getName() + " στην παραγγελία " + order.getId() + " καλύφθηκε πλήρως.");
                    }
                }
            }


            if (order.isFullyReserved()) {
                // Αν όλα τα items της παραγγελίας είναι τώρα πλήρως δεσμευμένα
                order.setStatus(OrderStatus.READY_TO_BE_DELIVERED);
                System.out.println("Η παραγγελία " + order.getId() + " είναι τώρα: READY_TO_BE_DELIVERED.");
            } else if (order.hasBackorderedItems()) {
                // Αν εξακολουθεί να έχει backordered items
                order.setStatus(OrderStatus.PARTIALLY_FULFILLED);
                System.out.println("Η παραγγελία " + order.getId() + " είναι τώρα: PARTIALLY_FULFILLED.");
            } else if (orderProgressedInThisAllocation) {
                order.setStatus(OrderStatus.PENDING); 
            }
        }

        System.out.println("Συνολικά κατανεμήθηκαν " + totalAllocated + " τεμάχια για το προϊόν " + product.getName() + " από backorders.");
        return totalAllocated;
    }
}
