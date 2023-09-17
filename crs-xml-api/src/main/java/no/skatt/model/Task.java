package no.skatt.model;

public class Task {
    public Delivery delivery;
    public String accountNumber;
    public String taxAccountAptitude;
    public String deposit;
    public String earnedInterest;
    public String accruedInterest;
    public String accountType;
    public TaskOwner owner;

    public Task() {
        owner = new TaskOwner();
    }

    @Override
    public String toString() {
        return "Task{" +
                "delivery=" + delivery +
                "\n accountNumber='" + accountNumber + '\'' +
                ", taxAccountAptitude='" + taxAccountAptitude + '\'' +
                ", deposit='" + deposit + '\'' +
                ", earnedInterest=" + earnedInterest +
                ", accruedInterest='" + accruedInterest + '\'' +
                ", accountType='" + accountType + '\'' +
                "\n owner='" + owner + '\'' +
                '}';
    }
}
