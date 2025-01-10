package de.thieme.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(tableName = "to_dos")
public class ToDo implements Serializable {

    private static final long serialVersionUID = -6410064189686738560L;

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String description;
    private long expiry;
    @SerializedName("done")
    private boolean isDone;
    @SerializedName("favourite")
    private boolean isFavourite;
    // private List<String> contacts = new ArrayList<>();

    // Default constructor required for Room
    public ToDo() {}

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

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Get expiry as Date
    public long getExpiry() {
        return expiry;
    }

    // Set expiry from Date
    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setIsDone(boolean isDone) {
        this.isDone = isDone;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setIsFavourite(boolean isFavourite) {
        this.isFavourite = isFavourite;
    }

    public List<String> getContacts() {
        return new ArrayList<>();
    }

    public void setContacts(List<String> contacts) {
        // this.contacts = contacts;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        return id == ((ToDo) object).id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @NonNull
    @Override
    public String toString() {
        return "ToDo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", expiry=" + expiry +
                ", isDone=" + isDone +
                ", isFavourite=" + isFavourite +
                '}';
    }
}