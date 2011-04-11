package org.eclipse.tycho.osgitest;

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.tycho.equinox.launching.DefaultEquinoxInstallationDescription;
import org.eclipse.tycho.equinox.launching.internal.DefaultEquinoxInstallation;
import org.eclipse.tycho.equinox.launching.internal.EquinoxLaunchConfiguration;
import org.eclipse.tycho.osgitest.TestMojo;

public class TestMojoTest
    extends TestCase
{

    public void testVMArgLineMultipleArgs()
        throws Exception
    {
        EquinoxLaunchConfiguration cli = createEquinoxConfiguration();
        TestMojo testMojo = new TestMojo();
        testMojo.addVMArgs( cli, " -Dfoo=bar -Dkey2=value2 " );
        String[] vmArguments = cli.getVMArguments();
        assertEquals( 2, vmArguments.length );
        assertEquals( "-Dfoo=bar", vmArguments[0] );
        assertEquals( "-Dkey2=value2", vmArguments[1] );
    }

    public void testAddProgramArgsNotEscaped()
        throws Exception
    {
        EquinoxLaunchConfiguration cli = createEquinoxConfiguration();
        TestMojo testMojo = new TestMojo();
        testMojo.addProgramArgs( false, cli, " foo bar   baz " );
        String[] args = cli.getProgramArguments();
        assertEquals( 3, args.length );
        assertEquals( "foo", args[0] );
        assertEquals( "bar", args[1] );
        assertEquals( "baz", args[2] );
    }

    public void testAddProgramArgsEscaped()
        throws Exception
    {
        EquinoxLaunchConfiguration cli = createEquinoxConfiguration();
        TestMojo testMojo = new TestMojo();
        testMojo.addProgramArgs( true, cli, "-data" , "/path with spaces " );
        assertEquals( 2, cli.getProgramArguments().length );
        assertEquals( "-data", cli.getProgramArguments()[0] );
        assertEquals( "/path with spaces ", cli.getProgramArguments()[1] );
    }

    
    public void testAddProgramArgsNullArg()
        throws Exception
    {
        EquinoxLaunchConfiguration cli = createEquinoxConfiguration();
        TestMojo testMojo = new TestMojo();
        // null arg must be ignored
        testMojo.addProgramArgs( true, cli, "-data", null );
        assertEquals( 1, cli.getProgramArguments().length );
    }

    private EquinoxLaunchConfiguration createEquinoxConfiguration()
    {
        DefaultEquinoxInstallation testRuntime =
            new DefaultEquinoxInstallation( new DefaultEquinoxInstallationDescription(), null );
        return new EquinoxLaunchConfiguration( testRuntime );
    }

}
