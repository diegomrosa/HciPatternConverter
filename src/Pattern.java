public class Pattern implements CollectionConstants {
    private int number;
    private String id;
    private String name;
    private String group;
    private String category;
    private String collection;

    public Pattern(int number, String id, String name, String group, String category) {
        this(number, id, name, group, category, DEFAULT_COLLECTION);
    }

    public Pattern(int number, String id, String name, String group, String category, String collection) {
        this.number = number;
        this.id = id;
        this.name = name;
        this.group = group;
        this.category = category;
        this.collection = collection;
    }

    public int getNumber() {
        return number;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public String getCategory() {
        return category;
    }

    public String getCollection() {
        return collection;
    }
}
