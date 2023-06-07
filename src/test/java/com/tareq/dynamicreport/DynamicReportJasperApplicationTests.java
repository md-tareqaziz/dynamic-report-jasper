package com.tareq.dynamicreport;

import com.tareq.dynamicreport.builder.JasperDesignBuilder;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.UrlResource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class DynamicReportJasperApplicationTests {

	@Test
	void contextLoads() throws JRException {
		List<Employee> employees = Arrays.asList(
				new Employee(1, "Ismail Hossain", "Software Engg.", "200.00", "abc"),
				new Employee(2, "Shakil", "Software Engg.", "200.00", "abc"),
				new Employee(3, "TQ", "Software Engg.", "200.00", "abc"),
				new Employee(4, "Mahsin", "Software Engg.", "200.00", "abc"),
				new Employee(5, "Nadif", "Software Engg.", "200.00", "abc")

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

		JasperDesignBuilder jasperDesignBuilder=new JasperDesignBuilder()
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
				.addParameters(parameters)
				.addPageFooter()
				.build();

		jasperDesignBuilder.print("employees.pdf");


//		JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesignBuilder);
//
//		JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(employees);
//
//
//		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
//
//		JasperExportManager.exportReportToPdfFile(jasperPrint, "employees.pdf");

	}

}
