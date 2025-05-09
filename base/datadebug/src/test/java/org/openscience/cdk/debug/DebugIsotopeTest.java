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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.test.interfaces.AbstractIsotopeTest;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IElement;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.test.interfaces.ITestObjectBuilder;

/**
 * Checks the functionality of the AtomContainer.
 *
 */
class DebugIsotopeTest extends AbstractIsotopeTest {

    @BeforeAll
    static void setUp() {
        setTestObjectBuilder(new ITestObjectBuilder() {

            @Override
            public IChemObject newTestObject() {
                return new DebugIsotope("C");
            }
        });
    }

    @Test
    void testDebugIsotope_String() {
        IIsotope i = new DebugIsotope("C");
        Assertions.assertEquals("C", i.getSymbol());
    }

    @Test
    void testDebugIsotope_IElement() {
        IElement element = newChemObject().getBuilder().newInstance(IElement.class, "C");
        IIsotope i = new DebugIsotope(element);
        Assertions.assertEquals("C", i.getSymbol());
    }

    @Test
    void testDebugIsotope_int_String_int_double_double() {
        IIsotope i = new DebugIsotope(6, "C", 12, 12.001, 80.0);
        Assertions.assertEquals(12, i.getMassNumber().intValue());
        Assertions.assertEquals("C", i.getSymbol());
        Assertions.assertEquals(6, i.getAtomicNumber().intValue());
        Assertions.assertEquals(12.001, i.getExactMass(), 0.001);
        Assertions.assertEquals(80.0, i.getNaturalAbundance(), 0.001);
    }

    @Test
    void testDebugIsotope_String_int() {
        IIsotope i = new DebugIsotope("C", 12);
        Assertions.assertEquals(12, i.getMassNumber().intValue());
        Assertions.assertEquals("C", i.getSymbol());
    }

    @Test
    void testDebugIsotope_int_String_double_double() {
        IIsotope i = new DebugIsotope(6, "C", 12.001, 80.0);
        Assertions.assertEquals("C", i.getSymbol());
        Assertions.assertEquals(6, i.getAtomicNumber().intValue());
        Assertions.assertEquals(12.001, i.getExactMass(), 0.001);
        Assertions.assertEquals(80.0, i.getNaturalAbundance(), 0.001);
    }
}
