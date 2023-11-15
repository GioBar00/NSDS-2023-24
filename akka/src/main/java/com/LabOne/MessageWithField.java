package com.LabOne;

public class MessageWithField {

    private boolean isIncrement;

    /*! Constructor for the message */
    public MessageWithField(boolean isIncrement){
        this.isIncrement = isIncrement;
    }

    /*! Getter method for the message */
    public boolean isIncremental(){
        return this.isIncrement;
    }
}
