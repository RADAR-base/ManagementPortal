package org.radarcns.management.service.dto;

/**
 * Created by nivethika on 30-8-17.
 */
public class AttributeMapDTO {

    public AttributeMapDTO () {
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
}
