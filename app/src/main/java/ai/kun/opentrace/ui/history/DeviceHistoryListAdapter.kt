package ai.kun.opentrace.ui.history

import ai.kun.opentrace.R
import ai.kun.opentrace.dao.Device
import ai.kun.opentrace.util.Constants.SIGNAL_DISTANCE_LIGHT_WARN
import ai.kun.opentrace.util.Constants.SIGNAL_DISTANCE_OK
import ai.kun.opentrace.util.Constants.SIGNAL_DISTANCE_STRONG_WARN
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class DeviceHistoryListAdapter internal constructor(
    adapterContext: Context
) : RecyclerView.Adapter<DeviceHistoryListAdapter.DeviceViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(adapterContext)
    private val context: Context = adapterContext
    private var devices = emptyList<Device>()

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val distanceTextView: TextView = itemView.findViewById(R.id.textView_distance)
        val signalTextView: TextView = itemView.findViewById(R.id.textView_signal)
        val peopleImageView: ImageView = itemView.findViewById(R.id.imageView_people)
        val bluetoothImageView: ImageView = itemView.findViewById(R.id.imageView_bluetooth_signal_too_close_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val itemView = inflater.inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val current = devices[position]

        val signal = current.txPower + current.rssi
        when {
            signal <= SIGNAL_DISTANCE_OK -> {
                holder.distanceTextView.text = context.resources.getString(R.string.ok)
                holder.peopleImageView.imageTintList = ColorStateList.valueOf(context.resources.getColor(R.color.green, context.theme))
                holder.bluetoothImageView.imageTintList = ColorStateList.valueOf(context.resources.getColor(R.color.green, context.theme))
            }
            signal <= SIGNAL_DISTANCE_LIGHT_WARN -> {
                holder.distanceTextView.text = context.resources.getString(R.string.warning)
                holder.peopleImageView.imageTintList = ColorStateList.valueOf(context.resources.getColor(R.color.yellow, context.theme))
                holder.bluetoothImageView.imageTintList = ColorStateList.valueOf(context.resources.getColor(R.color.yellow, context.theme))           }
            signal <= SIGNAL_DISTANCE_STRONG_WARN -> {
                holder.distanceTextView.text = context.resources.getString(R.string.strong_warning)
                holder.peopleImageView.imageTintList = ColorStateList.valueOf(context.resources.getColor(R.color.orange, context.theme))
                holder.bluetoothImageView.imageTintList = ColorStateList.valueOf(context.resources.getColor(R.color.orange, context.theme))            }
            else -> {
                holder.distanceTextView.text = context.resources.getString(R.string.too_close)
                holder.peopleImageView.imageTintList = ColorStateList.valueOf(context.resources.getColor(R.color.red, context.theme))
                holder.bluetoothImageView.imageTintList = ColorStateList.valueOf(context.resources.getColor(R.color.red, context.theme))            }
        }

        holder.signalTextView.text = signal.toString()
    }

    internal fun setDevices(devices: List<Device>) {
        this.devices = devices
        notifyDataSetChanged()
    }

    override fun getItemCount() = devices.size
}