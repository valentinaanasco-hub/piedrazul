import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Save, Loader2, Clock } from 'lucide-react';
import configurationService from '../../api/configurationService';

/**
 * Página de configuración de disponibilidad del profesional.
 * Permite configurar días de atención, franja horaria e intervalo entre citas.
 */
const ProfessionalConfigurationPage = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [doctors, setDoctors] = useState([]);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Estado del formulario
  const [selectedDays, setSelectedDays] = useState({
    1: false, // Lunes
    2: false, // Martes
    3: false, // Miércoles
    4: false, // Jueves
    5: false, // Viernes
    6: false, // Sábado
    7: false, // Domingo
  });
  const [startTime, setStartTime] = useState('08:00');
  const [endTime, setEndTime] = useState('18:00');
  const [intervalMinutes, setIntervalMinutes] = useState(30);

  const dayNames = {
    1: 'Lunes',
    2: 'Martes',
    3: 'Miércoles',
    4: 'Jueves',
    5: 'Viernes',
    6: 'Sábado',
    7: 'Domingo',
  };

  useEffect(() => {
    loadDoctors();
  }, []);

  useEffect(() => {
    if (selectedDoctor) {
      loadDoctorSchedule();
    }
  }, [selectedDoctor]);

  const loadDoctors = async () => {
    try {
      setLoading(true);
      const data = await configurationService.listAllDoctors();
      setDoctors(data);
    } catch (err) {
      setError('Error al cargar la lista de profesionales');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const loadDoctorSchedule = async () => {
    try {
      const schedules = await configurationService.getDoctorSchedule(selectedDoctor);
      
      if (schedules.length > 0) {
        // Cargar días seleccionados
        const days = {};
        schedules.forEach((schedule) => {
          days[schedule.dayOfWeek] = true;
        });
        setSelectedDays(days);

        // Usar el primer horario como referencia para hora inicio/fin e intervalo
        setStartTime(schedules[0].startTime);
        setEndTime(schedules[0].endTime);
        setIntervalMinutes(schedules[0].intervalMinutes);
      } else {
        // Resetear formulario si no hay horarios
        setSelectedDays({
          1: false,
          2: false,
          3: false,
          4: false,
          5: false,
          6: false,
          7: false,
        });
        setStartTime('08:00');
        setEndTime('18:00');
        setIntervalMinutes(30);
      }
    } catch (err) {
      // Si no hay horarios configurados, no es un error
      console.log('No hay horarios configurados para este profesional');
    }
  };

  const handleDayToggle = (day) => {
    setSelectedDays((prev) => ({
      ...prev,
      [day]: !prev[day],
    }));
  };

  const handleSave = async () => {
    try {
      setSaving(true);
      setError('');
      setSuccess('');

      if (!selectedDoctor) {
        setError('Debe seleccionar un profesional');
        return;
      }

      // Validar que al menos un día esté seleccionado
      const hasSelectedDays = Object.values(selectedDays).some((selected) => selected);
      if (!hasSelectedDays) {
        setError('Debe seleccionar al menos un día de atención');
        return;
      }

      // Validar horarios
      if (startTime >= endTime) {
        setError('La hora de inicio debe ser anterior a la hora de fin');
        return;
      }

      if (intervalMinutes < 1) {
        setError('El intervalo entre citas debe ser al menos 1 minuto');
        return;
      }

      // Construir array de horarios
      const schedules = Object.entries(selectedDays)
        .filter(([_, selected]) => selected)
        .map(([day, _]) => ({
          dayOfWeek: parseInt(day),
          startTime,
          endTime,
          intervalMinutes,
        }));

      await configurationService.updateDoctorSchedule(selectedDoctor, schedules);
      setSuccess('Disponibilidad guardada exitosamente');

      setTimeout(() => {
        navigate('/admin/configuration');
      }, 1500);
    } catch (err) {
      setError(err.response?.data?.message || 'Error al guardar la disponibilidad');
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    navigate('/admin/configuration');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <button
            onClick={() => navigate('/admin/configuration')}
            className="flex items-center space-x-2 text-gray-600 hover:text-gray-900 mb-4"
          >
            <ArrowLeft className="w-5 h-5" />
            <span>Volver</span>
          </button>
          <div className="flex items-center space-x-2 text-sm text-gray-500 mb-2">
            <span>Administración</span>
            <span>/</span>
            <span>Configuración</span>
            <span>/</span>
            <span>Profesional</span>
          </div>
          <h1 className="text-3xl font-bold text-gray-900">
            Configuración de Disponibilidad del Profesional
          </h1>
          <p className="mt-2 text-gray-600">
            Define los días, horarios y duración de las citas para cada profesional.
          </p>
        </div>
      </div>

      {/* Content */}
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-8">
          {/* Alerts */}
          {error && (
            <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
              {error}
            </div>
          )}
          {success && (
            <div className="mb-6 bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg">
              {success}
            </div>
          )}

          {/* Form */}
          <div className="space-y-8">
            {/* Selección de Profesional */}
            <div>
              <label
                htmlFor="doctor"
                className="block text-sm font-medium text-gray-700 mb-2"
              >
                Profesional
                <span className="text-red-500 ml-1">*</span>
              </label>
              <select
                id="doctor"
                value={selectedDoctor || ''}
                onChange={(e) => setSelectedDoctor(parseInt(e.target.value) || null)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">Seleccione un profesional...</option>
                {doctors.map((doctor) => (
                  <option key={doctor.id} value={doctor.id}>
                    {doctor.firstName} {doctor.firstSurname} - {doctor.licenseNumber}
                  </option>
                ))}
              </select>
            </div>

            {selectedDoctor && (
              <>
                {/* Días de atención */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-3">
                    Días de atención
                  </label>
                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                    {Object.entries(dayNames).map(([day, name]) => (
                      <label
                        key={day}
                        className="flex items-center space-x-2 cursor-pointer"
                      >
                        <input
                          type="checkbox"
                          checked={selectedDays[parseInt(day)]}
                          onChange={() => handleDayToggle(parseInt(day))}
                          className="w-5 h-5 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                        />
                        <span className="text-gray-700">{name}</span>
                      </label>
                    ))}
                  </div>
                </div>

                {/* Franja horaria */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-3">
                    Franja horaria de atención
                  </label>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                      <label
                        htmlFor="startTime"
                        className="block text-sm text-gray-600 mb-2"
                      >
                        Hora de inicio
                        <span className="text-red-500 ml-1">*</span>
                      </label>
                      <input
                        type="time"
                        id="startTime"
                        value={startTime}
                        onChange={(e) => setStartTime(e.target.value)}
                        className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      />
                    </div>
                    <div>
                      <label htmlFor="endTime" className="block text-sm text-gray-600 mb-2">
                        Hora de fin
                        <span className="text-red-500 ml-1">*</span>
                      </label>
                      <input
                        type="time"
                        id="endTime"
                        value={endTime}
                        onChange={(e) => setEndTime(e.target.value)}
                        className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      />
                    </div>
                  </div>
                </div>

                {/* Intervalo entre citas */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-3">
                    Intervalo entre citas
                  </label>
                  <div>
                    <label
                      htmlFor="interval"
                      className="block text-sm text-gray-600 mb-2"
                    >
                      Intervalo entre citas (minutos)
                      <span className="text-red-500 ml-1">*</span>
                    </label>
                    <input
                      type="number"
                      id="interval"
                      min="1"
                      value={intervalMinutes}
                      onChange={(e) => setIntervalMinutes(parseInt(e.target.value) || 1)}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="30"
                    />
                  </div>
                </div>
              </>
            )}
          </div>

          {/* Actions */}
          <div className="mt-8 flex justify-end space-x-4">
            <button
              onClick={handleCancel}
              disabled={saving}
              className="px-6 py-3 border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50 transition-colors disabled:opacity-50"
            >
              Cancelar
            </button>
            <button
              onClick={handleSave}
              disabled={saving || !selectedDoctor}
              className="px-6 py-3 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 transition-colors disabled:opacity-50 flex items-center space-x-2"
            >
              {saving ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  <span>Guardando...</span>
                </>
              ) : (
                <>
                  <Save className="w-5 h-5" />
                  <span>Guardar disponibilidad</span>
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProfessionalConfigurationPage;
