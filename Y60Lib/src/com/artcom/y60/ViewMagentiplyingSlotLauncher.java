package com.artcom.y60;

import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.view.View;
import android.widget.ImageView;

public class ViewMagentiplyingSlotLauncher extends DecoratingSlotLauncher {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = ViewMagentiplyingSlotLauncher.class.getName();

    private static final long DROPPING_VIBRATION = 100;

    private static final long ON_TARGET_VIBRATION = 50;

    // Constructors ------------------------------------------------------

    public ViewMagentiplyingSlotLauncher() {
        super();
        // TODO Auto-generated constructor stub
    }

    public ViewMagentiplyingSlotLauncher(SlotLauncher pTarget) {
        super(pTarget);
        // TODO Auto-generated constructor stub
    }

    // Public Instance Methods -------------------------------------------

    @Override
    protected void focusThis() {

        Logger.d(LOG_TAG, "focus!");
        View view = getSlot().getViewer().view();
        ((ImageView) view).getDrawable().setColorFilter(Color.MAGENTA, Mode.MULTIPLY);
        view.invalidate();
    }

    @Override
    protected void launchThis() {
    }

    @Override
    protected void unfocusThis() {

        View view = getSlot().getViewer().view();
        ((ImageView) view).getDrawable().clearColorFilter();
        
    }

}
