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

/**
 * List adapter for showing devices in the home fragment.  Note that this is nearly identical to
 * the code in the history fragment.  The plan was to keep this and make history look different using
 * some gamification, but since Apple pulled the application from the App Store we gave up on doing
 * more.
 *
 * @constructor
 * Build the list adapter for devices that have been detected
 *
 * @param adapterContext The context to use
 */
class DeviceListAdapter internal constructor(
    adapterContext: Context
) : RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(adapterContext)
    private val context: Context = adapterContext
    private var devices = emptyList<Device>()

    /**
     * A room with a view holder for each device detected in the last detection cycle.
     *
     * @constructor
     * Build the holder and associate the correct live data.
     *
     * @param itemView
     */
    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val distanceTextView: TextView = itemView.findViewById(R.id.textView_distance)
        val peopleImageView: ImageView = itemView.findViewById(R.id.imageView_people)
    }

    /**
     * Inflate the holder
     *
     * @param parent View group
     * @param viewType View type
     * @return an inflated device view holder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val itemView = inflater.inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(itemView)
    }

    /**
     * Correctly translate the values from the database into what we show in the view. The database
     * just contains a device type and a signal strength.  This is where we decide that it's too
     * close and show it as red, etc.
     *
     * @param holder Device holder to bind to
     * @param position Position in the list
     */
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

    /**
     * Allow the devices to be changed
     *
     * @param devices The last scanned set of devices
     */
    internal fun setDevices(devices: List<Device>) {
        this.devices = devices
        notifyDataSetChanged()
    }

    /**
     * The size of the list is based on the number of devices in the last scan cycle sampled from
     * the database.
     *
     */
    override fun getItemCount() = devices.size
}