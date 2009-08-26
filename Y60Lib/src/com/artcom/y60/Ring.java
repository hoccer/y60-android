package com.artcom.y60;

import java.util.ArrayList;

public class Ring<T> {

    private final ArrayList<T> mRingContent = new ArrayList<T>();
    private int                mCurrent     = 0;

    public void add(T pNewMember) {
        mRingContent.add(pNewMember);

        if (size() == 1) {
            onChange(getCurrent(), getCurrent());
        }
    }

    public void jumpTo(int pNewIndex) {
        int oldIndex = mCurrent;
        pNewIndex = calculateRealIndex(pNewIndex);

        if (oldIndex == pNewIndex) {
            return;
        }

        mCurrent = pNewIndex;

        onChange(mRingContent.get(oldIndex), mRingContent.get(pNewIndex));
    }

    public void stepForward() {
        jumpTo(mCurrent + 1);
    }

    public void stepBackward() {
        jumpTo(mCurrent - 1);
    }

    public T getCurrent() {
        return mRingContent.get(getCurrentIndex());
    }

    public int getCurrentIndex() {
        return mCurrent;
    }

    public int size() {
        return mRingContent.size();
    }

    protected void onChange(T pOldActiveMember, T pNextActiveMember) {
    }

    private int calculateRealIndex(int pIndex) {
        pIndex = pIndex % mRingContent.size();

        if (pIndex < 0)
            pIndex = pIndex + mRingContent.size();

        return pIndex;
    }

}
