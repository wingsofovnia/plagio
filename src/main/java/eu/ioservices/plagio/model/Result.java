package eu.ioservices.plagio.model;

import java.io.Serializable;

/**
 * @author superuser
 *         Created 13-Aug-15
 */
public class Result implements Serializable {
    private final String docName;
    private final int docShingles;
    private final int coincidences;
    private final double duplicationLevel;

    public Result(Meta meta, int coincidences) {
        if (meta == null)
            throw new IllegalArgumentException("Meta is null");
        this.docName = meta.getName();
        this.docShingles = meta.getShinglesAmt();
        this.coincidences = coincidences >= 0 ? coincidences : 0;
        this.duplicationLevel = (double) this.coincidences / this.docShingles * 100.0;
    }

    public String getDocName() {
        return docName;
    }

    public int getDocShingles() {
        return docShingles;
    }

    public int getCoincidences() {
        return coincidences;
    }

    public double getDuplicationLevel() {
        return duplicationLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof Result))
            return false;

        Result result = (Result) o;

        return docName.equals(result.docName);

    }

    @Override
    public int hashCode() {
        return docName.hashCode();
    }

    @Override
    public String toString() {
        return "Result{" +
                "docName='" + docName + '\'' +
                ", docShingles=" + docShingles +
                ", coincidences=" + coincidences +
                ", duplicationLevel=" + duplicationLevel +
                '}';
    }
}
