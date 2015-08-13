package ua.cv.ovchynnikov.pojo;

import java.io.Serializable;

/**
 * @author superuser
 *         Created 13-Aug-15
 */
public class Result implements Serializable {
    private final String docPath;
    private final int docShingles;
    private final int coincidences;
    private final double duplicationLevel;

    public Result(DocumentMeta meta, int coincidences) {
        if (meta == null)
            throw new IllegalArgumentException("Meta is null");
        this.docPath = meta.getPath();
        this.docShingles = meta.getShinglesAmount();
        this.coincidences = coincidences >= 0 ? coincidences : 0;
        this.duplicationLevel = (double) this.coincidences / this.docShingles * 100.0;
    }

    public String getDocPath() {
        return docPath;
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

        return docPath.equals(result.docPath);

    }

    @Override
    public int hashCode() {
        return docPath.hashCode();
    }

    @Override
    public String toString() {
        return "Result {" +
                "document path = '" + docPath + '\'' +
                ", distinct shingles = " + docShingles +
                ", coincidences = " + coincidences +
                ", duplication level = " + duplicationLevel +
                '}';
    }
}
