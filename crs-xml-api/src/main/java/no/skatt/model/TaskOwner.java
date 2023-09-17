package no.skatt.model;

public class TaskOwner {
    public String nationalIdentityNumber;
    public String firstName;
    public String middleName;
    public String lastName;
    public String sectorCode;
    public String alternativeIdentifier;
    public String orgNumber;
    public String orgName;
    public InternationalIdentifier internationalIdentifier;
    public TaskOwnerAddress address;

    @Override
    public String toString() {
        return "TaskOwner{" +
                "nationalIdentityNumber='" + nationalIdentityNumber + '\'' +
                ", firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", sectorCode='" + sectorCode + '\'' +
                ", alternativeIdentifier='" + alternativeIdentifier + '\'' +
                ", orgNumber='" + orgNumber + '\'' +
                ", orgName='" + orgName + '\'' +
                ", internationalIdentifier=" + internationalIdentifier +
                ", address=" + address +
                '}';
    }
}
