package com.receiptradar.domain.pipeline

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CategorizerTest {
    
    private lateinit var categorizer: Categorizer
    
    @Before
    fun setup() {
        categorizer = Categorizer()
    }
    
    @Test
    fun `categorize should identify food items`() {
        val foodItems = listOf(
            "Burger",
            "Pizza Margherita",
            "Coffee Latte",
            "Chicken Biryani",
            "Ice Cream"
        )
        
        for (item in foodItems) {
            val categoryId = categorizer.categorize(item, "")
            assertEquals("Failed for: $item", 1L, categoryId) // Food & Dining
        }
    }
    
    @Test
    fun `categorize should identify grocery items`() {
        val groceryItems = listOf(
            "Milk 1L",
            "Bread",
            "Fresh Vegetables",
            "Cooking Oil",
            "Detergent"
        )
        
        for (item in groceryItems) {
            val categoryId = categorizer.categorize(item, "")
            assertEquals("Failed for: $item", 2L, categoryId) // Groceries
        }
    }
    
    @Test
    fun `categorize should use merchant context`() {
        // When merchant is McDonald's, categorize as Food
        val categoryId = categorizer.categorize("Combo Meal", "McDonald's")
        assertEquals(1L, categoryId)
        
        // When merchant is BigBasket, categorize as Groceries
        val categoryId2 = categorizer.categorize("Fresh Items", "BigBasket")
        assertEquals(2L, categoryId2)
    }
    
    @Test
    fun `categorize should identify transport items`() {
        val transportItems = listOf(
            "Petrol",
            "Uber Ride",
            "Parking Fee",
            "Metro Ticket"
        )
        
        for (item in transportItems) {
            val categoryId = categorizer.categorize(item, "")
            assertEquals("Failed for: $item", 3L, categoryId) // Transport
        }
    }
    
    @Test
    fun `categorize should identify subscription items`() {
        val subscriptionItems = listOf(
            "Netflix Monthly",
            "Spotify Premium",
            "Gym Membership"
        )
        
        for (item in subscriptionItems) {
            val categoryId = categorizer.categorize(item, "")
            assertEquals("Failed for: $item", 6L, categoryId) // Subscriptions
        }
    }
    
    @Test
    fun `categorize should return Other for unknown items`() {
        val unknownItem = "Random Unknown Item XYZ"
        val categoryId = categorizer.categorize(unknownItem, "")
        assertEquals(10L, categoryId) // Other
    }
    
    @Test
    fun `getCategoryName should return correct names`() {
        assertEquals("Food & Dining", categorizer.getCategoryName(1L))
        assertEquals("Groceries", categorizer.getCategoryName(2L))
        assertEquals("Transport", categorizer.getCategoryName(3L))
        assertEquals("Other", categorizer.getCategoryName(999L))
    }
}
