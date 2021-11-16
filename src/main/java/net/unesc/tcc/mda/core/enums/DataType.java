package net.unesc.tcc.mda.core.enums;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public enum DataType {

    INT_2("int2", Integer.class),
    INT_4("int4", Integer.class),
    INT_8("int8", Long.class),
    BIG_SERIAL("bigserial", Long.class),
    ENUM("enum", Enum.class),
    BOOLEAN("bool", Boolean.class),
    NUMERIC("numeric", Long.class),
    DECIMAL("numeric", BigDecimal.class),
    CHAR("bpchar", String.class),
    VARCHAR("varchar", String.class),
    TEXT("text", String.class),
    TIME("time", LocalTime.class),
    TIMESTAMP("timestamp", LocalDate.class),
    DATE("date", LocalDate.class);

    private final String value;
    private final Class type;

    DataType(String value, Class type) {
        this.value = value;
        this.type = type;
    }

    public static DataType of(String value) {
        for (DataType dataType : values()) {
            if (dataType.getValue().equals(value)) {
                return dataType;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public Class getType() {
        return type;
    }

}
