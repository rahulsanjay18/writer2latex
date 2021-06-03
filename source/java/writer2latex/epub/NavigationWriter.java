/************************************************************************
 *
 *  NavigationWriter.java
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
 *  version 1.6 (2015-04-21)
 *
 */
package writer2latex.epub;

import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

import writer2latex.api.ContentEntry;
import writer2latex.api.ConverterResult;
import writer2latex.base.DOMDocument;
import writer2latex.util.Misc;

/** This class writes the EPUB Navigation Document as defined in EPUB 3
 */
public class NavigationWriter extends DOMDocument {

	public NavigationWriter(ConverterResult cr) {
		super("nav", "xhtml");
		
        // create DOM
        Document contentDOM = null;
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            DOMImplementation domImpl = builder.getDOMImplementation();
            DocumentType doctype = domImpl.createDocumentType("xhtml","",""); 
            contentDOM = domImpl.createDocument("http://www.w3.org/1999/xhtml","html",doctype);
        }
        catch (ParserConfigurationException t) { // this should never happen
            throw new RuntimeException(t);
        }
        
        // Populate the DOM tree
        Element doc = contentDOM.getDocumentElement();
        doc.setAttribute("xmlns","http://www.w3.org/1999/xhtml");
        doc.setAttribute("xmlns:epub","http://www.idpf.org/2007/ops");
        doc.setAttribute("xml:lang", cr.getMetaData().getLanguage());
        doc.setAttribute("lang", cr.getMetaData().getLanguage());
        
        // Create and populate the head
        Element head = contentDOM.createElement("head");
        doc.appendChild(head);
        
        Element title = contentDOM.createElement("title");
        head.appendChild(title);
        title.appendChild(contentDOM.createTextNode("EPUB 3 Navigation Document"));
        // Or use the document title cr.getMetaData().getTitle()?
        
        // Create the body
        Element body = contentDOM.createElement("body");
        doc.appendChild(body);
        
        // Create nav element
        Element nav = contentDOM.createElement("nav");
        nav.setAttribute("epub:type", "toc");
        body.appendChild(nav);
        
        // Populate the nav element from the content table in the converter result
        Element currentContainer = body;
        int nCurrentLevel = 0;
        int nCurrentEntryLevel = 0; // This may differ from nCurrentLevel if the heading levels "jump" in then document
        Iterator<ContentEntry> content = cr.getContent().iterator();
        while (content.hasNext()) {
        	ContentEntry entry = content.next();
        	int nEntryLevel = Math.max(entry.getLevel(), 1);
        	
        	if (nEntryLevel<nCurrentLevel) {
        		// Return to higher level
        		for (int i=nEntryLevel; i<nCurrentLevel; i++) {
        			currentContainer = (Element) currentContainer.getParentNode().getParentNode();
        		}
        		nCurrentLevel = nEntryLevel;
        	}
        	else if (nEntryLevel>nCurrentEntryLevel) {
        		// To lower level (always one step; a jump from e.g. heading 1 to heading 3 in the document
        		// is considered an error)
        		currentContainer = (Element) currentContainer.getLastChild();
        		Element ol = contentDOM.createElement("ol");
        		currentContainer.appendChild(ol);
        		currentContainer = ol;
        		nCurrentLevel++;
        	}
        	
        	nCurrentEntryLevel = nEntryLevel;
        	
        	// Create the actual toc entry
        	Element li = contentDOM.createElement("li");
        	currentContainer.appendChild(li);
        	Element a = contentDOM.createElement("a");
        	li.appendChild(a);
        	String sHref = Misc.makeHref(entry.getFile().getFileName());
        	if (entry.getTarget()!=null) { sHref+="#"+entry.getTarget(); }
        	a.setAttribute("href", sHref);
        	a.appendChild(contentDOM.createTextNode(entry.getTitle()));
        }
        
        setContentDOM(contentDOM);
	}

}
