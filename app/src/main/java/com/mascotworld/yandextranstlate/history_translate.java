package com.mascotworld.yandextranstlate;

/**
 * Created by mascot on 20.03.2017.
 */

class history_translate {
    String defaultlanguage;
    String translatelanguage;
    String wtr;
   private boolean favorite;


    history_translate(String defaultlanguage, String translatelanguage,String wtr) {
        this.defaultlanguage = defaultlanguage;
        this.translatelanguage = translatelanguage;
        this.wtr = wtr;
        this.favorite = false;
    }
    public void setFavorite() {
        if (favorite) {
            this.favorite = false;
        } else
            this.favorite = true;
    }
    public boolean isFavorite() {
        return favorite;
    }

    public void swapFavorite(boolean def){
        favorite = def;
    }

}