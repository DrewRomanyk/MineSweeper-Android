package app.drewromanyk.com.minesweeper.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.enums.GameDifficulty;
import app.drewromanyk.com.minesweeper.interfaces.PlayNavigator;
import app.drewromanyk.com.minesweeper.util.UserPrefStorage;

/**
 * Created by Drew on 12/11/15.
 */
public class StatsGameDifficultyAdapter extends RecyclerView.Adapter<StatsGameDifficultyAdapter.PlayViewHolder> {

    private ArrayList<GameDifficulty> gameDifficultyList;

    public class PlayViewHolder extends RecyclerView.ViewHolder {

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

        public PlayViewHolder(View itemView) {
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
    }

    public void setGameDifficultyList(ArrayList<GameDifficulty> gameDifficultyList) {
        this.gameDifficultyList.clear();
        this.gameDifficultyList.addAll(gameDifficultyList);
        notifyDataSetChanged();
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

        holder.bestScore.setText(boldString(context.getString(R.string.stats_title_best_score), "" + String.format("%.2f", (double) bestScore / 1000)));
        holder.avgScore.setText(boldString(context.getString(R.string.stats_title_avg_score), String.format("%.2f", (double) avgScore / 1000)));
        holder.bestTime.setText(boldString(context.getString(R.string.stats_title_best_time), "" + bestTime));
        holder.avgTime.setText(boldString(context.getString(R.string.stats_title_avg_time), String.format("%.2f", avgTime)));
        holder.gamesWon.setText(boldString(context.getString(R.string.stats_title_games_won), "" + wins));
        holder.gamesPlayed.setText(boldString(context.getString(R.string.stats_title_games_played), "" + totalGames));
        holder.winPercent.setText(boldString(context.getString(R.string.stats_title_win_percent), String.format("%.2f", winPerct) + "%"));
        holder.explorePercent.setText(boldString(context.getString(R.string.stats_title_explore_percent), String.format("%.2f", explorPerct) + "%"));
        holder.winStreak.setText(boldString(context.getString(R.string.stats_title_win_streak), "" + winStreak));
        holder.loseStreak.setText(boldString(context.getString(R.string.stats_title_lose_streak), "" + losesStreak));
        holder.currentStreak.setText(boldString(context.getString(R.string.stats_title_current_streak),
                "" + ((currentWinStreak == 0) ? currentLosesStreak : currentWinStreak)));
    }

    @Override
    public int getItemCount() {
        return gameDifficultyList.size();
    }

    private Spanned boldString(String phrase, String otherPhrase) {
        return Html.fromHtml("<b>" + phrase + "</b>" + otherPhrase);
    }
}
