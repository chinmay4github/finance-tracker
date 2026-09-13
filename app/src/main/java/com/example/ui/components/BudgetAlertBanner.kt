package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertSeverity
import com.example.data.model.BudgetAlert
import com.example.ui.theme.AlertDangerBg
import com.example.ui.theme.AlertDangerRed
import com.example.ui.theme.AlertWarningAmber
import com.example.ui.theme.AlertWarningBg

@Composable
fun BudgetAlertBanner(
  alerts: List<BudgetAlert>,
  onViewBudgetsClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  AnimatedVisibility(
    visible = alerts.isNotEmpty(),
    modifier = modifier
  ) {
    val topAlert = alerts.firstOrNull() ?: return@AnimatedVisibility
    val isCritical = alerts.any { it.severity == AlertSeverity.EXCEEDED }
    val exceededCount = alerts.count { it.severity == AlertSeverity.EXCEEDED }
    val warningCount = alerts.count { it.severity == AlertSeverity.WARNING }

    val bgColor = if (isCritical) AlertDangerBg else AlertWarningBg
    val contentColor = if (isCritical) AlertDangerRed else AlertWarningAmber
    val borderColor = if (isCritical) AlertDangerRed.copy(alpha = 0.3f) else AlertWarningAmber.copy(alpha = 0.3f)

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .clickable { onViewBudgetsClicked() }
        .testTag("budget_alert_banner"),
      colors = CardDefaults.cardColors(containerColor = bgColor),
      shape = RoundedCornerShape(16.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(contentColor.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.NotificationsActive,
            contentDescription = "Alert icon",
            tint = contentColor,
            modifier = Modifier.size(22.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = if (isCritical) "Automated Budget Alert" else "Budget Threshold Warning",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = contentColor
            )

            if (alerts.size > 1) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(contentColor.copy(alpha = 0.2f))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "${alerts.size} active",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = contentColor
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(2.dp))

          Text(
            text = topAlert.category + ": " + topAlert.message,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            fontWeight = FontWeight.Medium,
            maxLines = 2
          )
        }

        Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = "View alerts",
          tint = contentColor,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
