package com.artcom.y60;

public abstract class DecoratingSlotLauncher extends SlotLauncher {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = DecoratingSlotLauncher.class.getName();
    
    
    // Instance Variables ------------------------------------------------

    private SlotLauncher mTarget;
    
    
    
    // Constructors ------------------------------------------------------

    public DecoratingSlotLauncher() {
        
        this(null);
    }
    
    
    public DecoratingSlotLauncher(SlotLauncher pTarget) {
    
        mTarget = pTarget;
    }
    
    
    
    // Public Instance Methods -------------------------------------------
    
    @Override
    public final void launch() {
        
        launchThis();
        
        if (mTarget != null) {
            
            mTarget.launch();
        }
    }

    
    @Override
    public void focus() {
        
        focusThis();
        
        if (mTarget != null) {
            
            mTarget.focus();
        }
    }


    @Override
    public void unfocus() {
        
        unfocusThis();
        
        if (mTarget != null) {
            
            mTarget.unfocus();
        }
    }
    
    
    public SlotLauncher getTarget() {
        
        return mTarget;
    }
    
    
    public void setTarget(SlotLauncher pTarget) {
        
        mTarget = pTarget;
    }

    
    
    // Protected Instance Methods ----------------------------------------

    protected abstract void launchThis();

    protected abstract void focusThis();
    
    protected abstract void unfocusThis();
    
    
    
    
    // Package Protected Instance Methods --------------------------------

    @Override
    void setSlot(Slot pSlot) {
        
        super.setSlot(pSlot);
        if (mTarget != null) {
            mTarget.setSlot(pSlot);
        }
    }

}
