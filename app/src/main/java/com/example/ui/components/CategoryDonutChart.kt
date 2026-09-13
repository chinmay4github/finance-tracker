package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.CategoryConstants
import com.example.util.CurrencyUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDonutChart(
  categorySpendMap: Map<String, Double>,
  totalExpense: Double,
  modifier: Modifier = Modifier
) {
  if (categorySpendMap.isEmpty() || totalExpense <= 0) {
    Box(
      modifier = modifier
        .fillMaxWidth()
        .height(160.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "No expenses recorded this month",
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    return
  }

  val sortedEntries = remember(categorySpendMap) {
    categorySpendMap.entries
      .sortedByDescending { it.value }
      .take(6)
  }

  // Animation for progress sweep
  val animationProgress = remember { Animatable(0f) }
  LaunchedEffect(categorySpendMap) {
    animationProgress.snapTo(0f)
    animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(190.dp)
        .testTag("category_donut_chart"),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.size(160.dp)) {
        val strokeWidth = 24.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        var currentStartAngle = -90f

        // Draw background subtle track
        drawCircle(
          color = Color.LightGray.copy(alpha = 0.2f),
          radius = radius,
          style = Stroke(width = strokeWidth)
        )

        for (entry in sortedEntries) {
          val proportion = (entry.value / totalExpense).toFloat()
          val sweepAngle = proportion * 360f * animationProgress.value
          val categoryColor = CategoryConstants.getCategoryInfo(entry.key).color

          drawArc(
            color = categoryColor,
            startAngle = currentStartAngle,
            sweepAngle = (sweepAngle - 2f).coerceAtLeast(1f), // small gap
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
          )

          currentStartAngle += sweepAngle
        }
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "Total Spent",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Medium
        )
        Text(
          text = CurrencyUtils.formatCompact(totalExpense),
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Interactive/Visual Legend
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      for (entry in sortedEntries) {
        val catInfo = CategoryConstants.getCategoryInfo(entry.key)
        val percent = (entry.value / totalExpense) * 100.0

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(catInfo.color)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${catInfo.name} (${percent.toInt()}%)",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
  }
}
