package org.sonatype.tycho.p2.impl.publisher;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.equinox.internal.p2.updatesite.SiteFeature;
import org.eclipse.equinox.internal.p2.updatesite.SiteModel;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.metadata.MetadataFactory;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.metadata.VersionRange;

@SuppressWarnings( "restriction" )
public abstract class AbstractSiteDependenciesAction extends AbstractDependenciesAction
{

    private final String id;

    private final String version;


    public AbstractSiteDependenciesAction( String id, String version )
    {
        this.id = id;
        this.version = version;
    }

    abstract SiteModel getSiteModel();
    
    @Override
    protected Set<IRequirement> getRequiredCapabilities()
    {
        Set<IRequirement> required = new LinkedHashSet<IRequirement>();

        for ( SiteFeature feature : getSiteModel().getFeatures() )
        {
            String id = feature.getFeatureIdentifier() + FEATURE_GROUP_IU_SUFFIX; //$NON-NLS-1$

            VersionRange range = getVersionRange( createVersion( feature.getFeatureVersion() ) );

            required.add( MetadataFactory.createRequirement( IInstallableUnit.NAMESPACE_IU_ID, id, range, null,
                                                                    false, false ) );
        }
        return required;
    }

    @Override
    protected String getId()
    {
        return id;
    }

    @Override
    protected Version getVersion()
    {
        return createSiteVersion( version );
    }

    public static Version createSiteVersion( String version )
    {
        try
        {
            // try default (OSGi?) format first
            return Version.create( version );
        }
        catch ( IllegalArgumentException e )
        {
            // treat as raw otherwise
            return Version.create( "format(n[.n=0;[.n=0;['-'S]]]):" + version );
        }
    }
}