/*  Copyright (C) 2008  Arvid Berg <goglepox@users.sf.net>
 *
 *  Contact: cdk-devel@list.sourceforge.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.renderer.generators;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Point2d;

import org.openscience.cdk.geometry.GeometryUtil;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IElement;
import org.openscience.cdk.interfaces.IPseudoAtom;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.color.CDK2DAtomColors;
import org.openscience.cdk.renderer.color.IAtomColorer;
import org.openscience.cdk.renderer.elements.AtomSymbolElement;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.MarkedElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.elements.RectangleElement;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator.Scale;
import org.openscience.cdk.renderer.generators.parameter.AbstractGeneratorParameter;
import org.openscience.cdk.validate.ProblemMarker;

/**
 * Generates basic {@link IRenderingElement}s for atoms in an atom container.
 *
 */
public class BasicAtomGenerator implements IGenerator<IAtomContainer> {

    /** Class to hold the color by which atom labels are drawn.
     *  This color is overwritten by the {@link IAtomColorer}. */
    public static class AtomColor extends AbstractGeneratorParameter<Color> {

        /** Returns the default value.
         * @return {@link Color}.BLACK */
        @Override
        public Color getDefault() {
            return Color.BLACK;
        }
    }

    /** The default atom color. */
    private final IGeneratorParameter<Color> atomColor = new AtomColor();

    /** {@link IAtomColorer} used to draw elements. */
    public static class AtomColorer extends AbstractGeneratorParameter<IAtomColorer> {

        /** Returns the default value.
         * @return {@link CDK2DAtomColors} */
        @Override
        public IAtomColorer getDefault() {
            return new CDK2DAtomColors();
        }
    }

    /** Converter between atoms and colors. */
    private final IGeneratorParameter<IAtomColorer> atomColorer = new AtomColorer();

    /** Boolean property that triggers atoms to be colored by type
     *  when set to true. */
    public static class ColorByType extends AbstractGeneratorParameter<Boolean> {

        /** Returns the default value.
         * @return {@link Boolean}.TRUE */
        @Override
        public Boolean getDefault() {
            return Boolean.TRUE;
        }
    }

    /** If true, colors atoms by their type. */
    private final IGeneratorParameter<Boolean> colorByType = new ColorByType();

    /** Boolean property that triggers explicit hydrogens to be
     *  drawn if set to true. */
    public static class ShowExplicitHydrogens extends AbstractGeneratorParameter<Boolean> {

        /** Returns the default value.
         * @return {@link Boolean}.TRUE */
        @Override
        public Boolean getDefault() {
            return Boolean.TRUE;
        }
    }

    /** If true, explicit hydrogens are displayed. */
    private final IGeneratorParameter<Boolean> showExplicitHydrogens = new ShowExplicitHydrogens();

    /** Magic number with unknown units that defines the radius
     *  around an atom, e.g. used for highlighting atoms. */
    public static class AtomRadius extends AbstractGeneratorParameter<Double> {

        /** Returns the default value.
         * @return 8.0 */
        @Override
        public Double getDefault() {
            return 8.0;
        }
    }

    /** The atom radius on screen. */
    private final IGeneratorParameter<Double> atomRadius = new AtomRadius();

    /** Boolean parameters that will cause atoms to be drawn as
     *  filled shapes when set to true. The actual used shape used
     *  is defined by the {@link CompactShape} parameter. */
    public static class CompactAtom extends AbstractGeneratorParameter<Boolean> {

        /** Returns the default value.
         * @return {@link Boolean}.FALSE */
        @Override
        public Boolean getDefault() {
            return Boolean.FALSE;
        }
    }

    /** If true, atoms are displayed as 'compact' symbols, not text. */
    private final IGeneratorParameter<Boolean> isCompact = new CompactAtom();

    /** Determines whether structures should be drawn as Kekule structures, thus
     * giving each carbon element explicitly, instead of not displaying the
     * element symbol. Example C-C-C instead of /\.
     */
    public static class KekuleStructure extends AbstractGeneratorParameter<Boolean> {

        /** Returns the default value.
         * @return {@link Boolean}.FALSE */
        @Override
        public Boolean getDefault() {
            return Boolean.FALSE;
        }
    }

    /**
     * Determines whether structures should be drawn as Kekule structures, thus
     * giving each carbon element explicitly, instead of not displaying the
     * element symbol. Example C-C-C instead of /\.
     */
    private final IGeneratorParameter<Boolean> isKekule = new KekuleStructure();

    /**
     * When atoms are selected or in compact mode, they will
     * be covered by a shape determined by this enumeration.
     */
    public enum Shape {
        OVAL, SQUARE
    }

    /**
     * Shape to be used when drawing atoms in compact mode,
     * as defined by the {@link CompactAtom} parameter.
     */
    public static class CompactShape extends AbstractGeneratorParameter<Shape> {

        /** Returns the default value.
         * @return Shape.SQUARE */
        @Override
        public Shape getDefault() {
            return Shape.SQUARE;
        }
    }

    /** The compact shape used to display atoms when isCompact is true. */
    private final IGeneratorParameter<Shape> compactShape = new CompactShape();

    /** Boolean parameters that will show carbons with only one
     * (non-hydrogen) neighbor to be drawn with an element symbol.
     *  This setting overwrites and is used in combination with
     *  the {@link KekuleStructure} parameter.
     */
    public static class ShowEndCarbons extends AbstractGeneratorParameter<Boolean> {

        /** Returns the default value.
         * @return {@link Boolean}.FALSE */
        @Override
        public Boolean getDefault() {
            return Boolean.FALSE;
        }
    }

    /**
     * Determines whether methyl carbons' symbols should be drawn explicit for
     * methyl carbons. Example C/\C instead of /\.
     */
    private final IGeneratorParameter<Boolean> showEndCarbons = new ShowEndCarbons();

    /**
     * An empty constructor necessary for reflection.
     */
    public BasicAtomGenerator() {}

    /** {@inheritDoc} */
    @Override
    public IRenderingElement generate(IAtomContainer container, RendererModel model) {
        ElementGroup elementGroup = new ElementGroup();
        for (IAtom atom : container.atoms()) {
            elementGroup.add(MarkedElement.markupAtom(this.generate(container, atom, model), atom));
        }
        return elementGroup;
    }

    /**
     * Checks an atom to see if it has 2D coordinates.
     *
     * @param atom the atom to check
     * @return true if the atom is not null, and it has non-null coordinates
     */
    protected boolean hasCoordinates(IAtom atom) {
        return atom != null && atom.getPoint2d() != null;
    }

    /**
     * Determines if the atom is a hydrogen.
     *
     * @param  atom {@link IAtom} to be tested
     * @return      true, if the atom is a hydrogen, and false, otherwise.
     */
    protected boolean isHydrogen(IAtom atom) {
        return atom.getAtomicNumber() == IElement.H;
    }

    /**
     * Determines if the atom is a carbon.
     *
     * @param  atom {@link IAtom} to be tested
     * @return      true, if the atom is a carbon, and false, otherwise.
     */
    private boolean isCarbon(IAtom atom) {
        return atom.getAtomicNumber() == IElement.C;
    }

    /**
     * Checks an atom to see if it is an 'invisible hydrogen' - that is, it
     * is a) an (explicit) hydrogen, and b) explicit hydrogens are set to off.
     *
     * @param atom the atom to check
     * @param model the renderer model
     * @return true if this atom should not be shown
     */
    protected boolean invisibleHydrogen(IAtom atom, RendererModel model) {
        return isHydrogen(atom) && !(Boolean) model.get(ShowExplicitHydrogens.class);
    }

    /**
     * Checks an atom to see if it is an 'invisible carbon' - that is, it is:
     * a) a carbon atom and b) this carbon should not be shown.
     *
     * @param atom the atom to check
     * @param atomContainer the atom container the atom is part of
     * @param model the renderer model
     * @return true if this atom should not be shown
     */
    protected boolean invisibleCarbon(IAtom atom, IAtomContainer atomContainer, RendererModel model) {
        return isCarbon(atom) && !showCarbon(atom, atomContainer, model);
    }

    /**
     * Checks an atom to see if it should be drawn. There are three reasons
     * not to draw an atom - a) no coordinates, b) an invisible hydrogen or
     * c) an invisible carbon.
     *
     * @param atom the atom to check
     * @param container the atom container the atom is part of
     * @param model the renderer model
     * @return true if the atom should be drawn
     */
    protected boolean canDraw(IAtom atom, IAtomContainer container, RendererModel model) {
        // don't draw atoms without coordinates
        if (!hasCoordinates(atom)) {
            return false;
        }

        // don't draw invisible hydrogens
        if (invisibleHydrogen(atom, model)) {
            return false;
        }

        // don't draw invisible carbons
        if (invisibleCarbon(atom, container, model)) {
            return false;
        }

        return true;
    }

    /**
     * Generate the rendering element(s) for a particular atom.
     *
     * @param atomContainer the atom container that the atom is from
     * @param atom the atom to generate the rendering element for
     * @param model the renderer model
     * @return a rendering element, or group of elements
     */
    public IRenderingElement generate(IAtomContainer atomContainer, IAtom atom, RendererModel model) {
        if (!canDraw(atom, atomContainer, model)) {
            return null;
        } else if (model.get(CompactAtom.class)) {
            return this.generateCompactElement(atom, model);
        } else {
            int alignment;
            if (atom.getAtomicNumber() == IElement.C) {
                alignment = GeometryUtil.getBestAlignmentForLabel(atomContainer, atom);
            } else {
                alignment = GeometryUtil.getBestAlignmentForLabelXY(atomContainer, atom);
            }

            return generateElement(atom, alignment, model);
        }
    }

    /**
     * Generate a compact element for an atom, such as a circle or a square,
     * rather than text element.
     *
     * @param atom the atom to generate the compact element for
     * @param model the renderer model
     * @return a compact rendering element
     */
    public IRenderingElement generateCompactElement(IAtom atom, RendererModel model) {
        Point2d point = atom.getPoint2d();
        double radius = model.get(AtomRadius.class) / model.getParameter(Scale.class).getValue();
        double distance = 2 * radius;
        if (model.get(CompactShape.class) == Shape.SQUARE) {
            return new RectangleElement(point.x - radius, point.y - radius, distance, distance, true, getAtomColor(
                    atom, model));
        } else {
            return new OvalElement(point.x, point.y, radius, true, getAtomColor(atom, model));
        }
    }

    /**
     * Generate an atom symbol element.
     *
     * @param atom the atom to use
     * @param alignment the alignment of the atom's label
     * @param model the renderer model
     * @return an atom symbol element
     */
    public AtomSymbolElement generateElement(IAtom atom, int alignment, RendererModel model) {
        String text;
        if (atom instanceof IPseudoAtom) {
            text = ((IPseudoAtom) atom).getLabel();
        } else {
            text = atom.getSymbol();
        }
        return new AtomSymbolElement(atom.getPoint2d().x, atom.getPoint2d().y, text, atom.getFormalCharge(),
                atom.getImplicitHydrogenCount(), alignment, getAtomColor(atom, model));
    }

    /**
     * Checks a carbon atom to see if it should be shown.
     *
     * @param carbonAtom the carbon atom to check
     * @param container the atom container
     * @param model the renderer model
     * @return true if the carbon should be shown
     */
    protected boolean showCarbon(IAtom carbonAtom, IAtomContainer container, RendererModel model) {

        if (model.get(KekuleStructure.class)) return true;

        if (carbonAtom.getFormalCharge() != 0) return true;

        int connectedBondCount = container.getConnectedBondsList(carbonAtom).size();

        if (connectedBondCount < 1) return true;

        if (model.get(ShowEndCarbons.class) && connectedBondCount == 1) return true;

        if (carbonAtom.getProperty(ProblemMarker.ERROR_MARKER) != null) return true;

        if (container.getConnectedSingleElectronsCount(carbonAtom) > 0) return true;

        return false;
    }

    /**
     * Returns the drawing color of the given atom. An atom is colored as
     * highlighted if highlighted. The atom is color marked if in a
     * substructure. If not, the color from the CDK2DAtomColor is used (if
     * selected). Otherwise, the atom is colored black.
     */
    protected Color getAtomColor(IAtom atom, RendererModel model) {
        Color atomColor = model.get(AtomColor.class);
        if (model.get(ColorByType.class)) {
            atomColor = model.get(AtomColorer.class).getAtomColor(atom);
        }
        return atomColor;
    }

    /** {@inheritDoc} */
    @Override
    public List<IGeneratorParameter<?>> getParameters() {
        return Arrays.asList(new IGeneratorParameter<?>[]{atomColor, atomColorer, atomRadius, colorByType,
                compactShape, isCompact, isKekule, showEndCarbons, showExplicitHydrogens});
    }
}
