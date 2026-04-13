package product.management.Domain.Enums;

public enum ShipmentStatus {
    CREATED,
    IN_TRANSIT,
    DELIVERED,
    PENDING_PICKUP;

    public static boolean isValid(String status){
        try{
            valueOf(status);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
