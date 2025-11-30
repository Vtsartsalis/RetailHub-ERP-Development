package finalVersion;

class Customer {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private int age;

    // Πρώτος κατασκευαστής: Για βασικά στοιχεία (ID και Όνομα)
    public Customer(int id, String name) {
        if (id <= 0) throw new IllegalArgumentException("Το Customer ID πρέπει να είναι θετικός ακέραιος.");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Το όνομα του πελάτη δεν μπορεί να είναι κενό.");
        this.id = id;
        this.name = name;
        // Αρχικοποίηση των υπόλοιπων πεδίων σε προεπιλεγμένες τιμές
        this.email = null;
        this.phone = null;
        this.address = null;
        this.age = 0; 
    }

    // Δεύτερος κατασκευαστής: Για όλα τα στοιχεία του πελάτη 
    public Customer(int id, String name, String email, String phone, String address, int age) {
        if (id <= 0) throw new IllegalArgumentException("Το Customer ID πρέπει να είναι θετικός ακέραιος.");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Το όνομα του πελάτη δεν μπορεί να είναι κενό.");
        if (age < 0) throw new IllegalArgumentException("Η ηλικία δεν μπορεί να είναι αρνητική."); 
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.age = age;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public int getAge() { return age; }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) this.name = name;
    }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setAge(int age) { if (age >= 0) this.age = age; }

    public String display() {
        return "ID: " + id + " | Όνομα: " + name + " | Email: " + (email != null ? email : "N/A") +
               " | Τηλέφωνο: " + (phone != null ? phone : "N/A") +
               " | Διεύθυνση: " + (address != null ? address : "N/A") + " | Ηλικία: " + age;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Όνομα: " + name + ", Email: " + (email != null ? email : "N/A") +
               ", Τηλ: " + (phone != null ? phone : "N/A") + ", Διεύθυνση: " + (address != null ? address : "N/A") +
               ", Ηλικία: " + age;
    }
}