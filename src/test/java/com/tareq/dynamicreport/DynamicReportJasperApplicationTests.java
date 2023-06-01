package com.tareq.dynamicreport;

import com.tareq.dynamicreport.builder.JasperDesignBuilder;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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
		);
		JasperDesign jasperDesign=new JasperDesignBuilder("hello")
				.addTitle("Heloo")
				.addColumn("ID", "id", Integer.class.getName(), 10)
				.addColumn("Name", "name", String.class.getName(), 10)
				.addColumn("Designation", "designation", String.class.getName(), 10)
				.addColumn("Salary", "salary", String.class.getName(), 10)
//				.addParameters()
				.addPageFooter()
				.build();

		JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

		JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(employees);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("createdBy", "Java World");
		parameters.put("id", "ID");
		parameters.put("name", "NAME");
		parameters.put("designation", "DESIGNATION");
		parameters.put("salary", "Salary");

		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

		JasperExportManager.exportReportToPdfFile(jasperPrint, "employees.pdf");
	}

}
