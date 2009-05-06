package com.artcom.y60.ui;


import android.app.Activity;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsoluteLayout;
import android.widget.TableLayout;

import com.artcom.y60.HorizontalDirection;


public class WorkSpace {
	  
    public enum Target { IN, OUT }
    
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "WorkSpace";
    private static final int    ANIMATION_DURATION = 500;

	private AbsoluteLayout   mAbsoluteLayout;
	private TableLayout mTableLayout;
	private View mContent;
	private Activity    m_Activity;
	private String      m_Name;
	
    private Animation m_OutToLeft;  
    private Animation m_OutToRight;   
    private Animation m_InFromLeft;
    private Animation m_InFromRight;

	public WorkSpace( String p_Name, Activity p_Context, View p_View ) {

	    m_Name     = p_Name;
		m_Activity = p_Context;
		mContent = p_View;
        mContent.setLongClickable(true);
        mTableLayout     =  new TableLayout( m_Activity );
        mTableLayout.setOrientation( TableLayout.HORIZONTAL );
        mTableLayout.setLongClickable(true);
        mTableLayout.addView(mContent);
		mAbsoluteLayout   = new AbsoluteLayout( m_Activity);
		mAbsoluteLayout.addView(mTableLayout);
        mAbsoluteLayout.setLongClickable(true);
		
        
        m_OutToLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF,  0, Animation.RELATIVE_TO_SELF, -1,
                Animation.RELATIVE_TO_SELF,  0, Animation.RELATIVE_TO_SELF,  0
        );
        m_OutToLeft.setDuration(ANIMATION_DURATION);
        
        m_InFromLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, -1, Animation.RELATIVE_TO_SELF,  0,
                Animation.RELATIVE_TO_SELF,  0, Animation.RELATIVE_TO_SELF,  0
        );
        m_InFromLeft.setDuration(ANIMATION_DURATION);
        
        m_OutToRight = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF,  0, Animation.RELATIVE_TO_SELF,  1,
                Animation.RELATIVE_TO_SELF,  0, Animation.RELATIVE_TO_SELF,  0
        );
        m_OutToRight.setDuration(ANIMATION_DURATION);
        
        m_InFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF,  1, Animation.RELATIVE_TO_SELF,  0,
                Animation.RELATIVE_TO_SELF,  0, Animation.RELATIVE_TO_SELF,  0
        );
        m_InFromRight.setDuration(ANIMATION_DURATION);
	}
	
	public View getContent() {
	
	    return mContent;
	}
	
	public AbsoluteLayout getAbsoluteLayout() { return mAbsoluteLayout; }
	public TableLayout getTableLayout() { return mTableLayout; }
	public Activity getParent() { return m_Activity;}
    public String getName() { return m_Name; }
    public void invalidate() { refresh(); }
    public String toString() { return getName(); }
      
	public void animate( HorizontalDirection p_Direction, Target p_Target ) {

        mAbsoluteLayout.clearAnimation();
    	
        if (p_Target == Target.IN) {
            
            refresh();
            
            if (p_Direction == HorizontalDirection.LEFT) {
                mAbsoluteLayout.startAnimation(m_InFromRight);
            } else {
                mAbsoluteLayout.startAnimation(m_InFromLeft);
            }
        } else {
            if (p_Direction == HorizontalDirection.LEFT) {
                mAbsoluteLayout.startAnimation(m_OutToLeft);
            } else {
                mAbsoluteLayout.startAnimation(m_OutToRight);
            }
        }
        
        showBackground();
	}
	
	public void showBackground() {
        Window win = m_Activity.getWindow();
        win.setBackgroundDrawableResource( R.drawable.bg );
	}
	
	public void clear() {
	    
        mTableLayout.removeAllViews();
        mAbsoluteLayout.removeAllViews();
	}
	
	protected void setContent(View pContent) {
	    mContent = pContent;
	}
	
	protected synchronized void refresh() {
	    
	    clear();
	    mTableLayout.addView(mContent);
	    mAbsoluteLayout.addView( mTableLayout );
	}
	
	protected Activity getActivity() {
	    
	    return m_Activity;
	}
}