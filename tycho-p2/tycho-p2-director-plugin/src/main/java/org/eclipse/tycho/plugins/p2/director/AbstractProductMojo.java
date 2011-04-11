package org.eclipse.tycho.plugins.p2.director;

import java.io.File;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.eclipse.tycho.TargetEnvironment;
import org.eclipse.tycho.TargetPlatform;
import org.eclipse.tycho.TargetPlatformConfiguration;
import org.eclipse.tycho.TychoProject;
import org.eclipse.tycho.utils.TychoProjectUtils;

abstract class AbstractProductMojo
    extends AbstractMojo
{

    /** @parameter expression="${project}" */
    private MavenProject project;

    /** @parameter expression="${session}" */
    private MavenSession session;

    /**
     * @parameter
     */
    private List<Product> products;

    MavenProject getProject()
    {
        return project;
    }

    MavenSession getSession()
    {
        return session;
    }

    File getBuildDirectory()
    {
        return new File( getProject().getBuild().getDirectory() );
    }

    File getProductsBuildDirectory()
    {
        return new File( getBuildDirectory(), "products" );
    }

    File getProductMaterializeDirectory( Product product, TargetEnvironment env )
    {
        return new File( getProductsBuildDirectory(), product.getId() + "/" + getOsWsArch( env, '/' ) );
    }

    List<TargetEnvironment> getEnvironments()
    {
        TargetPlatformConfiguration configuration = TychoProjectUtils.getTargetPlatformConfiguration( project );
        return configuration.getEnvironments();
    }

    TargetPlatform getTargetPlatform()
    {
        return getTychoProjectFacet( project.getPackaging() ).getTargetPlatform( project );
    }

    private TychoProject getTychoProjectFacet( String packaging )
    {
        TychoProject facet;
        try
        {
            facet = (TychoProject) session.lookup( TychoProject.class.getName(), packaging );
        }
        catch ( ComponentLookupException e )
        {
            throw new IllegalStateException( "Could not lookup required component", e );
        }
        return facet;
    }

    ProductConfig getProductConfig()
        throws MojoFailureException
    {
        return new ProductConfig( products, getProductsBuildDirectory() );
    }

    static String getOsWsArch( TargetEnvironment env, char separator )
    {
        return env.getOs() + separator + env.getWs() + separator + env.getArch();
    }
}
