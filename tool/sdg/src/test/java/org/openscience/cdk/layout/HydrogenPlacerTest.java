/* Copyright (C) 2003-2007  The Chemistry Development Kit (CDK) project
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
 */
package org.openscience.cdk.layout;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.Atom;
import org.openscience.cdk.Bond;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.test.CDKTestCase;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.geometry.GeometryUtil;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.IChemObjectReader.Mode;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

import javax.vecmath.Point2d;
import java.io.InputStream;

class HydrogenPlacerTest extends CDKTestCase {

    public boolean       standAlone = false;
    private final ILoggingTool logger     = LoggingToolFactory.createLoggingTool(HydrogenPlacerTest.class);

    @Test
    void testAtomWithoutCoordinates() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            HydrogenPlacer hydrogenPlacer = new HydrogenPlacer();
            hydrogenPlacer.placeHydrogens2D(DefaultChemObjectBuilder.getInstance().newAtomContainer(), new Atom(), 1.5);
        });
    }

    @Test
    void testNullContainer() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            HydrogenPlacer hydrogenPlacer = new HydrogenPlacer();
            hydrogenPlacer.placeHydrogens2D(null, new Atom(), 1.5);
        });
    }

    @Test
    void testNoConnections() {
        HydrogenPlacer hydrogenPlacer = new HydrogenPlacer();
        IAtomContainer  container     = DefaultChemObjectBuilder.getInstance().newAtomContainer();
        Atom           atom         = new Atom("C", new Point2d(0, 0));
        container.addAtom(atom);
        hydrogenPlacer.placeHydrogens2D(container, atom, 1.5);
    }

    /** @cdk.bug 1269 */
    @Test
    void testH2() {
        HydrogenPlacer hydrogenPlacer = new HydrogenPlacer();

        // h1 has no coordinates
        IAtom h1 = new Atom("H");
        IAtom h2 = new Atom("H", new Point2d(0, 0));
        IAtomContainer m = DefaultChemObjectBuilder.getInstance().newAtomContainer();
        m.addAtom(h1);
        m.addAtom(h2);
        m.addBond(new Bond(h1, h2));
        hydrogenPlacer.placeHydrogens2D(m, 1.5);
        Assertions.assertNotNull(h1.getPoint2d());
    }

    @Test
    void unplacedNonHydrogen() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            HydrogenPlacer hydrogenPlacer = new HydrogenPlacer();

            // c2 is unplaced
            IAtom c1 = new Atom("C", new Point2d(0, 0));
            IAtom c2 = new Atom("C");
            IAtomContainer m = DefaultChemObjectBuilder.getInstance().newAtomContainer();
            m.addAtom(c1);
            m.addAtom(c2);
            m.addBond(new Bond(c1, c2));
            hydrogenPlacer.placeHydrogens2D(m, 1.5);
        });
    }

    /** @cdk.bug 933572 */
    @Test
    void testBug933572() throws Exception {
        IAtomContainer ac = DefaultChemObjectBuilder.getInstance().newAtomContainer();
        ac.addAtom(new Atom("H"));
        ac.getAtom(0).setPoint2d(new Point2d(0, 0));
        addExplicitHydrogens(ac);
        HydrogenPlacer hPlacer = new HydrogenPlacer();
        hPlacer.placeHydrogens2D(ac, 36);
        for (int i = 0; i < ac.getAtomCount(); i++) {
            Assertions.assertNotNull(ac.getAtom(i).getPoint2d());
        }
    }

    @Test
    void testPlaceHydrogens2D() throws Exception {
        HydrogenPlacer hydrogenPlacer = new HydrogenPlacer();
        IAtomContainer dichloromethane = DefaultChemObjectBuilder.getInstance().newAtomContainer();
        Atom carbon = new Atom("C");
        Point2d carbonPos = new Point2d(0.0, 0.0);
        carbon.setPoint2d(carbonPos);
        Atom h1 = new Atom("H");
        Atom h2 = new Atom("H");
        Atom cl1 = new Atom("Cl");
        Point2d cl1Pos = new Point2d(0.0, -1.0);
        cl1.setPoint2d(cl1Pos);
        Atom cl2 = new Atom("Cl");
        Point2d cl2Pos = new Point2d(-1.0, 0.0);
        cl2.setPoint2d(cl2Pos);
        dichloromethane.addAtom(carbon);
        dichloromethane.addAtom(h1);
        dichloromethane.addAtom(h2);
        dichloromethane.addAtom(cl1);
        dichloromethane.addAtom(cl2);
        dichloromethane.addBond(new Bond(carbon, h1));
        dichloromethane.addBond(new Bond(carbon, h2));
        dichloromethane.addBond(new Bond(carbon, cl1));
        dichloromethane.addBond(new Bond(carbon, cl2));

        Assertions.assertNull(h1.getPoint2d());
        Assertions.assertNull(h2.getPoint2d());

        // generate new coords
        hydrogenPlacer.placeHydrogens2D(dichloromethane, carbon);
        // check that previously set coordinates are kept
        assertEquals(carbonPos, carbon.getPoint2d(), 0.01);
        assertEquals(cl1Pos, cl1.getPoint2d(), 0.01);
        assertEquals(cl2Pos, cl2.getPoint2d(), 0.01);
        Assertions.assertNotNull(h1.getPoint2d());
        Assertions.assertNotNull(h2.getPoint2d());
    }

    /*
     * This one tests adding hydrogens to all atoms of a molecule and doing the
     * layout for them. It is intended for visually checking the work of
     * HydrogenPlacer, not to be run as a JUnit test. Thus the name without
     * "test".
     */
    void visualFullMolecule2DEvaluation() throws Exception {
        HydrogenPlacer hydrogenPlacer = new HydrogenPlacer();
        String filename = "data/mdl/reserpine.mol";
        InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
        MDLReader reader = new MDLReader(ins, Mode.STRICT);
        ChemFile chemFile = (ChemFile) reader.read((ChemObject) new ChemFile());
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        IAtomContainer mol = model.getMoleculeSet().getAtomContainer(0);
        double bondLength = GeometryUtil.getBondLengthAverage(mol);
        logger.debug("Read Reserpine");
        logger.debug("Starting addition of H's");
        addExplicitHydrogens(mol);
        logger.debug("ended addition of H's");
        hydrogenPlacer.placeHydrogens2D(mol, bondLength);
    }

}
