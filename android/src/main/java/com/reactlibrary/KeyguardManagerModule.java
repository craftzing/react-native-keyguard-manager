package com.reactlibrary;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class KeyguardManagerModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private static final String E_ACTIVITY_DOES_NOT_EXIST = "E_ACTIVITY_DOES_NOT_EXIST";
    private static final int KEYGUARD_REQUEST = 44;
    private static final String E_KEYGUARD_CANCELLED = "E_KEYGUARD_CANCELLED";
    private static final String E_FAILED_TO_SHOW_KEYGUARD = "E_FAILED_TO_SHOW_KEYGUARD";
    private final ReactApplicationContext reactContext;
    private Promise mKeyguardPromise;

    public KeyguardManagerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "KeyguardManager";
    }

    @ReactMethod
    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    }

    @ReactMethod
    public void keyguardAuthenticateAsync(String title, String description, Promise promise) {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            promise.reject(E_ACTIVITY_DOES_NOT_EXIST, "Activity doesn't exist");
            return;
        }

        mKeyguardPromise = promise;
        try {
            if (Build.VERSION.SDK_INT >= 21) {
                Intent i = getKeyguardManager().createConfirmDeviceCredentialIntent(title, description);
                currentActivity.startActivityForResult(i, KEYGUARD_REQUEST);
            }
        } catch (Exception e) {
            mKeyguardPromise.reject(E_FAILED_TO_SHOW_KEYGUARD, e);
            mKeyguardPromise = null;
        }

    }

    @ReactMethod
    public void isKeyguardNotDepricated(Promise promise) {
        promise.resolve(Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT <= 28);
    }

    @ReactMethod
    public void canUseDepricatedKeyguard(Promise promise) {
        promise.resolve(Build.VERSION.SDK_INT >= 21);
    }


    @ReactMethod
    public void isKeyguardSecure(Promise promise) {
        promise.resolve(getKeyguardManager().isKeyguardSecure());
    }


    private KeyguardManager getKeyguardManager() {
        return (KeyguardManager) getCurrentActivity().getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
    }

    @Override
    public void onNewIntent(Intent intent) {
    }

    @Override
    public void onActivityResult(Activity activity, final int requestCode, final int resultCode, final Intent intent) {
        if (requestCode != KEYGUARD_REQUEST)
            return;
        if (mKeyguardPromise == null)
            return;
        if (resultCode == Activity.RESULT_CANCELED) {
            mKeyguardPromise.reject(E_KEYGUARD_CANCELLED, "Keyguard cancelled");
        } else if (resultCode == Activity.RESULT_OK) {
            mKeyguardPromise.resolve("OPENED");
        }
    }

}
