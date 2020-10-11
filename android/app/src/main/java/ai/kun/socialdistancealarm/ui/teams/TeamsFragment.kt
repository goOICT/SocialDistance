package ai.kun.socialdistancealarm.ui.history

import ai.kun.socialdistancealarm.LiveBarcodeScanningActivity
import ai.kun.socialdistancealarm.R
import ai.kun.opentracesdk_fat.BLETrace
import ai.kun.socialdistancealarm.util.BarcodeEncoder
import ai.kun.socialdistancealarm.util.Constants
import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix;
import java.util.*


/**
 * The teams fragment allows users to ignore detections of other devices by creating a team.
 * We could have implemented this by having the user tap on another device, but we wanted
 * to make it be something where you consent to letting another user ignore the detections
 * of your device, so we used QR codes.  The QR code contains the user's UUID.
 *
 * Note that if you tap the reset button it forgets all of the UUIDs that have been scanned
 * AND it changes your UUID to a new one.  That way you can leave a team without interacting
 * with the other team member's devices and without the other team members having to form a new
 * team.
 *
 */
class TeamsFragment : Fragment() {
    private val TAG = "TeamsFragment"

    private val REQUEST_CAMERA = 4
    private val SCAN_ACTIVITY = 1

    private var scanMessage: Int? = null

    /**
     * Create the view with the reset button and the QR code on it which contains the current
     * UUID
     *
     * @param inflater The inflater
     * @param container the view group
     * @param savedInstanceState not used
     * @return An inflated view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val historyViewModel: HistoryViewModel by viewModels()
        val root = inflater.inflate(R.layout.fragment_teams, container, false)


        val cameraButton = root.findViewById<FloatingActionButton>(R.id.cameraButton)
        cameraButton.setOnClickListener {
            if (requireContext().applicationContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA);
            } else {
                scanBarcode()
            }
        }

        val resetButton = root.findViewById<TextView>(R.id.TextView_exit_teams)
        resetButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setCancelable(true)
                .setMessage(R.string.reset_dialog_message)
                .setTitle(R.string.are_you_sure)
                .setPositiveButton(R.string.ok, { dialogInterface: DialogInterface, i: Int ->
                    BLETrace.leaveTeam()
                    setQrCode()
                    setTeamCount()
                })
                .show()
        }

        return root
    }

    /**
     * set the QR code to be the current UUID
     *
     */
    private fun setQrCode() {
        // Show the QR Code...
        val barCodeImageView = view?.findViewById<ImageView>(R.id.barcodeImage)
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix: BitMatrix =
                multiFormatWriter.encode(BLETrace.uuidString, BarcodeFormat.QR_CODE, Constants.QR_CODE_SIZE, Constants.QR_CODE_SIZE)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)
            barCodeImageView?.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }

    }

    /**
     * set the count of the number of devices that you currently have UUID's for that will not
     * cause notifications and show as team members.
     *
     */
    private fun setTeamCount() {
        val teamCountTextView = view?.findViewById<TextView>(R.id.TextView_team_count)
        val text = getString(R.string.your_team_has_0_people)
        val count = BLETrace.teamUuids?.let { it.size } ?: 0
        teamCountTextView?.let { it.text = text.replace("0", count.toString(), true) }
    }

    /**
     * Show a scan message if we are resuming after a scan.
     */
    override fun onResume() {
        super.onResume()
        setQrCode()
        scanMessage?.let {
            val alertDialog = AlertDialog.Builder(context)
                .setMessage(it)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, { dialogInterface: DialogInterface, i: Int ->
                })
            if (it == R.string.scanned_this_app_will_be_added) {
                alertDialog.setTitle(R.string.scannned)
            } else {
                alertDialog.setTitle(R.string.try_again)
            }
            scanMessage = null

            alertDialog.show()
        }

        setTeamCount()
    }

    /**
     * We used some code from Google to do the barcode scanning.  This call starts the activity.
     *
     */
    private fun scanBarcode() {
        startActivityForResult(Intent(activity, LiveBarcodeScanningActivity::class.java), SCAN_ACTIVITY)
    }

    /**
     * Process the result of scanning storing the UUID of the device that was scanned in shared pref
     * so that we can ignore it as a team mate in other parts of the code.
     *
     * @param requestCode Should always be SCAN_ACTIVITY
     * @param resultCode Not used
     * @param data Should contain the UUID, but we validate it here.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SCAN_ACTIVITY) {
            if (data == null) {
                Log.w(TAG, "Data returned from intent was null.")
                scanMessage = R.string.nothing_scanned
            }
            data?.let {
                val uuidString = it.getStringExtra("UUID")
                if (uuidString == null) {
                    Log.w(TAG, "Data returned from intent had a UUID that was null.")
                    scanMessage = R.string.no_data_scanned
                }
                uuidString?.let {
                    try {
                        val uuid = UUID.fromString(uuidString)

                        scanMessage = R.string.scanned_this_app_will_be_added

                        var newSet = BLETrace.teamUuids!!.toMutableSet()
                        newSet.add(uuidString)
                        BLETrace.teamUuids = newSet
                    } catch (e: IllegalArgumentException) {
                        Log.w(TAG, "Data returned was not a UUID.")
                        scanMessage = R.string.you_didnt_scan
                    }
                }
            }
        }
    }

    /**
     * Act on camera permissions from the user
     *
     * @param requestCode the request code we set
     * @param permissions the permissions we requested
     * @param grantResults the grant results
     */
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
