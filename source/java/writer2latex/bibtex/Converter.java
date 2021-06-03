/************************************************************************
 *
 *  Converter.java
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
 *  Copyright: 2001-2015 by Henrik Just
 *
 *  All Rights Reserved.
 * 
 *  Version 1.6 (2015-06-22)
 *
 */

package writer2latex.bibtex;

import writer2latex.api.Config;
import writer2latex.base.ConverterBase;
import writer2latex.latex.LaTeXConfig;
import writer2latex.util.Misc;

import java.io.IOException;

/** This class exports bibliographic information from an OpenDocument text file to a BibTeX data file
 */
public final class Converter extends ConverterBase {
                        
    // Implement converter API

	// TODO: Doesn't really use the configuration - should use some fake config
    private LaTeXConfig config;
    
    public Converter() {
        super();
        config = new LaTeXConfig();
    }

    public Config getConfig() {
    	return config;
    }
	
	// Extend converter base
    
    /** Convert the document into BibTeX format.</p>
     *
     *  @throws IOException If any I/O error occurs.
     */
    @Override public void convertInner() throws IOException {      
        sTargetFileName = Misc.trimDocumentName(sTargetFileName,".bib");

        BibTeXDocument bibDoc = new BibTeXDocument(sTargetFileName,true,ofr);
      
        converterResult.addDocument(bibDoc);
    }

}