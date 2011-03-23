package org.sonatype.tycho.p2.tools;

import java.io.File;
import java.util.List;

public class BuildContext
{
    private final String qualifier;

    private final List<TargetEnvironment> environments;

    private final File targetDirectory;

    /**
     * Creates a new <code>BuildContext</code> instance.
     * 
     * @param qualifier The build qualifier of the current project
     * @param environments The list of environments targeted by the build; must contain at least one
     *            entry
     * @param targetDirectory The build target directory of the current project
     * @throws IllegalArgumentException if no target environment has been specified
     */
    public BuildContext( String qualifier, List<TargetEnvironment> environments, File targetDirectory )
        throws IllegalArgumentException
    {
        if ( environments.size() == 0 )
        {
            throw new IllegalArgumentException( "List of target environments must not be empty" );
        }

        this.qualifier = qualifier;
        this.environments = environments;
        this.targetDirectory = targetDirectory;
    }

    /**
     * @return the build qualifier of the current project
     */
    public String getQualifier()
    {
        return qualifier;
    }

    /**
     * Returns the list of configured target environments, or the running environment if no
     * environments have been specified explicitly.
     * 
     * @return the list of {@link TargetEnvironment} to be addressed; never <code>null</code> or
     *         empty
     */
    public List<TargetEnvironment> getEnvironments()
    {
        return environments;
    }

    /**
     * @return the build target directory of the current project
     */
    public File getTargetDirectory()
    {
        return targetDirectory;
    }
}