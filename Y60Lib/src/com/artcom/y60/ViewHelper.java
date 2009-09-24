package com.artcom.y60;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsoluteLayout;

public class ViewHelper {

    public static View getCurrentView(int pId, Activity pActivity) {
        return pActivity.getWindow().getDecorView().findViewById(pId);
    }

    /**
     * Convenience method for getting x location of the polaroid view.
     * 
     * @param view
     *            a view which must be part of an absolute layout.
     * @return x location (pixel).
     * @throws NullPointerException
     *             if given view is null.
     * @throws ClassCastException
     *             if given view is not part of an absolute layout.
     */
    public static int getAbsolutePosX(View view) throws NullPointerException, ClassCastException {

        if (view == null)
            throw new NullPointerException("View must not be null!");

        return ((AbsoluteLayout.LayoutParams) view.getLayoutParams()).x;
    }

    /**
     * Convenience method for getting y location of the polaroid view.
     * 
     * @param view
     *            a view which must be part of an absolute layout.
     * @return y location (pixel).
     * @throws NullPointerException
     *             if given view is null.
     * @throws ClassCastException
     *             if given view is not part of an absolute layout.
     */
    public static int getAbsolutePosY(View view) throws NullPointerException, ClassCastException {

        if (view == null)
            throw new NullPointerException("View must not be null!");

        return ((AbsoluteLayout.LayoutParams) view.getLayoutParams()).y;
    }

    /**
     * Convenience method for setting the absolute location of a given view.
     * 
     * @param view
     *            a view which must be part of an absolute layout.
     * @param x
     *            horizontal pixel location.
     * @param y
     *            vertical pixel location.
     * @throws NullPointerException
     *             if given view is null.
     * @throws ClassCastException
     *             if given view is not part of an absolute layout.
     */
    public static void setAbsolutePos(View view, int x, int y) {

        if (view == null)
            throw new NullPointerException("View must not be null!");

        AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, x, y);

        view.setLayoutParams(lp);
    }

}
