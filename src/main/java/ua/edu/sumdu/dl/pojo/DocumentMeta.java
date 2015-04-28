package ua.edu.sumdu.dl.pojo;

import java.io.Serializable;

/**
 * @author superuser
 *         Created 20-Apr-15
 */

public class DocumentMeta implements Serializable {
    private int documentId;
    private String name;

    private int shinglesAmount;
    private boolean marked;

    public DocumentMeta(int documentId) {
        this.documentId = documentId;
    }

    public DocumentMeta(int documentId, String name) {
        this.documentId = documentId;
        this.name = name;
    }

    public DocumentMeta(int documentId, String name, boolean marked) {
        this.documentId = documentId;
        this.name = name;
        this.marked = marked;
    }

    public DocumentMeta(int documentId, String name, boolean marked, int shinglesAmount) {
        this.documentId = documentId;
        this.name = name;
        this.shinglesAmount = shinglesAmount;
        this.marked = marked;
    }

    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getShinglesAmount() {
        return shinglesAmount;
    }

    public void setShinglesAmount(int shinglesAmount) {
        this.shinglesAmount = shinglesAmount;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof DocumentMeta))
            return false;

        DocumentMeta metadata = (DocumentMeta) o;
        return documentId == metadata.documentId;
    }

    @Override
    public int hashCode() {
        return documentId;
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "documentId=" + documentId +
                ", name='" + name + '\'' +
                ", shinglesAmount=" + shinglesAmount +
                ", marked=" + marked +
                '}';
    }
}
