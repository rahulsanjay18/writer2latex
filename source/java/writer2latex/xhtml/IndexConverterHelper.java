/************************************************************************
 *
 *	IndexConverterBase.java
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
 *  Copyright: 2002-2018 by Henrik Just
 *
 *  All Rights Reserved.
 * 
 *  Version 1.6.1 (2018-08-07)
 *
 */
package writer2latex.xhtml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import writer2latex.office.OfficeReader;
import writer2latex.office.XMLString;
import writer2latex.util.Misc;

/** This is a base class for conversion of indexes (table of contents, bibliography, alphabetical index,
 *  list of tables, list of figures, user index)
 */
abstract class IndexConverterHelper extends ConverterHelper {
	
	private String sSourceName;
	
    private List<IndexData> indexes = new ArrayList<IndexData>(); // Data for all indexes of this type
    
	/** Construct a new index converter
	 * 
	 * @param ofr the office reader used to read the source document
	 * @param config the configuration
	 * @param converter the converter
	 * @param sSourceName the name of the source data element in the index
	 */
	IndexConverterHelper(OfficeReader ofr, XhtmlConfig config, Converter converter, String sSourceName) {
        super(ofr,config,converter);
		this.sSourceName = sSourceName;
    }
	
	/** Generate the contents of the index given by the supplied index data
	 * 
	 * @param data the data describing the index
	 */
	abstract void generate(IndexData data);
	
    /** Generate all indexes of this type. This method should be called after all other content has been converted.
     */
    void generate() {
        int nSaveOutFileIndex = converter.getOutFileIndex();
	    int nIndexCount = indexes.size();
	    for (int i=0; i<nIndexCount; i++) {
	        converter.changeOutFile(indexes.get(i).nOutFileIndex);
	        generate(indexes.get(i));
	    }
        converter.changeOutFile(nSaveOutFileIndex);
    }
	
    /** Handle an index
     * 
     * @param onode an index node
     * @param hnode the index will be added to this block HTML node
     * @param nChapterNumber the chapter number for this index
     */
    void handleIndex(Element onode, Element hnode, int nChapterNumber) {
        Element source = Misc.getChildByTagName(onode,sSourceName);
        if (source!=null) {
            Element container = createContainer(onode, hnode); 
            convertTitle(source, container);
            convertContent(source, container, nChapterNumber);
        }
    }
    
    // Create a container node for the index
    private Element createContainer(Element source, Element hnode) {
		Element container = converter.createAlternativeElement("section","div");
		hnode.appendChild(container);

		String sName = source.getAttribute(XMLString.TEXT_NAME);
		if (sName!=null) {
			converter.addTarget(container,sName);
		}
		
		String sStyleName = source.getAttribute(XMLString.TEXT_STYLE_NAME);
		if (sStyleName!=null) {
	        StyleInfo sectionInfo = new StyleInfo();
	        getSectionSc().applyStyle(sStyleName,sectionInfo);
	        applyStyle(sectionInfo,container);
		}
		return container;
    }
    
    // Convert the index title and add it to the container
    private void convertTitle(Element source, Element container) {
        Node title = Misc.getChildByTagName(source,XMLString.TEXT_INDEX_TITLE_TEMPLATE);
        if (title!=null) {
            Element h1 = converter.createElement("h1");
            container.appendChild(h1);
            String sStyleName = Misc.getAttribute(title,XMLString.TEXT_STYLE_NAME);
    		StyleInfo info = new StyleInfo();
    		info.sTagName = "h1";
    		getHeadingSc().applyStyle(1, sStyleName, info);
    		applyStyle(info,h1);
            getTextCv().traversePCDATA(title,h1);
        }
    }
    
    // Convert the index body and add it to the container
    private void convertContent(Element source, Element container, int nChapterNumber) {
    	Element ul = converter.createElement("ul");
    	// TODO: Support column formatting from the index source
    	ul.setAttribute("style", "list-style-type:none;margin:0;padding:0");
    	container.appendChild(ul);
    	
    	// We are not populating the index now, but collects information to generate it later
    	IndexData data = new IndexData();
    	data.nOutFileIndex = converter.getOutFileIndex();
    	data.onode = source;
    	data.nChapterNumber = nChapterNumber;
    	data.hnode = ul;
    	indexes.add(data);
    }

}
