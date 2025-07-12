package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.dto.LeaveSummaryReportDTO;

import java.util.List;

public interface LeaveSummaryServiceReport {
    List<LeaveSummaryReportDTO> getSummaryByUser(Long userId); // overall summary
}
