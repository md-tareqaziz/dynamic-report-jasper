package com.tareq.dynamicreport.builder;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.type.OrientationEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.List;

import static com.tareq.dynamicreport.builder.Defs.*;

public class JasperDesignBuilder<T> {
    private Integer GROUP_HEADER_COUNT = 0;
    private Integer COLUMN_COUNT = 0;
    protected final JasperDesign design = new JasperDesign();
    protected final JRDesignBand header = new JRDesignBand();
    protected final JRDesignBand field = new JRDesignBand();
    protected final JRDesignBand footer = new JRDesignBand();
    protected final JRDesignBand noData = new JRDesignBand();

    protected final JRDesignBand reportNameBand = new JRDesignBand();

    private java.util.List<T> dataList = new ArrayList<T>();
    private Map<String, Object> parameters = new HashMap<>();
    private byte[] report = null;

    private List<Integer> GROUP_HEADER_POSITIONS = new ArrayList<>();
    private String type = "pdf";
    private boolean havingSubHeader = false;

    private JasperPrint jasperPrint = null;
    private List<JRDesignTextField> groupHeaders = new ArrayList<>();
    private List<JRDesignTextField> headers = new ArrayList<>();
    private List<JRDesignTextField> fields = new ArrayList<>();

    public JasperDesignBuilder() {
    }

    public JasperDesignBuilder addColumn(String title, String property, Class className, int width) throws JRException {
        JRDesignField field = new JRDesignField();
        field.setName(property);
        field.setValueClass(className);
        design.addField(field);
        design.setOrientation(OrientationEnum.PORTRAIT);

        //dynamically creating header
        headers.add(createHeader(property));

        //dynamically creating field
        this.field.setHeight(HEIGHT_COLUMN_DEFAULT);
        fields.add(createField(property));

        //setting header values as parameter
        JRDesignParameter parameter = new JRDesignParameter();
        parameter.setName(property);
        parameter.setValueClass(String.class);
        design.addParameter(parameter);
        parameters.put(property, title);

        COLUMN_COUNT++;
        return this;
    }

    private JRDesignTextField createField(String property) {
        JRDesignTextField field = new JRDesignTextField();
        setBorderLineWidthOne(field);
        field.setBlankWhenNull(true);
        field.setY(0);
        field.setHeight(HEIGHT_COLUMN_DEFAULT);
        field.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        field.setExpression(new JRDesignExpression(String.format("$F{%s}", property)));
        field.setStretchWithOverflow(true);
        field.setMode(ModeEnum.OPAQUE);
        return field;
    }

    private JRDesignTextField createHeader(String property) {
        JRDesignTextField header = new JRDesignTextField();
        setBorderLineWidthOne(header);
        header.setBlankWhenNull(true);
        if (havingSubHeader && GROUP_HEADER_COUNT > 0) {
            header.setY(HEIGHT_HEADER_DEFAULT);
            header.setHeight(HEIGHT_HEADER_DEFAULT);
            GROUP_HEADER_COUNT--;
        } else if(havingSubHeader){
            header.setHeight(HEIGHT_HEADER_LARGE);
        }else{
            header.setHeight(HEIGHT_HEADER_DEFAULT);
            header.setY(0);
        }
        header.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        header.setExpression(new JRDesignExpression(String.format("$P{%s}", property)));
        header.setForecolor(FOREGROUND_HEADER_COLOR);
        header.setBackcolor(BACKGROUND_HEADER_COLOR);
        header.setMode(ModeEnum.OPAQUE);
        header.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        header.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        return header;
    }


    public JasperDesignBuilder addGroup(Integer numberOfHeader, String subHeaderTitle) throws JRException {
        havingSubHeader = true;
        if (GROUP_HEADER_COUNT > 0) {
            throw new RuntimeException("Columns of previous group still pending.");
        } else {
            GROUP_HEADER_COUNT = numberOfHeader;
        }
        for (JRDesignTextField e : headers) {
            e.setHeight(HEIGHT_HEADER_LARGE);
            e.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
            e.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        }

        GROUP_HEADER_POSITIONS.add(COLUMN_COUNT);

        JRDesignTextField groupHeader = new JRDesignTextField();
        setBorderLineWidthOne(groupHeader);
        groupHeader.setBlankWhenNull(true);
        groupHeader.setY(0);
        groupHeader.setWidth(numberOfHeader);
        groupHeader.setHeight(HEIGHT_HEADER_DEFAULT);
        groupHeader.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        groupHeader.setExpression(new JRDesignExpression(String.format("$P{%s}", subHeaderTitle)));
        groupHeader.setForecolor(FOREGROUND_HEADER_COLOR);
        groupHeader.setBackcolor(BACKGROUND_HEADER_COLOR);
        groupHeader.setMode(ModeEnum.OPAQUE);
        groupHeaders.add(groupHeader);

        //parameter
        JRDesignParameter parameter = new JRDesignParameter();
        parameter.setName(subHeaderTitle);
        parameter.setValueClass(String.class);
        design.addParameter(parameter);
        parameters.put(subHeaderTitle, subHeaderTitle);

        return this;
    }


    public JasperDesignBuilder addType(String type) {
        this.type = type;
        return this;
    }

    public JasperDesignBuilder addTitle(String title) {
        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(HEIGHT_TITLE_DEFAULT);

        JRDesignStaticText titleText = new JRDesignStaticText();
        titleText.setText(title);
        titleText.setX(0);
        titleText.setY(0);
        titleText.setHeight(HEIGHT_TITLE_DEFAULT);
        titleText.setWidth(WIDTH_MAX);
        titleText.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        titleText.setFontSize(FONT_SIZE_TITLE_DEFAULT);
        titleBand.addElement(titleText);
        design.setTitle(titleBand);

        return this;
    }
    public JasperDesignBuilder addReportName(String reportName) {

        reportNameBand.setHeight(HEIGHT_REPORT_DEFAULT+MARGIN_DEFAULT);

        JRDesignStaticText titleText = new JRDesignStaticText();
        titleText.setText(reportName);
        titleText.setX(0);
        titleText.setY(0);
        titleText.setHeight(HEIGHT_REPORT_DEFAULT);
        titleText.setWidth(WIDTH_MAX);
        titleText.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        titleText.setFontSize(FONT_SIZE_REPORTNAME_DEFAULT);

        reportNameBand.addElement(titleText);

        return this;
    }

    public JasperDesignBuilder addCriteriaDetails(String criteriaDetails) {

        reportNameBand.setHeight(reportNameBand.getHeight()+HEIGHT_CRITERIA_DEFAULT+MARGIN_DEFAULT);

        JRDesignStaticText criteria = new JRDesignStaticText();
        criteria.setText(criteriaDetails);
        criteria.setX(0);
        criteria.setY((reportNameBand.getElements().length>0)?HEIGHT_CRITERIA_DEFAULT+MARGIN_DEFAULT:0);
        criteria.setHeight(HEIGHT_CRITERIA_DEFAULT);
        criteria.setWidth(WIDTH_MAX);
        criteria.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
        criteria.setFontSize(FONT_SIZE_CRITERIA_DEFAULT);
        reportNameBand.addElement(criteria);

        JRDesignTextField date = new JRDesignTextField();
        date.setBlankWhenNull(true);
        date.setX(WIDTH_MAX-WIDTH_DATE_DEFAULT);
        date.setY((reportNameBand.getElements().length>0)?HEIGHT_CRITERIA_DEFAULT+MARGIN_DEFAULT:0);
        date.setWidth(WIDTH_DATE_DEFAULT);
        date.setHeight(HEIGHT_CRITERIA_DEFAULT);
        date.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
//        tfc5.setStyle(mystyle);
        date.setExpression(new JRDesignExpression("new java.util.Date()"));

        reportNameBand.addElement(date);


        return this;
    }

    private void setBorderLineWidthOne(JRDesignTextField textField) {
        JRLineBox box= textField.getLineBox();
        box.getLeftPen().setLineWidth(1);
        box.getRightPen().setLineWidth(1);
        box.getTopPen().setLineWidth(1);
        box.getBottomPen().setLineWidth(1);
    }

    public JasperDesignBuilder addPageFooter() {
        footer.setHeight(30);

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

    public JasperDesignBuilder addParameters(Map<String, Object> parameters) throws JRException {
//        this.parameters.putAll(parameters);
        JRDesignParameter par = new JRDesignParameter();
        par.setName("createdBy");
        par.setValueClass(String.class);
        design.addParameter(par);
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

    public JasperDesignBuilder addData(List<T> list) {
        this.dataList = list;
        return this;
    }

    public JasperDesignBuilder build() throws JRException {
        int avgWidth= WIDTH_MAX /fields.size();
        for (int i=0;i<groupHeaders.size();i++) {
            this.groupHeaders.get(i).setX(GROUP_HEADER_POSITIONS.get(i)*avgWidth);
            this.groupHeaders.get(i).setWidth(this.groupHeaders.get(i).getWidth()*avgWidth);
            header.addElement(this.groupHeaders.get(i));
        }
        for (int i=0;i<headers.size();i++) {
            this.headers.get(i).setWidth(avgWidth);
            this.headers.get(i).setX(avgWidth*i);
            this.header.addElement(headers.get(i));
        }
        for (int i=0;i<fields.size();i++) {
            this.fields.get(i).setWidth(avgWidth);
            this.fields.get(i).setX(avgWidth*i);
            this.field.addElement(fields.get(i));
        }

        if (havingSubHeader) {
            header.setHeight(HEIGHT_HEADER_LARGE);
        } else {
            header.setHeight(HEIGHT_HEADER_DEFAULT);
        }

        design.setName("dynamic_report");

        design.setColumnHeader(header);
        ((JRDesignSection) design.getDetailSection()).addBand(field);

        design.setPageHeader(reportNameBand);
        design.setPageFooter(footer);
        design.setNoData(noData);

        JasperReport jasperReport = JasperCompileManager.compileReport(design);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);
        this.jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        if (type.equalsIgnoreCase("xlsx")) {
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(this.jasperPrint));
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(byteStream));
            SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
            configuration.setDetectCellType(true);//Set configuration as you like it!!
            configuration.setCollapseRowSpan(false);
            exporter.setConfiguration(configuration);
            exporter.exportReport();
            report = byteStream.toByteArray();
        } else {
            report = JasperExportManager.exportReportToPdf(jasperPrint);
        }

        return this;
    }

    public byte[] getReport() {
        return report;
    }

    public void print(String path) throws JRException {
        JasperExportManager.exportReportToPdfFile(this.jasperPrint, path);
    }

    public JasperDesign getDesign() {
        return design;
    }
}
