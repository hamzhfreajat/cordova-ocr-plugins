//  Copyright (c) 2016 PayPal. All rights reserved.

package io.card.cordova.sdk;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.io.ByteArrayOutputStream;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

public class CardIOCordovaPlugin extends CordovaPlugin {

    private CallbackContext callbackContext;
    private Activity activity = null;
    private static final int REQUEST_CARD_SCAN = 10;

    @Override
    public boolean execute(String action, JSONArray args,
                           CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        this.activity = this.cordova.getActivity();
        boolean retValue = true;
        if (action.equals("scan")) {
            this.scan(args);
        } else if (action.equals("canScan")) {
            this.canScan(args);
        } else if (action.equals("version")) {
            this.callbackContext.success(CardIOActivity.sdkVersion());
        } else {
            retValue = false;
        }

        return retValue;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void prepareToRender(JSONArray args) throws JSONException {
        this.callbackContext.success();
    }

    private void scan(JSONArray args) throws JSONException {
        Intent scanIntent = new Intent(this.activity, CardIOActivity.class);
        JSONObject configurations = args.getJSONObject(0);
        // customize these values to suit your needs.
             scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default: false
             scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true); // default: false
             scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, true); // default: false
             scanIntent.putExtra(CardIOActivity.EXTRA_RESTRICT_POSTAL_CODE_TO_NUMERIC_ONLY, false); // default: false
             scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CARDHOLDER_NAME, false); // default: false
             scanIntent.putExtra(CardIOActivity.EXTRA_USE_CARDIO_LOGO, false);
             scanIntent.putExtra(CardIOActivity.EXTRA_CAPTURED_CARD_IMAGE, true);
             scanIntent.putExtra(CardIOActivity.EXTRA_USE_PAYPAL_ACTIONBAR_ICON, false);
             scanIntent.putExtra(CardIOActivity.EXTRA_HIDE_CARDIO_LOGO, true);
             scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION, true);
             scanIntent.putExtra(CardIOActivity.EXTRA_SCAN_EXPIRY, true); //
             scanIntent.putExtra(CardIOActivity.EXTRA_RETURN_CARD_IMAGE, true);
             scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, false);
             scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_SCAN, true);
             scanIntent.putExtra(CardIOActivity.EXTRA_SCAN_RESULT, true);

        this.cordova.startActivityForResult(this, scanIntent, REQUEST_CARD_SCAN);
    }

    private void canScan(JSONArray args) throws JSONException {
        if (CardIOActivity.canReadCardWithCamera()) {
            // This is where we return if scanning is enabled.
            this.callbackContext.success("Card Scanning is enabled");
        } else {
            this.callbackContext.error("Card Scanning is not enabled");
        }
    }

    // onActivityResult
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (REQUEST_CARD_SCAN == requestCode) {
           Bitmap bitmap = CardIOActivity.getCapturedCardImage(intent);
           ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
           bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
           byte[] byteArray = byteArrayOutputStream.toByteArray();
           String encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);
           this.callbackContext.success(this.toJSONObject(encoded));
        }
    }

    private JSONObject toJSONObject(String encoded) {
        JSONObject scanCard = new JSONObject();
        try {
            scanCard.put("image64", encoded);
        } catch (JSONException e) {
            scanCard = null;
        }

        return scanCard;
    }

    private <T> T getConfiguration(JSONObject configurations, String name, T defaultValue) {
        if (configurations.has(name)) {
            try {
                return (T)configurations.get(name);
            } catch (JSONException ex) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }
}
