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
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.List;

import static com.tareq.dynamicreport.builder.Defs.*;

public class JasperDesignBuilder<T> {
    private Integer GROUP_HEADER_COUNT = 0;
    private Integer COLUMN_COUNT = 0;
    protected final JasperDesign DESIGN = new JasperDesign();
    protected final JRDesignBand HEADER_BAND = new JRDesignBand();
    protected final JRDesignBand FIELD_BAND = new JRDesignBand();
    protected final JRDesignBand FOOTER_BAND = new JRDesignBand();
    protected final JRDesignBand NO_DATA = new JRDesignBand();

    protected final JRDesignBand NAME_BAND = new JRDesignBand();

    private java.util.List<T> DATA_LIST = new ArrayList<T>();
    private Map<String, Object> PARAMETERS = new HashMap<>();
    private byte[] REPORT = null;

    private List<Integer> GROUP_HEADER_POSITIONS = new ArrayList<>();
    private String TYPE = "pdf";
    private boolean HAVING_SUBHEADER = false;

    private JasperPrint JASPER_PRINT = null;
    private List<JRDesignTextField> GROUP_HEADERS = new ArrayList<>();
    private List<JRDesignTextField> HEADERS = new ArrayList<>();
    private List<JRDesignTextField> FIELDS = new ArrayList<>();
    private Map<String, Integer> GROUP_MAP = new HashMap<>();
    List<String> WHITELIST = new ArrayList<>();

    public JasperDesignBuilder() {
    }

    public JasperDesignBuilder createReportFromObject(List<T> dataList) throws JRException, IllegalAccessException, NoSuchFieldException {
        Object data = dataList.get(0);

        //counting groups
        for (int i = 0; i < data.getClass().getDeclaredFields().length; i++) {
            Field field = data.getClass().getDeclaredFields()[i];
            if (field.getName().contains("_")) {
                String group = field.getName().split("_")[0];
                if (GROUP_MAP.get(group) == null) {
                    GROUP_MAP.put(group, 1);
                } else {
                    GROUP_MAP.put(group, GROUP_MAP.get(group) + 1);
                }
            }
        }

        populateTableColumn(data);
        addData(dataList);
        return this;
    }

    public JasperDesignBuilder createReportFromObject(List<T> dataList, List<String> whiteList) throws JRException, IllegalAccessException, NoSuchFieldException {
        Object data = dataList.get(0);

        //counting groups
        for (int i = 0; i < data.getClass().getDeclaredFields().length; i++) {
            Field field = data.getClass().getDeclaredFields()[i];
            if (field.getName().contains("_")) {
                String group = field.getName().split("_")[0];
                if (GROUP_MAP.get(group) == null) {
                    GROUP_MAP.put(group, 1);
                } else {
                    GROUP_MAP.put(group, GROUP_MAP.get(group) + 1);
                }
            }
        }

        //getting whitelist
//        Field fields = data.getClass().getDeclaredField("whitelist");
//        if (fields != null && fields.getGenericType() instanceof ParameterizedType) {
//            WHITELIST = (List<String>) fields.get(data);
//        }

        this.WHITELIST=whiteList;

        populateTableColumn(data);
        addData(dataList);
        return this;
    }

    private void populateTableColumn(Object object) throws JRException, IllegalAccessException {
        for (int i = 0; i < object.getClass().getDeclaredFields().length; i++) {
            Field field = object.getClass().getDeclaredFields()[i];

            //checking whitelist
            if(WHITELIST!=null && !WHITELIST.isEmpty() && !WHITELIST.contains(field.getName())){
                continue;
            }

            //identifying and adding group and column
            //ignoring whitelist as a group/column
            if (field.getName().contains("_")) {
                String group = field.getName().split("_")[0];
                String column = field.getName().split("_")[1];
                if (object.getClass().getDeclaredFields()[i - 1].getName().contains("_") && group.equalsIgnoreCase(object.getClass().getDeclaredFields()[i - 1].getName().split("_")[0])) {
                    this.addColumn(column, field.getName(), field.get(object).getClass(), 10);
                } else {
                    this.addGroup(GROUP_MAP.get(group), group);
                    this.addColumn(column, field.getName(), field.get(object).getClass(), 10);
                }
            } else if (!field.getName().equalsIgnoreCase("whitelist")) {
                this.addColumn(field.getName(), field.getName(), field.get(object).getClass(), 10);
            }
        }
    }

    public JasperDesignBuilder addColumn(String title, String property, Class className, int width) throws JRException {
        DESIGN.setOrientation(OrientationEnum.PORTRAIT);

        //dynamically creating header
        HEADERS.add(createHeader(property));

        //dynamically creating field
        this.FIELD_BAND.setHeight(HEIGHT_COLUMN_DEFAULT);
        JRDesignField field = new JRDesignField();
        field.setName(property);
        field.setValueClass(className);
        DESIGN.addField(field);
        FIELDS.add(createField(property));

        //setting header values as parameter
        if (PARAMETERS.get(property) == null) {
            JRDesignParameter parameter = new JRDesignParameter();
            parameter.setName(property);
            parameter.setValueClass(String.class);
            DESIGN.addParameter(parameter);
            PARAMETERS.put(property, title.toUpperCase());
        }
        COLUMN_COUNT++;

        return this;
    }

    public JasperDesignBuilder addGroup(Integer numberOfHeader, String subHeaderTitle) throws JRException {
        HAVING_SUBHEADER = true;
        if (GROUP_HEADER_COUNT > 0) {
            throw new RuntimeException("Columns of previous group still pending.");
        } else {
            GROUP_HEADER_COUNT = numberOfHeader;
        }
        for (JRDesignTextField e : HEADERS) {
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
        GROUP_HEADERS.add(groupHeader);

        //parameter
        if (PARAMETERS.get(subHeaderTitle) == null) {
            JRDesignParameter parameter = new JRDesignParameter();
            parameter.setName(subHeaderTitle);
            parameter.setValueClass(String.class);
            DESIGN.addParameter(parameter);
            PARAMETERS.put(subHeaderTitle, subHeaderTitle.toUpperCase());
        }
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
        if (HAVING_SUBHEADER && GROUP_HEADER_COUNT > 0) {
            header.setY(HEIGHT_HEADER_DEFAULT);
            header.setHeight(HEIGHT_HEADER_DEFAULT);
            GROUP_HEADER_COUNT--;
        } else if (HAVING_SUBHEADER) {
            header.setHeight(HEIGHT_HEADER_LARGE);
        } else {
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


    public JasperDesignBuilder addType(String type) {
        this.TYPE = type;
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
        DESIGN.setTitle(titleBand);

        return this;
    }

    public JasperDesignBuilder addReportName(String reportName) {

        NAME_BAND.setHeight(HEIGHT_REPORT_DEFAULT + MARGIN_DEFAULT);

        JRDesignStaticText titleText = new JRDesignStaticText();
        titleText.setText(reportName);
        titleText.setX(0);
        titleText.setY(0);
        titleText.setHeight(HEIGHT_REPORT_DEFAULT);
        titleText.setWidth(WIDTH_MAX);
        titleText.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        titleText.setFontSize(FONT_SIZE_REPORTNAME_DEFAULT);

        NAME_BAND.addElement(titleText);

        return this;
    }

    public JasperDesignBuilder addCriteriaDetails(String criteriaDetails) {

        NAME_BAND.setHeight(NAME_BAND.getHeight() + HEIGHT_CRITERIA_DEFAULT + MARGIN_DEFAULT);

        JRDesignStaticText criteria = new JRDesignStaticText();
        criteria.setText(criteriaDetails);
        criteria.setX(0);
        criteria.setY((NAME_BAND.getElements().length > 0) ? HEIGHT_CRITERIA_DEFAULT + MARGIN_DEFAULT : 0);
        criteria.setHeight(HEIGHT_CRITERIA_DEFAULT);
        criteria.setWidth(WIDTH_MAX);
        criteria.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
        criteria.setFontSize(FONT_SIZE_CRITERIA_DEFAULT);
        NAME_BAND.addElement(criteria);

        JRDesignTextField date = new JRDesignTextField();
        date.setBlankWhenNull(true);
        date.setX(WIDTH_MAX - WIDTH_DATE_DEFAULT);
        date.setY((NAME_BAND.getElements().length > 0) ? HEIGHT_CRITERIA_DEFAULT + MARGIN_DEFAULT : 0);
        date.setWidth(WIDTH_DATE_DEFAULT);
        date.setHeight(HEIGHT_CRITERIA_DEFAULT);
        date.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
//        tfc5.setStyle(mystyle);
        date.setExpression(new JRDesignExpression("new java.util.Date()"));

        NAME_BAND.addElement(date);


        return this;
    }

    private void setBorderLineWidthOne(JRDesignTextField textField) {
        JRLineBox box = textField.getLineBox();
        box.getLeftPen().setLineWidth(1);
        box.getRightPen().setLineWidth(1);
        box.getTopPen().setLineWidth(1);
        box.getBottomPen().setLineWidth(1);
    }

    public JasperDesignBuilder addPageFooter() {
        FOOTER_BAND.setHeight(30);

        JRDesignTextField tfc6 = new JRDesignTextField();
        tfc6.setBlankWhenNull(true);
        tfc6.setX(0);
        tfc6.setY(0);
        tfc6.setWidth(500);
        tfc6.setHeight(30);
        tfc6.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
//        tfc6.setStyle(mystyle);
        tfc6.setExpression(new JRDesignExpression("\"Page \"+$V{PAGE_NUMBER}+\" of\"+\" \" + $V{PAGE_NUMBER}"));

        FOOTER_BAND.addElement(tfc6);

        return this;
    }

//    public JasperDesignBuilder addParameters(Map<String, Object> parameters) throws JRException {
////        this.parameters.putAll(parameters);
//        JRDesignParameter par = new JRDesignParameter();
//        par.setName("createdBy");
//        par.setValueClass(String.class);
//        design.addParameter(par);
//        return this;
//    }

    public JasperDesignBuilder addNoData() {
        NO_DATA.setHeight(30);

        JRDesignStaticText tfc5 = new JRDesignStaticText();

        tfc5.setX(0);
        tfc5.setY(0);
        tfc5.setWidth(500);
        tfc5.setHeight(30);
        tfc5.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
//        tfc5.setStyle(mystyle);
        tfc5.setText("No Data Found");

        NO_DATA.addElement(tfc5);

        return this;
    }

    public JasperDesignBuilder addData(List<T> list) {
        this.DATA_LIST = list;
        return this;
    }

    public JasperDesignBuilder build() throws JRException {
        int avgWidth = WIDTH_MAX / FIELDS.size();
        for (int i = 0; i < GROUP_HEADERS.size(); i++) {
            this.GROUP_HEADERS.get(i).setX(GROUP_HEADER_POSITIONS.get(i) * avgWidth);
            this.GROUP_HEADERS.get(i).setWidth(this.GROUP_HEADERS.get(i).getWidth() * avgWidth);
            HEADER_BAND.addElement(this.GROUP_HEADERS.get(i));
        }
        for (int i = 0; i < HEADERS.size(); i++) {
            this.HEADERS.get(i).setWidth(avgWidth);
            this.HEADERS.get(i).setX(avgWidth * i);
            this.HEADER_BAND.addElement(HEADERS.get(i));
        }
        for (int i = 0; i < FIELDS.size(); i++) {
            this.FIELDS.get(i).setWidth(avgWidth);
            this.FIELDS.get(i).setX(avgWidth * i);
            this.FIELD_BAND.addElement(FIELDS.get(i));
        }

        if (HAVING_SUBHEADER) {
            HEADER_BAND.setHeight(HEIGHT_HEADER_LARGE);
        } else {
            HEADER_BAND.setHeight(HEIGHT_HEADER_DEFAULT);
        }

        DESIGN.setName("dynamic_report");

        DESIGN.setColumnHeader(HEADER_BAND);
        ((JRDesignSection) DESIGN.getDetailSection()).addBand(FIELD_BAND);

        DESIGN.setPageHeader(NAME_BAND);
        DESIGN.setPageFooter(FOOTER_BAND);
        DESIGN.setNoData(NO_DATA);

        JasperReport jasperReport = JasperCompileManager.compileReport(DESIGN);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(DATA_LIST);
        this.JASPER_PRINT = JasperFillManager.fillReport(jasperReport, PARAMETERS, dataSource);
        if (TYPE.equalsIgnoreCase("xlsx")) {
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(this.JASPER_PRINT));
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(byteStream));
            SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
            configuration.setDetectCellType(true);//Set configuration as you like it!!
            configuration.setCollapseRowSpan(false);
            exporter.setConfiguration(configuration);
            exporter.exportReport();
            REPORT = byteStream.toByteArray();
        } else {
            REPORT = JasperExportManager.exportReportToPdf(JASPER_PRINT);
        }

        return this;
    }

    public byte[] getReport() {
        return REPORT;
    }

    public void print(String path) throws JRException {
        JasperExportManager.exportReportToPdfFile(this.JASPER_PRINT, path);
    }

    public JasperDesign getDesign() {
        return DESIGN;
    }
}
