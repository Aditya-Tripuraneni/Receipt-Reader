package com.receiptradar.domain.pipeline

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Categorizer @Inject constructor() {
    
    private val categoryKeywords = mapOf(
        1L to listOf(
            "burger", "pizza", "sandwich", "fries", "cola", "coke", "pepsi",
            "coffee", "tea", "latte", "cappuccino", "espresso", "mocha",
            "meal", "combo", "chicken", "fish", "rice", "noodles", "pasta",
            "biryani", "curry", "thali", "dosa", "idli", "vada", "samosa",
            "ice cream", "dessert", "cake", "pastry", "brownie", "cookie"
        ),
        2L to listOf(
            "milk", "bread", "egg", "butter", "cheese", "yogurt", "curd",
            "vegetable", "fruit", "apple", "banana", "orange", "tomato",
            "onion", "potato", "rice", "wheat", "flour", "atta", "dal",
            "oil", "sugar", "salt", "spice", "masala", "detergent", "soap",
            "shampoo", "toothpaste", "tissue", "paper towel"
        ),
        3L to listOf(
            "uber", "ola", "cab", "taxi", "auto", "rickshaw",
            "petrol", "diesel", "fuel", "gas", "cng",
            "parking", "toll", "metro", "bus", "train", "ticket",
            "flight", "airfare", "boarding"
        ),
        4L to listOf(
            "shirt", "pant", "jeans", "dress", "shoes", "sandals",
            "watch", "bag", "purse", "wallet", "accessories",
            "electronics", "phone", "charger", "cable", "headphones",
            "book", "stationery", "pen", "notebook"
        ),
        5L to listOf(
            "movie", "cinema", "pvr", "inox", "ticket",
            "game", "gaming", "playstation", "xbox", "steam",
            "concert", "show", "event", "festival"
        ),
        6L to listOf(
            "netflix", "prime", "hotstar", "disney", "spotify",
            "youtube", "premium", "subscription", "membership",
            "gym", "fitness", "cloud", "storage", "monthly"
        ),
        7L to listOf(
            "electricity", "electric", "power", "water", "gas",
            "internet", "wifi", "broadband", "mobile", "recharge",
            "bill", "utility", "maintenance"
        ),
        8L to listOf(
            "medicine", "pharmacy", "medical", "doctor", "hospital",
            "clinic", "lab", "test", "health", "vitamin", "tablet",
            "syrup", "injection", "consultation"
        ),
        9L to listOf(
            "school", "college", "university", "course", "class",
            "tuition", "coaching", "book", "textbook", "exam",
            "certification", "training", "workshop"
        )
    )
    
    private val merchantCategories = mapOf(
        "mcdonald's" to 1L, "kfc" to 1L, "domino's" to 1L, "pizza hut" to 1L,
        "subway" to 1L, "starbucks" to 1L, "café coffee day" to 1L, "dunkin'" to 1L,
        "zomato" to 1L, "swiggy" to 1L, "uber eats" to 1L,
        
        "bigbasket" to 2L, "grofers" to 2L, "blinkit" to 2L, "dmart" to 2L,
        "reliance fresh" to 2L, "big bazaar" to 2L, "more" to 2L, "spar" to 2L,
        
        "uber" to 3L, "ola" to 3L, "rapido" to 3L,
        
        "amazon" to 4L, "flipkart" to 4L, "myntra" to 4L, "ajio" to 4L,
        
        "pvr" to 5L, "inox" to 5L, "bookmyshow" to 5L,
        
        "netflix" to 6L, "spotify" to 6L, "hotstar" to 6L,
        
        "apollo" to 8L, "medplus" to 8L, "netmeds" to 8L, "pharmeasy" to 8L
    )

    fun categorize(itemName: String, merchantName: String): Long? {
        val lowerItem = itemName.lowercase()
        val lowerMerchant = merchantName.lowercase()
        
        for ((merchant, categoryId) in merchantCategories) {
            if (lowerMerchant.contains(merchant)) return categoryId
        }
        
        for ((categoryId, keywords) in categoryKeywords) {
            for (keyword in keywords) {
                if (lowerItem.contains(keyword)) return categoryId
            }
        }
        
        return 10L
    }

    fun getCategoryName(categoryId: Long): String = when (categoryId) {
        1L -> "Food & Dining"
        2L -> "Groceries"
        3L -> "Transport"
        4L -> "Shopping"
        5L -> "Entertainment"
        6L -> "Subscriptions"
        7L -> "Utilities"
        8L -> "Healthcare"
        9L -> "Education"
        else -> "Other"
    }
}
