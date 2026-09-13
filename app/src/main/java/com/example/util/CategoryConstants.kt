package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.CatEducation
import com.example.ui.theme.CatEntertainment
import com.example.ui.theme.CatFood
import com.example.ui.theme.CatHealthcare
import com.example.ui.theme.CatHousing
import com.example.ui.theme.CatIncome
import com.example.ui.theme.CatInvestment
import com.example.ui.theme.CatOther
import com.example.ui.theme.CatPersonal
import com.example.ui.theme.CatShopping
import com.example.ui.theme.CatTransport
import com.example.ui.theme.CatUtilities

data class CategoryInfo(
  val name: String,
  val icon: ImageVector,
  val color: Color,
  val isIncome: Boolean = false
)

object CategoryConstants {
  val EXPENSE_CATEGORIES = listOf(
    CategoryInfo("Food & Dining", Icons.Default.Restaurant, CatFood),
    CategoryInfo("Housing & Rent", Icons.Default.Home, CatHousing),
    CategoryInfo("Shopping", Icons.Default.ShoppingBag, CatShopping),
    CategoryInfo("Transportation", Icons.Default.DirectionsCar, CatTransport),
    CategoryInfo("Utilities", Icons.Default.Bolt, CatUtilities),
    CategoryInfo("Entertainment", Icons.Default.Movie, CatEntertainment),
    CategoryInfo("Healthcare", Icons.Default.LocalHospital, CatHealthcare),
    CategoryInfo("Personal Care", Icons.Default.Person, CatPersonal),
    CategoryInfo("Education", Icons.Default.School, CatEducation),
    CategoryInfo("Other", Icons.Default.MoreHoriz, CatOther)
  )

  val INCOME_CATEGORIES = listOf(
    CategoryInfo("Salary & Wages", Icons.Default.Work, CatIncome, true),
    CategoryInfo("Freelance", Icons.Default.AttachMoney, CatIncome, true),
    CategoryInfo("Investments", Icons.Default.TrendingUp, CatInvestment, true),
    CategoryInfo("Other Income", Icons.Default.AccountBalance, CatOther, true)
  )

  val ALL_CATEGORIES = EXPENSE_CATEGORIES + INCOME_CATEGORIES

  val PAYMENT_METHODS = listOf(
    "Credit Card",
    "Debit Card",
    "Cash",
    "Bank Transfer",
    "Mobile Pay"
  )

  fun getCategoryInfo(name: String): CategoryInfo {
    return ALL_CATEGORIES.find { it.name.equals(name, ignoreCase = true) }
      ?: CategoryInfo(name, Icons.Default.MoreHoriz, CatOther)
  }
}
