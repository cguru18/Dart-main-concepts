package com.example.sms.model;

public enum Subject {
    MATHEMATICS("Mathematics", Category.COMPULSORY),
    ENGLISH("English", Category.COMPULSORY),
    KISWAHILI("Kiswahili", Category.COMPULSORY),

    PHYSICS("Physics", Category.STEM),
    CHEMISTRY("Chemistry", Category.STEM),
    BIOLOGY("Biology", Category.STEM),
    COMPUTER_STUDIES("Computer Studies", Category.STEM),

    HISTORY_GOVERNMENT("History and Government", Category.HUMANITIES),
    GEOGRAPHY("Geography", Category.HUMANITIES),
    BUSINESS_STUDIES("Business Studies", Category.HUMANITIES),

    DRAWING_DESIGN("Drawing and Design", Category.TECHNICAL),
    ART_DESIGN("Art and Design", Category.TECHNICAL),
    MUSIC("Music", Category.TECHNICAL);

    public enum Category { COMPULSORY, STEM, HUMANITIES, TECHNICAL }

    private final String displayName;
    private final Category category;

    Subject(String displayName, Category category) {
        this.displayName = displayName;
        this.category = category;
    }

    public String getDisplayName() { return displayName; }
    public Category getCategory() { return category; }
}
