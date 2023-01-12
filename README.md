# Cordova-Plugin-InAppPurchases

***Cordova*** plugin to add ***in-app purchases*** (and ***subscriptions***) into an app. Use javascript to view, make, and complete purchases.

Updated __2023__ for Cordova with current:
__Android__: *Billing Library 5* (released May 2022) (verifies receipts internally)
__iOS__: *StoreKit* (not StoreKit 2, released 2021, which is only available for iOS 15 and up) (does not verify receipts currently - but returns them)
__Amazon Fire__: compatible, but untested 
__Browser__: not implemented
__Windows__: not implemented 

If you notice any issues, submit here: [github issues](https://github.com/cozycodegh/cordova-plugin-inapppurchases/issues)
Based on the original plugin working for many years made by Alex Disler, it is out of date for the latest apps: [`cordova-plugin-inapppurchase`](https://github.com/AlexDisler/cordova-plugin-inapppurchase)
Forked from another plugin, which is kept updated as well, if this plugin is broken, try this plugin: [`cordova-plugin-inapppurchase2`](https://github.com/wccrawford/cordova-plugin-inapppurchase-2)

# PLUGIN USAGE

Add these calls inside of your cordova javascript to make in-app purchases and subscriptions:

[`inAppPurchases.getAllProductInfo(productIds)`](docs/getAllProductInfo.md)
    - get a list of the products found on the app store for your app 
    - use on the start up of the device (`onDeviceReady`) to edit UI and include prices from the app store
    - on iOS make sure to use this when the device loads in `onDeviceReady` before calling the other requests
    ![show price listings](docs/price_buy_button.png)
<!--
[`inAppPurchases.getPurchases()`](docs/getPurchases.md)
    - get a list of the purchased products from the app store 
    - use when device first loads in onDeviceReady, and onResume to update which products are currently owned
    - same as restore purchases, but does not prompt to log in
    - will not restore if the user is not logged in (Apple?), Apple recommends this as default, so the app does not prompt for login on each startup, when a user wants to stay logged out-->

[`inAppPurchases.restorePurchases()`](docs/restorePurchases.md)
    - get a list of the purchased products from the app store 
    - use when device first loads in `onDeviceReady`, and `onResume` to update which products are currently owned
    - create a button, (for example in a settings section), called `restore` so that users can call this function themselves to restore their products, Apple requires a button
    ![put a restore button somewhere](docs/restore_button.png)
    
    <!--- prompts for login if they are not logged in-->

[`inAppPurchases.purchase(productId)`](docs/purchase.md)
    - make a purchase (a one-time purchase or a subscription)
    - when this is sucessful (and not pending), call completePurchase
    - you can deliver the paid content right away after the first purchase call and revert it if the completePurchase fails, or deliver the paid content after the completePurchase goes through

[`inAppPurchase.completePurchase(productId,consume)`](docs/completePurchase.md)
    - confirm the purchase
    - use right after purchasing
    - this "consumes" consumable products (able to buy it again) and "acknowledges" the products (if they are not acknowledged in Android, the purchase will be revoked after a few days)
    - FYI: this function is only needed on Android, as on iOS purchases are consumed during the inAppPurcahses.purchase(productId) call


Calls should be done after cordova's `onDeviceReady` function is called.
The plugin creates an `inAppPurchases` object for you to make calls to the store.
The object is a promise-based API, click on each for more information on using the function.

# CREATE AN APP WITH CORDOVA

How to create a cordova app: 
> `cordova create directory_name com.your_name_or_company.your_app_name` 
![cordova app](docs/cordova.png)

Cordova is an open source project maintened by Apache that lets you make apps written in HTML, css, and javascript. You can create cross-platform apps to publish on the Google Play Store for Android, and the Appstore for iOS, macOS, and apple devices.

# ADD IN APP PURCHASES

1. Add the plugin
The spelling of this plugin is cordova-plugin-inapppurchases with an s, 
    (more plugins we've tested but were not fully working December 2022: 
        - `cordova-plugin-inapppurchase` (first plugin worked for years)
        - `cordova-plugin-inapppurchase-2` (active and almost fully working plugin by Wccrawford)
        - `cordova-plugin-purchase` (not updated to work in latest Android and iOS) 
> `cordova plugin add cordova-plugin-inapppurchases`

2. In the Google Play Store and iOS app store, add your in app purchases and subscriptions (set product ids here, for example, com.name_or_company.app_name.purchase_product_name) (see testing tips below)

2.5 For the Google Play Store, retreive your *Android Billing Key* to use in the app
    Create a file within your cordova project at: www/manifest.json
    Contents:
    `{ "play_store_key": "<Base64-encoded public key from the Google Play Store>" }`
    
3. Add buy buttons (or any UI you make) to your Cordova html code

4. Add calls to the plugin with the `inAppPurchases` object in your Cordova javascript code

5. Test that your in-app purchases are working with test accounts

<!--# Example code
coming soon
-->

# Testing Tips

see [common error messages](docs/errors.md) to handle other results 

## Android

1. create a test track (closed works) and add an email address with a google play account
2. after adding the plugin, *build an aab file* and add it to the Google Play test track, this enables the google play library for your app
3. get your **Android billing key** in the monetization section to add to the *www/manifest.json* 
4. make "products" with ids on the google play store in the in-app products and subscriptions sections
5. log into your test account on your device and make purchases
    * use a real android device to test, simulators don't support billing (see the error message)
    * Google Store doesn't make a difference between consumable and non-consumable in-app purchases, don't call inAppPurchases(product_id,is_consumable); with is_comsumable=true for non-consumable purchases because it will consume them too 

## iOS

1. create in app-purchases and subscriptions on App Store Connect, takes up to an hour to enable after creating the first one
2. to enable testing, create a build and upload it to App Store Connect (once you do it for one app, you may not need to do this step again) 
3. create test accounts in the TestFlight section of App Store Connect 
4. now you can log in to test accounts in the test device's settings, or the prompt that comes up in a simulator and test out in-app purchases
    * you should be able to test without having to submit your app for review

# Future Improvements
[ ] add internal reciept checking for iOS
[ ] update to also include Storekit 2
[ ] support Windows apps
[x] tested working 2023 with Android Billing library 5 for consumables, non-consumables, and subscriptions
[x] tested working 2023 with StoreKit for consumables, non-consumables, and subscriptions

# Open Source License

## Additional Copyright for new plugin distribution
MIT Licensed (MIT)

Copyright © 2023 cozycode.ca

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do  so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all  copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## Original Copyright

The MIT License

Copyright (c) 2016, Alex Disler

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


