package finalVersion;

import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

class CustomerManager {
    private List<Customer> customers = new ArrayList<>();
    private int nextIdCounter = 1;

    public CustomerManager() {
    }

    public boolean addCustomer(Customer c) {
        if (c == null) {
            return false;
        }
        // Ελέγχουμε αν υπάρχει ήδη πελάτης με αυτό το ID
        for (Customer existingCustomer : customers) {
            if (existingCustomer.getId() == c.getId()) {
                return false; // Ο πελάτης με αυτό το ID υπάρχει ήδη
            }
        }
        customers.add(c);
        // Ενημερώνουμε τον nextIdCounter αν το ID του νέου πελάτη είναι μεγαλύτερο
        if (c.getId() >= nextIdCounter) {
            nextIdCounter = c.getId() + 1;
        }
        return true; // Προστέθηκε επιτυχώς
    }

    // Αυτή η μέθοδος είναι η πιο κοινή για προσθήκη νέου πελάτη,
    // όπου ο manager αναθέτει το ID.
    public Customer addCustomer(String name, String email, String phone, String address, int age) {
        int newId = nextIdCounter;
        boolean idExists;
        do {
            idExists = false;
            for (Customer cust : customers) {
                if (cust.getId() == newId) {
                    idExists = true;
                    newId++; // Δοκιμάζουμε το επόμενο ID
                    break;
                }
            }
        } while (idExists);
        
        nextIdCounter = newId + 1; // Ενημερώνουμε τον counter για την επόμενη κλήση
        Customer c = new Customer(newId, name, email, phone, address, age);
        customers.add(c);
        return c; // Επιστρέφουμε τον νέο πελάτη
    }

    public Customer findById(int id) {
        for (Customer c : customers) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null; // Δεν βρέθηκε
    }

    public Customer findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return null;
        for (Customer c : customers) {
            String custEmail = c.getEmail();
            if (custEmail != null && custEmail.equalsIgnoreCase(email)) {
                return c;
            }
        }
        return null;
    }

    public boolean updateCustomer(int id, String newName, String newEmail, String newPhone, String newAddress, int newAge) {
        Customer c = findById(id); // Χρησιμοποιούμε τη findById
        if (c == null) {
            return false; // Ο πελάτης δεν βρέθηκε
        }

        c.setName(newName);
        c.setEmail(newEmail);
        c.setPhone(newPhone);
        c.setAddress(newAddress);
        c.setAge(newAge);
        return true; // Ενημερώθηκε επιτυχώς
    }

    public boolean deleteCustomer(int id) {
        int indexToRemove = -1;
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getId() == id) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove != -1) {
            customers.remove(indexToRemove);
            return true;
        }
        return false;
    }

    public List<Customer> listAll() {
        return new ArrayList<>(customers);
    }

    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers); 
    }

    public Customer getCustomerById(int customerId) {
        return findById(customerId); 
    }
}
