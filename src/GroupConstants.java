public interface GroupConstants {
    public static final String USER_NEEDS = "User needs";
    public static final String APPLICATION_NEEDS = "Application needs";
    public static final String CONTEXT_OF_DESIGN = "Context of design";
    public static final int USER_NEEDS_LAST = 84;
    public static final int APPLICATION_NEEDS_LAST = 96;

    public static String fromNumber(int number) {
        if (number == 132) { // image-browser
            return USER_NEEDS;
        } else if (number <= USER_NEEDS_LAST) {
            return USER_NEEDS;
        } else if (number <= APPLICATION_NEEDS_LAST) {
            return APPLICATION_NEEDS;
        }
        return CONTEXT_OF_DESIGN;
    }
}
