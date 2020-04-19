package ru.ifmo.rain.zhukov.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentGroupQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class StudentDB implements StudentGroupQuery {
    private Comparator<Student> comparatorByName =
            Comparator.comparing(Student::getLastName)
                    .thenComparing(Student::getFirstName)
                    .thenComparing(Student::getId);


    public List<String> getFromStudents(List<Student> students, Function<Student, String> extractor) {
        return students.stream()
                .map(extractor)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getFromStudents(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getFromStudents(students, Student::getLastName);

    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return getFromStudents(students, Student::getGroup);

    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getFromStudents(students, student -> student.getFirstName() + " " + student.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream().map(Student::getFirstName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream()
                .min(Student::compareTo)
                .map(Student::getFirstName)
                .orElse("");
    }

    private List<Student> getStudentsSorted(Collection<Student> students, Comparator<Student> comparator) {
        return students.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return getStudentsSorted(students, Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return getStudentsSorted(students, comparatorByName);
    }

    private List<Student> findStudentBy(Collection<Student> students, Predicate<Student> pred) {
        return students.stream().filter(pred)
                .sorted(comparatorByName)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentBy(students, student -> student.getFirstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentBy(students, student -> student.getLastName().equals(name));

    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return findStudentBy(students, student -> student.getGroup().equals(group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return findStudentsByGroup(students, group).stream()
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(String::compareTo)));
    }


    private List<Group> collectGroups(Collection<Student> students, UnaryOperator<List<Student>> studentsOperator) {
        return students.stream()
                .collect(Collectors.groupingBy(Student::getGroup))
                .entrySet().stream()
                .map(stringListEntry -> new Group(
                        stringListEntry.getKey(),
                        studentsOperator.apply(stringListEntry.getValue())))
                .collect(Collectors.toList());
    }

    private List<Group> getGroupsBy(Collection<Student> students, Comparator<Student> studentComparator) {
        return collectGroups(
                students,
                studentsList ->
                        studentsList.stream()
                                .sorted(studentComparator)
                                .collect(Collectors.toList()))
                .stream()
                .sorted(Comparator.comparing(Group::getName))
                .collect(Collectors.toList());
    }


    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroupsBy(students, comparatorByName);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroupsBy(students, Student::compareTo);
    }

    private String getLargestGroupBy(Collection<Student> students, Function<Collection<Student>, Integer> weightFunction) {
        return collectGroups(students, UnaryOperator.identity())
                .stream()
                .collect(Collectors.toMap(
                        Group::getName,
                        group -> weightFunction.apply(group.getStudents())
                ))
                .entrySet()
                .stream()
                .max(Map.Entry.<String, Integer>comparingByValue()
                        .thenComparing(Map.Entry.comparingByKey(
                                Collections.reverseOrder(String::compareTo)))
                )
                .map(Map.Entry::getKey)
                .orElse("");
    }

    @Override
    public String getLargestGroup(Collection<Student> students) {
        return getLargestGroupBy(students, Collection::size);
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return getLargestGroupBy(students, list -> (int) list.stream().map(Student::getFirstName).distinct().count());
    }

}
