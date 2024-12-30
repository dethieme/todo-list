package de.thieme.model;

import java.util.ArrayList;
import java.util.List;

public class ToDoCRUDOperations implements ICRUDEntityOperations {

    private static long ID_COUNT = 0;

    private final List<ToDo> todos = new ArrayList<>();

    public ToDoCRUDOperations() {
        create(new ToDo("Buy groceries", "Get milk, eggs, and bread"));
        create(new ToDo("Call Mom", "Check in with her"));
        create(new ToDo("Finish Android project", "Complete RecyclerView to-do list"));
    }

    @Override
    public ToDo create(ToDo todo) {
        todo.setId(++ID_COUNT);
        todos.add(todo);
        return todo;
    }

    @Override
    public List<ToDo> readAll() {
        try {
            Thread.sleep(5000);
        } catch (Exception e) {

        }

        return new ArrayList<>(todos);
    }

    @Override
    public ToDo read(long id) {
        return null;
    }

    @Override
    public boolean update(ToDo todo) {
        return true;
    }

    @Override
    public boolean delete(long id) {
        return false;
    }
}
