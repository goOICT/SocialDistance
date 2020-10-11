package ai.kun.socialdistancealarm

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Sadly there were not enough resources to do test automation on this project and still deliver it
 * to the people that need it.  It would have been good, but BLE functionality would not have been
 * easy to test, so we just did it old school with lots of handsets, and lots of fingers,
 * and lots of crash reporting.
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("ai.kun.socialdistancealarm", appContext.packageName)
    }
}
