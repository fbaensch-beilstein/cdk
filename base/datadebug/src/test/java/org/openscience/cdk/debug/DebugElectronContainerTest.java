/* Copyright (C) 1997-2007  The Chemistry Development Kit (CDK) project
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */
package org.openscience.cdk.debug;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openscience.cdk.test.interfaces.AbstractElectronContainerTest;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IElectronContainer;
import org.openscience.cdk.test.interfaces.ITestObjectBuilder;

/**
 * Checks the functionality of the {@link DebugElectronContainer}.
 *
 * @cdk.module test-datadebug
 */
public class DebugElectronContainerTest extends AbstractElectronContainerTest {

    @BeforeClass
    public static void setUp() {
        setTestObjectBuilder(DebugElectronContainer::new);
    }

    @Test
    public void testDebugElectronContainer() {
        IElectronContainer ec = new DebugElectronContainer();
        Assert.assertNotNull(ec);
        Assert.assertEquals(0, ec.getElectronCount().intValue());
    }
}
