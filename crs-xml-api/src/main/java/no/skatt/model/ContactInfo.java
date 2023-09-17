package no.skatt.model;

public class ContactInfo {
    public String name;
    public String phone;
    public String email;
    public String mobile;

    @Override
    public String toString() {
        return "ContactInfo{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", mobile='" + mobile + '\'' +
                '}';
    }
}
