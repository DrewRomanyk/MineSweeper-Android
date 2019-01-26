package app.drewromanyk.com.minesweeper.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

import com.squareup.picasso.Picasso

import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.fragment.PlayFragment
import app.drewromanyk.com.minesweeper.fragment.StatsFragment
import app.drewromanyk.com.minesweeper.enums.ResultCodes
import app.drewromanyk.com.minesweeper.util.DialogInfoUtils
import com.crashlytics.android.Crashlytics
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.material.navigation.NavigationView
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by Drew on 1/10/2015.
 * MainActivity
 * Main view for users to select a new game or play an old one
 */

class MainActivity : AdsActivity() {
    //NAV DRAWER
    private var drawerLayout: DrawerLayout? = null
    private var navView: NavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupAds()

        setupDrawerContent(findViewById(R.id.drawer_layout), findViewById(R.id.nav_view))
        setupFragmentContent(savedInstanceState)
    }

    private fun setupDrawerContent(drawerLayout: DrawerLayout, navView: NavigationView) {
        this.drawerLayout = drawerLayout
        this.navView = navView
        val headerLayout = navView.getHeaderView(0)

        headerLayout.setOnClickListener { v ->
            if (isSignedIn()) {
                googleSignInClient!!.signOut().addOnCompleteListener(this) {
                    onDisconnected()
                }

                (navView.findViewById(R.id.name) as TextView).text = getString(R.string.nav_header_playername_empty)
                Picasso.with(v.context).load(R.drawable.person_image_empty).into(navView.findViewById<ImageView>(R.id.avatar))
                Picasso.with(v.context).load(R.color.nav_drawer_header_background).into(navView.findViewById<ImageView>(R.id.cover))
            } else {
                startSignInIntent()
            }
        }

        navView.menu.getItem(0).isChecked = true
        navView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            val transaction = supportFragmentManager.beginTransaction()
            when (menuItem.itemId) {
                R.id.nav_play -> {
                    transaction.replace(R.id.content_fragment, PlayFragment())
                    transaction.commit()
                }
                R.id.nav_leaderboards -> if (isSignedIn())
                    leaderboardsClient!!.allLeaderboardsIntent
                            .addOnSuccessListener { intent -> startActivityForResult(intent, RC_UNUSED) }
                else
                    showYesNoDialog(ResultCodes.NEEDGOOGLE_DIALOG.ordinal)
                R.id.nav_achievements -> if (isSignedIn())
                    achievementsClient!!.achievementsIntent
                            .addOnSuccessListener{ intent -> startActivityForResult(intent, RC_UNUSED) }
                else
                    showYesNoDialog(ResultCodes.NEEDGOOGLE_DIALOG.ordinal)
                R.id.nav_statistics -> {
                    transaction.replace(R.id.content_fragment, StatsFragment())
                    transaction.commit()
                }
                R.id.nav_help -> {
                    menuItem.isChecked = false
                    showYesNoDialog(ResultCodes.HELP_DIALOG.ordinal)
                }
                R.id.nav_about -> {
                    menuItem.isChecked = false
                    showYesNoDialog(ResultCodes.ABOUT_DIALOG.ordinal)
                }
                R.id.nav_settings -> {
                    menuItem.isChecked = false
                    startActivity(Intent(navView.context, SettingsActivity::class.java))
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun setupFragmentContent(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.content_fragment, PlayFragment())
                    .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /*
     * DIALOGS
     */

    private fun showYesNoDialog(requestCode: Int) {
        val (title, description) = DialogInfoUtils.getInstance(this).getDialogInfo(requestCode)
        val dialog = AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(description)
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    if (requestCode == ResultCodes.NEEDGOOGLE_DIALOG.ordinal) {
                        startSignInIntent()
                    }
                }
                .setNegativeButton(android.R.string.no) { _, _ -> }
                .create()
        dialog.show()
    }

    /*
     * GOOGLE PLAY GAMES
     */

    override fun onConnected(googleSignInAccount: GoogleSignInAccount) {
        super.onConnected(googleSignInAccount)

//        val nameDisplay = navViw!!.findViewById(R.id.name) as TextView
////        val avatar = navView!!.findViewById(R.id.avatar) as CircleImageView
////
////        nameDisplay.text = googleSignInAccount.displayName
////
////        var playerAvatarURL = googleSignInAccount.photoUrl.toString()
////        val index = playerAvatarURL.indexOf("?sz=")
////        if (index != -1) {
////            playerAvatarURL = playerAvatarURL.substring(0, index) + "?sz=200"
////        }
////
////        Picasso.with(thise).load(playerAvatarURL).placeholder(R.drawable.person_image_empty).into(avatar)
    }
}
