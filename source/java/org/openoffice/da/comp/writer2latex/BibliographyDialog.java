/************************************************************************
 *
 *  BibliographyDialog.java
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
 *  Version 1.6 (2015-07-23)
 *
 */ 
 
package org.openoffice.da.comp.writer2latex;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.sun.star.awt.XContainerWindowEventHandler;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XWindow;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XChangesBatch;

import com.sun.star.lib.uno.helper.WeakBase;

import org.openoffice.da.comp.w2lcommon.helper.DialogAccess;
import org.openoffice.da.comp.w2lcommon.helper.FolderPicker;
import org.openoffice.da.comp.w2lcommon.helper.MessageBox;
import org.openoffice.da.comp.w2lcommon.helper.RegistryHelper;
import org.openoffice.da.comp.w2lcommon.helper.XPropertySetHelper;

/** This class provides a uno component which implements the configuration
 *  of the bibliography for the Writer2LaTeX toolbar
 */
public final class BibliographyDialog
    extends WeakBase
    implements XServiceInfo, XContainerWindowEventHandler {
	
	public static final String REGISTRY_PATH = "/org.openoffice.da.Writer2LaTeX.toolbar.ToolbarOptions/BibliographyOptions"; //$NON-NLS-1$

    private XComponentContext xContext;
    private FolderPicker folderPicker;
    
    /** The component will be registered under this name.
     */
    public static String __serviceName = "org.openoffice.da.writer2latex.BibliographyDialog"; //$NON-NLS-1$

    /** The component should also have an implementation name.
     */
    public static String __implementationName = "org.openoffice.da.comp.writer2latex.BibliographyDialog"; //$NON-NLS-1$

    /** Create a new ConfigurationDialog */
    public BibliographyDialog(XComponentContext xContext) {
        this.xContext = xContext;
        folderPicker = new FolderPicker(xContext);
    }

    
    // Implement XContainerWindowEventHandler
    public boolean callHandlerMethod(XWindow xWindow, Object event, String sMethod)
        throws com.sun.star.lang.WrappedTargetException {
		XDialog xDialog = (XDialog)UnoRuntime.queryInterface(XDialog.class, xWindow);
		DialogAccess dlg = new DialogAccess(xDialog);

        try {
            if (sMethod.equals("external_event") ){ //$NON-NLS-1$
                return handleExternalEvent(dlg, event);
            }
            else if (sMethod.equals("ConvertZoteroCitationsChange")) { //$NON-NLS-1$
                return convertZoteroCitationsChange(dlg);
            }
            else if (sMethod.equals("ConvertJabRefCitationsChange")) { //$NON-NLS-1$
                return convertJabRefCitationsChange(dlg);
            }
            else if (sMethod.equals("UseExternalBibTeXFilesChange")) { //$NON-NLS-1$
                return useExternalBibTeXFilesChange(dlg);
            }
            else if (sMethod.equals("UseNatbibChange")) { //$NON-NLS-1$
                return useNatbibChange(dlg);
            }
            else if (sMethod.equals("BibTeXLocationChange")) { //$NON-NLS-1$
                return bibTeXLocationChange(dlg);
            }
            else if (sMethod.equals("BibTeXDirClick")) { //$NON-NLS-1$
                return bibTeXDirClick(dlg);
            }
        }
        catch (com.sun.star.uno.RuntimeException e) {
            throw e;
        }
        catch (com.sun.star.uno.Exception e) {
            throw new com.sun.star.lang.WrappedTargetException(sMethod, this, e);
        }
        return false;
    }
	
	public String[] getSupportedMethodNames() {
        String[] sNames = { "external_event", "UseExternalBibTeXFilesChange", "ConvertZoteroCitationsChange", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        		"ConvertJabRefCitationsChange", "UseNatbibChange", "BibTeXLocationChange", "ExternalBibTeXDirClick" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        return sNames;
    }
    
    // Implement the interface XServiceInfo
    public boolean supportsService(String sServiceName) {
        return sServiceName.equals(__serviceName);
    }

    public String getImplementationName() {
        return __implementationName;
    }
    
    public String[] getSupportedServiceNames() {
        String[] sSupportedServiceNames = { __serviceName };
        return sSupportedServiceNames;
    }
	
    // Private stuff
    
    private boolean handleExternalEvent(DialogAccess dlg, Object aEventObject)
        throws com.sun.star.uno.Exception {
        try {
            String sMethod = AnyConverter.toString(aEventObject);
            if (sMethod.equals("ok")) { //$NON-NLS-1$
                saveConfiguration(dlg);
                return true;
            } else if (sMethod.equals("back") || sMethod.equals("initialize")) { //$NON-NLS-1$ //$NON-NLS-2$
                loadConfiguration(dlg);
                enableBibTeXSettings(dlg);
                useNatbibChange(dlg);
                return true;
            }
        }
        catch (com.sun.star.lang.IllegalArgumentException e) {
            throw new com.sun.star.lang.IllegalArgumentException(
            "Method external_event requires a string in the event object argument.", this,(short) -1); //$NON-NLS-1$
        }
        return false;
    }
    
    // Load settings from the registry into the dialog
    private void loadConfiguration(DialogAccess dlg) {
    	RegistryHelper registry = new RegistryHelper(xContext);
    	try {
    		Object view = registry.getRegistryView(REGISTRY_PATH, false);
    		XPropertySet xProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class,view);
    		dlg.setCheckBoxStateAsBoolean("UseExternalBibTeXFiles", //$NON-NLS-1$
    				XPropertySetHelper.getPropertyValueAsBoolean(xProps, "UseExternalBibTeXFiles")); //$NON-NLS-1$
    		dlg.setCheckBoxStateAsBoolean("ConvertZoteroCitations", //$NON-NLS-1$
    				XPropertySetHelper.getPropertyValueAsBoolean(xProps, "ConvertZoteroCitations")); //$NON-NLS-1$
    		dlg.setCheckBoxStateAsBoolean("ConvertJabRefCitations", //$NON-NLS-1$
    				XPropertySetHelper.getPropertyValueAsBoolean(xProps, "ConvertJabRefCitations")); //$NON-NLS-1$
    		dlg.setCheckBoxStateAsBoolean("IncludeOriginalCitations", //$NON-NLS-1$
    				XPropertySetHelper.getPropertyValueAsBoolean(xProps, "IncludeOriginalCitations")); //$NON-NLS-1$
        	dlg.setListBoxSelectedItem("BibTeXLocation", //$NON-NLS-1$
        			XPropertySetHelper.getPropertyValueAsShort(xProps, "BibTeXLocation")); //$NON-NLS-1$
        	dlg.setTextFieldText("BibTeXDir", //$NON-NLS-1$
        			XPropertySetHelper.getPropertyValueAsString(xProps, "BibTeXDir")); //$NON-NLS-1$
        	dlg.setListBoxSelectedItem("BibTeXEncoding", //$NON-NLS-1$
        			XPropertySetHelper.getPropertyValueAsShort(xProps, "BibTeXEncoding")); //$NON-NLS-1$
    		dlg.setCheckBoxStateAsBoolean("UseNatbib", //$NON-NLS-1$
    				XPropertySetHelper.getPropertyValueAsBoolean(xProps, "UseNatbib")); //$NON-NLS-1$
    		dlg.setTextFieldText("NatbibOptions", //$NON-NLS-1$
        			XPropertySetHelper.getPropertyValueAsString(xProps, "NatbibOptions")); //$NON-NLS-1$
        	registry.disposeRegistryView(view);
    	}
    	catch (Exception e) {
    		// Failed to get registry view
    	}
    	
    	// Update dialog according to the settings
    	convertZoteroCitationsChange(dlg);
    	useExternalBibTeXFilesChange(dlg);
	}

    // Save settings from the dialog to the registry
	private void saveConfiguration(DialogAccess dlg) {
		RegistryHelper registry = new RegistryHelper(xContext);
    	try {
    		Object view = registry.getRegistryView(REGISTRY_PATH, true);
    		XPropertySet xProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class,view);
			XPropertySetHelper.setPropertyValue(xProps, "UseExternalBibTeXFiles", dlg.getCheckBoxStateAsBoolean("UseExternalBibTeXFiles")); //$NON-NLS-1$ //$NON-NLS-2$
    		XPropertySetHelper.setPropertyValue(xProps, "ConvertZoteroCitations", dlg.getCheckBoxStateAsBoolean("ConvertZoteroCitations")); //$NON-NLS-1$ //$NON-NLS-2$
    		XPropertySetHelper.setPropertyValue(xProps, "ConvertJabRefCitations", dlg.getCheckBoxStateAsBoolean("ConvertJabRefCitations")); //$NON-NLS-1$ //$NON-NLS-2$
    		XPropertySetHelper.setPropertyValue(xProps, "IncludeOriginalCitations", dlg.getCheckBoxStateAsBoolean("IncludeOriginalCitations")); //$NON-NLS-1$ //$NON-NLS-2$
   			XPropertySetHelper.setPropertyValue(xProps, "BibTeXLocation", dlg.getListBoxSelectedItem("BibTeXLocation")); //$NON-NLS-1$ //$NON-NLS-2$
   			XPropertySetHelper.setPropertyValue(xProps, "BibTeXDir", dlg.getTextFieldText("BibTeXDir")); //$NON-NLS-1$ //$NON-NLS-2$
   			XPropertySetHelper.setPropertyValue(xProps, "BibTeXEncoding", dlg.getListBoxSelectedItem("BibTeXEncoding")); //$NON-NLS-1$ //$NON-NLS-2$
    		XPropertySetHelper.setPropertyValue(xProps, "UseNatbib", dlg.getCheckBoxStateAsBoolean("UseNatbib")); //$NON-NLS-1$ //$NON-NLS-2$
   			XPropertySetHelper.setPropertyValue(xProps, "NatbibOptions", dlg.getTextFieldText("NatbibOptions")); //$NON-NLS-1$ //$NON-NLS-2$
   			
            // Commit registry changes
            XChangesBatch  xUpdateContext = (XChangesBatch)
                UnoRuntime.queryInterface(XChangesBatch.class,view);
            try {
                xUpdateContext.commitChanges();
            }
            catch (Exception e) {
                // ignore
            }
                        
        	registry.disposeRegistryView(view);
    	}
    	catch (Exception e) {
    		// Failed to get registry view
    	}		
	}

	private boolean useExternalBibTeXFilesChange(DialogAccess dlg) {
		enableBibTeXSettings(dlg);
		return true;
	}

	private boolean convertZoteroCitationsChange(DialogAccess dlg) {
		enableBibTeXSettings(dlg);
		return true;
	}

	private boolean convertJabRefCitationsChange(DialogAccess dlg) {
		enableBibTeXSettings(dlg);
		return true;
	}
	
	private boolean useNatbibChange(DialogAccess dlg) {
		boolean bUseNatbib = dlg.getCheckBoxStateAsBoolean("UseNatbib"); //$NON-NLS-1$
		dlg.setControlEnabled("NatbibOptionsLabel", bUseNatbib); //$NON-NLS-1$
		dlg.setControlEnabled("NatbibOptions", bUseNatbib); //$NON-NLS-1$
		return true;
	}
		
	private boolean bibTeXLocationChange(DialogAccess dlg) {
		enableBibTeXSettings(dlg);
		return true;
	}

	private void enableBibTeXSettings(DialogAccess dlg) {
		boolean bEnableSettings = dlg.getCheckBoxStateAsBoolean("UseExternalBibTeXFiles"); //$NON-NLS-1$
		boolean bEnableOriginalCitations = dlg.getCheckBoxStateAsBoolean("ConvertZoteroCitations") //$NON-NLS-1$
			|| dlg.getCheckBoxStateAsBoolean("ConvertJabRefCitations"); //$NON-NLS-1$
		boolean bEnableDir = dlg.getListBoxSelectedItem("BibTeXLocation")<2; //$NON-NLS-1$
		dlg.setControlEnabled("BibTeXLocationLabel", bEnableSettings); //$NON-NLS-1$
		dlg.setControlEnabled("BibTeXLocation", bEnableSettings); //$NON-NLS-1$
		dlg.setControlEnabled("BibTeXDirLabel", bEnableSettings && bEnableDir); //$NON-NLS-1$
		dlg.setControlEnabled("BibTeXDir",  bEnableSettings && bEnableDir); //$NON-NLS-1$
		dlg.setControlEnabled("BibTeXDirButton",  bEnableSettings && bEnableDir); //$NON-NLS-1$
		dlg.setControlEnabled("BibTeXEncodingLabel",  bEnableSettings); //$NON-NLS-1$
		dlg.setControlEnabled("BibTeXEncoding",  bEnableSettings); //$NON-NLS-1$
		dlg.setControlEnabled("ConvertZoteroCitations", bEnableSettings); //$NON-NLS-1$
		dlg.setControlEnabled("ConvertJabRefCitations", bEnableSettings); //$NON-NLS-1$
		dlg.setControlEnabled("IncludeOriginalCitations", bEnableSettings && bEnableOriginalCitations); //$NON-NLS-1$
	}
	
	private String getDocumentDirURL() {
		// Get the desktop from the service manager
		Object desktop=null;
		try {
			desktop = xContext.getServiceManager().createInstanceWithContext("com.sun.star.frame.Desktop", xContext); //$NON-NLS-1$
		} catch (Exception e) {
			// Failed to get the desktop service
			return ""; //$NON-NLS-1$
		}
		XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
		
		// Get the current component and verify that it really is a text document		
		if (xDesktop!=null) {
			XComponent xComponent = xDesktop.getCurrentComponent();
			XServiceInfo xInfo = (XServiceInfo)UnoRuntime.queryInterface(XServiceInfo.class, xComponent);
			if (xInfo!=null && xInfo.supportsService("com.sun.star.text.TextDocument")) { //$NON-NLS-1$
				// Get the model, which provides the URL
				XModel xModel = (XModel) UnoRuntime.queryInterface(XModel.class, xComponent);
				if (xModel!=null) {
					String sURL = xModel.getURL();
					int nSlash = sURL.lastIndexOf('/');
					return nSlash>-1 ? sURL.substring(0, nSlash) : ""; //$NON-NLS-1$
				}
			}
		}
		
		return ""; //$NON-NLS-1$
	}
	
	private boolean hasBibTeXFiles(File dir) {
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File file : files) {
				if (file.isFile() && file.getName().endsWith(".bib")) { //$NON-NLS-1$
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean bibTeXDirClick(DialogAccess dlg) {
		String sPath = folderPicker.getPath();
    	if (sPath!=null) {
    		try {
    			File bibDir = new File(new URI(sPath));
    			String sBibPath = bibDir.getCanonicalPath();
    			if (dlg.getListBoxSelectedItem("BibTeXLocation")==1) { //$NON-NLS-1$
    				// Path relative to document directory, remove the document directory part
    				String sDocumentDirURL = getDocumentDirURL();
    				if (sDocumentDirURL.length()>0) {
    					String sDocumentDirPath = new File(new URI(sDocumentDirURL)).getCanonicalPath();
    					if (sBibPath.startsWith(sDocumentDirPath)) {
    						if (sBibPath.length()>sDocumentDirPath.length()) {
    							sBibPath = sBibPath.substring(sDocumentDirPath.length()+1);
    						}
    						else { // Same as document directory
    							sBibPath = ""; //$NON-NLS-1$
    						}
    					}
    					else { // not a subdirectory
    						sBibPath = ""; //$NON-NLS-1$
    					}
    				}
    			}
    			dlg.setTextFieldText("BibTeXDir", sBibPath); //$NON-NLS-1$
    			if (!hasBibTeXFiles(bibDir)) {
    				MessageBox msgBox = new MessageBox(xContext);
    				msgBox.showMessage("Writer2LaTeX", Messages.getString("BibliographyDialog.nobibtexfiles"));    				 //$NON-NLS-1$ //$NON-NLS-2$
    			}
			}
    		catch (IOException e) {
			}
    		catch (URISyntaxException e) {
			}
    	}     
		return true;
	}
	
}
