/* Function.java
 *
 * Copyright (C) 1997-2007  Stephan Michels <stephan@vern.chem.tu-berlin.de>
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

package org.openscience.cdk.math;

/**
 * A class, which has a function value should implement this interface.
 *
 */
public interface IFunction {

    /**
     * Return the function value at (x,y,z)
     */
    double getValue(double x, double y, double z);

    /**
     * Return the function value
     *
     * The rows of the matrix x are the Parameters like x,y,z
     * and the columns are the values which must calculated.
     */
    Vector getValues(Matrix x);
}
