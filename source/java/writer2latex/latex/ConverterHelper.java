/************************************************************************
 *
 *  ConverterHelper.java
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
 *  Copyright: 2002-2016 by Henrik Just
 *
 *  All Rights Reserved.
 * 
 *  Version 1.6 (2015-06-20)
 *
 */

package writer2latex.latex;

import writer2latex.office.OfficeReader;

/**
 *  <p>This is an abstract superclass for converter helpers.</p>
 */
abstract class ConverterHelper {
    
    OfficeReader ofr;
    LaTeXConfig config;
    ConverterPalette palette;
	
    ConverterHelper(OfficeReader ofr, LaTeXConfig config, ConverterPalette palette) {
        this.ofr = ofr;
        this.config = config;
        this.palette = palette;
    }
	
    abstract void appendDeclarations(LaTeXDocumentPortion pack, LaTeXDocumentPortion decl);
    
}