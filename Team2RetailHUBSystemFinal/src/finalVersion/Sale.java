package finalVersion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Sale implements Serializable {
    private static final AtomicInteger nextSaleId = new AtomicInteger(1);

    private final int saleId;
    private final int orderId; // Το ID της παραγγελίας από την οποία προήλθε η πώληση
    private final Customer customer;
    private final double totalSaleValue;
    private final Date saleDate;
    private final List<OrderItem> soldItems; // Τα OrderItems που πουλήθηκαν

    public Sale(int orderId, Customer customer, double totalSaleValue, Date saleDate, List<OrderItem> soldItems) {
        if (customer == null || soldItems == null || soldItems.isEmpty()) {
            throw new IllegalArgumentException("Δεν μπορούν να δημιουργηθούν δεδομένα πώλησης χωρίς πελάτη ή είδη.");
        }
        this.saleId = nextSaleId.getAndIncrement();
        this.orderId = orderId;
        this.customer = customer;
        this.totalSaleValue = totalSaleValue;
        this.saleDate = (Date) saleDate.clone(); // Defensive copy
        this.soldItems = new ArrayList<>(soldItems); // Defensive copy
    }

    // --- Getters ---
    public int getSaleId() {
        return saleId;
    }

    public int getOrderId() {
        return orderId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public double getTotalSaleValue() {
        return totalSaleValue;
    }

    public Date getSaleDate() {
        return (Date) saleDate.clone(); // Defensive copy
    }

    public List<OrderItem> getSoldItems() {
        return Collections.unmodifiableList(soldItems);
    }

    @Override
    public String toString() {
        return "Sale ID: " + saleId + ", Order ID: " + orderId + ", Customer: " + customer.getName() +
               ", Value: " + String.format("%.2f€", totalSaleValue) + ", Date: " + saleDate;
    }
}
