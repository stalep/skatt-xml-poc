package no.skatt.model;

public class DeliveryOwner {
    public String orgNumber;
    public String orgName;
    public ContactInfo contactInfo;

    @Override
    public String toString() {
        return "DeliveryOwner{" +
                "orgNumber='" + orgNumber + '\'' +
                ", orgName='" + orgName + '\'' +
                ", contactInfo=" + contactInfo +
                '}';
    }
}
