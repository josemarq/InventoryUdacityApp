package josemarq.inventoryudacityapp.data;

public class Items {

    private final String productName;
    private final String productPrice;
    private final int productQuantity;
    private final String supplierName;
    private final String supplierEmail;
    private final String image;

    public Items(String productName, String price, int quantity, String supplierName, String supplierEmail, String image) {
        this.productName = productName;
        this.productPrice = price;
        this.productQuantity = quantity;
        this.supplierName = supplierName;
        this.supplierEmail = supplierEmail;
        this.image = image;
    }

    public String getProductName() {
        return productName;
    }

    public String getPrice() {
        return productPrice;
    }

    public int getQuantity() {
        return productQuantity;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getSupplierEmail() {
        return supplierEmail;
    }

    public String getImage() {
        return image;
    }

    @Override
    public String toString() {
        return "Items{" +
                "productName='" + productName + '\'' +
                ", price='" + productPrice + '\'' +
                ", quantity=" + productQuantity +
                ", supplierName='" + supplierName + '\'' +
                ", supplierEmail='" + supplierEmail + '\'' +
                '}';
    }

}
