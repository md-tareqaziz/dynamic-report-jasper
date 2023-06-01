package com.tareq.dynamicreport.builder;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.ModeEnum;

import java.awt.*;

public class JasperDesignBuilder {
    protected final JasperDesign design = new JasperDesign();
    protected final JRDesignBand header = new JRDesignBand();
    protected final JRDesignBand element = new JRDesignBand();
    protected final JRDesignBand footer = new JRDesignBand();
    protected final JRDesignBand noData = new JRDesignBand();

    public JasperDesignBuilder(String name) {
        design.setName(name);
    }

    public JasperDesignBuilder addColumn(String title, String property, String className, int width) throws JRException {
        JRDesignField field = new JRDesignField();
        field.setName(property);
        field.setValueClassName(className);
        design.addField(field);


        //Header
        header.setHeight(30);

        JRDesignTextField textField = new JRDesignTextField();
        textField.setBlankWhenNull(true);
        textField.setX(header.getElements().length*100);
        textField.setY(0);
        textField.setWidth(100);
        textField.setHeight(15);
        textField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        textField.setExpression(new JRDesignExpression(String.format("$P{%s}", property)));
        textField.setForecolor(new Color(67, 108, 168));
        textField.setBackcolor(new Color(.95f, .95f, .95f, 0.5f));
        textField.setMode(ModeEnum.OPAQUE);

        header.addElement(textField);

        //data

        element.setHeight(30);

        JRDesignTextField tf1 = new JRDesignTextField();
        tf1.setBlankWhenNull(true);
        tf1.setX(element.getElements().length*100);
        tf1.setY(0);
        tf1.setWidth(100);
        tf1.setHeight(30);
        tf1.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        tf1.setExpression(new JRDesignExpression(String.format("$F{%s}", property)));
        tf1.setStretchWithOverflow(true);
        tf1.setMode(ModeEnum.OPAQUE);
        element.addElement(tf1);

//        parameter
        JRDesignParameter parameter = new JRDesignParameter();
        parameter.setName(property);
        parameter.setValueClassName(className);
        design.addParameter(parameter);

        return this;
    }

    public JasperDesignBuilder addTitle(String title) {
        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(40);

        JRDesignStaticText titleText = new JRDesignStaticText();
        titleText.setText(title);
        titleText.setX(0);
        titleText.setY(10);
        titleText.setWidth(515);
        titleText.setHeight(30);
        titleText.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        titleText.setFontSize(22f);
        titleBand.addElement(titleText);
        design.setTitle(titleBand);

        return this;
    }

    public JasperDesignBuilder addPageFooter() {
        footer.setHeight(30);

        JRDesignTextField tfc5 = new JRDesignTextField();
        tfc5.setBlankWhenNull(true);
        tfc5.setX(0);
        tfc5.setY(0);
        tfc5.setWidth(100);
        tfc5.setHeight(30);
        tfc5.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
//        tfc5.setStyle(mystyle);
        tfc5.setExpression(new JRDesignExpression("new java.util.Date()"));

        footer.addElement(tfc5);


        JRDesignTextField tfc6 = new JRDesignTextField();
        tfc6.setBlankWhenNull(true);
        tfc6.setX(0);
        tfc6.setY(0);
        tfc6.setWidth(500);
        tfc6.setHeight(30);
        tfc6.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
//        tfc6.setStyle(mystyle);
        tfc6.setExpression(new JRDesignExpression("\"Page \"+$V{PAGE_NUMBER}+\" of\"+\" \" + $V{PAGE_NUMBER}"));

        footer.addElement(tfc6);

        return this;
    }

    public JasperDesignBuilder addParameters() throws JRException {
        JRDesignParameter par = new JRDesignParameter();
        par.setName("createdBy");
        par.setValueClass(String.class);
        design.addParameter(par);

        JRDesignParameter par2 = new JRDesignParameter();
        par2.setName("id");
        par2.setValueClass(String.class);
        design.addParameter(par2);

        JRDesignParameter par3 = new JRDesignParameter();
        par3.setName("name");
        par3.setValueClass(String.class);
        design.addParameter(par3);

        JRDesignParameter par4 = new JRDesignParameter();
        par4.setName("designation");
        par4.setValueClass(String.class);
        design.addParameter(par4);

        JRDesignParameter par5 = new JRDesignParameter();
        par5.setName("salary");
        par5.setValueClass(String.class);
        design.addParameter(par5);

        return this;
    }

    public JasperDesignBuilder addNoData() {
        noData.setHeight(30);

        JRDesignStaticText tfc5 = new JRDesignStaticText();

        tfc5.setX(0);
        tfc5.setY(0);
        tfc5.setWidth(500);
        tfc5.setHeight(30);
        tfc5.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
//        tfc5.setStyle(mystyle);
        tfc5.setText("No Data Found");

        noData.addElement(tfc5);

        return this;
    }

    public JasperDesign build() {
        design.setColumnHeader(header);
        ((JRDesignSection) design.getDetailSection()).addBand(element);

        design.setPageFooter(footer);
        design.setNoData(noData);

        return design;
    }
}
