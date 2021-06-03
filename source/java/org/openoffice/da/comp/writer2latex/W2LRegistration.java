/************************************************************************
 *
 *  W2LRegistration.java
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
 *  Version 1.6 (2014-12-11) 
 *
 */ 
 
package org.openoffice.da.comp.writer2latex;

import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XSingleServiceFactory;
import com.sun.star.registry.XRegistryKey;

import com.sun.star.comp.loader.FactoryHelper;

/** This class provides a static method to instantiate our uno components
 * on demand (__getServiceFactory()), and a static method to give
 * information about the components (__writeRegistryServiceInfo()).
 * Furthermore, it saves the XMultiServiceFactory provided to the
 * __getServiceFactory method for future reference by the componentes.
 */
public class W2LRegistration {
    
    public static XMultiServiceFactory xMultiServiceFactory;

    /**
     * Returns a factory for creating the service.
     * This method is called by the <code>JavaLoader</code>
     *
     * @return  returns a <code>XSingleServiceFactory</code> for creating the
     *          component
     *
     * @param   implName     the name of the implementation for which a
     *                       service is desired
     * @param   multiFactory the service manager to be used if needed
     * @param   regKey       the registryKey
     *
     * @see                  com.sun.star.comp.loader.JavaLoader
     */
    public static XSingleServiceFactory __getServiceFactory(String implName,
        XMultiServiceFactory multiFactory, XRegistryKey regKey) {
        xMultiServiceFactory = multiFactory;
        XSingleServiceFactory xSingleServiceFactory = null;
        if (implName.equals(W2LExportFilter.class.getName()) ) {
            xSingleServiceFactory = FactoryHelper.getServiceFactory(W2LExportFilter.class,
            W2LExportFilter.__serviceName,
            multiFactory,						    
            regKey);
        }
        else if (implName.equals(LaTeXOptionsDialog.__implementationName)) {
            xSingleServiceFactory = FactoryHelper.getServiceFactory(LaTeXOptionsDialog.class,
            LaTeXOptionsDialog.__serviceName,
            multiFactory,						    
            regKey);
        }
        else if (implName.equals(W2LStarMathConverter.__implementationName)) {
            xSingleServiceFactory = FactoryHelper.getServiceFactory(W2LStarMathConverter.class,
            W2LStarMathConverter.__serviceName,
            multiFactory,						    
            regKey);
        }
        else if (implName.equals(ConfigurationDialog.__implementationName)) {
            xSingleServiceFactory = FactoryHelper.getServiceFactory(ConfigurationDialog.class,
            ConfigurationDialog.__serviceName,
            multiFactory,						    
            regKey);
        }
        else if (implName.equals(Writer2LaTeX.__implementationName) ) {
            xSingleServiceFactory = FactoryHelper.getServiceFactory(Writer2LaTeX.class,
            Writer2LaTeX.__serviceName,
            multiFactory,						    
            regKey);
        }
        else if (implName.equals(TeXImportFilter.__implementationName) ) {
            xSingleServiceFactory = FactoryHelper.getServiceFactory(TeXImportFilter.class,
            TeXImportFilter.__serviceName,
            multiFactory,						    
            regKey);
        }
        else if (implName.equals(TeXDetectService.__implementationName) ) {
            xSingleServiceFactory = FactoryHelper.getServiceFactory(TeXDetectService.class,
            TeXDetectService.__serviceName,
            multiFactory,						    
            regKey);
        }
        else if (implName.equals(ApplicationsDialog.__implementationName) ) {
            xSingleServiceFactory = FactoryHelper.getServiceFactory(ApplicationsDialog.class,
            ApplicationsDialog.__serviceName,
            multiFactory,						    
            regKey);
        }
        else if (implName.equals(BibliographyDialog.__implementationName) ) {
            xSingleServiceFactory = FactoryHelper.getServiceFactory(BibliographyDialog.class,
            BibliographyDialog.__serviceName,
            multiFactory,						    
            regKey);
        }
        else if (implName.equals(LogViewerDialog.__implementationName) ) {
            xSingleServiceFactory = FactoryHelper.getServiceFactory(LogViewerDialog.class,
            LogViewerDialog.__serviceName,
            multiFactory,						    
            regKey);
        }
        else if (implName.equals(BibTeXDialog.__implementationName) ) {
            xSingleServiceFactory = FactoryHelper.getServiceFactory(BibTeXDialog.class,
            BibTeXDialog.__serviceName,
            multiFactory,						    
            regKey);
        }
        
        return xSingleServiceFactory;
    }
    
    /**
     * Writes the service information into the given registry key.
     * This method is called by the <code>JavaLoader</code>
     * <p>
     * @return  returns true if the operation succeeded
     * @param   regKey       the registryKey
     * @see                  com.sun.star.comp.loader.JavaLoader
     */
    public static boolean __writeRegistryServiceInfo(XRegistryKey regKey) {
        return
            FactoryHelper.writeRegistryServiceInfo(W2LExportFilter.__implementationName,
                W2LExportFilter.__serviceName, regKey) &
            FactoryHelper.writeRegistryServiceInfo(LaTeXOptionsDialog.__implementationName,
                LaTeXOptionsDialog.__serviceName, regKey) &
            FactoryHelper.writeRegistryServiceInfo(W2LStarMathConverter.__implementationName,
                W2LStarMathConverter.__serviceName, regKey) &
        	FactoryHelper.writeRegistryServiceInfo(ConfigurationDialog.__implementationName,
                ConfigurationDialog.__serviceName, regKey) &
            FactoryHelper.writeRegistryServiceInfo(Writer2LaTeX.__implementationName,
                Writer2LaTeX.__serviceName, regKey) &
            FactoryHelper.writeRegistryServiceInfo(TeXImportFilter.__implementationName,
                TeXImportFilter.__serviceName, regKey) &
            FactoryHelper.writeRegistryServiceInfo(TeXDetectService.__implementationName,
                TeXDetectService.__serviceName, regKey) &
            FactoryHelper.writeRegistryServiceInfo(ApplicationsDialog.__implementationName,
            	ApplicationsDialog.__serviceName, regKey) &
            FactoryHelper.writeRegistryServiceInfo(BibliographyDialog.__implementationName,
                BibliographyDialog.__serviceName, regKey) &
            FactoryHelper.writeRegistryServiceInfo(LogViewerDialog.__implementationName,
                LogViewerDialog.__serviceName, regKey) &    
        	FactoryHelper.writeRegistryServiceInfo(BibTeXDialog.__implementationName,
                BibTeXDialog.__serviceName, regKey);    
    }
}
