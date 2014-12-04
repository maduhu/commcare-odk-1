/**
 * 
 */
package org.commcare.android.models;



/**
 * @author ctsims
 *
 */
public class Entity<T> {
    
    T t;
    Object[] data;
    String[] sortData;
    boolean[] relevancyData;
	String[] backgroundData;
    
    protected Entity(T t) {
        this.t = t;
    }
    
    public Entity(Object[] data, String[] sortData, String[] backgroundData, boolean[] relevancyData,  T t) {
        this.t = t;
        this.sortData = sortData;
        this.backgroundData = backgroundData;
        this.data = data;
        this.relevancyData = relevancyData;
    }
    
    public Object getField(int i) {
        return data[i];
    }
    
    /*
     * Same as getField, but guaranteed to return a string.
     * If field is not already a string, will return blank string.
     */
    public String getFieldString(int i) {
        Object field = getField(i);
        if (field instanceof String) {
            return (String) field;
        }
        return "";
    }
    
    /**
     * @param i index of field
     * @return True iff the given field is relevant and has a non-blank value.
     */
    public boolean isValidField(int i) {
        return !getField(i).equals("") && relevancyData[i];
    }
    
    public String getSortField(int i) {
        return sortData[i];
    }
    
    public T getElement() {
        return t;
    }

    public int getNumFields() {
        return data.length;
    }
	
	public Object[] getData(){
		return data;
	}
	
	public String [] getBackgroundData(){
		return backgroundData;
	}
}
