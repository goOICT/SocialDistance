package ai.kun.socialdistancealarm.ui.home

import ai.kun.socialdistancealarm.R

import ai.kun.opentracesdk_fat.dao.Device
import ai.kun.opentracesdk_fat.util.BluetoothUtils
import ai.kun.opentracesdk_fat.util.Constants.SIGNAL_DISTANCE_LIGHT_WARN
import ai.kun.opentracesdk_fat.util.Constants.SIGNAL_DISTANCE_OK
import ai.kun.opentracesdk_fat.util.Constants.SIGNAL_DISTANCE_STRONG_WARN
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView

class DeviceListAdapter internal constructor(
    adapterContext: Context
) : RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(adapterContext)
    private val context: Context = adapterContext
    private var devices = emptyList<Device>()

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val distanceTextView: TextView = itemView.findViewById(R.id.textView_distance)
        val peopleImageView: ImageView = itemView.findViewById(R.id.imageView_people)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val itemView = inflater.inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val current = devices[position]

        // Notify the user when we are adding a device that's too close
        val signal = BluetoothUtils.calculateSignal(current.rssi, current.txPower, current.isAndroid)
        when {
            signal <= SIGNAL_DISTANCE_OK -> {
                holder.distanceTextView.text = context.resources.getString(R.string.safer)
                holder.peopleImageView.setImageResource(R.drawable.ic_person_good_icon)
                holder.peopleImageView.imageTintList = ColorStateList.valueOf(ResourcesCompat.getColor(context.resources, R.color.green, context.theme))
            }
            signal <= SIGNAL_DISTANCE_LIGHT_WARN -> {
                holder.distanceTextView.text = context.resources.getString(R.string.warning)
                holder.peopleImageView.setImageResource(R.drawable.ic_person_warning_icon)
                holder.peopleImageView.imageTintList = ColorStateList.valueOf(ResourcesCompat.getColor(context.resources, R.color.yellow, context.theme))
            }
            signal <= SIGNAL_DISTANCE_STRONG_WARN -> {
                holder.distanceTextView.text = context.resources.getString(R.string.strong_warning)
                holder.peopleImageView.setImageResource(R.drawable.ic_person_danger_icon)
                holder.peopleImageView.imageTintList = ColorStateList.valueOf(ResourcesCompat.getColor(context.resources, R.color.orange, context.theme))
            }
            else -> {
                holder.distanceTextView.text = context.resources.getString(R.string.too_close)
                holder.peopleImageView.setImageResource(R.drawable.ic_person_too_close_icon)
                holder.peopleImageView.imageTintList = ColorStateList.valueOf(ResourcesCompat.getColor(context.resources, R.color.red, context.theme))
            }
        }

        if (current.isTeamMember) {
            holder.distanceTextView.text = context.resources.getString(R.string.safer)
            holder.peopleImageView.setImageResource(R.drawable.ic_people_black_24dp)
            holder.peopleImageView.imageTintList = ColorStateList.valueOf(ResourcesCompat.getColor(context.resources, R.color.green, context.theme))
        }
    }

    internal fun setDevices(devices: List<Device>) {
        this.devices = devices
        notifyDataSetChanged()
    }

    override fun getItemCount() = devices.size
}