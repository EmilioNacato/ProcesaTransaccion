package com.banquito.paymentprocessor.procesatransaccion.banquito.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import com.banquito.paymentprocessor.procesatransaccion.banquito.client.BancoClient;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.FraudeClient;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.MarcaClient;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ProcesoBancarioRequest;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ProcesoBancarioResponse;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionFraudeRequest;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionFraudeResponse;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionMarcaRequest;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionMarcaResponse;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.HistorialEstadoTransaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.repository.TransaccionRepository;
import com.banquito.paymentprocessor.procesatransaccion.banquito.repository.HistorialEstadoTransaccionRepository;
import com.banquito.paymentprocessor.procesatransaccion.banquito.exception.NotFoundException;
import com.banquito.paymentprocessor.procesatransaccion.banquito.exception.TransaccionRechazadaException;
import com.banquito.paymentprocessor.procesatransaccion.banquito.context.TransaccionContextHolder;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final HistorialEstadoTransaccionRepository historialRepository;
    private final MarcaClient marcaClient;
    private final FraudeClient fraudeClient;
    private final BancoClient bancoClient;
    private final RedisService redisService;
    private final GatewayService gatewayService;
    
    // Estados de transacción
    public static final String ESTADO_PENDIENTE = "PEN";
    public static final String ESTADO_VALIDACION_MARCA = "VMA";
    public static final String ESTADO_VALIDACION_FRAUDE = "VFR";
    public static final String ESTADO_PROCESO_DEBITO = "DEB";
    public static final String ESTADO_PROCESO_CREDITO = "CRE";
    public static final String ESTADO_COMPLETADA = "COM";
    public static final String ESTADO_RECHAZADA = "REC";
    public static final String ESTADO_ERROR = "ERR";
    public static final String ESTADO_FRAUDE = "FRA";
    public static final String ESTADO_PROCESO_REVERSO = "REV";
    public static final String ESTADO_REVERSADA = "REV_COM";
    public static final String ESTADO_ERROR_IRRECUPERABLE = "ERR_IRRECUPERABLE";

    public Transaccion procesarTransaccion(Transaccion transaccion) {
        try {
            log.info("Iniciando procesamiento de transacción: {}", 
                    transaccion.getCodTransaccion());
            
            // Obtener el codigoGtw para validación
            String codigoGtw = transaccion.getCodigoGtw();
            
            // Verificar si el código del gateway es válido
            if (codigoGtw == null || codigoGtw.trim().isEmpty()) {
                log.error("Se recibió una transacción sin código de gateway");
                throw new TransaccionRechazadaException("Código de gateway no proporcionado");
            }
            
            // Verificar que el gateway exista en nuestra base de datos
            boolean gatewayValido = gatewayService.verificarCodigoGateway(codigoGtw);
            if (!gatewayValido) {
                log.error("Se recibió una transacción con código de gateway inválido: {}", codigoGtw);
                throw new TransaccionRechazadaException("Código de gateway no válido o no autorizado: " + codigoGtw);
            }
            
            log.info("Gateway validado correctamente: {}", codigoGtw);
            
            // Verificar que el código único de transacción no exista previamente
            if (transaccion.getCodigoUnico() != null && !transaccion.getCodigoUnico().isEmpty()) {
                // Buscar si ya existe una transacción con ese código único
                try {
                    List<Transaccion> transaccionesExistentes = transaccionRepository.findByCodigoUnico(transaccion.getCodigoUnico());
                    if (!transaccionesExistentes.isEmpty()) {
                        log.error("Ya existe una transacción con el código único: {}", transaccion.getCodigoUnico());
                        throw new TransaccionRechazadaException("Código único de transacción duplicado: " + transaccion.getCodigoUnico());
                    }
                    log.info("Verificación de código único de transacción exitosa: {}", transaccion.getCodigoUnico());
                } catch (Exception e) {
                    if (!(e instanceof TransaccionRechazadaException)) {
                        log.error("Error al verificar unicidad del código de transacción: {}", e.getMessage(), e);
                        throw new RuntimeException("Error al verificar unicidad del código de transacción: " + e.getMessage(), e);
                    } else {
                        throw e;
                    }
                }
            }
            
            // Inicializar la transacción con estado pendiente
            inicializarTransaccion(transaccion);
            
            // Paso 2: Validar tarjeta con marca
            try {
                validarMarca(transaccion);
                
                // Verificar si la tarjeta fue rechazada y detener el procesamiento
                if (ESTADO_RECHAZADA.equals(transaccion.getEstado())) {
                    log.info("Transacción rechazada en validación de marca: {}", transaccion.getCodTransaccion());
                    return transaccion;
                }
            } catch (TransaccionRechazadaException e) {
                log.warn("Transacción rechazada en validación de marca: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("Error en validación con marca: {}", e.getMessage(), e);
                actualizarEstadoTransaccion(transaccion, ESTADO_ERROR, 
                        "Error en validación con marca: " + e.getMessage());
                throw new TransaccionRechazadaException("Error en validación con marca: " + e.getMessage());
            }
            
            // Validar con el sistema de fraude
            validarFraude(transaccion);
            
            // Procesar débito a la tarjeta
            procesarDebitoTarjeta(transaccion);
            
            // Procesar crédito al comercio
            procesarCreditoComercio(transaccion);
            
            // Transacción completada exitosamente
            actualizarEstadoTransaccion(transaccion, ESTADO_COMPLETADA, 
                    "Transacción procesada exitosamente");
            
            log.info("Transacción procesada exitosamente: {}", transaccion.getCodTransaccion());
            return transaccion;
            
        } catch (TransaccionRechazadaException e) {
            // Las excepciones de rechazo ya tienen el estado actualizado
            log.warn("Transacción rechazada: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado procesando transacción: {}", e.getMessage(), e);
            try {
            actualizarEstadoTransaccion(transaccion, ESTADO_ERROR, 
                        "Error inesperado: " + e.getMessage());
            } catch (Exception ex) {
                log.error("No se pudo actualizar estado de error en transacción: {}", ex.getMessage());
                // Si falló la actualización del estado, intentamos eliminar la transacción de Redis directamente
                try {
                    if (transaccion.getId() != null) {
                        redisService.deleteTransaccion(transaccion.getId());
                    }
                    if (transaccion.getCodTransaccion() != null) {
                        redisService.deleteTransaccionByCodigo(transaccion.getCodTransaccion());
                    }
                    log.info("Transacción eliminada de Redis por error crítico");
                } catch (Exception deleteEx) {
                    log.error("No se pudo eliminar la transacción de Redis: {}", deleteEx.getMessage());
                }
            }
            throw new RuntimeException("Error procesando transacción: " + e.getMessage(), e);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void inicializarTransaccion(Transaccion transaccion) {
        log.debug("Inicializando transacción");
        
        try {
        transaccion.setFechaTransaccion(LocalDateTime.now());
        transaccion.setEstado(ESTADO_PENDIENTE);
        
        if (transaccion.getCodTransaccion() == null || transaccion.getCodTransaccion().isEmpty()) {
            transaccion.setCodTransaccion(UUID.randomUUID().toString().substring(0, 10));
        }
        
            // Guardado en la base de datos primero
            try {
        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);
        transaccion.setId(transaccionGuardada.getId());
                log.info("Transacción guardada en PostgreSQL con ID: {}", transaccion.getId());
                
                // SOLO si se guarda exitosamente en PostgreSQL, intentamos guardar en Redis
                try {
                    redisService.saveTransaccion(transaccion);
                    log.info("Transacción guardada en Redis con código: {}", transaccion.getCodTransaccion());
                } catch (Exception ex) {
                    log.error("Error al guardar transacción en Redis (continuando proceso): {}", ex.getMessage(), ex);
                }
                
                // SOLO si se guarda exitosamente en PostgreSQL, registramos el evento inicial
                try {
        registrarHistorialEstado(transaccion, ESTADO_PENDIENTE, "Transacción recibida del gateway");
                    log.debug("Historial de estado inicial registrado");
                } catch (Exception ex) {
                    log.error("Error al registrar historial de estado inicial: {}", ex.getMessage(), ex);
                }
        
        log.info("Transacción inicializada con código: {}", transaccion.getCodTransaccion());
            } catch (Exception ex) {
                log.error("Error crítico al guardar transacción en PostgreSQL: {}", ex.getMessage(), ex);
                throw new RuntimeException("Error al guardar transacción en base de datos: no se guardará en Redis ni en historial", ex);
            }
        } catch (Exception e) {
            log.error("Error en inicialización de transacción: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private void validarFraude(Transaccion transaccion) {
        log.debug("Validando fraude para transacción: {}", transaccion.getCodTransaccion());
        actualizarEstadoTransaccion(transaccion, ESTADO_VALIDACION_FRAUDE, 
                "Iniciando validación de fraude");
        
        // Validar datos necesarios antes de enviar
        if (transaccion.getNumeroTarjeta() == null || transaccion.getNumeroTarjeta().trim().isEmpty()) {
            String mensaje = "Número de tarjeta no proporcionado";
            log.error(mensaje);
            actualizarEstadoTransaccion(transaccion, ESTADO_ERROR, mensaje);
            throw new TransaccionRechazadaException(mensaje);
        }
        
        if (transaccion.getMonto() == null || transaccion.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            String mensaje = "Monto no válido";
            log.error(mensaje);
            actualizarEstadoTransaccion(transaccion, ESTADO_ERROR, mensaje);
            throw new TransaccionRechazadaException(mensaje);
        }
        
        // Registro seguro de información (ocultar datos sensibles)
        if (transaccion.getNumeroTarjeta().length() >= 4) {
            log.info("Datos de transacción para validar fraude: numeroTarjeta={}****, monto={}, codigoUnico={}",
                transaccion.getNumeroTarjeta().substring(0, 4),
                transaccion.getMonto(),
                transaccion.getCodigoUnico());
        }
        
        ValidacionFraudeRequest fraudeRequest = new ValidacionFraudeRequest();
        fraudeRequest.setNumeroTarjeta(transaccion.getNumeroTarjeta());
        fraudeRequest.setMonto(transaccion.getMonto());
        fraudeRequest.setCodigoUnico(transaccion.getCodigoUnico());
        
        // Determinar y establecer el tipo de transacción
        String tipoTransaccion = "COMPRA"; // Valor por defecto
        if (transaccion.getTipo() != null) {
            // Mapeo de tipos de transacción según la nomenclatura esperada por el servicio de fraude
            switch (transaccion.getTipo()) {
                case "PAG":
                    tipoTransaccion = "PAGO";
                    break;
                case "TRA":
                    tipoTransaccion = "TRANSFERENCIA";
                    break;
                case "RET":
                    tipoTransaccion = "RETIRO";
                    break;
                default:
                    tipoTransaccion = "COMPRA";
            }
        }
        fraudeRequest.setTipoTransaccion(tipoTransaccion);
        
        // Establecer el código de comercio (si está disponible)
        String codigoComercio = "COM123"; // Valor por defecto
        if (transaccion.getSwiftBancoComercio() != null && !transaccion.getSwiftBancoComercio().isEmpty()) {
            codigoComercio = transaccion.getSwiftBancoComercio();
        }
        fraudeRequest.setCodigoComercio(codigoComercio);
        
        log.debug("Enviando solicitud de validación de fraude: {}", fraudeRequest);
        
        try {
            log.debug("Llamando al servicio de fraude en URL: ${app.fraude-service.url}/api/v1/fraude/validar");
            
            ValidacionFraudeResponse fraudeResponse = fraudeClient.validarTransaccion(fraudeRequest);
            
            if (fraudeResponse == null) {
                String mensaje = "Respuesta de validación de fraude es null";
                log.error(mensaje);
                actualizarEstadoTransaccion(transaccion, ESTADO_ERROR, mensaje);
                throw new TransaccionRechazadaException(mensaje);
            }
            
            log.debug("Respuesta del servicio de fraude: {}", fraudeResponse);
            
            // Verificar si la transacción es fraudulenta
            if (fraudeResponse.getEsFraude() != null && fraudeResponse.getEsFraude()) {
                String detalleRazon = "";
                if (fraudeResponse.getCodigoRegla() != null && !fraudeResponse.getCodigoRegla().isEmpty()) {
                    detalleRazon += " Regla: " + fraudeResponse.getCodigoRegla();
                }
                
                if (fraudeResponse.getNivelRiesgo() != null) {
                    detalleRazon += " Nivel de riesgo: " + fraudeResponse.getNivelRiesgo();
                }
                
                String mensaje = "Posible fraude detectado." + detalleRazon;
                if (fraudeResponse.getMensaje() != null && !fraudeResponse.getMensaje().isEmpty()) {
                    mensaje += " Detalle: " + fraudeResponse.getMensaje();
                }
                
                log.warn(mensaje);
                actualizarEstadoTransaccion(transaccion, ESTADO_FRAUDE, mensaje);
                throw new TransaccionRechazadaException("Transacción rechazada por posible fraude: " + 
                        (fraudeResponse.getMensaje() != null ? fraudeResponse.getMensaje() : "Validación de fraude"));
            }
            
            log.info("Validación de fraude exitosa para transacción: {}", 
                    transaccion.getCodTransaccion());
            
        } catch (Exception e) {
            log.error("Error en validación de fraude: {}", e.getMessage(), e);
            actualizarEstadoTransaccion(transaccion, ESTADO_ERROR, 
                    "Error en validación de fraude: " + e.getMessage());
            throw new TransaccionRechazadaException("Error en validación de fraude: " 
                    + e.getMessage());
        }
    }
    
    private void validarMarca(Transaccion transaccion) {
        log.debug("Validando tarjeta con la marca para transacción: {}", transaccion.getCodTransaccion());
        actualizarEstadoTransaccion(transaccion, ESTADO_VALIDACION_MARCA, 
                "Iniciando validación con marca de tarjeta");
        
        // Validar datos necesarios antes de enviar
        if (transaccion.getNumeroTarjeta() == null || transaccion.getNumeroTarjeta().trim().isEmpty()) {
            String mensaje = "Número de tarjeta no proporcionado";
            log.error(mensaje);
            actualizarEstadoTransaccion(transaccion, ESTADO_ERROR, mensaje);
            throw new TransaccionRechazadaException(mensaje);
        }
        
        if (transaccion.getCvv() == null || transaccion.getCvv().trim().isEmpty()) {
            String mensaje = "Código de seguridad (CVV) no proporcionado";
            log.error(mensaje);
            actualizarEstadoTransaccion(transaccion, ESTADO_ERROR, mensaje);
            throw new TransaccionRechazadaException(mensaje);
        }
        
        if (transaccion.getFechaCaducidad() == null || transaccion.getFechaCaducidad().trim().isEmpty()) {
            String mensaje = "Fecha de caducidad no proporcionada";
            log.error(mensaje);
            actualizarEstadoTransaccion(transaccion, ESTADO_ERROR, mensaje);
            throw new TransaccionRechazadaException(mensaje);
        }
        
        if (transaccion.getMonto() == null || transaccion.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            String mensaje = "Monto no válido";
            log.error(mensaje);
            actualizarEstadoTransaccion(transaccion, ESTADO_ERROR, mensaje);
            throw new TransaccionRechazadaException(mensaje);
        }
        
        // Registrar información de la transacción (ocultando datos sensibles)
        log.info("Datos de transacción para validar: numeroTarjeta={}****, codigoSeguridad=***, fechaExpiracion={}, monto={}, codigoUnico={}",
            transaccion.getNumeroTarjeta().substring(0, 4),
            transaccion.getFechaCaducidad(),
            transaccion.getMonto(),
            transaccion.getCodigoUnico());
        
        ValidacionMarcaRequest marcaRequest = new ValidacionMarcaRequest();
        marcaRequest.setNumeroTarjeta(transaccion.getNumeroTarjeta());
        marcaRequest.setCodigoSeguridad(transaccion.getCvv());
        marcaRequest.setFechaExpiracion(transaccion.getFechaCaducidad());
        marcaRequest.setMonto(transaccion.getMonto());
        marcaRequest.setCodigoUnicoTransaccion(transaccion.getCodigoUnico());
        
        log.debug("Enviando request a marca: numeroTarjeta={}****, codigoSeguridad=***, fechaExpiracion={}, monto={}, codigoUnico={}",
            marcaRequest.getNumeroTarjeta().substring(0, 4),
            marcaRequest.getFechaExpiracion(),
            marcaRequest.getMonto(),
            marcaRequest.getCodigoUnicoTransaccion());
        
        try {
            // Verificar usando Try-Catch específico para identificar mejor el error
            ValidacionMarcaResponse marcaResponse = null;
            try {
                log.debug("Enviando solicitud a ValidaMServerless en URL: {}/dev/api/v1/marca/validar", 
                        "${app.marca-service.url}");
                marcaResponse = marcaClient.validarTarjeta(marcaRequest);
            } catch (Exception e) {
                log.error("Error al llamar al servicio de marca: {}", e.getMessage(), e);
                
                // Comprobar si es un error de timeout
                if (e.getMessage() != null && e.getMessage().contains("Read timed out")) {
                    log.error("Timeout en la conexión con el servicio de marca");
                    actualizarEstadoTransaccion(transaccion, ESTADO_ERROR, 
                            "Timeout en la conexión con servicio de marca");
                    throw new TransaccionRechazadaException("Timeout en la conexión con servicio de marca");
                }
                
                // Si es otro tipo de error, registrarlo con más detalle
                actualizarEstadoTransaccion(transaccion, ESTADO_ERROR, 
                        "Error en comunicación con servicio de marca: " + e.getMessage());
                throw new TransaccionRechazadaException("Error en validación con marca: " + e.getMessage());
            }
            
            log.debug("Respuesta de marca recibida: {}", 
                marcaResponse != null ? marcaResponse.toString() : "null");
                
            // Log adicional para diagnosticar la validación
            if (marcaResponse != null) {
                log.debug("Detalles de validación: tarjetaValida={}, swiftBanco={}, mensaje={}, resultado isValida()={}",
                    marcaResponse.getTarjetaValida(),
                    marcaResponse.getSwiftBanco(),
                    marcaResponse.getMensaje() != null ? 
                        (marcaResponse.getMensaje().length() > 100 ? 
                            marcaResponse.getMensaje().substring(0, 100) + "..." : 
                            marcaResponse.getMensaje()) : 
                        "null",
                    marcaResponse.isValida());
            }
            
            if (marcaResponse == null) {
                log.error("Servicio de marca devolvió una respuesta nula");
                actualizarEstadoTransaccion(transaccion, ESTADO_ERROR, 
                        "Error en comunicación con servicio de marca: Respuesta nula");
                throw new TransaccionRechazadaException("Servicio de marca devolvió una respuesta nula");
            }
            
            // Verificar si la tarjeta es válida según la respuesta de la marca
            if (!marcaResponse.isValida()) {
                String razonRechazo;
                if (marcaResponse.getMensaje() != null && !marcaResponse.getMensaje().isEmpty()) {
                    // Si el mensaje contiene un error HTTP, extraemos solo la parte relevante
                    if (marcaResponse.getMensaje().contains("[404") || marcaResponse.getMensaje().contains("[400") || 
                            marcaResponse.getMensaje().contains("[500")) {
                        
                        if (marcaResponse.getMensaje().contains("Tarjeta no encontrada")) {
                            razonRechazo = "Tarjeta no encontrada en el sistema";
                        } else if (marcaResponse.getMensaje().toLowerCase().contains("incorrectos")) {
                            razonRechazo = "Datos de la tarjeta incorrectos";
                        } else {
                            razonRechazo = "Error en el servicio de validación de tarjeta";
                        }
                    } else {
                        razonRechazo = marcaResponse.getMensaje();
                    }
                } else {
                    razonRechazo = "Validación de tarjeta fallida";
                }
                
                String mensaje = "Tarjeta rechazada: " + razonRechazo;
                log.warn(mensaje);
                actualizarEstadoTransaccion(transaccion, ESTADO_RECHAZADA, mensaje);
                // No lanzamos excepción, simplemente retornamos para terminar el proceso aquí
                return;
            }
            
            // Actualizar la transacción con la información del banco emisor
            String swiftBanco = marcaResponse.getSwiftBanco();
            if (swiftBanco == null || swiftBanco.isEmpty()) {
                log.warn("Servicio de marca no devolvió el código SWIFT del banco, se usará valor por defecto");
                swiftBanco = "DESCONOCIDO";
            } else if ("N/A".equals(swiftBanco)) {
                log.warn("Servicio de marca devolvió N/A como código SWIFT, se usará valor por defecto");
                swiftBanco = "DESCONOCIDO";
            }
            
            log.info("Validación de marca exitosa para transacción: {} - Swift del banco: {}", 
                    transaccion.getCodTransaccion(), swiftBanco);
            
            // Actualizar la transacción con el SWIFT del banco
            transaccion.setSwiftBancoTarjeta(swiftBanco);
            actualizarEstadoTransaccion(transaccion, ESTADO_VALIDACION_FRAUDE, "Iniciando validación de fraude");
        } catch (TransaccionRechazadaException e) {
            // No hacemos nada especial con esta excepción, la propagamos hacia arriba
            throw e;
        } catch (Exception e) {
            log.error("Error general en validación con marca: {}", e.getMessage(), e);
            actualizarEstadoTransaccion(transaccion, ESTADO_ERROR, 
                    "Error general en validación con marca: " + e.getMessage());
            throw new TransaccionRechazadaException("Error general en validación con marca: " + e.getMessage());
        }
    }
    
    private void procesarDebitoTarjeta(Transaccion transaccion) {
        log.debug("Procesando débito a tarjeta para transacción: {}", transaccion.getCodTransaccion());
        actualizarEstadoTransaccion(transaccion, ESTADO_PROCESO_DEBITO, 
                "Iniciando proceso de débito a tarjeta");
        
        try {
            ProcesoBancarioRequest request = new ProcesoBancarioRequest();
            request.setNumeroTarjeta(transaccion.getNumeroTarjeta());
            request.setMonto(transaccion.getMonto());
            request.setCodigoUnico(transaccion.getCodigoUnico());
            request.setSwiftBancoTarjeta(transaccion.getSwiftBancoTarjeta());
            request.setReferencia(transaccion.getReferencia());
            
            // Agregar los campos relacionados con el comercio
            request.setSwiftBancoComercio(transaccion.getSwiftBancoComercio());
            request.setCuentaIbanComercio(transaccion.getCuentaIbanComercio());
            
            // Agregar campos obligatorios usando los valores del modelo
            request.setTipo(transaccion.getTipo() != null ? transaccion.getTipo() : "COM");
            request.setCodigoMoneda(transaccion.getCodigoMoneda() != null ? transaccion.getCodigoMoneda() : "USD");
            request.setPais(transaccion.getPais() != null ? transaccion.getPais() : "EC");
            request.setTransaccionEncriptada(transaccion.getTransaccionEncriptada());
            
            // Establecer los valores de diferido y cuotas desde el contexto
            Boolean esDiferido = TransaccionContextHolder.getDiferido();
            Integer numCuotas = TransaccionContextHolder.getCuotas();
            
            // Convertir boolean diferido a Integer (0 si es false, 1 si es true)
            if (esDiferido != null) {
                request.setDiferido(esDiferido ? 1 : 0);
            } else {
                request.setDiferido(0);  // 0 por defecto si no está definido
            }
            
            // Establecer número de cuotas, por defecto 1 si no está definido o es diferido=false
            if (numCuotas != null && (esDiferido == null || esDiferido)) {
                request.setCuotas(numCuotas);
            } else {
                request.setCuotas(1);  // 1 por defecto
            }
            
            ProcesoBancarioResponse response = bancoClient.procesarTransaccion(request);
            
            if (!"APROBADO".equals(response.getEstado())) {
                String mensaje = "Débito rechazado: " + response.getMensaje();
                log.error(mensaje);
                actualizarEstadoTransaccion(transaccion, ESTADO_RECHAZADA, mensaje);
                throw new TransaccionRechazadaException(mensaje);
            }
            
            log.info("Débito a tarjeta exitoso para transacción: {}", transaccion.getCodTransaccion());
                
        } catch (TransaccionRechazadaException e) {
            // Reenviar excepciones de rechazo
            throw e;
        } catch (Exception e) {
            log.error("Error general en proceso de débito: {}", e.getMessage(), e);
            String mensajeError = "Error inesperado en proceso de débito: " + 
                    (e.getMessage() != null ? e.getMessage() : "Error desconocido");
            actualizarEstadoTransaccion(transaccion, ESTADO_ERROR, mensajeError);
            throw new TransaccionRechazadaException(mensajeError);
        }
    }
    
    private void procesarCreditoComercio(Transaccion transaccion) {
        log.debug("Procesando crédito a comercio para transacción: {}", transaccion.getCodTransaccion());
        actualizarEstadoTransaccion(transaccion, ESTADO_PROCESO_CREDITO, 
                "Iniciando proceso de crédito a comercio");
        
        try {
            ProcesoBancarioRequest request = new ProcesoBancarioRequest();
            request.setNumeroTarjeta(transaccion.getNumeroTarjeta());
            request.setMonto(transaccion.getMonto());
            request.setCodigoUnico(transaccion.getCodigoUnico());
            request.setSwiftBancoTarjeta(transaccion.getSwiftBancoTarjeta());
            request.setReferencia(transaccion.getReferencia());
            
            // Agregar los campos relacionados con el comercio
            request.setSwiftBancoComercio(transaccion.getSwiftBancoComercio());
            request.setCuentaIbanComercio(transaccion.getCuentaIbanComercio());
            
            // Agregar campos obligatorios usando los valores del modelo
            request.setTipo(transaccion.getTipo() != null ? transaccion.getTipo() : "COM");
            request.setCodigoMoneda(transaccion.getCodigoMoneda() != null ? transaccion.getCodigoMoneda() : "USD");
            request.setPais(transaccion.getPais() != null ? transaccion.getPais() : "EC");
            request.setTransaccionEncriptada(transaccion.getTransaccionEncriptada());
            
            // Establecer los valores de diferido y cuotas desde el contexto
            Boolean esDiferido = TransaccionContextHolder.getDiferido();
            Integer numCuotas = TransaccionContextHolder.getCuotas();
            
            // Convertir boolean diferido a Integer (0 si es false, 1 si es true)
            if (esDiferido != null) {
                request.setDiferido(esDiferido ? 1 : 0);
            } else {
                request.setDiferido(0);  // 0 por defecto si no está definido
            }
            
            // Establecer número de cuotas, por defecto 1 si no está definido o es diferido=false
            if (numCuotas != null && (esDiferido == null || esDiferido)) {
                request.setCuotas(numCuotas);
            } else {
                request.setCuotas(1);  // 1 por defecto
            }
            
            ProcesoBancarioResponse response = bancoClient.procesarTransaccion(request);
            
            if (!"APROBADO".equals(response.getEstado())) {
                String mensaje = "Crédito rechazado: " + response.getMensaje();
                log.error(mensaje);
                actualizarEstadoTransaccion(transaccion, ESTADO_RECHAZADA, mensaje);
                throw new TransaccionRechazadaException(mensaje);
            }
            
            log.info("Crédito a comercio exitoso para transacción: {}", transaccion.getCodTransaccion());
            
        } catch (TransaccionRechazadaException e) {
            // Reenviar excepciones de rechazo
            throw e;
        } catch (Exception e) {
            log.error("Error general en proceso de crédito: {}", e.getMessage(), e);
            String mensajeError = "Error inesperado en proceso de crédito: " + 
                    (e.getMessage() != null ? e.getMessage() : "Error desconocido");
            actualizarEstadoTransaccion(transaccion, ESTADO_ERROR, mensajeError);
            throw new TransaccionRechazadaException(mensajeError);
        }
    }
    
    private void reversoDebitoTarjeta(Transaccion transaccion) {
        log.debug("Procesando reverso de débito para transacción: {}", transaccion.getCodTransaccion());
        actualizarEstadoTransaccion(transaccion, ESTADO_PROCESO_REVERSO, 
                "Iniciando proceso de reverso de débito");
        
        try {
            ProcesoBancarioRequest request = new ProcesoBancarioRequest();
            request.setNumeroTarjeta(transaccion.getNumeroTarjeta());
            request.setMonto(transaccion.getMonto());
            request.setCodigoUnico("REV-" + transaccion.getCodigoUnico());
            request.setSwiftBancoTarjeta(transaccion.getSwiftBancoTarjeta());
            request.setReferencia("REVERSO-" + transaccion.getReferencia());
            
            // Agregar los campos relacionados con el comercio
            request.setSwiftBancoComercio(transaccion.getSwiftBancoComercio());
            request.setCuentaIbanComercio(transaccion.getCuentaIbanComercio());
            
            // Agregar campos obligatorios usando los valores del modelo
            request.setTipo(transaccion.getTipo() != null ? transaccion.getTipo() : "REV");
            request.setCodigoMoneda(transaccion.getCodigoMoneda() != null ? transaccion.getCodigoMoneda() : "USD");
            request.setPais(transaccion.getPais() != null ? transaccion.getPais() : "EC");
            request.setTransaccionEncriptada(transaccion.getTransaccionEncriptada() != null ? 
                    transaccion.getTransaccionEncriptada() : "ENCRIPTADO-REV-" + transaccion.getCodigoUnico());
            
            // Establecer los valores de diferido y cuotas desde el contexto
            Boolean esDiferido = TransaccionContextHolder.getDiferido();
            Integer numCuotas = TransaccionContextHolder.getCuotas();
            
            // Convertir boolean diferido a Integer (0 si es false, 1 si es true)
            if (esDiferido != null) {
                request.setDiferido(esDiferido ? 1 : 0);
            } else {
                request.setDiferido(0);  // 0 por defecto si no está definido
            }
            
            // Establecer número de cuotas, por defecto 1 si no está definido o es diferido=false
            if (numCuotas != null && (esDiferido == null || esDiferido)) {
                request.setCuotas(numCuotas);
            } else {
                request.setCuotas(1);  // 1 por defecto
            }
            
            ProcesoBancarioResponse response = bancoClient.procesarTransaccion(request);
            
            if (!"APROBADO".equals(response.getEstado())) {
                String mensaje = "Reverso rechazado: " + response.getMensaje();
                log.error(mensaje);
                actualizarEstadoTransaccion(transaccion, ESTADO_ERROR_IRRECUPERABLE, mensaje);
                throw new TransaccionRechazadaException(mensaje);
            }
            
            log.info("Reverso de débito exitoso para transacción: {}", transaccion.getCodTransaccion());
            actualizarEstadoTransaccion(transaccion, ESTADO_REVERSADA, 
                    "Transacción reversada exitosamente");
            
        } catch (Exception e) {
            log.error("Error grave en proceso de reverso: {}", e.getMessage(), e);
            String mensajeError = "Error inesperado en proceso de reverso: " + 
                    (e.getMessage() != null ? e.getMessage() : "Error desconocido");
            actualizarEstadoTransaccion(transaccion, ESTADO_ERROR_IRRECUPERABLE, mensajeError);
            throw new TransaccionRechazadaException(mensajeError);
        }
    }

    public Transaccion obtenerTransaccionPorCodigo(String codTransaccion) {
        log.info("Buscando transacción con código: {}", codTransaccion);
        
        Transaccion transaccionRedis = redisService.getTransaccionByCodigo(codTransaccion);
        if (transaccionRedis != null) {
            log.info("Transacción encontrada en Redis: {}", transaccionRedis.getCodTransaccion());
            return transaccionRedis;
        }
        
        return transaccionRepository.findByCodTransaccion(codTransaccion)
                .orElseThrow(() -> new NotFoundException("Transacción no encontrada con código: " + codTransaccion));
    }

    public List<Transaccion> buscarTransaccionesPorFecha(LocalDateTime desde, LocalDateTime hasta) {
        log.info("Buscando transacciones entre {} y {}", desde, hasta);
        return transaccionRepository.findByFechaTransaccionBetweenOrderByFechaTransaccionDesc(desde, hasta);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void actualizarEstadoTransaccion(Transaccion transaccion, String estado, String mensaje) {
        // Asegurarse de que la transacción tenga un ID antes de guardar
        if (transaccion.getId() == null) {
            log.error("No se puede actualizar una transacción sin ID");
            throw new RuntimeException("Intento de actualizar transacción sin ID");
        }
        
        try {
            // Actualizar en PostgreSQL primero
            transaccion.setEstado(estado);
            Transaccion transaccionActualizada = transaccionRepository.save(transaccion);
            log.info("Transacción con ID {} actualizada en PostgreSQL con estado: {}", transaccion.getId(), estado);
            
            // Registrar historial sólo si la actualización en PostgreSQL fue exitosa
            try {
                registrarHistorialEstado(transaccion, estado, mensaje);
            } catch (Exception e) {
                log.error("Error al registrar historial de estado: {}", e.getMessage(), e);
                // Continuamos el proceso aunque falle el registro del historial
            }
            
            // MODIFICADO: Actualizar en Redis para TODOS los estados, incluyendo estados de error
            // Esto mantiene las transacciones con error en Redis para análisis de fraude
            try {
        redisService.updateTransaccion(transaccion);
                log.info("Transacción actualizada en Redis con estado {}: {}", estado, transaccion.getCodTransaccion());
            } catch (Exception e) {
                log.error("Error al actualizar transacción en Redis: {}", e.getMessage(), e);
            }
        
        log.info("Estado de transacción actualizado a: {} - {}", estado, mensaje);
        } catch (Exception e) {
            log.error("Error al actualizar estado de transacción en PostgreSQL: {}", e.getMessage(), e);
            throw new RuntimeException("Error al actualizar transacción en base de datos", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void registrarHistorialEstado(Transaccion transaccion, String estado, String mensaje) {
        // Validación de entradas
        if (transaccion == null || transaccion.getCodTransaccion() == null) {
            log.error("No se puede registrar historial sin una transacción o código válido");
            return;
        }
        
        // Truncar mensaje si es necesario (máximo 200 caracteres)
        String mensajeTruncado = null;
        if (mensaje != null) {
            if (mensaje.length() > 200) {
                mensajeTruncado = mensaje.substring(0, 197) + "...";
                log.debug("Mensaje truncado por exceder longitud máxima: {}", mensajeTruncado);
            } else {
                mensajeTruncado = mensaje;
            }
        } else {
            mensajeTruncado = "Sin información adicional";
        }
        
        try {
        HistorialEstadoTransaccion historial = new HistorialEstadoTransaccion();
        historial.setCodHistorialEstado(UUID.randomUUID().toString().substring(0, 10));
        historial.setCodTransaccion(transaccion.getCodTransaccion());
        historial.setEstado(estado);
            historial.setMensaje(mensajeTruncado);
        historial.setFechaEstadoCambio(LocalDateTime.now());
        historialRepository.save(historial);
            log.debug("Historial de estado registrado: {} - {}", estado, mensajeTruncado);
        } catch (Exception e) {
            log.error("Error al registrar historial de estado: {} - {}", estado, e.getMessage(), e);
        }
    }

    private String obtenerMarcaTarjeta(String numeroTarjeta) {
        if (numeroTarjeta.startsWith("4")) return "VISA";
        if (numeroTarjeta.startsWith("5")) return "MASTERCARD";
        if (numeroTarjeta.startsWith("34") || numeroTarjeta.startsWith("37")) return "AMEX";
        return "DESCONOCIDA";
    }

    public List<Transaccion> findAll() {
        return transaccionRepository.findAll();
    }

    public List<Transaccion> findByNumeroTarjeta(String numeroTarjeta) {
        return transaccionRepository.findByNumeroTarjeta(numeroTarjeta);
    }

    public List<Transaccion> findByEstado(String estado) {
        return transaccionRepository.findByEstado(estado);
    }

    /**
     * Obtiene el último mensaje registrado para una transacción
     * @param transaccion La transacción de la que se desea obtener el último mensaje
     * @return El último mensaje registrado o null si no hay mensajes
     */
    public String obtenerUltimoMensaje(Transaccion transaccion) {
        if (transaccion == null || transaccion.getCodTransaccion() == null) {
            return null;
        }
        
        try {
            // Obtener el historial de estados más reciente por código de transacción
            List<HistorialEstadoTransaccion> historial = historialRepository.findByCodTransaccionOrderByFechaEstadoCambioDesc(
                    transaccion.getCodTransaccion());
            
            if (historial != null && !historial.isEmpty()) {
                // Retornar el mensaje del estado más reciente
                return historial.get(0).getMensaje();
            }
        } catch (Exception e) {
            log.error("Error al obtener último mensaje de transacción: {}", e.getMessage(), e);
        }
        
        return null;
    }
} 