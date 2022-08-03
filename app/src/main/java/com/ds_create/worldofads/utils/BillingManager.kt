package com.ds_create.worldofads.utils

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.ds_create.worldofads.R

class BillingManager(val act: AppCompatActivity) {
    private var billingClient: BillingClient? = null

    init {
        setUpBillingClient()
    }

    private fun setUpBillingClient() {
        billingClient = BillingClient.newBuilder(act)
            .setListener(getPurchaseListener()).enablePendingPurchases().build()
    }
    
    private fun savePurchase(isPurchased: Boolean) {
        val pref = act.getSharedPreferences(MAIN_PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean(REMOVE_ADS_PREF, isPurchased)
        editor.apply()
    }

    //Тут подключение к PlayMarket
    fun startConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {}

            override fun onBillingSetupFinished(result: BillingResult) {
                getItem()
            }
        }
        )
    }

    //Создается диалог для покупки в PlayMarket
    private fun getItem() {
        val skuList = ArrayList<String>()
        skuList.add(REMOVE_ADS)
        val skuDetails = SkuDetailsParams.newBuilder()
        skuDetails.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        billingClient?.querySkuDetailsAsync(skuDetails.build()) {
                result, list ->
            run {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    if (!list.isNullOrEmpty()) {
                        val billingFlowParams = BillingFlowParams
                            .newBuilder().setSkuDetails(list[0]).build()
                        billingClient?.launchBillingFlow(act, billingFlowParams)
                    }
                }
            }
        }
    }

    //Функция для подтверждения покупки
    private fun nonConsumableItem(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken).build()
                billingClient?.acknowledgePurchase(acParams) {
                    if (it.responseCode == BillingClient.BillingResponseCode.OK) {
                        savePurchase(true)
                        Toast.makeText(act, act.getString(R.string.thanks_for_sale), Toast.LENGTH_SHORT).show()
                    } else {
                        savePurchase(false)
                        Toast.makeText(act, act.getString(R.string.failed_to_realize_purchase), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getPurchaseListener(): PurchasesUpdatedListener {
        return PurchasesUpdatedListener{
            result, list ->
            run {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    list?.get(0)?.let { nonConsumableItem(it) }
                }
            }
        }
    }

    fun closeConnection() {
        billingClient?.endConnection()
    }

    companion object {
        const val REMOVE_ADS = "remove_ads"
        const val REMOVE_ADS_PREF = "remove_ads_pref"
        const val MAIN_PREF = "main_pref"
    }
}