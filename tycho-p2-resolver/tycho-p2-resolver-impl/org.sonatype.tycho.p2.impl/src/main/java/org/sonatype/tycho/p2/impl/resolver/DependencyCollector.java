package org.sonatype.tycho.p2.impl.resolver;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.metadata.RequiredCapability;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.sonatype.tycho.p2.impl.Activator;
import org.sonatype.tycho.p2.impl.publisher.FeatureDependenciesAction;
import org.sonatype.tycho.p2.resolver.P2Logger;

@SuppressWarnings( "restriction" )
public class DependencyCollector
    extends ResolutionStrategy
{
    private final P2Logger logger;

    public DependencyCollector( P2Logger logger )
    {
        this.logger = logger;
    }

    @Override
    public Collection<IInstallableUnit> resolve( IProgressMonitor monitor )
    {
        Set<IInstallableUnit> result = new LinkedHashSet<IInstallableUnit>();

        LinkedHashSet<IStatus> errors = new LinkedHashSet<IStatus>();

        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Available IUs:\n" + ResolverDebugUtils.toDebugString( availableIUs, false, monitor ) );
            logger.debug( "Root IUs:\n" + ResolverDebugUtils.toDebugString( rootIUs, true ) );
            logger.debug( "Extra IUs:\n" + ResolverDebugUtils.toDebugString( rootIUs, true ) );
        }

        result.addAll( rootIUs );

        for ( IInstallableUnit iu : rootIUs )
        {
            collectIncludedIUs( result, errors, iu, true, monitor );
        }

        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Collected IUs:\n" + ResolverDebugUtils.toDebugString( result, false ) );
        }

        // TODO additionalRequirements

        if ( !errors.isEmpty() )
        {
            MultiStatus status =
                new MultiStatus( Activator.PLUGIN_ID, 0, errors.toArray( new IStatus[errors.size()] ),
                                 "Missing dependencies", null );

            throw new RuntimeException( status.toString(), new ProvisionException( status ) );
        }

        return result;
    }

    private void collectIncludedIUs( Set<IInstallableUnit> result, Set<IStatus> errors, IInstallableUnit iu,
                                     boolean immediate, IProgressMonitor monitor )
    {
        // features listed in site.xml directly
        // features/bundles included in included features (RequiredCapability.isVersionStrict is approximation of this)

        for ( IRequirement req : iu.getRequirements() )
        {
            IQueryResult<IInstallableUnit> matches =
                availableIUs.query( QueryUtil.createLatestQuery( QueryUtil.createMatchQuery( req.getMatches() ) ),
                                    monitor );

            if ( !matches.isEmpty() )
            {
                IInstallableUnit match = matches.iterator().next(); // can only be one

                if ( immediate || isIncluded( iu, req, match ) )
                {
                    result.add( match );

                    if ( isFeature( match ) )
                    {
                        collectIncludedIUs( result, errors, match, false, monitor );
                    }
                }
            }
            else
            {
                errors.add( new Status( IStatus.ERROR, Activator.PLUGIN_ID, "Unable to find dependency from "
                    + iu.toString() + " to " + req.toString() ) );
            }
        }
    }

    private boolean isIncluded( IInstallableUnit iu, IRequirement req, IInstallableUnit match )
    {
        Set<String> includedIUs = FeatureDependenciesAction.getIncludedUIs( iu );

        if ( includedIUs.contains( match.getId() ) )
        {
            return true;
        }

        return RequiredCapability.isVersionStrict( req.getMatches() );
    }

    private boolean isFeature( IInstallableUnit iu )
    {
        return QueryUtil.isGroup( iu );
    }
}
