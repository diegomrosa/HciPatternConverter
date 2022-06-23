import java.util.Objects;

public class PatternLink implements CollectionConstants {
    private Integer number;
    private LinkType type;
    private String patternId;
    private String collection;
    private Pattern source;
    private Pattern target;

    public PatternLink(Integer number, LinkType type, String patternId) {
        this(number, type, patternId, DEFAULT_COLLECTION);
    }

    public PatternLink(Integer number, LinkType type, String patternId, String collection) {
        this.number = number;
        this.type = type;
        this.patternId = patternId;
        if (collection != null) {
            this.collection = collection;
        } else {
            this.collection = DEFAULT_COLLECTION;
        }
        this.source = null;
        this.source = null;
    }

    public Integer getNumber() {
        return number;
    }

    public LinkType getType() {
        return type;
    }

    public String getPatternId() {
        return patternId;
    }

    public String getCollection() {
        return collection;
    }

    public Pattern getSource() {
        return source;
    }

    public void setSource(Pattern source) {
        this.source = source;
    }

    public Pattern getTarget() {
        return target;
    }

    public void setTarget(Pattern target) {
        this.target = target;
    }

    public void setType(LinkType type) {
        this.type = type;
    }

    public void setPatternId(String patternId) {
        this.patternId = patternId;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatternLink that = (PatternLink) o;
        return number.equals(that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }
}
