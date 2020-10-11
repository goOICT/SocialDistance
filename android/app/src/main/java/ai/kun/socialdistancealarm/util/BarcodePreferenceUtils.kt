package ai.kun.socialdistancealarm.util

import ai.kun.socialdistancealarm.camera.CameraSizePair
import ai.kun.socialdistancealarm.camera.GraphicOverlay
import android.content.Context
import android.graphics.RectF
import androidx.annotation.StringRes
import com.google.android.gms.common.images.Size
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

/**
 * This code was imported from the example project we used.
 */
object BarcodePreferenceUtils {

    fun isAutoSearchEnabled(context: Context): Boolean {
        return true
    }

    fun isMultipleObjectsMode(context: Context): Boolean {
        return false
    }

    fun isClassificationEnabled(context: Context): Boolean {
        return false
    }

    fun getConfirmationTimeMs(context: Context): Int = when {
        isMultipleObjectsMode(context) -> 300
        isAutoSearchEnabled(context) -> 1500
        else -> 500
    }

    fun getProgressToMeetBarcodeSizeRequirement(
        overlay: GraphicOverlay,
        barcode: FirebaseVisionBarcode
    ): Float {
        val context = overlay.context
        return 1f
    }

    fun getBarcodeReticleBox(overlay: GraphicOverlay): RectF {
        val context = overlay.context
        val overlayWidth = overlay.width.toFloat()
        val overlayHeight = overlay.height.toFloat()
        val boxWidth = overlayWidth * 80 / 100
        val boxHeight = overlayHeight * 35 / 100
        val cx = overlayWidth / 2
        val cy = overlayHeight / 2
        return RectF(cx - boxWidth / 2, cy - boxHeight / 2, cx + boxWidth / 2, cy + boxHeight / 2)
    }

    fun shouldDelayLoadingBarcodeResult(context: Context): Boolean {
        return true
    }

    private fun getIntPref(context: Context, @StringRes prefKeyId: Int, defaultValue: Int): Int {
        return defaultValue
    }

    fun getUserSpecifiedPreviewSize(context: Context): CameraSizePair? {
        return try {
            CameraSizePair(
                Size.parseSize(null),
                Size.parseSize(null))
        } catch (e: Exception) {
            null
        }
    }

    private fun getBooleanPref(context: Context, @StringRes prefKeyId: Int, defaultValue: Boolean): Boolean {
        return defaultValue
    }
}
