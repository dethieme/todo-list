package de.thieme.model;

import java.util.List;

public interface ICRUDEntityOperations {
    public ToDo create(ToDo todo);
    public List<ToDo> readAll();
    public ToDo read(long id);
    public boolean update(ToDo todo);
    public boolean delete(long id);
}