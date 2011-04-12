/*******************************************************************************
 * Copyright (c) 2008, 2011 Sonatype Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.tycho.surefire.junit;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import junit.framework.Test;

import org.apache.maven.surefire.suite.AbstractDirectoryTestSuite;
import org.apache.maven.surefire.testset.PojoTestSet;
import org.apache.maven.surefire.testset.SurefireTestSet;
import org.apache.maven.surefire.testset.TestSetFailedException;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * Test suite for JUnit tests based on a directory of Java test classes.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class JUnitDirectoryTestSuite
    extends AbstractDirectoryTestSuite
{
    public JUnitDirectoryTestSuite( File basedir, ArrayList includes, ArrayList excludes )
    {
        super( basedir, includes, excludes );
    }

    protected SurefireTestSet createTestSet( Class testClass, ClassLoader classLoader )
        throws TestSetFailedException
    {
        Class junitClass = null;
        try
        {
            junitClass = classLoader.loadClass( Test.class.getName() );
        }
        catch ( ClassNotFoundException e )
        {
            // ignore this
        }

        SurefireTestSet testSet;
        if ( junitClass != null && junitClass.isAssignableFrom( testClass ) )
        {
            testSet = new JUnitTestSet( testClass );
        }
        else if (classHasTestSuiteMethod( testClass, junitClass ))
        {
            testSet = new JUnitTestSet( testClass );
        }
        else if (classHasPublicNoArgConstructor( testClass ))
        {
            testSet = new PojoTestSet( testClass );
        }
        else
        {
            testSet = null;
        }
        return testSet;
    }

    private boolean classHasTestSuiteMethod( Class testClass, Class junitClass ) 
    {
    	if ( junitClass == null )
    	{
    		return false;
    	}

        try
        {
            Method method = testClass.getMethod("suite", new Class[0] );
            return method != null
                   && Modifier.isPublic( method.getModifiers() )
                   && Modifier.isStatic( method.getModifiers() )
                   && junitClass.isAssignableFrom( method.getReturnType() );
        }
        catch ( Exception e )
        {
            return false;
        }
	}

	private boolean classHasPublicNoArgConstructor( Class testClass )
    {
        try
        {
            testClass.getConstructor( new Class[0] );
            return true;
        }
        catch ( Exception e )
        {
            return false;
        }
    }
}
