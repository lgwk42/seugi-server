package com.seugi.api.domain.timetable.domain.mapper

import com.seugi.api.domain.timetable.domain.TimetableEntity
import com.seugi.api.domain.timetable.domain.model.Timetable
import com.seugi.api.global.common.Mapper
import com.seugi.api.global.infra.nice.school.timetable.Row
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TimetableMapper : Mapper<Timetable, TimetableEntity> {

    override fun toDomain(entity: TimetableEntity): Timetable {
        return Timetable(
            id = entity.id,
            workspaceId = entity.workspaceId,
            grade = entity.grade,
            classNum = entity.classNum,
            time = entity.time,
            subject = entity.subject,
            date = entity.date,
        )
    }

    override fun toEntity(domain: Timetable): TimetableEntity {
        return TimetableEntity(
            id = domain.id,
            workspaceId = domain.workspaceId ?: "",
            grade = domain.grade ?: "",
            classNum = domain.classNum ?: "",
            time = domain.time ?: "",
            subject = domain.subject ?: "",
            date = domain.date ?: "",
            updatedAt = LocalDateTime.now()
        )
    }

    fun niceTimetableToModel(niceData: Row, workspaceId: String): Timetable {
        return Timetable(
            workspaceId = workspaceId,
            grade = niceData.grade,
            classNum = niceData.classNm,
            time = niceData.perio,
            subject = niceData.itrtCntnt,
            date = niceData.allTiYmd
        )
    }
}