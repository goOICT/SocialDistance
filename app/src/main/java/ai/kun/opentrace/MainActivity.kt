package ai.kun.opentrace

import ai.kun.opentrace.worker.BLETrace
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*


class MainActivity : AppCompatActivity()  {
    private val TAG = "MainActivity"

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
                    navView?.visibility = View.GONE
                    toolbar.visibility = View.GONE
                }
                else -> {
                    navView?.visibility = View.VISIBLE
                    toolbar.visibility = View.VISIBLE
                }
            }
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_history, R.id.navigation_home))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onDestroy() {
        super.onDestroy()
        BLETrace.start(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
         menu?.let {
             // Change the state of the toolbar depending on the state of BLETrace
             BLETrace.isStarted.addOnPropertyChangedCallback(object : androidx.databinding.Observable.OnPropertyChangedCallback() {
                 override fun onPropertyChanged(
                     sender: androidx.databinding.Observable?,
                     propertyId: Int
                 ) {
                    setPausePlayOption(menu)
                 }
             })

             // Initialize to the current state
             setPausePlayOption(menu)
         }
        return true
    }

    private fun setPausePlayOption(optionsMenu: Menu) {
        if (BLETrace.isStarted.get()) {
            optionsMenu.findItem(R.id.app_bar_pause).isVisible = true
            optionsMenu.findItem(R.id.app_bar_play).isVisible = false
        } else {
            optionsMenu.findItem(R.id.app_bar_pause).isVisible = false
            optionsMenu.findItem(R.id.app_bar_play).isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        when (id) {
            R.id.action_notification_settings -> {
                val intent = Intent()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, applicationContext.getPackageName())
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                } else {
                    intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    intent.putExtra("app_package", applicationContext.getPackageName())
                    intent.putExtra("app_uid", applicationContext.getApplicationInfo().uid)
                }
                applicationContext.startActivity(intent)
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
