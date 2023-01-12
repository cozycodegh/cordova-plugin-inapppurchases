/* Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/** cordova-plugin-inapppurchases MIT © 2023 cozycode.ca **/

package com.alexdisler_github_cozycode.inapppurchases;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.BillingClient.ProductType;

/**
 * Represents an in-app product's listing details.
 */
public class IabProductDetails {
    
    ProductDetails mProductDetails;
    
    String mProductId;
    String mProductType;
    String mTitle;
    String mDescription;
    String mPrice;
    String mPriceCurrency;
    Double mPriceAsDecimal;
    String mPriceRaw;
    String mCountry;
    String mOfferToken;
    
    String TAG = "google.payments ISD";
    
    public IabProductDetails(ProductDetails productDetails) {
        mProductDetails = productDetails;
        //store info
        mProductId = productDetails.getProductId();
        mProductType = productDetails.getProductType();
        mTitle = productDetails.getTitle();
        mDescription = productDetails.getDescription();
        mCountry = "-"; //not supported
        //         mPriceAsDecimal = Double.valueOf(1000);
        // All product types - com.android.billingclient.api.BillingClient.ProductType;
        if(ProductType.INAPP.equals(mProductType)) {
            ProductDetails.OneTimePurchaseOfferDetails otp = productDetails.getOneTimePurchaseOfferDetails();
            mPrice = otp.getFormattedPrice();
            mPriceCurrency = otp.getPriceCurrencyCode();
            mPriceAsDecimal = otp.getPriceAmountMicros()/Double.valueOf(1000000);
        } else {
            ProductDetails.SubscriptionOfferDetails so = productDetails.getSubscriptionOfferDetails().get(0); //getting first offer
            ProductDetails.PricingPhase pp = so.getPricingPhases().getPricingPhaseList().get(0);
            mPrice = pp.getFormattedPrice();
            mPriceCurrency = pp.getPriceCurrencyCode();
            mPriceAsDecimal = pp.getPriceAmountMicros()/Double.valueOf(1000000);
            mOfferToken = so.getOfferToken();
        }
        //         long priceMicros = productDetails.getPriceAmountMicros();
        DecimalFormat formatter = new DecimalFormat("#.00####");
        formatter.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        mPriceRaw = formatter.format(mPriceAsDecimal);
    }
    
    public String getProductId() { return mProductId; }
    public String getProductType() { return mProductType; }
    public String getPrice() { return mPrice; }
    public String getPriceCurrency() { return mPriceCurrency; }
    public Double getPriceAsDecimal() { return mPriceAsDecimal; }
    public String getPriceRaw() { return mPriceRaw; }
    public String getTitle() { return mTitle; }
    public String getDescription() { return mDescription; }
    public String getOfferToken() { return mOfferToken; }
    public ProductDetails getProductDetails() { return mProductDetails; }
    
    public JSONObject getDetailsJSON() throws JSONException {
        JSONObject detailsJson = new JSONObject();
        detailsJson.put("productId", getProductId());
        detailsJson.put("productType", getProductType());
        detailsJson.put("title", getTitle());
        detailsJson.put("description", getDescription());
        detailsJson.put("price", getPrice());
        detailsJson.put("priceAsDecimal", getPriceAsDecimal());
        detailsJson.put("priceRaw", getPriceRaw());
        detailsJson.put("country", "-");
        detailsJson.put("currency", getPriceCurrency());
        return detailsJson;
    }
    
    @Override
    public String toString() {
        return "IabProductDetails { \n"
        + " mProductId: " + mProductId + "\n"
        + " mProductType: " + mProductType  + "\n"
        + " mTitle: " + mTitle + "\n"
        + " mDescription: " + mDescription + "\n"
        + " mPrice: " + mPrice + "\n"
        + " mPriceCurrency: " + mPriceCurrency  + "\n"
        + " mPriceAsDecimal: " + mPriceAsDecimal + "\n"
        + " mPriceRaw: " + mPriceRaw + "\n"
        + " mCountry: " + mCountry + "\n"
        + "}\n";
        //         if(mProductIdDetails != null) {
        //             return "IabProductDetails:" + mProductIdDetails;
        //         } else {
        //             return "IabProductDetails Product:" + mProductDetails;
        //         }
    }
}
