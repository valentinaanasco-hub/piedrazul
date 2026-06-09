package co.unicauca.piedrazul.appointment.domain.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.Set;

/**
 * Servicio de dominio para calcular festivos en Colombia.
 * Incluye festivos fijos y festivos móviles según la Ley Emiliani.
 * Clase pura de dominio — sin dependencias de frameworks.
 */
public class ColombianHolidaysService {

    public boolean isHoliday(LocalDate date) {
        return getHolidaysForYear(date.getYear()).contains(date);
    }

    public Set<LocalDate> getHolidaysForYear(int year) {
        Set<LocalDate> holidays = new HashSet<>();

        // Festivos fijos que NO se trasladan
        holidays.add(LocalDate.of(year, Month.JANUARY,  1));
        holidays.add(LocalDate.of(year, Month.MAY,      1));
        holidays.add(LocalDate.of(year, Month.JULY,    20));
        holidays.add(LocalDate.of(year, Month.AUGUST,   7));
        holidays.add(LocalDate.of(year, Month.DECEMBER, 8));
        holidays.add(LocalDate.of(year, Month.DECEMBER,25));

        // Festivos móviles (Ley Emiliani)
        holidays.add(moveToMonday(LocalDate.of(year, Month.JANUARY,   6)));
        holidays.add(moveToMonday(LocalDate.of(year, Month.MARCH,    19)));
        holidays.add(moveToMonday(LocalDate.of(year, Month.JUNE,     29)));
        holidays.add(moveToMonday(LocalDate.of(year, Month.AUGUST,   15)));
        holidays.add(moveToMonday(LocalDate.of(year, Month.OCTOBER,  12)));
        holidays.add(moveToMonday(LocalDate.of(year, Month.NOVEMBER,  1)));
        holidays.add(moveToMonday(LocalDate.of(year, Month.NOVEMBER, 11)));

        // Festivos basados en Semana Santa
        LocalDate easter = calculateEasterSunday(year);
        holidays.add(easter.minusDays(3));
        holidays.add(easter.minusDays(2));
        holidays.add(moveToMonday(easter.plusDays(39)));
        holidays.add(moveToMonday(easter.plusDays(60)));
        holidays.add(moveToMonday(easter.plusDays(68)));

        return holidays;
    }

    private LocalDate moveToMonday(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.MONDAY) {
            return date;
        }
        int daysUntilMonday = (8 - dayOfWeek.getValue()) % 7;
        if (daysUntilMonday == 0) {
            daysUntilMonday = 7;
        }
        return date.plusDays(daysUntilMonday);
    }

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
        int day   = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }
}
