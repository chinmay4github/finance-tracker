package com.example.data

import com.example.data.model.BudgetEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.util.Calendar

object SampleData {
  fun getInitialBudgets(): List<BudgetEntity> {
    return listOf(
      BudgetEntity(
        category = "Overall",
        monthlyLimit = 3200.0,
        alertThresholdPercent = 80,
        isAlertEnabled = true
      ),
      BudgetEntity(
        category = "Food & Dining",
        monthlyLimit = 550.0,
        alertThresholdPercent = 80,
        isAlertEnabled = true
      ),
      BudgetEntity(
        category = "Shopping",
        monthlyLimit = 350.0,
        alertThresholdPercent = 85,
        isAlertEnabled = true
      ),
      BudgetEntity(
        category = "Transportation",
        monthlyLimit = 220.0,
        alertThresholdPercent = 75,
        isAlertEnabled = true
      ),
      BudgetEntity(
        category = "Entertainment",
        monthlyLimit = 180.0,
        alertThresholdPercent = 80,
        isAlertEnabled = true
      ),
      BudgetEntity(
        category = "Utilities",
        monthlyLimit = 260.0,
        alertThresholdPercent = 90,
        isAlertEnabled = true
      ),
      BudgetEntity(
        category = "Housing & Rent",
        monthlyLimit = 1200.0,
        alertThresholdPercent = 100,
        isAlertEnabled = true
      )
    )
  }

  fun getInitialTransactions(): List<TransactionEntity> {
    val cal = Calendar.getInstance()
    val now = cal.timeInMillis

    // Helper to get time offset by days
    fun daysAgo(days: Int, hour: Int = 12, minute: Int = 0): Long {
      val c = Calendar.getInstance()
      c.add(Calendar.DAY_OF_MONTH, -days)
      c.set(Calendar.HOUR_OF_DAY, hour)
      c.set(Calendar.MINUTE, minute)
      return c.timeInMillis
    }

    return listOf(
      // Income
      TransactionEntity(
        title = "Monthly Salary",
        amount = 3850.0,
        type = TransactionType.INCOME,
        category = "Salary & Wages",
        paymentMethod = "Bank Transfer",
        timestamp = daysAgo(12, 9, 30),
        note = "Direct deposit from Acme Corp"
      ),
      TransactionEntity(
        title = "Freelance UX Design",
        amount = 450.0,
        type = TransactionType.INCOME,
        category = "Freelance",
        paymentMethod = "Bank Transfer",
        timestamp = daysAgo(5, 15, 0),
        note = "Client logo & app icon redesign"
      ),

      // Housing & Rent
      TransactionEntity(
        title = "Apartment Rent",
        amount = 1200.0,
        type = TransactionType.EXPENSE,
        category = "Housing & Rent",
        paymentMethod = "Bank Transfer",
        timestamp = daysAgo(12, 10, 0),
        note = "September rent payment"
      ),

      // Food & Dining (High spend -> Warning alert ~88%)
      TransactionEntity(
        title = "Whole Foods Groceries",
        amount = 142.50,
        type = TransactionType.EXPENSE,
        category = "Food & Dining",
        paymentMethod = "Credit Card",
        timestamp = daysAgo(10, 17, 30),
        note = "Weekly fresh groceries & pantry staples"
      ),
      TransactionEntity(
        title = "Italian Bistro Dinner",
        amount = 86.20,
        type = TransactionType.EXPENSE,
        category = "Food & Dining",
        paymentMethod = "Credit Card",
        timestamp = daysAgo(7, 20, 15),
        note = "Dinner with team"
      ),
      TransactionEntity(
        title = "Supermarket Essentials",
        amount = 135.00,
        type = TransactionType.EXPENSE,
        category = "Food & Dining",
        paymentMethod = "Debit Card",
        timestamp = daysAgo(3, 14, 20),
        note = "Household grocery restock"
      ),
      TransactionEntity(
        title = "Artisan Cafe & Bakery",
        amount = 28.50,
        type = TransactionType.EXPENSE,
        category = "Food & Dining",
        paymentMethod = "Credit Card",
        timestamp = daysAgo(1, 10, 45),
        note = "Coffee and pastries"
      ),
      TransactionEntity(
        title = "Sushi Express Delivery",
        amount = 64.80,
        type = TransactionType.EXPENSE,
        category = "Food & Dining",
        paymentMethod = "Credit Card",
        timestamp = daysAgo(0, 19, 10),
        note = "Weekend dinner delivery"
      ),

      // Shopping (Exceeded budget alert: 350 budget vs 378 spent -> 108% Exceeded!)
      TransactionEntity(
        title = "Autumn Jacket & Boots",
        amount = 220.00,
        type = TransactionType.EXPENSE,
        category = "Shopping",
        paymentMethod = "Credit Card",
        timestamp = daysAgo(8, 16, 0),
        note = "Seasonal wardrobe essentials"
      ),
      TransactionEntity(
        title = "Electronics Store Gadgets",
        amount = 158.00,
        type = TransactionType.EXPENSE,
        category = "Shopping",
        paymentMethod = "Credit Card",
        timestamp = daysAgo(2, 13, 30),
        note = "USB-C dock & wireless mouse"
      ),

      // Transportation
      TransactionEntity(
        title = "Metropolitan Transit Pass",
        amount = 85.00,
        type = TransactionType.EXPENSE,
        category = "Transportation",
        paymentMethod = "Debit Card",
        timestamp = daysAgo(11, 8, 15),
        note = "Monthly subway & bus pass"
      ),
      TransactionEntity(
        title = "Gas Station Fuel",
        amount = 45.00,
        type = TransactionType.EXPENSE,
        category = "Transportation",
        paymentMethod = "Credit Card",
        timestamp = daysAgo(4, 18, 0),
        note = "Full tank refill"
      ),

      // Utilities
      TransactionEntity(
        title = "Electric & Energy Bill",
        amount = 95.40,
        type = TransactionType.EXPENSE,
        category = "Utilities",
        paymentMethod = "Bank Transfer",
        timestamp = daysAgo(9, 11, 0),
        note = "Monthly power consumption"
      ),
      TransactionEntity(
        title = "High-Speed Fiber Internet",
        amount = 60.00,
        type = TransactionType.EXPENSE,
        category = "Utilities",
        paymentMethod = "Credit Card",
        timestamp = daysAgo(6, 12, 0),
        note = "Home fiber plan"
      ),

      // Entertainment
      TransactionEntity(
        title = "Cinema IMAX Tickets",
        amount = 38.00,
        type = TransactionType.EXPENSE,
        category = "Entertainment",
        paymentMethod = "Credit Card",
        timestamp = daysAgo(4, 21, 0),
        note = "Weekend sci-fi movie night"
      ),
      TransactionEntity(
        title = "Streaming Subscriptions",
        amount = 29.99,
        type = TransactionType.EXPENSE,
        category = "Entertainment",
        paymentMethod = "Credit Card",
        timestamp = daysAgo(9, 6, 0),
        note = "Music & video subscription bundle"
      )
    )
  }
}
