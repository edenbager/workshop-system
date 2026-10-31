package patmal.workshops.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;

/**
 * In-memory implementation of the workshop registration system.
 *
 * Unknown keys (section 3.3): getX(id) returns null, and
 * getStudentsOfX(id) returns an empty list. No exception is thrown.
 */
public class WorkshopSystemImpl implements WorkshopSystem {

    // Data, kept in maps keyed by id. LinkedHashMap keeps creation order.
    private final Map<String, InstructorImpl> instructors = new LinkedHashMap<>();
    private final Map<String, StudentImpl> students = new LinkedHashMap<>();
    private final Map<Integer, WorkshopImpl> workshops = new LinkedHashMap<>();
    private final Map<Integer, TeamImpl> teams = new LinkedHashMap<>();

    // Running id counters for workshops and teams (each starts at 1).
    private int nextWorkshopId = 1;
    private int nextTeamId = 1;

    public WorkshopSystemImpl() {
    }

    // ---------- Creating things ----------

    @Override
    public Instructor createInstructor(String id, String name, String email) {
        if (id == null || instructors.containsKey(id)) {
            throw new IllegalArgumentException("Instructor id already exists or is null: " + id);
        }
        InstructorImpl instructor = new InstructorImpl(id, name, email);
        instructors.put(id, instructor);
        return instructor;
    }

    @Override
    public Student createStudent(String id, String name, String email) {
        if (id == null || students.containsKey(id)) {
            throw new IllegalArgumentException("Student id already exists or is null: " + id);
        }
        StudentImpl student = new StudentImpl(id, name, email);
        students.put(id, student);
        return student;
    }

    @Override
    public Workshop createWorkshop(String name, String description) {
        WorkshopImpl workshop = new WorkshopImpl(nextWorkshopId, name, description);
        workshops.put(nextWorkshopId, workshop);
        nextWorkshopId++;
        return workshop;
    }

    @Override
    public void associateWorkshopWithInstructor(Instructor instructor, Workshop workshop) {
        if (instructor == null || workshop == null) {
            throw new IllegalArgumentException("Instructor and workshop must not be null");
        }
        InstructorImpl newInstructor = instructors.get(instructor.getId());
        WorkshopImpl theWorkshop = workshops.get(workshop.getId());
        if (newInstructor == null || theWorkshop == null) {
            throw new IllegalArgumentException("Unknown instructor or workshop");
        }

        // If the workshop had a different instructor before, remove it from that instructor.
        Instructor oldInstructor = theWorkshop.getInstructor();
        if (oldInstructor != null && oldInstructor != newInstructor) {
            ((InstructorImpl) oldInstructor).removeWorkshop(theWorkshop);
        }

        // Update both sides of the relation.
        theWorkshop.setInstructor(newInstructor);
        newInstructor.addWorkshop(theWorkshop);
    }

    @Override
    public Team createTeam(String name, Workshop workshop, List<String> members) {
        // 1. Validate everything first, so a rejected call changes nothing.
        if (workshop == null || workshops.get(workshop.getId()) == null) {
            throw new IllegalArgumentException("Unknown workshop");
        }
        if (members == null) {
            throw new IllegalArgumentException("Members list must not be null");
        }
        List<StudentImpl> teamStudents = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();
        for (String studentId : members) {
            if (!seenIds.add(studentId)) {
                throw new IllegalArgumentException("Student listed twice: " + studentId);
            }
            StudentImpl student = students.get(studentId);
            if (student == null) {
                throw new IllegalArgumentException("Unknown student: " + studentId);
            }
            if (!student.isFree()) {
                throw new IllegalArgumentException("Student already in a team: " + studentId);
            }
            teamStudents.add(student);
        }

        // 2. Everything is valid: now change the system.
        WorkshopImpl theWorkshop = workshops.get(workshop.getId());
        TeamImpl team = new TeamImpl(nextTeamId, name, theWorkshop, new ArrayList<>(teamStudents));
        teams.put(nextTeamId, team);
        nextTeamId++;
        for (StudentImpl student : teamStudents) {
            student.setTeam(team);
        }
        return team;
    }

    // ---------- Querying ----------

    @Override
    public List<Instructor> getInstructors() {
        return new ArrayList<>(instructors.values());
    }

    @Override
    public Instructor getInstructor(String id) {
        return instructors.get(id);
    }

    @Override
    public List<Student> getStudents() {
        return new ArrayList<>(students.values());
    }

    @Override
    public Student getStudent(String id) {
        return students.get(id);
    }

    @Override
    public List<Workshop> getWorkshops() {
        return new ArrayList<>(workshops.values());
    }

    @Override
    public Workshop getWorkshop(Integer id) {
        return workshops.get(id);
    }

    @Override
    public List<Team> getTeams() {
        return new ArrayList<>(teams.values());
    }

    @Override
    public Team getTeam(Integer id) {
        return teams.get(id);
    }

    @Override
    public List<Student> getFreeStudents() {
        List<Student> free = new ArrayList<>();
        for (StudentImpl student : students.values()) {
            if (student.isFree()) {
                free.add(student);
            }
        }
        return free;
    }

    @Override
    public List<Student> getStudentsOfInstructor(String instructorId) {
        List<Student> result = new ArrayList<>();
        InstructorImpl instructor = instructors.get(instructorId);
        if (instructor == null) {
            return result;
        }
        for (Workshop workshop : instructor.getWorkshops()) {
            for (Student student : getStudentsOfWorkshop(workshop.getId())) {
                if (!result.contains(student)) {
                    result.add(student);
                }
            }
        }
        return result;
    }

    @Override
    public List<Student> getStudentsOfWorkshop(Integer workshopId) {
        List<Student> result = new ArrayList<>();
        for (TeamImpl team : teams.values()) {
            if (team.getWorkshop().getId().equals(workshopId)) {
                for (Student student : team.getStudents()) {
                    if (!result.contains(student)) {
                        result.add(student);
                    }
                }
            }
        }
        return result;
    }
}
