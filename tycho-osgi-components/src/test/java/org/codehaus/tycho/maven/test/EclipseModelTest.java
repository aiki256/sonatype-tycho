package org.codehaus.tycho.maven.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.codehaus.tycho.model.Feature;
import org.codehaus.tycho.model.Platform;
import org.codehaus.tycho.model.PluginRef;
import org.codehaus.tycho.model.Target;
import org.codehaus.tycho.model.UpdateSite;
import org.codehaus.tycho.model.Target.Location;

public class EclipseModelTest
    extends TestCase
{

    File target = new File( "target/modelio" );

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        target.mkdirs();
    }

    public void testUpdateSite()
        throws Exception
    {
        UpdateSite site = UpdateSite.read( new File( "src/test/resources/modelio/site.xml" ) );

        List<UpdateSite.FeatureRef> features = site.getFeatures();
        assertEquals( 2, features.size() );
        assertEquals( "featureB", features.get( 1 ).getId() );
        assertEquals( "2.0.0", features.get( 1 ).getVersion() );

        Map<String, String> archives = site.getArchives();
        assertEquals( 2, archives.size() );
        assertEquals( "http://www.company.com/updates/plugins/pluginA_1.0.0.jar",
                      archives.get( "plugins/pluginA_1.0.0.jar" ) );

        features.get( 0 ).setVersion( "3.0.0" );

        site.removeArchives();
        assertTrue( site.getArchives().isEmpty() );

        File updatedFile = new File( target, "site.xml" );
        UpdateSite.write( site, updatedFile );
        UpdateSite updated = UpdateSite.read( updatedFile );
        assertEquals( "3.0.0", updated.getFeatures().get( 0 ).getVersion() );
        assertTrue( updated.getArchives().isEmpty() );
    }

    public void testFeature()
        throws Exception
    {
        Feature feature = Feature.read( new File( "src/test/resources/modelio/feature.xml" ) );

        assertEquals( "1.0.0", feature.getVersion() );

        List<PluginRef> plugins = feature.getPlugins();
        assertEquals( 1, plugins.size() );
        assertEquals( "pluginA", plugins.get( 0 ).getId() );

        List<Feature.FeatureRef> features = feature.getIncludedFeatures();
        assertEquals( 1, features.size() );

        List<Feature.RequiresRef> requires = feature.getRequires();
        assertEquals( 1, requires.size() );
        assertEquals( "pluginB", requires.get( 0 ).getImports().get( 0 ).getPlugin() );
        assertEquals( "featureC", requires.get( 0 ).getImports().get( 1 ).getFeature() );

        feature.setVersion( "1.2.3" );
        plugins.get( 0 ).setVersion( "3.4.5" );

        File updatedFile = new File( target, "feature.xml" );
        Feature.write( feature, updatedFile );
        Feature updated = Feature.read( updatedFile );
        assertEquals( "1.2.3", updated.getVersion() );
        assertEquals( "3.4.5", updated.getPlugins().get( 0 ).getVersion() );
    }

    public void testPlatform()
        throws Exception
    {
        Platform platform = Platform.read( new File( "src/test/resources/modelio/platform.xml" ) );

        assertEquals( false, platform.isTransient() );

        List<Platform.Site> sites = platform.getSites();
        assertEquals( 2, sites.size() );

        List<String> plugins = sites.get( 0 ).getPlugins();
        assertEquals( 2, plugins.size() );
        assertEquals( "m2eclipse/org.maven.ide.components.archetype-common/", plugins.get( 0 ) );

        List<Platform.Feature> features = sites.get( 1 ).getFeatures();
        assertEquals( 2, features.size() );

        Platform transientPlatform = new Platform( platform );
        transientPlatform.setTransient( true );

        Platform.Site transientSite = new Platform.Site( "file:/xxx" );
        transientPlatform.addSite( transientSite );

        List<String> transientPlugins = new ArrayList<String>();
        transientPlugins.add( "plugins/yyy/" );
        transientPlugins.add( "plugins/zzz/" );
        transientSite.setPlugins( transientPlugins );

        List<Platform.Feature> transientFeatures = new ArrayList<Platform.Feature>();

        Platform.Feature transientFeature = new Platform.Feature();
        transientFeature.setId( "transient.feature" );
        transientFeature.setUrl( "transient-url" );
        transientFeature.setVersion( "1.2.3" );
        transientFeatures.add( transientFeature );

        transientSite.setFeatures( transientFeatures );

        File updatedFile = new File( target, "platform.xml" );
        Platform.write( transientPlatform, updatedFile );

        Platform updated = Platform.read( updatedFile );

        assertEquals( "plugins/yyy/,plugins/zzz/", updated.getSites().get( 2 ).getPluginsStr() );

        List<Platform.Feature> updatedFeatures = updated.getSites().get( 2 ).getFeatures();
        assertEquals( 1, updatedFeatures.size() );
        assertEquals( transientFeature.getId(), updatedFeatures.get( 0 ).getId() );
        assertEquals( transientFeature.getUrl(), updatedFeatures.get( 0 ).getUrl() );
        assertEquals( transientFeature.getVersion(), updatedFeatures.get( 0 ).getVersion() );
    }

    public void testTarget()
        throws Exception
    {
        Target target = Target.read( new File( "src/test/resources/modelio/target.target" ) );

        List<Location> locations = target.getLocations();
        assertEquals( 2, locations.size() );

        Location l01 = locations.get( 0 );
        assertEquals( "http://download.eclipse.org/eclipse/updates/3.5", l01.getRepositoryLocation() );
        assertEquals( 1, l01.getUnits().size() );
        assertEquals( "org.eclipse.sdk.ide", l01.getUnits().get( 0 ).getId() );
        assertEquals( "3.5.0.I20090611-1540", l01.getUnits().get( 0 ).getVersion() );

        Location l02 = locations.get( 1 );
        assertEquals( 4, l02.getUnits().size() );
    }
}
