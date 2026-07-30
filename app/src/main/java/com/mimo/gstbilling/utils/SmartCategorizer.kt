package com.mimo.gstbilling.utils

object SmartCategorizer {

    private val categoryKeywords = mapOf(
        "Rent" to listOf("rent", "office rent", "shop rent", "building rent", "house rent"),
        "Salary" to listOf("salary", "wages", "staff salary", "employee", "payment to staff"),
        "Travel" to listOf("travel", "taxi", "uber", "ola", "flight", "train", "fuel", "petrol", "diesel", "parking", "toll"),
        "Food" to listOf("food", "tea", "coffee", "lunch", "dinner", "breakfast", "snacks", "refreshment", "hotel", "restaurant", "mess"),
        "Office Supplies" to listOf("office", "stationery", "printer", "paper", "toner", "ink", "pen", "pencil", "file", "folder"),
        "Utilities" to listOf("electricity", "water", "phone", "mobile", "internet", "wifi", "broadband", "gas", "power"),
        "Marketing" to listOf("marketing", "advertisement", "ad", "\u5ba3\u4f20", "promotion", "banner", "flex", "hoarding"),
        "Professional Services" to listOf("ca", "chartered accountant", "lawyer", "legal", "consultant", "audit", "accounting"),
        "Maintenance" to listOf("repair", "maintenance", "ac repair", "plumbing", "electrician", "painting"),
        "Insurance" to listOf("insurance", "premium", "policy", "lic", "health insurance"),
        "Tax" to listOf("tax", "gst", "income tax", "professional tax", "tds")
    )

    private val hsnItemMap = mapOf(
        "tea" to Pair("0902", "Tea"),
        "coffee" to Pair("0902", "Tea"),
        "beverage" to Pair("0902", "Tea"),
        "sugar" to Pair("1101", "Cereal flour"),
        "atta" to Pair("1101", "Cereal flour"),
        "flour" to Pair("1101", "Cereal flour"),
        "rice" to Pair("1006", "Rice"),
        "milk" to Pair("0401", "Milk"),
        "bread" to Pair("1905", "Bread"),
        "soap" to Pair("3401", "Soap"),
        "detergent" to Pair("3401", "Soap"),
        "pen" to Pair("9608", "Pens"),
        "pencil" to Pair("9608", "Pens"),
        "paper" to Pair("4819", "Paper"),
        "mobile" to Pair("8517", "Telephone sets"),
        "phone" to Pair("8517", "Telephone sets"),
        "laptop" to Pair("8471", "Data processing machines"),
        "computer" to Pair("8471", "Data processing machines"),
        "shirt" to Pair("6105", "Men's shirts"),
        "t-shirt" to Pair("6105", "Men's shirts"),
        "pants" to Pair("6203", "Men's suits"),
        "trousers" to Pair("6203", "Men's suits"),
        "shoes" to Pair("6403", "Footwear"),
        "medicine" to Pair("3004", "Medicaments"),
        "tablet" to Pair("3004", "Medicaments"),
        "oil" to Pair("1507", "Vegetable oil"),
        "ghee" to Pair("1507", "Vegetable oil"),
        "spice" to Pair("0910", "Spices"),
        "masala" to Pair("0910", "Spices"),
        "biscuit" to Pair("1905", "Biscuits"),
        "cookie" to Pair("1905", "Biscuits"),
        "chocolate" to Pair("1806", "Chocolate"),
        "water bottle" to Pair("2201", "Mineral water"),
        "cement" to Pair("2523", "Cement"),
        "steel" to Pair("7213", "Steel bars"),
        "iron" to Pair("7213", "Steel bars")
    )

    private val taxRateByHsnPrefix = listOf(
        listOf("04", "07", "09", "10", "11") to 5.0,
        listOf("1905", "2106", "2201", "2202") to 5.0,
        listOf("0910") to 5.0,
        listOf("61", "62", "63", "64") to 5.0,
        listOf("84", "85") to 18.0,
        listOf("2523") to 28.0
    )

    fun categorizeExpense(description: String): String {
        val lowerDesc = description.lowercase().trim()
        for ((category, keywords) in categoryKeywords) {
            for (keyword in keywords) {
                if (lowerDesc.contains(keyword.lowercase())) {
                    return category
                }
            }
        }
        return "Other"
    }

    fun suggestHsnCode(itemName: String): Pair<String, String> {
        val lowerName = itemName.lowercase().trim()
        for ((key, value) in hsnItemMap) {
            if (lowerName.contains(key)) {
                return value
            }
        }
        return Pair("9999", "General goods")
    }

    fun getTaxRate(hsnCode: String): Double {
        for ((prefixes, rate) in taxRateByHsnPrefix) {
            for (prefix in prefixes) {
                if (hsnCode.startsWith(prefix)) {
                    return rate
                }
            }
        }
        return 18.0
    }

    fun getCategoryColor(category: String): Long {
        return when (category) {
            "Rent" -> 0xFFE91E63
            "Salary" -> 0xFF9C27B0
            "Travel" -> 0xFF2196F3
            "Food" -> 0xFFFF9800
            "Office Supplies" -> 0xFF4CAF50
            "Utilities" -> 0xFF00BCD4
            "Marketing" -> 0xFFFF5722
            "Professional Services" -> 0xFF3F51B5
            "Maintenance" -> 0xFF795548
            "Insurance" -> 0xFF607D8B
            "Tax" -> 0xFFF44336
            else -> 0xFF9E9E9E
        }
    }
}
