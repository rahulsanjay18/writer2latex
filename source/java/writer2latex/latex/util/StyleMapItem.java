/************************************************************************
 *
 *  StyleMapItem.java
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
 *  Copyright: 2002-2011 by Henrik Just
 *
 *  All Rights Reserved.
 * 
 *  Version 1.2 (2011-03-30) 
 * 
 */
 
package writer2latex.latex.util;

// A struct to hold data about a style map 
public class StyleMapItem {
	public static final int NONE = 0;
	public static final int LINE = 1;
	public static final int PAR = 2;
	
    String sBefore;
    String sAfter;
    String sNext;
    int nBreakAfter;
    boolean bLineBreak;
    boolean bVerbatim;
}
