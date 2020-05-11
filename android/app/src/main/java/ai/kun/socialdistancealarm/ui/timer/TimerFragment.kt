package ai.kun.socialdistancealarm.ui.timer

import ai.kun.socialdistancealarm.R
import ai.kun.socialdistancealarm.alarm.BLETrace
import ai.kun.socialdistancealarm.ui.home.HomeViewModel
import ai.kun.socialdistancealarm.util.PrefUtil
import ai.kun.socialdistancealarm.util.PrefUtil.TIMER_STATE_ID
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.fragment_timer.*
import java.util.*
import kotlin.concurrent.schedule

class TimerFragment : Fragment(), OnSharedPreferenceChangeListener {

    companion object {
        fun newInstance(): TimerFragment {
            return TimerFragment()
        }
    }

    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds: Long = 0
    private var timerState = TimerState.Stopped
    private var secondsRemaining: Long = 0
    private lateinit var sharedPreferenceChangeListener : OnSharedPreferenceChangeListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_timer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val homeViewModel: HomeViewModel by viewModels()
        context?.let { context ->
            homeViewModel.isStarted.observe(viewLifecycleOwner, Observer { isStarted ->
                isStarted?.let {
                    // If BLE is started then
                    if (timerState != TimerState.Running) {
                        Timer("BLE Two Ticks", false).schedule(2000) {
                            activity?.runOnUiThread {
                                startTimer(context)
                                timerState = TimerState.Running
                            }
                        }
                    }
                }
            })

            sharedPreferenceChangeListener = this
            val prefs: SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(activity)

            prefs.registerOnSharedPreferenceChangeListener(
                sharedPreferenceChangeListener
            )
        }


    }

    private fun startTimer(context: Context) {
        BLETrace.stop()
        timerState = TimerState.Running
        val twoTicks = timerLengthSeconds

        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onFinish() = onTimerFinished(context)

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000

                updateCountdownUI()
            }
        }.start()
    }

    private fun updateCountdownUI() {
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        textView_countdown?.text =
            "$minutesUntilFinished:${if (secondsStr.length == 2) secondsStr else "0" + secondsStr}"
    }


    private fun onTimerFinished(context: Context) {
        timerState = TimerState.Stopped
        BLETrace.start(true)

        //set the length of the timer to be the one set in SettingsActivity
        //if the length was changed when the timer was running
        setNewTimerLength(context)

        activity?.let { PrefUtil.setSecondsRemaining(timerLengthSeconds, it) }
        secondsRemaining = timerLengthSeconds
        updateCountdownUI()
    }

    private fun setNewTimerLength(context: Context) {
        timerLengthSeconds = PrefUtil.getTimerLength(context).toLong()
    }

    private fun setPreviousTimerLength(context: Context) {
        timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(context)
    }

    override fun onResume() {
        super.onResume()
        context?.let { context ->
            initTimer(context)
        }
    }

    override fun onPause() {
        super.onPause()

        context?.let { context ->

            if (timerState == TimerState.Running) {
                timer.cancel()
            }

            PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, context)
            PrefUtil.setSecondsRemaining(secondsRemaining, context)
            PrefUtil.setTimerState(timerState, context)
        }
    }

    private fun initTimer(context: Context) {

        timerState = PrefUtil.getTimerState(context)

        //we don't want to change the length of the timer which is already running
        //if the length was changed in settings while it was backgrounded
        if (timerState == TimerState.Stopped)
            setNewTimerLength(context)
        else
            setPreviousTimerLength(context)

        secondsRemaining = (if (timerState == TimerState.Running || timerState == TimerState.Paused)
             PrefUtil.getSecondsRemaining(context)
        else
            timerLengthSeconds)

        if (secondsRemaining <= 0)
            onTimerFinished(context)
        else if (timerState == TimerState.Running)
            startTimer(context)
        updateCountdownUI()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

        context?.let { context ->

            if (key == TIMER_STATE_ID) {
                val timerState = PrefUtil.getTimerState(context)
                Log.d("Timer State", timerState.toString())
                when (timerState) {
                    TimerState.Stopped -> {
                        timer.cancel()
                        onTimerFinished(context)
                    }
                    TimerState.Paused -> {
                        timer.cancel()
                        this.timerState = TimerState.Paused
                    }else -> return
                }
            }
        }
    }


}
