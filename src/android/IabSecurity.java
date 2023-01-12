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

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * IabSecurity-related methods. For a secure implementation, all of this code
 * should be implemented on a server that communicates with the
 * application on the device. For the sake of simplicity and clarity of this
 * example, this code is included here and is executed on the device. If you
 * must verify the purchases on the phone, you should obfuscate this code to
 * make it harder for an attacker to replace the code with stubs that treat all
 * purchases as verified.
 */
public class IabSecurity {
    private static final String TAG = "IABUtil/IabSecurity";

    private static final String KEY_FACTORY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
    
    private static String mFailKeyDecodeMessage;
    private static String mFailVerifyMessage;

    /**
     * Verifies that the data was signed with the given signature, and returns
     * the verified purchase. The data is in JSON format and signed
     * with a private key. The data also contains the {@link PurchaseState}
     * and product ID of the purchase.
     * @param base64PublicKey the base64-encoded public key to use for verifying.
     * @param signedData the signed JSON string (signed, not encrypted)
     * @param signature the signature for the data, signed with the private key
     */
    public static boolean verifyPurchase(String base64PublicKey, String signedData, String signature) {
        mFailKeyDecodeMessage = null;
        mFailVerifyMessage = null;
        if (TextUtils.isEmpty(signedData)){
            mFailVerifyMessage = "Missing data - no purchase JSON was found to verify";
            Log.e(TAG, "Purchase verification failed: "+mFailVerifyMessage);
            return false;
        }
        if (TextUtils.isEmpty(base64PublicKey)){
            mFailKeyDecodeMessage = "Missing data - no base64 public key was entered";
            Log.e(TAG, "Purchase verification failed: "+mFailKeyDecodeMessage);
            return false;
        }
        if (TextUtils.isEmpty(signature)) {
            mFailVerifyMessage = "Misssing data - no signature was found to verify";
            Log.e(TAG, "Purchase verification failed: "+mFailVerifyMessage);
            return false;
        }

        PublicKey key = IabSecurity.generatePublicKey(base64PublicKey);
        return IabSecurity.verify(key, signedData, signature);
    }

    /**
     * Generates a PublicKey instance from a string containing the
     * Base64-encoded public key.
     *
     * @param encodedPublicKey Base64-encoded public key
     * @throws IllegalArgumentException if encodedPublicKey is invalid
     */
    public static PublicKey generatePublicKey(String encodedPublicKey) {
        try {
            byte[] decodedKey = Base64.decode(encodedPublicKey);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
        } catch (NoSuchAlgorithmException e) {
            mFailKeyDecodeMessage = e.toString();
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            if (mFailKeyDecodeMessage == null) mFailKeyDecodeMessage = "InvalidKeySpecException "+e.toString();
            Log.e(TAG, "Invalid key specification.");
            throw new IllegalArgumentException(e);
        } catch (Base64DecoderException e) {
            if (mFailKeyDecodeMessage == null) mFailKeyDecodeMessage = e.toString();
            Log.e(TAG, "Base64 decoding failed - invalid base64 public key entered.");
            throw new IllegalArgumentException(e+" - invalid base64 public key entered (base64 decode failed)");
        }
    }

    /**
     * Verifies that the signature from the server matches the computed
     * signature on the data.  Returns true if the data is correctly signed.
     *
     * @param publicKey public key associated with the developer account
     * @param signedData signed data from server
     * @param signature server signature
     * @return true if the data and signature match
     */
    public static boolean verify(PublicKey publicKey, String signedData, String signature) {
        Signature sig;
        try {
            sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initVerify(publicKey);
            sig.update(signedData.getBytes());
            if (!sig.verify(Base64.decode(signature))) {
                String msg = "Signature verification failed.";
                if (mFailVerifyMessage == null) mFailVerifyMessage = msg;
                Log.e(TAG, msg);
                return false;
            }
            return true;
        } catch (NoSuchAlgorithmException e) {
            if (mFailVerifyMessage == null) mFailVerifyMessage = "NoSuchAlgorithmException on "+SIGNATURE_ALGORITHM;
            Log.e(TAG, "NoSuchAlgorithmException.");
        } catch (InvalidKeyException e) {
            if (mFailVerifyMessage == null) mFailVerifyMessage = e.toString();
            Log.e(TAG, "Invalid key specification.");
        } catch (SignatureException e) {
            if (mFailVerifyMessage == null) mFailVerifyMessage = e.toString();
            Log.e(TAG, "Signature exception.");
        } catch (Base64DecoderException e) {
            if (mFailVerifyMessage == null) mFailVerifyMessage = e.toString();
            Log.e(TAG, "Base64 decoding failed.");
        }
        return false;
    }
    
    /* Decoding and Verifying messages */
    public static boolean hasDecodeFailMessage(){
        return mFailKeyDecodeMessage != null;
    }
    public static boolean hasVerifyFailMessage(){
        return mFailKeyDecodeMessage != null;
    }
    public static String getFailDecodeMessage(){
        return mFailKeyDecodeMessage;
    }
    public static String getFailVerifyMessage(){
        return mFailVerifyMessage;
    }
}
