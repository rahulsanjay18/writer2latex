/************************************************************************
 *
 *  BibTeXDocument.java
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
 *  Version 1.6 (2015-07-01)
 *
 */

package writer2latex.bibtex;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.List;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.w3c.dom.Element;

import writer2latex.api.ConverterFactory;
import writer2latex.api.MIMETypes;
import writer2latex.api.OutputFile;
import writer2latex.latex.LaTeXConfig;
import writer2latex.latex.i18n.ClassicI18n;
import writer2latex.latex.i18n.I18n;
import writer2latex.util.ExportNameCollection;
import writer2latex.office.BibMark;
import writer2latex.office.BibMark.EntryType;
import writer2latex.office.OfficeReader;

/** Class representing a BibTeX document
 */
public class BibTeXDocument implements OutputFile {
    private static final String FILE_EXTENSION = ".bib";
	
    private String sName;
    private Hashtable<String, BibMark> entries = new Hashtable<String, BibMark>();
    private ExportNameCollection exportNames = new ExportNameCollection("",true,"_-:");
    private I18n i18n;
    
    private boolean bIsMaster;

    /** Constructs a new BibTeX Document based on an office document 
     *
     * @param sName The name of the document
     * @param bIsMaster is this a master document?
     * @param ofr the office document
     */
    public BibTeXDocument(String sName, boolean bIsMaster, OfficeReader ofr) {
    	this.sName = sName;
        this.bIsMaster = bIsMaster;
        loadEntries(ofr);
        // Use default config (only ascii, no extra font packages)
        i18n = new ClassicI18n(new LaTeXConfig());
    }
    
    private void loadEntries(OfficeReader ofr) {
    	List<Element> bibMarks = ofr.getBibliographyMarks();
    	for (Element bibMark : bibMarks) {
    		BibMark entry = new BibMark(bibMark);
            entries.put(entry.getIdentifier(),entry);
            exportNames.addName(entry.getIdentifier());    		
    	}
    }
    
    // Methods to query the content
    
    /** Test whether or not this BibTeX document contains any entries
     * 
     * @return true if there is one or more entries in the document
     */
    public boolean isEmpty() {
    	return entries.size()==0;
    }
    
    /** Get export name for an identifier
     * 
     *  @param sIdentifier the identifier
     *  @return the export name
     */
    public String getExportName(String sIdentifier) {
        return exportNames.getExportName(sIdentifier);
    }
    
    /** Returns the document name without file extension
     *
     * @return the document name without file extension
     */
    public String getName() {
        return sName;
    }
    
    // Implement writer2latex.api.OutputFile
    
    public String getFileName() {
        return new String(sName + FILE_EXTENSION);
    }
    
	public String getMIMEType() {
		return MIMETypes.BIBTEX;
	}
	
	public boolean isMasterDocument() {
		return bIsMaster;
	}
	
	public boolean containsMath() {
		return false;
	}

    public void write(OutputStream os) throws IOException {
        // BibTeX files are plain ascii
        OutputStreamWriter osw = new OutputStreamWriter(os,"ASCII");
        osw.write("%% This file was converted to BibTeX by Writer2BibTeX ver. "+ConverterFactory.getVersion()+".\n");
        osw.write("%% See http://writer2latex.sourceforge.net for more info.\n");
        osw.write("\n");
        Enumeration<BibMark> enumeration = entries.elements();
        while (enumeration.hasMoreElements()) {
            BibMark entry = enumeration.nextElement();
            osw.write("@");
            osw.write(entry.getEntryType().toUpperCase());
            osw.write("{");
            osw.write(exportNames.getExportName(entry.getIdentifier()));
            osw.write(",\n");
            for (EntryType entryType : EntryType.values()) {
                String sValue = entry.getField(entryType);
                if (sValue!=null) {
                    if (entryType==EntryType.author || entryType==EntryType.editor) {
                        // OOo uses ; to separate authors and editors - BibTeX uses and
                        sValue = sValue.replaceAll(";" , " and ");
                    }
                    osw.write("    ");
                    osw.write(BibTeXEntryMap.getFieldName(entryType).toUpperCase());
                    osw.write(" = {");
                    for (int j=0; j<sValue.length(); j++) {
                        String s = i18n.convert(Character.toString(sValue.charAt(j)),false,"en");
                        if (s.charAt(0)=='\\') { osw.write("{"); }
                        osw.write(s);
                        if (s.charAt(0)=='\\') { osw.write("}"); }
                    }
                    osw.write("},\n");
                }
            }
            osw.write("}\n\n");
        }
        osw.flush();
        osw.close();
    }	

}
