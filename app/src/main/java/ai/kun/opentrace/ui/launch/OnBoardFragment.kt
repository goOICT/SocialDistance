package ai.kun.opentrace.ui.launch

import ai.kun.opentrace.R
import ai.kun.opentrace.util.Constants
import ai.kun.opentrace.worker.BLETrace
import android.content.Context
import android.content.SharedPreferences
import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomnavigation.BottomNavigationView

private const val ONBOARD_ONE = 1
private const val ONBOARD_TWO = 2
private const val ONBOARD_THREE = 3

/**
 * A simple [Fragment] subclass.
 * Use the [OnBoardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OnBoardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_on_board, container, false)
        return view
    }

    val args: OnBoardFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        when (args.screen) {
            ONBOARD_ONE -> {
                view.findViewById<TextView>(R.id.section_label).text = getText(R.string.onboard_one_section_label)
                view.findViewById<TextView>(R.id.section_description).text = getText(R.string.onboard_one_section_description)
                view.findViewById<FrameLayout>(R.id.onboard_frame_layout).setBackgroundColor(context!!.getColor(R.color.design_default_color_secondary))
                view.findViewById<Button>(R.id.intro_btn_finish).isGone = true
                view.findViewById<Button>(R.id.intro_btn_next).isGone = false
                view.findViewById<ImageButton>(R.id.intro_btn_next).setOnClickListener {
                    findNavController().navigate(R.id.action_onBoardFragment_1_to_onBoardFragment_2)
                }
                view.findViewById<Button>(R.id.intro_btn_skip).isGone = false
                view.findViewById<Button>(R.id.intro_btn_skip).setOnClickListener {
                    onBoardCompleted()
                    findNavController().navigate(R.id.action_onBoardFragment_1_to_navigation_home)
                }
            }
            ONBOARD_TWO -> {
                view.findViewById<TextView>(R.id.section_label).text = getText(R.string.onboard_two_section_label)
                view.findViewById<TextView>(R.id.section_description).text = getText(R.string.onboard_two_section_description)
                view.findViewById<FrameLayout>(R.id.onboard_frame_layout).setBackgroundColor(context!!.getColor(R.color.design_default_color_primary))
                view.findViewById<Button>(R.id.intro_btn_finish).isGone = true
                view.findViewById<Button>(R.id.intro_btn_next).isGone = false
                view.findViewById<ImageButton>(R.id.intro_btn_next).setOnClickListener {
                    findNavController().navigate(R.id.action_onBoardFragment_2_to_onBoardFragment_3)
                }
                view.findViewById<Button>(R.id.intro_btn_skip).isGone = false
                view.findViewById<Button>(R.id.intro_btn_skip).setOnClickListener {
                    onBoardCompleted()
                    findNavController().navigate(R.id.action_onBoardFragment_2_to_navigation_home)
                }
            }
            ONBOARD_THREE -> {
                view.findViewById<TextView>(R.id.section_label).text = getText(R.string.onboard_three_section_label)
                view.findViewById<TextView>(R.id.section_description).text = getText(R.string.onboard_three_section_description)
                view.findViewById<FrameLayout>(R.id.onboard_frame_layout).setBackgroundColor(context!!.getColor(R.color.design_default_color_secondary_variant))
                view.findViewById<Button>(R.id.intro_btn_finish).isGone = false
                view.findViewById<Button>(R.id.intro_btn_finish).setOnClickListener {
                    onBoardCompleted()
                    findNavController().navigate(R.id.action_onBoardFragment_3_to_navigation_home)
                }
                view.findViewById<ImageButton>(R.id.intro_btn_next).isGone = true
                view.findViewById<Button>(R.id.intro_btn_skip).isGone = true
            }
        }
    }

    private fun onBoardCompleted() {
        val sharedPrefs = BLETrace.context.getSharedPreferences(
            Constants.PREF_FILE_NAME, Context.MODE_PRIVATE
        )
        val editor: SharedPreferences.Editor = sharedPrefs.edit()
        editor.putBoolean(Constants.PREF_IS_ONBOARDED, true)
        editor.apply()
    }

}
