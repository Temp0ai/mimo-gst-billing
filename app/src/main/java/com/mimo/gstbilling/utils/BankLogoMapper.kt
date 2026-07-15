package com.mimo.gstbilling.utils

import com.mimo.gstbilling.R

object BankLogoMapper {

    data class BankInfo(
        val name: String,
        val logoResId: Int,
        val shortName: String
    )

    private val bankMap = listOf(
        BankInfo("State Bank of India", R.drawable.bank_logo_sbi, "SBI"),
        BankInfo("SBI", R.drawable.bank_logo_sbi, "SBI"),
        BankInfo("HDFC Bank", R.drawable.bank_logo_hdfc, "HDFC"),
        BankInfo("HDFC", R.drawable.bank_logo_hdfc, "HDFC"),
        BankInfo("ICICI Bank", R.drawable.bank_logo_icici, "ICICI"),
        BankInfo("ICICI", R.drawable.bank_logo_icici, "ICICI"),
        BankInfo("Axis Bank", R.drawable.bank_logo_axis, "Axis"),
        BankInfo("Axis", R.drawable.bank_logo_axis, "Axis"),
        BankInfo("Punjab National Bank", R.drawable.bank_logo_pnb, "PNB"),
        BankInfo("PNB", R.drawable.bank_logo_pnb, "PNB"),
        BankInfo("Bank of Baroda", R.drawable.bank_logo_bob, "BOB"),
        BankInfo("BOB", R.drawable.bank_logo_bob, "BOB"),
        BankInfo("Baroda Bank", R.drawable.bank_logo_bob, "BOB"),
        BankInfo("Canara Bank", R.drawable.bank_logo_canara, "Canara"),
        BankInfo("Canara", R.drawable.bank_logo_canara, "Canara"),
        BankInfo("Union Bank of India", R.drawable.bank_logo_ubi, "UBI"),
        BankInfo("Union Bank", R.drawable.bank_logo_ubi, "UBI"),
        BankInfo("UBI", R.drawable.bank_logo_ubi, "UBI"),
        BankInfo("IndusInd Bank", R.drawable.bank_logo_indusind, "IndusInd"),
        BankInfo("IndusInd", R.drawable.bank_logo_indusind, "IndusInd"),
        BankInfo("Kotak Mahindra Bank", R.drawable.bank_logo_kotak, "Kotak"),
        BankInfo("Kotak", R.drawable.bank_logo_kotak, "Kotak"),
        BankInfo("IDBI Bank", R.drawable.bank_logo_idbi, "IDBI"),
        BankInfo("IDBI", R.drawable.bank_logo_idbi, "IDBI"),
        BankInfo("Central Bank of India", R.drawable.bank_logo_central, "Central"),
        BankInfo("Central Bank", R.drawable.bank_logo_central, "Central"),
        BankInfo("Bank of India", R.drawable.bank_logo_central, "BOI"),
        BankInfo("Indian Bank", R.drawable.bank_logo_central, "Indian"),
        BankInfo("Indian Overseas Bank", R.drawable.bank_logo_central, "IOB"),
        BankInfo("UCO Bank", R.drawable.bank_logo_central, "UCO"),
        BankInfo("Punjab and Sind Bank", R.drawable.bank_logo_pnb, "PSB"),
        BankInfo("Bank of Maharashtra", R.drawable.bank_logo_central, "BoM"),
        BankInfo("Corporation Bank", R.drawable.bank_logo_central, "Corp"),
        BankInfo("Federal Bank", R.drawable.bank_logo_indusind, "Federal"),
        BankInfo("South Indian Bank", R.drawable.bank_logo_indusind, "SIB"),
        BankInfo("City Union Bank", R.drawable.bank_logo_indusind, "CUB"),
        BankInfo("Karur Vysya Bank", R.drawable.bank_logo_indusind, "KVB"),
        BankInfo("DCB Bank", R.drawable.bank_logo_indusind, "DCB"),
        BankInfo("Tamilnad Mercantile Bank", R.drawable.bank_logo_indusind, "TMB"),
        BankInfo("Yes Bank", R.drawable.bank_logo_indusind, "Yes"),
        BankInfo("RBL Bank", R.drawable.bank_logo_indusind, "RBL"),
        BankInfo("Dhanlaxmi Bank", R.drawable.bank_logo_indusind, "DLB"),
        BankInfo("Karnataka Bank", R.drawable.bank_logo_indusind, "KBL"),
        BankInfo("South Indian Bank", R.drawable.bank_logo_indusind, "SIB")
    )

    fun getBankLogo(bankName: String?): Int {
        if (bankName.isNullOrBlank()) return R.drawable.bank_logo_generic

        val normalizedName = bankName.trim().lowercase()

        for (bank in bankMap) {
            if (bank.name.lowercase() == normalizedName ||
                bank.shortName.lowercase() == normalizedName ||
                normalizedName.contains(bank.name.lowercase()) ||
                bank.name.lowercase().contains(normalizedName)) {
                return bank.logoResId
            }
        }

        return R.drawable.bank_logo_generic
    }

    fun getBankShortName(bankName: String?): String {
        if (bankName.isNullOrBlank()) return "Bank"

        val normalizedName = bankName.trim().lowercase()

        for (bank in bankMap) {
            if (bank.name.lowercase() == normalizedName ||
                bank.shortName.lowercase() == normalizedName ||
                normalizedName.contains(bank.name.lowercase()) ||
                bank.name.lowercase().contains(normalizedName)) {
                return bank.shortName
            }
        }

        return bankName.take(6)
    }
}
