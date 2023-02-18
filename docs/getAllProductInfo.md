# inAppPurchases.getAllProductInfo(productIds)

## Usage:
```js
inAppPurchases.getAllProductInfo(productIds).then(function (products) {
    // access product information
    // update your app to show store pricing
}).catch (function(err){
    // view or handle error messages
});
```
<p align="center">
<img src="price_buy_button.png" alt="show price listings" width="300" align="center" />
</p>

## Description:
 - get a list of the products found on the app store for your app
 - use on the start up of the device (`onDeviceReady`) to edit UI and include prices from the app store

## Parameters:
` - list of product ids `

## Returns:
Array of product details,
Each product detail contains information that was set in the store (Google Play or Appstore):
```js
products[i]["productId"]    // the product id
products[i]["title"]        // name of the product
products[i]["description"]  // description of the product
products[i]["price"]        // price of the product with currency, gets the best "offer" available
products[i]["priceAsDecimal"] // price of the product as number
products[i]["priceRaw"]     // price of the product as a number string
products[i]["country"]      // country of the pricing information, iOS only
products[i]["currency"]     // price units 
products[i]["introductoryPrice"] //contains an array with more information about subscription introductory free trials or other offers (price, priceRaw, subscriptionPeriod, etc.)
products[i]["introductoryPriceSupported"] //introductoryPrice price offers are supported for this account currently in their billing country
products[i]["introductoryOriginalPrice"] //contains the original subsription price before the introductory offer was applied
products[i]["introductoryOriginalPriceAsDecimal"] //contains the original subsription price as a number, before the introductory offer was applied
```

## Example
```js
//doing this after deviceready event to get product prices and restore purchases
function populatePrices_afterDeviceReady(){
    inAppPurchases.getAllProductInfo(productIds).then( function (products) {
        //handle
        for (var i=0; i<products.length; i++){
            if (products[i]["productId"] == ad_product_id){
                //show the price that was set in the stores
                var buy_amount_elem = document.getElementById("buy_amount");
                buy_amount_elem.innerHTML = products[i].price;
            }
        }
        return inAppPurchases.restorePurchases();
    }).then( function(purchases){
        //restore bought purchases
        for (var i=0; i<purchases.length; i++){
            if (purchases[i]["productId"] == ad_product_id){
                //bought the removes ads purchase
                if (purchases[i]["pending"]) continue;
                if (!purchases[i]["completed"]) inAppPurchases.completePurchase(purchases[i]["productId"])
                    .catch(function(err){ });
                var buy_elem = document.getElementById("buy_button");
                buy_elem.parentElement.removeChild(buy_elem);
                removeAds();
            }
        }
    }).catch( function(err) {
        console.log("price won't be updated, try again later if connection issue or debug error" + JSON.stringify(err));
    });
}
```

## Tips:

- call from `onDeviceReady` to load products and prices into your app
- in *iOS this must be called before* the other calls
- call this again if it fails

<hr/>

<p align="center">

[go to main](../README.md#plugin-usage)

</p>
