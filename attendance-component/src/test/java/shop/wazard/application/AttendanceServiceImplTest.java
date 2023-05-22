package shop.wazard.application;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import shop.wazard.application.domain.AccountForAttendance;
import shop.wazard.application.domain.Attendance;
import shop.wazard.application.domain.EnterRecord;
import shop.wazard.application.domain.ExitRecord;
import shop.wazard.application.port.in.AttendanceService;
import shop.wazard.application.port.out.AbsentForAttendancePort;
import shop.wazard.application.port.out.AccountForAttendancePort;
import shop.wazard.application.port.out.CommuteRecordForAttendancePort;
import shop.wazard.dto.*;
import shop.wazard.exception.EnterRecordNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AttendanceServiceImpl.class})
class AttendanceServiceImplTest {

    @Autowired
    private AttendanceService attendanceService;
    @MockBean
    private AccountForAttendancePort accountForAttendancePort;
    @MockBean
    private CommuteRecordForAttendancePort commuteRecordForAttendancePort;
    @MockBean
    private AbsentForAttendancePort absentForAttendancePort;

    @Test
    @DisplayName("고용주 - 근무자 결석 처리 - 성공")
    public void markingAbsentSuccess() throws Exception {
        // given
        MarkingAbsentReqDto markingAbsentReqDto = MarkingAbsentReqDto.builder()
                .email("test@email.com")
                .companyId(1L)
                .accountId(2L)
                .build();
        AccountForAttendance accountForAttendance = AccountForAttendance.builder()
                .id(3L)
                .roles("EMPLOYER")
                .build();

        // when
        Mockito.when(accountForAttendancePort.findAccountByEmail(anyString()))
                .thenReturn(accountForAttendance);

        // then
        Assertions.assertDoesNotThrow(() -> attendanceService.markingAbsent(markingAbsentReqDto));
    }

    @Test
    @DisplayName("근무자 - 출근 시간 기록 - 성공")
    public void recordEnterTimeSuccess() throws Exception {
        // given
        RecordEnterTimeReqDto recordEnterTimeReqDto = RecordEnterTimeReqDto.builder()
                .accountId(1L)
                .companyId(2L)
                .tardy(false)
                .build();

        // when
        Mockito.doNothing().when(commuteRecordForAttendancePort)
                .recordEnterTime(any(EnterRecord.class));

        // then
        Assertions.assertDoesNotThrow(() -> attendanceService.recordEnterTime(recordEnterTimeReqDto));
    }

    @Test
    @DisplayName("근무자 - 퇴근 시간 기록 - 성공")
    public void recordExitTimeSuccess() throws Exception {
        // given
        RecordExitTimeReqDto recordExitTimeReqDto = RecordExitTimeReqDto.builder()
                .accountId(1L)
                .companyId(2L)
                .build();

        // when
        Mockito.when(commuteRecordForAttendancePort.findEnterRecord(any(RecordExitTimeReqDto.class)))
                .thenReturn(3L);
        Mockito.doNothing().when(commuteRecordForAttendancePort)
                .recordExitTime(any(ExitRecord.class), anyLong());

        // then
        Assertions.assertDoesNotThrow(() -> attendanceService.recordExitTime(recordExitTimeReqDto));
    }

    @Test
    @DisplayName("근무자 - 퇴근 시간 기록 실패 - 출근 기록이 없는 경우")
    public void recordExitTimeFailed() throws Exception {
        // given
        RecordExitTimeReqDto recordExitTimeReqDto = RecordExitTimeReqDto.builder()
                .accountId(1L)
                .companyId(2L)
                .build();

        // when
        Mockito.when(commuteRecordForAttendancePort.findEnterRecord(any(RecordExitTimeReqDto.class)))
                .thenThrow(EnterRecordNotFoundException.class);

        // then
        Assertions.assertThrows(EnterRecordNotFoundException.class, () -> attendanceService.recordExitTime(recordExitTimeReqDto));
    }

    @Test
    @DisplayName("근무자 - 요일별 출근부 조회 - 성공")
    public void getMyAttendanceByDayOfTheWeekSuccess() throws Exception {
        // given
        GetAttendanceByDayOfTheWeekReqDto getAttendanceByDayOfTheWeekReqDto = GetAttendanceByDayOfTheWeekReqDto.builder()
                .accountId(1L)
                .companyId(2L)
                .email("test@email.com")
                .build();
        LocalDate date = setDefaultDayEmployeeAccountForAttendance();
        AccountForAttendance accountForAttendance = setDefaultEmployeeAccountForAttendance();
        List<GetAttendanceByDayOfTheWeekResDto> getAttendanceByDayOfTheWeekResDtoList = setDefaultEmployeeAttendanceList();

        // when
        Mockito.when(accountForAttendancePort.findAccountByEmail(anyString()))
                .thenReturn(accountForAttendance);
        Mockito.when(commuteRecordForAttendancePort.getMyAttendanceByDayOfTheWeek(any(Attendance.class)))
                .thenReturn(getAttendanceByDayOfTheWeekResDtoList);
        List<GetAttendanceByDayOfTheWeekResDto> result = attendanceService.getMyAttendanceByDayOfTheWeek(getAttendanceByDayOfTheWeekReqDto, date);

        // then
        Assertions.assertAll(
                () -> Assertions.assertEquals(getAttendanceByDayOfTheWeekResDtoList.get(0).getAccountId(),result.get(0).getAccountId()),
                () -> Assertions.assertEquals(getAttendanceByDayOfTheWeekResDtoList.get(1).getAccountId(),result.get(1).getAccountId())
        );
    }

    private AccountForAttendance setDefaultEmployeeAccountForAttendance() {
        return AccountForAttendance.builder()
                .id(1L)
                .roles("EMPLOYEE")
                .build();
    }

    private LocalDate setDefaultDayEmployeeAccountForAttendance() {
        return LocalDate.now();
    }

    private List<GetAttendanceByDayOfTheWeekResDto> setDefaultEmployeeAttendanceList() {
        List<GetAttendanceByDayOfTheWeekResDto> getAttendanceByDayOfTheWeekResDtoList = new ArrayList<>();
        GetAttendanceByDayOfTheWeekResDto getAttendanceByDayOfTheWeekResDto1 = GetAttendanceByDayOfTheWeekResDto.builder()
                .accountId(1L)
                .userName("test")
                .enterTime(LocalDateTime.now())
                .exitTime(LocalDateTime.now())
                .build();
        GetAttendanceByDayOfTheWeekResDto getAttendanceByDayOfTheWeekResDto2 = GetAttendanceByDayOfTheWeekResDto.builder()
                .accountId(1L)
                .userName("test")
                .enterTime(LocalDateTime.now())
                .exitTime(LocalDateTime.now())
                .build();
        getAttendanceByDayOfTheWeekResDtoList.add(getAttendanceByDayOfTheWeekResDto1);
        getAttendanceByDayOfTheWeekResDtoList.add(getAttendanceByDayOfTheWeekResDto2);
        return getAttendanceByDayOfTheWeekResDtoList;
    }

}