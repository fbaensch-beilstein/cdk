/* Copyright (C) 2004-2007  Miguel Rojas <miguel.rojas@uni-koeln.de>
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
package org.openscience.cdk.reaction.type;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.Atom;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.SingleElectron;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.reaction.IReactionProcess;
import org.openscience.cdk.reaction.ReactionProcessTest;
import org.openscience.cdk.reaction.type.parameters.IParameterReact;
import org.openscience.cdk.reaction.type.parameters.SetReactionCenter;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.LoggingToolFactory;
import org.openscience.cdk.tools.manipulator.ReactionManipulator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TestSuite that runs a test for the RadicalSiteInitiationHReactionTest.
 * Generalized Reaction: [A*]-B-H => A=B + [H*].
 *
 */
public class RadicalSiteInitiationHReactionTest extends ReactionProcessTest {

    private final IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();

    /**
     *  The JUnit setup method
     */
    RadicalSiteInitiationHReactionTest() throws Exception {
        setReaction(RadicalSiteInitiationHReaction.class);
    }

    /**
     *  The JUnit setup method
     */
    @Test
    void testRadicalSiteInitiationHReaction() throws Exception {
        IReactionProcess type = new RadicalSiteInitiationHReaction();
        Assertions.assertNotNull(type);
    }

    /**
     * A unit test suite for JUnit. Reaction: [C*]([H])([H])C([H])([H])[H] => C=C +[H*]
     * Automatic search of the center active.
     *
     *
     */
    @Test
    @Override
    public void testInitiate_IAtomContainerSet_IAtomContainerSet() throws Exception {
        IReactionProcess type = new RadicalSiteInitiationHReaction();
        IAtomContainerSet setOfReactants = getExampleReactants();
        IAtomContainer molecule = setOfReactants.getAtomContainer(0);

        /* initiate */
        makeSureAtomTypesAreRecognized(molecule);

        List<IParameterReact> paramList = new ArrayList<>();
        IParameterReact param = new SetReactionCenter();
        param.setParameter(Boolean.FALSE);
        paramList.add(param);
        type.setParameterList(paramList);
        IReactionSet setOfReactions = type.initiate(setOfReactants, null);

        Assertions.assertEquals(3, setOfReactions.getReactionCount());
        Assertions.assertEquals(2, setOfReactions.getReaction(0).getProductCount());

        IAtomContainer product1 = setOfReactions.getReaction(0).getProducts().getAtomContainer(0);

        /* C=C */
        IAtomContainer molecule1 = getExpectedProducts().getAtomContainer(0);

        assertEquals(molecule1, product1);

        IAtomContainer product2 = setOfReactions.getReaction(0).getProducts().getAtomContainer(1);

        /* [H*] */
        IAtomContainer molecule2 = getExpectedProducts().getAtomContainer(1);

        assertEquals(molecule2, product2);
    }

    /**
     * A unit test suite for JUnit. Reaction: [C*]([H])([H])C([H])([H])[H] => C=C +[H*]
     * Automatic search of the center active.
     *
     *
     */
    @Test
    void testManuallyCentreActive() throws Exception {
        IReactionProcess type = new RadicalSiteInitiationHReaction();
        IAtomContainerSet setOfReactants = getExampleReactants();
        IAtomContainer molecule = setOfReactants.getAtomContainer(0);

        /* initiate */
        makeSureAtomTypesAreRecognized(molecule);

        /* manually put the reactive center */
        molecule.getAtom(0).setFlag(IChemObject.REACTIVE_CENTER, true);
        molecule.getAtom(3).setFlag(IChemObject.REACTIVE_CENTER, true);
        molecule.getAtom(4).setFlag(IChemObject.REACTIVE_CENTER, true);
        molecule.getBond(2).setFlag(IChemObject.REACTIVE_CENTER, true);
        molecule.getBond(3).setFlag(IChemObject.REACTIVE_CENTER, true);

        List<IParameterReact> paramList = new ArrayList<>();
        IParameterReact param = new SetReactionCenter();
        param.setParameter(Boolean.TRUE);
        paramList.add(param);
        type.setParameterList(paramList);
        IReactionSet setOfReactions = type.initiate(setOfReactants, null);

        Assertions.assertEquals(1, setOfReactions.getReactionCount());
        Assertions.assertEquals(2, setOfReactions.getReaction(0).getProductCount());

        IAtomContainer product1 = setOfReactions.getReaction(0).getProducts().getAtomContainer(0);

        /* C=C */
        IAtomContainer molecule1 = getExpectedProducts().getAtomContainer(0);

        assertEquals(molecule1, product1);

        IAtomContainer product2 = setOfReactions.getReaction(0).getProducts().getAtomContainer(1);

        /* [H*] */
        IAtomContainer molecule2 = getExpectedProducts().getAtomContainer(1);

        assertEquals(molecule2, product2);
    }

    /**
     * A unit test suite for JUnit.
     *
     *
     */
    @Test
    void testCDKConstants_REACTIVE_CENTER() throws Exception {
        IReactionProcess type = new RadicalSiteInitiationHReaction();
        IAtomContainerSet setOfReactants = getExampleReactants();
        IAtomContainer molecule = setOfReactants.getAtomContainer(0);

        /* initiate */
        makeSureAtomTypesAreRecognized(molecule);

        /* manually put the reactive center */
        molecule.getAtom(0).setFlag(IChemObject.REACTIVE_CENTER, true);
        molecule.getAtom(3).setFlag(IChemObject.REACTIVE_CENTER, true);
        molecule.getAtom(4).setFlag(IChemObject.REACTIVE_CENTER, true);
        molecule.getBond(2).setFlag(IChemObject.REACTIVE_CENTER, true);
        molecule.getBond(3).setFlag(IChemObject.REACTIVE_CENTER, true);

        List<IParameterReact> paramList = new ArrayList<>();
        IParameterReact param = new SetReactionCenter();
        param.setParameter(Boolean.TRUE);
        paramList.add(param);
        type.setParameterList(paramList);
        IReactionSet setOfReactions = type.initiate(setOfReactants, null);

        IAtomContainer reactant = setOfReactions.getReaction(0).getReactants().getAtomContainer(0);
        Assertions.assertTrue(molecule.getAtom(0).getFlag(IChemObject.REACTIVE_CENTER));
        Assertions.assertTrue(reactant.getAtom(0).getFlag(IChemObject.REACTIVE_CENTER));
        Assertions.assertTrue(molecule.getAtom(3).getFlag(IChemObject.REACTIVE_CENTER));
        Assertions.assertTrue(reactant.getAtom(3).getFlag(IChemObject.REACTIVE_CENTER));
        Assertions.assertTrue(molecule.getAtom(4).getFlag(IChemObject.REACTIVE_CENTER));
        Assertions.assertTrue(reactant.getAtom(4).getFlag(IChemObject.REACTIVE_CENTER));
        Assertions.assertTrue(molecule.getBond(2).getFlag(IChemObject.REACTIVE_CENTER));
        Assertions.assertTrue(reactant.getBond(2).getFlag(IChemObject.REACTIVE_CENTER));
        Assertions.assertTrue(molecule.getBond(3).getFlag(IChemObject.REACTIVE_CENTER));
        Assertions.assertTrue(reactant.getBond(3).getFlag(IChemObject.REACTIVE_CENTER));
    }

    /**
     * A unit test suite for JUnit.
     *
     *
     */
    @Test
    void testMapping() throws Exception {
        IReactionProcess type = new RadicalSiteInitiationHReaction();
        IAtomContainerSet setOfReactants = getExampleReactants();
        IAtomContainer molecule = setOfReactants.getAtomContainer(0);
        /* initiate */

        /* manually put the reactive center */
        molecule.getAtom(0).setFlag(IChemObject.REACTIVE_CENTER, true);
        molecule.getAtom(3).setFlag(IChemObject.REACTIVE_CENTER, true);
        molecule.getAtom(4).setFlag(IChemObject.REACTIVE_CENTER, true);
        molecule.getBond(2).setFlag(IChemObject.REACTIVE_CENTER, true);
        molecule.getBond(3).setFlag(IChemObject.REACTIVE_CENTER, true);

        List<IParameterReact> paramList = new ArrayList<>();
        IParameterReact param = new SetReactionCenter();
        param.setParameter(Boolean.TRUE);
        paramList.add(param);
        type.setParameterList(paramList);
        IReactionSet setOfReactions = type.initiate(setOfReactants, null);

        IAtomContainer product = setOfReactions.getReaction(0).getProducts().getAtomContainer(0);

        Assertions.assertEquals(7, setOfReactions.getReaction(0).getMappingCount());

        IAtom mappedProductA1 = (IAtom) ReactionManipulator.getMappedChemObject(setOfReactions.getReaction(0),
                molecule.getAtom(0));
        Assertions.assertEquals(mappedProductA1, product.getAtom(0));
        IAtom mappedProductA2 = (IAtom) ReactionManipulator.getMappedChemObject(setOfReactions.getReaction(0),
                molecule.getAtom(0));
        Assertions.assertEquals(mappedProductA2, product.getAtom(0));
        IAtom mappedProductA3 = (IAtom) ReactionManipulator.getMappedChemObject(setOfReactions.getReaction(0),
                molecule.getAtom(3));
        Assertions.assertEquals(mappedProductA3, product.getAtom(3));

    }

    /**
     * Get the AtomContainer
     *
     * @return The IAtomContainerSet
     */
    private IAtomContainerSet getExampleReactants() {
        IAtomContainerSet setOfReactants = DefaultChemObjectBuilder.getInstance().newInstance(IAtomContainerSet.class);

        IAtomContainer molecule = builder.newInstance(IAtomContainer.class);
        molecule.addAtom(builder.newInstance(IAtom.class, "C"));
        molecule.addAtom(builder.newInstance(IAtom.class, "H"));
        molecule.addBond(0, 1, IBond.Order.SINGLE);
        molecule.addAtom(builder.newInstance(IAtom.class, "H"));
        molecule.addBond(0, 2, IBond.Order.SINGLE);
        molecule.addAtom(builder.newInstance(IAtom.class, "C"));
        molecule.addBond(0, 3, IBond.Order.SINGLE);
        molecule.addAtom(builder.newInstance(IAtom.class, "H"));
        molecule.addBond(3, 4, IBond.Order.SINGLE);
        molecule.addAtom(builder.newInstance(IAtom.class, "H"));
        molecule.addBond(3, 5, IBond.Order.SINGLE);
        molecule.addAtom(builder.newInstance(IAtom.class, "H"));
        molecule.addBond(3, 6, IBond.Order.SINGLE);

        IAtom atom = molecule.getAtom(0);
        molecule.addSingleElectron(new SingleElectron(atom));
        try {
            makeSureAtomTypesAreRecognized(molecule);
        } catch (CDKException e) {
            LoggingToolFactory.createLoggingTool(getClass())
                              .error("Unexpected Error:", e);
        }
        try {
            addExplicitHydrogens(molecule);
        } catch (Exception e) {
            // ignored
        }
        setOfReactants.addAtomContainer(molecule);
        return setOfReactants;
    }

    /**
     * Get the expected set of molecules.
     *
     * @return The IAtomContainerSet
     */
    private IAtomContainerSet getExpectedProducts() {
        IAtomContainerSet setOfProducts = builder.newInstance(IAtomContainerSet.class);

        IAtomContainer molecule1 = builder.newInstance(IAtomContainer.class);
        molecule1.addAtom(builder.newInstance(IAtom.class, "C"));
        molecule1.addAtom(builder.newInstance(IAtom.class, "H"));
        molecule1.addBond(0, 1, IBond.Order.SINGLE);
        molecule1.addAtom(builder.newInstance(IAtom.class, "H"));
        molecule1.addBond(0, 2, IBond.Order.SINGLE);
        molecule1.addAtom(builder.newInstance(IAtom.class, "C"));
        molecule1.addBond(0, 3, IBond.Order.DOUBLE);
        molecule1.addAtom(builder.newInstance(IAtom.class, "H"));
        molecule1.addBond(3, 4, IBond.Order.SINGLE);
        molecule1.addAtom(builder.newInstance(IAtom.class, "H"));
        molecule1.addBond(3, 5, IBond.Order.SINGLE);

        /* [H*] */
        IAtomContainer molecule2 = builder.newInstance(IAtomContainer.class);
        molecule2.addAtom(new Atom("H"));
        molecule2.addSingleElectron(new SingleElectron(molecule2.getAtom(0)));

        try {
            addExplicitHydrogens(molecule1);
            addExplicitHydrogens(molecule2);
        } catch (Exception e) {
            // ignored
        }

        setOfProducts.addAtomContainer(molecule1);
        setOfProducts.addAtomContainer(molecule2);
        return setOfProducts;
    }

    /**
     * Test to recognize if a IAtomContainer matcher correctly identifies the CDKAtomTypes.
     *
     * @param molecule          The IAtomContainer to analyze
     * @throws CDKException
     */
    private void makeSureAtomTypesAreRecognized(IAtomContainer molecule) throws CDKException {

        Iterator<IAtom> atoms = molecule.atoms().iterator();
        CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(molecule.getBuilder());
        while (atoms.hasNext()) {
            IAtom nextAtom = atoms.next();
            Assertions.assertNotNull(matcher.findMatchingAtomType(molecule, nextAtom), "Missing atom type for: " + nextAtom);
        }
    }
}
