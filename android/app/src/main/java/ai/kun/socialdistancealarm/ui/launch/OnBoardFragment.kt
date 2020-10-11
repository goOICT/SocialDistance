package ai.kun.socialdistancealarm.ui.launch

import ai.kun.socialdistancealarm.R
import ai.kun.socialdistancealarm.util.Constants
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

private const val ONBOARD_ONE = 1
private const val ONBOARD_TWO = 2
private const val ONBOARD_THREE = 3
private const val ONBOARD_FOUR = 4

/**
 * Display the different screens for the onboarding process.
 *
 * So this is not built the way that it should be.  In the original design there were different layouts
 * in the onboarding screens, so using a recycler view and a single fragment didn't work, but
 * we changed the designs so that they could all use the same fragment and this code didn't get
 * changed to match.
 */
class OnBoardFragment : Fragment() {

    /**
     * Inflate the view
     *
     * @param inflater the inflater
     * @param container the container
     * @param savedInstanceState not used
     * @return An inflated view
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_on_board, container, false)
        return view
    }

    val args: OnBoardFragmentArgs by navArgs()

    /**
     * Set things into the view.  This is the part that really should just be using a recycler view.
     *
     * @param view The view
     * @param savedInstanceState not used
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        when (args.screen) {
            ONBOARD_ONE -> {
                view.findViewById<TextView>(R.id.section_label).text = getText(R.string.onboard_one_section_label)
                view.findViewById<TextView>(R.id.section_description).text = getText(R.string.onboard_one_section_description)
                view.findViewById<ImageView>(R.id.section_img).background = resources.getDrawable(R.drawable.intro_svg, null)

                view.findViewById<ImageView>(R.id.intro_indicator_0).isSelected = true
                view.findViewById<ImageView>(R.id.intro_indicator_1).isSelected = false
                view.findViewById<ImageView>(R.id.intro_indicator_2).isSelected = false
                view.findViewById<ImageView>(R.id.intro_indicator_3).isSelected = false

                view.findViewById<Button>(R.id.intro_btn_finish).isGone = true
                view.findViewById<Button>(R.id.intro_btn_next).isGone = false
                view.findViewById<ImageButton>(R.id.intro_btn_next).setOnClickListener {
                    findNavController().navigate(R.id.action_onBoardFragment_1_to_onBoardFragment_2)
                }
            }
            ONBOARD_TWO -> {
                view.findViewById<TextView>(R.id.section_label).text = getText(R.string.onboard_two_section_label)
                view.findViewById<TextView>(R.id.section_description).text = getText(R.string.onboard_two_section_description)
                view.findViewById<ImageView>(R.id.section_img).background = resources.getDrawable(R.drawable.teams_svg, null)

                view.findViewById<ImageView>(R.id.intro_indicator_0).isSelected = false
                view.findViewById<ImageView>(R.id.intro_indicator_1).isSelected = true
                view.findViewById<ImageView>(R.id.intro_indicator_2).isSelected = false
                view.findViewById<ImageView>(R.id.intro_indicator_3).isSelected = false

                view.findViewById<Button>(R.id.intro_btn_finish).isGone = true
                view.findViewById<Button>(R.id.intro_btn_next).isGone = false
                view.findViewById<ImageButton>(R.id.intro_btn_next).setOnClickListener {
                    findNavController().navigate(R.id.action_onBoardFragment_2_to_onBoardFragment_3)
                }
            }
            ONBOARD_THREE -> {
                view.findViewById<TextView>(R.id.section_label).text = getText(R.string.onboard_three_section_label)
                view.findViewById<TextView>(R.id.section_description).text = getText(R.string.onboard_three_section_description)
                view.findViewById<ImageView>(R.id.section_img).background = resources.getDrawable(R.drawable.paused_svg, null)

                view.findViewById<ImageView>(R.id.intro_indicator_0).isSelected = false
                view.findViewById<ImageView>(R.id.intro_indicator_1).isSelected = false
                view.findViewById<ImageView>(R.id.intro_indicator_2).isSelected = true
                view.findViewById<ImageView>(R.id.intro_indicator_3).isSelected = false

                view.findViewById<Button>(R.id.intro_btn_finish).isGone = true
                view.findViewById<Button>(R.id.intro_btn_next).isGone = false
                view.findViewById<ImageButton>(R.id.intro_btn_next).setOnClickListener {
                    findNavController().navigate(R.id.action_onBoardFragment_3_to_onBoardFragment_4)
                }
            }
            ONBOARD_FOUR -> {
                view.findViewById<TextView>(R.id.section_label).text = getText(R.string.onboard_four_section_label)
                view.findViewById<TextView>(R.id.section_description).text = getText(R.string.onboard_four_section_description)
                view.findViewById<ImageView>(R.id.section_img).background = resources.getDrawable(R.mipmap.map_svg, null)

                view.findViewById<ImageView>(R.id.intro_indicator_0).isSelected = false
                view.findViewById<ImageView>(R.id.intro_indicator_1).isSelected = false
                view.findViewById<ImageView>(R.id.intro_indicator_2).isSelected = false
                view.findViewById<ImageView>(R.id.intro_indicator_3).isSelected = true

                view.findViewById<Button>(R.id.intro_btn_finish).isGone = false
                view.findViewById<Button>(R.id.intro_btn_finish).setOnClickListener {
                    onBoardCompleted()
                    findNavController().navigate(R.id.action_onBoardFragment_4_to_navigation_home)
                }
                view.findViewById<ImageButton>(R.id.intro_btn_next).isGone = true
            }
        }
    }

    /**
     * Once the user has seen all the onboarding screens mark it as complete in the shared prefs.
     *
     */
    private fun onBoardCompleted() {
        context?.let {
            val sharedPrefs = it.applicationContext.getSharedPreferences(
                Constants.PREF_FILE_NAME, Context.MODE_PRIVATE
            )
            val editor: SharedPreferences.Editor = sharedPrefs.edit()
            editor.putBoolean(Constants.PREF_IS_ONBOARDED, true)
            editor.apply()
        }
    }

}
