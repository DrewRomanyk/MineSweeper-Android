package app.drewromanyk.com.minesweeper.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.drewromanyk.com.minesweeper.R
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Fragment to help teach what Minesweeper is
 */
class HelpFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_help, container, false)
    }

    override fun onResume() {
        super.onResume()
        val firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        firebaseAnalytics.setCurrentScreen(requireActivity(), AboutFragment::javaClass.javaClass.simpleName, AboutFragment::javaClass.javaClass.simpleName)
    }
}
