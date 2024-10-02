/* Copyright (C) 2024 Beilstein-Institute
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
package org.openscience.cdk.rinchi;

class RInChIConsts {
    protected static String RINCHI_VERSION = "1.00";
    protected static String RINCHI_IDENTIFIER = "RInChI" + RINCHI_VERSION;

    protected static String INCHI_STD_HEADER = "InChI=1S/";
    protected static String RINCHI_STD_HEADER = "RInChI=" + RINCHI_VERSION + ".1S/";

    protected static String INCHI_AUXINFO_HEADER = "AuxInfo=1/";
    protected static String RINCHI_AUXINFO_HEADER = "RAuxInfo=" + RINCHI_VERSION + ".1/";

    // Delimiters in RInChI strings.
    protected static String DELIM_LAYER = "/";
    protected static String DELIM_COMP = "!";
    protected static String DELIM_GROUP = "<>";

    protected static String DIRECTION_TAG = "/d";
    protected static char DIRECTION_FORWARD = '+';
    protected static char DIRECTION_REVERSE = '-';
    protected static char DIRECTION_EQUILIBRIUM = '=';

    protected static String NOSTRUCT_INCHI = INCHI_STD_HEADER + DELIM_LAYER;
    protected static String NOSTRUCT_AUXINFO = INCHI_AUXINFO_HEADER + DELIM_LAYER;
    protected static String NOSTRUCT_RINCHI_LONGKEY = "MOSFIJXAXDLOML-UHFFFAOYSA-N";

    protected static String NOSTRUCT_TAG = "/u";
    protected static char NOSTRUCT_DELIM = '-';

    protected static String RINCHI_LONG_KEY_HEADER = "Long-RInChIKey=";
    protected static String RINCHI_SHORT_KEY_HEADER = "Short-RInChIKey=";
    protected static String RINCHI_WEB_KEY_HEADER = "Web-RInChIKey=";

    protected static String RINCHI_KEY_VERSION_ID_HEADER = "SA";

    // Delimiters in RInChI keys.
    protected static String KEY_DELIM_BLOCK = "-";
    protected static String KEY_DELIM_COMP = "-";
    protected static String KEY_DELIM_GROUP = "--";

    //Empty hashes
    protected static String HASH_04_EMPTY_STRING = "UHFF";
    protected static String HASH_10_EMPTY_STRING = "UHFFFADPSC";
    protected static String HASH_12_EMPTY_STRING = "UHFFFADPSCTJ";
    protected static String HASH_14_EMPTY_STRING = "UHFFFADPSCTJAU";
    protected static String HASH_17_EMPTY_STRING = "UHFFFADPSCTJAUYIS";
}
