package com.hospital.service;

import com.hospital.dao.DepartmentDAO;
import com.hospital.model.Department;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class DepartmentService {

    private final DepartmentDAO departmentDAO = new DepartmentDAO();

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9+][0-9\\s-]{6,19}$");

    public List<Department> getAllDepartments() throws SQLException {
        return departmentDAO.findAll();
    }

    public Department getDepartmentById(int id) throws SQLException {
        return departmentDAO.findById(id);
    }

    public int addDepartment(Department department) throws SQLException {
        validate(department);
        return departmentDAO.create(department);
    }

    public boolean updateDepartment(Department department) throws SQLException {
        validate(department);
        return departmentDAO.update(department);
    }

    public boolean deleteDepartment(int id) throws SQLException {
        return departmentDAO.delete(id);
    }

    private void validate(Department department) {
        if (isBlank(department.getName())) {
            throw new IllegalArgumentException("Department name is required.");
        }
        if (!isBlank(department.getPhoneNumber()) && !PHONE_PATTERN.matcher(department.getPhoneNumber()).matches()) {
            throw new IllegalArgumentException("Phone number format is invalid.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}