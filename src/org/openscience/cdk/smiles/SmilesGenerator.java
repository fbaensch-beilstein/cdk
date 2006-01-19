/*
 *  $RCSfile$
 *  $Author$
 *  $Date$
 *  $Revision$
 *
 *  Copyright (C) 2002-2005  The Chemistry Development Kit (CDK) Project
 *
 *  Contact: cdk-devel@lists.sourceforge.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *  All we ask is that proper credit is given for our work, which includes
 *  - but is not limited to - adding the above copyright notice to the beginning
 *  of your source code files, and to any copyright notice that you may distribute
 *  with programs based on this work.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */
package org.openscience.cdk.smiles;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.interfaces.Molecule;
import org.openscience.cdk.interfaces.IPseudoAtom;
import org.openscience.cdk.interfaces.Reaction;
import org.openscience.cdk.interfaces.RingSet;
import org.openscience.cdk.interfaces.SetOfMolecules;
import org.openscience.cdk.aromaticity.HueckelAromaticityDetector;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.BondTools;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.graph.invariant.CanonicalLabeler;
import org.openscience.cdk.graph.invariant.MorganNumbersTools;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.ringsearch.RingPartitioner;
import org.openscience.cdk.tools.manipulator.RingSetManipulator;

/**
 *  Generates SMILES strings {@cdk.cite WEI88, WEI89}. It takes into account the
 *  isotope and formal charge information of the atoms. In addition to this it
 *  takes stereochemistry in account for both Bond's and Atom's. IMPORTANT: The
 *  aromaticity detection for this SmilesGenerator relies on AllRingsFinder,
 *  which is known to take very long for some molecules with many cycles or
 *  special cyclic topologies. Thus, the AllRingsFinder has a built-in timeout
 *  of 5 seconds after which it aborts and throws an Exception. If you want your
 *  SMILES generated at any expense, you need to create your own AllRingsFinder,
 *  set the timeout to a higher value, and assign it to this SmilesGenerator. In
 *  the vast majority of cases, however, the defaults will be fine.
 *
 *@author         Oliver Horlacher,
 *@author         Stefan Kuhn (chiral smiles)
 *@cdk.created    2002-02-26
 *@cdk.keyword    SMILES, generator
 */
public class SmilesGenerator
{
	private final static boolean debug = false;

	/**
	 *  The number of rings that have been opened
	 */
	private int ringMarker = 0;

	/**
	 *  Collection of all the bonds that were broken
	 */
	private Vector brokenBonds = new Vector();

	/**
	 *  The isotope factory which is used to write the mass is needed
	 */
	private IsotopeFactory isotopeFactory;

	AllRingsFinder ringFinder;

	/**
	* RingSet that holds all rings of the molecule
	*/
	private RingSet rings = null; 
	
	/**
	 *  The canonical labler
	 */
	private CanonicalLabeler canLabler = new CanonicalLabeler();
	private final String RING_CONFIG = "stereoconfig";
	private final String UP = "up";
	private final String DOWN = "down";

	private IChemObjectBuilder builder;

	/**
	 *  Default constructor
	 */
	public SmilesGenerator(IChemObjectBuilder builder)
	{
		this.builder = builder;
		try
		{
			isotopeFactory = IsotopeFactory.getInstance(builder);
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}


	/**
	 *  Tells if a certain bond is center of a valid double bond configuration.
	 *
	 *@param  container  The atomcontainer.
	 *@param  bond       The bond.
	 *@return            true=is a potential configuration, false=is not.
	 */
	public boolean isValidDoubleBondConfiguration(IAtomContainer container, IBond bond)
	{
		org.openscience.cdk.interfaces.IAtom[] atoms = bond.getAtoms();
		org.openscience.cdk.interfaces.IAtom[] connectedAtoms = container.getConnectedAtoms(atoms[0]);
		org.openscience.cdk.interfaces.IAtom from = null;
		for (int i = 0; i < connectedAtoms.length; i++)
		{
			if (connectedAtoms[i] != atoms[1])
			{
				from = connectedAtoms[i];
			}
		}
		boolean[] array = new boolean[container.getBonds().length];
		for (int i = 0; i < array.length; i++)
		{
			array[i] = true;
		}
		if (isStartOfDoubleBond(container, atoms[0], from, array) && isEndOfDoubleBond(container, atoms[1], atoms[0], array) && !bond.getFlag(CDKConstants.ISAROMATIC))
		{
			return (true);
		} else
		{
			return (false);
		}
	}

	/**
	 * Provide a reference to a RingSet that holds ALL rings of the molecule.<BR>
	 * During creation of a SMILES the aromaticity of the molecule has to be detected.
	 * This, in turn, requires the dermination of all rings of the molecule. If this
	 * computationally expensive calculation has been done beforehand, a RingSet can
	 * be handed over to the SmilesGenerator to save the effort of another all-rings-
	 * calculation.
	 *
	 * @param  rings  RingSet that holds ALL rings of the molecule
	 * @return        reference to the SmilesGenerator object this method was called for
	 */
	public SmilesGenerator setRings(RingSet rings)
	{
	  this.rings = rings;
	  return this;
	} 

	/**
	 *  Generate canonical SMILES from the <code>molecule</code>. This method
	 *  canonicaly lables the molecule but does not perform any checks on the
	 *  chemical validity of the molecule.
	 *  IMPORTANT: A precomputed Set of All Rings (SAR) can be passed to this 
	 *  SmilesGenerator in order to avoid recomputing it. Use setRings() to 
	 *  assign the SAR.
	 *
	 *@param  molecule  The molecule to evaluate
	 *@return           Description of the Returned Value
	 *@see              org.openscience.cdk.graph.invariant.CanonicalLabeler#canonLabel(IAtomContainer)
	 */
	public synchronized String createSMILES(Molecule molecule)
	{
		try
		{
			return (createSMILES(molecule, false, new boolean[molecule.getBondCount()]));
		} catch (CDKException exception)
		{
			// This exception can only happen if a chiral smiles is requested
			return ("");
		}
	}


	/**
	 *  Generate a SMILES for the given <code>Reaction</code>.
	 *
	 *@param  reaction          Description of the Parameter
	 *@return                   Description of the Return Value
	 *@exception  CDKException  Description of the Exception
	 */
	public synchronized String createSMILES(Reaction reaction) throws CDKException
	{
		StringBuffer reactionSMILES = new StringBuffer();
		Molecule[] reactants = reaction.getReactants().getMolecules();
		for (int i = 0; i < reactants.length; i++)
		{
			reactionSMILES.append(createSMILES(reactants[i]));
			if (i + 1 < reactants.length)
			{
				reactionSMILES.append('.');
			}
		}
		reactionSMILES.append('>');
		Molecule[] agents = reaction.getAgents().getMolecules();
		for (int i = 0; i < agents.length; i++)
		{
			reactionSMILES.append(createSMILES(agents[i]));
			if (i + 1 < agents.length)
			{
				reactionSMILES.append('.');
			}
		}
		reactionSMILES.append('>');
		Molecule[] products = reaction.getProducts().getMolecules();
		for (int i = 0; i < products.length; i++)
		{
			reactionSMILES.append(createSMILES(products[i]));
			if (i + 1 < products.length)
			{
				reactionSMILES.append('.');
			}
		}
		return reactionSMILES.toString();
	}


	/**
	 *  Generate canonical and chiral SMILES from the <code>molecule</code>. This
	 *  method canonicaly lables the molecule but dose not perform any checks on
	 *  the chemical validity of the molecule. The chiral smiles is done like in
	 *  the <a href="http://www.daylight.com/dayhtml/doc/theory/theory.smiles.html">
	 *  daylight theory manual</a> . I did not find rules for canonical and chiral
	 *  smiles, therefore there is no guarantee that the smiles complies to any
	 *  externeal rules, but it is canonical compared to other smiles produced by
	 *  this method. The method checks if there are 2D coordinates but does not
	 *  check if coordinates make sense. Invalid stereo configurations are ignored;
	 *  if there are no valid stereo configuration the smiles will be the same as
	 *  the non-chiral one. Note that often stereo configurations are only complete
	 *  and can be converted to a smiles if explicit Hs are given.
	 *  IMPORTANT: A precomputed Set of All Rings (SAR) can be passed to this 
	 *  SmilesGenerator in order to avoid recomputing it. Use setRings() to 
	 *  assign the SAR.
	 *
	 *@param  molecule                 The molecule to evaluate
	 *@param  doubleBondConfiguration  Description of Parameter
	 *@return                          Description of the Returned Value
	 *@exception  CDKException         At least one atom has no Point2D;
	 *      coordinates are needed for creating the chiral smiles.
	 *@see                             org.openscience.cdk.graph.invariant.CanonicalLabeler#canonLabel(IAtomContainer)
	 */
	public synchronized String createChiralSMILES(Molecule molecule, boolean[] doubleBondConfiguration) throws CDKException
	{
		return (createSMILES(molecule, true, doubleBondConfiguration));
	}


	/**
	 *  Generate canonical SMILES from the <code>molecule</code>. This method
	 *  canonicaly lables the molecule but dose not perform any checks on the
	 *  chemical validity of the molecule. This method also takes care of multiple
	 *  molecules.
	 *  IMPORTANT: A precomputed Set of All Rings (SAR) can be passed to this 
	 *  SmilesGenerator in order to avoid recomputing it. Use setRings() to 
	 *  assign the SAR.
	 *
	 *@param  molecule                 The molecule to evaluate
	 *@param  chiral                   true=SMILES will be chiral, false=SMILES
	 *      will not be chiral.
	 *@param  doubleBondConfiguration  Description of Parameter
	 *@return                          Description of the Returned Value
	 *@exception  CDKException         At least one atom has no Point2D;
	 *      coordinates are needed for crating the chiral smiles. This excpetion
	 *      can only be thrown if chiral smiles is created, ignore it if you want a
	 *      non-chiral smiles (createSMILES(AtomContainer) does not throw an
	 *      exception).
	 *@see                             org.openscience.cdk.graph.invariant.CanonicalLabeler#canonLabel(IAtomContainer)
	 */
	public synchronized String createSMILES(Molecule molecule, boolean chiral, boolean doubleBondConfiguration[]) throws CDKException
	{
		SetOfMolecules moleculeSet = ConnectivityChecker.partitionIntoMolecules(molecule);
		if (moleculeSet.getMoleculeCount() > 1)
		{
			StringBuffer fullSMILES = new StringBuffer();
			Molecule[] molecules = moleculeSet.getMolecules();
			for (int i = 0; i < molecules.length; i++)
			{
				Molecule molPart = molecules[i];
				fullSMILES.append(createSMILESWithoutCheckForMultipleMolecules(molPart, chiral, doubleBondConfiguration));
				if (i < (molecules.length - 1))
				{
					// are there more molecules?
					fullSMILES.append('.');
				}
			}
			return fullSMILES.toString();
		} else
		{
			return (createSMILESWithoutCheckForMultipleMolecules(molecule, chiral, doubleBondConfiguration));
		}
	}


	/**
	 *  Generate canonical SMILES from the <code>molecule</code>. This method
	 *  canonicaly lables the molecule but dose not perform any checks on the
	 *  chemical validity of the molecule. Does not care about multiple molecules.
	 *  IMPORTANT: A precomputed Set of All Rings (SAR) can be passed to this 
	 *  SmilesGenerator in order to avoid recomputing it. Use setRings() to 
	 *  assign the SAR.
	 *
	 *@param  molecule                 The molecule to evaluate
	 *@param  chiral                   true=SMILES will be chiral, false=SMILES
	 *      will not be chiral.
	 *@param  doubleBondConfiguration  Description of Parameter
	 *@return                          Description of the Returned Value
	 *@exception  CDKException         At least one atom has no Point2D;
	 *      coordinates are needed for creating the chiral smiles. This excpetion
	 *      can only be thrown if chiral smiles is created, ignore it if you want a
	 *      non-chiral smiles (createSMILES(AtomContainer) does not throw an
	 *      exception).
	 *@see                             org.openscience.cdk.graph.invariant.CanonicalLabeler#canonLabel(IAtomContainer)
	 */
	public synchronized String createSMILESWithoutCheckForMultipleMolecules(Molecule molecule, boolean chiral, boolean doubleBondConfiguration[]) throws CDKException
	{
		if (molecule.getAtomCount() == 0)
		{
			return "";
		}
		canLabler.canonLabel(molecule);
		brokenBonds.clear();
		ringMarker = 0;
		org.openscience.cdk.interfaces.IAtom[] all = molecule.getAtoms();
		org.openscience.cdk.interfaces.IAtom start = null;
		for (int i = 0; i < all.length; i++)
		{
			org.openscience.cdk.interfaces.IAtom atom = all[i];
			if (chiral && atom.getPoint2d() == null)
			{
				throw new CDKException("Atom number " + i + " has no 2D coordinates, but 2D coordinates are needed for creating chiral smiles");
			}
			//System.out.println("Setting all VISITED flags to false");
			atom.setFlag(CDKConstants.VISITED, false);
			if (((Long) atom.getProperty("CanonicalLable")).longValue() == 1)
			{
				start = atom;
			}
		}

		//detect aromaticity
		if(rings == null)
		{
			if (ringFinder == null)
			{
				ringFinder = new AllRingsFinder();
			}
			rings = ringFinder.findAllRings(molecule);
		}
		HueckelAromaticityDetector.detectAromaticity(molecule, rings, false);
		if (chiral && rings.size() > 0)
		{
			Vector v = RingPartitioner.partitionRings(rings);
			//System.out.println("RingSystems: " + v.size());
			for (int i = 0; i < v.size(); i++)
			{
				int counter = 0;
				IAtomContainer allrings = RingSetManipulator.getAllInOneContainer((RingSet) v.get(i));
				for (int k = 0; k < allrings.getAtomCount(); k++)
				{
					if (!BondTools.isStereo(molecule, allrings.getAtomAt(k)) && hasWedges(molecule, allrings.getAtomAt(k)) != null)
					{
						IBond bond = molecule.getBond(allrings.getAtomAt(k), hasWedges(molecule, allrings.getAtomAt(k)));
						if (bond.getStereo() == CDKConstants.STEREO_BOND_UP)
						{
							allrings.getAtomAt(k).setProperty(RING_CONFIG, UP);
						} else
						{
							allrings.getAtomAt(k).setProperty(RING_CONFIG, DOWN);
						}
						counter++;
					}
				}
				if (counter == 1)
				{
					for (int k = 0; k < allrings.getAtomCount(); k++)
					{
						allrings.getAtomAt(k).setProperty(RING_CONFIG, UP);
					}
				}
			}
		}

		StringBuffer l = new StringBuffer();
		createSMILES(start, l, molecule, chiral, doubleBondConfiguration);
		rings = null;
		return l.toString();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  ac  Description of the Parameter
	 *@param  a   Description of the Parameter
	 *@return     Description of the Return Value
	 */
	private org.openscience.cdk.interfaces.IAtom hasWedges(IAtomContainer ac, org.openscience.cdk.interfaces.IAtom a)
	{
		org.openscience.cdk.interfaces.IAtom[] atoms = ac.getConnectedAtoms(a);
		for (int i = 0; i < atoms.length; i++)
		{
			if (ac.getBond(a, atoms[i]).getStereo() != CDKConstants.STEREO_BOND_NONE && !atoms[i].getSymbol().equals("H"))
			{
				return (atoms[i]);
			}
		}
		for (int i = 0; i < atoms.length; i++)
		{
			if (ac.getBond(a, atoms[i]).getStereo() != CDKConstants.STEREO_BOND_NONE)
			{
				return (atoms[i]);
			}
		}
		return (null);
	}


	/**
	 *  Says if an atom is the end of a double bond configuration
	 *
	 *@param  atom                     The atom which is the end of configuration
	 *@param  container                The atomContainer the atom is in
	 *@param  parent                   The atom we came from
	 *@param  doubleBondConfiguration  The array indicating where double bond
	 *      configurations are specified (this method ensures that there is
	 *      actually the possibility of a double bond configuration)
	 *@return                          false=is not end of configuration, true=is
	 */
	private boolean isEndOfDoubleBond(IAtomContainer container, org.openscience.cdk.interfaces.IAtom atom, org.openscience.cdk.interfaces.IAtom parent, boolean[] doubleBondConfiguration)
	{
		if (container.getBondNumber(atom, parent) == -1 || doubleBondConfiguration.length <= container.getBondNumber(atom, parent) || !doubleBondConfiguration[container.getBondNumber(atom, parent)])
		{
			return false;
		}
		int lengthAtom = container.getConnectedAtoms(atom).length + atom.getHydrogenCount();
		int lengthParent = container.getConnectedAtoms(parent).length + parent.getHydrogenCount();
		if (container.getBond(atom, parent) != null)
		{
			if (container.getBond(atom, parent).getOrder() == CDKConstants.BONDORDER_DOUBLE && (lengthAtom == 3 || (lengthAtom == 2 && atom.getSymbol().equals("N"))) && (lengthParent == 3 || (lengthParent == 2 && parent.getSymbol().equals("N"))))
			{
				org.openscience.cdk.interfaces.IAtom[] atoms = container.getConnectedAtoms(atom);
				org.openscience.cdk.interfaces.IAtom one = null;
				org.openscience.cdk.interfaces.IAtom two = null;
				for (int i = 0; i < atoms.length; i++)
				{
					if (atoms[i] != parent && one == null)
					{
						one = atoms[i];
					} else if (atoms[i] != parent && one != null)
					{
						two = atoms[i];
					}
				}
				String[] morgannumbers = MorganNumbersTools.getMorganNumbersWithElementSymbol(container);
				if ((one != null && two == null && atom.getSymbol().equals("N") && Math.abs(BondTools.giveAngleBothMethods(parent, atom, one, true)) > Math.PI / 10) || (!atom.getSymbol().equals("N") && one != null && two != null && !morgannumbers[container.getAtomNumber(one)].equals(morgannumbers[container.getAtomNumber(two)])))
				{
					return (true);
				} else
				{
					return (false);
				}
			}
		}
		return (false);
	}


	/**
	 *  Says if an atom is the start of a double bond configuration
	 *
	 *@param  a                        The atom which is the start of configuration
	 *@param  container                The atomContainer the atom is in
	 *@param  parent                   The atom we came from
	 *@param  doubleBondConfiguration  The array indicating where double bond
	 *      configurations are specified (this method ensures that there is
	 *      actually the possibility of a double bond configuration)
	 *@return                          false=is not start of configuration, true=is
	 */
	private boolean isStartOfDoubleBond(IAtomContainer container, org.openscience.cdk.interfaces.IAtom a, org.openscience.cdk.interfaces.IAtom parent, boolean[] doubleBondConfiguration)
	{
		int lengthAtom = container.getConnectedAtoms(a).length + a.getHydrogenCount();
		if (lengthAtom != 3 && (lengthAtom != 2 && a.getSymbol() != ("N")))
		{
			return (false);
		}
		org.openscience.cdk.interfaces.IAtom[] atoms = container.getConnectedAtoms(a);
		org.openscience.cdk.interfaces.IAtom one = null;
		org.openscience.cdk.interfaces.IAtom two = null;
		boolean doubleBond = false;
		org.openscience.cdk.interfaces.IAtom nextAtom = null;
		for (int i = 0; i < atoms.length; i++)
		{
			if (atoms[i] != parent && container.getBond(atoms[i], a).getOrder() == CDKConstants.BONDORDER_DOUBLE && isEndOfDoubleBond(container, atoms[i], a, doubleBondConfiguration))
			{
				doubleBond = true;
				nextAtom = atoms[i];
			}
			if (atoms[i] != nextAtom && one == null)
			{
				one = atoms[i];
			} else if (atoms[i] != nextAtom && one != null)
			{
				two = atoms[i];
			}
		}
		String[] morgannumbers = MorganNumbersTools.getMorganNumbersWithElementSymbol(container);
		if (one != null && ((!a.getSymbol().equals("N") && two != null && !morgannumbers[container.getAtomNumber(one)].equals(morgannumbers[container.getAtomNumber(two)]) && doubleBond && doubleBondConfiguration[container.getBondNumber(a, nextAtom)]) || (doubleBond && a.getSymbol().equals("N") && Math.abs(BondTools.giveAngleBothMethods(nextAtom, a, parent, true)) > Math.PI / 10)))
		{
			return (true);
		} else
		{
			return (false);
		}
	}


	/**
	 *  Gets the bondBroken attribute of the SmilesGenerator object
	 *
	 *@param  a1  Description of Parameter
	 *@param  a2  Description of Parameter
	 *@return     The bondBroken value
	 */
	private boolean isBondBroken(IAtom a1, IAtom a2)
	{
		Iterator it = brokenBonds.iterator();
		while (it.hasNext())
		{
			BrokenBond bond = ((BrokenBond) it.next());
			if ((bond.getA1().equals(a1) || bond.getA1().equals(a2)) && (bond.getA2().equals(a1) || bond.getA2().equals(a2)))
			{
				return (true);
			}
		}
		return false;
	}


	/**
	 *  Determines if the atom <code>a</code> is a atom with a ring marker.
	 *
	 *@param  a  the atom to test
	 *@return    true if the atom participates in a bond that was broken in the
	 *      first pass.
	 */
	private boolean isRingOpening(IAtom a)
	{
		Iterator it = brokenBonds.iterator();
		while (it.hasNext())
		{
			BrokenBond bond = (BrokenBond) it.next();
			if (bond.getA1().equals(a) || bond.getA2().equals(a))
			{
				return true;
			}
		}
		return false;
	}


	/**
	 *  Determines if the atom <code>a</code> is a atom with a ring marker.
	 *
	 *@param  a1  Description of Parameter
	 *@param  v   Description of the Parameter
	 *@return     true if the atom participates in a bond that was broken in the
	 *      first pass.
	 */
	private boolean isRingOpening(IAtom a1, Vector v)
	{
		Iterator it = brokenBonds.iterator();
		while (it.hasNext())
		{
			BrokenBond bond = (BrokenBond) it.next();
			for (int i = 0; i < v.size(); i++)
			{
				if ((bond.getA1().equals(a1) && bond.getA2().equals((IAtom) v.get(i))) || (bond.getA1().equals((IAtom) v.get(i)) && bond.getA2().equals(a1)))
				{
					return true;
				}
			}
		}
		return false;
	}


	/**
	 *  Return the neighbours of atom <code>a</code> in canonical order with the
	 *  atoms that have high bond order at the front.
	 *
	 *@param  a          the atom whose neighbours are to be found.
	 *@param  container  the AtomContainer that is being parsed.
	 *@return            Vector of atoms in canonical oreder.
	 */
	private Vector getCanNeigh(final org.openscience.cdk.interfaces.IAtom a, final IAtomContainer container)
	{
		Vector v = container.getConnectedAtomsVector(a);
		if (v.size() > 1)
		{
			Collections.sort(v,
				new Comparator()
				{
					public int compare(Object o1, Object o2)
					{
						return (int) (((Long) ((IAtom) o1).getProperty("CanonicalLable")).longValue() - ((Long) ((IAtom) o2).getProperty("CanonicalLable")).longValue());
					}
				});
		}
		return v;
	}


	/**
	 *  Gets the ringOpenings attribute of the SmilesGenerator object
	 *
	 *@param  a       Description of Parameter
	 *@param  vbonds  Description of the Parameter
	 *@return         The ringOpenings value
	 */
	private Vector getRingOpenings(IAtom a, Vector vbonds)
	{
		Iterator it = brokenBonds.iterator();
		Vector v = new Vector(10);
		while (it.hasNext())
		{
			BrokenBond bond = (BrokenBond) it.next();
			if (bond.getA1().equals(a) || bond.getA2().equals(a))
			{
				v.add(new Integer(bond.getMarker()));
				if (vbonds != null)
				{
					vbonds.add(bond.getA1().equals(a) ? bond.getA2() : bond.getA1());
				}
			}
		}
		Collections.sort(v);
		return v;
	}


	/**
	 *  Returns true if the <code>atom</code> in the <code>container</code> has
	 *  been marked as a chiral center by the user.
	 *
	 *@param  atom       Description of Parameter
	 *@param  container  Description of Parameter
	 *@return            The chiralCenter value
	 */
	private boolean isChiralCenter(IAtom atom, IAtomContainer container)
	{
		IBond[] bonds = container.getConnectedBonds(atom);
		for (int i = 0; i < bonds.length; i++)
		{
			IBond bond = bonds[i];
			int stereo = bond.getStereo();
			if (stereo == CDKConstants.STEREO_BOND_DOWN ||
					stereo == CDKConstants.STEREO_BOND_UP)
			{
				return true;
			}
		}
		return false;
	}


	/**
	 *  Gets the last atom object (not Vector) in a Vector as created by
	 *  createDSFTree.
	 *
	 *@param  v       The Vector
	 *@param  result  The feature to be added to the Atoms attribute
	 */
	private void addAtoms(Vector v, Vector result)
	{
		for (int i = 0; i < v.size(); i++)
		{
			if (v.get(i) instanceof IAtom)
			{
				result.add((IAtom) v.get(i));
			} else
			{
				addAtoms((Vector) v.get(i), result);
			}
		}
	}


	/**
	 *  Performes a DFS search on the <code>atomContainer</code>. Then parses the
	 *  resulting tree to create the SMILES string.
	 *
	 *@param  a                        the atom to start the search at.
	 *@param  line                     the StringBuffer that the SMILES is to be
	 *      appended to.
	 *@param  chiral                   true=SMILES will be chiral, false=SMILES
	 *      will not be chiral.
	 *@param  atomContainer            the AtomContainer that the SMILES string is
	 *      generated for.
	 *@param  doubleBondConfiguration  Description of Parameter
	 */
	private void createSMILES(org.openscience.cdk.interfaces.IAtom a, StringBuffer line, IAtomContainer atomContainer, boolean chiral, boolean[] doubleBondConfiguration)
	{
		Vector tree = new Vector();
		createDFSTree(a, tree, null, atomContainer);
		//System.out.println("Done with tree");
		parseChain(tree, line, atomContainer, null, chiral, doubleBondConfiguration, new Vector());
	}


	/**
	 *  Recursively perform a DFS search on the <code>container</code> placing
	 *  atoms and branches in the vector <code>tree</code>.
	 *
	 *@param  a          the atom being visited.
	 *@param  tree       vector holding the tree.
	 *@param  parent     the atom we came from.
	 *@param  container  the AtomContainer that we are parsing.
	 */
	private void createDFSTree(org.openscience.cdk.interfaces.IAtom a, Vector tree, org.openscience.cdk.interfaces.IAtom parent, IAtomContainer container)
	{
		tree.add(a);
		Vector neighbours = getCanNeigh(a, container);
		neighbours.remove(parent);
		IAtom next;
		a.setFlag(CDKConstants.VISITED, true);
		//System.out.println("Starting with DFSTree and AtomContainer of size " + container.getAtomCount());
		//System.out.println("Current Atom has " + neighbours.size() + " neighbours");
		for (int x = 0; x < neighbours.size(); x++)
		{
			next = (IAtom) neighbours.elementAt(x);
			if (!next.getFlag(CDKConstants.VISITED))
			{
				if (x == neighbours.size() - 1)
				{
					//Last neighbour therefore in this chain
					createDFSTree(next, tree, a, container);
				} else
				{
					Vector branch = new Vector();
					tree.add(branch);
					//System.out.println("adding branch");
					createDFSTree(next, branch, a, container);
				}
			} else
			{
				//Found ring closure between next and a
				//System.out.println("found ringclosure in DFTTreeCreation");
				ringMarker++;
				BrokenBond bond = new BrokenBond(a, next, ringMarker);
				if (!brokenBonds.contains(bond))
				{
					brokenBonds.add(bond);
				} else
				{
					ringMarker--;
				}
			}
		}
	}


	/**
	 *  Parse a branch
	 *
	 *@param  v                        Description of Parameter
	 *@param  buffer                   Description of Parameter
	 *@param  container                Description of Parameter
	 *@param  parent                   Description of Parameter
	 *@param  chiral                   Description of Parameter
	 *@param  doubleBondConfiguration  Description of Parameter
	 *@param  atomsInOrderOfSmiles     Description of Parameter
	 */
	private void parseChain(Vector v, StringBuffer buffer, IAtomContainer container, IAtom parent, boolean chiral, boolean[] doubleBondConfiguration, Vector atomsInOrderOfSmiles)
	{
		int positionInVector = 0;
		IAtom atom;
		//System.out.println("in parse chain. Size of tree: " + v.size());
		for (int h = 0; h < v.size(); h++)
		{
			Object o = v.get(h);
			if (o instanceof IAtom)
			{
				atom = (IAtom) o;
				if (parent != null)
				{
					parseBond(buffer, atom, parent, container);
				} else
				{
					if (chiral && BondTools.isStereo(container, atom))
					{
						parent = (IAtom) ((Vector) v.get(1)).get(0);
					}
				}
				parseAtom(atom, buffer, container, chiral, doubleBondConfiguration, parent, atomsInOrderOfSmiles, v);
				//System.out.println("in parseChain after parseAtom()");
				/*
				 *  The principle of making chiral smiles is quite simple, although the code is
				 *  pretty uggly. The Atoms connected to the chiral center are put in sorted[] in the
				 *  order they have to appear in the smiles. Then the Vector v is rearranged according
				 *  to sorted[]
				 */
				if (chiral && BondTools.isStereo(container, atom) && container.getBond(parent, atom) != null)
				{
					//System.out.println("in parseChain in isChiral");
					IAtom[] sorted = null;
					Vector chiralNeighbours = container.getConnectedAtomsVector(atom);
					if (BondTools.isTetrahedral(container, atom,false) > 0)
					{
						sorted = new IAtom[3];
					}
					if (BondTools.isTetrahedral(container, atom,false) == 1)
					{
						if (container.getBond(parent, atom).getStereo() == CDKConstants.STEREO_BOND_DOWN)
						{
							for (int i = 0; i < chiralNeighbours.size(); i++)
							{
								if (chiralNeighbours.get(i) != parent)
								{
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == 0 && BondTools.isLeft(((IAtom) chiralNeighbours.get(i)), parent, atom) && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										sorted[2] = (IAtom) chiralNeighbours.get(i);
									}
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == 0 && !BondTools.isLeft(((IAtom) chiralNeighbours.get(i)), parent, atom) && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										sorted[1] = (IAtom) chiralNeighbours.get(i);
									}
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_UP && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										sorted[0] = (IAtom) chiralNeighbours.get(i);
									}
								}
							}
						}
						if (container.getBond(parent, atom).getStereo() == CDKConstants.STEREO_BOND_UP)
						{
							for (int i = 0; i < chiralNeighbours.size(); i++)
							{
								if (chiralNeighbours.get(i) != parent)
								{
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == 0 && BondTools.isLeft(((IAtom) chiralNeighbours.get(i)), parent, atom) && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										sorted[1] = (IAtom) chiralNeighbours.get(i);
									}
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == 0 && !BondTools.isLeft(((IAtom) chiralNeighbours.get(i)), parent, atom) && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										sorted[2] = (IAtom) chiralNeighbours.get(i);
									}
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_DOWN && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										sorted[0] = (IAtom) chiralNeighbours.get(i);
									}
								}
							}
						}
						if (container.getBond(parent, atom).getStereo() == CDKConstants.STEREO_BOND_UNDEFINED || container.getBond(parent, atom).getStereo() == CDKConstants.STEREO_BOND_NONE)
						{
							boolean normalBindingIsLeft = false;
							for (int i = 0; i < chiralNeighbours.size(); i++)
							{
								if (chiralNeighbours.get(i) != parent)
								{
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == 0)
									{
										if (BondTools.isLeft(((IAtom) chiralNeighbours.get(i)), parent, atom))
										{
											normalBindingIsLeft = true;
											break;
										}
									}
								}
							}
							for (int i = 0; i < chiralNeighbours.size(); i++)
							{
								if (chiralNeighbours.get(i) != parent)
								{
									if (normalBindingIsLeft)
									{
										if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == 0)
										{
											sorted[0] = (IAtom) chiralNeighbours.get(i);
										}
										if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_UP)
										{
											sorted[2] = (IAtom) chiralNeighbours.get(i);
										}
										if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_DOWN)
										{
											sorted[1] = (IAtom) chiralNeighbours.get(i);
										}
									} else
									{
										if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_UP)
										{
											sorted[1] = (IAtom) chiralNeighbours.get(i);
										}
										if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == 0)
										{
											sorted[0] = (IAtom) chiralNeighbours.get(i);
										}
										if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_DOWN)
										{
											sorted[2] = (IAtom) chiralNeighbours.get(i);
										}
									}
								}
							}
						}
					}
					if (BondTools.isTetrahedral(container, atom,false) == 2)
					{
						if (container.getBond(parent, atom).getStereo() == CDKConstants.STEREO_BOND_UP)
						{
							for (int i = 0; i < chiralNeighbours.size(); i++)
							{
								if (chiralNeighbours.get(i) != parent)
								{
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_DOWN && BondTools.isLeft(((IAtom) chiralNeighbours.get(i)), parent, atom) && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										sorted[1] = (IAtom) chiralNeighbours.get(i);
									}
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_DOWN && !BondTools.isLeft(((IAtom) chiralNeighbours.get(i)), parent, atom) && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										sorted[2] = (IAtom) chiralNeighbours.get(i);
									}
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_UP && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										sorted[0] = (IAtom) chiralNeighbours.get(i);
									}
								}
							}
						}
						if (container.getBond(parent, atom).getStereo() == CDKConstants.STEREO_BOND_DOWN)
						{
							double angle1 = 0;
							double angle2 = 0;
							IAtom atom1 = null;
							IAtom atom2 = null;
							for (int i = 0; i < chiralNeighbours.size(); i++)
							{
								if (chiralNeighbours.get(i) != parent)
								{
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_UP && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										if (angle1 == 0)
										{
											angle1 = BondTools.giveAngle(atom, parent, (IAtom) chiralNeighbours.get(i));
											atom1 = (IAtom) chiralNeighbours.get(i);
										} else
										{
											angle2 = BondTools.giveAngle(atom, parent, (IAtom) chiralNeighbours.get(i));
											atom2 = (IAtom) chiralNeighbours.get(i);
										}
									}
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_DOWN && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										sorted[1] = (IAtom) chiralNeighbours.get(i);
									}
								}
							}
							if (angle1 < angle2)
							{
								sorted[0] = atom2;
								sorted[2] = atom1;
							} else
							{
								sorted[0] = atom1;
								sorted[2] = atom2;
							}
						}
					}
					if (BondTools.isTetrahedral(container, atom,false) == 3)
					{
						if (container.getBond(parent, atom).getStereo() == CDKConstants.STEREO_BOND_UP)
						{
							TreeMap hm = new TreeMap();
							for (int i = 0; i < chiralNeighbours.size(); i++)
							{
								if (chiralNeighbours.get(i) != parent && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
								{
									hm.put(new Double(BondTools.giveAngle(atom, parent, ((IAtom) chiralNeighbours.get(i)))), new Integer(i));
								}
							}
							Object[] ohere = hm.values().toArray();
							for (int i = ohere.length - 1; i > -1; i--)
							{
								sorted[i] = ((IAtom) chiralNeighbours.get(((Integer) ohere[i]).intValue()));
							}
						}
						if (container.getBond(parent, atom).getStereo() == 0)
						{
							double angle1 = 0;
							double angle2 = 0;
							IAtom atom1 = null;
							IAtom atom2 = null;
							for (int i = 0; i < chiralNeighbours.size(); i++)
							{
								if (chiralNeighbours.get(i) != parent)
								{
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == 0 && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										if (angle1 == 0)
										{
											angle1 = BondTools.giveAngle(atom, parent, (IAtom) chiralNeighbours.get(i));
											atom1 = (IAtom) chiralNeighbours.get(i);
										} else
										{
											angle2 = BondTools.giveAngle(atom, parent, (IAtom) chiralNeighbours.get(i));
											atom2 = (IAtom) chiralNeighbours.get(i);
										}
									}
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_UP && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										sorted[0] = (IAtom) chiralNeighbours.get(i);
									}
								}
							}
							if (angle1 < angle2)
							{
								sorted[1] = atom2;
								sorted[2] = atom1;
							} else
							{
								sorted[1] = atom1;
								sorted[2] = atom2;
							}
						}
					}
					if (BondTools.isTetrahedral(container, atom,false) == 4)
					{
						if (container.getBond(parent, atom).getStereo() == CDKConstants.STEREO_BOND_DOWN)
						{
							TreeMap hm = new TreeMap();
							for (int i = 0; i < chiralNeighbours.size(); i++)
							{
								if (chiralNeighbours.get(i) != parent && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
								{
									hm.put(new Double(BondTools.giveAngle(atom, parent, ((IAtom) chiralNeighbours.get(i)))), new Integer(i));
								}
							}
							Object[] ohere = hm.values().toArray();
							for (int i = ohere.length - 1; i > -1; i--)
							{
								sorted[i] = ((IAtom) chiralNeighbours.get(((Integer) ohere[i]).intValue()));
							}
						}
						if (container.getBond(parent, atom).getStereo() == 0)
						{
							double angle1 = 0;
							double angle2 = 0;
							IAtom atom1 = null;
							IAtom atom2 = null;
							for (int i = 0; i < chiralNeighbours.size(); i++)
							{
								if (chiralNeighbours.get(i) != parent)
								{
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == 0 && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										if (angle1 == 0)
										{
											angle1 = BondTools.giveAngle(atom, parent, (IAtom) chiralNeighbours.get(i));
											atom1 = (IAtom) chiralNeighbours.get(i);
										} else
										{
											angle2 = BondTools.giveAngle(atom, parent, (IAtom) chiralNeighbours.get(i));
											atom2 = (IAtom) chiralNeighbours.get(i);
										}
									}
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_DOWN && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										sorted[2] = (IAtom) chiralNeighbours.get(i);
									}
								}
							}
							if (angle1 < angle2)
							{
								sorted[1] = atom2;
								sorted[0] = atom1;
							} else
							{
								sorted[1] = atom1;
								sorted[0] = atom2;
							}
						}
					}
					if (BondTools.isTetrahedral(container, atom,false) == 5)
					{
						if (container.getBond(parent, atom).getStereo() == CDKConstants.STEREO_BOND_DOWN)
						{
							for (int i = 0; i < chiralNeighbours.size(); i++)
							{
								if (chiralNeighbours.get(i) != parent)
								{
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_UP)
									{
										sorted[0] = (IAtom) chiralNeighbours.get(i);
									}
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == 0)
									{
										sorted[2] = (IAtom) chiralNeighbours.get(i);
									}
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_DOWN)
									{
										sorted[1] = (IAtom) chiralNeighbours.get(i);
									}
								}
							}
						}
						if (container.getBond(parent, atom).getStereo() == CDKConstants.STEREO_BOND_UP)
						{
							for (int i = 0; i < chiralNeighbours.size(); i++)
							{
								if (chiralNeighbours.get(i) != parent)
								{
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_DOWN && BondTools.isLeft(((IAtom) chiralNeighbours.get(i)), parent, atom) && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										sorted[0] = (IAtom) chiralNeighbours.get(i);
									}
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_DOWN && !BondTools.isLeft(((IAtom) chiralNeighbours.get(i)), parent, atom) && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										sorted[2] = (IAtom) chiralNeighbours.get(i);
									}
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == 0)
									{
										sorted[1] = (IAtom) chiralNeighbours.get(i);
									}
								}
							}
						}
						if (container.getBond(parent, atom).getStereo() == CDKConstants.STEREO_BOND_UNDEFINED || container.getBond(parent, atom).getStereo() == CDKConstants.STEREO_BOND_NONE)
						{
							for (int i = 0; i < chiralNeighbours.size(); i++)
							{
								if (chiralNeighbours.get(i) != parent)
								{
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_DOWN && BondTools.isLeft(((IAtom) chiralNeighbours.get(i)), parent, atom) && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										sorted[0] = (IAtom) chiralNeighbours.get(i);
									}
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_DOWN && !BondTools.isLeft(((IAtom) chiralNeighbours.get(i)), parent, atom) && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										sorted[2] = (IAtom) chiralNeighbours.get(i);
									}
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_UP)
									{
										sorted[1] = (IAtom) chiralNeighbours.get(i);
									}
								}
							}
						}
					}
					if (BondTools.isTetrahedral(container, atom,false) == 6)
					{
						if (container.getBond(parent, atom).getStereo() == CDKConstants.STEREO_BOND_UP)
						{
							for (int i = 0; i < chiralNeighbours.size(); i++)
							{
								if (chiralNeighbours.get(i) != parent)
								{
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_UP)
									{
										sorted[0] = (IAtom) chiralNeighbours.get(i);
									}
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == 0)
									{
										sorted[2] = (IAtom) chiralNeighbours.get(i);
									}
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_DOWN)
									{
										sorted[1] = (IAtom) chiralNeighbours.get(i);
									}
								}
							}
						}
						if (container.getBond(parent, atom).getStereo() == CDKConstants.STEREO_BOND_DOWN)
						{
							for (int i = 0; i < chiralNeighbours.size(); i++)
							{
								if (chiralNeighbours.get(i) != parent)
								{
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_UP && BondTools.isLeft(((IAtom) chiralNeighbours.get(i)), parent, atom) && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										sorted[2] = (IAtom) chiralNeighbours.get(i);
									}
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_UP && !BondTools.isLeft(((IAtom) chiralNeighbours.get(i)), parent, atom) && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										sorted[0] = (IAtom) chiralNeighbours.get(i);
									}
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == 0)
									{
										sorted[1] = (IAtom) chiralNeighbours.get(i);
									}
								}
							}
						}
						if (container.getBond(parent, atom).getStereo() == CDKConstants.STEREO_BOND_UNDEFINED || container.getBond(parent, atom).getStereo() == CDKConstants.STEREO_BOND_NONE)
						{
							for (int i = 0; i < chiralNeighbours.size(); i++)
							{
								if (chiralNeighbours.get(i) != parent)
								{
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_UP && BondTools.isLeft(((IAtom) chiralNeighbours.get(i)), parent, atom) && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										sorted[2] = (IAtom) chiralNeighbours.get(i);
									}
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_UP && !BondTools.isLeft(((IAtom) chiralNeighbours.get(i)), parent, atom) && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
									{
										sorted[0] = (IAtom) chiralNeighbours.get(i);
									}
									if (container.getBond((IAtom) chiralNeighbours.get(i), atom).getStereo() == CDKConstants.STEREO_BOND_DOWN)
									{
										sorted[1] = (IAtom) chiralNeighbours.get(i);
									}
								}
							}
						}
					}
					if (BondTools.isSquarePlanar(container, atom))
					{
						sorted = new IAtom[3];
						//This produces a U=SP1 order in every case
						TreeMap hm = new TreeMap();
						for (int i = 0; i < chiralNeighbours.size(); i++)
						{
							if (chiralNeighbours.get(i) != parent && !isBondBroken((IAtom) chiralNeighbours.get(i), atom))
							{
								hm.put(new Double(BondTools.giveAngle(atom, parent, ((IAtom) chiralNeighbours.get(i)))), new Integer(i));
							}
						}
						Object[] ohere = hm.values().toArray();
						for (int i = 0; i < ohere.length; i++)
						{
							sorted[i] = ((IAtom) chiralNeighbours.get(((Integer) ohere[i]).intValue()));
						}
					}
					if (BondTools.isTrigonalBipyramidalOrOctahedral(container, atom)!=0)
					{
						sorted = new IAtom[container.getConnectedAtoms(atom).length - 1];
						TreeMap hm = new TreeMap();
						if (container.getBond(parent, atom).getStereo() == CDKConstants.STEREO_BOND_UP)
						{
							for (int i = 0; i < chiralNeighbours.size(); i++)
							{
								if (container.getBond(atom, (IAtom) chiralNeighbours.get(i)).getStereo() == 0)
								{
									hm.put(new Double(BondTools.giveAngle(atom, parent, ((IAtom) chiralNeighbours.get(i)))), new Integer(i));
								}
								if (container.getBond(atom, (IAtom) chiralNeighbours.get(i)).getStereo() == CDKConstants.STEREO_BOND_DOWN)
								{
									sorted[sorted.length - 1] = (IAtom) chiralNeighbours.get(i);
								}
							}
							Object[] ohere = hm.values().toArray();
							for (int i = 0; i < ohere.length; i++)
							{
								sorted[i] = ((IAtom) chiralNeighbours.get(((Integer) ohere[i]).intValue()));
							}
						}
						if (container.getBond(parent, atom).getStereo() == CDKConstants.STEREO_BOND_DOWN)
						{
							for (int i = 0; i < chiralNeighbours.size(); i++)
							{
								if (container.getBond(atom, (IAtom) chiralNeighbours.get(i)).getStereo() == 0)
								{
									hm.put(new Double(BondTools.giveAngle(atom, parent, ((IAtom) chiralNeighbours.get(i)))), new Integer(i));
								}
								if (container.getBond(atom, (IAtom) chiralNeighbours.get(i)).getStereo() == CDKConstants.STEREO_BOND_UP)
								{
									sorted[sorted.length - 1] = (IAtom) chiralNeighbours.get(i);
								}
							}
							Object[] ohere = hm.values().toArray();
							for (int i = 0; i < ohere.length; i++)
							{
								sorted[i] = ((IAtom) chiralNeighbours.get(((Integer) ohere[i]).intValue()));
							}
						}
						if (container.getBond(parent, atom).getStereo() == 0)
						{
							for (int i = 0; i < chiralNeighbours.size(); i++)
							{
								if (chiralNeighbours.get(i) != parent)
								{
									if (container.getBond(atom, (IAtom) chiralNeighbours.get(i)).getStereo() == 0)
									{
										hm.put(new Double((BondTools.giveAngleFromMiddle(atom, parent, ((IAtom) chiralNeighbours.get(i))))), new Integer(i));
									}
									if (container.getBond(atom, (IAtom) chiralNeighbours.get(i)).getStereo() == CDKConstants.STEREO_BOND_UP)
									{
										sorted[0] = (IAtom) chiralNeighbours.get(i);
									}
									if (container.getBond(atom, (IAtom) chiralNeighbours.get(i)).getStereo() == CDKConstants.STEREO_BOND_DOWN)
									{
										sorted[sorted.length - 2] = (IAtom) chiralNeighbours.get(i);
									}
								}
							}
							Object[] ohere = hm.values().toArray();
							sorted[sorted.length - 1] = ((IAtom) chiralNeighbours.get(((Integer) ohere[ohere.length - 1]).intValue()));
							if (ohere.length == 2)
							{
								sorted[sorted.length - 3] = ((IAtom) chiralNeighbours.get(((Integer) ohere[0]).intValue()));
								if (BondTools.giveAngleFromMiddle(atom, parent, ((IAtom) chiralNeighbours.get(((Integer) ohere[1]).intValue()))) < 0)
								{
									IAtom dummy = sorted[sorted.length - 2];
									sorted[sorted.length - 2] = sorted[0];
									sorted[0] = dummy;
								}
							}
							if (ohere.length == 3)
							{
								sorted[sorted.length - 3] = sorted[sorted.length - 2];
								sorted[sorted.length - 2] = ((IAtom) chiralNeighbours.get(((Integer) ohere[ohere.length - 2]).intValue()));
								sorted[sorted.length - 4] = ((IAtom) chiralNeighbours.get(((Integer) ohere[ohere.length - 3]).intValue()));
							}
						}
					}
					//This builds an onew[] containing the objects after the center of the chirality in the order given by sorted[]
					if (sorted != null)
					{
						int numberOfAtoms = 3;
						if (BondTools.isTrigonalBipyramidalOrOctahedral(container, atom)!=0)
						{
							numberOfAtoms = container.getConnectedAtoms(atom).length - 1;
						}
						Object[] omy = new Object[numberOfAtoms];
						Object[] onew = new Object[numberOfAtoms];
						for (int k = getRingOpenings(atom, null).size(); k < numberOfAtoms; k++)
						{
							if (positionInVector + 1 + k - getRingOpenings(atom, null).size() < v.size())
							{
								omy[k] = v.get(positionInVector + 1 + k - getRingOpenings(atom, null).size());
							}
						}
						for (int k = 0; k < sorted.length; k++)
						{
							if (sorted[k] != null)
							{
								for (int m = 0; m < omy.length; m++)
								{
									if (omy[m] instanceof IAtom)
									{
										if (omy[m] == sorted[k])
										{
											onew[k] = omy[m];
										}
									} else
									{
										if (omy[m] == null)
										{
											onew[k] = null;
										} else
										{
											if (((Vector) omy[m]).get(0) == sorted[k])
											{
												onew[k] = omy[m];
											}
										}
									}
								}
							} else
							{
								onew[k] = null;
							}
						}
						//This is a workaround for 3624.MOL.2 I don't have a better solution currently
						boolean doubleentry = false;
						for (int m = 0; m < onew.length; m++)
						{
							for (int k = 0; k < onew.length; k++)
							{
								if (m != k && onew[k] == onew[m])
								{
									doubleentry = true;
								}
							}
						}
						if (!doubleentry)
						{
							//Make sure that the first atom in onew is the first one in the original smiles order. This is important to have a canonical smiles.
							if (positionInVector + 1 < v.size())
							{
								Object atomAfterCenterInOriginalSmiles = v.get(positionInVector + 1);
								int l = 0;
								while (onew[0] != atomAfterCenterInOriginalSmiles)
								{
									Object placeholder = onew[onew.length - 1];
									for (int k = onew.length - 2; k > -1; k--)
									{
										onew[k + 1] = onew[k];
									}
									onew[0] = placeholder;
									l++;
									if (l > onew.length)
									{
										break;
									}
								}
							}
							//This cares about ring openings. Here the ring closure (represendted by a figure) must be the first atom. In onew the closure is null.
							if (getRingOpenings(atom, null).size() > 0)
							{
								int l = 0;
								while (onew[0] != null)
								{
									Object placeholder = onew[0];
									for (int k = 1; k < onew.length; k++)
									{
										onew[k - 1] = onew[k];
									}
									onew[onew.length - 1] = placeholder;
									l++;
									if (l > onew.length)
									{
										break;
									}
								}
							}
							//The last in onew is a vector: This means we need to exchange the rest of the original smiles with the rest of this vector.
							if (onew[numberOfAtoms - 1] instanceof Vector)
							{
								for (int i = 0; i < numberOfAtoms; i++)
								{
									if (onew[i] instanceof IAtom)
									{
										Vector vtemp = new Vector();
										vtemp.add(onew[i]);
										for (int k = positionInVector + 1 + numberOfAtoms; k < v.size(); k++)
										{
											vtemp.add(v.get(k));
										}
										onew[i] = vtemp;
										for (int k = v.size() - 1; k > positionInVector + 1 + numberOfAtoms - 1; k--)
										{
											v.remove(k);
										}
										for (int k = 1; k < ((Vector) onew[numberOfAtoms - 1]).size(); k++)
										{
											v.add(((Vector) onew[numberOfAtoms - 1]).get(k));
										}
										onew[numberOfAtoms - 1] = ((Vector) onew[numberOfAtoms - 1]).get(0);
										break;
									}
								}
							}
							//Put the onew objects in the original Vector
							int k = 0;
							for (int m = 0; m < onew.length; m++)
							{
								if (onew[m] != null)
								{
									v.set(positionInVector + 1 + k, onew[m]);
									k++;
								}
							}
						}
					}
				}
				parent = atom;
			} else
			{
				//Have Vector
				//System.out.println("in parseChain after else");
				boolean brackets = true;
				Vector result = new Vector();
				addAtoms((Vector) o, result);
				if (isRingOpening(parent, result) && container.getBondCount(parent) < 4)
				{
					brackets = false;
				}
				if (brackets)
				{
					buffer.append('(');
				}
				parseChain((Vector) o, buffer, container, parent, chiral, doubleBondConfiguration, atomsInOrderOfSmiles);
				if (brackets)
				{
					buffer.append(')');
				}
			}

			positionInVector++;
			//System.out.println("in parseChain after positionVector++");
		}
	}


	/**
	 *  Append the symbol for the bond order between <code>a1</code> and <code>a2</code>
	 *  to the <code>line</code>.
	 *
	 *@param  line           the StringBuffer that the bond symbol is appended to.
	 *@param  a1             Atom participating in the bond.
	 *@param  a2             Atom participating in the bond.
	 *@param  atomContainer  the AtomContainer that the SMILES string is generated
	 *      for.
	 */
	private void parseBond(StringBuffer line, IAtom a1, IAtom a2, IAtomContainer atomContainer)
	{
		//System.out.println("in parseBond()");
		if (a1.getFlag(CDKConstants.ISAROMATIC) && a1.getFlag(CDKConstants.ISAROMATIC))
		{
			return;
		}
		if (atomContainer.getBond(a1, a2) == null)
		{
			return;
		}
		int type = 0;
		type = (int) atomContainer.getBond(a1, a2).getOrder();
		if (type == 1)
		{
		} else if (type == 2)
		{
			line.append("=");

		} else if (type == 3)
		{
			line.append("#");
		} else
		{
			// //System.out.println("Unknown bond type");
		}
	}


	/**
	 *  Generates the SMILES string for the atom
	 *
	 *@param  a                        the atom to generate the SMILES for.
	 *@param  buffer                   the string buffer that the atom is to be
	 *      apended to.
	 *@param  container                the AtomContainer to analyze.
	 *@param  chiral                   is a chiral smiles wished?
	 *@param  parent                   the atom we came from.
	 *@param  atomsInOrderOfSmiles     a vector containing the atoms in the order
	 *      they are in the smiles.
	 *@param  currentChain             The chain we currently deal with.
	 *@param  doubleBondConfiguration  Description of Parameter
	 */
	private void parseAtom(IAtom a, StringBuffer buffer, IAtomContainer container, boolean chiral, boolean[] doubleBondConfiguration, IAtom parent, Vector atomsInOrderOfSmiles, Vector currentChain)
	{
		String symbol = a.getSymbol();
		boolean stereo = BondTools.isStereo(container, a);
		boolean brackets = symbol.equals("B") || symbol.equals("C") || symbol.equals("N") || symbol.equals("O") || symbol.equals("P") || symbol.equals("S") || symbol.equals("F") || symbol.equals("Br") || symbol.equals("I") || symbol.equals("Cl");
		brackets = !brackets;
		//System.out.println("in parseAtom()");
		//Deal with the start of a double bond configuration
		if (isStartOfDoubleBond(container, a, parent, doubleBondConfiguration))
		{
			buffer.append('/');
		}

		if (a instanceof IPseudoAtom)
		{
			buffer.append("[*]");
		} else
		{
			String mass = generateMassString(a);
			brackets = brackets | !mass.equals("");

			String charge = generateChargeString(a);
			brackets = brackets | !charge.equals("");

			if (chiral && stereo)
			{
				brackets = true;
			}
			if (brackets)
			{
				buffer.append('[');
			}
			buffer.append(mass);
			if (a.getFlag(CDKConstants.ISAROMATIC))
			{
				// Strictly speaking, this is wrong. Lower case is only used for sp2 atoms!
				buffer.append(a.getSymbol().toLowerCase());
			} else if (a.getHybridization() == CDKConstants.HYBRIDIZATION_SP2)
			{
				buffer.append(a.getSymbol().toLowerCase());
			} else
			{
				buffer.append(symbol);
			}
			if (a.getProperty(RING_CONFIG) != null && a.getProperty(RING_CONFIG).equals(UP))
			{
				buffer.append('/');
			}
			if (a.getProperty(RING_CONFIG) != null && a.getProperty(RING_CONFIG).equals(DOWN))
			{
				buffer.append('\\');
			}
			if (chiral && stereo && (BondTools.isTrigonalBipyramidalOrOctahedral(container, a)!=0 || BondTools.isSquarePlanar(container, a) || BondTools.isTetrahedral(container, a,false) != 0))
			{
				buffer.append('@');
			}
			if (chiral && stereo && BondTools.isSquarePlanar(container, a))
			{
				buffer.append("SP1");
			}
			//chiral
			//hcount
			buffer.append(charge);
			if (brackets)
			{
				buffer.append(']');
			}
		}
		//System.out.println("in parseAtom() after dealing with Pseudoatom or not");
		//Deal with the end of a double bond configuration
		if (isEndOfDoubleBond(container, a, parent, doubleBondConfiguration))
		{
			IAtom viewFrom = null;
			for (int i = 0; i < currentChain.size(); i++)
			{
				if (currentChain.get(i) == parent)
				{
					int k = i - 1;
					while (k > -1)
					{
						if (currentChain.get(k) instanceof IAtom)
						{
							viewFrom = (IAtom) currentChain.get(k);
							break;
						}
						k--;
					}
				}
			}
			if (viewFrom == null)
			{
				for (int i = 0; i < atomsInOrderOfSmiles.size(); i++)
				{
					if (atomsInOrderOfSmiles.get(i) == parent)
					{
						viewFrom = (IAtom) atomsInOrderOfSmiles.get(i - 1);
					}
				}
			}
			boolean afterThisAtom = false;
			IAtom viewTo = null;
			for (int i = 0; i < currentChain.size(); i++)
			{
				if (afterThisAtom && currentChain.get(i) instanceof IAtom)
				{
					viewTo = (IAtom) currentChain.get(i);
					break;
				}
				if (afterThisAtom && currentChain.get(i) instanceof Vector)
				{
					viewTo = (IAtom) ((Vector) currentChain.get(i)).get(0);
					break;
				}
				if (a == currentChain.get(i))
				{
					afterThisAtom = true;
				}
			}
      try{
        if (BondTools.isCisTrans(viewFrom,a,parent,viewTo,container))
        {
          buffer.append('\\');
        } else
        {
          buffer.append('/');
        }
      }catch(CDKException ex){
        //If the user wants a double bond configuration, where there is none, we ignore this.
      }
		}
		Vector v = new Vector();
		Iterator it = getRingOpenings(a, v).iterator();
		Iterator it2 = v.iterator();
		//System.out.println("in parseAtom() after checking for Ring openings");
		while (it.hasNext())
		{
			Integer integer = (Integer) it.next();
			IBond b = container.getBond((IAtom) it2.next(), a);
			int type = (int) b.getOrder();
			if (type == 2 && !b.getFlag(CDKConstants.ISAROMATIC))
			{
				buffer.append("=");
			} else if (type == 3 && !b.getFlag(CDKConstants.ISAROMATIC))
			{
				buffer.append("#");
			}
			buffer.append(integer);
		}
		atomsInOrderOfSmiles.add(a);
		//System.out.println("End of parseAtom()");
	}


	/**
	 *  Creates a string for the charge of atom <code>a</code>. If the charge is 1
	 *  + is returned if it is -1 - is returned. The positive values all have + in
	 *  front of them.
	 *
	 *@param  a  Description of Parameter
	 *@return    string representing the charge on <code>a</code>
	 */
	private String generateChargeString(IAtom a)
	{
		int charge = a.getFormalCharge();
		StringBuffer buffer = new StringBuffer(3);
		if (charge > 0)
		{
			//Positive
			buffer.append('+');
			if (charge > 1)
			{
				buffer.append(charge);
			}
		} else if (charge < 0)
		{
			//Negative
			if (charge == -1)
			{
				buffer.append('-');
			} else
			{
				buffer.append(charge);
			}
		}
		return buffer.toString();
	}


	/**
	 *  Creates a string containing the mass of the atom <code>a</code>. If the
	 *  mass is the same as the majour isotope an empty string is returned.
	 *
	 *@param  a  the atom to create the mass
	 *@return    Description of the Returned Value
	 */
	private String generateMassString(IAtom a)
	{
		IIsotope majorIsotope = isotopeFactory.getMajorIsotope(a.getSymbol());
		if (majorIsotope.getMassNumber() == a.getMassNumber())
		{
			return "";
		} else if (a.getMassNumber() == 0)
		{
			return "";
		} else
		{
			return Integer.toString(a.getMassNumber());
		}
	}


	/**
	 *  Description of the Class
	 *
	 *@author         shk3
	 *@cdk.created    2003-06-17
	 */
	class BrokenBond
	{

		/**
		 *  The atoms which close the ring
		 */
		private org.openscience.cdk.interfaces.IAtom a1, a2;

		/**
		 *  The number of the marker
		 */
		private int marker;


		/**
		 *  Construct a BrokenBond between <code>a1</code> and <code>a2</code> with
		 *  the marker <code>marker</code>.
		 *
		 *@param  marker  the ring closure marker. (Great comment!)
		 *@param  a1      Description of Parameter
		 *@param  a2      Description of Parameter
		 */
		BrokenBond(org.openscience.cdk.interfaces.IAtom a1, org.openscience.cdk.interfaces.IAtom a2, int marker)
		{
			this.a1 = a1;
			this.a2 = a2;
			this.marker = marker;
		}


		/**
		 *  Getter method for a1 property
		 *
		 *@return    The a1 value
		 */
		public org.openscience.cdk.interfaces.IAtom getA1()
		{
			return a1;
		}


		/**
		 *  Getter method for a2 property
		 *
		 *@return    The a2 value
		 */
		public org.openscience.cdk.interfaces.IAtom getA2()
		{
			return a2;
		}


		/**
		 *  Getter method for marker property
		 *
		 *@return    The marker value
		 */
		public int getMarker()
		{
			return marker;
		}


		/**
		 *  Description of the Method
		 *
		 *@return    Description of the Returned Value
		 */
		public String toString()
		{
			return Integer.toString(marker);
		}


		/**
		 *  Description of the Method
		 *
		 *@param  o  Description of Parameter
		 *@return    Description of the Returned Value
		 */
		public boolean equals(Object o)
		{
			if (!(o instanceof BrokenBond))
			{
				return false;
			}
			BrokenBond bond = (BrokenBond) o;
			return (a1.equals(bond.getA1()) && a2.equals(bond.getA2())) || (a1.equals(bond.getA2()) && a2.equals(bond.getA1()));
		}
	}


	/**
	 *  Returns the current AllRingsFinder instance
	 *
	 *@return   the current AllRingsFinder instance
	 */
	public AllRingsFinder getRingFinder()
	{
		return ringFinder;
	}


	/**
	 *  Sets the current AllRingsFinder instance
	 * Use this if you want to customize the timeout for 
	 * the AllRingsFinder. AllRingsFinder is stopping its 
	 * quest to find all rings after a default of 5 seconds.
	 *
	 * @see org.openscience.cdk.ringsearch.AllRingsFinder
	 * 
	 * @param  ringFinder  The value to assign ringFinder.
	 */
	public void setRingFinder(AllRingsFinder ringFinder)
	{
		this.ringFinder = ringFinder;
	}

}


