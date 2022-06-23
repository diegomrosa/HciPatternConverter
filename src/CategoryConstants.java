public interface CategoryConstants {
    public static final String NAVIGATING = "Navigating around";
    public static final String BASIC = "Basic interactions";
    public static final String SEARCHING = "Searching";
    public static final String DEALING = "Dealing with data";
    public static final String PERSONALIZING = "Personalizing";
    public static final String SHOPPING = "Shopping";
    public static final String CHOICES = "Making choices";
    public static final String INPUT = "Giving input";
    public static final String MISCELLANEOUS = "Miscellaneous";
    public static final String ATTENTION = "Drawing attention";
    public static final String FEEDBACK = "Feedback";
    public static final String SIMPLIFYING = "Simplifying interaction";
    public static final String SITE_TYPES = "Site types";
    public static final String EXPERIENCES = "Experiences";
    public static final String PAGE_TYPES = "Page types";

    public static final int NAVIGATING_LAST = 25;
    public static final int BASIC_LAST = 32;
    public static final int SEARCHING_LAST = 45;
    public static final int DEALING_LAST = 59;
    public static final int PERSONALIZING_LAST = 62;
    public static final int SHOPPING_LAST = 71;
    public static final int CHOICES_LAST = 76;
    public static final int INPUT_LAST = 79;
    public static final int MISCELLANEOUS_LAST = 84;
    public static final int ATTENTION_LAST = 92;
    public static final int FEEDBACK_LAST = 94;
    public static final int SIMPLIFYING_LAST = 96;
    public static final int SITE_TYPES_LAST = 110;
    public static final int EXPERIENCES_LAST = 118;

    public static String fromNumber(int number) {
        if (number == 132) { // image-browser
            return NAVIGATING;
        } else if (number <= NAVIGATING_LAST) {
            return NAVIGATING;
        } else if (number <= BASIC_LAST) {
            return BASIC;
        } else if (number <= SEARCHING_LAST) {
            return SEARCHING;
        } else if (number <= DEALING_LAST) {
            return DEALING;
        } else if (number <= PERSONALIZING_LAST) {
            return PERSONALIZING;
        } else if (number <= SHOPPING_LAST) {
            return SHOPPING;
        } else if (number <= CHOICES_LAST) {
            return CHOICES;
        } else if (number <= INPUT_LAST) {
            return INPUT;
        } else if (number <= ATTENTION_LAST) {
            return ATTENTION;
        } else if (number <= FEEDBACK_LAST) {
            return FEEDBACK;
        } else if (number <= SIMPLIFYING_LAST) {
            return SIMPLIFYING;
        } else if (number <= SITE_TYPES_LAST) {
            return SITE_TYPES;
        } else if (number <= EXPERIENCES_LAST) {
            return EXPERIENCES;
        }
        return PAGE_TYPES;
    }
}
