package co.unicauca.piedrazul.appointment.domain.service;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

/**
 * Servicio para calcular festivos en Colombia
 * Incluye festivos fijos y festivos móviles según la Ley Emiliani
 */
@Service
public class ColombianHolidaysService {

    /**
     * Verifica si una fecha es festivo en Colombia
     */
    public boolean isHoliday(LocalDate date) {
        Set<LocalDate> holidays = getHolidaysForYear(date.getYear());
        return holidays.contains(date);
    }

    /**
     * Obtiene todos los festivos de un año específico
     */
    public Set<LocalDate> getHolidaysForYear(int year) {
        Set<LocalDate> holidays = new HashSet<>();

        // Festivos fijos que NO se trasladan
        holidays.add(LocalDate.of(year, Month.JANUARY, 1));    // Año Nuevo
        holidays.add(LocalDate.of(year, Month.MAY, 1));        // Día del Trabajo
        holidays.add(LocalDate.of(year, Month.JULY, 20));      // Independencia
        holidays.add(LocalDate.of(year, Month.AUGUST, 7));     // Batalla de Boyacá
        holidays.add(LocalDate.of(year, Month.DECEMBER, 8));   // Inmaculada Concepción
        holidays.add(LocalDate.of(year, Month.DECEMBER, 25));  // Navidad

        // Festivos móviles (Ley Emiliani - se trasladan al lunes siguiente)
        holidays.add(moveToMonday(LocalDate.of(year, Month.JANUARY, 6)));    // Reyes Magos
        holidays.add(moveToMonday(LocalDate.of(year, Month.MARCH, 19)));     // San José
        holidays.add(moveToMonday(LocalDate.of(year, Month.JUNE, 29)));      // San Pedro y San Pablo
        holidays.add(moveToMonday(LocalDate.of(year, Month.AUGUST, 15)));    // Asunción
        holidays.add(moveToMonday(LocalDate.of(year, Month.OCTOBER, 12)));   // Día de la Raza
        holidays.add(moveToMonday(LocalDate.of(year, Month.NOVEMBER, 1)));   // Todos los Santos
        holidays.add(moveToMonday(LocalDate.of(year, Month.NOVEMBER, 11)));  // Independencia de Cartagena

        // Festivos que dependen de Semana Santa
        LocalDate easterSunday = calculateEasterSunday(year);
        holidays.add(easterSunday.minusDays(3));  // Jueves Santo
        holidays.add(easterSunday.minusDays(2));  // Viernes Santo
        holidays.add(moveToMonday(easterSunday.plusDays(39)));  // Ascensión (39 días después)
        holidays.add(moveToMonday(easterSunday.plusDays(60)));  // Corpus Christi (60 días después)
        holidays.add(moveToMonday(easterSunday.plusDays(68)));  // Sagrado Corazón (68 días después)

        return holidays;
    }

    /**
     * Traslada un festivo al lunes siguiente si cae entre martes y domingo
     * Ley Emiliani: festivos que no caen en lunes se trasladan al lunes siguiente
     */
    private LocalDate moveToMonday(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.MONDAY) {
            return date;
        }
        // Calcular días hasta el próximo lunes
        int daysUntilMonday = (8 - dayOfWeek.getValue()) % 7;
        if (daysUntilMonday == 0) {
            daysUntilMonday = 7;
        }
        return date.plusDays(daysUntilMonday);
    }

    /**
     * Calcula el Domingo de Pascua usando el algoritmo de Meeus/Jones/Butcher
     * Este algoritmo es válido para años 1583-4099
     */
    private LocalDate calculateEasterSunday(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;

        return LocalDate.of(year, month, day);
    }

    /**
     * Obtiene el próximo día hábil (no festivo ni fin de semana)
     */
    public LocalDate getNextBusinessDay(LocalDate date) {
        LocalDate next = date.plusDays(1);
        while (isHoliday(next) || isWeekend(next)) {
            next = next.plusDays(1);
        }
        return next;
    }

    /**
     * Verifica si una fecha es fin de semana
     */
    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
