package org.sonatype.tycho.p2.facade.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Implementation of RepositoryReader interface that delegates to Maven repository subsystem to retrieve artifacts from
 * remote repository.
 */
@Component( role = MavenRepositoryReader.class, instantiationStrategy = "per-lookup" )
public class MavenRepositoryReader
    implements RepositoryReader
{
    /**
     * Maps repository URL to set of GAVC keys that are not present in the repository
     */
    private static Map<String, Set<String>> notFoundCache = new HashMap<String, Set<String>>();

    @Requirement
    private RepositorySystem repositorySystem;

    private List<ArtifactRepository> repositories;

    private ArtifactRepository localRepository;

    public InputStream getContents( GAV gav, String classifier, String extension )
        throws IOException
    {
        String key = getKey( gav, classifier, extension );

        if ( isNotFound( key ) )
        {
            throw new IOException( );
        }

        Artifact a = repositorySystem.createArtifactWithClassifier( gav.getGroupId(), gav.getArtifactId(), gav
            .getVersion(), extension, classifier );

        ArtifactResolutionRequest request = new ArtifactResolutionRequest();
        request.setArtifact( a );
        request.setLocalRepository( localRepository );
        request.setRemoteRepositories( repositories );
        ArtifactResolutionResult result = repositorySystem.resolve( request );

        if ( !a.isResolved() )
        {
            setNotFound( key );

            IOException exception = new IOException( "Could not resolve artifact" );
            if ( result.hasExceptions() )
            {
                exception.initCause( result.getExceptions().get( 0 ) );
            }
            throw exception;
        }

        return new FileInputStream( a.getFile() );
    }
    
    public InputStream getContents( String remoteRelpath )
        throws IOException
    {
        if ( repositories.size() != 1 )
        {
            throw new IllegalStateException( "Ambiguous repository request" );
        }

        ArtifactRepository repository = repositories.get( 0 );

        final File file = File.createTempFile( repository.getId(), ".tmp" );

        try
        {
            repositorySystem.retrieve( repository, file, remoteRelpath, null );
        }
        catch ( TransferFailedException cause )
        {
            IOException e = new IOException();
            e.initCause( cause );
            throw e;
        }
        catch ( ResourceDoesNotExistException cause )
        {
            IOException e = new FileNotFoundException();
            e.initCause( cause );
            throw e;
        }

        return new FileInputStream( file )
        {
            @Override
            public void close()
                throws IOException
            {
                super.close();
                file.delete();
            }
        };
    }

    private boolean isNotFound( String key )
    {
        for ( ArtifactRepository repository : repositories )
        {
            Set<String> keys = notFoundCache.get( repository.getUrl() );

            if ( keys == null || !keys.contains( key ) )
            {
                return false;
            }
        }

        return true;
    }

    private void setNotFound( String key )
    {
        for ( ArtifactRepository repository : repositories )
        {
            Set<String> keys = notFoundCache.get( repository.getUrl() );

            if ( keys == null )
            {
                keys = new HashSet<String>();
                notFoundCache.put( repository.getUrl(), keys );
            }

            keys.add( key );
        }
    }

    private String getKey( GAV gav, String classifier, String extension )
    {
        StringBuilder sb = new StringBuilder();
        sb.append( gav.getGroupId() );
        sb.append( ':' ).append( gav.getArtifactId() );
        sb.append( ':' ).append( gav.getVersion() );
        sb.append( ':' );
        if ( classifier != null )
        {
            sb.append( classifier );
        }
        sb.append( ':' );
        if ( extension != null )
        {
            sb.append( extension );
        }
        return sb.toString();
    }

    public void setArtifactRepository( ArtifactRepository repository )
    {
        this.repositories = new ArrayList<ArtifactRepository>();
        this.repositories.add( repository );
    }

    public void setLocalRepository( ArtifactRepository localRepository )
    {
        this.localRepository = localRepository;
    }
}
