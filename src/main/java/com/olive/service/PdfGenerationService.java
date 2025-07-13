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

        if (summary.getDeveloperNames() != null && !summary.getDeveloperNames().isEmpty()) {
            document.add(new Paragraph("Contributing Developers: " + String.join(", ", summary.getDeveloperNames())));
        }
        if (summary.getTesterNames() != null && !summary.getTesterNames().isEmpty()) {
            document.add(new Paragraph("Contributing Testers: " + String.join(", ", summary.getTesterNames())));
        }

        document.add(new Paragraph("\n"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1, 1}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.addHeaderCell(new Cell().add(new Paragraph("Discipline").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Actual Hours").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Estimated Hours").setBold()));

        table.addCell("Development");
        table.addCell(String.valueOf(summary.getBreakdown().getDevelopmentHours()));
        table.addCell(String.valueOf(summary.getDevelopmentDueHours()));

        table.addCell("Testing");
        table.addCell(String.valueOf(summary.getBreakdown().getTestingHours()));
        table.addCell(String.valueOf(summary.getTestingDueHours()));

        document.add(table);
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
