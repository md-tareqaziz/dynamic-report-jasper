package com.tareq.dynamicreport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

//@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class Employee {
    public Integer id;
     public String name;
     public String details;
     public String group_designation;
     public String group_salary;

    public Employee(Integer id, String name, String designation, String salary) {
        this.id = id;
        this.name = name;
        this.group_designation = designation;
        this.group_salary = salary;
    }

    public Employee(Integer id, String name, String group_designation, String group_salary, String details) {
        this.id = id;
        this.name = name;
        this.details = details;
        this.group_designation = group_designation;
        this.group_salary = group_salary;
    }
}
