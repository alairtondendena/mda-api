package net.unesc.tcc.mda.core.enums;

public enum YesNo {

    YES("YES"),
    NO("NO");

    private final String value;

    YesNo(String value) {
        this.value = value;
    }

    public static YesNo of(String value) {
        for (YesNo yesNo : values()) {
            if (yesNo.getValue().equals(value)) {
                return yesNo;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

}
