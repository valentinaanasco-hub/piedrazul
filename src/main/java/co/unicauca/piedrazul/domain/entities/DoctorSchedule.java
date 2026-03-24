/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.domain.entities;

import java.time.LocalTime;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */

public class DoctorSchedule {
    private int scheduleId;
    private int dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private int intervalMinutes;
    
    // Constructor vacío (necesario para mapeo desde repositorio)
    public DoctorSchedule() {}

    public int getScheduleId() { 
        return scheduleId; 
    }
    public void setScheduleId(int scheduleId) { 
        this.scheduleId = scheduleId; 
    }
    public int getDayOfWeek() { 
        return dayOfWeek; 
    }
    public void setDayOfWeek(int dayOfWeek) { 
        this.dayOfWeek = dayOfWeek; 
    }
    public LocalTime getStartTime() { 
        return startTime; 
    }
    public void setStartTime(LocalTime startTime) { 
        this.startTime = startTime; 
    }
    public LocalTime getEndTime() { 
        return endTime; 
    }
    public void setEndTime(LocalTime endTime) { 
        this.endTime = endTime; 
    }
    public int getIntervalMinutes() { 
        return intervalMinutes; 
    }
    public void setIntervalMinutes(int intervalMinutes) { 
        this.intervalMinutes = intervalMinutes;
    }
}
