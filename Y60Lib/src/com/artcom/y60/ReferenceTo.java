package com.artcom.y60;

public class ReferenceTo<T> {
    
    // Instance Variables ------------------------------------------------

    private T mValue;
    
    
    
    // Constructors ------------------------------------------------------

    public ReferenceTo(T pValue) {
        
        mValue = pValue;
    }
    
    
    
    // Public Instance Methods -------------------------------------------

    public T getValue() {
        
        return mValue;
    }
    
    
    public void setValue(T pValue) {
        
        mValue = pValue;
    }

}
