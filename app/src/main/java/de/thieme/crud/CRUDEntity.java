package de.thieme.crud;

import java.io.Serializable;

public interface CRUDEntity extends Serializable {

    long getId();

    void setId(long id);
}