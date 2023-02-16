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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;

/**
 * Represents a block of information about in-app products.
 * An IabInventory is returned by Iab calls that return product or purchase information
 * Used to store:
 * - ProductDetails
 *      getAllKnownProductIds() to get product ids
 *      getAllKnownProductDetailsJSON() to get JSON of all product information
 * - Purchases (details of purchased products)
 *      getAllOwnedProductIds() to get purhcased product ids
 *      getAllOwnedPurchasesJSON() to get JSON of all purchase information
 */
public class IabInventory {
    
    Map<String,IabProductDetails> mProductDetailsMap = new HashMap<String,IabProductDetails>();
    Map<String,IabPurchase> mPurchaseMap = new HashMap<String,IabPurchase>();
    
    public IabInventory() { }
    public IabInventory(List<ProductDetails> productDetailsList){
        addProductDetails(productDetailsList);
    }
    public IabInventory(List<Purchase> purchaseList, String productType){ //String isPurchaseList){
        addPurchase(purchaseList, productType);
    }
    
    /* ProductIds */
    public List<String> combineProductIdLists(List<String> list1, List<String> list2){
        List<String> combined = new ArrayList<String>();
        for (String productId: list1){
            if (!combined.contains(productId)) combined.add(productId);
        }
        for (String productId: list2){
            if (!combined.contains(productId)) combined.add(productId);
        }
        return combined;
    }
    public List<String> getAllProductIds(){
        return combineProductIdLists(getAllKnownProductIds(), getAllOwnedProductIds());
    }
    
    /** Product Details **/
    // Returns a list of all known product IDs with details.
    List<String> getAllKnownProductIds(){
        return new ArrayList<String>(mProductDetailsMap.keySet());
    }
    // Returns a list of all known product IDs of a given type with details.
    List<String> getAllKnownProductIds(String productType) {
        List<String> result = new ArrayList<String>();
        for (IabProductDetails p : mProductDetailsMap.values()) {
            if ((productType == null) || p.getProductType().equals(productType)) result.add(p.getProductId());
        }
        return result;
    }
    // Get JSON string of all product details
    public JSONArray getAllKnownProductDetailsJSON() throws JSONException {
        JSONArray productDetailsJSON = new JSONArray();
        for (IabProductDetails d : mProductDetailsMap.values()) {
            productDetailsJSON.put(d.getDetailsJSON());
        }
        return productDetailsJSON;
    }
    // Get JSON string of all product details
    public JSONArray getSelectedProductDetailsJSON(List<String> productIds) throws JSONException {
        JSONArray productDetailsJSON = new JSONArray();
        for (String productId : productIds) {
            if (hasDetails(productId)){
                productDetailsJSON.put(getDetailsJSON(productId));
            }
        }
        return productDetailsJSON;
    }
    // Edit the inventory
    void addProductDetails(IabProductDetails d) {
        mProductDetailsMap.put(d.getProductId(), d);
    }
    void addProductDetails(ProductDetails d){
        this.addProductDetails(new IabProductDetails(d));
    }
    void addProductDetails(List<ProductDetails> productDetailsList){
        for (ProductDetails d: productDetailsList){
            this.addProductDetails(d);
        }
    }
    void removeProductDetails(IabProductDetails d) {
        this.removeProductDetails(d.getProductId());
    }
    void removeProductDetails(String productId){
        mProductDetailsMap.remove(productId);
    }
    void removeAllProductDetails(){
        mProductDetailsMap.clear();
    }
    // Returns IabDetails for an in-app product
    public IabProductDetails getDetails(String productId) {
        return mProductDetailsMap.get(productId);
    }
    // Returns JSON IabDetails for an in-app product
    public JSONObject getDetailsJSON(String productId) throws JSONException {
        return mProductDetailsMap.get(productId).getDetailsJSON();
    }
    // Return whether or not details about the given product are available. */
    public boolean hasDetails(String productId) {
        return mProductDetailsMap.containsKey(productId);
    }
    
    /** Purchases **/
    // Returns a list of all owned product IDs.
    List<String> getAllOwnedProductIds() {
        return new ArrayList<String>(mPurchaseMap.keySet());
    }
    // Returns a list of all owned product IDs of a given type
    List<String> getAllOwnedProductIds(String productType) {
        List<String> result = new ArrayList<String>();
        for (IabPurchase p : mPurchaseMap.values()) {
            if ((productType == null) || p.getProductType().equals(productType)) result.add(p.getProductId());
        }
        return result;
    }
    // Get all purchases in a list
    public List<IabPurchase> getAllOwnedPurchases(){
        List<IabPurchase> purchases = new ArrayList<IabPurchase>();
        for (IabPurchase p : mPurchaseMap.values()) {
            purchases.add(p);
        }
        return purchases;
    }
    // Get JSON string of all purchases
    public JSONArray getAllOwnedPurchasesJSON() throws JSONException {
        JSONArray purchaseJSON = new JSONArray();
        for (IabPurchase d : mPurchaseMap.values()) {
            purchaseJSON.put(d.getDetailsJSON());
        }
        return purchaseJSON;
    }
    // Edit the inventory
    void addPurchase(IabPurchase p) {
        mPurchaseMap.put(p.getProductId(), p);
    }
    void addPurchase(Purchase p, String productType) {
        IabPurchase ip = new IabPurchase(p,productType);
        this.addPurchase(ip);
    }
    void addPurchase(List<Purchase> purchaseList, String productType){
        for (Purchase d: purchaseList){
            this.addPurchase(d, productType);
        }
    }
    void removePurchase(IabPurchase d) {
        this.removePurchase(d.getProductId());
    }
    void removePurchase(String productId){
        mPurchaseMap.remove(productId);
    }
    void removeAllPurchases(){
        mPurchaseMap.clear();
    }
    // Returns a list of all purchases stored in the inventory.
    List<IabPurchase> getAllPurchases() {
        return new ArrayList<IabPurchase>(mPurchaseMap.values());
    }
    // Returns purchase information for a given product, or null if there is no purchase.
    public IabPurchase getPurchase(String productId) {
        return mPurchaseMap.get(productId);
    }
    // Returns whether or not there exists a purchase of the given product.
    public boolean hasPurchase(String productId) {
        return mPurchaseMap.containsKey(productId);
    }
    
    // Combine inventory
    public Map<String,IabProductDetails> getProductDetailsMap(){ return mProductDetailsMap; }
    public Map<String,IabPurchase> getPurchaseMap(){ return mPurchaseMap; }
    public void overwriteProductDetailsInventory(IabInventory inv){
        mProductDetailsMap = inv.getProductDetailsMap();
    }
    public void overwritePurchaseInventory(IabInventory inv){
        mPurchaseMap = inv.getPurchaseMap();
    }
    public void overwriteInventory(IabInventory inv){
        overwriteProductDetailsInventory(inv);
        overwritePurchaseInventory(inv);
    }
    public void addInventory(IabInventory inv){
        for (IabProductDetails p : inv.getProductDetailsMap().values()) {
            addProductDetails(p);
        }
        for (IabPurchase p : inv.getPurchaseMap().values()) {
            addPurchase(p);
        }
    }
    
    // Log
    public String toString() {
        String out = "IabInventory Class { ";
        List<String> productIds = getAllProductIds();
        boolean first = true;
        for (int i=0; i<productIds.size(); i++){
            if (first) first = false;
            else out += ", ";
            String productId = productIds.get(i);
            out += productId;
            if (hasPurchase(productId)){
                out += " (owned";
                IabPurchase p = getPurchase(productId);
                if (p.getPending()) out += " - pending";
                if (!p.getCompleted()) out += " - not completed";
                out += ")";
            }
        }
        out += " }";
        return out;
    }
}
