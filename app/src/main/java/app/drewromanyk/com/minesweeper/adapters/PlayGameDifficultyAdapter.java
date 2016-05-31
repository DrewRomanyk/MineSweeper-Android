package app.drewromanyk.com.minesweeper.adapters;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.enums.GameDifficulty;
import app.drewromanyk.com.minesweeper.interfaces.PlayNavigator;
import app.drewromanyk.com.minesweeper.util.UserPrefStorage;

/**
 * Created by Drew on 12/11/15.
 */
public class PlayGameDifficultyAdapter extends RecyclerView.Adapter<PlayGameDifficultyAdapter.PlayViewHolder> {

    private static int FIRST_ITEM = 0;

    private static int RATING_TYPE = 0;
    private static int DIFFICULTY_TYPE = 1;

    private static int RATING_FIRST = 0;
    private static int RATING_NO = 1;
    private static int RATING_YES = 2;


    private ArrayList<GameDifficulty> gameDifficultyList;
    private boolean canShowRating;
    private PlayNavigator navigator;

    public class PlayViewHolder extends RecyclerView.ViewHolder {

        View card;

        TextView ratingDesc;
        Button ratingNo;
        Button ratingYes;
        int dialogRatingPosition = 0;

        TextView difficultyText;
        TextView boardInfoText;
        FloatingActionButton fab;

        public PlayViewHolder(View itemView, int itemViewType) {
            super(itemView);
            card = itemView.findViewById(R.id.card);

            if (itemViewType == RATING_TYPE) {
                ratingDesc = (TextView) itemView.findViewById(R.id.rating_desc);
                ratingNo = (Button) itemView.findViewById(R.id.rating_no);
                ratingYes = (Button) itemView.findViewById(R.id.rating_yes);
            } else {
                difficultyText = (TextView) itemView.findViewById(R.id.card_difficulty_text);
                boardInfoText = (TextView) itemView.findViewById(R.id.card_board_info);
                fab = (FloatingActionButton) itemView.findViewById(R.id.card_play_fab);
            }
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

    public void setCanShowRating(boolean canShowRating) {
        this.canShowRating = canShowRating;
    }

    @Override
    public int getItemViewType(int position) {
        if(canShowRating && position == FIRST_ITEM) {
            return RATING_TYPE;
        } else {
            return DIFFICULTY_TYPE;
        }
    }

    @Override
    public PlayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == RATING_TYPE) {
            return new PlayViewHolder(View.inflate(parent.getContext(), R.layout.card_play_feedback, null), viewType);
        } else {
            return new PlayViewHolder(View.inflate(parent.getContext(), R.layout.card_play_difficulty, null), viewType);
        }
    }

    @Override
    public void onBindViewHolder(final PlayViewHolder holder, int position) {
        if(getItemViewType(position) == RATING_TYPE) {
            holder.ratingYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.ratingYes.setText(R.string.inapp_rating_secondary_yes);
                    holder.ratingNo.setText(R.string.inapp_rating_secondary_no);

                    if (holder.dialogRatingPosition == RATING_FIRST) {
                        holder.dialogRatingPosition = RATING_YES;
                        holder.ratingDesc.setText(R.string.inapp_rating_rating);
                    } else if (holder.dialogRatingPosition == RATING_YES) {
                        canShowRating = false;
                        notifyDataSetChanged();
                        UserPrefStorage.setHasFinishedRatingDialog(v.getContext());
                        navigator.startPlayStore();
                    } else if (holder.dialogRatingPosition == RATING_NO) {
                        canShowRating = false;
                        notifyDataSetChanged();
                        UserPrefStorage.setHasFinishedRatingDialog(v.getContext());
                        navigator.sendFeedback();
                    }
                }
            });
            holder.ratingNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.ratingYes.setText(R.string.inapp_rating_secondary_yes);
                    holder.ratingNo.setText(R.string.inapp_rating_secondary_no);

                    if (holder.dialogRatingPosition == RATING_FIRST) {
                        holder.dialogRatingPosition = RATING_NO;
                        holder.ratingDesc.setText(R.string.inapp_rating_feedback);
                    } else if (holder.dialogRatingPosition == RATING_YES) {
                        canShowRating = false;
                        notifyDataSetChanged();
                        UserPrefStorage.setHasFinishedRatingDialog(v.getContext());
                    } else if (holder.dialogRatingPosition == RATING_NO) {
                        canShowRating = false;
                        notifyDataSetChanged();
                        UserPrefStorage.setHasFinishedRatingDialog(v.getContext());
                    }
                }
            });
        } else {
            final GameDifficulty gameDifficulty = gameDifficultyList.get(
                    (canShowRating) ? position - 1 : position);
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
    }

    @Override
    public int getItemCount() {
        return (canShowRating) ? gameDifficultyList.size() + 1 : gameDifficultyList.size();
    }
}
