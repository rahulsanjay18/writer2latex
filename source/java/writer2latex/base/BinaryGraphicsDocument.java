/************************************************************************
 *
 *  BinaryGraphicsDocument.java
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License version 2.1, as published by the Free Software Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *  MA  02111-1307  USA
 *
 *  Copyright: 2002-2015 by Henrik Just
 *
 *  All Rights Reserved.
 * 
 *  Version 1.6 (2015-05-05)
 *
 */

package writer2latex.base;

import java.io.OutputStream;
import java.io.IOException;

import writer2latex.api.OutputFile;


/** This class is used to represent a binary graphics document to be included in the converter result.
 *  I may also represent a linked image, which should <em>not</em> be included (and will produce an empty file
 *  if it is).
 */
public class BinaryGraphicsDocument implements OutputFile {

    private String sFileName;
    private String sMimeType;
    
    private boolean bAcceptedFormat;
    
    private boolean bRecycled = false;
    
    // Data for an embedded image
    private byte[] blob = null;
    private int nOff = 0;
    private int nLen = 0;
    
    /**Constructs a new graphics document.
     * Until data is added using the <code>read</code> methods, the document is considered a link to
     * the image given by the file name.
     *
     * @param sFileName The name or URL of the <code>GraphicsDocument</code>.
     * @param sMimeType the MIME type of the document
     */
    public BinaryGraphicsDocument(String sFileName, String sMimeType) {
        this.sFileName = sFileName; 
        this.sMimeType = sMimeType;
        bAcceptedFormat = false; // or rather "don't know"
    }
    
    /** Construct a new graphics document which is a recycled version of the supplied one.
     *  This implies that all information is identical, but the recycled version does not contain any data.
     *  This is for images that are used more than once in the document.
     * 
     * @param bgd the source document
     */
    public BinaryGraphicsDocument(BinaryGraphicsDocument bgd) {
    	this.sFileName = bgd.getFileName();
    	this.sMimeType = bgd.getMIMEType();
    	this.bAcceptedFormat = bgd.isAcceptedFormat();
    	this.bRecycled = true;
    }
    
    /** Is this graphics document recycled?
     * 
     * @return true if this is the case
     */
    public boolean isRecycled() {
    	return bRecycled;
    }

    /** Set image contents to a byte array
     * 
     * @param data the image data
     * @param bIsAcceptedFormat flag to indicate that the format of the image is acceptable for the converter
     */
    public void setData(byte[] data, boolean bIsAcceptedFormat) {
        setData(data,0,data.length,bIsAcceptedFormat);
    }
    
    /** Set image contents to part of a byte array
     * 
     * @param data the image data
     * @param nOff the offset into the byte array
     * @param nLen the number of bytes to use
     * @param bIsAcceptedFormat flag to indicate that the format of the image is acceptable for the converter
     */
    public void setData(byte[] data, int nOff, int nLen, boolean bIsAcceptedFormat) {
        this.blob = data;
        this.nOff = nOff;
        this.nLen = nLen;
        this.bAcceptedFormat = bIsAcceptedFormat;
    }
    
    /** Does this <code>BinaryGraphicsDocument</code> represent a linked image?
     * 
     * @return true if so
     */
    public boolean isLinked() {
    	return blob==null && !bRecycled;
    }
    
    /** Is this image in an acceptable format for the converter?
     * 
     * @return true if so (always returns false for linked images)
     */
    public boolean isAcceptedFormat() {
    	return bAcceptedFormat;
    }
    
    /** Get the data of the image
     * 
     * @return the image data as a byte array - or null if this is a linked image
     */
    public byte[] getData() {
    	return blob;
    }
    
    // Implement OutputFile
    
    /** Writes out the content to the specified <code>OutputStream</code>.
     *  Linked images will not write any data.
     *
     * @param  os  <code>OutputStream</code> to write out the  content.
     *
     * @throws  IOException  If any I/O error occurs.
     */
    public void write(OutputStream os) throws IOException {
    	if (blob!=null) {
    		os.write(blob, nOff, nLen);
    	}
    }
    
    /** Get the document name or URL</p>
    *
    * @return  The document name or URL
    */
   public String getFileName() {
       return sFileName;
   }
   
    /** Get the MIME type of the document.
     *
     * @return  The MIME type or null if this is unknown
     */
	public String getMIMEType() {
		return sMimeType;
	}
	
    /** Is this document a master document?
     * 
     *  @return false - a graphics file is never a master document
     */
    public boolean isMasterDocument() {
		return false;
	}
    
    /** Does this document contain formulas?
     * 
     *  @return false - a graphics file does not contain formulas
     */
    public boolean containsMath() {
    	return false;
    }
}