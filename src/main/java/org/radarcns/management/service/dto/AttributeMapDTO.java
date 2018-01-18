package org.radarcns.management.service.dto;

import java.util.Objects;

/**
 * Created by nivethika on 30-8-17.
 */
public class AttributeMapDTO {

    public AttributeMapDTO() {
        //default constructor
    }

    public AttributeMapDTO(String key, String value) {
        this.key = key;
        this.value = value;
    }

    private String key;

    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AttributeMapDTO attributeMapDto = (AttributeMapDTO) o;

        return Objects.equals(key, attributeMapDto.key)
                && Objects.equals(value, attributeMapDto.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "AttributeMapDTO{"
                + " key='" + key + "'"
                + ", value='" + value + "'"
                + '}';
    }
}
