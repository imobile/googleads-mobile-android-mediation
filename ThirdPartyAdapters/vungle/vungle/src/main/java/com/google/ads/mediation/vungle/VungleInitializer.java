package com.google.ads.mediation.vungle;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.vungle.mediation.VungleConsent;
import com.vungle.mediation.VungleNetworkSettings;
import com.vungle.warren.InitCallback;
import com.vungle.warren.Plugin;
import com.vungle.warren.Vungle;
import com.vungle.warren.VungleApiClient;
import com.vungle.warren.VungleSettings;

import java.util.concurrent.ConcurrentHashMap;

public class VungleInitializer implements InitCallback {

    private static VungleInitializer instance;
    private boolean mIsInitializing = false;
    private ConcurrentHashMap<String, VungleInitializationListener> mInitListeners;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public static VungleInitializer getInstance() {
        if (instance == null) {
            instance = new VungleInitializer();
        }
        return instance;
    }

    private VungleInitializer() {
        mInitListeners = new ConcurrentHashMap<>();
    }

    boolean isInitializing() {
        return mIsInitializing;
    }

    public boolean isInitialized() {
        return Vungle.isInitialized();
    }

    public void initialize(final String appId, final Context context, String adapterId, VungleInitializationListener listener) {
        if (isInitializing()) {
            mInitListeners.put(adapterId, listener);
            return;
        }

        if (isInitialized()) {
            listener.onInitializeSuccess();
            return;
        }

        mIsInitializing = true;

        Plugin.addWrapperInfo(VungleApiClient.WrapperFramework.admob,
                com.vungle.warren.BuildConfig.VERSION_NAME.replace('.', '_'));

        //Keep monitoring VungleSettings in case of any changes we need to re-init SDK to apply updated settings.
        VungleNetworkSettings.setVungleSettingsChangedListener(new VungleNetworkSettings.VungleSettingsChangedListener() {
            @Override
            public void onVungleSettingsChanged(VungleSettings updatedSettings) {
                //Ignore if sdk is yet to initialize, it will get considered while init
                if(!Vungle.isInitialized()){
                    return;
                }

                VungleSettings settings = (updatedSettings != null) ? updatedSettings : new VungleSettings.Builder().build();
                //Pass new settings to SDK.
                Vungle.init(appId, context.getApplicationContext(), VungleInitializer.this, settings);
            }
        });

        VungleSettings vungleSettings = VungleNetworkSettings.getVungleSettings();
        VungleSettings settings = (vungleSettings != null) ? vungleSettings : new VungleSettings.Builder().build();
        Vungle.init(appId, context.getApplicationContext(), VungleInitializer.this, settings);
        getInstance().mInitListeners.put(adapterId, listener);
    }

    @Override
    public void onSuccess() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (VungleConsent.getCurrentVungleConsent() != null) {
                    Vungle.updateConsentStatus(VungleConsent.getCurrentVungleConsent(),
                            VungleConsent.getCurrentVungleConsentMessageVersion());
                }
                for (VungleInitializationListener listener : mInitListeners.values()) {
                    listener.onInitializeSuccess();
                }
                mInitListeners.clear();
            }
        });
        mIsInitializing = false;
    }

    @Override
    public void onError(final Throwable throwable) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (VungleInitializationListener listener : mInitListeners.values()) {
                    listener.onInitializeError(throwable.getLocalizedMessage());
                }
                mInitListeners.clear();
            }
        });
        mIsInitializing = false;
    }

    @Override
    public void onAutoCacheAdAvailable(String placementID) {
        // Unused
    }

    public interface VungleInitializationListener {
        void onInitializeSuccess();
        void onInitializeError(String errorMessage);
    }

}
