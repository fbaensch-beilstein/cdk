/* Copyright (C) 2003-2007  The Chemistry Development Kit (CDK) project
 *
 * Contact: cdk-devel@lists.sourceforge.net
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
package org.openscience.cdk.io.cml;

import java.io.InputStream;
import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IElement;
import org.openscience.cdk.test.CDKTestCase;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.geometry.GeometryUtil;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemSequence;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * TestCase for the reading CML 2 files using a few test files
 * in data/cmltest.
 *
 * @cdk.require java1.5+
 */
class CML2Test extends CDKTestCase {

    private static final ILoggingTool logger = LoggingToolFactory.createLoggingTool(CML2Test.class);

    @Test
    void testFile3() throws Exception {
        String filename = "3.cml";
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        IAtomContainer mol = ChemFileManipulator.getAllAtomContainers(chemFile).get(0);

        for (int i = 0; i <= 3; i++) {
            Assertions.assertFalse(mol.getBond(i).getFlag(IChemObject.AROMATIC), "Bond " + (i + 1) + " is not aromatic in the file");
        }
        for (int i = 4; i <= 9; i++) {
            Assertions.assertTrue(mol.getBond(i).getFlag(IChemObject.AROMATIC), "Bond " + (i + 1) + " is aromatic in the file");
        }
    }

    /**
     * @cdk.bug 2114987
     */
    @Test
    void testCMLTestCase() throws Exception {
        String filename = "olaCmlAtomType.cml";
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = new ChemFile();
        chemFile = reader.read(chemFile);
        reader.close();
        IAtomContainer container = ChemFileManipulator.getAllAtomContainers(chemFile).get(0);
        for (IAtom atom : container.atoms()) {
            Assertions.assertEquals(CDKConstants.UNSET, atom.getImplicitHydrogenCount());
        }
    }

    @Test
    void testCOONa() throws Exception {
        String filename = "COONa.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(seq.getChemModelCount(), 1);
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(model.getMoleculeSet().getAtomContainerCount(), 1);

        // test the molecule
        IAtomContainer mol = model.getMoleculeSet().getAtomContainer(0);
        Assertions.assertNotNull(mol);
        Assertions.assertEquals(4, mol.getAtomCount());
        Assertions.assertEquals(2, mol.getBondCount());
        Assertions.assertTrue(GeometryUtil.has3DCoordinates(mol));
        Assertions.assertTrue(!GeometryUtil.has2DCoordinates(mol));

        for (IAtom atom : mol.atoms()) {
            if (atom.getAtomicNumber() == IElement.Na) Assertions.assertEquals(+1, atom.getFormalCharge().intValue());
        }
    }

    @Test
    void testNitrate() throws Exception {
        String filename = "nitrate.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(seq.getChemModelCount(), 1);
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(model.getMoleculeSet().getAtomContainerCount(), 1);

        // test the molecule
        IAtomContainer mol = model.getMoleculeSet().getAtomContainer(0);
        Assertions.assertNotNull(mol);
        Assertions.assertEquals(4, mol.getAtomCount());
        Assertions.assertEquals(3, mol.getBondCount());
        Assertions.assertTrue(GeometryUtil.has3DCoordinates(mol));
        Assertions.assertTrue(!GeometryUtil.has2DCoordinates(mol));

        for (IAtom atom : mol.atoms()) {
            if (atom.getAtomicNumber() == IElement.N) Assertions.assertEquals(+1, atom.getFormalCharge().intValue());
        }
    }

    @Test
    void testCMLOK1() throws Exception {
        String filename = "cs2a.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(seq.getChemModelCount(), 1);
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(model.getMoleculeSet().getAtomContainerCount(), 1);

        // test the molecule
        IAtomContainer mol = model.getMoleculeSet().getAtomContainer(0);
        Assertions.assertNotNull(mol);
        Assertions.assertEquals(38, mol.getAtomCount());
        Assertions.assertEquals(48, mol.getBondCount());
        Assertions.assertTrue(GeometryUtil.has3DCoordinates(mol));
        Assertions.assertFalse(GeometryUtil.has2DCoordinates(mol));
    }

    @Test
    void testCMLOK2() throws Exception {
        String filename = "cs2a.mol.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(seq.getChemModelCount(), 1);
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(model.getMoleculeSet().getAtomContainerCount(), 1);

        // test the molecule
        IAtomContainer mol = model.getMoleculeSet().getAtomContainer(0);
        Assertions.assertNotNull(mol);
        Assertions.assertEquals(38, mol.getAtomCount());
        Assertions.assertEquals(29, mol.getBondCount());
        Assertions.assertTrue(GeometryUtil.has3DCoordinates(mol));
        Assertions.assertFalse(GeometryUtil.has2DCoordinates(mol));
    }

    @Test
    void testCMLOK3() throws Exception {
        String filename = "nsc2dmol.1.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(seq.getChemModelCount(), 1);
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(model.getMoleculeSet().getAtomContainerCount(), 1);

        // test the molecule
        IAtomContainer mol = model.getMoleculeSet().getAtomContainer(0);
        Assertions.assertNotNull(mol);
        Assertions.assertEquals(13, mol.getAtomCount());
        Assertions.assertEquals(12, mol.getBondCount());
        Assertions.assertFalse(GeometryUtil.has3DCoordinates(mol));
        Assertions.assertTrue(GeometryUtil.has2DCoordinates(mol));
    }

    @Test
    void testCMLOK4() throws Exception {
        String filename = "nsc2dmol.2.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(seq.getChemModelCount(), 1);
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(model.getMoleculeSet().getAtomContainerCount(), 1);

        // test the molecule
        IAtomContainer mol = model.getMoleculeSet().getAtomContainer(0);
        Assertions.assertNotNull(mol);
        Assertions.assertEquals(13, mol.getAtomCount());
        Assertions.assertEquals(12, mol.getBondCount());
        Assertions.assertFalse(GeometryUtil.has3DCoordinates(mol));
        Assertions.assertTrue(GeometryUtil.has2DCoordinates(mol));
    }

    @Test
    void testCMLOK5() throws Exception {
        String filename = "nsc2dmol.a1.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(seq.getChemModelCount(), 1);
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(model.getMoleculeSet().getAtomContainerCount(), 1);

        // test the molecule
        IAtomContainer mol = model.getMoleculeSet().getAtomContainer(0);
        Assertions.assertNotNull(mol);
        Assertions.assertEquals(13, mol.getAtomCount());
        Assertions.assertEquals(12, mol.getBondCount());
        Assertions.assertFalse(GeometryUtil.has3DCoordinates(mol));
        Assertions.assertTrue(GeometryUtil.has2DCoordinates(mol));
    }

    @Test
    void testCMLOK6() throws Exception {
        String filename = "nsc2dmol.a2.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(seq.getChemModelCount(), 1);
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(model.getMoleculeSet().getAtomContainerCount(), 1);

        // test the molecule
        IAtomContainer mol = model.getMoleculeSet().getAtomContainer(0);
        Assertions.assertNotNull(mol);
        Assertions.assertEquals(13, mol.getAtomCount());
        Assertions.assertEquals(12, mol.getBondCount());
        Assertions.assertFalse(GeometryUtil.has3DCoordinates(mol));
        Assertions.assertTrue(GeometryUtil.has2DCoordinates(mol));
    }

    @Test
    void testCMLOK7() throws Exception {
        String filename = "nsc3dcml.xml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(seq.getChemModelCount(), 1);
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(model.getMoleculeSet().getAtomContainerCount(), 1);

        // test the molecule
        IAtomContainer mol = model.getMoleculeSet().getAtomContainer(0);
        Assertions.assertNotNull(mol);
        Assertions.assertEquals(27, mol.getAtomCount());
        Assertions.assertEquals(27, mol.getBondCount());
        Assertions.assertTrue(GeometryUtil.has3DCoordinates(mol));
        Assertions.assertFalse(GeometryUtil.has2DCoordinates(mol));
    }

    @Test
    void testCMLOK8() throws Exception {
        String filename = "nsc2dcml.xml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(seq.getChemModelCount(), 1);
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(model.getMoleculeSet().getAtomContainerCount(), 1);

        // test the molecule
        IAtomContainer mol = model.getMoleculeSet().getAtomContainer(0);
        Assertions.assertNotNull(mol);
        Assertions.assertEquals(15, mol.getAtomCount());
        Assertions.assertEquals(14, mol.getBondCount());
        Assertions.assertFalse(GeometryUtil.has3DCoordinates(mol));
        Assertions.assertTrue(GeometryUtil.has2DCoordinates(mol));
    }

    @Test
    void testCMLOK9() throws Exception {
        String filename = "nsc3dmol.1.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(seq.getChemModelCount(), 1);
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(model.getMoleculeSet().getAtomContainerCount(), 1);

        // test the molecule
        IAtomContainer mol = model.getMoleculeSet().getAtomContainer(0);
        Assertions.assertNotNull(mol);
        Assertions.assertEquals(15, mol.getAtomCount());
        Assertions.assertEquals(15, mol.getBondCount());
        Assertions.assertTrue(GeometryUtil.has3DCoordinates(mol));
        Assertions.assertFalse(GeometryUtil.has2DCoordinates(mol));
    }

    @Test
    void testCMLOK10() throws Exception {
        String filename = "nsc3dmol.2.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(seq.getChemModelCount(), 1);
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(model.getMoleculeSet().getAtomContainerCount(), 1);

        // test the molecule
        IAtomContainer mol = model.getMoleculeSet().getAtomContainer(0);
        Assertions.assertNotNull(mol);
        Assertions.assertEquals(15, mol.getAtomCount());
        Assertions.assertEquals(15, mol.getBondCount());
        Assertions.assertTrue(GeometryUtil.has3DCoordinates(mol));
        Assertions.assertFalse(GeometryUtil.has2DCoordinates(mol));
    }

    @Test
    void testCMLOK11() throws Exception {
        String filename = "nsc3dmol.a1.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(seq.getChemModelCount(), 1);
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(model.getMoleculeSet().getAtomContainerCount(), 1);

        // test the molecule
        IAtomContainer mol = model.getMoleculeSet().getAtomContainer(0);
        Assertions.assertNotNull(mol);
        Assertions.assertEquals(15, mol.getAtomCount());
        Assertions.assertEquals(15, mol.getBondCount());
        Assertions.assertTrue(GeometryUtil.has3DCoordinates(mol));
        Assertions.assertFalse(GeometryUtil.has2DCoordinates(mol));
    }

    @Test
    void testCMLOK12() throws Exception {
        String filename = "nsc3dmol.a2.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(seq.getChemModelCount(), 1);
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(model.getMoleculeSet().getAtomContainerCount(), 1);

        // test the molecule
        IAtomContainer mol = model.getMoleculeSet().getAtomContainer(0);
        Assertions.assertNotNull(mol);
        Assertions.assertEquals(15, mol.getAtomCount());
        Assertions.assertEquals(15, mol.getBondCount());
        Assertions.assertTrue(GeometryUtil.has3DCoordinates(mol));
        Assertions.assertFalse(GeometryUtil.has2DCoordinates(mol));
    }

    /**
     * This test tests whether the CMLReader is able to ignore the CMLSpect part
     * of a CML file, while extracting the molecule.
     */
    @Test
    void testCMLSpectMolExtraction() throws Exception {
        String filename = "molAndspect.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(seq.getChemModelCount(), 1);
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(model.getMoleculeSet().getAtomContainerCount(), 1);

        // test the molecule
        IAtomContainer mol = model.getMoleculeSet().getAtomContainer(0);
        Assertions.assertNotNull(mol);
        Assertions.assertEquals(17, mol.getAtomCount());
        Assertions.assertEquals(18, mol.getBondCount());
        Assertions.assertFalse(GeometryUtil.has3DCoordinates(mol));
        Assertions.assertTrue(GeometryUtil.has2DCoordinates(mol));
    }

    /**
     * This test tests whether the CMLReader is able to ignore the CMLReaction part
     * of a CML file, while extracting the reaction.
     */
    @Test
    void testCMLReaction() throws Exception {
        String filename = "reaction.2.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(seq.getChemModelCount(), 1);
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(model.getReactionSet().getReactionCount(), 1);

        // test the reaction
        IReaction reaction = model.getReactionSet().getReaction(0);
        Assertions.assertNotNull(reaction);
        Assertions.assertEquals("react", reaction.getReactants().getAtomContainer(0).getID());
        Assertions.assertEquals("product", reaction.getProducts().getAtomContainer(0).getID());
        Assertions.assertEquals("a14293164", reaction.getReactants().getAtomContainer(0).getAtom(0).getID());
        Assertions.assertEquals(6, reaction.getProducts().getAtomContainer(0).getAtomCount());
        Assertions.assertEquals(6, reaction.getReactants().getAtomContainer(0).getAtomCount());
    }

    /**
     * This test tests whether the CMLReader is able to ignore the CMLReaction part
     * of a CML file, while extracting the reaction.
     */
    @Test
    void testCMLReactionWithAgents() throws Exception {
        String filename = "reaction.1.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(seq.getChemModelCount(), 1);
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(model.getReactionSet().getReactionCount(), 1);

        // test the reaction
        IReaction reaction = model.getReactionSet().getReaction(0);
        Assertions.assertNotNull(reaction);
        Assertions.assertEquals("react", reaction.getReactants().getAtomContainer(0).getID());
        Assertions.assertEquals("product", reaction.getProducts().getAtomContainer(0).getID());
        Assertions.assertEquals("water", reaction.getAgents().getAtomContainer(0).getID());
        Assertions.assertEquals("H+", reaction.getAgents().getAtomContainer(1).getID());
        Assertions.assertEquals(6, reaction.getProducts().getAtomContainer(0).getAtomCount());
        Assertions.assertEquals(6, reaction.getReactants().getAtomContainer(0).getAtomCount());
    }

    /**
     * This test tests whether the CMLReader is able to ignore the CMLReaction part
     * of a CML file, while extracting the reaction.
     */
    @Test
    void testCMLReactionList() throws Exception {
        String filename = "reactionList.1.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(1, seq.getChemModelCount());
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(2, model.getReactionSet().getReactionCount());
        Assertions.assertEquals("1.3.2", model.getReactionSet().getReaction(0).getID());

        // test the reaction
        IReaction reaction = model.getReactionSet().getReaction(0);
        Assertions.assertNotNull(reaction);
        Assertions.assertEquals("actey", reaction.getReactants().getAtomContainer(0).getID());
        Assertions.assertEquals("a14293164", reaction.getReactants().getAtomContainer(0).getAtom(0).getID());
        Assertions.assertEquals(6, reaction.getProducts().getAtomContainer(0).getAtomCount());
        Assertions.assertEquals(6, reaction.getReactants().getAtomContainer(0).getAtomCount());
    }

    /**
     * @cdk.bug 1560486
     */
    @Test
    void testCMLWithFormula() throws Exception {
        String filename = "cmlWithFormula.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(seq.getChemModelCount(), 1);
        IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);

        IAtomContainer mol = model.getMoleculeSet().getAtomContainer(0);
        Assertions.assertNotNull(mol);
        Assertions.assertEquals("a", mol.getID());
        Assertions.assertEquals("a1", mol.getAtom(0).getID());
        Assertions.assertEquals(27, mol.getAtomCount());
        Assertions.assertEquals(32, mol.getBondCount());
    }

    /**
     * Only Molecule with concise MolecularFormula
     */
    @Test
    void testCMLConciseFormula() throws Exception {
        String filename = "cmlConciseFormula.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(seq.getChemModelCount(), 1);
        IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);

        IAtomContainer mol = model.getMoleculeSet().getAtomContainer(0);
        Assertions.assertNotNull(mol);

        // FIXME: REACT: It should return two different formulas
        Assertions.assertEquals("[C 18 H 21 Cl 2 Mn 1 N 5 O 1]", mol.getProperty(CDKConstants.FORMULA).toString());
    }

    /**
     * Only Molecule with concise MolecularFormula
     */
    @Test
    void testCMLConciseFormula2() throws Exception {
        String filename = "cmlConciseFormula2.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(seq.getChemModelCount(), 1);
        IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);

        IAtomContainer mol = model.getMoleculeSet().getAtomContainer(0);
        Assertions.assertNotNull(mol);

        // FIXME: REACT: It should return two different formulas
        Assertions.assertEquals("[C 18 H 21 Cl 2 Mn 1 N 5 O 1, C 4 H 10]", mol.getProperty(CDKConstants.FORMULA).toString());
    }

    /**
     * This test tests whether the CMLReader is able to ignore the CMLReaction part
     * of a CML file, while extracting the reaction.
     */
    @Test
    void testCMLScheme1() throws Exception {
        String filename = "reactionScheme.1.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(1, seq.getChemModelCount());
        IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);

        // test reaction
        Assertions.assertEquals(4, model.getReactionSet().getReactionCount());
        String[] idReaction = {"r1", "r2", "r3", "r4"};
        String[] idReactants = {"A", "B", "A", "F"};
        String[] idProducts = {"B", "C", "F", "G"};
        for (int i = 0; i < idReaction.length; i++) {
            IReaction reaction = model.getReactionSet().getReaction(i);
            Assertions.assertEquals(idReaction[i], reaction.getID());
            // test molecule
            Assertions.assertEquals(1, reaction.getProducts().getAtomContainerCount());
            Assertions.assertEquals(idProducts[i], reaction.getProducts().getAtomContainer(0).getID());

            Assertions.assertEquals(1, reaction.getReactants().getAtomContainerCount());
            Assertions.assertEquals(idReactants[i], reaction.getReactants().getAtomContainer(0).getID());
        }
    }

    /**
     * This test tests whether the CMLReader is able to ignore the CMLReaction part
     * of a CML file, while extracting the reaction.
     */
    @Test
    void testCMLScheme2() throws Exception {
        String filename = "reactionScheme.2.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(1, seq.getChemModelCount());
        IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);

        // test reaction
        Assertions.assertEquals(2, model.getReactionSet().getReactionCount());
        String[] idReaction = {"r1", "r2"};
        String[] idReactants = {"A", "B"};
        String[] idProducts = {"B", "C"};
        for (int i = 0; i < idReaction.length; i++) {
            IReaction reaction = model.getReactionSet().getReaction(i);
            Assertions.assertEquals(idReaction[i], reaction.getID());
            // test molecule
            Assertions.assertEquals(1, reaction.getProducts().getAtomContainerCount());
            Assertions.assertEquals(idProducts[i], reaction.getProducts().getAtomContainer(0).getID());

            Assertions.assertEquals(1, reaction.getReactants().getAtomContainerCount());
            Assertions.assertEquals(idReactants[i], reaction.getReactants().getAtomContainer(0).getID());
        }
    }

    /**
     * This test tests whether the CMLReader is able to ignore the CMLReaction part
     * of a CML file, while extracting the reaction.
     */
    @Test
    void testCMLSchemeStepList1() throws Exception {
        String filename = "reactionSchemeStepList.1.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(1, seq.getChemModelCount());
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);

        // test reaction
        Assertions.assertEquals(4, model.getReactionSet().getReactionCount());
        String[] idReaction = {"r1.1", "r1.2", "r2.1", "r2.2"};
        String[] idReactants = {"A", "B", "A", "D"};
        String[] idProducts = {"B", "C", "D", "E"};
        for (int i = 0; i < idReaction.length; i++) {
            IReaction reaction = model.getReactionSet().getReaction(i);
            Assertions.assertEquals(idReaction[i], reaction.getID());
            // test molecule
            Assertions.assertEquals(1, reaction.getProducts().getAtomContainerCount());
            Assertions.assertEquals(idProducts[i], reaction.getProducts().getAtomContainer(0).getID());

            Assertions.assertEquals(1, reaction.getReactants().getAtomContainerCount());
            Assertions.assertEquals(idReactants[i], reaction.getReactants().getAtomContainer(0).getID());
        }

    }

    /**
     * This test tests whether the CMLReader is able to ignore the CMLReaction part
     * of a CML file, while extracting the reaction.
     */
    @Test
    void testCMLStepList() throws Exception {
        String filename = "reactionStepList.1.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(1, seq.getChemModelCount());
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);

        // test reaction
        Assertions.assertEquals(3, model.getReactionSet().getReactionCount());
        String[] idReaction = {"r1", "r2", "r3"};
        String[] idReactants = {"A", "B", "C"};
        String[] idProducts = {"B", "C", "D"};
        for (int i = 0; i < idReaction.length; i++) {
            IReaction reaction = model.getReactionSet().getReaction(i);
            Assertions.assertEquals(idReaction[i], reaction.getID());
            // test molecule
            Assertions.assertEquals(1, reaction.getProducts().getAtomContainerCount());
            Assertions.assertEquals(idProducts[i], reaction.getProducts().getAtomContainer(0).getID());

            Assertions.assertEquals(1, reaction.getReactants().getAtomContainerCount());
            Assertions.assertEquals(idReactants[i], reaction.getReactants().getAtomContainer(0).getID());
        }

    }

    /**
     * This test tests whether the CMLReader is able to read a reactionscheme object with
     * references to list of molecules.
     */
    @Test
    void testCMLSchemeMoleculeSet() throws Exception {
        String filename = "reactionSchemeMoleculeSet.cml";
        logger.info("Testing: " + filename);
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = reader.read(new ChemFile());
        reader.close();

        // test the resulting ChemFile content
        Assertions.assertNotNull(chemFile);
        Assertions.assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        Assertions.assertNotNull(seq);
        Assertions.assertEquals(1, seq.getChemModelCount());
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        Assertions.assertNotNull(model);

        // test reaction
        Assertions.assertEquals(1, model.getReactionSet().getReactionCount());
        String[] idReaction = {"react_1"};
        String[] idReactants = {"A"};
        String[] idProducts = {"B", "C"};

        IReaction reaction = model.getReactionSet().getReaction(0);
        Assertions.assertEquals(idReaction[0], reaction.getID());
        // test molecule
        Assertions.assertEquals(2, reaction.getProducts().getAtomContainerCount());
        Assertions.assertEquals(idProducts[0], reaction.getProducts().getAtomContainer(0).getID());
        Assertions.assertEquals("C 9 H 20 N 1", ((ArrayList<String>) reaction.getProducts().getAtomContainer(0)
                                                                             .getProperty(CDKConstants.FORMULA)).get(0));
        Assertions.assertEquals(idProducts[1], reaction.getProducts().getAtomContainer(1).getID());

        Assertions.assertEquals(1, reaction.getReactants().getAtomContainerCount());
        Assertions.assertEquals(idReactants[0], reaction.getReactants().getAtomContainer(0).getID());
        Assertions.assertEquals("C 28 H 60 N 1", ((ArrayList<String>) reaction.getReactants().getAtomContainer(0)
                                                                              .getProperty(CDKConstants.FORMULA)).get(0));
    }

    /**
     * @cdk.bug 2697568
     */
    @Test
    void testReadReactionWithPointersToMoleculeSet() throws Exception {
        String filename = "AlanineTree.cml";
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = new ChemFile();
        chemFile = reader.read(chemFile);
        reader.close();
        Assertions.assertSame(chemFile.getChemSequence(0).getChemModel(0).getMoleculeSet().getAtomContainer(0), chemFile
                .getChemSequence(0).getChemModel(0).getReactionSet().getReaction(0).getReactants().getAtomContainer(0));
    }

    /**
     * @cdk.bug 2697568
     */
    @Test
    void testBug2697568() throws Exception {
        String filename = "AlanineTreeReverse.cml";
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = new ChemFile();
        chemFile = reader.read(chemFile);
        reader.close();
        Assertions.assertSame(chemFile.getChemSequence(0).getChemModel(0).getMoleculeSet().getAtomContainer(0), chemFile
                .getChemSequence(0).getChemModel(0).getReactionSet().getReaction(0).getReactants().getAtomContainer(0));
    }

    /**
     */
    @Test
    void testReactionProperties() throws Exception {
        String filename = "reaction.2.cml";
        InputStream ins = this.getClass().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = new ChemFile();
        chemFile = reader.read(chemFile);
        reader.close();
        IReaction reaction = chemFile.getChemSequence(0).getChemModel(0).getReactionSet().getReaction(0);

        Assertions.assertEquals("3", reaction.getProperty("Ka"));
    }
}
