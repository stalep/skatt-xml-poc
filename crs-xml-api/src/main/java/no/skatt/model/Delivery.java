package no.skatt.model;

public class Delivery {
    public String sourceSystem;
    public String fiscalYear;
    public String taskDeliveryReference;
    public String deliveryType;
    public String presentingName;
    public DeliveryOwner deliveryOwner;

    @Override
    public String toString() {
        return "Delivery{" +
                "sourceSystem='" + sourceSystem + '\'' +
                ", fiscalYear='" + fiscalYear + '\'' +
                ", taskDeliveryReference='" + taskDeliveryReference + '\'' +
                ", deliveryType='" + deliveryType + '\'' +
                ", presentingName='" + presentingName + '\'' +
                "\n deliveryOwner=" + deliveryOwner +
                '}';
    }
}
