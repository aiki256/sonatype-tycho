package org.sonatype.tycho.p2.publisher;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.equinox.internal.p2.publisher.eclipse.IProductDescriptor;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.metadata.IVersionedId;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.osgi.service.environment.Constants;
import org.sonatype.tycho.p2.publisher.model.VersionedName2;

@SuppressWarnings( "restriction" )
public class ProductDependenciesAction
    extends AbstractDependenciesAction
{
    private final IProductDescriptor product;

    private final List<Map<String,String>> environments;

    public ProductDependenciesAction( IProductDescriptor product, List<Map<String, String>> environments )
    {
        this.product = product;
        this.environments = environments;
    }

    @Override
    protected Version getVersion()
    {
        return Version.create( product.getVersion() );
    }

    @Override
    protected String getId()
    {
        return product.getId();
    }

    @Override
    protected Set<IRequirement> getRequiredCapabilities()
    {
        Set<IRequirement> required = new LinkedHashSet<IRequirement>();

        if ( product.useFeatures() )
        {
            for ( IVersionedId feature : (List<IVersionedId>) product.getFeatures() )
            {
                String id = feature.getId() + FEATURE_GROUP_IU_SUFFIX; //$NON-NLS-1$
                Version version = feature.getVersion();

                addRequiredCapability( required, id, version, null );
            }
        }
        else
        {
            for ( IVersionedId plugin : (List<IVersionedId>) product.getBundles( true ) )
            {
                addRequiredCapability( required, plugin.getId(), plugin.getVersion(), getFilter( plugin ) );
            }
        }

        // TODO only include when includeLaunchers=true (includeLaunchers is not exposed by IProductDescriptor)
        addRequiredCapability( required, "org.eclipse.equinox.executable.feature.group", null, null );

        // these are implicitly required, see
        // See also org.codehaus.tycho.osgitools.AbstractArtifactDependencyWalker.traverseProduct
        addRequiredCapability( required, "org.eclipse.equinox.launcher", null, null );
        if ( environments != null )
        {
            for ( Map<String,String> env : environments )
            {
                addNativeRequirements( required, env.get( OSGI_OS ), env.get( OSGI_WS ),
                                       env.get( OSGI_ARCH ) );
            }
        }
        return required;
    }

    private String getFilter( IVersionedId name )
    {
        if ( !( name instanceof VersionedName2 ) )
        {
            return null;
        }

        VersionedName2 name2 = (VersionedName2) name;
        return getFilter( name2.getOs(), name2.getWs(), name2.getArch() );
    }

    private void addNativeRequirements( Set<IRequirement> required, String os, String ws, String arch )
    {
        String filter = getFilter( os, ws, arch );

        if ( Constants.OS_MACOSX.equals( os ) )
        {
            // macosx is twisted
            if ( Constants.ARCH_X86.equals( arch ) )
            {
                addRequiredCapability( required, "org.eclipse.equinox.launcher." + ws + "." + os, null, filter );
                return;
            }
        }

        addRequiredCapability( required, "org.eclipse.equinox.launcher." + ws + "." + os + "." + arch, null, filter );
    }

}
