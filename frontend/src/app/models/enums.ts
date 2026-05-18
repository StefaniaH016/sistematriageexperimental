export enum TipoSolicitud {
  // Trámites de asignaturas
  REGISTRO_ASIGNATURAS = 'REGISTRO_ASIGNATURAS',
  CANCELACION_ASIGNATURAS = 'CANCELACION_ASIGNATURAS',
  SOLICITUD_CUPOS = 'SOLICITUD_CUPOS',
  HOMOLOGACION = 'HOMOLOGACION',
  EXAMEN_SUPLETORIO = 'EXAMEN_SUPLETORIO',
  // Trámites de matrícula y continuidad
  MODIFICACION_MATRICULA = 'MODIFICACION_MATRICULA',
  CANCELACION_SEMESTRE = 'CANCELACION_SEMESTRE',
  RESERVA_CUPO = 'RESERVA_CUPO',
  REINGRESO = 'REINGRESO',
  TRANSFERENCIA_INTERNA = 'TRANSFERENCIA_INTERNA',
  // Trámites documentales
  CERTIFICADO_ESTUDIOS = 'CERTIFICADO_ESTUDIOS',
  // Apoyos y bienestar
  APOYO_ECONOMICO = 'APOYO_ECONOMICO',
  APOYO_PSICOSOCIAL = 'APOYO_PSICOSOCIAL',
  // Procesos académicos especiales
  TRABAJO_GRADO = 'TRABAJO_GRADO',
  PROCESO_DISCIPLINARIO = 'PROCESO_DISCIPLINARIO',
  // General
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
  ADMINISTRATIVO = 'ADMINISTRATIVO',
  RESPONSABLE = 'RESPONSABLE'
}

export const TIPO_SOLICITUD_LABELS: Record<TipoSolicitud, string> = {
  [TipoSolicitud.REGISTRO_ASIGNATURAS]: 'Registro de Asignaturas',
  [TipoSolicitud.CANCELACION_ASIGNATURAS]: 'Cancelación de Asignaturas',
  [TipoSolicitud.SOLICITUD_CUPOS]: 'Solicitud de Cupos',
  [TipoSolicitud.HOMOLOGACION]: 'Homologación y Reconocimiento de Créditos',
  [TipoSolicitud.EXAMEN_SUPLETORIO]: 'Examen Supletorio o Habilitación',
  [TipoSolicitud.MODIFICACION_MATRICULA]: 'Modificación de Matrícula',
  [TipoSolicitud.CANCELACION_SEMESTRE]: 'Cancelación de Semestre',
  [TipoSolicitud.RESERVA_CUPO]: 'Reserva de Cupo',
  [TipoSolicitud.REINGRESO]: 'Reingreso',
  [TipoSolicitud.TRANSFERENCIA_INTERNA]: 'Transferencia Interna de Programa',
  [TipoSolicitud.CERTIFICADO_ESTUDIOS]: 'Certificados y Documentos Académicos',
  [TipoSolicitud.APOYO_ECONOMICO]: 'Apoyo Económico y Becas',
  [TipoSolicitud.APOYO_PSICOSOCIAL]: 'Apoyo Psicosocial',
  [TipoSolicitud.TRABAJO_GRADO]: 'Trabajo de Grado o Práctica Empresarial',
  [TipoSolicitud.PROCESO_DISCIPLINARIO]: 'Proceso Disciplinario / Apelación',
  [TipoSolicitud.CONSULTA_ACADEMICA]: 'Consulta Académica General'
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
  [Rol.ADMINISTRATIVO]: 'Administrativo',
  [Rol.RESPONSABLE]: 'Responsable'
};
