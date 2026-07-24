package com.mimo.gstbilling.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Build
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.InvoiceItemEntity
import com.mimo.gstbilling.data.local.entity.CompanyEntity
import com.mimo.gstbilling.data.local.entity.PartyEntity
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class ThermalTheme(val displayName: String) {
    THEME_1("Theme 1 - Classic"),
    THEME_2("Theme 2 - Bold Header"),
    THEME_3("Theme 3 - Minimal"),
    THEME_4("Theme 4 - With Border"),
    THEME_5("Theme 5 - Detailed")
}

object ThermalPrinter {
    private const val ESC_POS_ALIGN_CENTER = "\u001B\u0061\u0001"
    private const val ESC_POS_ALIGN_LEFT = "\u001B\u0061\u0000"
    private const val ESC_POS_ALIGN_RIGHT = "\u001B\u0061\u0002"
    private const val ESC_POS_BOLD_ON = "\u001B\u0045\u0001"
    private const val ESC_POS_BOLD_OFF = "\u001B\u0045\u0000"
    private const val ESC_POS_DOUBLE_HEIGHT = "\u001B\u0021\u0010"
    private const val ESC_POS_DOUBLE_WIDTH = "\u001B\u0021\u0020"
    private const val ESC_POS_DOUBLE_BOTH = "\u001B\u0021\u0030"
    private const val ESC_POS_NORMAL = "\u001B\u0021\u0000"
    private const val ESC_POS_CUT = "\u001D\u0056\u0000"
    private const val ESC_POS_PARTIAL_CUT = "\u001D\u0056\u0001"
    private const val ESC_POS_INVERSE_ON = "\u001B\u0034\u0001"
    private const val ESC_POS_INVERSE_OFF = "\u001B\u0034\u0000"
    private const val ESC_POS_UNDERLINE_ON = "\u001B\u002D\u0001"
    private const val ESC_POS_UNDERLINE_OFF = "\u001B\u002D\u0000"

    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var usbConnection: UsbDeviceConnection? = null
    private var usbInterface: UsbInterface? = null
    private var connectionType: ConnectionType = ConnectionType.NONE

    private enum class ConnectionType { BLUETOOTH, USB, NONE }

    // ========== BLUETOOTH CONNECTION ==========
    fun connect(macAddress: String): Boolean {
        return try {
            val adapter = BluetoothAdapter.getDefaultAdapter() ?: return false
            val device = adapter.getRemoteDevice(macAddress)
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            socket = device.createRfcommSocketToServiceRecord(uuid)
            adapter.cancelDiscovery()
            socket?.connect()
            outputStream = socket?.outputStream
            connectionType = ConnectionType.BLUETOOTH
            true
        } catch (e: Exception) {
            false
        }
    }

    // ========== USB CONNECTION ==========
    fun connectUsb(context: Context, device: UsbDevice): Boolean {
        return try {
            val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            val connection = usbManager.openDevice(device) ?: return false
            usbConnection = connection

            val iface = device.getInterface(0)
            usbInterface = iface

            var outEndpoint = null as android.hardware.usb.UsbEndpoint?
            for (i in 0 until iface.endpointCount) {
                val ep = iface.getEndpoint(i)
                if (ep.type == android.hardware.usb.UsbConstants.USB_ENDPOINT_XFER_BULK && ep.direction == android.hardware.usb.UsbConstants.USB_DIR_OUT) {
                    outEndpoint = ep
                    break
                }
            }

            if (outEndpoint != null) {
                connection.claimInterface(iface, true)
                connectionType = ConnectionType.USB
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun disconnect() {
        try {
            outputStream?.close()
            socket?.close()
            usbConnection?.close()
        } catch (_: Exception) {}
        outputStream = null
        socket = null
        usbConnection = null
        usbInterface = null
        connectionType = ConnectionType.NONE
    }

    fun isConnected(): Boolean = when (connectionType) {
        ConnectionType.BLUETOOTH -> socket?.isConnected == true
        ConnectionType.USB -> usbConnection != null
        ConnectionType.NONE -> false
    }

    // ========== PRINT INVOICE ==========
    fun printInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        company: CompanyEntity?,
        party: PartyEntity? = null,
        theme: ThermalTheme = ThermalTheme.THEME_1
    ): Boolean {
        if (!isConnected()) return false
        return try {
            val text = when (theme) {
                ThermalTheme.THEME_1 -> buildTheme1(invoice, items, company, party)
                ThermalTheme.THEME_2 -> buildTheme2(invoice, items, company, party)
                ThermalTheme.THEME_3 -> buildTheme3(invoice, items, company, party)
                ThermalTheme.THEME_4 -> buildTheme4(invoice, items, company, party)
                ThermalTheme.THEME_5 -> buildTheme5(invoice, items, company, party)
            }
            sendBytes(text.toByteArray())
            true
        } catch (e: Exception) {
            false
        }
    }

    // ========== THEME 1: CLASSIC ==========
    private fun buildTheme1(invoice: InvoiceEntity, items: List<InvoiceItemEntity>, company: CompanyEntity?, party: PartyEntity?): String {
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
        party?.name?.let { sb.appendLine("Bill To: $it") }
        sb.appendLine("Status: ${invoice.paymentStatus.uppercase()}")
        sb.appendLine("--------------------------------")
        sb.appendLine(String.format("%-15s %5s %8s %10s", "Item", "Qty", "Rate", "Amount"))
        sb.appendLine("--------------------------------")

        items.forEach { item ->
            sb.appendLine(String.format("%-15s %5s %8s %10s", item.itemName.take(15), "${item.quantity.toInt()}", String.format(Locale.US, "%.0f", item.price), String.format(Locale.US, "%.0f", item.totalAmount)))
        }
        sb.appendLine("--------------------------------")
        sb.appendLine(String.format("%-24s %10s", "Subtotal:", String.format(Locale.US, "%.2f", invoice.subTotal)))
        if (invoice.discount > 0) sb.appendLine(String.format("%-24s %10s", "Discount:", String.format(Locale.US, "-%.2f", invoice.discount)))
        sb.appendLine(String.format("%-24s %10s", "CGST:", String.format(Locale.US, "%.2f", invoice.cgstTotal)))
        sb.appendLine(String.format("%-24s %10s", "SGST:", String.format(Locale.US, "%.2f", invoice.sgstTotal)))
        if (invoice.igstTotal > 0) sb.appendLine(String.format("%-24s %10s", "IGST:", String.format(Locale.US, "%.2f", invoice.igstTotal)))
        sb.appendLine("================================")
        sb.append(ESC_POS_BOLD_ON)
        sb.append(ESC_POS_DOUBLE_HEIGHT)
        sb.appendLine(String.format("%-24s %10s", "TOTAL:", String.format(Locale.US, "%.2f", invoice.totalAmount)))
        sb.append(ESC_POS_NORMAL)
        sb.append(ESC_POS_BOLD_OFF)
        sb.appendLine("================================")
        if (invoice.amountPaid > 0) {
            sb.appendLine(String.format("%-24s %10s", "Paid:", String.format(Locale.US, "%.2f", invoice.amountPaid)))
            sb.appendLine(String.format("%-24s %10s", "Balance:", String.format(Locale.US, "%.2f", invoice.totalAmount - invoice.amountPaid)))
        }
        sb.appendLine("Thank you! Visit again.")
        sb.appendLine()
        sb.append(ESC_POS_CUT)

        return sb.toString()
    }

    // ========== THEME 2: BOLD HEADER ==========
    private fun buildTheme2(invoice: InvoiceEntity, items: List<InvoiceItemEntity>, company: CompanyEntity?, party: PartyEntity?): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        val sb = StringBuilder()

        sb.append(ESC_POS_ALIGN_CENTER)
        sb.append(ESC_POS_BOLD_ON)
        sb.append(ESC_POS_DOUBLE_BOTH)
        sb.appendLine(company?.name ?: "MY BUSINESS")
        sb.append(ESC_POS_NORMAL)
        sb.append(ESC_POS_BOLD_OFF)
        company?.address?.let { sb.appendLine(it) }
        company?.phone?.let { sb.appendLine("Ph: $it") }
        company?.gstin?.let { sb.appendLine("GSTIN: $it") }

        sb.appendLine("******************************")
        sb.append(ESC_POS_BOLD_ON)
        sb.append(ESC_POS_DOUBLE_HEIGHT)
        sb.appendLine("TAX INVOICE")
        sb.append(ESC_POS_NORMAL)
        sb.append(ESC_POS_BOLD_OFF)
        sb.appendLine("******************************")

        sb.append(ESC_POS_ALIGN_LEFT)
        sb.appendLine("Invoice: ${invoice.invoiceNumber}")
        sb.appendLine("Date: ${dateFormat.format(Date(invoice.invoiceDate))}")
        party?.name?.let { sb.appendLine("Bill To: $it") }
        sb.appendLine("Status: ${invoice.paymentStatus.uppercase()}")
        sb.appendLine("******************************")
        sb.appendLine(String.format("%-15s %5s %8s %10s", "Item", "Qty", "Rate", "Amount"))
        sb.appendLine("******************************")

        items.forEach { item ->
            sb.appendLine(String.format("%-15s %5s %8s %10s", item.itemName.take(15), "${item.quantity.toInt()}", String.format(Locale.US, "%.0f", item.price), String.format(Locale.US, "%.0f", item.totalAmount)))
        }
        sb.appendLine("******************************")
        sb.appendLine(String.format("%-24s %10s", "Subtotal:", String.format(Locale.US, "%.2f", invoice.subTotal)))
        sb.appendLine(String.format("%-24s %10s", "CGST:", String.format(Locale.US, "%.2f", invoice.cgstTotal)))
        sb.appendLine(String.format("%-24s %10s", "SGST:", String.format(Locale.US, "%.2f", invoice.sgstTotal)))
        if (invoice.igstTotal > 0) sb.appendLine(String.format("%-24s %10s", "IGST:", String.format(Locale.US, "%.2f", invoice.igstTotal)))
        sb.appendLine("******************************")
        sb.append(ESC_POS_ALIGN_CENTER)
        sb.append(ESC_POS_BOLD_ON)
        sb.append(ESC_POS_DOUBLE_BOTH)
        sb.appendLine(String.format("%-24s %10s", "TOTAL:", String.format(Locale.US, "%.2f", invoice.totalAmount)))
        sb.append(ESC_POS_NORMAL)
        sb.append(ESC_POS_BOLD_OFF)
        sb.appendLine("******************************")
        sb.appendLine("Thank you! Visit again.")
        sb.appendLine()
        sb.append(ESC_POS_CUT)

        return sb.toString()
    }

    // ========== THEME 3: MINIMAL ==========
    private fun buildTheme3(invoice: InvoiceEntity, items: List<InvoiceItemEntity>, company: CompanyEntity?, party: PartyEntity?): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        val sb = StringBuilder()

        sb.append(ESC_POS_ALIGN_CENTER)
        sb.append(ESC_POS_BOLD_ON)
        sb.appendLine(company?.name ?: "MY BUSINESS")
        sb.append(ESC_POS_BOLD_OFF)
        company?.gstin?.let { sb.appendLine("GSTIN: $it") }
        sb.appendLine("---")

        sb.append(ESC_POS_ALIGN_LEFT)
        sb.appendLine("Invoice ${invoice.invoiceNumber}")
        sb.appendLine("Date: ${dateFormat.format(Date(invoice.invoiceDate))}")
        party?.name?.let { sb.appendLine("To: $it") }
        sb.appendLine("---")

        items.forEach { item ->
            sb.appendLine(String.format("%-18s %3s x %6s = %8s", item.itemName.take(18), "${item.quantity.toInt()}", String.format(Locale.US, "%.0f", item.price), String.format(Locale.US, "%.0f", item.totalAmount)))
        }
        sb.appendLine("---")
        sb.appendLine(String.format("%-20s %10s", "Subtotal:", String.format(Locale.US, "%.2f", invoice.subTotal)))
        sb.appendLine(String.format("%-20s %10s", "Tax:", String.format(Locale.US, "%.2f", invoice.cgstTotal + invoice.sgstTotal + invoice.igstTotal)))
        sb.append(ESC_POS_BOLD_ON)
        sb.appendLine(String.format("%-20s %10s", "TOTAL:", String.format(Locale.US, "%.2f", invoice.totalAmount)))
        sb.append(ESC_POS_BOLD_OFF)
        sb.appendLine("---")
        sb.appendLine("Thank you!")
        sb.appendLine()
        sb.append(ESC_POS_CUT)

        return sb.toString()
    }

    // ========== THEME 4: WITH BORDER ==========
    private fun buildTheme4(invoice: InvoiceEntity, items: List<InvoiceItemEntity>, company: CompanyEntity?, party: PartyEntity?): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        val sb = StringBuilder()
        val border = "+----------------------------------+"

        sb.append(ESC_POS_ALIGN_CENTER)
        sb.appendLine(border)
        sb.append(ESC_POS_BOLD_ON)
        sb.append(ESC_POS_DOUBLE_HEIGHT)
        sb.appendLine("|  ${company?.name ?: "MY BUSINESS"}  |")
        sb.append(ESC_POS_NORMAL)
        sb.append(ESC_POS_BOLD_OFF)
        company?.address?.let { sb.appendLine("|  $it  |") }
        company?.phone?.let { sb.appendLine("|  Ph: $it  |") }
        company?.gstin?.let { sb.appendLine("|  GSTIN: $it  |") }
        sb.appendLine(border)
        sb.append(ESC_POS_BOLD_ON)
        sb.appendLine("|         TAX INVOICE              |")
        sb.append(ESC_POS_BOLD_OFF)
        sb.appendLine(border)

        sb.append(ESC_POS_ALIGN_LEFT)
        sb.appendLine("| Invoice: ${invoice.invoiceNumber.padEnd(23)}|")
        sb.appendLine("| Date: ${dateFormat.format(Date(invoice.invoiceDate)).padEnd(27)}|")
        party?.name?.let { sb.appendLine("| Bill To: ${it.take(23).padEnd(23)}|") }
        sb.appendLine(border)
        sb.appendLine(String.format("| %-14s | %3s | %6s | %8s |", "Item", "Qty", "Rate", "Amount"))
        sb.appendLine(border)

        items.forEach { item ->
            sb.appendLine(String.format("| %-14s | %3s | %6s | %8s |", item.itemName.take(14), "${item.quantity.toInt()}", String.format(Locale.US, "%.0f", item.price), String.format(Locale.US, "%.0f", item.totalAmount)))
        }
        sb.appendLine(border)
        sb.appendLine(String.format("| %-26s | %8s |", "Subtotal", String.format(Locale.US, "%.2f", invoice.subTotal)))
        sb.appendLine(String.format("| %-26s | %8s |", "CGST", String.format(Locale.US, "%.2f", invoice.cgstTotal)))
        sb.appendLine(String.format("| %-26s | %8s |", "SGST", String.format(Locale.US, "%.2f", invoice.sgstTotal)))
        if (invoice.igstTotal > 0) sb.appendLine(String.format("| %-26s | %8s |", "IGST", String.format(Locale.US, "%.2f", invoice.igstTotal)))
        sb.appendLine(border)
        sb.append(ESC_POS_BOLD_ON)
        sb.appendLine(String.format("| %-26s | %8s |", "TOTAL", String.format(Locale.US, "%.2f", invoice.totalAmount)))
        sb.append(ESC_POS_BOLD_OFF)
        sb.appendLine(border)
        sb.append(ESC_POS_ALIGN_CENTER)
        sb.appendLine("Thank you! Visit again.")
        sb.appendLine()
        sb.append(ESC_POS_CUT)

        return sb.toString()
    }

    // ========== THEME 5: DETAILED ==========
    private fun buildTheme5(invoice: InvoiceEntity, items: List<InvoiceItemEntity>, company: CompanyEntity?, party: PartyEntity?): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        val sb = StringBuilder()

        sb.append(ESC_POS_ALIGN_CENTER)
        sb.append(ESC_POS_BOLD_ON)
        sb.append(ESC_POS_DOUBLE_BOTH)
        sb.appendLine(company?.name ?: "MY BUSINESS")
        sb.append(ESC_POS_NORMAL)
        sb.append(ESC_POS_BOLD_OFF)
        company?.address?.let { sb.appendLine(it) }
        company?.phone?.let { sb.appendLine("Ph: $it  |  Email: ${company.email ?: ""}") }
        company?.gstin?.let { sb.appendLine("GSTIN: $it") }
        company?.state?.let { sb.appendLine("State: $it") }
        sb.appendLine("================================")
        sb.append(ESC_POS_BOLD_ON)
        sb.appendLine("TAX INVOICE")
        sb.append(ESC_POS_BOLD_OFF)
        sb.appendLine("================================")

        sb.append(ESC_POS_ALIGN_LEFT)
        sb.appendLine("Invoice No: ${invoice.invoiceNumber}")
        sb.appendLine("Date: ${dateFormat.format(Date(invoice.invoiceDate))}")
        invoice.dueDate?.let { sb.appendLine("Due Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(it))}") }
        party?.name?.let { sb.appendLine("Bill To: $it") }
        party?.phone?.let { sb.appendLine("Phone: $it") }
        party?.gstin?.let { sb.appendLine("Party GSTIN: $it") }
        sb.appendLine("Payment: ${invoice.paymentStatus.uppercase()}")
        sb.appendLine("================================")
        sb.appendLine(String.format("%-3s %-14s %5s %7s %9s", "#", "Item", "Qty", "Rate", "Amount"))
        sb.appendLine("================================")

        items.forEachIndexed { index, item ->
            sb.appendLine(String.format("%-3d %-14s %5s %7s %9s", index + 1, item.itemName.take(14), "${item.quantity.toInt()}", String.format(Locale.US, "%.0f", item.price), String.format(Locale.US, "%.0f", item.totalAmount)))
            if (!item.hsnCode.isNullOrBlank()) {
                sb.appendLine("    HSN: ${item.hsnCode}  GST: ${item.gstRate.toInt()}%")
            }
        }
        sb.appendLine("================================")
        sb.appendLine(String.format("%-24s %10s", "Subtotal:", String.format(Locale.US, "%.2f", invoice.subTotal)))
        if (invoice.discount > 0) sb.appendLine(String.format("%-24s %10s", "Discount:", String.format(Locale.US, "-%.2f", invoice.discount)))
        sb.appendLine(String.format("%-24s %10s", "CGST:", String.format(Locale.US, "%.2f", invoice.cgstTotal)))
        sb.appendLine(String.format("%-24s %10s", "SGST:", String.format(Locale.US, "%.2f", invoice.sgstTotal)))
        if (invoice.igstTotal > 0) sb.appendLine(String.format("%-24s %10s", "IGST:", String.format(Locale.US, "%.2f", invoice.igstTotal)))
        if (invoice.tcsAmount > 0) sb.appendLine(String.format("%-24s %10s", "TCS:", String.format(Locale.US, "%.2f", invoice.tcsAmount)))
        if (invoice.tdsAmount > 0) sb.appendLine(String.format("%-24s %10s", "TDS:", String.format(Locale.US, "%.2f", invoice.tdsAmount)))
        sb.appendLine("================================")
        sb.append(ESC_POS_BOLD_ON)
        sb.append(ESC_POS_DOUBLE_HEIGHT)
        sb.appendLine(String.format("%-24s %10s", "TOTAL:", String.format(Locale.US, "%.2f", invoice.totalAmount)))
        sb.append(ESC_POS_NORMAL)
        sb.append(ESC_POS_BOLD_OFF)
        sb.appendLine("================================")
        if (invoice.amountPaid > 0) {
            sb.appendLine(String.format("%-24s %10s", "Amount Paid:", String.format(Locale.US, "%.2f", invoice.amountPaid)))
            sb.appendLine(String.format("%-24s %10s", "Balance Due:", String.format(Locale.US, "%.2f", invoice.totalAmount - invoice.amountPaid)))
        }
        invoice.notes?.let { sb.appendLine("Notes: $it") }
        sb.appendLine("================================")
        sb.append(ESC_POS_ALIGN_CENTER)
        sb.appendLine("Thank you for your business!")
        sb.appendLine("Powered by Mimo GST Billing")
        sb.appendLine()
        sb.append(ESC_POS_CUT)

        return sb.toString()
    }

    // ========== SEND BYTES ==========
    private fun sendBytes(data: ByteArray) {
        when (connectionType) {
            ConnectionType.BLUETOOTH -> {
                outputStream?.write(data)
                outputStream?.flush()
            }
            ConnectionType.USB -> {
                usbConnection?.let { conn ->
                    val iface = usbInterface ?: return
                    var outEndpoint = null as android.hardware.usb.UsbEndpoint?
                    for (i in 0 until iface.endpointCount) {
                        val ep = iface.getEndpoint(i)
                        if (ep.type == android.hardware.usb.UsbConstants.USB_ENDPOINT_XFER_BULK && ep.direction == android.hardware.usb.UsbConstants.USB_DIR_OUT) {
                            outEndpoint = ep
                            break
                        }
                    }
                    outEndpoint?.let { ep ->
                        conn.bulkTransfer(ep, data, data.size, 5000)
                    }
                }
            }
            ConnectionType.NONE -> {}
        }
    }

    fun printText(text: String): Boolean {
        if (!isConnected()) return false
        return try {
            sendBytes(text.toByteArray())
            sendBytes(ESC_POS_CUT.toByteArray())
            true
        } catch (e: Exception) {
            false
        }
    }
}
