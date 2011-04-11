package org.eclipse.tycho.p2.impl.publisher;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.updatesite.Activator;
import org.eclipse.equinox.internal.p2.updatesite.SiteModel;
import org.eclipse.equinox.internal.p2.updatesite.UpdateSite;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.publisher.IPublisherInfo;
import org.eclipse.equinox.p2.publisher.IPublisherResult;

@SuppressWarnings( "restriction" )
public class SiteDependenciesAction
    extends AbstractSiteDependenciesAction
{
    private final File location;

    private UpdateSite updateSite;

    public SiteDependenciesAction( File location, String id, String version )
    {
        super( id, version );
        this.location = location;
    }
    
    @Override
    SiteModel getSiteModel()
    {
        return updateSite.getSite();
    }
    
    @Override
    public IStatus perform( IPublisherInfo publisherInfo, IPublisherResult results, IProgressMonitor monitor )
    {
        try
        {
            updateSite = UpdateSite.load( location.toURI(), monitor );
        }
        catch ( ProvisionException e )
        {
            return new Status( IStatus.ERROR, Activator.ID, "Error generating site xml action.", e );
        }

        return super.perform( publisherInfo, results, monitor );
    }



}
