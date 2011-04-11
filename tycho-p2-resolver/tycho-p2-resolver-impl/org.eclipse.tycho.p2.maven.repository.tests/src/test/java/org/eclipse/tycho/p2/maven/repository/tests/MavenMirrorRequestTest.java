package org.eclipse.tycho.p2.maven.repository.tests;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.metadata.ArtifactKey;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.MetadataFactory;
import org.eclipse.equinox.p2.metadata.MetadataFactory.InstallableUnitDescription;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRequest;
import org.eclipse.tycho.p2.maven.repository.Activator;
import org.eclipse.tycho.p2.maven.repository.LocalArtifactRepository;
import org.eclipse.tycho.p2.maven.repository.LocalMetadataRepository;
import org.eclipse.tycho.p2.maven.repository.MavenMirrorRequest;
import org.junit.Assert;
import org.junit.Test;

public class MavenMirrorRequestTest
{
    private IProgressMonitor monitor = new NullProgressMonitor();

    @Test
    public void testMirror()
        throws Exception
    {
        IProvisioningAgent agent = Activator.getProvisioningAgent();

        IArtifactRepositoryManager manager =
            (IArtifactRepositoryManager) agent.getService( IArtifactRepositoryManager.SERVICE_NAME );

        IArtifactRepository repository =
            manager.loadRepository( new File( "resources/repositories/e342" ).toURI(), monitor );

        LocalArtifactRepository localRepository = new LocalArtifactRepository( new File( "target/local" ) );
        LocalMetadataRepository localMetadataRepository =
            new LocalMetadataRepository( new File( "target/local" ).toURI(), "local" );

        IArtifactKey key =
            new ArtifactKey( "osgi.bundle", "org.eclipse.osgi", Version.parseVersion( "3.4.3.R34x_v20081215-1030" ) );

        InstallableUnitDescription iud = new InstallableUnitDescription();
        IInstallableUnit iu = MetadataFactory.createInstallableUnit( iud );

        MavenMirrorRequest request = new MavenMirrorRequest( key, localRepository );

        repository.getArtifacts( new IArtifactRequest[] { request }, monitor );

        Assert.assertEquals( 1, localRepository.getArtifactDescriptors( key ).length );
    }
}
