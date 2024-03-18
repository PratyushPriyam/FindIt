class SellUploadClass(
    val sellerName: String,
    val productName: String,
    val location: String,
    val price: Int,
    val sellerId: String,
    val isBought: String,
    val boughtBy: String,
    val phno: String
) {
    // Primary constructor with arguments

    // Add an empty secondary constructor for Firebase
    constructor() : this("", "", "", 0, "", "no", "", "") {
        // Empty implementation for the no-argument constructor
    }
}
