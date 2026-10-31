package patmal.workshops.impl;

import java.util.ArrayList;
import java.util.List;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Workshop;

public class InstructorImpl extends PersonImpl implements Instructor {

    private final List<Workshop> workshops = new ArrayList<>();

    public InstructorImpl(String id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public List<Workshop> getWorkshops() {
        return new ArrayList<>(workshops);
    }

    void addWorkshop(Workshop workshop) {
        if (!workshops.contains(workshop)) {
            workshops.add(workshop);
        }
    }

    void removeWorkshop(Workshop workshop) {
        workshops.remove(workshop);
    }
}
