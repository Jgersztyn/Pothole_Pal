package com.jgersztyn.pothole_pal;


/*
Class to represent an object to store into the database
In this case we are storing pin points to be displayed on a map
 */
public class PinPointObj {

    private long id;
    private String text;
    private String position;

    /*
    default constructor
     */
    public PinPointObj() {

    }

    /*
    overloaded constructor, which takes in the id
     */
    public PinPointObj(long id, String text, String position) {
        this.setId(id);
        this.setText(text);
        this.setPosition(position);
    }

    /*
    overloaded constructor, which does not take in the id
 */
    public PinPointObj(String text, String position) {
        this.setText(text);
        this.setPosition(position);
    }

    /*
    Below here are the getters and setters for all of the fields within this class.
    We never want to access these fields directly.
     */

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
