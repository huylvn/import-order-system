package com.importorder.model;

import java.util.Objects;

/**
 * Master data for a merchandise item.
 */
public class Merchandise {

    private Long id;
    private String code;
    private String name;
    private String unit;
    private String description;

    public Merchandise() {
    }

    public Merchandise(Long id, String code, String name, String unit, String description) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.unit = unit;
        this.description = description;
    }

    public Merchandise(String code, String name, String unit, String description) {
        this(null, code, name, unit, description);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        if (code == null && name == null) {
            return "";
        }
        if (name == null) {
            return code;
        }
        if (code == null) {
            return name;
        }
        return code + " - " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Merchandise that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
