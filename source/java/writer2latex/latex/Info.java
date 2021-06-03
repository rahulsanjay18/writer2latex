/************************************************************************
 *
 *  Info.java
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
 *  Version 1.6 (2015-06-21)
 *
 */

package writer2latex.latex;

import org.w3c.dom.Element;

import writer2latex.util.Misc;
import writer2latex.office.OfficeReader;
import writer2latex.office.StyleWithProperties;
import writer2latex.office.XMLString;


/** This class creates various information to the user about the conversion.
 */
class Info extends ConverterHelper {
	
	@Override public void appendDeclarations(LaTeXDocumentPortion pack, LaTeXDocumentPortion decl) {
		// Currently nothing
	}
	
    Info(OfficeReader ofr, LaTeXConfig config, ConverterPalette palette) {
        super(ofr,config,palette);
    }

    void addDebugInfo(Element node, LaTeXDocumentPortion ldp) {	
        if (config.debug()) {
            ldp.append("% ").append(node.getNodeName());
            addDebugInfo(node,ldp,XMLString.TEXT_ID);
            addDebugInfo(node,ldp,XMLString.TEXT_NAME);
            addDebugInfo(node,ldp,XMLString.TABLE_NAME);
            addDebugInfo(node,ldp,XMLString.TEXT_STYLE_NAME);
            if (node.getNodeName().equals(XMLString.TEXT_P) || node.getNodeName().equals(XMLString.TEXT_H)) {
                StyleWithProperties style = ofr.getParStyle(node.getAttribute(XMLString.TEXT_STYLE_NAME));
                if (style!=null && style.isAutomatic()) {
                    ldp.append(" ("+style.getParentName()+")");
                }
                ldp.append(" ("+ofr.getParStyles().getDisplayName(node.getAttribute(XMLString.TEXT_STYLE_NAME))+")");
            }
            ldp.nl();
        }
    }

    void addDebugInfo(Element node, LaTeXDocumentPortion ldp, String sAttribute) {
        String sValue = Misc.getAttribute(node,sAttribute);
        if (sValue!=null) {
            ldp.append(" ").append(sAttribute).append("=\"").append(sValue).append("\"");
        }
    }

}