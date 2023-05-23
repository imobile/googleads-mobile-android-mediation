package com.google.ads.mediation.inmobi.rtb

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.ads.mediation.inmobi.InMobiAdFactory
import com.google.ads.mediation.inmobi.InMobiAdapterUtils
import com.google.ads.mediation.inmobi.InMobiConstants
import com.google.ads.mediation.inmobi.InMobiInitializer
import com.google.ads.mediation.inmobi.InMobiInterstitialWrapper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.mediation.MediationAdLoadCallback
import com.google.android.gms.ads.mediation.MediationInterstitialAd
import com.google.android.gms.ads.mediation.MediationInterstitialAdCallback
import com.google.android.gms.ads.mediation.MediationInterstitialAdConfiguration
import com.inmobi.ads.AdMetaInfo
import com.inmobi.ads.InMobiAdRequestStatus
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class InMobiRtbInterstitialAdTest {
  private val interstitialAdConfiguration = mock<MediationInterstitialAdConfiguration>()
  private val mediationAdLoadCallback =
    mock<MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback>>()
  private val inMobiInitializer = mock<InMobiInitializer>()
  private val inMobiAdFactory = mock<InMobiAdFactory>()
  private val inMobiInterstitialWrapper = mock<InMobiInterstitialWrapper>()
  private val mediationInterstitialAdCallback = mock<MediationInterstitialAdCallback>()
  private val context = ApplicationProvider.getApplicationContext<Context>()

  lateinit var rtbInterstitialAd: InMobiRtbInterstitialAd
  lateinit var adMetaInfo: AdMetaInfo

  @Before
  fun setUp() {
    adMetaInfo = AdMetaInfo("fake", null)
    whenever(mediationAdLoadCallback.onSuccess(any())).thenReturn(mediationInterstitialAdCallback)

    rtbInterstitialAd =
      InMobiRtbInterstitialAd(
        interstitialAdConfiguration,
        mediationAdLoadCallback,
        inMobiInitializer,
        inMobiAdFactory
      )
  }

  @Test
  fun onShowAd_ifInterstitialAdIsReady_AdIsShown() {
    whenever(inMobiAdFactory.createInMobiInterstitialWrapper(any(), any(), any()))
      .thenReturn(inMobiInterstitialWrapper)
    whenever(interstitialAdConfiguration.bidResponse).thenReturn("BiddingToken")
    whenever(inMobiInterstitialWrapper.isReady).thenReturn(true)
    whenever(mediationAdLoadCallback.onSuccess(any())).thenReturn(mediationInterstitialAdCallback)

    val placementId = 67890L
    rtbInterstitialAd.createAndLoadInterstitialAd(context, placementId)
    rtbInterstitialAd.showAd(context)

    verify(inMobiInterstitialWrapper).show()
  }

  @Test
  fun onShowAd_ifInterstitialAdNotReady_DoNothing() {
    whenever(inMobiAdFactory.createInMobiInterstitialWrapper(any(), any(), any()))
      .thenReturn(inMobiInterstitialWrapper)
    whenever(interstitialAdConfiguration.bidResponse).thenReturn("BiddingToken")
    whenever(inMobiInterstitialWrapper.isReady).thenReturn(false)

    val placementId = 67890L
    rtbInterstitialAd.createAndLoadInterstitialAd(context, placementId)
    rtbInterstitialAd.showAd(context)

    verify(inMobiInterstitialWrapper, never()).show()
  }

  @Test
  fun onUserLeftApplication_invokesOnAdLeftApplicationCallback() {
    // mimic an ad load
    rtbInterstitialAd.onAdLoadSucceeded(inMobiInterstitialWrapper.inMobiInterstitial, adMetaInfo)
    rtbInterstitialAd.onUserLeftApplication(inMobiInterstitialWrapper.inMobiInterstitial)

    verify(mediationInterstitialAdCallback).onAdLeftApplication()
  }

  @Test
  fun onAdLoadSucceeded_invokesOnSuccessCallback() {
    rtbInterstitialAd.onAdLoadSucceeded(inMobiInterstitialWrapper.inMobiInterstitial, adMetaInfo)

    verify(mediationAdLoadCallback)
      .onSuccess(ArgumentMatchers.any(rtbInterstitialAd::class.java))
  }

  @Test
  fun onAdLoadFailed_invokesOnFailureCallback() {
    var inMobiAdRequestStatus =
      InMobiAdRequestStatus(InMobiAdRequestStatus.StatusCode.INTERNAL_ERROR)

    rtbInterstitialAd.onAdLoadFailed(
      inMobiInterstitialWrapper.inMobiInterstitial,
      inMobiAdRequestStatus
    )

    val captor = argumentCaptor<AdError>()
    verify(mediationAdLoadCallback).onFailure(captor.capture())
    assertThat(captor.firstValue.code)
      .isEqualTo(InMobiAdapterUtils.getMediationErrorCode(inMobiAdRequestStatus))
    assertThat(captor.firstValue.domain).isEqualTo(InMobiConstants.INMOBI_SDK_ERROR_DOMAIN)
  }

  @Test
  fun onAdDisplayed_invokesOnAdOpenedCallback() {
    // mimic an ad load
    rtbInterstitialAd.onAdLoadSucceeded(inMobiInterstitialWrapper.inMobiInterstitial, adMetaInfo)
    rtbInterstitialAd.onAdDisplayed(inMobiInterstitialWrapper.inMobiInterstitial, adMetaInfo)

    verify(mediationInterstitialAdCallback).onAdOpened()
  }

  @Test
  fun onAdDismissed_invokedOnAdClosedCallback() {
    // mimic an ad load
    rtbInterstitialAd.onAdLoadSucceeded(inMobiInterstitialWrapper.inMobiInterstitial, adMetaInfo)
    rtbInterstitialAd.onAdDismissed(inMobiInterstitialWrapper.inMobiInterstitial)

    verify(mediationInterstitialAdCallback).onAdClosed()
  }

  @Test
  fun onAdClicked_invokedOnAdClickedCallback() {
    // mimic an ad load
    rtbInterstitialAd.onAdLoadSucceeded(inMobiInterstitialWrapper.inMobiInterstitial, adMetaInfo)
    rtbInterstitialAd.onAdClicked(inMobiInterstitialWrapper.inMobiInterstitial, null)

    verify(mediationInterstitialAdCallback).reportAdClicked()
  }

  @Test
  fun onAdImpression_invokesReportAdImpression() {
    // mimic an ad load
    rtbInterstitialAd.onAdLoadSucceeded(inMobiInterstitialWrapper.inMobiInterstitial, adMetaInfo)
    rtbInterstitialAd.onAdImpression(inMobiInterstitialWrapper.inMobiInterstitial)

    verify(mediationInterstitialAdCallback).reportAdImpression()
  }
}