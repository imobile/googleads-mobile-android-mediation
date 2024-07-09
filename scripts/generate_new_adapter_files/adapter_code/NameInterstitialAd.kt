// Copyright 2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.ads.mediation.##adapter_name_lower_cased##

import android.content.Context
import com.google.android.gms.ads.mediation.MediationAdLoadCallback
import com.google.android.gms.ads.mediation.MediationInterstitialAd
import com.google.android.gms.ads.mediation.MediationInterstitialAdCallback
import com.google.android.gms.ads.mediation.MediationInterstitialAdConfiguration

/**
 * Used to load ##adapter_name## interstitial ads and mediate callbacks between Google Mobile Ads SDK and ##adapter_name## SDK.
 */
class ##adapter_name##InterstitialAd
private constructor(
  private val context: Context,
  private val mediationAdLoadCallback:
    MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback>,
  // TODO: Add other parameters or remove unnecessary ones.
) : MediationInterstitialAd {

  fun loadAd() {
    // TODO: Implement this method.
  }

  override fun showAd(context: Context) {
    // TODO: Implement this method.
  }

  companion object {
    fun newInstance(
      mediationInterstitialAdConfiguration: MediationInterstitialAdConfiguration,
      mediationAdLoadCallback:
        MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback>,
    ): Result<##adapter_name##InterstitialAd> {
      val context = mediationInterstitialAdConfiguration.context
      val serverParameters = mediationInterstitialAdConfiguration.serverParameters

      // TODO: Implement necessary initialization steps.

      return Result.success(
        ##adapter_name##InterstitialAd(
          context,
          mediationAdLoadCallback,
        )
      )
    }
  }
}
