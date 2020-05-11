package ai.kun.socialdistancealarm.ui.base

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment

abstract class BaseFragment: Fragment() {

    protected fun addFragment(@IdRes containerViewId: Int, fragment: Fragment, fragmentTag: String) {
        childFragmentManager
            .beginTransaction()
            .add(containerViewId, fragment, fragmentTag)
            .commit()
    }

    protected fun replaceFragment(@IdRes containerViewId: Int, fragment: Fragment, fragmentTag: String, backStackStateName: String?) {
        childFragmentManager
            .beginTransaction()
            .replace(containerViewId, fragment, fragmentTag)
            .addToBackStack(backStackStateName)
            .commit()
    }
}