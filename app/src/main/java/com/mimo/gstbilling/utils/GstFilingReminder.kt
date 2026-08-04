package com.mimo.gstbilling.utils

import com.mimo.gstbilling.data.local.entity.InvoiceEntity

object GstFilingReminder {

    data class FilingDeadline(
        val returnType: String,
        val dueDate: String,
        val daysRemaining: Int,
        val status: String,
        val description: String,
        val period: String
    )

    fun getUpcoming(): List<FilingDeadline> {
        val cal = java.util.Calendar.getInstance()
        val now = cal.time
        val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US)
        val deadlines = mutableListOf<FilingDeadline>()

        cal.set(java.util.Calendar.DAY_OF_MONTH, 11)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
        cal.set(java.util.Calendar.MINUTE, 59)
        var gstr1Due = cal.time
        if (gstr1Due.before(now)) {
            cal.add(java.util.Calendar.MONTH, 1)
            gstr1Due = cal.time
        }
        val daysGstr1 = ((gstr1Due.time - now.time) / (24 * 60 * 60 * 1000)).toInt()
        deadlines.add(FilingDeadline("GSTR-1", dateFormat.format(gstr1Due), daysGstr1,
            if (daysGstr1 <= 3) "URGENT" else if (daysGstr1 <= 7) "UPCOMING" else "NORMAL",
            "Outward supplies return", "Monthly"))

        cal.set(java.util.Calendar.DAY_OF_MONTH, 20)
        var gstr3bDue = cal.time
        if (gstr3bDue.before(now)) {
            cal.add(java.util.Calendar.MONTH, 1)
            gstr3bDue = cal.time
        }
        val daysGstr3b = ((gstr3bDue.time - now.time) / (24 * 60 * 60 * 1000)).toInt()
        deadlines.add(FilingDeadline("GSTR-3B", dateFormat.format(gstr3bDue), daysGstr3b,
            if (daysGstr3b <= 3) "URGENT" else if (daysGstr3b <= 7) "UPCOMING" else "NORMAL",
            "Summary return with tax payment", "Monthly"))

        cal.set(java.util.Calendar.MONTH, java.util.Calendar.JULY)
        cal.set(java.util.Calendar.DAY_OF_MONTH, 31)
        var gstr9Due = cal.time
        if (gstr9Due.before(now)) {
            cal.add(java.util.Calendar.YEAR, 1)
            gstr9Due = cal.time
        }
        val daysGstr9 = ((gstr9Due.time - now.time) / (24 * 60 * 60 * 1000)).toInt()
        deadlines.add(FilingDeadline("GSTR-9", dateFormat.format(gstr9Due), daysGstr9,
            if (daysGstr9 <= 30) "UPCOMING" else "NORMAL",
            "Annual return", "Annual"))

        return deadlines
    }
}
