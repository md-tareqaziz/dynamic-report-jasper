package com.tareq.dynamicreport;

import com.tareq.dynamicreport.builder.DynamicReportBuilder;
import net.sf.jasperreports.engine.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
class DynamicReportJasperApplicationTests {

//	@Test
	void testNormalDynamicReport() throws JRException {
		List<Employee> employees = Arrays.asList(
//				new Employee(1, "Ismail Hossain", "Software Engg.", "200.00"),
//				new Employee(2, "Shakil", "Software Engg.", "200.00" ),
//				new Employee(3, "TQ", "Software Engg.", "200.00"),
//				new Employee(4, "Mahsin", "Software Engg.", "200.00"),
//				new Employee(5, "Nadif", "Software Engg.", "200.00")

//				new Employee("1", "Ismail Hossain", "Software Engg.", "200.00", "abc"),
//				new Employee("2", "Shakil", "Software Engg.", "200.00", "abc"),
//				new Employee("3", "TQ", "Software Engg.", "200.00", "abc"),
//				new Employee("4", "Mahsin", "Software Engg.", "200.00", "abc"),
//				new Employee("5", "Nadif", "Software Engg.", "200.00", "abc")
		);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("createdBy", "Java World");
//		parameters.put("id", "ID");
//		parameters.put("name", "NAME");
//		parameters.put("designation", "DESIGNATION");
//		parameters.put("salary", "Salary");

		DynamicReportBuilder dynamicReportBuilder =new DynamicReportBuilder()
				.addTitle("Sample dynamic report")
				.addReportName("Employee details report")
				.addCriteriaDetails("Employee details criteria")
				.addColumn("ID", "id", Integer.class, 10)
				.addGroup(2,"Profile")
				.addColumn("Name", "name", String.class, 10)
				.addColumn("Designation", "designation", String.class, 10)
				.addColumn("Salary", "salary", String.class, 10)
				.addColumn("DOB", "doj", String.class, 10)
				.addData(employees)
//				.addParameters(parameters)
				.addPageFooter()
				.build();

		dynamicReportBuilder.print("employees.pdf");


//		JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesignBuilder);
//
//		JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(employees);
//
//
//		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
//
//		JasperExportManager.exportReportToPdfFile(jasperPrint, "employees.pdf");

	}

	@Test
	void testGenericDynamicReport() throws NoSuchFieldException, IllegalAccessException, JRException {

		List<Employee> employees = Arrays.asList(
//				new Employee(1, "Ismail Hossain", "Software Engg.", "200.00", sl),
//				new Employee(2, "Shakil", "Software Engg.", "200.00", sl),
//				new Employee(3, "TQ", "Software Engg.", "200.00", sl),
//				new Employee(4, "Mahsin", "Software Engg.", "200.00", sl),
//				new Employee(5, "Nadif", "Software Engg.", "200.00", sl)

				new Employee(1, "Ismail Hossain", "Software Engg.", "200.00","some information"),
				new Employee(2, "Shakil", "Software Engg.", "200.00","some information"),
				new Employee(3, "TQ", "Software Engg.", "200.00","some information"),
				new Employee(4, "Mahsin", "Software Engg.", "200.00","some information"),
				new Employee(5, "Nadif", "Software Engg.", "200.00","some information")
		);

		List<String> whitelist=new ArrayList<>();
		whitelist.add("id");
		whitelist.add("name");
		whitelist.add("group_designation");
		whitelist.add("group_salary");
		whitelist.add("details");

		DynamicReportBuilder dynamicReportBuilder =new DynamicReportBuilder()
				.addTitle("Sample dynamic report")
				.addReportName("Employee details report")
				.addCriteriaDetails("Employee details criteria")
//				.createReportFromObject(employees)
				.createReportFromObject(employees,whitelist)
				.addPageFooter()
				.build();

		dynamicReportBuilder.print("employees.pdf");

	}

}
