package com.mascotworld.yandextranstlate;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.List;

import static android.R.attr.button;
import static android.R.attr.drawable;

/**
 * Created by mascot on 20.03.2017.
 */

public class Adapter_History extends RecyclerView.Adapter<Adapter_History.HistViewHolder> {


    public static class HistViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView defword;
        TextView tranword;
        TextView wtr;
        Button button;


        HistViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            defword = (TextView) itemView.findViewById(R.id.defword);
            tranword = (TextView) itemView.findViewById(R.id.tranword);
            wtr = (TextView) itemView.findViewById(R.id.wtr);
            button = (Button) itemView.findViewById(R.id.button3);
        }
    }


    List<history_translate> words;

    Adapter_History(List<history_translate> words) {
        this.words = words;
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public HistViewHolder onCreateViewHolder(ViewGroup parentViewGroup, int i) {
        View v = LayoutInflater.from(parentViewGroup.getContext()).inflate(R.layout.history_element, parentViewGroup, false);
        return new HistViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final HistViewHolder histViewHolder, final int i) {
        histViewHolder.defword.setText(words.get(i).defaultlanguage);
        histViewHolder.tranword.setText(words.get(i).translatelanguage);
        histViewHolder.wtr.setText(words.get(i).wtr);
        if (words.get(i).isFavorite()) {
            histViewHolder.button.setBackgroundResource(R.mipmap.ic_favorite_black_36dp);
        } else histViewHolder.button.setBackgroundResource(R.mipmap.ic_favorite_border_black_36dp);


        histViewHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                words.get(i).setFavorite();
                if (words.get(i).isFavorite()) {
                    histViewHolder.button.setBackgroundResource(R.mipmap.ic_favorite_black_36dp);
                    //butt.setBackgroundResource(R.mipmap.ic_favorite_black_36dp);

                } else {
                    histViewHolder.button.setBackgroundResource(R.mipmap.ic_favorite_border_black_36dp);
                   //butt.setBackgroundResource(R.mipmap.ic_favorite_border_black_36dp);
                }
            }
        });
        histViewHolder.cv.setTag(i);
    }



    @Override
    public int getItemCount() {
        return words.size();
    }


}
