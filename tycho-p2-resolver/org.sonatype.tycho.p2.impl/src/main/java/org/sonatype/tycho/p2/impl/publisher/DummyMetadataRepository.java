package org.sonatype.tycho.p2.impl.publisher;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.Collector;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.repository.IRepositoryReference;
import org.eclipse.equinox.p2.repository.metadata.spi.AbstractMetadataRepository;

// TODO delete this class when fix for Eclipse bug #332444 is integrated
public final class DummyMetadataRepository
    extends AbstractMetadataRepository
{

    public DummyMetadataRepository()
    {
        super( null );
    }

    @Override
    public void initialize( RepositoryState state )
    {
        // do nothing
    }

    @Override
    public boolean isModifiable()
    {
        // changes are allowed, but will go to /dev/null
        return true;
    }

    public IQueryResult<IInstallableUnit> query( IQuery<IInstallableUnit> query, IProgressMonitor monitor )
    {
        return new Collector<IInstallableUnit>();
    }

    public Collection<IRepositoryReference> getReferences()
    {
        return Collections.emptyList();
    }
}
