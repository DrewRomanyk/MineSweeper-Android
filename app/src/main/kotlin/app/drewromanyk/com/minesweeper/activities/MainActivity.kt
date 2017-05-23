package app.drewromanyk.com.minesweeper.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AlertDialog
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView

import com.google.android.gms.games.Games
import com.google.android.gms.plus.Plus
import com.squareup.picasso.Picasso

import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.fragment.PlayFragment
import app.drewromanyk.com.minesweeper.fragment.StatsFragment
import app.drewromanyk.com.minesweeper.enums.ResultCodes
import app.drewromanyk.com.minesweeper.util.DialogInfoUtils
import app.drewromanyk.com.minesweeper.util.Helper
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

        setupDrawerContent(findViewById(R.id.drawer_layout) as DrawerLayout, findViewById(R.id.nav_view) as NavigationView)
        setupFragmentContent(savedInstanceState)
    }

    private fun setupDrawerContent(drawerLayout: DrawerLayout, navView: NavigationView) {
        this.drawerLayout = drawerLayout
        this.navView = navView
        val headerLayout = navView.getHeaderView(0)

        headerLayout.setOnClickListener { v ->
            if (googleApiClient!!.isConnected) {
                Games.signOut(googleApiClient!!)
                googleApiClient!!.disconnect()

                (navView.findViewById(R.id.name) as TextView).text = getString(R.string.nav_header_playername_empty)
                Picasso.with(v.context).load(R.drawable.person_image_empty).into(navView.findViewById(R.id.avatar) as ImageView)
                Picasso.with(v.context).load(R.color.nav_drawer_header_background).into(navView.findViewById(R.id.cover) as ImageView)
            } else {
                onSignInClick()
                googleApiClient!!.connect()
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
                R.id.nav_leaderboards -> if (googleApiClient!!.isConnected)
                    startActivityForResult(Games.Leaderboards
                            .getAllLeaderboardsIntent(googleApiClient), ResultCodes.LEADERBOARDS.ordinal)
                else
                    showYesNoDialog(ResultCodes.NEEDGOOGLE_DIALOG.ordinal)
                R.id.nav_achievements -> if (googleApiClient!!.isConnected)
                    startActivityForResult(Games.Achievements
                            .getAchievementsIntent(googleApiClient), ResultCodes.ACHIEVEMENTS.ordinal)
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

    fun showYesNoDialog(requestCode: Int) {
        val (title, description) = DialogInfoUtils.getInstance(this).getDialogInfo(requestCode)
        val dialog = AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(description)
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    if (requestCode == ResultCodes.NEEDGOOGLE_DIALOG.ordinal) {
                        onSignInClick()
                        googleApiClient!!.connect()
                    }
                }
                .setNegativeButton(android.R.string.no) { _, _ -> }
                .create()
        dialog.show()
    }

    /*
     * GOOGLE PLAY GAMES
     */

    override fun onConnected(bundle: Bundle?) {
        if (Helper.isOnline(this) and (Plus.PeopleApi.getCurrentPerson(googleApiClient) != null)) {
            val currentPerson = Plus.PeopleApi.getCurrentPerson(googleApiClient)
            val nameDisplay = navView!!.findViewById(R.id.name) as TextView
            val avatar = navView!!.findViewById(R.id.avatar) as CircleImageView
            val cover = navView!!.findViewById(R.id.cover) as ImageView

            nameDisplay.text = currentPerson.displayName

            var playerAvatarURL = currentPerson.image.url
            val index = playerAvatarURL.indexOf("?sz=")
            if (index != -1) {
                playerAvatarURL = playerAvatarURL.substring(0, index) + "?sz=200"
            }

            Picasso.with(this).load(playerAvatarURL).placeholder(R.drawable.person_image_empty).into(avatar)

            if (currentPerson.cover?.hasCoverPhoto() as Boolean) {
                val playerCoverURL = currentPerson.cover.coverPhoto.url
                Picasso.with(this).load(playerCoverURL).placeholder(R.color.nav_drawer_header_background).into(cover)
            }
        }
    }
}
