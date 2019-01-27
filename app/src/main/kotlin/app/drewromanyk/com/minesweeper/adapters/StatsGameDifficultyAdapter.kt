package app.drewromanyk.com.minesweeper.adapters

import android.text.Spanned
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.squareup.phrase.Phrase

import java.util.ArrayList

import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.enums.GameDifficulty
import app.drewromanyk.com.minesweeper.util.Helper
import app.drewromanyk.com.minesweeper.util.PhraseKeys
import app.drewromanyk.com.minesweeper.util.UserPrefStorage

/**
 * Created by Drew on 12/11/15.
 * StatsGameDifficultyAdapter
 * Adapter to display all the local stats for each difficulty
 */
class StatsGameDifficultyAdapter : RecyclerView.Adapter<StatsGameDifficultyAdapter.PlayViewHolder>() {

    private val gameDifficultyList: MutableList<GameDifficulty>

    inner class PlayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val difficultyText: TextView = itemView.findViewById(R.id.card_difficulty_text) as TextView
        val bestScore: TextView = itemView.findViewById(R.id.best_score) as TextView
        val avgScore: TextView = itemView.findViewById(R.id.avg_score) as TextView
        val bestTime: TextView = itemView.findViewById(R.id.best_time) as TextView
        val avgTime: TextView = itemView.findViewById(R.id.avg_time) as TextView
        val gamesWon: TextView = itemView.findViewById(R.id.games_won) as TextView
        val gamesPlayed: TextView = itemView.findViewById(R.id.games_played) as TextView
        val winPercent: TextView = itemView.findViewById(R.id.win_percent) as TextView
        val explorePercent: TextView = itemView.findViewById(R.id.explore_percent) as TextView
        val winStreak: TextView = itemView.findViewById(R.id.win_streak) as TextView
        val loseStreak: TextView = itemView.findViewById(R.id.lose_streak) as TextView
        val currentStreak: TextView = itemView.findViewById(R.id.current_streak) as TextView
    }

    init {
        gameDifficultyList = ArrayList()
        gameDifficultyList.add(GameDifficulty.EASY)
        gameDifficultyList.add(GameDifficulty.MEDIUM)
        gameDifficultyList.add(GameDifficulty.EXPERT)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayViewHolder {
        return PlayViewHolder(View.inflate(parent.context, R.layout.card_stats_diifculty, null))
    }

    override fun onBindViewHolder(holder: PlayViewHolder, position: Int) {
        val gameDifficulty = gameDifficultyList[position]
        val context = holder.difficultyText.context
        val gameDifficultyColor = gameDifficulty.getColor(context)

        holder.difficultyText.setBackgroundColor(gameDifficultyColor)
        holder.difficultyText.text = gameDifficulty.getName(context)

        // Get data
        val wins = UserPrefStorage.getWinsForDifficulty(context, gameDifficulty)
        val loses = UserPrefStorage.getLosesForDifficulty(context, gameDifficulty)
        val bestTime = UserPrefStorage.getBestTimeForDifficulty(context, gameDifficulty)
        val avgTime = UserPrefStorage.getAvgTimeForDifficulty(context, gameDifficulty)
        val explorePercent = UserPrefStorage.getExplorPercentForDifficulty(context, gameDifficulty)
        val winStreak = UserPrefStorage.getWinStreakForDifficulty(context, gameDifficulty)
        val losesStreak = UserPrefStorage.getLoseStreakForDifficulty(context, gameDifficulty)
        val currentWinStreak = UserPrefStorage.getCurWinStreakForDifficulty(context, gameDifficulty)
        val currentLosesStreak = UserPrefStorage.getCurLoseStreakForDifficulty(context, gameDifficulty)
        val bestScore = UserPrefStorage.getBestScoreForDifficulty(context, gameDifficulty)
        val avgScore = UserPrefStorage.getAvgScoreForDifficulty(context, gameDifficulty)

        val totalGames = wins + loses

        val winPercentage = if (totalGames != 0) wins.toDouble() / totalGames * 100 else 0.0

        val locale = Helper.getLocale(context)

        holder.bestScore.text = boldFirstString(Phrase.from(context, R.string.stats_title_best_score)
                .put(PhraseKeys.AMOUNT, String.format(locale, "%.2f", bestScore.toDouble() / 1000))
                .format())
        holder.avgScore.text = boldFirstString(Phrase.from(context, R.string.stats_title_avg_score)
                .put(PhraseKeys.AMOUNT, String.format(locale, "%.2f", avgScore.toDouble() / 1000))
                .format())
        holder.bestTime.text = boldFirstString(Phrase.from(context, R.string.stats_title_best_time)
                .put(PhraseKeys.AMOUNT, bestTime)
                .format())
        holder.avgTime.text = boldFirstString(Phrase.from(context, R.string.stats_title_avg_time)
                .put(PhraseKeys.AMOUNT, String.format(locale, "%.2f", avgTime))
                .format())
        holder.gamesWon.text = boldFirstString(Phrase.from(context, R.string.stats_title_games_won)
                .put(PhraseKeys.AMOUNT, wins)
                .format())
        holder.gamesPlayed.text = boldFirstString(Phrase.from(context, R.string.stats_title_games_played)
                .put(PhraseKeys.AMOUNT, totalGames)
                .format())
        holder.winPercent.text = boldFirstString(Phrase.from(context, R.string.stats_title_win_percent)
                .put(PhraseKeys.AMOUNT, String.format(locale, "%.2f", winPercentage) + "%")
                .format())
        holder.explorePercent.text = boldFirstString(Phrase.from(context, R.string.stats_title_explore_percent)
                .put(PhraseKeys.AMOUNT, String.format(locale, "%.2f", explorePercent) + "%")
                .format())
        holder.winStreak.text = boldFirstString(Phrase.from(context, R.string.stats_title_win_streak)
                .put(PhraseKeys.AMOUNT, winStreak)
                .format())
        holder.loseStreak.text = boldFirstString(Phrase.from(context, R.string.stats_title_lose_streak)
                .put(PhraseKeys.AMOUNT, losesStreak)
                .format())
        holder.currentStreak.text = boldFirstString(Phrase.from(context, R.string.stats_title_current_streak)
                .put(PhraseKeys.AMOUNT,
                        "" + if (currentWinStreak == 0) currentLosesStreak else currentWinStreak)
                .format())
    }

    override fun getItemCount(): Int {
        return gameDifficultyList.size
    }

    private fun boldFirstString(phraseChars: CharSequence): Spanned {
        val phrase = phraseChars.toString()
        val keyWordIndex = phrase.indexOf(":") + 1
        val keyWord = phrase.substring(0, keyWordIndex)
        val value = phrase.substring(keyWordIndex)
        return Helper.fromHtml("<b>$keyWord</b>$value")
    }
}
