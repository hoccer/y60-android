package com.artcom.y60;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

public class SlotFactory {

    private static final String SLOT_ICON_BASE_PATH = "/sdcard/slot_icons/";
    
    private static final String LOG_TAG = SlotFactory.class.getName();
    
    public static Slot createMoviePlayerSlot(String pName, Uri pResourceUri, Uri pRciUri, Context pContext) {
        
        RciLauncher launcher = new RciLauncher("movie_player", pResourceUri, pRciUri);
    
        Drawable drawable = BitmapDrawable.createFromPath(SLOT_ICON_BASE_PATH+"tv_icon.png");
        ImageView view = new ImageView(pContext);
        view.setImageDrawable(drawable);
        Logger.d(LOG_TAG, "image height ", view.getHeight(), ", image width ", view.getWidth());
        
        StaticSlotViewer viewer = new StaticSlotViewer(view);
        
        return new Slot(pName, launcher, viewer);
    }
}
