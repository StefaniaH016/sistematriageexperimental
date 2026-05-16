export enum TipoSolicitud {
  REGISTRO_ASIGNATURAS = 'REGISTRO_ASIGNATURAS',
  HOMOLOGACION = 'HOMOLOGACION',
  CANCELACION_ASIGNATURAS = 'CANCELACION_ASIGNATURAS',
  SOLICITUD_CUPOS = 'SOLICITUD_CUPOS',
  EXAMEN_SUPLETORIO = 'EXAMEN_SUPLETORIO',
  CERTIFICADO_ESTUDIOS = 'CERTIFICADO_ESTUDIOS',
  MODIFICACION_MATRICULA = 'MODIFICACION_MATRICULA',
  RESERVA_CUPO = 'RESERVA_CUPO',
  REINGRESO = 'REINGRESO',
  CONSULTA_ACADEMICA = 'CONSULTA_ACADEMICA'
}

export enum EstadoSolicitud {
  REGISTRADA = 'REGISTRADA',
  CLASIFICADA = 'CLASIFICADA',
  EN_ATENCION = 'EN_ATENCION',
  ATENDIDA = 'ATENDIDA',
  CERRADA = 'CERRADA'
}

export enum Prioridad {
  BAJA = 'BAJA',
  MEDIA = 'MEDIA',
  ALTA = 'ALTA',
  CRITICA = 'CRITICA'
}

export enum CanalOrigen {
  CSU = 'CSU',
  CORREO = 'CORREO',
  SAC = 'SAC',
  TELEFONICO = 'TELEFONICO',
  PRESENCIAL = 'PRESENCIAL'
}

export enum Rol {
  ESTUDIANTE = 'ESTUDIANTE',
  DOCENTE = 'DOCENTE',
  ADMINISTRATIVO = 'ADMINISTRATIVO',
  RESPONSABLE = 'RESPONSABLE'
}

export const TIPO_SOLICITUD_LABELS: Record<TipoSolicitud, string> = {
  [TipoSolicitud.REGISTRO_ASIGNATURAS]: 'Registro de Asignaturas',
  [TipoSolicitud.HOMOLOGACION]: 'Homologación',
  [TipoSolicitud.CANCELACION_ASIGNATURAS]: 'Cancelación de Asignaturas',
  [TipoSolicitud.SOLICITUD_CUPOS]: 'Solicitud de Cupos',
  [TipoSolicitud.EXAMEN_SUPLETORIO]: 'Examen Supletorio',
  [TipoSolicitud.CERTIFICADO_ESTUDIOS]: 'Certificado de Estudios',
  [TipoSolicitud.MODIFICACION_MATRICULA]: 'Modificación de Matrícula',
  [TipoSolicitud.RESERVA_CUPO]: 'Reserva de Cupo',
  [TipoSolicitud.REINGRESO]: 'Reingreso',
  [TipoSolicitud.CONSULTA_ACADEMICA]: 'Consulta Académica'
};

export const ESTADO_LABELS: Record<EstadoSolicitud, string> = {
  [EstadoSolicitud.REGISTRADA]: 'Registrada',
  [EstadoSolicitud.CLASIFICADA]: 'Clasificada',
  [EstadoSolicitud.EN_ATENCION]: 'En Atención',
  [EstadoSolicitud.ATENDIDA]: 'Atendida',
  [EstadoSolicitud.CERRADA]: 'Cerrada'
};

export const PRIORIDAD_LABELS: Record<Prioridad, string> = {
  [Prioridad.BAJA]: 'Baja',
  [Prioridad.MEDIA]: 'Media',
  [Prioridad.ALTA]: 'Alta',
  [Prioridad.CRITICA]: 'Crítica'
};

export const CANAL_LABELS: Record<CanalOrigen, string> = {
  [CanalOrigen.CSU]: 'CSU',
  [CanalOrigen.CORREO]: 'Correo',
  [CanalOrigen.SAC]: 'SAC',
  [CanalOrigen.TELEFONICO]: 'Telefónico',
  [CanalOrigen.PRESENCIAL]: 'Presencial'
};

export const ROL_LABELS: Record<Rol, string> = {
  [Rol.ESTUDIANTE]: 'Estudiante',
  [Rol.DOCENTE]: 'Docente',
  [Rol.ADMINISTRATIVO]: 'Administrativo',
  [Rol.RESPONSABLE]: 'Responsable'
};
