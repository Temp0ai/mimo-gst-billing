package com.mimo.gstbilling.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.InvoiceItemEntity
import com.mimo.gstbilling.data.local.entity.CompanyEntity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object ThermalPrinter {
    private const val ESC_POS_ALIGN_CENTER = "\u001B\u0061\u0001"
    private const val ESC_POS_ALIGN_LEFT = "\u001B\u0061\u0000"
    private const val ESC_POS_BOLD_ON = "\u001B\u0045\u0001"
    private const val ESC_POS_BOLD_OFF = "\u001B\u0045\u0000"
    private const val ESC_POS_DOUBLE_HEIGHT = "\u001B\u0021\u0010"
    private const val ESC_POS_NORMAL = "\u001B\u0021\u0000"
    private const val ESC_POS_CUT = "\u001D\u0056\u0000"

    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    fun connect(macAddress: String): Boolean {
        return try {
            val adapter = BluetoothAdapter.getDefaultAdapter() ?: return false
            val device = adapter.getRemoteDevice(macAddress)
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            socket = device.createRfcommSocketToServiceRecord(uuid)
            adapter.cancelDiscovery()
            socket?.connect()
            outputStream = socket?.outputStream
            true
        } catch (e: Exception) {
            false
        }
    }

    fun disconnect() {
        try {
            outputStream?.close()
            socket?.close()
        } catch (_: Exception) {}
        outputStream = null
        socket = null
    }

    fun isConnected(): Boolean = socket?.isConnected == true

    fun printInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        company: CompanyEntity?
    ): Boolean {
        if (!isConnected()) return false
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
            val sb = StringBuilder()

            sb.append(ESC_POS_ALIGN_CENTER)
            sb.append(ESC_POS_BOLD_ON)
            sb.append(ESC_POS_DOUBLE_HEIGHT)
            sb.appendLine(company?.name ?: "MY BUSINESS")
            sb.append(ESC_POS_NORMAL)
            sb.append(ESC_POS_BOLD_OFF)

            company?.address?.let { sb.appendLine(it) }
            company?.phone?.let { sb.appendLine("Ph: $it") }
            company?.gstin?.let { sb.appendLine("GSTIN: $it") }
            sb.appendLine("================================")
            sb.append(ESC_POS_BOLD_ON)
            sb.appendLine("TAX INVOICE")
            sb.append(ESC_POS_BOLD_OFF)
            sb.appendLine("Invoice: ${invoice.invoiceNumber}")
            sb.appendLine("Date: ${dateFormat.format(Date(invoice.invoiceDate))}")
            sb.appendLine("Status: ${invoice.paymentStatus.uppercase()}")
            sb.appendLine("--------------------------------")
            sb.appendLine(String.format("%-15s %5s %8s %10s", "Item", "Qty", "Rate", "Amount"))
            sb.appendLine("--------------------------------")

            items.forEach { item ->
                sb.appendLine(String.format(
                    "%-15s %5s %8s %10s",
                    item.itemName.take(15),
                    "${item.quantity.toInt()}",
                    String.format(Locale.US, "%.0f", item.price),
                    String.format(Locale.US, "%.0f", item.totalAmount)
                ))
            }
            sb.appendLine("--------------------------------")
            sb.appendLine(String.format("%-24s %10s", "Subtotal:", String.format(Locale.US, "%.2f", invoice.subTotal)))
            sb.appendLine(String.format("%-24s %10s", "CGST:", String.format(Locale.US, "%.2f", invoice.cgstTotal)))
            sb.appendLine(String.format("%-24s %10s", "SGST:", String.format(Locale.US, "%.2f", invoice.sgstTotal)))
            if (invoice.igstTotal > 0) sb.appendLine(String.format("%-24s %10s", "IGST:", String.format(Locale.US, "%.2f", invoice.igstTotal)))
            if (invoice.tcsAmount > 0) sb.appendLine(String.format("%-24s %10s", "TCS:", String.format(Locale.US, "%.2f", invoice.tcsAmount)))
            sb.appendLine("================================")
            sb.append(ESC_POS_BOLD_ON)
            sb.append(ESC_POS_DOUBLE_HEIGHT)
            sb.appendLine(String.format("%-24s %10s", "TOTAL:", String.format(Locale.US, "%.2f", invoice.totalAmount)))
            sb.append(ESC_POS_NORMAL)
            sb.append(ESC_POS_BOLD_OFF)
            sb.appendLine("================================")
            sb.appendLine("Thank you! Visit again.")
            sb.appendLine()
            sb.appendLine()
            sb.append(ESC_POS_CUT)

            outputStream?.write(sb.toString().toByteArray())
            outputStream?.flush()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun printText(text: String): Boolean {
        if (!isConnected()) return false
        return try {
            outputStream?.write(text.toByteArray())
            outputStream?.write(ESC_POS_CUT.toByteArray())
            outputStream?.flush()
            true
        } catch (e: Exception) {
            false
        }
    }
}
