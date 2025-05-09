/* Copyright (C) 2004-2007  The Chemistry Development Kit (CDK) project
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.io.formats;

import org.openscience.cdk.tools.DataFeatures;

/**
 * See <a href="http://www.uku.fi/~thassine/ghemical/">here</a>.
 *
 */
public class GhemicalMMFormat extends SimpleChemFormatMatcher implements IChemFormatMatcher {

    private static IResourceFormat myself = null;

    public GhemicalMMFormat() {}

    public static IResourceFormat getInstance() {
        if (myself == null) myself = new GhemicalMMFormat();
        return myself;
    }

    /** {@inheritDoc} */
    @Override
    public String getFormatName() {
        return "Ghemical Quantum/Molecular Mechanics Model";
    }

    /** {@inheritDoc} */
    @Override
    public String getMIMEType() {
        return "application/x-ghemical";
    }

    /** {@inheritDoc} */
    @Override
    public String getPreferredNameExtension() {
        return getNameExtensions()[0];
    }

    /** {@inheritDoc} */
    @Override
    public String[] getNameExtensions() {
        return new String[]{"gpr"};
    }

    /** {@inheritDoc} */
    @Override
    public String getReaderClassName() {
        return "org.openscience.cdk.io.GhemicalMMReader";
    }

    /** {@inheritDoc} */
    @Override
    public String getWriterClassName() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean matches(int lineNumber, String line) {
        if (line.startsWith("!Header gpr")) {
            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isXMLBased() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int getSupportedDataFeatures() {
        return DataFeatures.NONE;
    }

    /** {@inheritDoc} */
    @Override
    public int getRequiredDataFeatures() {
        return DataFeatures.NONE;
    }
}
