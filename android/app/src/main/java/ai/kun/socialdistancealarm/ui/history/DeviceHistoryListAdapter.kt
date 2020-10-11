package ai.kun.socialdistancealarm.ui.history

import ai.kun.socialdistancealarm.R

import ai.kun.opentracesdk_fat.dao.Device
import ai.kun.opentracesdk_fat.util.BluetoothUtils
import ai.kun.opentracesdk_fat.util.Constants.SIGNAL_DISTANCE_LIGHT_WARN
import ai.kun.opentracesdk_fat.util.Constants.SIGNAL_DISTANCE_OK
import ai.kun.opentracesdk_fat.util.Constants.SIGNAL_DISTANCE_STRONG_WARN
import ai.kun.opentracesdk_fat.util.Constants.TIME_FORMAT

import ai.kun.socialdistancealarm.util.DateUtils.getFormattedDateString
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
 * Lists the history of contacts with other users of the application.  We wanted to turn this
 * into some form of gamification similar to what the other applications have done, but we didn't
 * have the design resources to do it.
 *
 * @constructor
 * Constructs an adapter for the list view for device history
 *
 * @param adapterContext The adapter context to use
 */
class DeviceHistoryListAdapter internal constructor(
    adapterContext: Context
) : RecyclerView.Adapter<DeviceHistoryListAdapter.DeviceViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(adapterContext)
    private val context: Context = adapterContext
    private var devices = emptyList<Device>()

    /**
     * The view holder
     *
     * @constructor
     * populate the view with distance, time, etc.
     *
     * @param itemView The view to populate
     */
    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val distanceTextView: TextView = itemView.findViewById(R.id.textView_distance)
        val signalTextView: TextView = itemView.findViewById(R.id.textView_signal)
        val timestampTextView: TextView = itemView.findViewById(R.id.textView_timestamp)
        val peopleImageView: ImageView = itemView.findViewById(R.id.imageView_people)
    }

    /**
     * Inflate the layout
     *
     * @param parent The parent view group
     * @param viewType The view type
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val itemView = inflater.inflate(R.layout.item_device_history, parent, false)
        return DeviceViewHolder(itemView)
    }

    /**
     * Translate the values in the holder into the correct UI.
     * The code below should have be DRYed, but I was in a hurry.  It's nearly identical to what
     * is done for home.  Again the plan was to eventually change this to something that had
     * gamification, but the priority was launching iOS and apple didn't let us launch.
     *
     * @param holder The holder to set up
     * @param position The position of the holder
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

        if (current.isTeamMember) {
            holder.distanceTextView.text = context.resources.getString(R.string.safer)
            holder.peopleImageView.setImageResource(R.drawable.ic_people_black_24dp)
            holder.peopleImageView.imageTintList = ColorStateList.valueOf(ResourcesCompat.getColor(context.resources, R.color.green, context.theme))
        }

        holder.signalTextView.text = signal.toString()
        holder.timestampTextView.text = getFormattedDateString(TIME_FORMAT, current.timeStamp)
    }

    /**
     * Update the device list.  The history view shows a live list of devices.
     *
     * @param devices The devices in the current view
     */
    internal fun setDevices(devices: List<Device>) {
        this.devices = devices
        notifyDataSetChanged()
    }

    /**
     * Get the number of detected devices
     *
     */
    override fun getItemCount() = devices.size
}