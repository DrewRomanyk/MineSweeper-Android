package app.drewromanyk.com.minesweeper.adapters;

import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.enums.GameDifficulty;
import app.drewromanyk.com.minesweeper.interfaces.PlayNavigator;

/**
 * Created by Drew on 12/11/15.
 */
public class PlayGameDifficultyAdapter extends RecyclerView.Adapter<PlayGameDifficultyAdapter.PlayViewHolder> {

    private ArrayList<GameDifficulty> gameDifficultyList;
    private PlayNavigator navigator;

    public class PlayViewHolder extends RecyclerView.ViewHolder {

        View card;
        TextView difficultyText;
        TextView boardInfoText;
        FloatingActionButton fab;

        public PlayViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            difficultyText = (TextView) itemView.findViewById(R.id.card_difficulty_text);
            boardInfoText = (TextView) itemView.findViewById(R.id.card_board_info);
            fab = (FloatingActionButton) itemView.findViewById(R.id.card_play_fab);
        }
    }

    public PlayGameDifficultyAdapter(PlayNavigator navigator) {
        this.navigator = navigator;
        gameDifficultyList = new ArrayList<>();
    }

    public void setGameDifficultyList(ArrayList<GameDifficulty> gameDifficultyList) {
        this.gameDifficultyList.clear();
        this.gameDifficultyList.addAll(gameDifficultyList);
        notifyDataSetChanged();
    }

    @Override
    public PlayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PlayViewHolder(View.inflate(parent.getContext(), R.layout.card_play_diifculty, null));
    }

    @Override
    public void onBindViewHolder(PlayViewHolder holder, int position) {
        final GameDifficulty gameDifficulty = gameDifficultyList.get(position);
        int gameDifficultyColor = gameDifficulty.getColor(holder.fab.getContext());

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigator.startGame(gameDifficulty);
            }
        });
        holder.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigator.startGame(gameDifficulty);
            }
        });
        holder.fab.setBackgroundTintList(ColorStateList.valueOf(gameDifficultyColor));
        holder.difficultyText.setBackgroundColor(gameDifficultyColor);
        holder.difficultyText.setText(gameDifficulty.getName(holder.card.getContext()));
        holder.boardInfoText.setText(gameDifficulty.getDescription(holder.card.getContext()));
    }

    @Override
    public int getItemCount() {
        return gameDifficultyList.size();
    }
}
