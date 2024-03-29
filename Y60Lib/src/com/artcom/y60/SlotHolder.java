package com.artcom.y60;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.view.GestureDetector.OnGestureListener;

/**
 * A bridge between slot implementations ({@link SlotLauncher},
 * {@link SlotViewer}) and application code. Slots use their holder e.g. to
 * access a Context object and to signal that the view has to be updated
 * (invalidate). Applications use a slot holder object to activate and
 * deactivate all slots without having to know which and how many slots there
 * are. Subclass SlotHolder to provide a custom implementation or to add other
 * features.
 * 
 * @see Slot, DropTargetCollection, SlotLauncher, SlotViewer
 * @author arne
 */
public class SlotHolder {

    // Instance Variables ------------------------------------------------

    private final Context           mContext;
    private final OnGestureListener mGestureListener;

    protected List<Slot>            mSlots;

    // Constructors ------------------------------------------------------

    public SlotHolder(Context pContext) {

        mContext = pContext;
        mGestureListener = null;
        mSlots = new LinkedList<Slot>();
    }

    public SlotHolder(Context pContext, OnGestureListener pGestureListener) {

        mContext = pContext;
        mGestureListener = pGestureListener;
        mSlots = new LinkedList<Slot>();
    }

    // Public Instance Methods -------------------------------------------

    /**
     * Override if you're interested.
     */
    public void invalidate() {

        // nothing to do
    }

    public OnGestureListener getGestureListener() {
        return mGestureListener;
    }

    public Context getContext() {

        return mContext;
    }

    public void activateSlots() {

        for (Slot slot : mSlots) {

            slot.activate();
        }
    }

    public void deactivateSlots() {

        for (Slot slot : mSlots) {

            slot.deactivate();
        }
    }

    public void addSlot(Slot pSlot) {

        mSlots.add(pSlot);
        pSlot.setHolder(this);
    }

    public void addSlot(String pName, SlotLauncher pLauncher, SlotViewer pViewer) {

        addSlot(new Slot(pName, pLauncher, pViewer));
    }

    public void removeSlots() {
        deactivateSlots();
        mSlots.clear();
    }

    public List<Slot> getSlots() {

        return new LinkedList<Slot>(mSlots);
    }

    public int size() {
        return mSlots.size();
    }
}
