package com.seugi.api.domain.timetable.service

import com.seugi.api.domain.profile.application.port.`in`.RetrieveSchIdNumUseCase
import com.seugi.api.domain.timetable.domain.TimetableRepository
import com.seugi.api.domain.timetable.domain.mapper.TimetableMapper
import com.seugi.api.domain.timetable.domain.model.Timetable
import com.seugi.api.domain.timetable.exception.TimetableException
import com.seugi.api.domain.timetable.presentation.dto.request.CreateTimetableRequest
import com.seugi.api.domain.timetable.presentation.dto.request.FixTimetableRequest
import com.seugi.api.domain.workspace.domain.entity.WorkspaceEntity
import com.seugi.api.domain.workspace.domain.model.SchoolInfo
import com.seugi.api.domain.workspace.service.WorkspaceService
import com.seugi.api.global.exception.CustomException
import com.seugi.api.global.infra.nice.school.NiceSchoolService
import com.seugi.api.global.response.BaseResponse
import jakarta.transaction.Transactional
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Service
class TimetableServiceImpl(
    private val workspaceService: WorkspaceService,
    private val schIdNumUseCase: RetrieveSchIdNumUseCase,
    private val timetableMapper: TimetableMapper,
    private val timetableRepository: TimetableRepository,
    private val niceSchoolService: NiceSchoolService,
) : TimetableService {

    @Scheduled(cron = "0 0 0 ? * SUN")
    protected fun resetAllTimetable() {
        timetableRepository.deleteAll()
    }

    private fun checkUserInWorkspace(workspaceEntity: WorkspaceEntity, userId: Long) {
        if (workspaceEntity.workspaceAdmin != userId &&
            !workspaceEntity.middleAdmin.contains(userId) &&
            !workspaceEntity.teacher.contains(userId) &&
            !workspaceEntity.student.contains(userId)
        ) throw CustomException(TimetableException.FORBIDDEN)
    }

    private fun saveTimetable(timetables: List<Timetable>) {
        val entities = timetables.map(timetableMapper::toEntity).filter { it.classNum.isNotEmpty() }
        timetableRepository.saveAll(entities)
    }

    private fun getDateRange(): Pair<String, String> {
        val date = LocalDate.now()
        val startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        return startOfWeek.formatForTimetable().replace("-", "") to endOfWeek.formatForTimetable().replace("-", "")
    }

    private fun LocalDate.formatForTimetable(): String {
        return "$year-${monthValue.dateFormat()}-${dayOfMonth.dateFormat()}"
    }

    private fun Int.dateFormat(): String {
        return "%02d".format(this)
    }

    private fun getToday(): String {
        return LocalDate.now().formatForTimetable()
    }

    private fun getSchoolTimetable(
        schoolInfo: SchoolInfo,
        dateRange: Pair<String, String>,
        workspaceId: String,
    ): List<Timetable> {
        return niceSchoolService.getSchoolTimeTable(schoolInfo, dateRange.first, dateRange.second, workspaceId)
            ?.map { timetableMapper.niceTimetableToModel(it, workspaceId) }
            ?: emptyList()
    }

    private fun checkRole(workspaceId: String, userId: Long) {
        workspaceService.findWorkspaceById(workspaceId).let {
            if (it.workspaceAdmin != userId &&
                !it.middleAdmin.contains(userId) &&
                !it.teacher.contains(userId)
            ) throw CustomException(
                TimetableException.FORBIDDEN
            )
        }
    }

    @Transactional
    override fun createTimetable(createTimetableRequest: CreateTimetableRequest, userId: Long): BaseResponse<Unit> {

        checkRole(createTimetableRequest.workspaceId, userId)
        val timetableEntity = timetableMapper.requestToEntity(createTimetableRequest)

        timetableRepository.save(timetableEntity)

        return BaseResponse(
            message = "시간표 저장 성공!"
        )

    }

    @Transactional
    override fun fixTimetable(userId: Long, fixTimetableRequest: FixTimetableRequest): BaseResponse<Unit> {
        val timetable = timetableRepository.findById(fixTimetableRequest.id)
            .orElseThrow { CustomException(TimetableException.NOT_FOUND) }

        checkRole(timetable.workspaceId, userId)

        timetable.updateSubject(fixTimetableRequest.subject)

        timetableRepository.save(timetable)

        return BaseResponse(
            message = "시간표 수정 성공!"
        )

    }

    @Transactional
    override fun deleteTimetable(userId: Long, id: Long): BaseResponse<Unit> {
        val timetable = timetableRepository.findById(id).orElseThrow { CustomException(TimetableException.NOT_FOUND) }

        checkRole(timetable.workspaceId, userId)

        timetableRepository.delete(timetable)

        return BaseResponse(
            message = "시간표 삭제 성공!"
        )

    }

    @Transactional
    override fun resetTimetable(workspaceId: String, userId: Long) {
        val workspace = workspaceService.findWorkspaceById(workspaceId)
        checkUserInWorkspace(workspace, userId)

        timetableRepository.deleteAllByWorkspaceId(workspaceId)

        val dateRange = getDateRange()

        val timetables = getSchoolTimetable(workspace.schoolInfo, dateRange, workspaceId)
        saveTimetable(timetables.ifEmpty { listOf(Timetable(workspaceId = workspaceId)) })

    }

    @Transactional
    override fun getWeekendTimetableByUserInfo(workspaceId: String, userId: Long): List<Timetable> {
        if (!timetableRepository.checkTimetableByWorkspaceId(workspaceId)) resetTimetable(workspaceId, userId)

        val userInfo = schIdNumUseCase.retrieveSchIdNum(workspaceId, userId)

        val timetables = timetableRepository.findWeekendTimetableByWorkspaceId(workspaceId)
            .filter { it.classNum == userInfo.schClass.toString() && it.grade == userInfo.schGrade.toString() }

        return timetables.map(timetableMapper::toDomain)
    }

    @Transactional
    override fun getDayTimetableByUserInfo(workspaceId: String, userId: Long): List<Timetable> {
        if (!timetableRepository.checkTimetableByWorkspaceId(workspaceId)) resetTimetable(workspaceId, userId)

        val userInfo = schIdNumUseCase.retrieveSchIdNum(workspaceId, userId)
        val today = getToday()

        val timetables = timetableRepository.findTodayTimetableByWorkspaceId(workspaceId, today)
            .filter { it.classNum == userInfo.schClass.toString() && it.grade == userInfo.schGrade.toString() }

        return timetables.map(timetableMapper::toDomain)

    }
}
