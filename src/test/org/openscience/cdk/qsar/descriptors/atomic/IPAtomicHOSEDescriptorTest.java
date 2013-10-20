/* $Revision$ $Author$ $Date$
 * 
 * Copyright (C) 2006-2007  Miguel Rojas <miguel.rojas@uni-koeln.de>
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
package org.openscience.cdk.qsar.descriptors.atomic;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.IAtomicDescriptor;
import org.openscience.cdk.qsar.result.DoubleResult;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.LonePairElectronChecker;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

/**
 * TestSuite that runs all QSAR tests.
 *
 * @cdk.module test-qsaratomic
 */
public class IPAtomicHOSEDescriptorTest extends AtomicDescriptorTest {
	IPAtomicHOSEDescriptor descriptor;
	private SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
	LonePairElectronChecker lpcheck = new LonePairElectronChecker();
	/**
	 *  Constructor for the IPAtomicHOSEDescriptorTest object
	 *
	 */
    public  IPAtomicHOSEDescriptorTest() {
    	descriptor = new IPAtomicHOSEDescriptor();
    }
    
    @Before
    public void setUp() throws Exception {
    	setDescriptor(IPAtomicHOSEDescriptor.class);
    }

    /**
	 *  A unit test for JUnit
	 */
	@Test
    public void testIPAtomicHOSEDescriptor() throws Exception {
		IAtomicDescriptor descriptor = new IPAtomicHOSEDescriptor();
		Assert.assertNotNull(descriptor);
	}
	/**
	 *  A unit test for JUnit with CCCCl
	 *  
	 *  @cdk.inchi InChI=1/C3H7Cl/c1-2-3-4/h2-3H2,1H3
	 */
    @Test
    public void testIPDescriptor1() throws Exception{
        
		IAtomContainer mol = sp.parseSmiles("CCCCl");
		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
		addExplicitHydrogens(mol);
		lpcheck.saturate(mol);
		
        double result= ((DoubleResult)descriptor.calculate(mol.getAtom(3),mol).getValue()).doubleValue();
        double resultAccordingNIST = 10.8; 
        
        Assert.assertEquals(resultAccordingNIST, result, 0.00001);
    }
    /**
	 *  A unit test for JUnit with CC(C)Cl
	 *  
	 *  @cdk.inchi InChI=1/C3H7Cl/c1-3(2)4/h3H,1-2H3
	 */
    @Test
    public void testIPDescriptor2() throws Exception{
        
		IAtomContainer mol = sp.parseSmiles("CC(CC)Cl"); // not in db
		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
		addExplicitHydrogens(mol);
		lpcheck.saturate(mol);
		
        double result= ((DoubleResult)descriptor.calculate(mol.getAtom(4),mol).getValue()).doubleValue();
        double resultAccordingNIST = 10.57; //value for CC(C)Cl 
        
        Assert.assertEquals(resultAccordingNIST, result, 0.00001);
    }

    /**
	 *  A unit test for JUnit with C=CCCl
	 *  
	 *  @cdk.inchi InChI=1/C3H5Cl/c1-2-3-4/h2H,1,3H2
	 */
    @Test
    public void testNotDB() throws Exception{
        
		IAtomContainer mol = sp.parseSmiles("C=CCCl"); // not in db
		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
		addExplicitHydrogens(mol);
		lpcheck.saturate(mol);
		
        double result= ((DoubleResult)descriptor.calculate(mol.getAtom(3),mol).getValue()).doubleValue();
        double resultAccordingNIST = 10.8; //value for CCCCl aprox. 
        
        Assert.assertEquals(resultAccordingNIST, result, 0.00001);
    }
    /**
	 *  A unit test for JUnit with C-Cl
	 *  
	 *  @cdk.inchi InChI=1/CH3F/c1-2/h1H3
	 */
    @Test
    public void testIPDescriptor_1() throws Exception{
        
		IAtomContainer mol = sp.parseSmiles("C-Cl");
		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
		addExplicitHydrogens(mol);
		lpcheck.saturate(mol);
		
        double result= ((DoubleResult)descriptor.calculate(mol.getAtom(1),mol).getValue()).doubleValue();
        double resultAccordingNIST = 11.26; 
        
        Assert.assertEquals(resultAccordingNIST, result, 0.42);
    }
    /**
	 *  A unit test for JUnit with C-C-Br
	 *  
	 */
    @Test
    public void testIPDescriptor_2() throws Exception{
        
		IAtomContainer mol = sp.parseSmiles("C-C-Br");
		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
		addExplicitHydrogens(mol);
		lpcheck.saturate(mol);
		
        double result= ((DoubleResult)descriptor.calculate(mol.getAtom(2),mol).getValue()).doubleValue();
        double resultAccordingNIST = 11.29; 

        Assert.assertEquals(resultAccordingNIST, result, 1.95);
    }
    /**
	 *  A unit test for JUnit with C-C-C-I
	 *  
	 */
    @Test
    public void testIPDescriptor_3() throws Exception{
        
		IAtomContainer mol = sp.parseSmiles("C-C-C-I");
		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
		addExplicitHydrogens(mol);
		lpcheck.saturate(mol);
		
        double result= ((DoubleResult)descriptor.calculate(mol.getAtom(3), mol).getValue()).doubleValue();
        double resultAccordingNIST = 9.27;

        Assert.assertEquals(resultAccordingNIST, result, 0.02);
    }
//    /**
//	 *  A unit test for JUnit with C-C-O
//	 *  
//	 *  @cdk.inchi InChI=1/C2H6O/c1-2-3/h3H,2H2,1H3
//	 */
//    @Test
//    public void testIPDescriptor_4() throws Exception{
//        
//		IAtomContainer mol = sp.parseSmiles("C-C-O");
//
//		addExplicitHydrogens(mol);
//		
//		LonePairElectronChecker lpcheck = new LonePairElectronChecker();
//		lpcheck.saturate(mol);
//		
//        double result= ((DoubleResult)descriptor.calculate(mol.getAtom(2), mol).getValue()).doubleValue();
//        double resultAccordingNIST = 10.48;
//
//        Assert.assertEquals(resultAccordingNIST, result, 1.24);
//    }
//    
//    /**
//	 *  A unit test for JUnit with N1(C)CCC(C)(C)CC1
//	 *  
//	 */
//    @Test
//    public void testIPDescriptor_5() throws Exception{
//
//    	IAtomContainer mol = sp.parseSmiles("N1(C)CCC(C)(C)CC1");
//
//		addExplicitHydrogens(mol);
//		
//		LonePairElectronChecker lpcheck = new LonePairElectronChecker();
//		lpcheck.saturate(mol);
//		
//        double result= ((DoubleResult)descriptor.calculate(mol.getAtom(0), mol).getValue()).doubleValue();
//        double resultAccordingNIST = 7.77;
//
//        Assert.assertEquals(resultAccordingNIST, result, 0.02);
//    }
//    /**
//	 *  A unit test for JUnit with C-N-C
//	 *  
//	 *  @cdk.inchi InChI=1/C2H7N/c1-3-2/h3H,1-2H3
//	 */
//    @Test
//    public void testIPDescriptor_6() throws Exception{
//
//    	IAtomContainer mol = sp.parseSmiles("C-N-C");
//
//		addExplicitHydrogens(mol);
//		
//		LonePairElectronChecker lpcheck = new LonePairElectronChecker();
//		lpcheck.saturate(mol);
//		
//        double result= ((DoubleResult)descriptor.calculate(mol.getAtom(1),mol).getValue()).doubleValue();
//        double resultAccordingNIST = 8.24; 
//
//        Assert.assertEquals(resultAccordingNIST, result, 0.09);
//    }
//    /**
//	 *  A unit test for JUnit with C-C-N
//	 *  
//	 *  @cdk.inchi InChI=1/C2H7N/c1-2-3/h2-3H2,1H3
//	 */
//    @Test
//    public void testIPDescriptor_7() throws Exception{
//        
//		IAtomContainer mol = sp.parseSmiles("C-C-N");
//
//		addExplicitHydrogens(mol);
//		
//		LonePairElectronChecker lpcheck = new LonePairElectronChecker();
//		lpcheck.saturate(mol);
//		
//        double result= ((DoubleResult)descriptor.calculate(mol.getAtom(2),mol).getValue()).doubleValue();
//        double resultAccordingNIST = 8.9; 
//
//        Assert.assertEquals(resultAccordingNIST, result, 0.35);
//    }
//    /**
//	 *  A unit test for JUnit with C-C-P-C-C
//	 *  
//	 *  @cdk.inchi InChI=1/C4H11P/c1-3-5-4-2/h5H,3-4H2,1-2H3
//	 */
//    @Test
//    public void testIPDescriptor_8() throws Exception{
//
//    	IAtomContainer mol = sp.parseSmiles("C-C-P-C-C");
//
//		addExplicitHydrogens(mol);
//		
//		LonePairElectronChecker lpcheck = new LonePairElectronChecker();
//		lpcheck.saturate(mol);
//		
//        double result= ((DoubleResult)descriptor.calculate(mol.getAtom(2), mol).getValue()).doubleValue();
//        double resultAccordingNIST = 8.5; 
//
//        Assert.assertEquals(resultAccordingNIST, result, 0.051);
//    }
//
//    /**
//	 *  A unit test for JUnit with O=C(C)CC(C)C
//	 *  
//	 *  @cdk.inchi InChI=1/C6H12O/c1-5(2)4-6(3)7/h5H,4H2,1-3H3
//	 */
//    @Test
//    public void testIPDescriptor_9() throws Exception{
//
//    	IAtomContainer mol = sp.parseSmiles("O=C(C)CC(C)C");
//
//		addExplicitHydrogens(mol);
//		
//		LonePairElectronChecker lpcheck = new LonePairElectronChecker();
//		lpcheck.saturate(mol);
//		
//        double result= ((DoubleResult)descriptor.calculate(mol.getAtom(0), mol).getValue()).doubleValue();
//        double resultAccordingNIST = 9.3; 
//
//        Assert.assertEquals(resultAccordingNIST, result, 0.051);
//    }
//    /**
//	 *  A unit test for JUnit with O=C1C2CCC1CC2
//	 *  
//	 *  @cdk.inchi InChI=1/C7H10O/c8-7-5-1-2-6(7)4-3-5/h5-6H,1-4H2
//	 */
//    @Test
//    public void testIPDescriptor_10() throws Exception{
//        
//		IAtomContainer mol = sp.parseSmiles("O=C1C2CCC1CC2");
//
//		addExplicitHydrogens(mol);
//		
//		LonePairElectronChecker lpcheck = new LonePairElectronChecker();
//		lpcheck.saturate(mol);
//		
//        double result= ((DoubleResult)descriptor.calculate(mol.getAtom(0),mol).getValue()).doubleValue();
//        double resultAccordingNIST = 9.01; 
//
//        Assert.assertEquals(resultAccordingNIST, result, 0.06);
//    }
//
//    /**
//	 *  A unit test for JUnit with CCOCCCO
//	 *  
//	 *  @cdk.inchi InChI=1/C5H12O2/c1-2-7-5-3-4-6/h6H,2-5H2,1H3
//	 */
//    @Test
//    public void testIPDescriptor_14() throws Exception{
//        
//		IAtomContainer mol = sp.parseSmiles("CCOCCCO");
//
//		addExplicitHydrogens(mol);
//		
//		LonePairElectronChecker lpcheck = new LonePairElectronChecker();
//		lpcheck.saturate(mol);
//		
//        double result= ((DoubleResult)descriptor.calculate(mol.getAtom(2), mol).getValue()).doubleValue();
////        Assert.assertNotNull(result);
//        
//        result= ((DoubleResult)descriptor.calculate(mol.getAtom(7), mol).getValue()).doubleValue();
////        Assert.assertNotNull(result);
//        
//    }
//    /**
//     * A unit test for JUnit with C-C-N
//     * 
//	 *  @cdk.inchi  InChI=1/C2H7N/c1-2-3/h2-3H2,1H3
//     * 
//     * @throws ClassNotFoundException
//     * @throws CDKException
//     * @throws java.lang.Exception
//     */
//    @Test
//    public void testIPDescriptorReaction() throws Exception{
//        
//    	IAtomContainer mol = sp.parseSmiles("C-C-N");
//		assertEquals(3, mol.getAtomCount());
//		addExplicitHydrogens(mol);
//		assertEquals(10, mol.getAtomCount());
//		
//		LonePairElectronChecker lpcheck = new LonePairElectronChecker();
//		lpcheck.saturate(mol);
//		assertEquals("Unexpected number of lone pairs", 1, mol.getLonePairCount());
//		
//		assertEquals("N", mol.getAtom(2).getSymbol());
//		descriptor.calculate(mol.getAtom(2), mol);
//		IReactionSet reactionSet = descriptor.getReactionSet();
//		
//		assertNotNull("No reaction was found", reactionSet.getReaction(0));
//		assertNotNull("The ionization energy was not set for the reaction", reactionSet.getReaction(0).getProperty("IonizationEnergy"));
//        double result = ((Double) reactionSet.getReaction(0).getProperty("IonizationEnergy")).doubleValue();
//        double resultAccordingNIST = 8.9; 
//
//        Assert.assertEquals(1, reactionSet.getReactionCount());
//        Assert.assertEquals(resultAccordingNIST, result, 0.5);
//    }
//    /**
//     * A unit test for JUnit with CCCCCC
//     * 
//	 *  @cdk.inchi InChI=1/C6H14/c1-3-5-6-4-2/h3-6H2,1-2H3
//	 *  
//     * @throws ClassNotFoundException
//     * @throws CDKException
//     * @throws java.lang.Exception
//     */
//    @Test
//    public void testIPDescriptorReaction2() throws Exception{
//        
//		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
//		IAtomContainer mol = sp.parseSmiles("CCCCCC");
//
//		addExplicitHydrogens(mol);
//		
//		LonePairElectronChecker lpcheck = new LonePairElectronChecker();
//		lpcheck.saturate(mol);
//		
//		descriptor.calculate(mol.getAtom(0), mol);
//		IReactionSet reactionSet = descriptor.getReactionSet();
//		
//        Assert.assertEquals(0, reactionSet.getReactionCount());
//    }
//
//    /**
//     * A unit test for JUnit with O(C=CC=C)C
//     * 
//	 *  @cdk.inchi InChI=1/C5H8O/c1-3-4-5-6-2/h3-5H,1H2,2H3
//	 *  
//     * @throws ClassNotFoundException
//     * @throws CDKException
//     * @throws java.lang.Exception
//     */
//    @Test
//    public void testIPPySystemWithHeteroatomDescriptor3() throws Exception{
//        
//		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
//		IAtomContainer mol = sp.parseSmiles("O(C=CC=C)C");
//
//		addExplicitHydrogens(mol);
//		
//		LonePairElectronChecker lpcheck = new LonePairElectronChecker();
//		lpcheck.saturate(mol);
//		
//		double result= ((DoubleResult)descriptor.calculate(mol.getAtom(0),mol).getValue()).doubleValue();
//        double resultAccordingNIST = 8.03; 
//        Assert.assertEquals(resultAccordingNIST, result, 0.11);
//        
//        IReactionSet reactionSet = descriptor.getReactionSet();
//		assertEquals(5, reactionSet.getReactionCount());
//        
//    }
//    /**
//     * A unit test for JUnit with OC=CC
//     * 
//	 *  @cdk.inchi InChI=1/C3H6O/c1-2-3-4/h2-4H,1H3
//	 *  
//     * @throws ClassNotFoundException
//     * @throws CDKException
//     * @throws java.lang.Exception
//     */
//    @Test
//    public void testIPPySystemWithHeteroatomDescriptor2() throws Exception{
//        
//		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
//		IAtomContainer mol = sp.parseSmiles("OC=CC");
//
//		addExplicitHydrogens(mol);
//		
//		LonePairElectronChecker lpcheck = new LonePairElectronChecker();
//		lpcheck.saturate(mol);
//		
//		double result= ((DoubleResult)descriptor.calculate(mol.getAtom(0),mol).getValue()).doubleValue();
//        double resultAccordingNIST = 8.64; 
//        Assert.assertEquals(resultAccordingNIST, result, 0.21);
//        
//        IReactionSet reactionSet = descriptor.getReactionSet();
//		assertEquals(3, reactionSet.getReactionCount());
//        
//    }
//    /**
//     * A unit test for JUnit with C1=C(C)CCS1
//     * 
//	 *  @cdk.inchi  InChI=1/C5H8S/c1-5-2-3-6-4-5/h4H,2-3H2,1H3
//	 *  
//     * @throws ClassNotFoundException
//     * @throws CDKException
//     * @throws java.lang.Exception
//     */
//    @Test
//    public void testIPPySystemWithHeteroatomDescriptor1() throws Exception{
//        
//		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
//		IAtomContainer mol = sp.parseSmiles("C1=C(C)CCS1");
//
//		addExplicitHydrogens(mol);
//		
//		LonePairElectronChecker lpcheck = new LonePairElectronChecker();
//		lpcheck.saturate(mol);
//		
//		double result= ((DoubleResult)descriptor.calculate(mol.getAtom(5),mol).getValue()).doubleValue();
//        double resultAccordingNIST = 7.77; 
//        Assert.assertEquals(resultAccordingNIST, result, 0.3);
//        
//        IReactionSet reactionSet = descriptor.getReactionSet();
//		assertEquals(3, reactionSet.getReactionCount());
//        
//    }
//    
//    /**
//     * A unit test for JUnit with OC(C#CC)(C)C
//     * 
//	 *  @cdk.inchi InChI=1/C6H10O/c1-4-5-6(2,3)7/h7H,1-3H3
//	 *  
//     * @throws ClassNotFoundException
//     * @throws CDKException
//     * @throws java.lang.Exception
//     */
//    @Test
//    public void testIDescriptor5() throws Exception{
//        
//		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
//		IAtomContainer mol = sp.parseSmiles("OC(C#CC)(C)C");
//
//		addExplicitHydrogens(mol);
//		
//		LonePairElectronChecker lpcheck = new LonePairElectronChecker();
//		lpcheck.saturate(mol);
//		
//		descriptor.calculate(mol.getAtom(0),mol);
//        
//        IReactionSet reactionSet = descriptor.getReactionSet();
//		assertEquals(1, reactionSet.getReactionCount());
//        
//    }
//    
}
