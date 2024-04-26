class SellUploadClass(
    val sellerName: String,
    val productName: String,
    val location: String,
    val sellerId: String,
    var isBought: String,
    var boughtBy: String,
    val phno: String,
    val uid: String,
     var isBoughtByCurrentUser: Boolean = false,
    val imageUrl: String = ""
) {
    // Primary constructor with arguments

    // Add an empty secondary constructor for Firebase
    constructor() : this("", "", "", "", "no", "", "", "", false, "") {
        // Empty implementation for the no-argument constructor
    }
}
