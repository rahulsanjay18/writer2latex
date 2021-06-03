/************************************************************************
 *
 *  BlockConverter.java
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
 *  Version 1.6 (2015-04-15)
 *
 */

package writer2latex.latex;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import writer2latex.latex.util.Context;
import writer2latex.latex.util.StyleMap;
import writer2latex.office.OfficeReader;
import writer2latex.office.XMLString;
import writer2latex.util.Misc;

/**
 *  This class handles basic block content, such as the main text body,
 *  sections, tables, lists, headings and paragraphs.</p>
 */
public class BlockConverter extends ConverterHelper {

    public BlockConverter(OfficeReader ofr, LaTeXConfig config, ConverterPalette palette) {
        super(ofr,config,palette);
    }
	
    public void appendDeclarations(LaTeXDocumentPortion pack, LaTeXDocumentPortion decl) {
        // currently do nothing..
    }


    /** <p> Traverse block text (eg. content of body, section, list item).
     * This is traversed in logical order and dedicated handlers take care of
     * each block element.</p>
     * <p> (Note: As a rule, all handling of block level elements should add a
     * newline to the LaTeX document at the end of the block)</p>
     * @param node The element containing the block text
     * @param ldp the <code>LaTeXDocumentPortion</code> to which
     * LaTeX code should be added
     * @param oc the current context
     */
    public void traverseBlockText(Element node, LaTeXDocumentPortion ldp, Context oc) {
        Context ic = (Context) oc.clone();

        // The current paragraph block:
        StyleMap blockMap = config.getParBlockStyleMap();
        String sBlockName = null;

        if (node.hasChildNodes()) {
            NodeList list = node.getChildNodes();
            int nLen = list.getLength();

            for (int i = 0; i < nLen; i++) {
                Node childNode = list.item(i);
				
                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element child = (Element)childNode;
                    String sTagName = child.getTagName();
					
                    // Start/End a paragraph block (not in tables)
                    if (!ic.isInTable()) {
                        if (sTagName.equals(XMLString.TEXT_P)) {
                            String sStyleName = ofr.getParStyles().getDisplayName(child.getAttribute(XMLString.TEXT_STYLE_NAME));
                            if (sBlockName!=null && !blockMap.isNext(sBlockName,sStyleName)) {
                                // end current block
                                String sAfter = blockMap.getAfter(sBlockName);
                                if (sAfter.length()>0) ldp.append(sAfter).nl();
                                sBlockName = null;
                                ic.setVerbatim(false);
                            }
                            if (sBlockName==null && blockMap.contains(sStyleName)) {
                                // start a new block
                                sBlockName = sStyleName;
                                String sBefore = blockMap.getBefore(sBlockName);
                                if (sBefore.length()>0) ldp.append(sBefore).nl();
                                ic.setVerbatim(blockMap.getVerbatim(sStyleName));
                            }
                        }
                        else if (sBlockName!=null) {
                            // non-paragraph: end current block
                            String sAfter = blockMap.getAfter(sBlockName);
                            if (sAfter.length()>0) ldp.append(sAfter).nl();
                            sBlockName = null;
                            ic.setVerbatim(false);
                        }
                    }
					
                    palette.getFieldCv().flushReferenceMarks(ldp,ic);
                    palette.getIndexCv().flushIndexMarks(ldp,ic);
					
                    palette.getInfo().addDebugInfo(child,ldp);

                    // Basic block content; handle by this class
                    if (sTagName.equals(XMLString.TEXT_P)) {
                        // is this a caption?
                        String sSequence = ofr.getSequenceName(child);
                        if (ofr.isFigureSequenceName(sSequence)) {
                            palette.getDrawCv().handleCaption(child,ldp,ic);
                        }
                        else if (ofr.isTableSequenceName(sSequence)) {
                            // Next node *should* be a table
                            if (i+1<nLen && Misc.isElement(list.item(i+1),XMLString.TABLE_TABLE)) {
                                // Found table with caption above
                                palette.getTableCv().handleTable((Element)list.item(++i),child,true,ldp,ic);
                            }
                            else {
                                // Found lonely caption
                                palette.getTableCv().handleCaption(child,ldp,ic);
                            }
                        }
                        else {
                            palette.getParCv().handleParagraph(child,ldp,ic,i==nLen-1);
                        }
                    }

                    else if(sTagName.equals(XMLString.TEXT_H)) {
                        palette.getHeadingCv().handleHeading(child,ldp,ic);
                    }
                    
                    else if (sTagName.equals(XMLString.TEXT_LIST)) { // oasis
                        palette.getListCv().handleList(child,ldp,ic);
                    }

                    else if (sTagName.equals(XMLString.TEXT_UNORDERED_LIST)) {
                    	palette.getListCv().handleList(child,ldp,ic);
                    }
                    
                    else if (sTagName.equals(XMLString.TEXT_ORDERED_LIST)) {
                    	palette.getListCv().handleList(child,ldp,ic);
                    }
                    else if (sTagName.equals(XMLString.TABLE_TABLE)) {
                        // Next node *could* be a caption
                        if (i+1<nLen && Misc.isElement(list.item(i+1),XMLString.TEXT_P) &&
                            ofr.isTableSequenceName(ofr.getSequenceName((Element)list.item(i+1)))) {
                            // Found table with caption below
                            palette.getTableCv().handleTable(child,(Element)list.item(++i),false,ldp,oc);
                        }
                        else {
                            // Found table without caption
                            palette.getTableCv().handleTable(child,null,false,ldp,oc);
                        }
                    }

                    else if (sTagName.equals(XMLString.TABLE_SUB_TABLE)) {
                        palette.getTableCv().handleTable(child,null,true,ldp,ic);
                    }

                    else if (sTagName.equals(XMLString.TEXT_SECTION)) {
                        palette.getSectionCv().handleSection(child,ldp,ic);
                    }

                    // Draw elements may appear in block context if they are
                    // anchored to page
                    else if (sTagName.startsWith("draw:")) {
                        palette.getDrawCv().handleDrawElement(child,ldp,ic);
                    }
					
                    // Indexes
                    else if (sTagName.equals(XMLString.TEXT_TABLE_OF_CONTENT)) {
                        palette.getIndexCv().handleTOC(child,ldp,ic);
                    }

                    else if (sTagName.equals(XMLString.TEXT_ILLUSTRATION_INDEX)) {
                        palette.getIndexCv().handleLOF(child,ldp,ic);
                    }

                    else if (sTagName.equals(XMLString.TEXT_TABLE_INDEX)) {
                        palette.getIndexCv().handleLOT(child,ldp,ic);
                    }

                    else if (sTagName.equals(XMLString.TEXT_OBJECT_INDEX)) {
                        palette.getIndexCv().handleObjectIndex(child,ldp,ic);
                    }

                    else if (sTagName.equals(XMLString.TEXT_USER_INDEX)) {
                        palette.getIndexCv().handleUserIndex(child,ldp,ic);
                    }

                    else if (sTagName.equals(XMLString.TEXT_ALPHABETICAL_INDEX)) {
                        palette.getIndexCv().handleAlphabeticalIndex(child,ldp,ic);
                    }

                    else if (sTagName.equals(XMLString.TEXT_BIBLIOGRAPHY)) {
                        palette.getBibCv().handleBibliography(child,ldp,ic);
                    }

                    // Sequence declarations appear in the main text body (before the actual content)
                    else if (sTagName.equals(XMLString.TEXT_SEQUENCE_DECLS)) {
                        palette.getFieldCv().handleSequenceDecls(child);
                    }
                    // other tags are ignored
                }
            }
        }

        if (!oc.isInTable() && sBlockName!=null) {
            // end current block
            String sAfter = blockMap.getAfter(sBlockName);
            if (sAfter.length()>0) ldp.append(sAfter).nl();
            sBlockName = null;
        }
        palette.getFieldCv().flushReferenceMarks(ldp,ic);
        palette.getIndexCv().flushIndexMarks(ldp,ic);


    }
   
}