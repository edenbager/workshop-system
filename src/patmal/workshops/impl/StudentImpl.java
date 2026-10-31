package patmal.workshops.impl;

import java.util.ArrayList;
import java.util.List;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;

public class StudentImpl extends PersonImpl implements Student {

    // The team this student belongs to. null = a free student.
    private Team team;

    public StudentImpl(String id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public String getTeamName() {
        if (team == null) {
            return null;
        }
        return team.getName();
    }

    @Override
    public Instructor getInstructor() {
        if (team == null) {
            return null;
        }
        return team.getWorkshop().getInstructor();
    }

    @Override
    public List<Student> getTeamMates() {
        List<Student> mates = new ArrayList<>();
        if (team == null) {
            return mates;
        }
        for (Student s : team.getStudents()) {
            if (s != this) {
                mates.add(s);
            }
        }
        return mates;
    }

    boolean isFree() {
        return team == null;
    }

    void setTeam(Team team) {
        this.team = team;
    }
}
