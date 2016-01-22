package eu.ioservices.plagio.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 *         Created 27-Dec-15
 */
public class Metadata implements Serializable {
    private String documentId;
    private int totalShingles;
    private boolean isMarked;

    public Metadata(String documentId, int totalShingles) {
        this(documentId, totalShingles, false);
    }

    public Metadata(String documentId, int totalShingles, boolean isMarked) {
        this.documentId = Objects.requireNonNull(documentId);
        this.totalShingles = totalShingles;
        this.isMarked = isMarked;
    }

    public String getDocumentId() {
        return documentId;
    }

    public int getTotalShingles() {
        return totalShingles;
    }

    public boolean isMarked() {
        return isMarked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Metadata)) {
            return false;
        }

        Metadata that = (Metadata) o;

        if (isMarked != that.isMarked) {
            return false;
        }
        if (totalShingles != that.totalShingles) {
            return false;
        }
        if (!documentId.equals(that.documentId)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = documentId.hashCode();
        result = 31 * result + totalShingles;
        result = 31 * result + (isMarked ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DocumentMetadata{" +
                "documentId='" + documentId + '\'' +
                ", totalShingles=" + totalShingles +
                ", isMarked=" + isMarked +
                '}';
    }
}
