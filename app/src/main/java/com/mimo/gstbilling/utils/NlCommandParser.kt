package com.mimo.gstbilling.utils

enum class CommandType {
    CREATE_INVOICE, CREATE_EXPENSE, VIEW_REPORT, VIEW_PARTY, VIEW_ITEMS, SEARCH, HELP, UNKNOWN
}

data class NlCommandResult(
    val command: CommandType,
    val params: Map<String, String> = emptyMap(),
    val confidence: Float = 0f,
    val displayText: String = ""
)

object NlCommandParser {

    fun parseCommand(input: String): NlCommandResult {
        val lower = input.lowercase().trim()
        
        return when {
            lower.matches(Regex(".*(create|new|make|start).*(invoice|bill|sale|receipt).*")) -> {
                val party = extractParty(lower)
                NlCommandResult(CommandType.CREATE_INVOICE, mapOf("party" to party), 0.9f, "Create invoice for $party")
            }
            lower.matches(Regex(".*(add|new|record|enter).*(expense|cost|payment made).*")) -> {
                val amount = extractAmount(lower)
                val desc = extractDescription(lower)
                NlCommandResult(CommandType.CREATE_EXPENSE, mapOf("amount" to amount, "description" to desc), 0.85f, "Add expense: $desc ₹$amount")
            }
            lower.matches(Regex(".*(show|view|open|see|display).*(sale|sales|invoice|invoices).*")) -> {
                NlCommandResult(CommandType.VIEW_REPORT, mapOf("type" to "sales"), 0.9f, "View sales report")
            }
            lower.matches(Regex(".*(show|view|open|see).*(purchase|purchases).*")) -> {
                NlCommandResult(CommandType.VIEW_REPORT, mapOf("type" to "purchases"), 0.9f, "View purchases report")
            }
            lower.matches(Regex(".*(show|view|open|see).*(expense|expenses).*")) -> {
                NlCommandResult(CommandType.VIEW_REPORT, mapOf("type" to "expenses"), 0.9f, "View expenses report")
            }
            lower.matches(Regex(".*(show|view|see).*(stock|inventory|items|products).*")) -> {
                NlCommandResult(CommandType.VIEW_ITEMS, confidence = 0.9f, displayText = "View stock/items")
            }
            lower.matches(Regex(".*(pending|due|outstanding|unpaid).*")) -> {
                NlCommandResult(CommandType.VIEW_REPORT, mapOf("type" to "pending"), 0.85f, "View pending payments")
            }
            lower.matches(Regex(".*(party|customer|client|supplier).*(name|detail|info).*")) -> {
                val name = extractParty(lower)
                NlCommandResult(CommandType.VIEW_PARTY, mapOf("name" to name), 0.8f, "View party: $name")
            }
            lower.matches(Regex("(help|what can you do|commands|how to).*")) -> {
                NlCommandResult(CommandType.HELP, confidence = 1.0f, displayText = "Show available commands")
            }
            else -> {
                NlCommandResult(CommandType.SEARCH, mapOf("query" to input), 0.3f, "Search: $input")
            }
        }
    }

    private fun extractParty(input: String): String {
        val patterns = listOf(
            Regex("for\\s+(.+?)(?:\\s+₹|\\s+rs|\\s+amount|\\s+total|$)"),
            Regex("party\\s+(.+?)(?:\\s+₹|\\s+rs|$)"),
            Regex("customer\\s+(.+?)(?:\\s+₹|\\s+rs|$)")
        )
        for (p in patterns) {
            val match = p.find(input)
            if (match != null) return match.groupValues[1].trim().split(" ").take(3).joinToString(" ")
        }
        return ""
    }

    private fun extractAmount(input: String): String {
        val match = Regex("[₹rs]+\\s*(\\d+[.,]?\\d*)").find(input)
        return match?.groupValues?.get(1) ?: ""
    }

    private fun extractDescription(input: String): String {
        val match = Regex("(?:for|of|towards)\\s+(.+?)(?:\\s+₹|\\s+rs|\\s+amount|$)").find(input)
        return match?.groupValues?.get(1)?.trim() ?: input
    }

    fun getSuggestions(): List<String> = listOf(
        "Create invoice for Preetam",
        "Add expense ₹500 for office supplies",
        "Show sales this month",
        "View pending payments",
        "Show stock items",
        "Add expense ₹200 for tea",
        "Create invoice for Suresh",
        "View expenses report",
        "Help"
    )
}
