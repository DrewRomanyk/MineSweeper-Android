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

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.enums.GameDifficulty;
import app.drewromanyk.com.minesweeper.interfaces.PlayNavigator;
import app.drewromanyk.com.minesweeper.util.UserPrefStorage;

/**
 * Created by Drew on 12/11/15.
 * PlayGameDifficultyAdapter
 * RecyclerView to show list of difficulties to play along with optional cards to show such as
 * resume and rating
 */
public class PlayGameDifficultyAdapter extends RecyclerView.Adapter<PlayGameDifficultyAdapter.PlayViewHolder> {

    private static int RATING_TYPE = 0;
    private static int DIFFICULTY_TYPE = 1;

    private static int RATING_FIRST = 0;
    private static int RATING_NO = 1;
    private static int RATING_YES = 2;


    private List<GameDifficulty> gameDifficultyList;
    private boolean canShowRating;
    private PlayNavigator navigator;

    class PlayViewHolder extends RecyclerView.ViewHolder {

        View card;

        TextView ratingDesc;
        Button ratingNo;
        Button ratingYes;
        int dialogRatingPosition = 0;

        TextView difficultyText;
        TextView boardInfoText;
        FloatingActionButton fab;

        PlayViewHolder(View itemView, int itemViewType) {
            super(itemView);
            card = itemView.findViewById(R.id.card);

            if (itemViewType == RATING_TYPE) {
                ratingDesc = (TextView) itemView.findViewById(R.id.rating_desc);
                ratingNo = (Button) itemView.findViewById(R.id.rating_no);
                ratingYes = (Button) itemView.findViewById(R.id.rating_yes);
            } else if (itemViewType == DIFFICULTY_TYPE) {
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
        int FIRST_ITEM = 0;
        if (canShowRating && position == FIRST_ITEM) {
            return RATING_TYPE;
        } else {
            return DIFFICULTY_TYPE;
        }
    }

    @Override
    public PlayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == RATING_TYPE) {
            return new PlayViewHolder(View.inflate(parent.getContext(), R.layout.card_play_feedback, null), viewType);
        } else {
            return new PlayViewHolder(View.inflate(parent.getContext(), R.layout.card_play_difficulty, null), viewType);
        }
    }

    @Override
    public void onBindViewHolder(final PlayViewHolder holder, int position) {
        if (getItemViewType(position) == RATING_TYPE) {
            final FirebaseAnalytics fbAnalytics = FirebaseAnalytics.getInstance(holder.card.getContext());
            holder.ratingYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.ratingYes.setText(R.string.inapp_rating_secondary_yes);
                    holder.ratingNo.setText(R.string.inapp_rating_secondary_no);

                    if (holder.dialogRatingPosition == RATING_FIRST) {
                        fbAnalytics.logEvent("REVIEW_CARD_YES_ENJOYED", null);

                        holder.dialogRatingPosition = RATING_YES;
                        holder.ratingDesc.setText(R.string.inapp_rating_rating);
                    } else if (holder.dialogRatingPosition == RATING_YES) {
                        fbAnalytics.logEvent("REVIEW_CARD_RATE", null);

                        canShowRating = false;
                        notifyDataSetChanged();
                        UserPrefStorage.setHasFinishedRatingDialog(holder.card.getContext());
                        navigator.startPlayStore();
                    } else if (holder.dialogRatingPosition == RATING_NO) {
                        fbAnalytics.logEvent("REVIEW_CARD_FEEDBACK", null);

                        canShowRating = false;
                        notifyDataSetChanged();
                        UserPrefStorage.setHasFinishedRatingDialog(holder.card.getContext());
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
                        fbAnalytics.logEvent("REVIEW_CARD_NO_ENJOYED", null);

                        holder.dialogRatingPosition = RATING_NO;
                        holder.ratingDesc.setText(R.string.inapp_rating_feedback);
                    } else if (holder.dialogRatingPosition == RATING_YES) {
                        fbAnalytics.logEvent("REVIEW_CARD_NO_RATE", null);

                        canShowRating = false;
                        notifyDataSetChanged();
                        UserPrefStorage.setHasFinishedRatingDialog(holder.card.getContext());
                    } else if (holder.dialogRatingPosition == RATING_NO) {
                        fbAnalytics.logEvent("REVIEW_CARD_NO_FEEDBACK", null);

                        canShowRating = false;
                        notifyDataSetChanged();
                        UserPrefStorage.setHasFinishedRatingDialog(holder.card.getContext());
                    }
                }
            });
        } else {
            final GameDifficulty gameDifficulty = gameDifficultyList.get(
                    (canShowRating) ? position - 1 : position);
            int gameDifficultyColor = gameDifficulty.getColor(holder.card.getContext());

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
