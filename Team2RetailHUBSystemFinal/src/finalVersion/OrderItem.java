package finalVersion;

import java.io.Serializable;

public class OrderItem implements Serializable {
    private Product product;
    private int requestedQty;     // Η αρχικά ζητούμενη ποσότητα από τον χρήστη
    private int reservedQty;      // Η ποσότητα που έχει δεσμευτεί επιτυχώς από το απόθεμα
    private int backorderedQty;   // Η ποσότητα που εκκρεμεί (backorder) και αναμένει απόθεμα
    private double priceAtSale;   // Η τιμή του προϊόντος τη στιγμή της προσθήκης στην παραγγελία

    public OrderItem(Product product, int requestedQty) {
        if (product == null) {
            throw new IllegalArgumentException("Το προϊόν δεν μπορεί να είναι null.");
        }
        if (requestedQty <= 0) {
            throw new IllegalArgumentException("Η ζητούμενη ποσότητα πρέπει να είναι θετική.");
        }
        this.product = product;
        this.requestedQty = requestedQty;
        this.reservedQty = 0;
        this.backorderedQty = 0;
        this.priceAtSale = product.getPrice(); // Αποθηκεύουμε την τρέχουσα τιμή του προϊόντος
    }

    // --- Getters ---
    public Product getProduct() {
        return product;
    }

    public int getRequestedQty() {
        return requestedQty;
    }

    public int getReservedQuantity() {
        return reservedQty;
    }

    public int getBackorderedQuantity() {
        return backorderedQty;
    }

    public int getActualQuantity() {
        return reservedQty;
    }

    public double getPriceAtSale() {
        return priceAtSale;
    }

    // Το pending status εξαρτάται από το backorderedQty
    public boolean isPending() {
        return backorderedQty > 0;
    }

    public int getRemainingToCover() {
        return requestedQty - (reservedQty + backorderedQty);
    }

    // --- Setters
    public void setRequestedQty(int requestedQty) {
        if (requestedQty < 0) throw new IllegalArgumentException("Η ζητούμενη ποσότητα δεν μπορεί να είναι αρνητική.");
        this.requestedQty = requestedQty;
    }
    
    public void setReservedQuantity(int reservedQty) {
        if (reservedQty < 0 || reservedQty > this.requestedQty) {
            throw new IllegalArgumentException("Η δεσμευμένη ποσότητα δεν είναι έγκυρη: " + reservedQty + ". Ζητήθηκε: " + this.requestedQty);
        }
        this.reservedQty = reservedQty;
        // Ενημερώνουμε αυτόματα το backorderedQty
        this.backorderedQty = this.requestedQty - this.reservedQty;
    }

    public void setBackorderedQuantity(int backorderedQty) {
        if (backorderedQty < 0 || backorderedQty > this.requestedQty) {
            throw new IllegalArgumentException("Η ποσότητα backorder δεν είναι έγκυρη: " + backorderedQty + ". Ζητήθηκε: " + this.requestedQty);
        }
        this.backorderedQty = backorderedQty;
        // Ενημερώνουμε αυτόματα το reservedQty

        this.reservedQty = this.requestedQty - this.backorderedQty;
    }

    public void addQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Η επιπλέον ποσότητα πρέπει να είναι θετική.");
        }
        this.requestedQty += quantity;
        // reservedQty και backorderedQty θα πρέπει να ενημερωθούν
        // όταν η παραγγελία επεξεργαστεί ξανά ή αλλάξει κατάσταση.
    }

    public int allocate(int quantityToAllocate) {
        if (quantityToAllocate < 0) {
            throw new IllegalArgumentException("Η ποσότητα για εκχώρηση δεν μπορεί να είναι αρνητική.");
        }

        int actualAllocation = Math.min(quantityToAllocate, this.backorderedQty);

        if (actualAllocation > 0) {
            this.reservedQty += actualAllocation;
            this.backorderedQty -= actualAllocation;
        }
        return actualAllocation;
    }

    // Το pending status εξαρτάται από το backorderedQty
    public boolean isFullyReserved() {
        return this.requestedQty == this.reservedQty;
    }

    public double getTotalValue() {
        // Η συνολική αξία του συγκεκριμένου είδους στην παραγγελία
        return requestedQty * priceAtSale;
    }

    public void setPriceAtSale(double priceAtSale) {
        if (priceAtSale < 0) throw new IllegalArgumentException("Η τιμή πώλησης δεν μπορεί να είναι αρνητική.");
        this.priceAtSale = priceAtSale;
    }
}