package ua.cv.ovchynnikov.pojo;

/**
 * @author superuser
 *         Created 10-Aug-15
 */
public class DocumentMeta implements Meta {
    private final String path;
    private final int shinglesAmount;

    public DocumentMeta(String path, int shinglesAmount) {
        this.path = path;
        this.shinglesAmount = shinglesAmount;
    }

    public String getPath() {
        return path;
    }

    public int getShinglesAmount() {
        return shinglesAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DocumentMeta)) {
            return false;
        }

        DocumentMeta that = (DocumentMeta) o;

        if (!path.equals(that.path)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
