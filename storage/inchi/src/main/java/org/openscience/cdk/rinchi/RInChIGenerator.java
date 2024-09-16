package org.openscience.cdk.rinchi;

import io.github.dan2097.jnainchi.*;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

import java.util.*;
import java.util.stream.Collectors;

public class RInChIGenerator {

    /**
     * Parameter of RInChI to force the reaction to be interpreted as an equilibrium reaction.
     */
    private final boolean forceEquilibrium;

    private static final String RINCHI_VERSION = "1.00";
    private static final String INCHI_VERSION  = "1S";
    private static final Object lock           = new Object();

    private String rInChI;
    private String longRInChIKey;
    private String rAuxInfo;

    /**
     * RInChI sorts second and third layer, which contains reactants and products, based on the alphabetical order
     * of their InChIs. The direction of the reaction is shown in layer four and indicates whether layer two or three
     * represents the reactants.
     */
    private boolean productsFirst;

    private static final ILoggingTool LOGGER = LoggingToolFactory.createLoggingTool(RInChIGenerator.class);

    public RInChIGenerator(IReaction reaction, boolean forceEquilibrium) throws CDKException {
        Objects.requireNonNull(reaction, "Reaction must not be null.");
        this.forceEquilibrium = forceEquilibrium;
        this.generateRInChIFromIReaction(reaction);
    }

    public RInChIGenerator(IReaction reaction) throws CDKException {
        this(reaction, false);
    }

    private void generateRInChIFromIReaction(IReaction reaction) throws CDKException {
        List<InChIGenerator> reactants = new ArrayList<>();
        List<InChIGenerator> products = new ArrayList<>();
        List<InChIGenerator> agents = new ArrayList<>();

        //create InChIs etc. for each component
        for(IAtomContainer ac : reaction.getReactants()){
            reactants.add(getInChIGen(ac));
        }
        for(IAtomContainer ac : reaction.getProducts()){
            products.add(getInChIGen(ac));
        }
        for(IAtomContainer ac : reaction.getAgents()){
            agents.add(getInChIGen(ac));
        }

        reactants.sort(Comparator.comparing(InChIGenerator::getInchi));
        products.sort(Comparator.comparing(InChIGenerator::getInchi));
        agents.sort(Comparator.comparing(InChIGenerator::getInchi));

        String reactantsPart = reactants.stream().map(s -> s.getInchi().substring("InChI=1S/".length())).collect(Collectors.joining("!"));
        String productsPart = products.stream().map(s -> s.getInchi().substring("InChI=1S/".length())).collect(Collectors.joining("!"));
        String agentsPart = agents.stream().map(s -> s.getInchi().substring("InChI=1S/".length())).collect(Collectors.joining("!"));

        String reactantsLongKey = reactants.stream().map(s -> this.getInChIKey(s.getInchi())).collect(Collectors.joining("-"));
        String productsLongKey = products.stream().map(s -> this.getInChIKey(s.getInchi())).collect(Collectors.joining("-"));
        String agentsLongKey = agents.stream().map(s -> this.getInChIKey(s.getInchi())).collect(Collectors.joining("-"));

        String reactantsAuxInfo = reactants.stream().map(InChIGenerator::getAuxInfo).
                map(s -> s.substring("AuxInfo=1/".length())).collect(Collectors.joining("!"));
        String productsAuxInfo = products.stream().map(InChIGenerator::getAuxInfo).
                map(s -> s.substring("AuxInfo=1/".length())).collect(Collectors.joining("!"));
        String agentsAuxInfo = agents.stream().map(InChIGenerator::getAuxInfo).
                map(s -> s.substring("AuxInfo=1/".length())).collect(Collectors.joining("!"));

        StringBuilder sbRinchi = new StringBuilder();
        sbRinchi.append("RInChI=")
                .append(RINCHI_VERSION)
                .append(".")
                .append(INCHI_VERSION)
                .append("/");

        StringBuilder sbLongKey = new StringBuilder();
        sbLongKey.append("Long-RInChIKey=SA-");

        StringBuilder sbRAuxInfo = new StringBuilder();
        sbRAuxInfo.append("RAuxInfo=1.00.1/");

        // direction forwards?
        if (reactantsPart.compareTo(productsPart) <= 0) {
            //RInChI
            this.constructRInChIString(sbRinchi, reactantsPart, productsPart, agentsPart, "<>");
            sbRinchi.append("/d+");
            //RAuxInfo
            this.constructRInChIString(sbRAuxInfo, reactantsAuxInfo, productsAuxInfo, agentsAuxInfo, "<>");
            //Long-RInChIKey
            sbLongKey.append("FUHFF-");
            this.constructRInChIString(sbLongKey, reactantsLongKey, productsLongKey, agentsLongKey, "--");
        } else {  // ... direction => backwards
            //RInChI
            this.constructRInChIString(sbRinchi, productsPart, reactantsPart, agentsPart, "<>");
            sbRinchi.append("/d-");
            //RAuxInfo
            this.constructRInChIString(sbRAuxInfo, productsAuxInfo, reactantsAuxInfo, agentsAuxInfo, "<>");
            //Long-RInChIKey
            sbLongKey.append("BUHFF-");
            this.constructRInChIString(sbLongKey, productsLongKey, reactantsLongKey, agentsLongKey, "--");
        }
        if (forceEquilibrium || reaction.getDirection() == IReaction.Direction.BIDIRECTIONAL){
            sbRinchi.replace(sbRinchi.length()-1, sbRinchi.length(), "=");
            sbLongKey.replace(18,19, "E"); // position 18 defines direction in Long-RInChIKey
        }
        rInChI = sbRinchi.toString();
        longRInChIKey = sbLongKey.toString();
        rAuxInfo = sbRAuxInfo.toString();
    }

    private InChIGenerator getInChIGen(IAtomContainer atomContainer) {
        try {
            // InChI was not thread safe pre 1.0.5
            synchronized (lock) {
                InChIGenerator gen = InChIGeneratorFactory.getInstance().getInChIGenerator(atomContainer, new InchiOptions.InchiOptionsBuilder().build());
                if (gen.getStatus() == InchiStatus.SUCCESS)
                    return gen;
                else
                    return null;
            }
        } catch (CDKException e) {
            return null;
        }
    }

    private String getInChIKey(String inchi) {
        try {
            InchiKeyOutput inchiKeyOutput = JnaInchi.inchiToInchiKey(inchi);
            if (inchiKeyOutput.getStatus() == InchiKeyStatus.OK)
                return inchiKeyOutput.getInchiKey();
            else
                throw new CDKException("Error while creating InChIKey: " + inchiKeyOutput.getStatus());
        } catch (CDKException e) {
            return null;
        }
    }

    private void constructRInChIString(StringBuilder sb, String layerTwo, String layerThree, String layerFour, String delimiter) {
        sb.append(layerTwo);
        sb.append(delimiter);
        sb.append(layerThree);
        if (!layerFour.isEmpty()) {
            sb.append(delimiter);
            sb.append(layerFour);
        }
    }

    public String getRInChI() {
        return this.rInChI;
    }

    public String getLongRInChIKey() {
        return this.longRInChIKey;
    }

    public String getRAuxInfo() {
        return this.rAuxInfo;
    }
}
