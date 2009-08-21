package com.artcom.y60;

import java.util.ArrayList;

public abstract class Ring<T> {

    private ArrayList<T> mRingContent = new ArrayList<T>();
    private int          mCurrent     = 0;                  ;

    public void add(T pNewMember) {
        mRingContent.add(pNewMember);
    }

    public void setActive(int pIndex) {
        pIndex = calculateRealIndex(pIndex);
        onActiveChange(mRingContent.get(mCurrent), mRingContent.get(pIndex));

        mCurrent = pIndex;
    }

    public void setNext() {
        setActive(mCurrent + 1);
    }

    public void setPrevious() {
        setActive(mCurrent - 1);
    }

    public T getCurrent() {
        return mRingContent.get(mCurrent);
    }

    protected abstract void onActiveChange(T pOldActiveMember, T pNextActiveMember);

    private int calculateRealIndex(int pIndex) {
        pIndex = pIndex % mRingContent.size();

        if (pIndex < 0)
            pIndex = pIndex + mRingContent.size();

        return pIndex;
    }
}
