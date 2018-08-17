package realestateScraper.Constants;

public enum TimeZone {
    ET("ET", "Eastern Time", (byte)0),
    CT("CT", "Central Time", (byte)1),
    MT("MT", "Mountain Time", (byte)2),
    PT("PT", "Pacific Time", (byte)3);

    private final String abbreviation;
    private final String fullName;
    private final int value;

    TimeZone(String abbreviation, String fullName, byte value) {
        this.abbreviation = abbreviation;
        this.fullName = fullName;
        this.value = value;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getFullName() {
        return fullName;
    }

    public int getValue() {
        return value;
    }
}
