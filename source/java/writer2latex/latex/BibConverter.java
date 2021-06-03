/************************************************************************
 *
 *  BibConverter.java
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
 *  Version 1.6 (2015-07-27)
 *
 */

package writer2latex.latex;

import java.util.Collection;

import org.w3c.dom.Element;

import writer2latex.base.BibliographyGenerator;
import writer2latex.bibtex.BibTeXDocument;

import writer2latex.latex.i18n.ClassicI18n;
import writer2latex.latex.util.BeforeAfter;
import writer2latex.latex.util.Context;

import writer2latex.office.OfficeReader;
import writer2latex.office.StyleWithProperties;
import writer2latex.office.XMLString;
import writer2latex.util.Misc;

/** This class handles bibliographic citations and the bibliography. The result depends on these
 *  configuration options:
 *  <li><code>use_index</code>: If false, the bibliography will be omitted</li>
 *  <li><code>use_bibtex</code> true and <code>external_bibtex_files</code>
 *  empty: The citations will be exported to a BibTeX file, which will be used
 *  for the bibliography</li>
 *  <li><code>use_bibtex</code> true and <code>external_bibtex_files</code>
 *  non-empty: The citations will be not be exported to a BibTeX file, the
 *  files referred to by the option will be used instead</li>
 *  <li><code>use_bibtex</code> false: The bibliography will be exported as
 *  a thebibliography environment
 *  <li><code>bibtex_style</code> If BibTeX is used, this style will be applied
 *  </ul>
 *  The citations will always be exported as \cite commands.
 */
class BibConverter extends ConverterHelper {

    private BibTeXDocument bibDoc = null;
    private boolean bUseBibTeX;
    private String sBibTeXEncoding = null;
    private String sDocumentEncoding = null;
    
    /** Construct a new BibConverter.
     * 
     * @param config the configuration to use 
     * @param palette the ConverterPalette to use
     */
    BibConverter(OfficeReader ofr,LaTeXConfig config, ConverterPalette palette) {
        super(ofr,config,palette);
        
        // We need to create a BibTeX document except if we are using external BibTeX files
        if (!(config.useBibtex() && config.externalBibtexFiles().length()>0)) {
        	bibDoc = new BibTeXDocument(palette.getOutFileName(),false,ofr);
        }
        
        // We need to use a different encoding for the BibTeX files
        if (config.externalBibtexFiles().length()>0) {
        	int nBibTeXEncoding = config.getBibtexEncoding();
        	int nDocumentEncoding = config.getInputencoding();
        	if (config.getBackend()!=LaTeXConfig.XETEX && nBibTeXEncoding>-1 && nBibTeXEncoding!=nDocumentEncoding) {
        		sBibTeXEncoding = ClassicI18n.writeInputenc(nBibTeXEncoding);
            	sDocumentEncoding = ClassicI18n.writeInputenc(nDocumentEncoding);
        	}
        }
        
        // We need to export it 
        bUseBibTeX = config.useBibtex();
    }

    /** Export the bibliography directly as a thebibliography environment (as an alternative to using BibTeX) 
     */
    private class ThebibliographyGenerator extends BibliographyGenerator {
    	// The bibliography is the be inserted in this LaTeX document portion with that context
    	private LaTeXDocumentPortion ldp;
    	private Context context;
    	
    	// The current bibliography item is to formatted with this before/after pair with that context
    	private BeforeAfter itemBa = null;
    	private Context itemContext = null;

    	ThebibliographyGenerator(OfficeReader ofr) {
    		super(ofr,true);
    	}
    	
    	void handleBibliography(Element bibliography, LaTeXDocumentPortion ldp, Context context) {
    		this.ldp = ldp;
    		this.context = context;
    		
    		String sWidestLabel = "";
    		Collection<String> labels = getLabels();
    		for (String sLabel : labels) {
    			if (sLabel.length()>=sWidestLabel.length()) {
    				sWidestLabel = sLabel;
    			}
    		}
    		
        	ldp.append("\\begin{thebibliography}{").append(sWidestLabel).append("}\n");
    		generateBibliography(bibliography);
    		endBibliographyItem();
        	ldp.append("\\end{thebibliography}\n");
    	}
    	
    	@Override protected void insertBibliographyItem(String sStyleName, String sKey) {
    		endBibliographyItem();
    		
    		itemBa = new BeforeAfter();
    		itemContext = (Context) context.clone();

    		// Apply paragraph style (character formatting only)
            StyleWithProperties style = ofr.getParStyle(sStyleName);
            if (style!=null) {
                palette.getI18n().applyLanguage(style,true,true,itemBa);
                palette.getCharSc().applyFont(style,true,true,itemBa,itemContext);
                if (itemBa.getBefore().length()>0) {
                	itemBa.add(" ","");
                    itemBa.enclose("{", "}");
               	}
            }
            
            // Convert item
            ldp.append(itemBa.getBefore());
	        palette.getI18n().pushSpecialTable(palette.getCharSc().getFontName(style));
	        
    		ldp.append("\\bibitem");
    		if (!isNumberedEntries()) {
    			ldp.append("[").append(bibDoc.getExportName(sKey)).append("]");
    		}
    		ldp.append("{").append(bibDoc.getExportName(sKey)).append("} ");
    	}
    	
    	private void endBibliographyItem() {
    		if (itemBa!=null) {
    	        palette.getI18n().popSpecialTable();
        		ldp.append(itemBa.getAfter()).append("\n");    		    			
        		itemBa = null;
    		}
    	}
    	
    	@Override protected void insertBibliographyItemElement(String sStyleName, String sText) {
    		BeforeAfter ba = new BeforeAfter();
    		Context elementContext = (Context) itemContext.clone();
    		
        	// Apply character style
            StyleWithProperties style = ofr.getTextStyle(sStyleName);
           	palette.getCharSc().applyTextStyle(sStyleName,ba,elementContext);
            
           	// Convert text
    		ldp.append(ba.getBefore());
	        palette.getI18n().pushSpecialTable(palette.getCharSc().getFontName(style));
    		ldp.append(palette.getI18n().convert(sText, false, elementContext.getLang()));
            palette.getI18n().popSpecialTable();
    		ldp.append(ba.getAfter());    		
    	}
    }

    /** Append declarations needed by the <code>BibConverter</code> to the preamble.
     * 
     * @param pack the LaTeXDocumentPortion to which declarations of packages (\\usepackage) should be added.
     * @param decl the LaTeXDocumentPortion to which other declarations should be added.
     */
    void appendDeclarations(LaTeXDocumentPortion pack, LaTeXDocumentPortion decl) {
    	// Currently nothing
    }

    /** Process a bibliography
     * 
     * @param node A text:bibliography element
     * @param ldp the LaTeXDocumentPortion to which LaTeX code should be added
     * @param oc the current context
     */
    void handleBibliography(Element node, LaTeXDocumentPortion ldp, Context oc) {
        if (!config.noIndex()) {
	        if (config.useBibtex()) { // Export using BibTeX
	        	handleBibliographyAsBibTeX(ldp);
	        }
	        else { // Export as thebibliography environment
	        	ThebibliographyGenerator bibCv = new ThebibliographyGenerator(ofr);
	            Element source = Misc.getChildByTagName(node,XMLString.TEXT_BIBLIOGRAPHY_SOURCE);
	        	bibCv.handleBibliography(source, ldp, oc);
	        }
        }
    }
    
    private void handleBibliographyAsBibTeX(LaTeXDocumentPortion ldp) {
        // Use the style given in the configuration
        ldp.append("\\bibliographystyle{")
           .append(config.bibtexStyle())
           .append("}").nl();

        // Use BibTeX file from configuration, or exported BibTeX file
        // TODO: For XeTeX, probably use \XeTeXdefaultencoding?
        if (config.externalBibtexFiles().length()>0) {
        	if (sBibTeXEncoding!=null) {
        		ldp.append("\\inputencoding{").append(sBibTeXEncoding).append("}").nl();
        	}
    		ldp.append("\\bibliography{")
               .append(config.externalBibtexFiles())
               .append("}").nl();
        	if (sBibTeXEncoding!=null) {
        		ldp.append("\\inputencoding{").append(sDocumentEncoding).append("}").nl();
        	}
        }
        else {
            ldp.append("\\bibliography{")
               .append(bibDoc.getName())
               .append("}").nl();
        }
    }
	
    /** Process a Bibliography Mark
     * @param node a text:bibliography-mark element
     * @param ldp the LaTeXDocumentPortion to which LaTeX code should be added
     * @param oc the current context
     */
    void handleBibliographyMark(Element node, LaTeXDocumentPortion ldp, Context oc) {
        String sIdentifier = node.getAttribute(XMLString.TEXT_IDENTIFIER);
        if (sIdentifier!=null) {
            // Use original citation if using external files; stripped if exporting BibTeX
            ldp.append("\\cite{")
               .append(config.externalBibtexFiles().length()==0 ? bibDoc.getExportName(sIdentifier) : sIdentifier)
               .append("}");
        }
    }
	
    /** Get the BibTeX document, if any (that is if the document contains bibliographic data <em>and</em>
     * the configuration does not specify external BibTeX files)
     * 
     * @return the BiBTeXDocument, or null if no BibTeX file is needed
     */
    BibTeXDocument getBibTeXDocument() {
    	if (bUseBibTeX && bibDoc!=null && !bibDoc.isEmpty()) {
    		return bibDoc;
    	}
    	return null;
    }
	    
}