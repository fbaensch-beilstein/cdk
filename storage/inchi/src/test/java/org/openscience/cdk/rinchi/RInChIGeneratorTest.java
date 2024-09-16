package org.openscience.cdk.rinchi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.Reaction;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.io.MDLRXNV2000Reader;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

class RInChIGeneratorTest {

    @Test
    public void directionForwardTest() throws Exception {
        String molFile = "$RXN\n" +
                "\n" +
                " -INDIGO- 0916241043\n" +
                "\n" +
                "  1  1\n" +
                "$MOL\n" +
                "\n" +
                "  -INDIGO-09162410432D\n" +
                "\n" +
                "  2  1  0  0  0  0  0  0  0  0999 V2000\n" +
                "    2.0000   -5.3500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "    2.8660   -4.8500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "  1  2  1  0  0  0  0\n" +
                "M  END\n" +
                "$MOL\n" +
                "\n" +
                "  -INDIGO-09162410432D\n" +
                "\n" +
                "  3  2  0  0  0  0  0  0  0  0999 V2000\n" +
                "    7.9500   -6.1250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "    8.6571   -5.4179    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "    9.5231   -5.9179    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "  1  2  1  0  0  0  0\n" +
                "  2  3  1  0  0  0  0\n" +
                "M  END\n";

        MDLRXNV2000Reader reader = new MDLRXNV2000Reader(new ByteArrayInputStream(molFile.getBytes(StandardCharsets.UTF_8)));
        IReaction reaction = new Reaction();
        reaction = reader.read(reaction);
        reader.close();

        RInChIGenerator gen = new RInChIGenerator(reaction);
        String expectedRinchi = "RInChI=1.00.1S/C2H6/c1-2/h1-2H3<>C3H8/c1-3-2/h3H2,1-2H3/d+";
        Assertions.assertEquals(expectedRinchi, gen.getRInChI());

        String expectedLongKey = "Long-RInChIKey=SA-FUHFF-OTMSDBZUPAUEDD-UHFFFAOYSA-N--ATUOYWHBWRKTHZ-UHFFFAOYSA-N";
        Assertions.assertEquals(expectedLongKey, gen.getLongRInChIKey());
    }

    @Test
    public void directionForward2Test() throws Exception {
        String molFile = "$RXN\n" +
                "\n" +
                " -INDIGO- 0916241153\n" +
                "\n" +
                "  1  1\n" +
                "$MOL\n" +
                "\n" +
                "  -INDIGO-09162411532D\n" +
                "\n" +
                "  3  2  0  0  0  0  0  0  0  0999 V2000\n" +
                "    2.0420   -5.6750    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "    2.9080   -5.1750    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "    3.7740   -5.6750    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "  1  2  1  0  0  0  0\n" +
                "  2  3  1  0  0  0  0\n" +
                "M  END\n" +
                "$MOL\n" +
                "\n" +
                "  -INDIGO-09162411532D\n" +
                "\n" +
                "  2  1  0  0  0  0  0  0  0  0999 V2000\n" +
                "   10.8420   -5.9750    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "   11.7080   -5.4750    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "  1  2  1  0  0  0  0\n" +
                "M  END\n";

        MDLRXNV2000Reader reader = new MDLRXNV2000Reader(new ByteArrayInputStream(molFile.getBytes(StandardCharsets.UTF_8)));
        IReaction reaction = new Reaction();
        reaction = reader.read(reaction);
        reader.close();

        RInChIGenerator gen = new RInChIGenerator(reaction);
        String expected = "RInChI=1.00.1S/C2H6/c1-2/h1-2H3<>C3H8/c1-3-2/h3H2,1-2H3/d-";
        Assertions.assertEquals(expected, gen.getRInChI());

        String expectedLongKey = "Long-RInChIKey=SA-BUHFF-OTMSDBZUPAUEDD-UHFFFAOYSA-N--ATUOYWHBWRKTHZ-UHFFFAOYSA-N";
        Assertions.assertEquals(expectedLongKey, gen.getLongRInChIKey());
    }

    @Test
    public void directionBackwardTest() throws Exception {
        String molFile = "$RXN\n" +
                "\n" +
                " -INDIGO- 0916241108\n" +
                "\n" +
                "  1  1\n" +
                "$MOL\n" +
                "\n" +
                "  -INDIGO-09162411082D\n" +
                "\n" +
                "  3  2  0  0  0  0  0  0  0  0999 V2000\n" +
                "    7.9500   -6.1250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "    8.6571   -5.4179    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "    9.5231   -5.9179    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "  1  2  1  0  0  0  0\n" +
                "  2  3  1  0  0  0  0\n" +
                "M  END\n" +
                "$MOL\n" +
                "\n" +
                "  -INDIGO-09162411082D\n" +
                "\n" +
                "  2  1  0  0  0  0  0  0  0  0999 V2000\n" +
                "    2.0000   -5.3500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "    2.8660   -4.8500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "  1  2  1  0  0  0  0\n" +
                "M  END\n";

        MDLRXNV2000Reader reader = new MDLRXNV2000Reader(new ByteArrayInputStream(molFile.getBytes(StandardCharsets.UTF_8)));
        IReaction reaction = new Reaction();
        reaction = reader.read(reaction);
        reader.close();

        RInChIGenerator gen = new RInChIGenerator(reaction);
        String expected = "RInChI=1.00.1S/C2H6/c1-2/h1-2H3<>C3H8/c1-3-2/h3H2,1-2H3/d-";
        Assertions.assertEquals(expected, gen.getRInChI());

        String expectedLongKey = "Long-RInChIKey=SA-BUHFF-OTMSDBZUPAUEDD-UHFFFAOYSA-N--ATUOYWHBWRKTHZ-UHFFFAOYSA-N";
        Assertions.assertEquals(expectedLongKey, gen.getLongRInChIKey());

        String expectedRAuxInfo = "RAuxInfo=1.00.1/0/N:1,2/E:(1,2)/rA:2nCC/rB:s1;/rC:2,0000,-5,3500,0;2,8660,-4,8500,0;<>0/N:1,3,2/E:(1,2)/rA:3nCCC/rB:s1;s2;/rC:7,9500,-6,1250,0;8,6571,-5,4179,0;9,5231,-5,9179,0;";
        Assertions.assertEquals(expectedRAuxInfo, gen.getRAuxInfo());
    }

    @Test
    public void directionBackward2Test() throws Exception {
        String molFile = "$RXN\n" +
                "\n" +
                " -INDIGO- 0916241158\n" +
                "\n" +
                "  1  1\n" +
                "$MOL\n" +
                "\n" +
                "  -INDIGO-09162411582D\n" +
                "\n" +
                "  2  1  0  0  0  0  0  0  0  0999 V2000\n" +
                "   10.8420   -5.9750    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "   11.7080   -5.4750    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "  1  2  1  0  0  0  0\n" +
                "M  END\n" +
                "$MOL\n" +
                "\n" +
                "  -INDIGO-09162411582D\n" +
                "\n" +
                "  3  2  0  0  0  0  0  0  0  0999 V2000\n" +
                "    2.0420   -5.6750    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "    2.9080   -5.1750    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "    3.7740   -5.6750    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "  1  2  1  0  0  0  0\n" +
                "  2  3  1  0  0  0  0\n" +
                "M  END\n";

        MDLRXNV2000Reader reader = new MDLRXNV2000Reader(new ByteArrayInputStream(molFile.getBytes(StandardCharsets.UTF_8)));
        IReaction reaction = new Reaction();
        reaction = reader.read(reaction);
        reader.close();

        RInChIGenerator gen = new RInChIGenerator(reaction);
        String expected = "RInChI=1.00.1S/C2H6/c1-2/h1-2H3<>C3H8/c1-3-2/h3H2,1-2H3/d+";
        Assertions.assertEquals(expected, gen.getRInChI());

        String expectedLongKey = "Long-RInChIKey=SA-FUHFF-OTMSDBZUPAUEDD-UHFFFAOYSA-N--ATUOYWHBWRKTHZ-UHFFFAOYSA-N";
        Assertions.assertEquals(expectedLongKey, gen.getLongRInChIKey());
    }

    @Test
    public void directionEquilibriumTest() throws Exception {
        String molFile = "$RXN\n" +
                "\n" +
                " -INDIGO- 0916241108\n" +
                "\n" +
                "  1  1\n" +
                "$MOL\n" +
                "\n" +
                "  -INDIGO-09162411082D\n" +
                "\n" +
                "  3  2  0  0  0  0  0  0  0  0999 V2000\n" +
                "    7.9500   -6.1250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "    8.6571   -5.4179    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "    9.5231   -5.9179    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "  1  2  1  0  0  0  0\n" +
                "  2  3  1  0  0  0  0\n" +
                "M  END\n" +
                "$MOL\n" +
                "\n" +
                "  -INDIGO-09162411082D\n" +
                "\n" +
                "  2  1  0  0  0  0  0  0  0  0999 V2000\n" +
                "    2.0000   -5.3500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "    2.8660   -4.8500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "  1  2  1  0  0  0  0\n" +
                "M  END\n";

        MDLRXNV2000Reader reader = new MDLRXNV2000Reader(new ByteArrayInputStream(molFile.getBytes(StandardCharsets.UTF_8)));
        IReaction reaction = new Reaction();
        reaction = reader.read(reaction);
        reader.close();
        RInChIGenerator gen = new RInChIGenerator(reaction, true);
        String expected = "RInChI=1.00.1S/C2H6/c1-2/h1-2H3<>C3H8/c1-3-2/h3H2,1-2H3/d=";
        Assertions.assertEquals(expected, gen.getRInChI());

        String expectedLongKey = "Long-RInChIKey=SA-EUHFF-OTMSDBZUPAUEDD-UHFFFAOYSA-N--ATUOYWHBWRKTHZ-UHFFFAOYSA-N";
        Assertions.assertEquals(expectedLongKey, gen.getLongRInChIKey());
    }

    @Test
    public void noReactantTest() throws Exception {
        String molFile = "$RXN\n" +
                "\n" +
                " -INDIGO- 0916241528\n" +
                "\n" +
                "  0  1\n" +
                "$MOL\n" +
                "\n" +
                "  -INDIGO-09162415282D\n" +
                "\n" +
                "  2  1  0  0  0  0  0  0  0  0999 V2000\n" +
                "    7.6250   -6.7750    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "    8.4910   -6.2750    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "  1  2  1  0  0  0  0\n" +
                "M  END\n";

        MDLRXNV2000Reader reader = new MDLRXNV2000Reader(new ByteArrayInputStream(molFile.getBytes(StandardCharsets.UTF_8)));
        IReaction reaction = new Reaction();
        reaction = reader.read(reaction);
        reader.close();
        RInChIGenerator gen = new RInChIGenerator(reaction);
        String expected = "RInChI=1.00.1S/<>C2H6/c1-2/h1-2H3/d+";
        Assertions.assertEquals(expected, gen.getRInChI());

        String expectedLongKey = "Long-RInChIKey=SA-FUHFF---OTMSDBZUPAUEDD-UHFFFAOYSA-N";
        Assertions.assertEquals(expectedLongKey, gen.getLongRInChIKey());
    }

    @Test
    public void noProductTest() throws Exception {
        String molFile = "$RXN\n" +
                "\n" +
                " -INDIGO- 0916241535\n" +
                "\n" +
                "  1  0\n" +
                "$MOL\n" +
                "\n" +
                "  -INDIGO-09162415352D\n" +
                "\n" +
                "  3  2  0  0  0  0  0  0  0  0999 V2000\n" +
                "    2.2830   -5.9250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "    1.4170   -6.4250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "    2.7830   -6.7910    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "  2  1  1  0  0  0  0\n" +
                "  1  3  1  0  0  0  0\n" +
                "M  END\n";

        MDLRXNV2000Reader reader = new MDLRXNV2000Reader(new ByteArrayInputStream(molFile.getBytes(StandardCharsets.UTF_8)));
        IReaction reaction = new Reaction();
        reaction = reader.read(reaction);
        reader.close();
        RInChIGenerator gen = new RInChIGenerator(reaction);
        String expected = "RInChI=1.00.1S/<>C3H8/c1-3-2/h3H2,1-2H3/d-";
        Assertions.assertEquals(expected, gen.getRInChI());

        String expectedLongKey = "Long-RInChIKey=SA-BUHFF---ATUOYWHBWRKTHZ-UHFFFAOYSA-N";
        Assertions.assertEquals(expectedLongKey, gen.getLongRInChIKey());
    }


}
