/** cordova-plugin-inapppurchases MIT © 2023 cozycode.ca  **/
/*!
 * Author: Alex Disler (alexdisler.com)
 * github.com/alexdisler/cordova-plugin-inapppurchase
 * Licensed under the MIT license. Please see README for more information.
 */
var utils = {};
utils.errors = {
  'product_id_list' : { 101: 'invalid argument - productIds must be an array of strings'},
  'product_id_string' : { 102: 'invalid argument - productId must be a string' },
  'receipt_jsonstring' : { 103: 'invalid argument - receipt must be a string of a json'},
  'signature_string' : { 104: 'invalid argument - signature must be a string' },
  'replacement_mode_invalid' : { 105: 'invalid argument - unknown replacement mode, choose from inAppPurchases.subscriptionReplacementMode' },
  'billing_js_error' : { 111: 'billing js error' },
  'not_implemented' : { 112: 'not implemented for this platform - iOS and Android only' }
};
utils.getError = function (errn){
    //return new Error(utils.errors[code]);
    var err = utils.errors[errn];
    if (!err) err = { 110: errn };
    var code = Object.keys(err)[0];
    return {
        'code' : code,
        'message' : err[code]
    };
}
utils.getJSError = function (err){
    var e = utils.getError('billing_js_error');
    e['message'] += ": "+err;
    return e;
}
utils.validArrayOfStrings = function (val) {
  return val && Array.isArray(val) && val.length > 0 && !val.find(function (i) {
    return !i.length || typeof i !== 'string';
  });
};
utils.validString = function (val) {
  return val && val.length && typeof val === 'string';
};
utils.validReplacementMode = function (val) {
    return val && typeof val === 'number'
        && Object.values(inAppPurchases.subscriptionReplacementMode).indexOf(val) != -1;
}
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
    reciept: "get the receipt from the initial purchase call"
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
    var args = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : [];
    return new Promise(function (resolve, reject) {
      window.cordova.exec(function (res) {
          resolve(res);
        }, function (err) {
          reject(err);
        }, 'PaymentsPlugin', name, args);
    });
};

/** 2023 modifications: MIT © cozycode.ca **/
inAppPurchases.getAllProductInfo = function(productIds){
    try {
        return new Promise(function (resolve, reject) {
            if (!inAppPurchases.utils.validArrayOfStrings(productIds)) {
                reject(inAppPurchases.utils.getError('product_id_list'));
            } else {
                //return utils.chunk(productIds, utils.max_product_ids).reduce(function (promise, productIds) {
                    //return promise.then(function (result) {
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
                                introductoryPrice: [val.introductoryPrice],
                                introductoryPriceSupported: val.introductoryPriceSupported == 1,
                                introductoryOriginalPrice: val.price,
                                introductoryOriginalPriceAsDecimal: parseFloat(val.price.replace(/^[^0-9]*/,'')) 
                                };
                            });
                        return resolve(arr);
                    }
                })["catch"](reject);
            }
        });
    } catch (err){
        return Promise.reject(inAppPurchases.utils.getJSError(err));
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
            return Promise.reject(inAppPurchases.utils.getJSError(err));
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
            return Promise.reject(inAppPurchases.utils.getJSError(err));
        }
    });
}

inAppPurchases.purchase = function (productId,upgradeProductId="",replacementMode=-1){
    return new Promise(function (resolve, reject) {
        try {
            if (!inAppPurchases.utils.validString(productId)) {
                reject(inAppPurchases.utils.getError('product_id_string'));
            } else if (replacementMode != -1 && !inAppPurchases.utils.validReplacementMode(replacementMode)){
                reject(inAppPurchases.utils.getError('replacement_mode_invalid'));
            } else if (upgradeProductId && !inAppPurchases.utils.validString(upgradeProductId)){
                reject(inAppPurchases.utils.getError('product_id_string'));
            }  else {
                nativeCall('billingPurchase',[productId, upgradeProductId, replacementMode]).then(function (res) {
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
            reject(inAppPurchases.utils.getJSError(err));
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

inAppPurchases.getReceipt = function (productId){
    try {
        return new Promise(function (resolve, reject) {
            if (!inAppPurchases.utils.validString(productId)) {
                reject(inAppPurchases.utils.getError('product_id_string'));
            } else {
                nativeCall('billingGetReceipt',[productId]).then(function (res) {
                    resolve(res);
                })["catch"](reject);
            }
        });
    } catch (err) {
        return Promise.reject(inAppPurchases.utils.getJSError(err));
    }
}

inAppPurchases.subscriptionReplacementMode = {
    "CHARGE_FULL_PRICE":5,
    "CHARGE_PRORATED_PRICE":2,
    "DEFERRED":6,
    "UNKNOWN_REPLACEMENT_MODE":0,
    "WITHOUT_PRORATION":3,
    "WITH_TIME_PRORATION":1
};

module.exports = inAppPurchases;
