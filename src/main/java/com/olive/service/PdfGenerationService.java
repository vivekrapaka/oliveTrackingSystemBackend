package com.olive.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.olive.dto.DailyLogDTO;
import com.olive.dto.TaskTimeSummaryResponse;
import com.olive.dto.TeammateEffortDTO;
import com.olive.dto.TimesheetResponse;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.stream.Collectors;

@Service
public class PdfGenerationService {

    public ByteArrayInputStream generateTimesheetPdf(TimesheetResponse timesheet) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(new PdfDocument(new PdfWriter(out)));

        addHeader(document, "Karya Task Document - Teammate Timesheet");
        document.add(new Paragraph("Report for: " + timesheet.getTeammateName()).setBold());
        document.add(new Paragraph("Total Hours Logged: " + timesheet.getTotalHoursForPeriod()));
        document.add(new Paragraph("\n"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1, 4}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.addHeaderCell(new Cell().add(new Paragraph("Date").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Total Hours").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Tasks Worked On").setBold()));

        for (DailyLogDTO log : timesheet.getDailyLogs()) {
            table.addCell(log.getDate().toString());
            table.addCell(String.valueOf(log.getTotalHours()));
            String tasks = log.getTaskLogs().stream()
                    .map(t -> t.getTaskName() + " (" + t.getHours() + "h)")
                    .collect(Collectors.joining(", "));
            table.addCell(tasks);
        }
        document.add(table);
        document.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream generateTaskSummaryPdf(TaskTimeSummaryResponse summary) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(new PdfDocument(new PdfWriter(out)));

        addHeader(document, "Karya Task Document - Task Time Summary");
        document.add(new Paragraph("Report for Task: " + summary.getTaskName()).setBold());
        document.add(new Paragraph("Total Hours Logged: " + summary.getTotalHours()));
        document.add(new Paragraph("Dev Manager: " + summary.getDevManagerName()));
        document.add(new Paragraph("Test Manager: " + summary.getTestManagerName()));
        document.add(new Paragraph("\n"));

        // Development Effort Table
        document.add(new Paragraph("Development Effort").setBold());
        Table devTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}));
        devTable.setWidth(UnitValue.createPercentValue(100));
        devTable.addHeaderCell(new Cell().add(new Paragraph("Developer").setBold()));
        devTable.addHeaderCell(new Cell().add(new Paragraph("Hours Logged").setBold()));
        for (TeammateEffortDTO effort : summary.getDeveloperEffort()) {
            devTable.addCell(effort.getTeammateName());
            devTable.addCell(String.valueOf(effort.getHoursLogged()));
        }
        document.add(devTable);
        document.add(new Paragraph("Total Estimated Development Hours: " + summary.getDevelopmentDueHours()));
        document.add(new Paragraph("\n"));

        // Testing Effort Table
        document.add(new Paragraph("Testing Effort").setBold());
        Table testTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}));
        testTable.setWidth(UnitValue.createPercentValue(100));
        testTable.addHeaderCell(new Cell().add(new Paragraph("Tester").setBold()));
        testTable.addHeaderCell(new Cell().add(new Paragraph("Hours Logged").setBold()));
        for (TeammateEffortDTO effort : summary.getTesterEffort()) {
            testTable.addCell(effort.getTeammateName());
            testTable.addCell(String.valueOf(effort.getHoursLogged()));
        }
        document.add(testTable);
        document.add(new Paragraph("Total Estimated Testing Hours: " + summary.getTestingDueHours()));

        document.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addHeader(Document document, String title) {
        Paragraph header = new Paragraph(title)
                .setBold()
                .setFontColor(ColorConstants.BLUE)
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(header);
        document.add(new Paragraph("\n"));
    }
}
