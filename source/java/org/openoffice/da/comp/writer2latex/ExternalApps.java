/************************************************************************
 *
 *  ExternalApps.java
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
 *  Version 1.6 (2018-03-06)
 *
 */ 
 
package org.openoffice.da.comp.writer2latex;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.lang.Process;
import java.lang.ProcessBuilder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.openoffice.da.comp.w2lcommon.helper.RegistryHelper;
import org.openoffice.da.comp.w2lcommon.helper.StreamGobbler;
import org.openoffice.da.comp.w2lcommon.helper.XPropertySetHelper;

import com.sun.star.beans.XMultiHierarchicalPropertySet;
import com.sun.star.beans.XPropertySet;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XChangesBatch;

       
/** This class manages and executes external applications used by the Writer2LaTeX toolbar.
 *  These include TeX and friends as well as viewers for the various backend formats.
 *  The registry is used for persistent storage of the settings.
 */  
public class ExternalApps {
	
	public final static short EXPORT = (short)0;
	public final static short BUILD = (short)1;
	public final static short PREVIEW = (short)2;
	
    public final static String LATEX = "LaTeX"; //$NON-NLS-1$
    public final static String PDFLATEX = "PdfLaTeX"; //$NON-NLS-1$
    public final static String XELATEX = "XeLaTeX"; //$NON-NLS-1$
    public final static String BIBTEX = "BibTeX"; //$NON-NLS-1$
    public final static String MAKEINDEX = "Makeindex"; //$NON-NLS-1$
    public final static String MK4HT = "Mk4ht"; //$NON-NLS-1$
    public final static String DVIPS = "Dvips"; //$NON-NLS-1$
    public final static String DVIVIEWER = "DVIViewer"; //$NON-NLS-1$
    public final static String POSTSCRIPTVIEWER = "PostscriptViewer"; //$NON-NLS-1$
    public final static String PDFVIEWER = "PdfViewer"; //$NON-NLS-1$
	
    private final static String[] sApps = { LATEX, PDFLATEX, XELATEX, BIBTEX, MAKEINDEX, MK4HT, DVIPS, DVIVIEWER, POSTSCRIPTVIEWER, PDFVIEWER };
	
    private XComponentContext xContext;

    private short nLevel = (short)2;
    
    private Map<String,String[]> apps;
    private Set<String> defaultApps;
	
    /** Construct a new ExternalApps object with empty content */
    public ExternalApps(XComponentContext xContext) {
        this.xContext = xContext;
        apps = new HashMap<String,String[]>();
        defaultApps = new HashSet<String>();
        for (int i=0; i<sApps.length; i++) {
            setApplication(sApps[i], "", ""); //$NON-NLS-1$ //$NON-NLS-2$
           	setUseDefaultApplication(sApps[i],true);
        }
    }
    
    /** Return the localized name for an external app to use in the UI (only the viewers has a separate UI name)
     * 
     * @param sName the app name
     * @return the UI name
     */
    public static String getUIAppName(String sName) {
    	if (DVIVIEWER.equals(sName)) {
    		return Messages.getString("ExternalApps.dviviewer"); //$NON-NLS-1$
    	}
    	else if (PDFVIEWER.equals(sName)) {
    		return Messages.getString("ExternalApps.pdfviewer"); //$NON-NLS-1$
    	}
    	else if (POSTSCRIPTVIEWER.equals(sName)) {
    		return Messages.getString("ExternalApps.psviewer"); //$NON-NLS-1$
    	}
    	return sName;
    }
    
    /** Set the desired processing level (0: export only, 1: export and build, 2: export, build and preview)
     * 
     * @param nLevel the desired level
     */
    public void setProcessingLevel(short nLevel) {
    	this.nLevel = nLevel;
    }
    
    /** Get the desired processing level (0: export only, 1: export and build, 2: export, build and preview)
     * 
     * @return the level
     */
    public short getProcessingLevel() {
    	return nLevel;
    }
    
    public boolean isViewer(String sAppName) {
    	return sAppName!=null && sAppName.endsWith("Viewer"); //$NON-NLS-1$
    }
	
    /** Define an external application
     *  @param sAppName the name of the application to define
     *  @param sExecutable the system dependent path to the executable file
     *  @param sOptions the options to the external application; %s will be
     *  replaced by the filename on execution 
     */
    public void setApplication(String sAppName, String sExecutable, String sOptions) {
        String[] sValue = { sExecutable, sOptions };
        apps.put(sAppName, sValue);
    }
    
    /** Get the definition for an external application
     *  @param sAppName the name of the application to get
     *  @return a String array containing the system dependent path to the
     *  executable file as entry 0 and the parameters as entry 1
     *  returns null if the application is unknown
     */
    public String[] getApplication(String sAppName) {
        return apps.get(sAppName);
    } 
    
    /** Define to use the system's default for an external application. This is only possible if the application is a viewer,
     *  otherwise setting the value to true will be ignored
     *  @param sAppName the name of the application
     *  @param bUseDefault flag defining whether or not to use the default
     */
    public void setUseDefaultApplication(String sAppName, boolean bUseDefault) {
    	if (bUseDefault && isViewer(sAppName)) {
    		defaultApps.add(sAppName);
    	}
    	else if (defaultApps.contains(sAppName)) {
    		defaultApps.remove(sAppName);
    	}
    }
	
    /** Get the setting to use the system's default application
     * 
     * @param sAppName the name of the application
     * @return true if the system's default should be used, false if not or if the application is unknown
     */
    public boolean getUseDefaultApplication(String sAppName) {
    	return defaultApps.contains(sAppName);
    }
    
    /** Execute an external application
     *  @param sAppName the name of the application to execute (ignored for default apps)
     *  @param sFileName the file name to use
     *  @param workDir the working directory to use
     *  @param env map of environment variables to set (or null if no variables needs to be set, ignored for default apps)
     *  @param bWaitFor true if the method should wait for the execution to finish (ignored for default apps)
     *  @return error code 
     */
    public int execute(String sAppName, String sFileName, File workDir, Map<String,String> env, boolean bWaitFor) {
    	if (defaultApps.contains(sAppName)) {
    		return openWithDefaultApplication(new File(sFileName)) ? 0 : 1;
    	}
    	else {
    		return execute(sAppName, "", sFileName, workDir, env, bWaitFor); //$NON-NLS-1$
    	}
    }
    
    // Open the file in the default application on this system (if any)
    private boolean openWithDefaultApplication(File file) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
				desktop.open(file);
				return true;
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
        }
        return false;
    }

	
    /** Execute an external application
     *  @param sAppName the name of the application to execute
     *  @param sCommand subcommand/option to pass to the command
     *  @param sFileName the file name to use
     *  @param workDir the working directory to use
     *  @param env map of environment variables to set (or null if no variables needs to be set)
     *  @param bWaitFor true if the method should wait for the execution to finish
     *  @return error code 
     */
    public int execute(String sAppName, String sCommand, String sFileName, File workDir, Map<String,String> env, boolean bWaitFor) {
        // Assemble the command
        String[] sApp = getApplication(sAppName);
        if (sApp==null) { return 1; }
 
        try {
			Vector<String> command = new Vector<String>();
			command.add(sApp[0]);
			String[] sArguments = sApp[1].split(" "); //$NON-NLS-1$
			for (String s : sArguments) {
				command.add(s.replace("%c",sCommand).replace("%s",sFileName)); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(workDir);
            if (env!=null) {
            	pb.environment().putAll(env);
            }
            Process proc = pb.start();        
        
            // Gobble the error stream of the application
            StreamGobbler errorGobbler = new 
                StreamGobbler(proc.getErrorStream(), "ERROR");             //$NON-NLS-1$
            
            // Gobble the output stream of the application
            StreamGobbler outputGobbler = new 
                StreamGobbler(proc.getInputStream(), "OUTPUT"); //$NON-NLS-1$
                
            // Kick them off
            errorGobbler.start();
            outputGobbler.start();
                                    
            // Any error?
            return bWaitFor ? proc.waitFor() : 0;
        }
        catch (InterruptedException e) {
            return 1;
        }
        catch (IOException e) {
            return 1;
        }
    }
	
    /** Load the external applications from the registry
     */
    public void load() {
    	RegistryHelper registry = new RegistryHelper(xContext);
    	Object view;
    	try {
    		// Prepare registry view
    		view = registry.getRegistryView("/org.openoffice.da.Writer2LaTeX.toolbar.ToolbarOptions/Applications",false); //$NON-NLS-1$
    	}
        catch (com.sun.star.uno.Exception e) {
            // Give up...
            return;
        }

		XPropertySet xSimpleProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class,view);
		nLevel = XPropertySetHelper.getPropertyValueAsShort(xSimpleProps,"AfterExport"); //$NON-NLS-1$

		XMultiHierarchicalPropertySet xProps = (XMultiHierarchicalPropertySet)
            UnoRuntime.queryInterface(XMultiHierarchicalPropertySet.class, view);
        for (int i=0; i<sApps.length; i++) {
            String[] sNames = new String[3];
            sNames[0] = sApps[i]+"/Executable"; //$NON-NLS-1$
            sNames[1] = sApps[i]+"/Options"; //$NON-NLS-1$
            sNames[2] = sApps[i]+"/UseDefault"; //$NON-NLS-1$
            try {
                Object[] values = xProps.getHierarchicalPropertyValues(sNames);
                setApplication(sApps[i], (String) values[0], (String) values[1]);
                setUseDefaultApplication(sApps[i], ((Boolean) values[2]).booleanValue());
            }
            catch (com.sun.star.uno.Exception e) {
                // Ignore...
            }
        }
		
        registry.disposeRegistryView(view);
    }
	
    /** Save the external applications to the registry
     */
    public void save() {
    	RegistryHelper registry = new RegistryHelper(xContext);
        Object view;
        try {
    		view = registry.getRegistryView("/org.openoffice.da.Writer2LaTeX.toolbar.ToolbarOptions/Applications",true); //$NON-NLS-1$
        }
        catch (com.sun.star.uno.Exception e) {
            // Give up...
            return;
        }
        
		XPropertySet xSimpleProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class,view);
		XPropertySetHelper.setPropertyValue(xSimpleProps, "AfterExport", nLevel); //$NON-NLS-1$

        XMultiHierarchicalPropertySet xProps = (XMultiHierarchicalPropertySet)
            UnoRuntime.queryInterface(XMultiHierarchicalPropertySet.class, view);
        for (int i=0; i<sApps.length; i++) {
            String[] sNames = new String[3];
            sNames[0] = sApps[i]+"/Executable"; //$NON-NLS-1$
            sNames[1] = sApps[i]+"/Options"; //$NON-NLS-1$
            sNames[2] = sApps[i]+"/UseDefault"; //$NON-NLS-1$
            String[] sApp = getApplication(sApps[i]);
            boolean bUseDefault = getUseDefaultApplication(sApps[i]);
            Object[] values = { sApp[0], sApp[1], new Boolean(bUseDefault) }; 
            try {
                xProps.setHierarchicalPropertyValues(sNames, values);
            }
            catch (com.sun.star.uno.Exception e) {
                // Ignore...
            }
        }
		
        // Commit registry changes
        XChangesBatch  xUpdateContext = (XChangesBatch)
            UnoRuntime.queryInterface(XChangesBatch.class, view);
        try {
            xUpdateContext.commitChanges();
        }
        catch (Exception e) {
            // ignore
        }

        registry.disposeRegistryView(view);
    }
	
}