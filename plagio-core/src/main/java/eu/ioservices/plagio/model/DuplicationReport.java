package eu.ioservices.plagio.model;

import java.io.Serializable;

/**
 * Result class encapsulates resulting data produced by Plagio and
 * contains information about documents and its duplication level.
 * <br/>
 * Result objects are {@link java.io.Serializable} because are used in distributed environment and may be replicated
 * through Spark nodes.
 *
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public class DuplicationReport implements Serializable {
    private final int docCoincidences;
    private final double duplicationLevel;
    private final Metadata metadata;

    public DuplicationReport(Metadata metadata, int docCoincidences) {
        this.metadata = metadata;
        this.docCoincidences = docCoincidences;

        double calcDuplication = (double) this.docCoincidences / metadata.getTotalShingles() * 100.0;
        this.duplicationLevel = calcDuplication > 100.0 ? 100.0 : calcDuplication;
    }

    public DuplicationReport(Metadata metadata, int docCoincidences, double duplicationLevel) {
        this.docCoincidences = docCoincidences;
        this.duplicationLevel = duplicationLevel;
        this.metadata = metadata;
    }

    public int getDocCoincidences() {
        return docCoincidences;
    }

    public double getDuplicationLevel() {
        return duplicationLevel;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "DuplicationReport{" +
                "docCoincidences=" + docCoincidences +
                ", duplicationLevel=" + duplicationLevel +
                ", documentMetadata=" + metadata +
                '}';
    }
}
