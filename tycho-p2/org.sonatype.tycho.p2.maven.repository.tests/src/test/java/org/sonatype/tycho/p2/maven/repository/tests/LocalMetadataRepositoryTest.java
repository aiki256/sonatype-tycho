package org.sonatype.tycho.p2.maven.repository.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import junit.framework.Assert;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.MetadataFactory;
import org.eclipse.equinox.p2.metadata.MetadataFactory.InstallableUnitDescription;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.junit.Test;
import org.sonatype.tycho.p2.facade.RepositoryLayoutHelper;
import org.sonatype.tycho.p2.facade.internal.LocalRepositoryReader;
import org.sonatype.tycho.p2.facade.internal.LocalTychoRepositoryIndex;
import org.sonatype.tycho.p2.maven.repository.LocalMetadataRepository;

public class LocalMetadataRepositoryTest
{
    private IProgressMonitor monitor = new NullProgressMonitor();

    @Test
    public void emptyRepository()
        throws CoreException
    {
        File location = new File( "target/empty" );
        createRepository( location, "group", "artifact", "version" );

        IMetadataRepository repository = loadRepository( location );
        Assert.assertNotNull( repository );
    }

    protected IMetadataRepository loadRepository( File location )
        throws ProvisionException
    {
        return new LocalMetadataRepository( location.toURI(), new LocalTychoRepositoryIndex( location, LocalTychoRepositoryIndex.METADATA_INDEX_RELPATH ),
                                            new LocalRepositoryReader( location ) );
    }

    protected LocalMetadataRepository createRepository( File location, String groupId, String artifactId, String version )
        throws ProvisionException
    {
        location.mkdirs();
        File metadataFile = new File( location, LocalTychoRepositoryIndex.INDEX_RELPATH );
        metadataFile.delete();
        metadataFile.getParentFile().mkdirs();

        return new LocalMetadataRepository( location.toURI(), location.getAbsolutePath() );
    }

    @Test
    public void addInstallableUnit()
        throws CoreException
    {
        File location = new File( "target/metadataRepo" );
        LocalMetadataRepository repository = createRepository( location, "group", "artifact", "version" );

        InstallableUnitDescription iud = new MetadataFactory.InstallableUnitDescription();
        iud.setId( "test" );
        iud.setVersion( Version.parseVersion( "1.0.0" ) );

        iud.setProperty( RepositoryLayoutHelper.PROP_GROUP_ID, "group" );
        iud.setProperty( RepositoryLayoutHelper.PROP_ARTIFACT_ID, "artifact" );
        iud.setProperty( RepositoryLayoutHelper.PROP_VERSION, "version" );

        InstallableUnitDescription iud2 = new MetadataFactory.InstallableUnitDescription();
        iud2.setId( "test2" );
        iud2.setVersion( Version.parseVersion( "1.0.0" ) );

        iud2.setProperty( RepositoryLayoutHelper.PROP_GROUP_ID, "group" );
        iud2.setProperty( RepositoryLayoutHelper.PROP_ARTIFACT_ID, "artifact2" );
        iud2.setProperty( RepositoryLayoutHelper.PROP_VERSION, "version" );

        IInstallableUnit iu = MetadataFactory.createInstallableUnit( iud );
        IInstallableUnit iu2 = MetadataFactory.createInstallableUnit( iud2 );
        repository.addInstallableUnits( Arrays.asList( iu, iu2 ) );

        repository = (LocalMetadataRepository) loadRepository( location );

        IQueryResult<IInstallableUnit> result = repository.query( QueryUtil.ALL_UNITS, monitor );
        ArrayList<IInstallableUnit> allius = new ArrayList<IInstallableUnit>( result.toSet() );
        Assert.assertEquals( 2, allius.size() );

        // as of e3.5.2 Collector uses HashSet internally and does not guarantee collected results order
        // 3.6 IQueryResult, too, is backed by HashSet. makes no sense.
        // Assert.assertEquals( iu.getId(), allius.get( 0 ).getId() );

        Set<IInstallableUnit> ius = repository.getGAVs().get( RepositoryLayoutHelper.getGAV( iu.getProperties() ) );
        Assert.assertEquals( 1, ius.size() );
    }

}
