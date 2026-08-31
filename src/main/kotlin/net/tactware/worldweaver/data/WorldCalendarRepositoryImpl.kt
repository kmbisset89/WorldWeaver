package net.tactware.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.tactware.worldweaver.domain.WorldCalendar
import net.tactware.worldweaver.domain.WorldCalendarRepository

internal class WorldCalendarRepositoryImpl(
    private val calendarDao: WorldCalendarDao,
    private val monthDao: WorldCalendarMonthDao,
    private val weekdayDao: WorldCalendarWeekdayDao,
    private val converter: WorldCalendarEntityConverter,
) : WorldCalendarRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeByWorld(worldId: String): Flow<WorldCalendar?> {
        return calendarDao.observeByWorld(worldId).flatMapLatest { entity ->
            if (entity == null) {
                flowOf(null)
            } else {
                combine(
                    monthDao.observeByCalendar(entity.id),
                    weekdayDao.observeByCalendar(entity.id),
                ) { months, weekdays ->
                    converter.toCalendar(
                        entity,
                        converter.toMonths(months),
                        converter.toWeekdays(weekdays),
                    )
                }
            }
        }
    }

    override suspend fun getByWorld(worldId: String): WorldCalendar? {
        val entity = calendarDao.getByWorld(worldId) ?: return null
        return assemble(entity)
    }

    override suspend fun getById(id: String): WorldCalendar? {
        val entity = calendarDao.getById(id) ?: return null
        return assemble(entity)
    }

    override suspend fun insert(calendar: WorldCalendar) {
        calendarDao.insert(converter.toEntity(calendar))
        replaceChildren(calendar)
    }

    override suspend fun update(calendar: WorldCalendar) {
        calendarDao.update(converter.toEntity(calendar))
        replaceChildren(calendar)
    }

    private suspend fun replaceChildren(calendar: WorldCalendar) {
        monthDao.deleteByCalendar(calendar.id)
        weekdayDao.deleteByCalendar(calendar.id)
        val months = converter.toMonthEntities(calendar)
        if (months.isNotEmpty()) {
            monthDao.insertAll(months)
        }
        val weekdays = converter.toWeekdayEntities(calendar)
        if (weekdays.isNotEmpty()) {
            weekdayDao.insertAll(weekdays)
        }
    }

    private suspend fun assemble(entity: WorldCalendarEntity): WorldCalendar {
        return converter.toCalendar(
            entity,
            converter.toMonths(monthDao.getByCalendar(entity.id)),
            converter.toWeekdays(weekdayDao.getByCalendar(entity.id)),
        )
    }
}
