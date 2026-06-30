package com.mimo.gstbilling.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object CreateInvoice : Screen("create_invoice")
    object Parties : Screen("parties")
    object PartyDetail : Screen("party_detail/{partyId}") {
        fun createRoute(partyId: Long) = "party_detail/$partyId"
    }
    object Items : Screen("items")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
    object Sales : Screen("sales")
    object Purchases : Screen("purchases")
    object Expenses : Screen("expenses")
    object CashBank : Screen("cash_bank")
    object PartyDetails : Screen("party_details")
    object ItemDetail : Screen("item_detail/{itemId}") {
        fun createRoute(itemId: Long) = "item_detail/$itemId"
    }
    object AddParty : Screen("add_party")
    object AddItem : Screen("add_item")
}
