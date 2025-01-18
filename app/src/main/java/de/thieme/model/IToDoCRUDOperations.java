package de.thieme.model;

import java.util.List;

public interface IToDoCRUDOperations {
    ToDo create(ToDo todo);
    List<ToDo> readAll();
    boolean update(ToDo todo);
    boolean delete(long id);
    void synchronize();
    void deleteAllLocalTodos();
    void deleteAllRemoteTodos();
}