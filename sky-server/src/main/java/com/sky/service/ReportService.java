package com.sky.service;

import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {
    TurnoverReportVO getTurnoverStatistics(LocalDate begin,LocalDate end);

    /*
    * @Description 用户统计
    * @Param
    * @return
    **/

    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);
}
