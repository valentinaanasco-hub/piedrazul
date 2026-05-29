import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Save, Loader2 } from 'lucide-react';
import configurationService from '../../api/configurationService';

/**
 * Página de configuración global del sistema.
 * Permite configurar la ventana de tiempo para agendar citas.
 */
const GlobalConfigurationPage = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [weeks, setWeeks] = useState(4);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    loadConfiguration();
  }, []);

  const loadConfiguration = async () => {
    try {
      setLoading(true);
      const data = await configurationService.getGlobalConfiguration();
      setWeeks(data.appointmentWindowWeeks);
    } catch (err) {
      setError('Error al cargar la configuración');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    try {
      setSaving(true);
      setError('');
      setSuccess('');

      if (weeks < 1 || weeks > 52) {
        setError('La ventana de tiempo debe estar entre 1 y 52 semanas');
        return;
      }

      await configurationService.updateAppointmentWindow(weeks);
      setSuccess('Configuración guardada exitosamente');
      
      setTimeout(() => {
        navigate('/admin/configuration');
      }, 1500);
    } catch (err) {
      setError(err.response?.data?.message || 'Error al guardar la configuración');
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
            <span>Global</span>
          </div>
          <h1 className="text-3xl font-bold text-gray-900">
            Configuración Global del Sistema
          </h1>
          <p className="mt-2 text-gray-600">
            Define la ventana de tiempo en semanas en la que los pacientes pueden agendar
            citas.
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
          <div className="space-y-6">
            <div>
              <label
                htmlFor="weeks"
                className="block text-sm font-medium text-gray-700 mb-2"
              >
                Ventana de tiempo para agendar citas (semanas)
                <span className="text-red-500 ml-1">*</span>
              </label>
              <input
                type="number"
                id="weeks"
                min="1"
                max="52"
                value={weeks}
                onChange={(e) => setWeeks(parseInt(e.target.value) || 1)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="4"
              />
              <p className="mt-2 text-sm text-gray-500">
                Define cuántas semanas hacia adelante se pueden reservar citas.
              </p>
            </div>
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
              disabled={saving}
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
                  <span>Guardar configuración</span>
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default GlobalConfigurationPage;
