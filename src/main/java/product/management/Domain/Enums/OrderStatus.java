package product.management.Domain.Enums;

public enum OrderStatus {
    CREATED,
    PAID,
    PREPARING,
    SHIPPED,
    DELIVERED;

    public static boolean isValid(String status){
        try{
            valueOf(status);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
