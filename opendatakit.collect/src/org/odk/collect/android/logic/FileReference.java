/**
 * 
 */

package org.odk.collect.android.logic;

import org.javarosa.core.reference.Reference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author ctsims
 */
public class FileReference implements Reference {
    String localPart;
    String referencePart;


    public FileReference(String localPart, String referencePart) {
        this.localPart = localPart;
        this.referencePart = referencePart;
    }


    private String getInternalURI() {
        return "/" + localPart + referencePart;
    }


    /*
     * (non-Javadoc)
     * @see org.javarosa.core.reference.Reference#doesBinaryExist()
     */
    @Override
    public boolean doesBinaryExist() {
        return new File(getInternalURI()).exists();
    }


    /*
     * (non-Javadoc)
     * @see org.javarosa.core.reference.Reference#getStream()
     */
    @Override
    public InputStream getStream() throws IOException {
        return new FileInputStream(getInternalURI());
    }


    /*
     * (non-Javadoc)
     * @see org.javarosa.core.reference.Reference#getURI()
     */
    @Override
    public String getURI() {
        return "jr://file" + referencePart;
    }


    /*
     * (non-Javadoc)
     * @see org.javarosa.core.reference.Reference#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        return false;
    }


    /*
     * (non-Javadoc)
     * @see org.javarosa.core.reference.Reference#getOutputStream()
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        return new FileOutputStream(getInternalURI());
    }


    /*
     * (non-Javadoc)
     * @see org.javarosa.core.reference.Reference#remove()
     */
    @Override
    public void remove() {
        // TODO bad practice to ignore return values
        new File(getInternalURI()).delete();
    }


    /*
     * (non-Javadoc)
     * @see org.javarosa.core.reference.Reference#getLocalURI()
     */
    @Override
    public String getLocalURI() {
        return getInternalURI();
    }


    /*
     * (non-Javadoc)
     * @see org.javarosa.core.reference.Reference#probeAlternativeReferences()
     */
    @Override
    public Reference[] probeAlternativeReferences() {
        // TODO Auto-generated method stub
        return null;
    }

}
