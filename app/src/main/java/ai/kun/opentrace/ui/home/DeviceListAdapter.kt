package ai.kun.opentrace.ui.home

import ai.kun.opentrace.R
import ai.kun.opentrace.dao.Device
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DeviceListAdapter internal constructor(
    context: Context
) : RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var devices = emptyList<Device>()

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        holder.distanceTextView.text = current.distance.toString()
        holder.signalTextView.text = (current.txPower + current.rssi).toString()
    }

    internal fun setDevices(devices: List<Device>) {
        this.devices = devices
        notifyDataSetChanged()
    }

    override fun getItemCount() = devices.size
}