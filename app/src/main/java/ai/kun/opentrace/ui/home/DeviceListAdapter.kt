package ai.kun.opentrace.ui.home

import ai.kun.opentrace.R
import ai.kun.opentrace.dao.Device
import ai.kun.opentrace.util.Constants.SIGNAL_DISTANCE_LIGHT_WARN
import ai.kun.opentrace.util.Constants.SIGNAL_DISTANCE_OK
import ai.kun.opentrace.util.Constants.SIGNAL_DISTANCE_STRONG_WARN
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class DeviceListAdapter internal constructor(
    adapterContext: Context
) : RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(adapterContext)
    private val contxt: Context = adapterContext
    private var devices = emptyList<Device>()

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val constraintLayout: ConstraintLayout = itemView.findViewById((R.id.constraintLayout_device))
        val uuidTextView: TextView = itemView.findViewById(R.id.textView_uuid)
        val distanceTextView: TextView = itemView.findViewById(R.id.textView_distance)
        val signalTextView: TextView = itemView.findViewById(R.id.textView_signal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val itemView = inflater.inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val current = devices[position]
        holder.uuidTextView.text = current.deviceUuid

        val signal = current.txPower + current.rssi
        when {
            signal <= SIGNAL_DISTANCE_OK -> {
                holder.distanceTextView.text = contxt.resources.getString(R.string.ok)
                holder.constraintLayout.setBackgroundColor(contxt.resources.getColor(R.color.device_ok, contxt.theme))
            }
            signal <= SIGNAL_DISTANCE_LIGHT_WARN -> {
                holder.distanceTextView.text = contxt.resources.getString(R.string.warning)
                holder.constraintLayout.setBackgroundColor(contxt.resources.getColor(R.color.device_warn, contxt.theme))
            }
            signal <= SIGNAL_DISTANCE_STRONG_WARN -> {
                holder.distanceTextView.text = contxt.resources.getString(R.string.strong_warning)
                holder.constraintLayout.setBackgroundColor(contxt.resources.getColor(R.color.device_strong_warn, contxt.theme))
            }
            else -> {
                holder.distanceTextView.text = contxt.resources.getString(R.string.too_close)
                holder.constraintLayout.setBackgroundColor(contxt.resources.getColor(R.color.device_too_close, contxt.theme))
            }
        }

        holder.signalTextView.text = signal.toString()
    }

    internal fun setDevices(devices: List<Device>) {
        this.devices = devices
        notifyDataSetChanged()
    }

    override fun getItemCount() = devices.size
}