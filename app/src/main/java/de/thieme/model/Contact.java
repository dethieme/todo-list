package de.thieme.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;

@Entity(tableName = "contacts")
public class Contact implements Serializable {

    private static final long serialVersionUID = -8410064189777738560L;

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;

    // Default constructor required for Room
    public Contact() {
    }

    public Contact(String name) {
        this.name = name;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        return id == ((Contact) object).id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @NonNull
    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", name='" + name +
                '}';
    }
}