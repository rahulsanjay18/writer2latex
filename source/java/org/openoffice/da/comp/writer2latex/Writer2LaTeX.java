/************************************************************************
 *
 *  Writer2LaTeX.java
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
 *  Version 1.6 (2015-05-29)
 *
 */ 
 
package org.openoffice.da.comp.writer2latex;

import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XFrame;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.ui.dialogs.ExecutableDialogResults;
import com.sun.star.ui.dialogs.XExecutableDialog;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import org.openoffice.da.comp.w2lcommon.filter.UNOPublisher.TargetFormat;
import org.openoffice.da.comp.w2lcommon.helper.MessageBox;
import org.openoffice.da.comp.w2lcommon.helper.RegistryHelper;
import org.openoffice.da.comp.w2lcommon.helper.XPropertySetHelper;
       
/** This class implements the ui (dispatch) commands provided by the Writer2LaTeX toolbar.
 *  The actual processing is done by the core classes <code>UNOPublisher</code>,
 *  <code>LaTeXImporter</code> and the dialogs
 */
public final class Writer2LaTeX extends WeakBase
    implements com.sun.star.lang.XServiceInfo,
    com.sun.star.frame.XDispatchProvider,
    com.sun.star.lang.XInitialization,
    com.sun.star.frame.XDispatch {
	
    private static final String PROTOCOL = "org.openoffice.da.writer2latex:"; //$NON-NLS-1$
    
    // From constructor+initialization
    private final XComponentContext m_xContext;
    private XFrame m_xFrame;
    private LaTeXUNOPublisher unoPublisher = null;
	
    public static final String __implementationName = Writer2LaTeX.class.getName();
    public static final String __serviceName = "com.sun.star.frame.ProtocolHandler";  //$NON-NLS-1$
    private static final String[] m_serviceNames = { __serviceName };
      
    public Writer2LaTeX( XComponentContext xContext ) {
        m_xContext = xContext;
    }
	
    // com.sun.star.lang.XInitialization:
    public void initialize( Object[] object )
        throws com.sun.star.uno.Exception {
        if ( object.length > 0 ) {
            // The first item is the current frame
            m_xFrame = (com.sun.star.frame.XFrame) UnoRuntime.queryInterface(
            com.sun.star.frame.XFrame.class, object[0]);
        }
    }
	
    // com.sun.star.lang.XServiceInfo:
    public String getImplementationName() {
        return __implementationName;
    }

    public boolean supportsService( String sService ) {
        int len = m_serviceNames.length;

        for( int i=0; i < len; i++) {
            if (sService.equals(m_serviceNames[i]))
                return true;
        }
        return false;
    }

    public String[] getSupportedServiceNames() {
        return m_serviceNames;
    }

	
    // com.sun.star.frame.XDispatchProvider:
    public com.sun.star.frame.XDispatch queryDispatch( com.sun.star.util.URL aURL,
        String sTargetFrameName, int iSearchFlags ) {
        if ( aURL.Protocol.compareTo(PROTOCOL) == 0 ) {
            if ( aURL.Path.compareTo("ProcessDocument") == 0 ) //$NON-NLS-1$
                return this;
            else if ( aURL.Path.compareTo("ViewLog") == 0 ) //$NON-NLS-1$
                return this;
            else if ( aURL.Path.compareTo("InsertBibTeX") == 0 ) //$NON-NLS-1$
                return this;
        }
        return null;
    }

    public com.sun.star.frame.XDispatch[] queryDispatches(
    com.sun.star.frame.DispatchDescriptor[] seqDescriptors ) {
        int nCount = seqDescriptors.length;
        com.sun.star.frame.XDispatch[] seqDispatcher =
        new com.sun.star.frame.XDispatch[seqDescriptors.length];

        for( int i=0; i < nCount; ++i ) {
            seqDispatcher[i] = queryDispatch(seqDescriptors[i].FeatureURL,
            seqDescriptors[i].FrameName,
            seqDescriptors[i].SearchFlags );
        }
        return seqDispatcher;
    }


    // com.sun.star.frame.XDispatch:
    public void dispatch( com.sun.star.util.URL aURL,
        com.sun.star.beans.PropertyValue[] aArguments ) {
        if ( aURL.Protocol.compareTo(PROTOCOL) == 0 ) {
            if ( aURL.Path.compareTo("ProcessDocument") == 0 ) { //$NON-NLS-1$
               	process();
                return;
            }
            else if ( aURL.Path.compareTo("ViewLog") == 0 ) { //$NON-NLS-1$
                viewLog();
                return;
            }
            else if ( aURL.Path.compareTo("InsertBibTeX") == 0 ) { //$NON-NLS-1$
                insertBibTeX();
                return;
            }
        }
    }

    public void addStatusListener( com.sun.star.frame.XStatusListener xControl,
    com.sun.star.util.URL aURL ) {
    }

    public void removeStatusListener( com.sun.star.frame.XStatusListener xControl,
    com.sun.star.util.URL aURL ) {
    }
	
    // The actual commands...
	
    private void process() {
    	createUNOPublisher();
    	unoPublisher.publish(TargetFormat.latex);
    }
    
	private void viewLog() {
		createUNOPublisher();
    	if (unoPublisher.documentSaved()) {
            // Execute the log viewer dialog
            try {
                Object[] args = new Object[1];
                args[0] = unoPublisher.getTargetPath()+unoPublisher.getTargetFileName();
                Object dialog = m_xContext.getServiceManager()
                    .createInstanceWithArgumentsAndContext(
                    "org.openoffice.da.writer2latex.LogViewerDialog", args, m_xContext); //$NON-NLS-1$
                XExecutableDialog xDialog = (XExecutableDialog)
                    UnoRuntime.queryInterface(XExecutableDialog.class, dialog);
                if (xDialog.execute()==ExecutableDialogResults.OK) {
                    // Closed with the close button
                }
            }
            catch (com.sun.star.uno.Exception e) {
            }
        }
    }
	
	private void insertBibTeX() {
		if (useExternalBibTeXFiles()) {
			createUNOPublisher();
	    	if (unoPublisher.documentSaved()) {
				// Execute the BibTeX dialog
		        try {
		        	// The dialog needs the current frame and the path to the BibTeX directory
		            Object[] args = new Object[2];
		            args[0] = m_xFrame;
		            args[1] = unoPublisher.getBibTeXDirectory().getPath();
		            Object dialog = m_xContext.getServiceManager()
		                    .createInstanceWithArgumentsAndContext(
		                    "org.openoffice.da.writer2latex.BibTeXDialog", args, m_xContext); //$NON-NLS-1$
		            XExecutableDialog xDialog = (XExecutableDialog)
		                UnoRuntime.queryInterface(XExecutableDialog.class, dialog);
		            if (xDialog.execute()==ExecutableDialogResults.OK) {
		                // Closed with the close button
		            }
		        }
		        catch (com.sun.star.uno.Exception e) {
		        }
		    }
		}
		else {
            MessageBox msgBox = new MessageBox(m_xContext, m_xFrame);
            msgBox.showMessage("Writer2LaTeX",Messages.getString("Writer2LaTeX.bibtexnotenabled"));			 //$NON-NLS-1$
		}
	}
	
    private boolean useExternalBibTeXFiles() {
        // Get the BibTeX settings from the registry
    	RegistryHelper registry = new RegistryHelper(m_xContext);
		Object view;
		try {
			view = registry.getRegistryView(BibliographyDialog.REGISTRY_PATH, false);
		} catch (Exception e) {
			// Failed to get registry settings
			return false;
		}
		XPropertySet xProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class,view);
		return XPropertySetHelper.getPropertyValueAsBoolean(xProps, "UseExternalBibTeXFiles"); //$NON-NLS-1$
    }
	
	private void createUNOPublisher() {
    	if (unoPublisher==null) { 
    		unoPublisher = new LaTeXUNOPublisher(m_xContext,m_xFrame,"Writer2LaTeX"); //$NON-NLS-1$
    	}		
	}
    	
}