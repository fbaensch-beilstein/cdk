/* Copyright (C) 2004-2007  The Chemistry Development Kit (CDK) project
 *
 * Contact: cdk-devel@slists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *  */
package org.openscience.cdk.io.iterator;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.test.CDKTestCase;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.formats.IResourceFormat;
import org.openscience.cdk.io.formats.SMILESFormat;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * TestCase for the reading SMILES mol files using one test file.
 *
 * @see org.openscience.cdk.io.SMILESReader
 */
class IteratingSMILESReaderTest extends CDKTestCase {

    private static final ILoggingTool logger = LoggingToolFactory.createLoggingTool(IteratingSMILESReaderTest.class);

    @Test
    void testSMILESFileWithNames() throws Exception {
        String filename = "test.smi";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        IteratingSMILESReader reader = new IteratingSMILESReader(ins, DefaultChemObjectBuilder.getInstance());

        int molCount = 0;
        while (reader.hasNext()) {
            Object object = reader.next();
            Assertions.assertNotNull(object);
            Assertions.assertTrue(object instanceof IAtomContainer);
            molCount++;
        }

        Assertions.assertEquals(5, molCount);

        reader.close();
    }

    @Test
    void testSMILESFileWithSpacesAndTabs() throws Exception {
        String filename = "tabs.smi";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        IteratingSMILESReader reader = new IteratingSMILESReader(ins, DefaultChemObjectBuilder.getInstance());

        int molCount = 0;
        while (reader.hasNext()) {
            Object object = reader.next();
            Assertions.assertNotNull(object);
            Assertions.assertTrue(object instanceof IAtomContainer);
            molCount++;
        }

        Assertions.assertEquals(5, molCount);

        reader.close();
    }

    @Test
    void testSMILESTitles() throws Exception {
        String filename = "tabs.smi";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        IteratingSMILESReader reader = new IteratingSMILESReader(ins, DefaultChemObjectBuilder.getInstance());

        while (reader.hasNext()) {

            IAtomContainer mol = reader.next();
            String title = mol.getProperty(CDKConstants.TITLE);
            Assertions.assertNotNull(title);
        }

    }

    @Test
    void testSMILESFile() {
        String filename = "test2.smi";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        IteratingSMILESReader reader = new IteratingSMILESReader(ins, DefaultChemObjectBuilder.getInstance());

        int molCount = 0;
        while (reader.hasNext()) {
            Object object = reader.next();
            Assertions.assertNotNull(object);
            Assertions.assertTrue(object instanceof IAtomContainer);
            molCount++;
        }

        Assertions.assertEquals(5, molCount);
    }

    @Test
    void testGetFormat() {
        String filename = "test2.smi";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        IteratingSMILESReader reader = new IteratingSMILESReader(ins, DefaultChemObjectBuilder.getInstance());
        IResourceFormat format = reader.getFormat();
        Assertions.assertTrue(format instanceof SMILESFormat);
    }

    @Test
    void testSetReader1() {
        String filename = "test2.smi";
        InputStream ins1 = this.getClass().getResourceAsStream(filename);
        IteratingSMILESReader reader = new IteratingSMILESReader(ins1, DefaultChemObjectBuilder.getInstance());
        int molCount = 0;
        while (reader.hasNext()) {
            reader.next();
            molCount++;
        }
        filename = "tabs.smi";
        InputStream ins2 = this.getClass().getResourceAsStream(filename);
        reader.setReader(ins2);
        molCount = 0;
        while (reader.hasNext()) {
            reader.next();
            molCount++;
        }
        Assertions.assertEquals(5, molCount);
    }

    @Test
    void testRemove() {
        String filename = "test2.smi";
        InputStream ins1 = this.getClass().getResourceAsStream(filename);
        IteratingSMILESReader reader = new IteratingSMILESReader(ins1, DefaultChemObjectBuilder.getInstance());
        int molCount = 0;
        while (reader.hasNext()) {
            reader.next();
            molCount++;
            if (molCount > 2) break;
        }
        Assertions.assertThrows(UnsupportedOperationException.class,
                                reader::remove);
    }

    @Test
    void empty() {
        Reader reader = new StringReader(" empty1\n empty2");
        IteratingSMILESReader smis = new IteratingSMILESReader(reader, SilentChemObjectBuilder.getInstance());
        Assertions.assertTrue(smis.hasNext());
        IAtomContainer m1 = smis.next();
        assertThat(m1.getAtomCount(), is(0));
        assertThat(m1.getProperty(CDKConstants.TITLE, String.class), CoreMatchers.is("empty1"));
        Assertions.assertTrue(smis.hasNext());
        IAtomContainer m2 = smis.next();
        assertThat(m2.getAtomCount(), is(0));
        assertThat(m2.getProperty(CDKConstants.TITLE, String.class), CoreMatchers.is("empty2"));
        Assertions.assertFalse(smis.hasNext());
    }

    @Test
    void problemSmiles() {

        Reader reader = new StringReader(" okay\nn1cccc1 bad\n okay");
        IteratingSMILESReader smis = new IteratingSMILESReader(reader, SilentChemObjectBuilder.getInstance());
        Assertions.assertTrue(smis.hasNext());
        IAtomContainer m1 = smis.next();
        assertThat(m1.getAtomCount(), is(0));
        assertThat(m1.getProperty(CDKConstants.TITLE, String.class), CoreMatchers.is("okay"));
        Assertions.assertTrue(smis.hasNext());
        IAtomContainer m2 = smis.next();
        assertThat(m2.getAtomCount(), is(0));
        assertThat(m2.getProperty(CDKConstants.TITLE, String.class), CoreMatchers.is("bad"));
        assertThat(m2.getProperty(IteratingSMILESReader.BAD_SMILES_INPUT, String.class), CoreMatchers.is("n1cccc1 bad"));
        IAtomContainer m3 = smis.next();
        assertThat(m3.getAtomCount(), is(0));
        assertThat(m3.getProperty(CDKConstants.TITLE, String.class), CoreMatchers.is("okay"));
        Assertions.assertFalse(smis.hasNext());
    }
}
