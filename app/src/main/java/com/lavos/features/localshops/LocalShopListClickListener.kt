package com.lavos.features.localshops

/**
 * Created by riddhi on 2/1/18.
 */
interface LocalShopListClickListener {

    fun visitShop(shop: Any)

    fun onCallClick(shop: Any)

    fun onOrderClick(shop: Any)

    fun onLocationClick(shop: Any)

    fun onQuationClick(shop: Any)
}