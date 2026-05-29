import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Globe, Users } from 'lucide-react';

/**
 * Página principal de configuración del sistema.
 * Permite elegir entre configuración global o por profesional.
 */
const ConfigurationPage = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex items-center space-x-2 text-sm text-gray-500 mb-2">
            <span>Administración</span>
            <span>/</span>
            <span>Configuración</span>
          </div>
          <h1 className="text-3xl font-bold text-gray-900">
            Configuración del Sistema
          </h1>
          <p className="mt-2 text-gray-600">
            Seleccione el tipo de configuración que desea administrar.
          </p>
        </div>
      </div>

      {/* Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Configuración Global */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-8 hover:shadow-md transition-shadow">
            <div className="flex justify-center mb-6">
              <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center">
                <Globe className="w-8 h-8 text-blue-600" />
              </div>
            </div>
            <h2 className="text-2xl font-semibold text-gray-900 text-center mb-4">
              Configuración Global
            </h2>
            <p className="text-gray-600 text-center mb-8">
              Permite definir parámetros generales del sistema como la ventana de tiempo en
              semanas para agendar citas.
            </p>
            <button
              onClick={() => navigate('/admin/configuration/global')}
              className="w-full bg-blue-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-blue-700 transition-colors flex items-center justify-center space-x-2"
            >
              <span>Configurar</span>
              <svg
                className="w-5 h-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 5l7 7-7 7"
                />
              </svg>
            </button>
          </div>

          {/* Configuración por Profesional */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-8 hover:shadow-md transition-shadow">
            <div className="flex justify-center mb-6">
              <div className="w-16 h-16 bg-purple-100 rounded-full flex items-center justify-center">
                <Users className="w-8 h-8 text-purple-600" />
              </div>
            </div>
            <h2 className="text-2xl font-semibold text-gray-900 text-center mb-4">
              Configuración por Profesional
            </h2>
            <p className="text-gray-600 text-center mb-8">
              Permite definir días de atención, franja horaria y el intervalo entre citas
              para cada profesional.
            </p>
            <button
              onClick={() => navigate('/admin/configuration/professional')}
              className="w-full bg-blue-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-blue-700 transition-colors flex items-center justify-center space-x-2"
            >
              <span>Configurar</span>
              <svg
                className="w-5 h-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 5l7 7-7 7"
                />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ConfigurationPage;
