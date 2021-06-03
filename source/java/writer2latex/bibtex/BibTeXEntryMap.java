/************************************************************************
 *
 *  BibTeXEntryMap.java
 *
 *  This library is free software); you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License version 2.1, as published by the Free Software Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY); without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library); if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *  MA  02111-1307  USA
 *
 *  Copyright: 2002-2014 by Henrik Just
 *
 *  All Rights Reserved.
 * 
 *  Version 1.6 (2014-12-16)
 *
 */
package writer2latex.bibtex;

import java.util.HashMap;
import java.util.Map;

import writer2latex.office.BibMark.EntryType;

/**
 * This class provides static methods to map the the entry types of an ODF
 * bibliography mark to and from BibTeX field names
 * 
 */
public class BibTeXEntryMap {
	private static Map<EntryType, String> bibTeXFields = null;
	private static Map<String, EntryType> entryTypes = null;

	private static void createMaps() {
		// Note the BibTeX fileds key and crossref are not supported in ODF
		bibTeXFields = new HashMap<EntryType, String>();
		bibTeXFields.put(EntryType.address, "address");
		bibTeXFields.put(EntryType.annote, "annote");
		bibTeXFields.put(EntryType.author, "author");
		bibTeXFields.put(EntryType.booktitle, "booktitle");
		bibTeXFields.put(EntryType.chapter, "chapter");
		bibTeXFields.put(EntryType.edition, "edition");
		bibTeXFields.put(EntryType.editor, "editor");
		bibTeXFields.put(EntryType.howpublished, "howpublished");
		bibTeXFields.put(EntryType.institution, "institution");
		bibTeXFields.put(EntryType.journal, "journal");
		bibTeXFields.put(EntryType.month, "month");
		bibTeXFields.put(EntryType.note, "note");
		bibTeXFields.put(EntryType.number, "number");
		bibTeXFields.put(EntryType.organizations, "organization");
		bibTeXFields.put(EntryType.pages, "pages");
		bibTeXFields.put(EntryType.publisher, "publisher");
		bibTeXFields.put(EntryType.school, "school");
		bibTeXFields.put(EntryType.series, "series");
		bibTeXFields.put(EntryType.title, "title");
		bibTeXFields.put(EntryType.report_type, "type");
		bibTeXFields.put(EntryType.volume, "volume");
		bibTeXFields.put(EntryType.year, "year");
		bibTeXFields.put(EntryType.url, "url");
		bibTeXFields.put(EntryType.custom1, "custom1");
		bibTeXFields.put(EntryType.custom2, "custom2");
		bibTeXFields.put(EntryType.custom3, "custom3");
		bibTeXFields.put(EntryType.custom4, "custom4");
		bibTeXFields.put(EntryType.custom5, "custom5");
		bibTeXFields.put(EntryType.isbn, "isbn");

		entryTypes = new HashMap<String, EntryType>();
		for (EntryType entryType : bibTeXFields.keySet()) {
			entryTypes.put(bibTeXFields.get(entryType), entryType);
		}
	}

	/**
	 * Return BibTeX field name corresponding to and entry type
	 * 
	 * @param entryType
	 *            the entry type
	 * @return the BibTeX field name, or null if there is no corresponding
	 *         BibTeX field
	 */
	public static final String getFieldName(EntryType entryType) {
		if (bibTeXFields == null) {
			createMaps();
		}
		return bibTeXFields.containsKey(entryType) ? bibTeXFields.get(entryType) : null;
	}

	/**
	 * Return entry type corresponding to a BibTeX field
	 * 
	 * @param sFieldName
	 *            the BibTeX field name
	 * @return the entry type, or null if there is no corresponding entry type
	 */
	public static final EntryType getEntryType(String sFieldName) {
		if (bibTeXFields == null) {
			createMaps();
		}
		sFieldName = sFieldName.toLowerCase();
		return entryTypes.containsKey(sFieldName) ? entryTypes.get(sFieldName) : null;
	}
}
