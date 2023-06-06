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

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.List;

public class JasperDesignBuilder<T> {
    private Integer MAX_WIDTH = 555;
    private Integer HEIGHT_DEFAULT_HEADER = 15;
    private Integer HEIGHT_DEFAULT_COLUMN = 15;
    private Integer HEIGHT_SUB_HEADER = 30;
    private Integer WEIDTH_DEFAULT = 100;
    private Integer GROUP_HEADER_COUNT = 0;
    private Integer COLUMN_COUNT = 0;
    protected final JasperDesign design = new JasperDesign();
    protected final JRDesignBand header = new JRDesignBand();
    protected final JRDesignBand field = new JRDesignBand();
    protected final JRDesignBand footer = new JRDesignBand();
    protected final JRDesignBand noData = new JRDesignBand();

    private java.util.List<T> dataList = new ArrayList<T>();
    private Map<String, Object> parameters = new HashMap<>();
    private byte[] report = null;

    private List<Integer> GROUP_HEADER_POS = new ArrayList<>();
    private String type = "pdf";
    private boolean havingSubHeader = false;

    private JasperPrint jasperPrint = null;
    private List<JRDesignTextField> groupHeaders = new ArrayList<>();
    private List<JRDesignTextField> headers = new ArrayList<>();
    private List<JRDesignTextField> fields = new ArrayList<>();

    public JasperDesignBuilder(String name) {
        design.setName(name);
    }

    public JasperDesignBuilder addColumn(String title, String property, Class className, int width) throws JRException {
        JRDesignField field = new JRDesignField();
        field.setName(property);
        field.setValueClass(className);
        design.addField(field);
        design.setOrientation(OrientationEnum.PORTRAIT);

        //Header
        if (havingSubHeader) {
            header.setHeight(HEIGHT_SUB_HEADER);
        } else {
            header.setHeight(HEIGHT_SUB_HEADER);
        }

//        JRDesignStyle headerStyle = new JRDesignStyle();
//        headerStyle.setName("headerStyle");
//        headerStyle.setDefault(true);
//        headerStyle.setFontSize(10f);
//        headerStyle.setPdfFontName("Helvetica");
//        headerStyle.setPdfEncoding("UTF-8");
//        headerStyle.setBold(true);

        JRDesignTextField textField = new JRDesignTextField();
        setBorderLineWidthOne(textField);
        textField.setBlankWhenNull(true);
        textField.setX(COLUMN_COUNT * 100);
        if (havingSubHeader && GROUP_HEADER_COUNT > 0) {
            textField.setY(HEIGHT_DEFAULT_HEADER);
            textField.setHeight(HEIGHT_DEFAULT_HEADER);
            GROUP_HEADER_COUNT--;
        } else if(havingSubHeader){
            textField.setHeight(HEIGHT_SUB_HEADER);
        }else{
            textField.setHeight(HEIGHT_DEFAULT_HEADER);
            textField.setY(0);
        }
        textField.setWidth(WEIDTH_DEFAULT);
        textField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        textField.setExpression(new JRDesignExpression(String.format("$P{%s}", property)));
//        textField.setForecolor(Color.white);
        textField.setBackcolor(Color.LIGHT_GRAY);
        textField.setMode(ModeEnum.OPAQUE);
        textField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        textField.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
//        textField.setStyle(headerStyle);
//        textField.set
        headers.add(textField);
//        header.addElement(textField);

        //data
        this.field.setHeight(HEIGHT_DEFAULT_COLUMN);

        JRDesignTextField tf1 = new JRDesignTextField();
        setBorderLineWidthOne(tf1);
        tf1.setBlankWhenNull(true);
        tf1.setX(this.fields.size() * 100);
        tf1.setY(0);
        tf1.setWidth(WEIDTH_DEFAULT);
        tf1.setHeight(HEIGHT_DEFAULT_COLUMN);
        tf1.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        tf1.setExpression(new JRDesignExpression(String.format("$F{%s}", property)));
        tf1.setStretchWithOverflow(true);
        tf1.setMode(ModeEnum.OPAQUE);
        fields.add(tf1);
//        this.field.addElement(tf1);

//        parameter
        JRDesignParameter parameter = new JRDesignParameter();
        parameter.setName(property);
        parameter.setValueClass(String.class);
        design.addParameter(parameter);
//
        parameters.put(property, title);

        COLUMN_COUNT++;
        return this;
    }

    public JasperDesignBuilder addType(String type) {
        this.type = type;
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

    public JasperDesignBuilder addGroup(Integer numberOfHeader, String subHeaderTitle) throws JRException {
        for (JRDesignTextField e : headers) {
            e.setHeight(HEIGHT_SUB_HEADER);
            e.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
            e.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        }
//        headers=new ArrayList<>();
        havingSubHeader = true;
        if (GROUP_HEADER_COUNT > 0) {
            throw new RuntimeException("Add column to previous group still pending.");
        } else {
            GROUP_HEADER_COUNT = numberOfHeader;
        }

        GROUP_HEADER_POS.add(COLUMN_COUNT);

        JRDesignTextField textField = new JRDesignTextField();
        setBorderLineWidthOne(textField);
        textField.setBlankWhenNull(true);
        textField.setX(headers.size() * 100);
        textField.setY(0);
        textField.setWidth(numberOfHeader);
        textField.setHeight(HEIGHT_DEFAULT_HEADER);
        textField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        textField.setExpression(new JRDesignExpression(String.format("$P{%s}", subHeaderTitle)));
//        textField.setForecolor(new Color(67, 108, 168));
        textField.setBackcolor(Color.LIGHT_GRAY);
        textField.setMode(ModeEnum.OPAQUE);
        groupHeaders.add(textField);

//                parameter
        JRDesignParameter parameter = new JRDesignParameter();
        parameter.setName(subHeaderTitle);
        parameter.setValueClass(String.class);
        design.addParameter(parameter);

        parameters.put(subHeaderTitle, subHeaderTitle);

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
        int avgWidth=MAX_WIDTH/fields.size();
        for (int i=0;i<groupHeaders.size();i++) {
            this.groupHeaders.get(i).setX(GROUP_HEADER_POS.get(i)*avgWidth);
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

        design.setColumnHeader(header);
        ((JRDesignSection) design.getDetailSection()).addBand(field);

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
