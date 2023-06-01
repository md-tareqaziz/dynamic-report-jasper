package com.tareq.dynamicreport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class Employee {
    private Integer id;
    private String name;
    private String designation;
    private String salary;
    private String doj;
}
