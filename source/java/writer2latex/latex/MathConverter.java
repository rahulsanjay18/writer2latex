/************************************************************************
 *
 *  MathConverter.java
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
 *  Copyright: 2002-2014 by Henrik Just
 *
 *  All Rights Reserved.
 * 
 *  Version 1.4 (2014-09-03)
 *
 */

package writer2latex.latex;


import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import writer2latex.base.ConverterBase.TexMathsStyle;
import writer2latex.office.EmbeddedObject;
import writer2latex.office.EmbeddedXMLObject;
import writer2latex.office.MIMETypes;
import writer2latex.office.OfficeReader;
import writer2latex.office.StyleWithProperties;
import writer2latex.office.TableReader;
import writer2latex.office.XMLString;
import writer2latex.util.Misc;

/**
 *  This ConverterHelper converts mathematical content to LaTeX.
 *  It works slightly different than the other helpers: A number of elements may or may not
 *  have content that should be converted to math. Thus the methods offered first examines
 *  the content. If it turns out to be a mathematical formula, it is converted. Otherwise
 *  nothing is done, and the method returns false.
 *  Mathematical content may be MathML (with StarMath annotation), TexMaths or (the now obsolete) OOoLaTeX
 */
public final class MathConverter extends ConverterHelper {
	    
    private StarMathConverter smc;
	
    private boolean bContainsFormulas = false;
    private boolean bAddParAfterDisplay = false;
    
    private boolean bNeedTexMathsPreamble = false;
    private boolean bNeedOOoLaTeXPreamble = false;
    
    private Element theEquation = null;
    private Element theSequence = null;
	
    public MathConverter(OfficeReader ofr,LaTeXConfig config, ConverterPalette palette) {
        super(ofr,config,palette);
        smc = new StarMathConverter(palette.getI18n(),config);
        bAddParAfterDisplay = config.formatting()>=LaTeXConfig.CONVERT_MOST;
    }

    public void appendDeclarations(LaTeXDocumentPortion pack, LaTeXDocumentPortion decl) {
        if (bContainsFormulas) {
        	smc.appendDeclarations(pack,decl);
        }
        if (bNeedTexMathsPreamble) {
        	// The preamble may be stored as a user defined property (newline is represented as paragraph sign)
        	Map<String,String> props = palette.getMetaData().getUserDefinedMetaData();
        	if (props.containsKey("TexMathsPreamble")) {
        		decl.append("% TexMaths preamble\n")
        		    .append(props.get("TexMathsPreamble").replace('\u00a7', '\n'));
        	}
        }
        if (bNeedOOoLaTeXPreamble) {
            // The preamble may be stored in the description
            String sDescription = palette.getMetaData().getDescription();
            int nStart = sDescription.indexOf("%%% OOoLatex Preamble %%%%%%%%%%%%%%");
            int nEnd = sDescription.indexOf("%%% End OOoLatex Preamble %%%%%%%%%%%%");
            if (nStart>-1 && nEnd>nStart) {
                decl.append("% OOoLaTeX preamble").nl()
                    .append(sDescription.substring(nStart+37,nEnd));
            }
        }
    }
	
    
    // TODO: Replace with a method "handleEquation"
    public String convert(Element formula) {
        // TODO: Use settings to determine display mode/text mode
        // formula must be a math:math node
        // First try to find a StarMath annotation
    	Node semantics = Misc.getChildByTagName(formula,XMLString.SEMANTICS); // Since OOo 3.2
    	if (semantics==null) {
    		semantics = Misc.getChildByTagName(formula,XMLString.MATH_SEMANTICS);
    	}
		if (semantics!=null) {
			Node annotation = Misc.getChildByTagName(semantics,XMLString.ANNOTATION); // Since OOo 3.2
			if (annotation==null) {
				annotation = Misc.getChildByTagName(semantics,XMLString.MATH_ANNOTATION);
			}
            if (annotation!=null) {
                String sStarMath = "";
                if (annotation.hasChildNodes()) {
                    NodeList anl = annotation.getChildNodes();
                    int nLen = anl.getLength();
                    for (int i=0; i<nLen; i++) {
                        if (anl.item(i).getNodeType() == Node.TEXT_NODE) {
                            sStarMath+=anl.item(i).getNodeValue();
                        }
                    }
                    bContainsFormulas = true;      
                    return smc.convert(sStarMath);
                }
            }
        }
        // No annotation was found. In this case we should convert the mathml,
        // but currently we ignore the problem.
        // TODO: Investigate if Vasil I. Yaroshevich's MathML->LaTeX
        // XSL transformation could be used here. (Potential problem:
        // OOo uses MathML 1.01, not MathML 2)
		if (formula.hasChildNodes()) {
			return "\\text{Warning: No StarMath annotation}";
		}
		else { // empty formula
			return " ";
		}
			
    }

    /** Try to convert a draw:frame or draw:g element as an (inline) TexMaths or OOoLaTeX equation
     * 
     * @param node the element containing the equation (draw:frame or draw:g)
     * @param ldp the LaTeXDocumentPortion to contain the converted equation
     * 
     * @return true if this elements happen to be a TexMaths equation, otherwise false
     */
    public boolean handleTexMathsEquation(Element node, LaTeXDocumentPortion ldp) {
    	String sLaTeX = null;
    	Element equation = palette.getTexMathsEquation(node);
    	if (equation!=null) {
    		sLaTeX = Misc.getPCDATA(equation);
    		if (sLaTeX!=null) { bNeedTexMathsPreamble = true; }
    	}
    	else { // Try OOoLaTeX
    		// The LaTeX code is embedded in a custom style attribute:
    		StyleWithProperties style = ofr.getFrameStyle(Misc.getAttribute(node, XMLString.DRAW_STYLE_NAME));
    		if (style!=null) {
    			sLaTeX = style.getProperty("OOoLatexArgs");
    			if (sLaTeX!=null) { bNeedOOoLaTeXPreamble = true; }
    		}
    	}
    	if (sLaTeX!=null) {
    		// Format is <point size>X<mode>X<TeX code>X<format>X<resolution>X<transparency>
    		// where X is a paragraph sign
    		switch (palette.getTexMathsStyle(sLaTeX)) {
    		case inline:
    			ldp.append("$").append(palette.getTexMathsEquation(sLaTeX)).append("$");
    			break;
    		case display:
    			ldp.append("$\\displaystyle ").append(palette.getTexMathsEquation(sLaTeX)).append("$");
    			break;
    		case latex:    		
    			ldp.append(palette.getTexMathsEquation(sLaTeX));
    		}
    		return true;
    	}
    	return false;
    }

    /** Try to convert a table as a display equation:
     *  A 1 row by 2 columns table in which each cell contains exactly one paragraph,
     *  the left cell contains exactly one formula and the right cell contains exactly
     *  one sequence number is treated as a (numbered) display equation.
     *  This happens to coincide with the AutoText provided with OOo Writer :-)
     *  @param table the table reader
     *  @param ldp the LaTeXDocumentPortion to contain the converted equation
     *  @return true if the conversion was successful, false if the table
     * did not represent a display equation
     */
    public boolean handleDisplayEquation(TableReader table, LaTeXDocumentPortion ldp) {
    	if (table.getRowCount()==1 && table.getColCount()==2 &&
    		OfficeReader.isSingleParagraph(table.getCell(0, 0)) && OfficeReader.isSingleParagraph(table.getCell(0, 1)) ) {
    		// Table of the desired form
    		if (parseDisplayEquation(Misc.getFirstChildElement(table.getCell(0, 0))) && theEquation!=null && theSequence==null) {
    			// Found equation in first cell
    			Element myEquation = theEquation;
    			if (parseDisplayEquation(Misc.getFirstChildElement(table.getCell(0, 1))) && theEquation==null && theSequence!=null) {
    				// Found sequence in second cell
    				handleDisplayEquation(myEquation, theSequence, ldp);
    				return true;
    			}
    		}
    	}
    	return false;
    }

    /**Try to convert a paragraph as a display equation:
     * A paragraph which contains exactly one formula + at most one sequence
     * number is treated as a display equation. Other content must be brackets
     * or whitespace (possibly with formatting).
     * @param node the paragraph
     * @param ldp the LaTeXDocumentPortion to contain the converted equation
     * @return true if the conversion was successful, false if the paragraph
     * did not contain a display equation
     */
    public boolean handleDisplayEquation(Element node, LaTeXDocumentPortion ldp) {
        if (parseDisplayEquation(node) && theEquation!=null) {
        	handleDisplayEquation(theEquation, theSequence, ldp);
        	return true;
        }
        else {
            return false;
        }
    }
    
    private void handleDisplayEquation(Element equation, Element sequence, LaTeXDocumentPortion ldp) {
    	boolean bTexMaths = equation.getTagName().equals(XMLString.SVG_DESC);
    	TexMathsStyle style = TexMathsStyle.inline;
    	String sLaTeX;
    	if (bTexMaths) {
    		// TeXMaths equation
    		sLaTeX = palette.getTexMathsEquation(Misc.getPCDATA(equation));
    		style = palette.getTexMathsStyle(Misc.getPCDATA(equation));
    		if (sLaTeX!=null) { bNeedTexMathsPreamble = true; }
    	}
    	else {
    		// MathML equation
    		sLaTeX = convert(equation);
    	}
    	if (sLaTeX!=null && !" ".equals(sLaTeX)) { // ignore empty formulas
    		if (!bTexMaths || style!=TexMathsStyle.latex) {
    			// Unfortunately we can't do numbered equations for TexMaths equations with latex style
    			if (sequence!=null) {
    				// Numbered equation
    				ldp.append("\\begin{equation}");
    				palette.getFieldCv().handleSequenceLabel(sequence,ldp);
    				if (bTexMaths && style==TexMathsStyle.inline) {
    					ldp.append("\\textstyle ");
    				}
    				ldp.nl()
    				.append(sLaTeX).nl()
    				.append("\\end{equation}").nl();
    			}
    			else {
    				// Unnumbered equation
    				ldp.append("\\begin{equation*}");
    				if (bTexMaths && style==TexMathsStyle.inline) {
    					ldp.append("\\textstyle ");
    				}
    				ldp.nl()
    				.append(sLaTeX).nl()
    				.append("\\end{equation*}").nl();
    			}    	
    		}
    		else {
    			ldp.append(sLaTeX).nl();
    		}
			if (bAddParAfterDisplay) { ldp.nl(); }
    	}
    }
    
	/** Determine whether or not a paragraph contains a display equation.
	 *  A paragraph is a display equation if it contains a single formula and no text content except whitespace
	 *  and an optional sequence number which may be in brackets.
	 *  As a side effect, this method keeps a reference to the equation and the sequence number
	 * 
	 * @param node the paragraph
	 * @return true if this is a display equation
	 */
	private boolean parseDisplayEquation(Node node) {
		theEquation = null;
		theSequence = null;
		return doParseDisplayEquation(node);
	}
	
    private boolean doParseDisplayEquation(Node node) {
        Node child = node.getFirstChild();
        while (child!=null) {
        	if (Misc.isElement(child)) {
        		Element elm = (Element) child;
        		String sName = elm.getTagName();
        		// First check for MathML or TexMaths equation
        		Element equation = getMathmlEquation(elm);
        		if (equation==null) {
        			equation = palette.getTexMathsEquation(elm);            		
        		}
        	
        		if (equation!=null) {
        			if (theEquation==null) {
        				theEquation = equation;
        			}
        			else { // two or more equations -> not a display
        				return false;
        			}
        		}
        		else if (XMLString.TEXT_SEQUENCE.equals(sName)) {
        			if (theSequence==null) {
        				theSequence = elm;
        			}
        			else { // two sequence numbers -> not a display
        				return false;
        			}
        		}
        		else if (XMLString.TEXT_SPAN.equals(sName)) {
        			if (!doParseDisplayEquation(child)) {
        				return false;
        			}
        		}
        		else if (XMLString.TEXT_S.equals(sName)) {
        			// Spaces are allowed
        		}
        		else if (XMLString.TEXT_TAB.equals(sName)) {
        			// Tab stops are allowed
        		}
        		else if (XMLString.TEXT_TAB_STOP.equals(sName)) { // old
        			// Tab stops are allowed
        		}
        		else if (XMLString.TEXT_SOFT_PAGE_BREAK.equals(sName)) { // since ODF 1.1
        			// Soft page breaks are allowed
        		}
        		else {
        			// Other elements -> not a display
        			return false;
        		}
        	}
            else if (Misc.isText(child)) {
                String s = child.getNodeValue();
                int nLen = s.length();
                for (int i=0; i<nLen; i++) {
                    char c = s.charAt(i);
                    if (c!='(' && c!=')' && c!='[' && c!=']' && c!='{' && c!='}' && c!=' ' && c!='\u00A0') {
                        // Characters except brackets and whitespace -> not a display
                        return false;
                    }
                }
            }
            child = child.getNextSibling();
        }
        return true;
    }

    /** Get a MathML formula from a draw:frame
     * 
     * @param node the draw:frame
     * @return the MathML element, or null if this is not a MathML formula
     */
    private Element getMathmlEquation(Element node) {
        if (node.getTagName().equals(XMLString.DRAW_FRAME)) {
            node=Misc.getFirstChildElement(node);
        }
        
        String sHref = Misc.getAttribute(node,XMLString.XLINK_HREF);
		
        if (sHref!=null) { // Embedded object in package or linked object
            if (ofr.isInPackage(sHref)) { // Embedded object in package
                if (sHref.startsWith("#")) { sHref=sHref.substring(1); }
                if (sHref.startsWith("./")) { sHref=sHref.substring(2); }
                EmbeddedObject object = palette.getEmbeddedObject(sHref); 
                if (object!=null) {
                    if (MIMETypes.MATH.equals(object.getType()) || MIMETypes.ODF.equals(object.getType())) { // Formula!
                        try {
                            Document formuladoc = ((EmbeddedXMLObject) object).getContentDOM();
                            Element formula = Misc.getChildByTagName(formuladoc,XMLString.MATH); // Since OOo 3.2
                            if (formula==null) {
                            	formula = Misc.getChildByTagName(formuladoc,XMLString.MATH_MATH);
                            }
                            return formula;
                        }
                        catch (org.xml.sax.SAXException e) {
                            e.printStackTrace();
                        }
                        catch (java.io.IOException e) {
                            e.printStackTrace();
                        }
	                }
                }
            }
        }
        else { // flat XML, object is contained in node
            Element formula = Misc.getChildByTagName(node,XMLString.MATH); // Since OOo 3.2
            if (formula==null) {
            	formula = Misc.getChildByTagName(node,XMLString.MATH_MATH);
            }
            return formula;
        }
        return null;
    }


}