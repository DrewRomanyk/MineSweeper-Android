package app.drewromanyk.com.minesweeper.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.phrase.Phrase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.enums.GameDifficulty;
import app.drewromanyk.com.minesweeper.util.Helper;
import app.drewromanyk.com.minesweeper.util.PhraseKeys;
import app.drewromanyk.com.minesweeper.util.UserPrefStorage;

/**
 * Created by Drew on 12/11/15.
 * StatsGameDifficultyAdapter
 * Adapter to display all the local stats for each difficulty
 */
public class StatsGameDifficultyAdapter extends RecyclerView.Adapter<StatsGameDifficultyAdapter.PlayViewHolder> {

    private List<GameDifficulty> gameDifficultyList;

    class PlayViewHolder extends RecyclerView.ViewHolder {

        TextView difficultyText;

        TextView bestScore;
        TextView avgScore;
        TextView bestTime;
        TextView avgTime;
        TextView gamesWon;
        TextView gamesPlayed;
        TextView winPercent;
        TextView explorePercent;
        TextView winStreak;
        TextView loseStreak;
        TextView currentStreak;

        PlayViewHolder(View itemView) {
            super(itemView);

            difficultyText = (TextView) itemView.findViewById(R.id.card_difficulty_text);

            bestScore = (TextView) itemView.findViewById(R.id.best_score);
            avgScore = (TextView) itemView.findViewById(R.id.avg_score);
            bestTime = (TextView) itemView.findViewById(R.id.best_time);
            avgTime = (TextView) itemView.findViewById(R.id.avg_time);
            gamesWon = (TextView) itemView.findViewById(R.id.games_won);
            gamesPlayed = (TextView) itemView.findViewById(R.id.games_played);
            winPercent = (TextView) itemView.findViewById(R.id.win_percent);
            explorePercent = (TextView) itemView.findViewById(R.id.explore_percent);
            winStreak = (TextView) itemView.findViewById(R.id.win_streak);
            loseStreak = (TextView) itemView.findViewById(R.id.lose_streak);
            currentStreak = (TextView) itemView.findViewById(R.id.current_streak);
        }
    }

    public StatsGameDifficultyAdapter() {
        gameDifficultyList = new ArrayList<>();
        gameDifficultyList.add(GameDifficulty.EASY);
        gameDifficultyList.add(GameDifficulty.MEDIUM);
        gameDifficultyList.add(GameDifficulty.EXPERT);
    }

    @Override
    public PlayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PlayViewHolder(View.inflate(parent.getContext(), R.layout.card_stats_diifculty, null));
    }

    @Override
    public void onBindViewHolder(PlayViewHolder holder, int position) {
        final GameDifficulty gameDifficulty = gameDifficultyList.get(position);
        Context context = holder.difficultyText.getContext();
        int gameDifficultyColor = gameDifficulty.getColor(context);

        holder.difficultyText.setBackgroundColor(gameDifficultyColor);
        holder.difficultyText.setText(gameDifficulty.getName(context));

        // Get data
        int wins = UserPrefStorage.getWinsForDifficulty(context, gameDifficulty);
        int loses = UserPrefStorage.getLosesForDifficulty(context, gameDifficulty);
        int bestTime = UserPrefStorage.getBestTimeForDifficulty(context, gameDifficulty);
        float avgTime = UserPrefStorage.getAvgTimeForDifficulty(context, gameDifficulty);
        float explorPerct = UserPrefStorage.getExplorPercentForDifficulty(context, gameDifficulty);
        int winStreak = UserPrefStorage.getWinStreakForDifficulty(context, gameDifficulty);
        int losesStreak = UserPrefStorage.getLoseStreakForDifficulty(context, gameDifficulty);
        int currentWinStreak = UserPrefStorage.getCurWinStreakForDifficulty(context, gameDifficulty);
        int currentLosesStreak = UserPrefStorage.getCurLoseStreakForDifficulty(context, gameDifficulty);
        int bestScore = UserPrefStorage.getBestScoreForDifficulty(context, gameDifficulty);
        float avgScore = UserPrefStorage.getAvgScoreForDifficulty(context, gameDifficulty);

        int totalGames = wins + loses;

        double winPerct = ((totalGames != 0) ? ((((double) wins / totalGames)) * 100) : 0);

        Locale locale = Helper.getLocale(context);

        holder.bestScore.setText(boldFirstString(Phrase.from(context, R.string.stats_title_best_score)
                .put(PhraseKeys.AMOUNT, String.format(locale, "%.2f", (double) bestScore / 1000))
                .format()));
        holder.avgScore.setText(boldFirstString(Phrase.from(context, R.string.stats_title_avg_score)
                .put(PhraseKeys.AMOUNT, String.format(locale, "%.2f", (double) avgScore / 1000))
                .format()));
        holder.bestTime.setText(boldFirstString(Phrase.from(context, R.string.stats_title_best_time)
                .put(PhraseKeys.AMOUNT, bestTime)
                .format()));
        holder.avgTime.setText(boldFirstString(Phrase.from(context, R.string.stats_title_avg_time)
                .put(PhraseKeys.AMOUNT, String.format(locale, "%.2f", avgTime))
                .format()));
        holder.gamesWon.setText(boldFirstString(Phrase.from(context, R.string.stats_title_games_won)
                .put(PhraseKeys.AMOUNT, wins)
                .format()));
        holder.gamesPlayed.setText(boldFirstString(Phrase.from(context, R.string.stats_title_games_played)
                .put(PhraseKeys.AMOUNT, totalGames)
                .format()));
        holder.winPercent.setText(boldFirstString(Phrase.from(context, R.string.stats_title_win_percent)
                .put(PhraseKeys.AMOUNT, String.format(locale, "%.2f", winPerct) + "%")
                .format()));
        holder.explorePercent.setText(boldFirstString(Phrase.from(context, R.string.stats_title_explore_percent)
                .put(PhraseKeys.AMOUNT, String.format(locale, "%.2f", explorPerct) + "%")
                .format()));
        holder.winStreak.setText(boldFirstString(Phrase.from(context, R.string.stats_title_win_streak)
                .put(PhraseKeys.AMOUNT, winStreak)
                .format()));
        holder.loseStreak.setText(boldFirstString(Phrase.from(context, R.string.stats_title_lose_streak)
                .put(PhraseKeys.AMOUNT, losesStreak)
                .format()));
        holder.currentStreak.setText(boldFirstString(Phrase.from(context, R.string.stats_title_current_streak)
                .put(PhraseKeys.AMOUNT,
                        "" + ((currentWinStreak == 0) ? currentLosesStreak : currentWinStreak))
                .format()));
    }

    @Override
    public int getItemCount() {
        return gameDifficultyList.size();
    }

    private Spanned boldFirstString(CharSequence phraseChars) {
        String phrase = phraseChars.toString();
        int keyWordIndex = phrase.indexOf(":") + 1;
        String keyWord = phrase.substring(0, keyWordIndex);
        String value = phrase.substring(keyWordIndex);
        return Helper.fromHtml("<b>" + keyWord + "</b>" + value);
    }
}
