package com.example.data.remote

/**
 * Provides the complete, production-ready Google Apps Script source code
 * that users can copy and paste directly into Google Sheets (Extensions > Apps Script).
 *
 * It creates a REST API with GET and POST handlers to support real-time
 * storage, deletion, and two-way sync with the Android Room DB.
 */
object GoogleSheetScriptTemplate {

  const val DEFAULT_DEMO_URL = "https://script.google.com/macros/s/AKfycbwfI_vg3pq-Lj5sa8QAFgBALBLE-sDR4zbgJGLX2OIJ97fseHq452l9OeE9PAIYIjZduw/exec"

  val APPS_SCRIPT_CODE = """
/**
 * ===================================================================
 * Personal Finance Tracker - Real-time Google Sheet Web API
 * Created for: chinmay.moharana99@gmail.com
 * ===================================================================
 * Quick 6-Step Setup Instructions:
 * 1. Log in to Google with chinmay.moharana99@gmail.com
 * 2. Open https://sheets.new (create a new Google Sheet)
 * 3. In the menu: Extensions > Apps Script
 * 4. Replace Code.gs with THIS entire script & click Save (disk icon)
 * 5. Click 'Deploy' > 'New deployment'
 *    - Type: 'Web app'
 *    - Description: Finance Tracker Sync API
 *    - Execute as: 'Me (chinmay.moharana99@gmail.com)'
 *    - Who has access: 'Anyone'
 * 6. Click 'Deploy', grant permission for chinmay.moharana99@gmail.com,
 *    and copy the Web App URL into the Android Finance app.
 * ===================================================================
 */

function doGet(e) {
  try {
    const action = (e && e.parameter && e.parameter.action) || 'fetch';
    
    // Ping / Test connection
    if (action === 'ping' || action === 'test') {
      return respondJson({
        status: 'success',
        message: 'Google Sheets API is online and connected!',
        spreadsheetName: SpreadsheetApp.getActiveSpreadsheet().getName(),
        timestamp: new Date().toISOString()
      });
    }

    // Fetch transactions & budgets
    const ss = SpreadsheetApp.getActiveSpreadsheet();
    const txSheet = getOrCreateSheet(ss, 'Transactions', getTransactionHeaders());
    const txData = txSheet.getDataRange().getValues();
    
    const transactions = [];
    // Skip header row
    for (let i = 1; i < txData.length; i++) {
      const row = txData[i];
      if (row[0] === '' && row[2] === '') continue; // Skip empty rows
      transactions.push({
        id: Number(row[0]) || 0,
        dateFormatted: String(row[1] || ''),
        title: String(row[2] || ''),
        amount: Number(row[3]) || 0,
        type: String(row[4] || 'EXPENSE'),
        category: String(row[5] || 'General'),
        paymentMethod: String(row[6] || 'Card'),
        note: String(row[7] || ''),
        timestamp: row[8] ? new Date(row[8]).getTime() : Date.now()
      });
    }

    return respondJson({
      status: 'success',
      count: transactions.length,
      transactions: transactions,
      spreadsheetName: ss.getName(),
      lastUpdated: new Date().toISOString()
    });

  } catch (error) {
    return respondJson({
      status: 'error',
      message: error.toString()
    });
  }
}

function doPost(e) {
  try {
    if (!e || !e.postData || !e.postData.contents) {
      return respondJson({ status: 'error', message: 'No payload received' });
    }

    const payload = JSON.parse(e.postData.contents);
    const action = payload.action || 'STORE_TRANSACTION';
    const ss = SpreadsheetApp.getActiveSpreadsheet();

    if (action === 'STORE_TRANSACTION') {
      const tx = payload.transaction;
      if (!tx) {
        return respondJson({ status: 'error', message: 'No transaction object provided' });
      }

      const txSheet = getOrCreateSheet(ss, 'Transactions', getTransactionHeaders());
      const txData = txSheet.getDataRange().getValues();
      let rowIndex = -1;

      // Check if transaction ID already exists to update it
      for (let i = 1; i < txData.length; i++) {
        if (String(txData[i][0]) === String(tx.id)) {
          rowIndex = i + 1; // 1-based index
          break;
        }
      }

      const dateStr = tx.dateFormatted || formatDate(new Date(tx.timestamp || Date.now()));
      const rowValues = [
        tx.id,
        dateStr,
        tx.title,
        Number(tx.amount),
        tx.type,
        tx.category,
        tx.paymentMethod || 'Cash',
        tx.note || '',
        new Date().toISOString()
      ];

      if (rowIndex > 0) {
        // Update existing row
        txSheet.getRange(rowIndex, 1, 1, rowValues.length).setValues([rowValues]);
      } else {
        // Append new row
        txSheet.appendRow(rowValues);
      }

      return respondJson({
        status: 'success',
        message: 'Transaction saved to Google Sheet',
        transactionId: tx.id,
        action: rowIndex > 0 ? 'UPDATED' : 'INSERTED',
        timestamp: new Date().toISOString()
      });
    }

    if (action === 'DELETE_TRANSACTION') {
      const id = payload.id;
      const txSheet = getOrCreateSheet(ss, 'Transactions', getTransactionHeaders());
      const txData = txSheet.getDataRange().getValues();
      let deleted = false;

      for (let i = 1; i < txData.length; i++) {
        if (String(txData[i][0]) === String(id)) {
          txSheet.deleteRow(i + 1);
          deleted = true;
          break;
        }
      }

      return respondJson({
        status: 'success',
        message: deleted ? 'Transaction deleted from Google Sheet' : 'Row not found',
        deleted: deleted
      });
    }

    if (action === 'SYNC_ALL') {
      const transactions = payload.transactions || [];
      const txSheet = getOrCreateSheet(ss, 'Transactions', getTransactionHeaders());

      // Clear existing content below header
      const lastRow = txSheet.getLastRow();
      if (lastRow > 1) {
        txSheet.getRange(2, 1, lastRow - 1, txSheet.getLastColumn()).clearContent();
      }

      if (transactions.length > 0) {
        const rows = transactions.map(tx => [
          tx.id,
          tx.dateFormatted || formatDate(new Date(tx.timestamp || Date.now())),
          tx.title,
          Number(tx.amount),
          tx.type,
          tx.category,
          tx.paymentMethod || 'Card',
          tx.note || '',
          new Date().toISOString()
        ]);
        txSheet.getRange(2, 1, rows.length, rows[0].length).setValues(rows);
      }

      return respondJson({
        status: 'success',
        message: 'Full sync complete: ' + transactions.length + ' transactions stored',
        count: transactions.length,
        timestamp: new Date().toISOString()
      });
    }

    return respondJson({ status: 'error', message: 'Unknown action: ' + action });

  } catch (error) {
    return respondJson({ status: 'error', message: error.toString() });
  }
}

// Helper: Ensure sheet tab exists with beautiful header styling
function getOrCreateSheet(ss, sheetName, headers) {
  let sheet = ss.getSheetByName(sheetName);
  if (!sheet) {
    sheet = ss.insertSheet(sheetName);
    sheet.appendRow(headers);
    
    // Style headers
    const headerRange = sheet.getRange(1, 1, 1, headers.length);
    headerRange.setBackground('#0F9D58'); // Google Sheets green
    headerRange.setFontColor('#FFFFFF');
    headerRange.setFontWeight('bold');
    headerRange.setFontSize(11);
    sheet.setFrozenRows(1);
    
    // Auto-fit column widths
    for (let c = 1; c <= headers.length; c++) {
      sheet.setColumnWidth(c, 140);
    }
  }
  return sheet;
}

function getTransactionHeaders() {
  return ['ID', 'Date', 'Title', 'Amount', 'Type', 'Category', 'Payment Method', 'Note', 'Last Synced'];
}

function formatDate(d) {
  return Utilities.formatDate(d, Session.getScriptTimeZone() || 'GMT', 'yyyy-MM-dd HH:mm');
}

function respondJson(obj) {
  return ContentService
    .createTextOutput(JSON.stringify(obj))
    .setMimeType(ContentService.MimeType.JSON);
}
""".trimIndent()
}
