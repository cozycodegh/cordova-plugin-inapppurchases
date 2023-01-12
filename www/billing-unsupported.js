/** cordova-plugin-inapppurchases MIT © 2023 cozycode.ca  **/
/*!
 * Author: Alex Disler (alexdisler.com)
 * github.com/alexdisler/cordova-plugin-inapppurchase
 * Licensed under the MIT license. Please see README for more information.
 */
var utils = {};
utils.errors = {
  101: 'invalid argument - productIds must be an array of strings',
  102: 'invalid argument - productId must be a string',
  103: 'invalid argument - product type must be a string',
  104: 'invalid argument - receipt must be a string of a json',
  105: 'invalid argument - signature must be a string'
};
utils.validArrayOfStrings = function (val) {
  return val && Array.isArray(val) && val.length > 0 && !val.find(function (i) {
    return !i.length || typeof i !== 'string';
  });
};
utils.validString = function (val) {
  return val && val.length && typeof val === 'string';
};
utils.emptyiOSPurchase = function (productId){
    return {
    productId: productId,
    //productType: val.productType,
    purchaseTime: null,
    purchaseId: null,
    quantity: null,
    verified: false,
    pending: null,
    completed: null,
    reciept: null
    };
}

/*!
 * Author: Alex Disler (alexdisler.com)
 * github.com/alexdisler/cordova-plugin-inapppurchase
 * Licensed under the MIT license. Please see README for more information.
 */
var inAppPurchases = {
  utils: utils,
  purchases: {}
};

var nativeCall = function nativeCall(name) {
    return Promise.reject("not implemented for this platform - iOS and Android only")
};

/** 2023 modifications: MIT © cozycode.ca **/
inAppPurchases.getAllProductInfo = function(productIds){
    try {
        return new Promise(function (resolve, reject) {
            if (!inAppPurchases.utils.validArrayOfStrings(productIds)) {
                reject(new Error(inAppPurchases.utils.errors[101]));
            } else {
                //console.log("chunking");
                //console.log(productIds);
                //console.log(utils);
                //return utils.chunk(productIds, utils.max_product_ids).reduce(function (promise, productIds) {
                    //console.log(productIds);
                    //return promise.then(function (result) {
                        //console.log(result);
                        //console.log("calling java");
                return nativeCall('billingGetAllProductInfo', [productIds]).then(function (products) {
                    if (!products || !products.products) resolve([]);
                    else {
                        arr = products.products.map(function (val) {
                                return {
                                productId: val.productId,
                                title: val.title,
                                description: val.description,
                                price: (val.introductoryPriceSupported == 1 && val.introductoryPrice && val.introductoryPrice.price) ? val.introductoryPrice.price : val.price,
                                priceRaw: val.priceRaw,
                                priceAsDecimal: val.priceAsDecimal,
                                currency: val.currency,
                                country: val.country,
                                introductoryPrice: val.introductoryPrice,
                                introductoryPriceSupported: val.introductoryPriceSupported == 1,
                                introductoryOriginalPrice: val.price
                                };
                            });
                        //console.log(JSON.stringify(arr));
                        return resolve(arr);
                    }
                })["catch"](reject);
                    //});
                //})["catch"](reject);
            }
        });
    } catch (err){
        return Promise.reject("Billing js error: "+err);
    }
}

inAppPurchases.getPurchases = function(){
    return nativeCall('billingRestorePurchases').then( function(purchases){
        try {
            arr = [];
            if (purchases && purchases.transactions){
                var arr = purchases.transactions.map(function (val) {
                    return {
                    productId: val.productId,
                    //productType: val.productType,
                    purchaseTime: val.purchaseTime,
                    purchaseId: val.purchaseId,
                    quantity: val.quantity,
                    verified: val.verified == 1,
                    pending: val.pending == 1,
                    completed: val.pending == 0
                    };
                });
            }
            return arr;
        } catch (err) {
            return Promise.reject("Billing js error: "+err);
        }
    });
}
    
inAppPurchases.restorePurchases = function(){
    return nativeCall('billingRestorePurchases').then( function(purchases){
        try {
            arr = [];
            if (purchases && purchases.transactions){
                var arr = purchases.transactions.map(function (val) {
                    return {
                    productId: val.productId,
                    //productType: val.productType,
                    purchaseTime: val.purchaseTime,
                    purchaseId: val.purchaseId,
                    quantity: val.quantity,
                    verified: val.verified == 1,
                    pending: val.pending == 1,
                    completed: val.pending == 0
                    };
                });
            }
            return arr;
        } catch (err) {
            return Promise.reject("Billing js error: "+err);
        }
    });
}

inAppPurchases.purchase = function (productId){
    return new Promise(function (resolve, reject) {
        try {
            if (!inAppPurchases.utils.validString(productId)) {
                reject(new Error(inAppPurchases.utils.errors[102]));
            } else {
                nativeCall('billingPurchase',[productId]).then(function (res) {
                    var purchase = {
                        productId: res.productId,
                            //productType: res.productType,
                        purchaseTime: res.purchaseTime,
                        purchaseId: res.purchaseId,
                        quantity: res.quantity,
                        verified: res.verified == 1,
                        pending: res.pending == 1,
                        completed: res.pending == 0,
                        receipt: res.receipt
                    };
                    inAppPurchases.purchases[purchase.productId] = purchase;
                    return resolve(purchase);
                })["catch"](reject);
            }
        } catch (err) {
            return Promise.reject("Billing js error: "+err);
        }
    });
}
    
inAppPurchases.completePurchase = function (productId){
    return Promise.resolve(inAppPurchases.getPrevPurchase(productId));
}

inAppPurchases.getPrevPurchase = function (productId){
    if (!inAppPurchases.purchases[productId]) return inAppPurchases.utils.emptyiOSPurchase(productId);
    return inAppPurchases.purchases[productId];
}

inAppPurchases.getReciept = function (productId){
    return new Promise(function (resolve, reject) {
        if (!inAppPurchases.utils.validString(productId)) {
            reject(new Error(inAppPurchases.utils.errors[102]));
        } else {
            nativeCall('bilingGetReceipt',[productId]).then(function (res) {
                resolve(res);
            })["catch"](reject);
        }
    });
}

module.exports = inAppPurchases;
