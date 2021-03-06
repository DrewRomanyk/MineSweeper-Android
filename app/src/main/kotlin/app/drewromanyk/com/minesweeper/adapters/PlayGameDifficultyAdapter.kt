package app.drewromanyk.com.minesweeper.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.google.firebase.analytics.FirebaseAnalytics

import java.util.ArrayList

import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.enums.GameDifficulty
import app.drewromanyk.com.minesweeper.interfaces.PlayNavigator
import app.drewromanyk.com.minesweeper.util.UserPrefStorage

/**
 * Created by Drew on 12/11/15.
 * PlayGameDifficultyAdapter
 * RecyclerView to show list of difficulties to play along with optional cards to show such as
 * resume and rating
 */
class PlayGameDifficultyAdapter(private val navigator: PlayNavigator) : RecyclerView.Adapter<PlayGameDifficultyAdapter.PlayViewHolder>() {
    companion object {

        private const val RATING_TYPE = 0
        private const val DIFFICULTY_TYPE = 1

        private const val RATING_FIRST = 0
        private const val RATING_NO = 1
        private const val RATING_YES = 2

        private const val FIRST_ITEM = 0
    }

    private val gameDifficultyList: MutableList<GameDifficulty>
    private var canShowRating: Boolean = false

    inner class PlayViewHolder(itemView: View, itemViewType: Int) : RecyclerView.ViewHolder(itemView) {

        var card: View = itemView.findViewById(R.id.card)

        var ratingDesc: TextView? = null
        var ratingNo: Button? = null
        var ratingYes: Button? = null
        var dialogRatingPosition = 0

        var difficultyText: TextView? = null
        var boardInfoText: TextView? = null

        init {
            if (itemViewType == RATING_TYPE) {
                ratingDesc = itemView.findViewById(R.id.rating_desc) as TextView
                ratingNo = itemView.findViewById(R.id.rating_no) as Button
                ratingYes = itemView.findViewById(R.id.rating_yes) as Button
            } else if (itemViewType == DIFFICULTY_TYPE) {
                difficultyText = itemView.findViewById(R.id.card_difficulty_text) as TextView
                boardInfoText = itemView.findViewById(R.id.card_board_info) as TextView
            }
        }
    }

    init {
        gameDifficultyList = ArrayList()
    }

    fun setGameDifficultyList(gameDifficultyList: ArrayList<GameDifficulty>) {
        this.gameDifficultyList.clear()
        this.gameDifficultyList.addAll(gameDifficultyList)
        notifyDataSetChanged()
    }

    fun setCanShowRating(canShowRating: Boolean) {
        this.canShowRating = canShowRating
    }

    override fun getItemViewType(position: Int): Int {
        return if (canShowRating && (position == FIRST_ITEM)) {
            RATING_TYPE
        } else {
            DIFFICULTY_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayViewHolder {
        return if (viewType == RATING_TYPE) {
            PlayViewHolder(View.inflate(parent.context, R.layout.card_play_feedback, null), viewType)
        } else {
            PlayViewHolder(View.inflate(parent.context, R.layout.card_play_difficulty, null), viewType)
        }
    }

    override fun onBindViewHolder(holder: PlayViewHolder, position: Int) {
        if (getItemViewType(position) == RATING_TYPE) {
            val fbAnalytics = FirebaseAnalytics.getInstance(holder.card.context)
            holder.ratingYes!!.setOnClickListener {
                holder.ratingYes!!.setText(R.string.in_app_rating_secondary_yes)
                holder.ratingNo!!.setText(R.string.in_app_rating_secondary_no)

                when {
                    holder.dialogRatingPosition == RATING_FIRST -> {
                        fbAnalytics.logEvent("REVIEW_CARD_YES_ENJOYED", null)

                        holder.dialogRatingPosition = RATING_YES
                        holder.ratingDesc!!.setText(R.string.in_app_rating_rating)
                    }
                    holder.dialogRatingPosition == RATING_YES -> {
                        fbAnalytics.logEvent("REVIEW_CARD_RATE", null)

                        canShowRating = false
                        notifyDataSetChanged()
                        UserPrefStorage.setHasFinishedRatingDialog(holder.card.context)
                        navigator.startPlayStore()
                    }
                    holder.dialogRatingPosition == RATING_NO -> {
                        fbAnalytics.logEvent("REVIEW_CARD_FEEDBACK", null)

                        canShowRating = false
                        notifyDataSetChanged()
                        UserPrefStorage.setHasFinishedRatingDialog(holder.card.context)
                        navigator.sendFeedback()
                    }
                }
            }
            holder.ratingNo!!.setOnClickListener {
                holder.ratingYes!!.setText(R.string.in_app_rating_secondary_yes)
                holder.ratingNo!!.setText(R.string.in_app_rating_secondary_no)

                when {
                    holder.dialogRatingPosition == RATING_FIRST -> {
                        fbAnalytics.logEvent("REVIEW_CARD_NO_ENJOYED", null)

                        holder.dialogRatingPosition = RATING_NO
                        holder.ratingDesc!!.setText(R.string.in_app_rating_feedback)
                    }
                    holder.dialogRatingPosition == RATING_YES -> {
                        fbAnalytics.logEvent("REVIEW_CARD_NO_RATE", null)

                        canShowRating = false
                        notifyDataSetChanged()
                        UserPrefStorage.setHasFinishedRatingDialog(holder.card.context)
                    }
                    holder.dialogRatingPosition == RATING_NO -> {
                        fbAnalytics.logEvent("REVIEW_CARD_NO_FEEDBACK", null)

                        canShowRating = false
                        notifyDataSetChanged()
                        UserPrefStorage.setHasFinishedRatingDialog(holder.card.context)
                    }
                }
            }
        } else {
            val gameDifficulty = gameDifficultyList[if (canShowRating) position - 1 else position]
            val gameDifficultyColor = gameDifficulty.getColor(holder.card.context)

            holder.card.setOnClickListener { navigator.startGame(gameDifficulty) }
            holder.difficultyText!!.setBackgroundColor(gameDifficultyColor)
            holder.difficultyText!!.text = gameDifficulty.getName(holder.card.context)
            holder.boardInfoText!!.text = gameDifficulty.getDescription(holder.card.context)
        }
    }

    override fun getItemCount(): Int {
        return if (canShowRating) gameDifficultyList.size + 1 else gameDifficultyList.size
    }
}
