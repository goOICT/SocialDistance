package ai.kun.socialdistancealarm.ui.history

import ai.kun.socialdistancealarm.LiveBarcodeScanningActivity
import ai.kun.socialdistancealarm.R
import ai.kun.socialdistancealarm.util.BarcodeEncoder
import ai.kun.socialdistancealarm.util.Constants
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix;


class TeamsFragment : Fragment() {
    private val TAG = "TeamsFragment"

    private val REQUEST_CAMERA = 4
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val historyViewModel: HistoryViewModel by viewModels()
        val root = inflater.inflate(R.layout.fragment_teams, container, false)

        // Show the QR Code...
        val barCodeImageView = root.findViewById<ImageView>(R.id.barcodeImage)
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix: BitMatrix =
                multiFormatWriter.encode("SomeIDother", BarcodeFormat.QR_CODE, Constants.QR_CODE_SIZE, Constants.QR_CODE_SIZE)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)
            barCodeImageView.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }

        val cameraButton = root.findViewById<FloatingActionButton>(R.id.cameraButton)
        cameraButton.setOnClickListener {
            if (requireContext().applicationContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA);
            } else {
                scanBarcode()
            }
        }

        return root
    }

    private fun scanBarcode() {
        startActivity(Intent(activity, LiveBarcodeScanningActivity::class.java))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA -> {
                if (!grantResults.isEmpty()) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        Log.w(TAG, "...Request for camera access denied.")
                    } else {
                        Log.d(TAG, "...Request for camera access granted.")
                        scanBarcode()
                    }
                } else {
                    // for some lame reason Android gives you a result with nothing in it before
                    // there is a real result?
                    Log.d(TAG, "Prompting for camera response...")
                }
            }
            else -> {
                Log.w(TAG, "Unknown request code: $requestCode")
            }
        }
    }

}
