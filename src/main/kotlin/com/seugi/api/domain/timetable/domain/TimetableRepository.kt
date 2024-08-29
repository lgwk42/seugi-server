package com.seugi.api.domain.timetable.domain

import org.springframework.data.repository.CrudRepository

interface TimetableRepository : CrudRepository<TimetableEntity, Long>, TimetableRepositoryCustom {
    override fun checkTimetableByWorkspaceId(workspaceId: String): Boolean
    override fun findByWorkspaceId(workspaceId: String): List<TimetableEntity>
}