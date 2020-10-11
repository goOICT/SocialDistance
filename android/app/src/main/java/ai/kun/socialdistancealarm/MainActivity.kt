package ai.kun.socialdistancealarm

import ai.kun.opentracesdk_fat.BLETrace
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView


/**
 * This is a fragment based application so this is the high level activity.
 *
 */
class MainActivity : AppCompatActivity()  {
    private val TAG = "MainActivity"

    var isToolbarPausePlayHidden = false

    /**
     * Hide and show the nav bar and the tool bar as needed.
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.onBoardFragment_1,
                R.id.onBoardFragment_2,
                R.id.onBoardFragment_3,
                R.id.onBoardFragment_4,
                R.id.launchFragment-> {
                    navView.visibility = View.GONE
                    toolbar.visibility = View.GONE
                }
                R.id.navigation_home -> {
                    navView.visibility = View.VISIBLE
                    toolbar.visibility = View.VISIBLE
                    isToolbarPausePlayHidden = true
                    invalidateOptionsMenu()
                }
                else -> {
                    navView.visibility = View.VISIBLE
                    toolbar.visibility = View.VISIBLE
                    isToolbarPausePlayHidden = false
                    invalidateOptionsMenu()
                }
            }
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_history, R.id.navigation_home, R.id.navigation_teams))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    /**
     * restart the underling BLE lib in background mode.
     */
    override fun onDestroy() {
        super.onDestroy()
        BLETrace.start(true)
    }

    /**
     * set up the options menu
     *
     * @param menu the options meny
     * @return true
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        menu?.let {
            // Change the state of the toolbar depending on the state of BLETrace
            BLETrace.isStarted.observeForever(Observer { isStarted ->
                setPausePlayOption(it, isStarted)
            })

            // Initialize to the current state
            setPausePlayOption(it, BLETrace.isStarted.value)
        }

        return true
    }

    /**
     * set up the pause play option
     *
     * @param optionsMenu the menu
     * @param isStarted the state of the scanning
     */
    private fun setPausePlayOption(optionsMenu: Menu, isStarted: Boolean?) {
        isStarted?.let {
            if (isToolbarPausePlayHidden) {
                optionsMenu.findItem(R.id.app_bar_pause).isVisible = false
                optionsMenu.findItem(R.id.app_bar_play).isVisible = false
            } else {
                if (it) {
                    optionsMenu.findItem(R.id.app_bar_pause).isVisible = true
                    optionsMenu.findItem(R.id.app_bar_play).isVisible = false
                } else {
                    optionsMenu.findItem(R.id.app_bar_pause).isVisible = false
                    optionsMenu.findItem(R.id.app_bar_play).isVisible = true
                }
            }
        }
    }

    /**
     * Handle action bar item clicks here. The action bar will
     * automatically handle clicks on the Home/Up button, so long
     * as you specify a parent activity in AndroidManifest.xml.
     *
     * @param item items menu
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId
        when (id) {
            R.id.action_notification_settings -> {
                val intent = Intent()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, applicationContext.getPackageName())
                } else {
                    intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    intent.putExtra("app_package", applicationContext.getPackageName())
                    intent.putExtra("app_uid", applicationContext.getApplicationInfo().uid)
                }
                startActivity(intent)

                return true
            }
            R.id.app_bar_pause -> {
                BLETrace.isPaused = true
            }
            R.id.app_bar_play -> {
                BLETrace.isPaused = false
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
