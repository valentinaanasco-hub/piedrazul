/**
 * Servicio para calcular festivos en Colombia
 * Incluye festivos fijos y festivos móviles según la Ley Emiliani
 */

/**
 * Traslada un festivo al lunes siguiente si cae entre martes y domingo
 */
function moveToMonday(date) {
  const dayOfWeek = date.getDay()
  if (dayOfWeek === 1) {
    return date
  }
  const daysUntilMonday = dayOfWeek === 0 ? 1 : (8 - dayOfWeek)
  const result = new Date(date)
  result.setDate(result.getDate() + daysUntilMonday)
  return result
}

/**
 * Calcula el Domingo de Pascua usando el algoritmo de Meeus/Jones/Butcher
 */
function calculateEasterSunday(year) {
  const a = year % 19
  const b = Math.floor(year / 100)
  const c = year % 100
  const d = Math.floor(b / 4)
  const e = b % 4
  const f = Math.floor((b + 8) / 25)
  const g = Math.floor((b - f + 1) / 3)
  const h = (19 * a + b - d - g + 15) % 30
  const i = Math.floor(c / 4)
  const k = c % 4
  const l = (32 + 2 * e + 2 * i - h - k) % 7
  const m = Math.floor((a + 11 * h + 22 * l) / 451)
  const month = Math.floor((h + l - 7 * m + 114) / 31)
  const day = ((h + l - 7 * m + 114) % 31) + 1

  return new Date(year, month - 1, day)
}

/**
 * Obtiene todos los festivos de un año específico
 */
export function getHolidaysForYear(year) {
  const holidays = []

  // Festivos fijos que NO se trasladan
  holidays.push(new Date(year, 0, 1))   // Año Nuevo
  holidays.push(new Date(year, 4, 1))   // Día del Trabajo
  holidays.push(new Date(year, 6, 20))  // Independencia
  holidays.push(new Date(year, 7, 7))   // Batalla de Boyacá
  holidays.push(new Date(year, 11, 8))  // Inmaculada Concepción
  holidays.push(new Date(year, 11, 25)) // Navidad

  // Festivos móviles (Ley Emiliani)
  holidays.push(moveToMonday(new Date(year, 0, 6)))   // Reyes Magos
  holidays.push(moveToMonday(new Date(year, 2, 19)))  // San José
  holidays.push(moveToMonday(new Date(year, 5, 29)))  // San Pedro y San Pablo
  holidays.push(moveToMonday(new Date(year, 7, 15)))  // Asunción
  holidays.push(moveToMonday(new Date(year, 9, 12)))  // Día de la Raza
  holidays.push(moveToMonday(new Date(year, 10, 1)))  // Todos los Santos
  holidays.push(moveToMonday(new Date(year, 10, 11))) // Independencia de Cartagena

  // Festivos que dependen de Semana Santa
  const easterSunday = calculateEasterSunday(year)
  
  const jueveSanto = new Date(easterSunday)
  jueveSanto.setDate(easterSunday.getDate() - 3)
  holidays.push(jueveSanto)
  
  const viernesSanto = new Date(easterSunday)
  viernesSanto.setDate(easterSunday.getDate() - 2)
  holidays.push(viernesSanto)
  
  const ascension = new Date(easterSunday)
  ascension.setDate(easterSunday.getDate() + 39)
  holidays.push(moveToMonday(ascension))
  
  const corpusChristi = new Date(easterSunday)
  corpusChristi.setDate(easterSunday.getDate() + 60)
  holidays.push(moveToMonday(corpusChristi))
  
  const sagradoCorazon = new Date(easterSunday)
  sagradoCorazon.setDate(easterSunday.getDate() + 68)
  holidays.push(moveToMonday(sagradoCorazon))

  return holidays
}

/**
 * Verifica si una fecha es festivo en Colombia
 */
export function isHoliday(date) {
  const holidays = getHolidaysForYear(date.getFullYear())
  const dateStr = date.toISOString().split('T')[0]
  
  for (const holiday of holidays) {
    const holidayStr = holiday.toISOString().split('T')[0]
    if (dateStr === holidayStr) {
      return true
    }
  }
  
  return false
}

/**
 * Verifica si una fecha es fin de semana
 */
export function isWeekend(date) {
  const day = date.getDay()
  return day === 0 || day === 6
}
