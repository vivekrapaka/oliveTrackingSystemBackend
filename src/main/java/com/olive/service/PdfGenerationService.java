package com.olive.service;


import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.olive.dto.DailyLogDTO;
import com.olive.dto.TaskTimeSummaryResponse;
import com.olive.dto.TimesheetResponse;
import org.springframework.stereotype.Service;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
public class PdfGenerationService {

    public ByteArrayInputStream generateTimesheetPdf(TimesheetResponse timesheet) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Timesheet for: " + timesheet.getTeammateName()).setBold().setFontSize(18));
        document.add(new Paragraph("Total Hours for Period: " + timesheet.getTotalHoursForPeriod()));
        document.add(new Paragraph("\n"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
        table.addHeaderCell("Date");
        table.addHeaderCell("Hours Logged");

        for (DailyLogDTO log : timesheet.getDailyLogs()) {
            table.addCell(log.getDate().toString());
            table.addCell(String.valueOf(log.getTotalHours()));
        }
        document.add(table);
        document.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream generateTaskSummaryPdf(TaskTimeSummaryResponse summary) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Time Summary for Task: " + summary.getTaskName()).setBold().setFontSize(18));
        document.add(new Paragraph("Total Hours Logged: " + summary.getTotalHours()));
        document.add(new Paragraph("\n"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
        table.addHeaderCell("Discipline");
        table.addHeaderCell("Hours Logged");

        table.addCell("Development Hours");
        table.addCell(String.valueOf(summary.getBreakdown().getDevelopmentHours()));
        table.addCell("Testing Hours");
        table.addCell(String.valueOf(summary.getBreakdown().getTestingHours()));
        table.addCell("Other Hours");
        table.addCell(String.valueOf(summary.getBreakdown().getOtherHours()));

        document.add(table);
        document.close();
        return new ByteArrayInputStream(out.toByteArray());
    }
}
