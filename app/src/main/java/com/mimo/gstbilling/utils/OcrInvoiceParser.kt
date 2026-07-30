package com.mimo.gstbilling.utils

data class OcrItem(
    val name: String = "",
    val quantity: Double = 0.0,
    val rate: Double = 0.0,
    val amount: Double = 0.0
)

data class OcrInvoiceResult(
    val customerName: String = "",
    val gstin: String = "",
    val invoiceNumber: String = "",
    val items: List<OcrItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val date: String = "",
    val rawText: String = ""
)

object OcrInvoiceParser {

    private val gstinRegex = Regex("""[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}""")

    private val invoiceNumberPatterns = listOf(
        Regex("""(?i)(?:invoice|inv|bill)\s*(?:no|number|#|\.|:)\s*[:\-]?\s*(\S+)"""),
        Regex("""(?i)(?:invoice|inv|bill)\s+number\s*[:\-]?\s*(\S+)"""),
        Regex("""(?i)(?:invoice|inv|bill)\s*[:\-]\s*(\S+)""")
    )

    private val datePatterns = listOf(
        Regex("""\b(\d{1,2}[/-]\d{1,2}[/-]\d{2,4})\b"""),
        Regex("""\b(\d{1,2}\.\d{1,2}\.\d{2,4})\b"""),
        Regex("""(?i)(?:date|dated?)\s*[:\-]?\s*(\d{1,2}[/-]\d{1,2}[/-]\d{2,4})"""),
        Regex("""(?i)(?:date|dated?)\s*[:\-]?\s*(\d{1,2}\.\d{1,2}\.\d{2,4})"""),
        Regex("""(?i)(?:date|dated?)\s*[:\-]?\s*(\d{1,2}\s+\w+\s+\d{2,4})""")
    )

    private val totalPatterns = listOf(
        Regex("""(?i)(?:grand\s+)?total\s*[:\-]?\s*(?:rs\.?|inr|₹|\.)?\s*([\d,]+\.?\d*)"""),
        Regex("""(?i)(?:net\s+)?amount\s*[:\-]?\s*(?:rs\.?|inr|₹|\.)?\s*([\d,]+\.?\d*)"""),
        Regex("""(?i)amount\s+(?:payable|due)\s*[:\-]?\s*(?:rs\.?|inr|₹|\.)?\s*([\d,]+\.?\d*)"""),
        Regex("""(?i)(?:grand\s+)?total\s+(?:amount|value)\s*[:\-]?\s*(?:rs\.?|inr|₹|\.)?\s*([\d,]+\.?\d*)""")
    )

    private val customerPatterns = listOf(
        Regex("""(?i)(?:to|bill\s+to|party|customer|sold\s+to|buyer|consignee)\s*[:\-]?\s*(.+)"""),
        Regex("""(?i)(?:m/s|messrs?\.?)\s*[:\-]?\s*(.+)""")
    )

    private val amountRegex = Regex("""(?:rs\.?|inr|₹|\.)\s*([\d,]+\.?\d*)""")
    private val numberRegex = Regex("""[\d,]+\.?\d*""")

    fun parseInvoiceText(text: String): OcrInvoiceResult {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val fullText = lines.joinToString("\n")

        val gstin = extractGstin(fullText)
        val invoiceNumber = extractInvoiceNumber(fullText, lines)
        val date = extractDate(fullText, lines)
        val totalAmount = extractTotalAmount(fullText, lines)
        val customerName = extractCustomerName(fullText, lines)
        val items = extractItems(lines)

        return OcrInvoiceResult(
            customerName = customerName,
            gstin = gstin,
            invoiceNumber = invoiceNumber,
            items = items,
            totalAmount = totalAmount,
            date = date,
            rawText = text
        )
    }

    private fun extractGstin(text: String): String {
        val match = gstinRegex.find(text)
        return match?.value ?: ""
    }

    private fun extractInvoiceNumber(text: String, lines: List<String>): String {
        for (pattern in invoiceNumberPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1].trimEnd(',', ';', '.')
            }
        }
        return ""
    }

    private fun extractDate(text: String, lines: List<String>): String {
        for (pattern in datePatterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        return ""
    }

    private fun extractTotalAmount(text: String, lines: List<String>): Double {
        for (pattern in totalPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                val amountStr = match.groupValues[1].replace(",", "")
                return amountStr.toDoubleOrNull() ?: 0.0
            }
        }
        return 0.0
    }

    private fun extractCustomerName(text: String, lines: List<String>): String {
        for (pattern in customerPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                var name = match.groupValues[1].trim()
                if (name.length > 80) {
                    name = name.substring(0, 80)
                }
                if (name.isNotBlank() && name.length > 2) {
                    return name
                }
            }
        }
        return ""
    }

    private fun extractItems(lines: List<String>): List<OcrItem> {
        val items = mutableListOf<OcrItem>()

        for (line in lines) {
            val trimmed = line.trim()

            if (trimmed.lowercase().contains("total") ||
                trimmed.lowercase().contains("subtotal") ||
                trimmed.lowercase().contains("grand") ||
                trimmed.lowercase().contains("sgst") ||
                trimmed.lowercase().contains("cgst") ||
                trimmed.lowercase().contains("igst") ||
                trimmed.lowercase().contains("tax") ||
                trimmed.lowercase().contains("discount") ||
                trimmed.lowercase().contains("freight") ||
                trimmed.lowercase().contains("round off")
            ) {
                continue
            }

            val item = tryParseItemLine(trimmed)
            if (item != null && item.name.isNotBlank()) {
                items.add(item)
            }
        }

        return items
    }

    private fun tryParseItemLine(line: String): OcrItem? {
        val qtyRatePattern = Regex("""(\d+(?:\.\d+)?)\s*[xX×*]\s*(\d+(?:\.\d+)?)""")
        val qtyRateMatch = qtyRatePattern.find(line)

        if (qtyRateMatch != null) {
            val qty = qtyRateMatch.groupValues[1].toDoubleOrNull() ?: 0.0
            val rate = qtyRateMatch.groupValues[2].toDoubleOrNull() ?: 0.0
            val name = line.substring(0, qtyRateMatch.range.first).trim()
                .replace(Regex("""^\d+\.\s*"""), "")
                .trim()
                .trimEnd(',', ';')
            val amountMatches = amountRegex.findAll(line).toList()
            val amount = if (amountMatches.isNotEmpty()) {
                amountMatches.last().groupValues[1].replace(",", "").toDoubleOrNull() ?: (qty * rate)
            } else {
                qty * rate
            }
            return OcrItem(name = name, quantity = qty, rate = rate, amount = amount)
        }

        val parts = trimmedSplit(line)
        if (parts.size >= 3) {
            val nameParts = mutableListOf<String>()
            var foundNumbers = false
            var quantity = 0.0
            var rate = 0.0
            var amount = 0.0

            for (part in parts) {
                if (part.matches(Regex("""[\d,]+\.?\d*"""))) {
                    foundNumbers = true
                    val numVal = part.replace(",", "").toDoubleOrNull() ?: 0.0
                    if (quantity == 0.0) {
                        quantity = numVal
                    } else if (rate == 0.0) {
                        rate = numVal
                    } else if (amount == 0.0) {
                        amount = numVal
                    }
                } else if (!foundNumbers) {
                    nameParts.add(part)
                }
            }

            if (quantity > 0 && rate > 0) {
                val name = nameParts.joinToString(" ").trimEnd(',', ';')
                if (amount == 0.0) amount = quantity * rate
                return OcrItem(name = name, quantity = quantity, rate = rate, amount = amount)
            }
        }

        return null
    }

    private fun trimmedSplit(line: String): List<String> {
        return line.split(Regex("""\s{2,}|\t|(?<=\d)\s+(?=\d)|(?<=\d)\s+(?=Rs|INR|₹)"""))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}
